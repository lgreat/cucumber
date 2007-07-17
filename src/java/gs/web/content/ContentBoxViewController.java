/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: ContentBoxViewController.java,v 1.7 2007/07/17 23:01:35 thuss Exp $
 */

package gs.web.content;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

import gs.web.util.UrlUtil;
import gs.web.util.CacheablePageParameterizableViewController;

/**
 * Controller for the content box. Figures out what page to show and
 * and inherits CacheablePage controller to set cache control headers.
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
public class ContentBoxViewController extends CacheablePageParameterizableViewController {

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

        return super.handleRequestInternal(request, response);
    }

}
