package gs.web.promo;

import gs.data.admin.IPropertyDao;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author Dave Roy <mailto:droy@greatschools.org>
 */

public class SearchRealtorDotComController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());
    private String _viewName;
    private IPropertyDao _propertyDao;

    public static final double DEFAULT_SHOW_AD_PCT = 0.0d;
    
    public static final String PAGE_AD_RATIO_KEY_PARAM = "pageAdRatioKey";
    public static final String DEFAULT_CITY_PARAM = "defaultCity";
    public static final String DEFAULT_STATE_PARAM = "defaultState";
    public static final String SHOW_AD_PCT_PARAM = "showAdPct";
    public static final String OMNITURE_PAGE_NAME_PARAM = "omniturePageName";
    public static final String SIZE_PARAM = "size";
    public static final String STYLE_PARAM = "style";

    public static final String MODEL_DEFAULT_CITY = "defaultCity";
    public static final String MODEL_DEFAULT_STATE = "defaultState";
    public static final String MODEL_OMNITURE_PAGE_NAME = "omniturePageName";
    public static final String MODEL_SIZE = "size";
    public static final String MODEL_STYLE = "style";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        try {
            String pageAdRatioKey = request.getParameter(PAGE_AD_RATIO_KEY_PARAM);
            Integer overrideAdPercentage = null;
            if (!StringUtils.isBlank(pageAdRatioKey)) {
                overrideAdPercentage = _propertyDao.getPropertyAsInteger(pageAdRatioKey);
            }

            if (overrideAdPercentage == null) {
                String showAdParam = request.getParameter(SHOW_AD_PCT_PARAM);
                if (showAdParam != null && showAdParam.length() > 0) {
                    overrideAdPercentage = Integer.parseInt(showAdParam);
                }
            }

            double showAdPct = DEFAULT_SHOW_AD_PCT;
            if (overrideAdPercentage != null && overrideAdPercentage >= 0 && overrideAdPercentage <= 100) {
                showAdPct = overrideAdPercentage / 100.0d;
            }

            SessionContext context = SessionContextUtil.getSessionContext(request);
            boolean isFramed = (context != null) && context.isFramed();
            boolean showRealtorDotComPromos = (context != null) && context.isShowRealtorDotComPromos();
            boolean showAd = false;
            if (isFramed || Math.random() < showAdPct || !showRealtorDotComPromos) {
                showAd = true;
            }

            if (!showAd) {
                Map<String, Object> model = new HashMap<String, Object>();
                model.put(MODEL_DEFAULT_CITY, request.getParameter(DEFAULT_CITY_PARAM));
                model.put(MODEL_DEFAULT_STATE, request.getParameter(DEFAULT_STATE_PARAM));
                model.put(MODEL_OMNITURE_PAGE_NAME, request.getParameter(OMNITURE_PAGE_NAME_PARAM));
                String size = request.getParameter(SIZE_PARAM);
                if (StringUtils.isBlank(size)) {
                    size = "300x250";
                }
                model.put(MODEL_SIZE, size);
                String style = request.getParameter(STYLE_PARAM);
                model.put(MODEL_STYLE, style);
                return new ModelAndView(_viewName, model);
            }
        } catch (Exception e) {
            // Do nothing, fall through and have the alternate content be served instead
            _log.error("Error processing realtor.com search widget.", e);
        }

        response.setContentType("text/plain");
        //PrintWriter out = response.getWriter();
        // print nothing
        //out.flush();

        return null;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public IPropertyDao getPropertyDao() {
        return _propertyDao;
    }

    public void setPropertyDao(IPropertyDao propertyDao) {
        _propertyDao = propertyDao;
    }
}