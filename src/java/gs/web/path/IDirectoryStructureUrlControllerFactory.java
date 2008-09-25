package gs.web.path;

import org.springframework.web.servlet.mvc.Controller;

/**
 * Interface required for "standard" JDK interface-based proxies.
 * Using this instead of the default CGLIB partly so we don't get warnings in the logs like the following:
 *
 * WARN  org.springframework.aop.framework.Cglib2AopProxy  - Unable to proxy method [public final boolean org.springframework.web.servlet.support.WebContentGenerator.isUseExpiresHeader()] because it is final: All calls to this method via a proxy will be routed directly to the proxy.
 * WARN  org.springframework.aop.framework.Cglib2AopProxy  - Unable to proxy method [public final void org.springframework.web.servlet.support.WebContentGenerator.setUseCacheControlHeader(boolean)] because it is final: All calls to this method via a proxy will be routed directly to the proxy.
 * WARN  org.springframework.aop.framework.Cglib2AopProxy  - Unable to proxy method [public final boolean org.springframework.web.servlet.support.WebContentGenerator.isUseCacheControlHeader()] because it is final: All calls to this method via a proxy will be routed directly to the proxy.
 * WARN  org.springframework.aop.framework.Cglib2AopProxy  - Unable to proxy method [public final void org.springframework.web.servlet.support.WebContentGenerator.setUseCacheControlNoStore(boolean)] because it is final: All calls to this method via a proxy will be routed directly to the proxy.
 * WARN  org.springframework.aop.framework.Cglib2AopProxy  - Unable to proxy method [public final boolean org.springframework.web.servlet.support.WebContentGenerator.isUseCacheControlNoStore()] because it is final: All calls to this method via a proxy will be routed directly to the proxy.
 * WARN  org.springframework.aop.framework.Cglib2AopProxy  - Unable to proxy method [public final void org.springframework.web.servlet.support.WebContentGenerator.setCacheSeconds(int)] because it is final: All calls to this method via a proxy will be routed directly to the proxy.
 * WARN  org.springframework.aop.framework.Cglib2AopProxy  - Unable to proxy method [public final int org.springframework.web.servlet.support.WebContentGenerator.getCacheSeconds()] because it is final: All calls to this method via a proxy will be routed directly to the proxy.
 * WARN  org.springframework.aop.framework.Cglib2AopProxy  - Unable to proxy method [public final void org.springframework.web.context.support.WebApplicationObjectSupport.setServletContext(javax.servlet.ServletContext)] because it is final: All calls to this method via a proxy will be routed directly to the proxy.
 * WARN  org.springframework.aop.framework.Cglib2AopProxy  - Unable to proxy method [public final org.springframework.context.ApplicationContext org.springframework.context.support.ApplicationObjectSupport.getApplicationContext() throws java.lang.IllegalStateException] because it is final: All calls to this method via a proxy will be routed directly to the proxy.
 * WARN  org.springframework.aop.framework.Cglib2AopProxy  - Unable to proxy method [public final void org.springframework.context.support.ApplicationObjectSupport.setApplicationContext(org.springframework.context.ApplicationContext) throws org.springframework.beans.BeansException] because it is final: All calls to this method via a proxy will be routed directly to the proxy.
 *
 * ... which would happen if we used this:
 *
 * <aop:scoped-proxy/>
 *
 * ... instead of this:
 *
 * <aop:scoped-proxy proxy-target-class="false"/>
 *
 * @see <a href="http://static.springframework.org/spring/docs/2.5.x/reference/beans.html">3.4.4.5.1. Choosing the type of proxy created</a>
 * @see <a href="http://article.gmane.org/gmane.comp.java.springframework.devel/9396">Re: Cglib2AopProxy warnings...</a> 
 * @author Young Fan
 */
public interface IDirectoryStructureUrlControllerFactory {
    public IDirectoryStructureUrlController getController();
}
