/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.quark.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.greenscreens.quark.JsonDecoder;
import io.greenscreens.quark.QuarkEngine;
import io.greenscreens.quark.ext.annotations.ExtJSAction;
import io.greenscreens.quark.ext.annotations.ExtJSDirect;
import io.greenscreens.quark.ext.annotations.ExtJSDirectLiteral;
import io.greenscreens.quark.ext.annotations.ExtJSMethod;

/**
 * Singleton class used to find CDI bean and wraps it into destructible
 * instance. It is used as an internal bean finder.
 */
@ApplicationScoped
public class BeanManagerUtil {

	private ArrayNode API;

	@PostConstruct
	public void init() {
		getAPI();
	}
	
	/**
	 * Retrieve engine meta structure for web
	 * 
	 * @return
	 */
	public ArrayNode getAPI() {
		if (API == null) {
			build();
		}
		return API;
	}

	/**
	 * Finds CDI bean by class type and defined qualifier annotations
	 * 
	 * @param type       - class type implemented in CDI bean
	 * @param qualifiers - additional bean qualifier
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> IDestructibleBeanInstance<T> getDestructibleBeanInstance(final Class<T> type,
			final Annotation... qualifiers) {

		final Set<Bean<?>> beansF = new HashSet<Bean<?>>();
		final Set<Bean<?>> beans = QuarkEngine.getBeanManager().getBeans(Object.class, qualifiers);
		
		final Iterator<Bean<?>> iterator = beans.iterator();

		while (iterator.hasNext()) {

			final Bean<?> bean = iterator.next();

			if (type.isInterface()) {

				final Class<?>[] intfs = bean.getBeanClass().getInterfaces();

				for (final Class<?> intf : intfs) {

					if (type.equals(intf)) {
						beansF.add(bean);
					}

				}

			} else {

				if (bean.getBeanClass().equals(type)) {
					beansF.add(bean);
				}

			}
		}

		final Bean<T> bean = (Bean<T>) QuarkEngine.getBeanManager().resolve(beansF);
		return getDestructibleBeanInstance(bean);
	}

	/** 
	 * Wraps CDI bean into custom destructible instance
	 * 
	 * @param bean
	 * @return
	 */
	public <T> IDestructibleBeanInstance<T> getDestructibleBeanInstance(final Bean<T> bean) {

		IDestructibleBeanInstance<T> result = null;

		if (bean != null) {

			final CreationalContext<T> creationalContext = QuarkEngine.getBeanManager().createCreationalContext(bean);

			if (creationalContext != null) {
				final T instance = bean.create(creationalContext);
				result = new DestructibleBeanInstance<T>(instance, bean, creationalContext);
			}

		}

		return result;
	}

	/**
	 * Build meta structure for web
	 * 
	 * @return
	 */
	public ArrayNode build() {

		final ArrayNode root = JsonNodeFactory.instance.arrayNode();
		ObjectNode objectNode = null;
		ArrayNode methodsNode = null;

		final Set<Bean<?>> beans = QuarkEngine.getBeanManager().getBeans(Object.class, new ExtJSDirectLiteral(null));
		for (Bean<?> bean : beans) {

			final Class<?> clazz = bean.getBeanClass();

			final ExtJSDirect extJSDirect = clazz.getAnnotation(ExtJSDirect.class);
			final ExtJSAction extJSAction = clazz.getAnnotation(ExtJSAction.class);

			final JsonNode paths = JsonDecoder.getJSONEngine().valueToTree(extJSDirect.paths());

			objectNode = JsonNodeFactory.instance.objectNode();
			objectNode.put("namespace", extJSAction.namespace());
			objectNode.put("action", extJSAction.action());
			objectNode.set("paths", paths);
			methodsNode = objectNode.putArray("methods");

			ExtJSMethod extJSMethod = null;
			final Method[] methods = clazz.getMethods();

			for (Method method : methods) {
				extJSMethod = method.getAnnotation(ExtJSMethod.class);
				if (extJSMethod != null) {
					ObjectNode objNode = JsonNodeFactory.instance.objectNode();
					methodsNode.add(objNode);
					objNode.put("name", extJSMethod.value());
					objNode.put("len", method.getParameterCount());
					if (!extJSMethod.encrypt()) {
						objNode.put("encrypt", false);
					}
				}
			}

			root.add(objectNode);

		}

		API = root;
		return root;

	}
	
}
