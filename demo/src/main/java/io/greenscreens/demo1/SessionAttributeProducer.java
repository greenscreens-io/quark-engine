/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 */
package io.greenscreens.demo1;

import java.beans.PropertyVetoException;
import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.as400.access.AS400;

/**
 * Application level instance used to provide AS400 object / httpsession
 */
@ApplicationScoped
public class SessionAttributeProducer implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(SessionAttributeProducer.class);	
	
	@Inject
	private HttpServletRequest servletRequest;

	/**
	 * AS400 session level producer with default attribute name based on AS400 full class name
	 * @param ip
	 * @return
	 */
	@Produces
	@Default
	public AS400 sessionAS400AttributeDefault(final InjectionPoint ip) {
		return produce(ip, AS400.class.getCanonicalName());
	}
	
	/**
	 * AS400 session level producer with custom attribute name
	 * @param ip
	 * @return
	 */
	@Produces
	@SessionAttribute
	public AS400 sessionAS400Attribute(final InjectionPoint ip) {
		final SessionAttribute sa = ip.getAnnotated().getAnnotation(SessionAttribute.class);
		return produce(ip, sa.value());
	}

	/**
	 * Internal producer method produce as400 object from given attribute name
	 * @param ip
	 * @param name
	 * @return
	 */
	private AS400 produce(final InjectionPoint ip, final String name) {

		final HttpSession session = servletRequest.getSession();
		
		AS400 as400 = (AS400) session.getAttribute(name);
		if (as400 == null) {
			as400 = getInstance();
			session.setAttribute(name, as400);
		}

		return as400;
	}

	/**
	 * Create new AS400 instance and disable GUi prompt for server side usage
	 * @return
	 */
	private AS400 getInstance() {
		final AS400 as400 = new AS400();
		try {
			as400.setGuiAvailable(false);
		} catch (PropertyVetoException e) {
			LOG.error(e.getMessage());
			LOG.debug(e.getMessage(), e);
		}
		return as400;
	}
}
