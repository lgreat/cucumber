/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: ContentBoxViewController.java,v 1.6 2007/06/14 17:29:27 thuss Exp $
 */

package gs.web.content;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

import gs.web.util.UrlUtil;

/**
 * Controller for the content box. Figures out what page to show and
 * and sets cache control to cache the page.
 * <p/>
 * http://localhost:8080/gs-web/content/box/v1/WY/feature01.page
 * http://localhost:8080/gs-web/content/box/v1/WY/tipOfTheWeek.page
 * <p/>
 * Gets the content from
 * <p/>
 * http://spreadsheets.google.com/pub?key=pouqRkV5D_eZo2j7CmCj0OQ
 *
 * @author apeterson
 * @author thuss
 */
public class ContentBoxViewController extends ParameterizableViewController {

    public static final String MODEL_STATE = "state";

    public static final String MODEL_FEATURE = "feature";

    public static final String MODEL_WORKSHEET = "worksheet";

    public static final String BEAN_ID = "contentBoxController";

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        String uri = request.getRequestURI();
        String[] s = StringUtils.split(uri, '/');

        // Feature name such as tipOfTheWeek (key in spreadsheet)
        String feature = s[s.length - 1];
        feature = feature.replaceFirst(".page", "");
        request.setAttribute(MODEL_FEATURE, feature);

        // State abbreviation
        String state = s[s.length - 2];
        request.setAttribute(MODEL_STATE, state.toUpperCase());

        // Determine whether to use the DevStaging worksheet or the Production worksheet
        UrlUtil util = new UrlUtil();
        if (util.isDevEnvironment(request.getServerName())) {
            request.setAttribute(MODEL_WORKSHEET, "od6"); // od6 is always the first worksheet
        } else {
            request.setAttribute(MODEL_WORKSHEET, "od7");
        }

        // Allow caching
        response.setHeader("Cache-Control", "public; max-age: 600");
        response.setHeader("Pragma", "");
        Date date = new Date();
        response.setDateHeader("Expires", date.getTime() + 600000);

        return super.handleRequestInternal(request, response);
    }

}
