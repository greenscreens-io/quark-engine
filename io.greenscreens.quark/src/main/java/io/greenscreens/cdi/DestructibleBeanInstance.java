/*
 * Copyright (C) 2015, 2020  Green Screens Ltd.
 * 
 * https://www.greenscreens.io
 * 
 */
package io.greenscreens.cdi;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

/**
 * Wrapper class for CDI bean so we can destroy it programmatically
 * 
 * @param <T>
 */
class DestructibleBeanInstance<T> implements IDestructibleBeanInstance<T> {

	private final T instance;
	private final Bean<T> bean;
	private final CreationalContext<T> context;

	/**
	 * Creates a new instance
	 * 
	 * @param instance - CDI bean instance
	 * @param bean     - CDI bean found by BeanManager
	 * @param context  - current CDI context to work with; must be set to be able to
	 *                 destroy bean instance later
	 */
	public DestructibleBeanInstance(final T instance, final Bean<T> bean, final CreationalContext<T> context) {
		this.instance = instance;
		this.bean = bean;
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see IDestructibleBeanInstance#getInstance()
	 */
	@Override
	public T getInstance() {
		return instance;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see IDestructibleBeanInstance#destroy()
	 */
	@Override
	public void release() {

		if (instance != null) {
			bean.destroy(instance, context);
		}
	}

	/**
	 * Returns class type of CDI bean
	 */
	public Class<?> getBeanClass() {
		return bean.getBeanClass();
	}

	/**
	 * Returns CDI bean found by BeanManager
	 */
	@Override
	public Bean<T> getBean() {
		return bean;
	}

	@Override
	public String toString() {
		return "DestructibleBeanInstance [instance=" + instance + ", bean=" + bean + ", context=" + context + "]";
	}

}
