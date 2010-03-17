package gs.web.promo;

import gs.data.admin.IPropertyDao;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author Dave Roy <mailto:droy@greatschools.org>
 */

public class SearchRealtorDotComController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());
    private IPropertyDao _propertyDao;

    public static final double DEFAULT_SHOW_AD_PCT = 0.0d;
    
    public static final String PAGE_AD_RATIO_KEY_PARAM = "pageAdRatioKey";
    public static final String SHOW_AD_PCT_PARAM = "showAdPct";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
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

        boolean showAd = false;
        if (Math.random() < showAdPct) {
            showAd = true;
        }

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        out.print(showAd);
        out.flush();

        return null;
    }

    public IPropertyDao getPropertyDao() {
        return _propertyDao;
    }

    public void setPropertyDao(IPropertyDao propertyDao) {
        _propertyDao = propertyDao;
    }
}