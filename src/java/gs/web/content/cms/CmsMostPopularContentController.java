package gs.web.content.cms;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import gs.data.content.cms.*;
import gs.data.util.CmsUtil;
import gs.data.cms.IPublicationDao;

public class CmsMostPopularContentController extends AbstractController {
    private static final Log _log = LogFactory.getLog(CmsMostPopularContentController.class);
    private CmsContentLinkResolver _cmsFeatureEmbeddedLinkResolver;
    private String _viewName;
    private IPublicationDao _publicationDao;

    final private static String MODEL_LINKS = "links";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        CmsMostPopularContent mostPopularContent = null;
        if (CmsUtil.isCmsEnabled()) {
            Collection<CmsMostPopularContent> mpcs = _publicationDao.populateAllByContentType("MostPopularContent", new CmsMostPopularContent());
            if (mpcs.size() > 0) {
                mostPopularContent = mpcs.iterator().next();
                try {
                    _cmsFeatureEmbeddedLinkResolver.replaceEmbeddedLinks(mostPopularContent);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (mostPopularContent == null) {
            mostPopularContent = getSampleMostPopularContent();
        }

        model.put(MODEL_LINKS, mostPopularContent.getLinks());

        return new ModelAndView(_viewName, model);
    }

    // START sample topic center methods
    private CmsMostPopularContent getSampleMostPopularContent() {
        CmsMostPopularContent mpc = new CmsMostPopularContent();

        List<CmsLink> links = new ArrayList<CmsLink>();

        CmsLink link = new CmsLink();
        link.setUrl("http://www.google.com");
        link.setLinkText("Lorem ipsum lorem ipsum lorem ipsum 1");
        links.add(link);
        link = new CmsLink();
        link.setUrl("http://www.yahoo.com");
        link.setLinkText("Lorem ipsum lorem ipsum lorem ipsum 2");
        links.add(link);
        link = new CmsLink();
        link.setUrl("http://www.jquery.org");
        link.setLinkText("Lorem ipsum lorem ipsum lorem ipsum 3");
        links.add(link);
        link = new CmsLink();
        link.setUrl("http://www.greatschools.net");
        link.setLinkText("Lorem ipsum lorem ipsum lorem ipsum 4");
        links.add(link);
        link = new CmsLink();
        link.setUrl("http://www.loremipsum.net");
        link.setLinkText("Lorem ipsum lorem ipsum lorem ipsum 5");
        links.add(link);

        mpc.setLinks(links);

        return mpc; 
    }
    // END sample topic center methods

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
