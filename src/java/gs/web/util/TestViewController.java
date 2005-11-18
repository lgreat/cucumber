/*
 * Copyright (c) 2005 NDP Software. All Rights Reserved.
 * $Id: TestViewController.java,v 1.1 2005/11/18 22:49:44 apeterson Exp $
 */

package gs.web.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Anything in the test directory can be shown directly with this controller.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class TestViewController implements Controller {

    private static final Log _log = LogFactory.getLog(TestViewController.class);

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        _log.error(uri);
        String fileName = "/WEB-INF/page" + uri.replaceAll("gs\\-web/", "").replaceAll("\\.page", "") + ".jspx";
        _log.error(fileName);
        View view = new InternalResourceView(fileName);
        return new ModelAndView(view);
    }

}
