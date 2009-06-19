package gs.web.content.cms;

import gs.data.cms.IPublicationDao;
import gs.data.content.cms.CmsHomepage;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

public class CmsHomepageController extends AbstractController {
    private static final Logger _log = Logger.getLogger(CmsFeatureController.class);

    public static final String BEAN_ID = "/index.page";

    private IPublicationDao _publicationDao;
    private CmsContentLinkResolver _cmsFeatureEmbeddedLinkResolver;
    private String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {

        Collection<CmsHomepage> homepages = _publicationDao.populateAllByContentType("Homepage", new CmsHomepage());

        CmsHomepage homepage = new CmsHomepage();
        if (homepages.size() > 0) {
            homepage = homepages.iterator().next();
            try {
                _cmsFeatureEmbeddedLinkResolver.replaceEmbeddedLinks(homepage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return new ModelAndView(_viewName, "homepage", homepage);
    }

    public void setPublicationDao(IPublicationDao publicationDao) {
        _publicationDao = publicationDao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public CmsContentLinkResolver getCmsFeatureEmbeddedLinkResolver() {
        return _cmsFeatureEmbeddedLinkResolver;
    }

    public void setCmsFeatureEmbeddedLinkResolver(CmsContentLinkResolver cmsFeatureEmbeddedLinkResolver) {
        _cmsFeatureEmbeddedLinkResolver = cmsFeatureEmbeddedLinkResolver;
    }
}
