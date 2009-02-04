package gs.web.api;

//package org.springframework.web.servlet.mvc.annotation;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.handler.AbstractDetectingUrlHandlerMapping;

public class GSDefaultAnnotationHandlerMapping extends AbstractDetectingUrlHandlerMapping {

	private boolean useDefaultSuffixPattern = true;

	private final Map<Class, RequestMapping> cachedMappings = new HashMap<Class, RequestMapping>();


	/**
	 * Set whether to register paths using the default suffix pattern as well:
	 * i.e. whether "/users" should be registered as "/users.*" too.
	 * <p>Default is "true". Turn this convention off if you intend to interpret
	 * your <code>@RequestMapping</code> paths strictly.
	 * <p>Note that paths which include a ".xxx" suffix already will not be
	 * transformed using the default suffix pattern in any case.
	 */
	public void setUseDefaultSuffixPattern(boolean useDefaultSuffixPattern) {
		this.useDefaultSuffixPattern = useDefaultSuffixPattern;
        System.out.println ("DEBUG: setUseDefaultSuffixPattern: " + useDefaultSuffixPattern);
	}


	/**
	 * Checks for presence of the {@link org.springframework.web.bind.annotation.RequestMapping}
	 * annotation on the handler class and on any of its methods.
	 */
	protected String[] determineUrlsForHandler(String beanName) {
        System.out.println ("DEBUG: in determineUrlsForHandlers: " + beanName);
		ApplicationContext context = getApplicationContext();
		Class<?> handlerType = context.getType(beanName);
		RequestMapping mapping = AnnotationUtils.findAnnotation(handlerType, RequestMapping.class);
        System.out.println ("DEBUG: in determineUrlsForHandlers 2: " + mapping);
		if (mapping == null && context instanceof ConfigurableApplicationContext &&
				context.containsBeanDefinition(beanName)) {
			ConfigurableApplicationContext cac = (ConfigurableApplicationContext) context;
			BeanDefinition bd = cac.getBeanFactory().getMergedBeanDefinition(beanName);
			if (bd instanceof AbstractBeanDefinition) {
				AbstractBeanDefinition abd = (AbstractBeanDefinition) bd;
				if (abd.hasBeanClass()) {
					Class<?> beanClass = abd.getBeanClass();
					mapping = AnnotationUtils.findAnnotation(beanClass, RequestMapping.class);
				}
			}
		}
        System.out.println ("DEBUG: in determineUrlsForHandlers 3: " + mapping);
		if (mapping != null) {
			// @RequestMapping found at type level
			this.cachedMappings.put(handlerType, mapping);
			Set<String> urls = new LinkedHashSet<String>();
			String[] paths = mapping.value();
			if (paths.length > 0) {
				// @RequestMapping specifies paths at type level
				for (String path : paths) {
					addUrlsForPath(urls, path);
				}
				return StringUtils.toStringArray(urls);
			}
			else {
				// actual paths specified by @RequestMapping at method level
				return determineUrlsForHandlerMethods(handlerType);
			}
		}
		else if (AnnotationUtils.findAnnotation(handlerType, Controller.class) != null) {
			// @RequestMapping to be introspected at method level
			return determineUrlsForHandlerMethods(handlerType);
		}
		else {
			return null;
		}
	}

	/**
	 * Derive URL mappings from the handler's method-level mappings.
	 * @param handlerType the handler type to introspect
	 * @return the array of mapped URLs
	 */
	protected String[] determineUrlsForHandlerMethods(Class<?> handlerType) {
        System.out.println ("DEBUG: in determineUrlsForHandlerMethods: " + handlerType);
		final Set<String> urls = new LinkedHashSet<String>();
		ReflectionUtils.doWithMethods(handlerType, new ReflectionUtils.MethodCallback() {
			public void doWith(Method method) {
				RequestMapping mapping = method.getAnnotation(RequestMapping.class);
				if (mapping != null) {
					String[] mappedPaths = mapping.value();
					for (int i = 0; i < mappedPaths.length; i++) {
						addUrlsForPath(urls, mappedPaths[i]);
					}
				}
			}
		});
        System.out.println ("DEBUG: in determineUrlsForHandlerMethods 2: " + urls);
		return StringUtils.toStringArray(urls);
	}

	/**
	 * Add URLs and/or URL patterns for the given path.
	 * @param urls the Set of URLs for the current bean
	 * @param path the currently introspected path
	 */
	protected void addUrlsForPath(Set<String> urls, String path) {
        System.out.println ("DEBUG: addUrlsForPath 1: " + urls);
        System.out.println ("DEBUG: addUrlsForPath 2: " + path);
		urls.add(path);
		if (this.useDefaultSuffixPattern && path.indexOf('.') == -1) {
			urls.add(path + ".*");
		}
	}

	protected void validateHandler(Object handler, HttpServletRequest request) throws Exception {
        System.out.println ("DEBUG: validateHandler 1: " + handler);
        System.out.println ("DEBUG: validateHandler 2: " + handler);
		RequestMapping mapping = this.cachedMappings.get(handler.getClass());
		if (mapping == null) {
			mapping = AnnotationUtils.findAnnotation(handler.getClass(), RequestMapping.class);
		}
		if (mapping != null) {
			validateMapping(mapping, request);
		}
        System.out.println ("DEBUG: validateHandler 3: " + mapping);
	}

	protected void validateMapping(RequestMapping mapping, HttpServletRequest request) throws Exception {
        System.out.println ("DEBUG: validateMapping 1: " + mapping);
		RequestMethod[] mappedMethods = mapping.method();
		if (!GSServletAnnotationMappingUtils.checkRequestMethod(mappedMethods, request)) {
            System.out.println ("DEBUG: validateMapping 1a:");
			String[] supportedMethods = new String[mappedMethods.length];
			for (int i = 0; i < mappedMethods.length; i++) {
				supportedMethods[i] = mappedMethods[i].name();
			}
			throw new HttpRequestMethodNotSupportedException(request.getMethod(), supportedMethods);
		}

        System.out.println ("DEBUG: validateMapping 2: ");
		String[] mappedParams = mapping.params();
		if (!GSServletAnnotationMappingUtils.checkParameters(mappedParams, request)) {
            System.out.println ("DEBUG: validateMapping 2a:");
			throw new ServletException("Parameter conditions {" +
					StringUtils.arrayToDelimitedString(mappedParams, ", ") +
					"} not met for request parameters: " + request.getParameterMap());
		}
        System.out.println ("DEBUG: validateMapping 3: " + mappedParams);
	}
}
