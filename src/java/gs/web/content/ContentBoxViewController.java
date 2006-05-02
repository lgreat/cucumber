/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: ContentBoxViewController.java,v 1.1 2006/05/02 19:51:17 apeterson Exp $
 */

package gs.web.content;

import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceView;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller for the content box.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class ContentBoxViewController extends ParameterizableViewController {

    public static final String MODEL_PERL_PAGE = "framedUri";
    public static final String MODEL_STATE_ABBREV = "stateAbbrev";

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {

        String uri = request.getRequestURI();

        // This looks like this:
        //     /gs-web/content/box/v1/CA/articleName.page
        uri = uri.replaceAll(".page","");
        //     /gs-web/content/box/v1/CA/articleName
        String[] s = StringUtils.split(uri, '/');
        String articleName = s[s.length-1];
        String stateStr = s[s.length -2];

        request.setAttribute(MODEL_PERL_PAGE, "http://apeterson.dev.greatschools.net/content/box/"+articleName+".html");
        request.setAttribute(MODEL_STATE_ABBREV, stateStr);

        return super.handleRequestInternal(request, response);
    }

}
