/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 */
package io.greenscreens.quark.websocket;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.greenscreens.quark.JsonDecoder;
import io.greenscreens.quark.QuarkEngine;
import io.greenscreens.quark.cdi.BeanManagerUtil;
import io.greenscreens.quark.cdi.IDestructibleBeanInstance;
import io.greenscreens.quark.cdi.Required;
import io.greenscreens.quark.ext.ExtEncrypt;
import io.greenscreens.quark.ext.ExtJSDirectRequest;
import io.greenscreens.quark.ext.ExtJSDirectResponse;
import io.greenscreens.quark.ext.ExtJSObjectResponse;
import io.greenscreens.quark.ext.ExtJSResponse;
import io.greenscreens.quark.ext.annotations.ExtJSActionLiteral;
import io.greenscreens.quark.ext.annotations.ExtJSDirect;
import io.greenscreens.quark.ext.annotations.ExtJSMethod;
import io.greenscreens.quark.security.IAesKey;
import io.greenscreens.quark.security.Security;
import io.greenscreens.quark.web.QuarkConstants;
import io.greenscreens.quark.web.QuarkErrors;
import io.greenscreens.quark.websocket.data.WebSocketInstruction;

/**
 * Attach Java class to remote call
 */
public final class WebSocketOperations<T> {

	// private static final String [] ALL_PATHS = { "*" };
	private static final Logger LOG = LoggerFactory.getLogger(WebSocketOperations.class);

	private boolean requiredSession = false;

	public void setRequiredSession(boolean requiredSession) {
		this.requiredSession = requiredSession;
	}

	/**
	 * Decrypt RSA/AES data from web, but only if AES in session is not found If new
	 * AES, store to session
	 * 
	 * @param session
	 * @param encrypt
	 * @return
	 * @throws Exception
	 */
	private String decryptData(final WebSocketSession session, final ExtEncrypt encrypt) throws Exception {

		IAesKey crypt = (IAesKey) session.getUserProperties().get(QuarkConstants.HTTP_SEESION_ENCRYPT);
		String data = null;

		if (crypt == null) {
			crypt = Security.initAESfromRSA(encrypt.getK());
			session.getUserProperties().put(QuarkConstants.HTTP_SEESION_ENCRYPT, crypt);
			data = crypt.decrypt(encrypt.getD());
		} else {
			data = Security.decodeRequest(encrypt.getD(), encrypt.getK(), crypt);
		}

		return data;
	}

