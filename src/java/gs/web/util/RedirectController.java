/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: RedirectController.java,v 1.4 2009/12/04 22:27:03 chriskimm Exp $
 */

package gs.web.util;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Redirects to the URL specified by page. Useful for form processing.
 *
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
 */
public class RedirectController implements Controller {

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String p = request.getParameter("page");
        return new ModelAndView("redirect:"+p);
    }
}
