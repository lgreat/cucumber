package gs.web.content.cms;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

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
    final private static String MODEL_STYLE = "style";

    /** Style information (e.g. "Fall 2009") to decide how to present the view */
    public static final String PARAM_STYLE = "style";

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

            List<CmsLink> links = new ArrayList<CmsLink>();
            // strip out links that have empty urls -- these are unpublished content that should not be linked to
            if (mostPopularContent != null && mostPopularContent.getLinks() != null) {
                for (CmsLink link : mostPopularContent.getLinks()) {
                    if (StringUtils.isNotBlank(link.getUrl())) {
                        links.add(link);
                    }
                }
            }

            model.put(MODEL_LINKS, links);
            model.put(MODEL_STYLE, request.getParameter(PARAM_STYLE));
        }

        return new ModelAndView(_viewName, model);
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