	/**
	 * Decrypt encrypted JSON data and continue as normal
	 * 
	 * @param request
	 * @param session
	 * @param uri
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ExtJSDirectResponse<T> processEncrypted(final ExtJSDirectRequest<T> request,
			final WebSocketSession session, final String uri) {

		ExtJSDirectResponse<T> directResponse = null;
		ExtJSResponse response = null;
		boolean err = false;

		try {

			final int size = request.getData() == null ? 0 : request.getData().size();

			if (size == 0) {
				response = new ExtJSResponse(false, QuarkErrors.E0000.getMessage());
				response.setCode(QuarkErrors.E0000.getCode());
			} else {

				final Object paramData = request.getData().get(0);

				if (paramData instanceof JsonNode) {

					request.getData().clear();

					final JsonNode jnode = (JsonNode) paramData;
					final ObjectMapper mapper = JsonDecoder.getJSONEngine();
					final ExtEncrypt encrypt = mapper.treeToValue(jnode, ExtEncrypt.class);
					final String data = decryptData(session, encrypt);
					final JsonNode node = JsonDecoder.parse(data);

					if (node.isArray()) {

						final ArrayNode arr = (ArrayNode) node;
						final Iterator<JsonNode> it = arr.iterator();

						while (it.hasNext()) {
							request.getData().add((T) it.next());
						}

					} else {
						request.getData().add((T) node);
					}

					directResponse = process(request, session.getHttpSession(), uri);

				} else {
					response = new ExtJSResponse(false, QuarkErrors.E0000.getMessage());
					response.setCode(QuarkErrors.E0000.getCode());
				}
			}

		} catch (Exception e) {

			err = true;
			response = new ExtJSResponse(e, e.getMessage());

			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);

		} finally {

			if (directResponse == null) {
				directResponse = new ExtJSDirectResponse<T>(request, response);
				if (err) {
					directResponse.setType(WebSocketInstruction.ERR.getText());
				}
			}
		}

		return directResponse;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ExtJSDirectResponse<T> process(final ExtJSDirectRequest<T> request, final HttpSession session,
			final String uri) {

		ExtJSDirectResponse<T> directResponse = null;
		ExtJSResponse response = null;
		boolean err = false;

		try {

			final Bean<?> bean = findBean(request);
			final Class<?> beanClass = bean.getBeanClass();

			final AnnotatedType annType = QuarkEngine.getBeanManager().createAnnotatedType(beanClass);
			final AnnotatedMethod selectedMethod = findMethod(request, annType);
			final ExtJSDirect direct = beanClass.getAnnotation(ExtJSDirect.class);

			boolean error = checkForError(annType, selectedMethod, direct, session, uri);

			if (error) {
				response = new ExtJSResponse(false, QuarkErrors.E0001.getString());
				response.setCode(QuarkErrors.E0001.getCode());
			} else {
				final List<AnnotatedParameter<?>> paramList = selectedMethod.getParameters();
				final Object[] params = fillParams(request, paramList);

				error = isParametersInvalid(paramList, params);
				if (error) {
					response = new ExtJSResponse(false, QuarkErrors.E0002.getMessage());
					response.setCode(QuarkErrors.E0002.getCode());
				} else {										
					response = executeBean(bean, selectedMethod, params);
				}
			}

		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
			response = new ExtJSResponse(e, e.getMessage());
			err = true;
		} finally {

			directResponse = new ExtJSDirectResponse<T>(request, response);

			if (err) {
				directResponse.setType(WebSocketInstruction.ERR.getText());
			}
		}

		return directResponse;
	}

	/// PRIVATE SECTION

	private boolean checkForError(final AnnotatedType<?> annType, final AnnotatedMethod<?> selectedMethod,
			final ExtJSDirect direct, final HttpSession session, final String uri) {

		boolean error = false;

		// check for path
		if (direct == null) {
			error = true;
		} else if (!checkPath(uri, direct.paths())) {
			error = true;
		} else if (requiredSession && !isValidHttpSession(session)) {
			error = true;
		} else if (selectedMethod == null) {
			error = true;
		}
		
		return error;
	}

	private boolean checkPath(final String uri, final String[] paths) {

		boolean result = false;

		for (String path : paths) {

			if ("*".equals(path)) {
				result = true;
				break;
			}

			final int idx = uri.indexOf(path);
			if (idx == 0 || idx == 1) {
				result = true;
				break;
			}
		}
		return result;
	}

	private boolean isValidHttpSession(final HttpSession httpSession) {

		if (httpSession == null) {
			return false;
		}

		final String attr = (String) httpSession.getAttribute(QuarkConstants.HTTP_SEESION_STATUS);
		return Boolean.TRUE.toString().equalsIgnoreCase(attr);
	}

	private Bean<?> findBean(final ExtJSDirectRequest<?> request) {
		final ExtJSActionLiteral literal = new ExtJSActionLiteral(request.getNamespace(), request.getAction());
		final Iterator<Bean<?>> it = QuarkEngine.getBeanManager().getBeans(Object.class, literal).iterator();
		return it.next();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private AnnotatedMethod<?> findMethod(final ExtJSDirectRequest<T> request, final AnnotatedType annType) {

		AnnotatedMethod<?> selectedMethod = null;
		final Set<AnnotatedMethod<?>> aMethods = annType.getMethods();

		for (final AnnotatedMethod<?> aMethod : aMethods) {

			final ExtJSMethod annMethod = aMethod.getAnnotation(ExtJSMethod.class);

			if (annMethod == null) {
				continue;
			}

			if (annMethod.value().equals(request.getMethod())) {
				selectedMethod = aMethod;
				break;
			}
		}

		return selectedMethod;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object[] fillParams(final ExtJSDirectRequest<T> request, final List<AnnotatedParameter<?>> methodParams)
			throws IOException {

		final int paramSize = methodParams.size();
		final int incomingParamsSize = request.getData() == null ? 0 : request.getData().size();

		final ObjectMapper mapper = JsonDecoder.getJSONEngine();

		final Object[] params = new Object[paramSize];

		for (int i = 0; i < paramSize; i++) {

			if (i < incomingParamsSize) {

				final Object paramData = request.getData().get(i);

				if (paramData instanceof JsonNode) {

					final JsonNode jnode = (JsonNode) paramData;

					if (jnode != null) {

						Class<?> jType = null;

						AnnotatedParameter<?> param = methodParams.get(i);
						Type type = param.getBaseType();

						if (type instanceof ParameterizedType) {
							ParameterizedType ptype = (ParameterizedType) param.getBaseType();

							Type rtype = ptype.getRawType();
							if (Collection.class.isAssignableFrom((Class<?>) rtype)) {

								Type t1 = ptype.getActualTypeArguments()[0];
								Class<?> gen = null;

								try {
									gen = Class.forName(getClassName(t1));
									Collection l = createListOfType((Class) rtype, gen);
									params[i] = l;

									ArrayNode anode = (ArrayNode) jnode;

									for (JsonNode node : anode) {
										l.add(mapper.treeToValue(node, gen));
									}

								} catch (Exception e) {
									LOG.error(e.getMessage());
									LOG.trace(e.getMessage(), e);
								}

							} else {
								jType = (Class<?>) param.getBaseType();
								params[i] = mapper.treeToValue(jnode, jType);
							}

						} else {
							jType = (Class<?>) param.getBaseType();
							params[i] = mapper.treeToValue(jnode, jType);
						}

					}

				} else {
					params[i] = paramData;
				}
			}
		}

		return params;
	}

	static String NAME_PREFIX = "class ";

	private static String getClassName(Type type) {
		String fullName = type.toString();
		if (fullName.startsWith(NAME_PREFIX))
			return fullName.substring(NAME_PREFIX.length());
		return fullName;
	}

	@SuppressWarnings("unchecked")
	public static <T> Collection<T> createListOfType(Class<?> collection, T type)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {

		if (collection.isInterface()) {
			return new ArrayList<T>();
		}

		if (Modifier.isAbstract(collection.getModifiers())) {
			return new ArrayList<T>();
		}

		// Collection<T> tmp = (Collection<T>) collection.newInstance();
		return (Collection<T>) collection.getDeclaredConstructor().newInstance();
	}

	/**
	 * Check for @Required annotation,in such case, parameter can't be null
	 * 
	 * @param paramList
	 * @param params
	 * @return
	 */
	private boolean isParametersInvalid(List<AnnotatedParameter<?>> paramList, Object[] params) {

		boolean sts = false;
		Required req = null;
		int i = 0;

		for (AnnotatedParameter<?> param : paramList) {
			req = param.getAnnotation(Required.class);
			if (req != null && params[i] == null) {
				sts = true;
				break;
			}
			i++;
		}

		return sts;
	}

