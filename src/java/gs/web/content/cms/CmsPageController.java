package gs.web.content.cms;

import gs.data.content.cms.*;
import gs.web.ControllerFamily;
import gs.web.IControllerFamilySpecifier;
import gs.web.util.RedirectView301;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class CmsPageController extends AbstractController implements IControllerFamilySpecifier {
    private static final Logger _log = Logger.getLogger(CmsPageController.class);

    /** Spring Bean ID */
    public static final String BEAN_ID = "/content/cms/page.page";

    private ICmsPageDao _pageDao;
    private String _viewName;
    private Long _contentId = null;
    private ControllerFamily _controllerFamily;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        CmsPage page = null;

        if (_contentId != null) {
            page = _pageDao.get(_contentId);
        }

        if (page == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView("/status/error404.page");
        }

        if (!StringUtils.equals(page.getUri(), request.getRequestURI())) {
            return new ModelAndView(new RedirectView301(page.getUri()));
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("page", page);

        return new ModelAndView(_viewName, model);
    }

    public void setCmsPageDao(ICmsPageDao pageDao) {
        _pageDao = pageDao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public void setContentId(Long contentId) {
        _contentId = contentId;
    }

    public ControllerFamily getControllerFamily() {
        return _controllerFamily;
    }

    public void setControllerFamily(ControllerFamily controllerFamily) {
        _controllerFamily = controllerFamily;
    }
}