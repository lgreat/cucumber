/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: ContentBoxViewController.java,v 1.4 2006/05/02 22:47:57 apeterson Exp $
 */

package gs.web.content;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Controller for the content box. Figures out what page to show and
 * and sets cache control to cache the page.
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
        uri = uri.replaceAll(".page", "");
        //     /gs-web/content/box/v1/CA/articleName
        String[] s = StringUtils.split(uri, '/');
        String articleName = s[s.length - 1];
        String stateStr = s[s.length - 2];

        request.setAttribute(MODEL_PERL_PAGE, "http://" +
                request.getServerName() +
                //"dev.greatschools.net" +
                "/content/box/" +
                articleName +
                ".html");
        request.setAttribute(MODEL_STATE_ABBREV, stateStr);

        // Allow caching
        response.setHeader("Cache-Control", "public; max-age: 600");
        response.setHeader("Pragma", "");
        Date date = new Date();
        response.setDateHeader("Expires", date.getTime() + 600000);


        return super.handleRequestInternal(request, response);
    }

}