	private ExtJSResponse executeBean(final Bean<?> bean, final AnnotatedMethod<?> method, final Object[] params) {

		ExtJSResponse response = null;
		IDestructibleBeanInstance<?> di = null;

		try {
			final Method javaMethod = method.getJavaMember();
			// final AnnotatedType [] types = javaMethod.getAnnotatedParameterTypes();

			di = QuarkEngine.of(BeanManagerUtil.class).getDestructibleBeanInstance(bean);
			final Object beanInstance = di.getInstance();

			if (javaMethod.isAccessible()) {
			//if (javaMethod.canAccess(beanInstance)) {
				javaMethod.setAccessible(true);
			}

			validateParameters(beanInstance, javaMethod, params);						
			
			Class<?> clazz = javaMethod.getReturnType();
			Object obj = javaMethod.invoke(beanInstance, params);

			if (void.class == clazz || Void.class == clazz) {
				response = new ExtJSResponse(true, null);
			} else if (ExtJSResponse.class.isAssignableFrom(clazz)) {
				response = (ExtJSResponse) obj;
			} else {
				ExtJSObjectResponse<Object> objResponse = new ExtJSObjectResponse<>();
				objResponse.setData(obj);
				response = objResponse;
			}
			// response = (ExtJSResponse) javaMethod.invoke(beanInstance, params);

		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
			response = new ExtJSResponse(e, e.getMessage());
		} finally {

			if (di != null) {
				di.release();
			}

		}

		return response;
	}

	/**
	 * Helper method to validate calling method arguments annotated with JSR-380
	 * @param instance
	 * @param method
	 * @param params
	 * @throws Exception
	 */
	public void validateParameters(final Object instance, final Method method, final Object[] params) throws Exception {

		final ExtJSMethod annMethod = method.getAnnotation(ExtJSMethod.class);
		if (!annMethod.validate()) {
			return;
		}
		
		final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		final ExecutableValidator validator = factory.getValidator().forExecutables();
		final Set<ConstraintViolation<Object>> violations = validator.validateParameters(instance, method, params);
		
		if (!violations.isEmpty()) {
			final StringBuilder builder = new StringBuilder();
			
			for (ConstraintViolation<Object> violation : violations) {
				builder.append(violation.getMessage());
				builder.append("\n");
			}

			factory.close();
			throw new Exception(builder.toString().trim());
		}
		
		factory.close();
		
	}

}
