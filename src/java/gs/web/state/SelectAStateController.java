/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: SelectAStateController.java,v 1.1 2006/03/17 05:33:43 apeterson Exp $
 */

package gs.web.state;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class SelectAStateController extends AbstractController {

    private static final Log _log = LogFactory.getLog(SelectAStateController.class);

    private String _viewName;
    public static final String PARAM_PROMPT = "prompt";
    public static final String MODEL_URL = "url";
    public static final String PARAM_URL = MODEL_URL;
    public static final String MODEL_PROMPT = "promotext";
    public static final String MODEL_URL_SUFFIX = "extraParams";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse httpServletResponse) throws Exception {


        Map model = new HashMap();
        String promotext = request.getParameter(PARAM_PROMPT);
        if (StringUtils.isNotEmpty(promotext)) {
            model.put(MODEL_PROMPT, promotext);
        } else {
            model.put(MODEL_PROMPT, "");
        }

        String url = request.getParameter(PARAM_URL);
        if (StringUtils.isEmpty(url)) {
            return new ModelAndView(new RedirectView("/", true));
        }
        if (url.indexOf("?") == -1) {
            model.put(MODEL_URL, url + "?state=");
        } else {
            model.put(MODEL_URL, url + "&amp;state=");
        }

        model.put(MODEL_URL_SUFFIX, "");

        return new ModelAndView(_viewName, model);
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

}
