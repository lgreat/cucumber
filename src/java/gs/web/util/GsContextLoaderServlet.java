/*
 * Copyright (c) 2005 NDP Software. All Rights Reserved.
 * $Id: GsContextLoaderServlet.java,v 1.1 2005/10/12 16:47:42 apeterson Exp $
 */

package gs.web.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderServlet;

import javax.servlet.ServletContext;

/**
 * Provides place where we can insert the gs data application context as the parent
 * context.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class GsContextLoaderServlet extends ContextLoaderServlet {
    protected ContextLoader createContextLoader() {
        return new ContextLoader() {
            protected ApplicationContext loadParentContext(ServletContext servletContext) throws BeansException {
                return gs.data.util.SpringUtil.getApplicationContext();
            }
        };

    }
}
