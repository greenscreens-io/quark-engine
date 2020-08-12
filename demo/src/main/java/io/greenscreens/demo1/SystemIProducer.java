/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 *
 * https://www.greenscreens.io
 *
 */
package io.greenscreens.demo1;

import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application level instance used to provide AS400 object / httpsession
 */
@ApplicationScoped
public class SystemIProducer implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(SystemIProducer.class);

	@Inject
	private HttpServletRequest servletRequest;

	/**
	 * AS400 session level producer with default attribute name based on AS400 full class name
	 * @param ip
	 * @return
	 */
	@Produces
	public SystemI producer(final InjectionPoint ip) {

		final Annotated annotated = ip.getAnnotated();
		final boolean check = annotated.isAnnotationPresent(Authenticated.class);

		if (!annotated.isAnnotationPresent(SessionAttribute.class)) {
			return produce(ip, SystemI.class.getCanonicalName(), check);
		}

		final SessionAttribute sa = annotated.getAnnotation(SessionAttribute.class);
		final String key = Optional.of(sa.value()).filter(s -> !s.isEmpty())
			.orElse(SystemI.class.getCanonicalName()).toString();

		return produce(ip, key, check);
	}

	/**
	 * Internal producer method produce as400 object from given attribute name
	 * @param ip
	 * @param name
	 * @return
	 */
	private SystemI produce(final InjectionPoint ip, final String name, final boolean check) {

		final HttpSession session = servletRequest.getSession();

		SystemI as400 = (SystemI) session.getAttribute(name);
		if (as400 == null) {
			as400 = getInstance();
			session.setAttribute(name, as400);
		}

		if(check && !as400.isValid()) {
			throw new RuntimeException("User not verified!");
		}

		return as400;
	}

	/**
	 * Create new AS400 instance and disable GUi prompt for server side usage
	 * @return
	 */
	private SystemI getInstance() {

		final SystemI as400 = new SystemI();

		try {
			as400.setGuiAvailable(false);
		} catch (PropertyVetoException e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		}

		return as400;
	}

}
