package gs.web.promo;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.validation.BindException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;

import gs.data.admin.IPropertyDao;

/**
 * Created by IntelliJ IDEA.
 * User: john
 * Date: Jul 15, 2008
 * Time: 6:05:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class GetSurveyHoverInterceptConfigurationController implements Controller {
    protected final Log _log = LogFactory.getLog(getClass());


    /** Spring BEAN id */
    public static final String BEAN_ID = "GetSurveyHoverInterceptConfigurationController";
    private IPropertyDao _propertyDao;

    /**
     * @see gs.data.util.email.EmailUtils
     */
    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        String config = getPropertyDao().getProperty(IPropertyDao.SURVEY_HOVER_INTERCEPT_CONFIGURATION);
        if (StringUtils.isBlank(config)) {
            config = "0";
        }
        out.print(config);
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
