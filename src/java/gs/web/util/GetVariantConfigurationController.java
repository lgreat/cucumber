package gs.web.util;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;

import gs.data.admin.IPropertyDao;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class GetVariantConfigurationController  implements Controller {

    /** Spring BEAN id */
    public static final String BEAN_ID = "GetVariantConfigurationController";
    private IPropertyDao _propertyDao;

    /**
     * @see gs.data.util.email.EmailUtils
     */
    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        String variantConfiguration = getPropertyDao().getProperty(IPropertyDao.VARIANT_CONFIGURATION);
        if (StringUtils.isBlank(variantConfiguration)) {
            variantConfiguration = "1";
        }
        out.print(variantConfiguration);
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