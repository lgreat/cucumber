package gs.web.content.cms;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import gs.data.content.IArticleDao;
import gs.data.content.ArticleComment;
import gs.data.content.cms.*;
import gs.web.util.UrlBuilder;
import gs.web.util.PageHelper;

public class CmsTopicCenterController extends AbstractController {
    private static final Logger _log = Logger.getLogger(CmsTopicCenterController.class);

    /** Spring Bean ID */
    public static final String BEAN_ID = "/content/cms/topicCenter.page";

    public static final String GAM_AD_ATTRIBUTE_KEY = "editorial";

    // TODO: need to use featureDao or something like it
    //private ICmsFeatureDao _featureDao;
    private CmsContentLinkResolver _cmsFeatureEmbeddedLinkResolver;
    private String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();

        Map<String, Object> model = new HashMap<String, Object>();

        // TODO: fetch requested topic center from dao instead of hard-coding sample topic center
        CmsTopicCenter topicCenter = new CmsTopicCenter();
        topicCenter.setTitle("title");
        topicCenter.setMetaDescription("meta description goes here");
        topicCenter.setMetaKeywords("meta keywords");
        topicCenter.setFeatureImageUrl("/res/img/feature_image.jpg");
        topicCenter.setFeatureImageAltText("feature image alt text");
        topicCenter.setContentProviderLogoUrl("/res/img/content_provider_logo.gif");
        topicCenter.setContentProviderLogoAltText("content provider logo alt text");

        List<CmsLink> featureLinks = new ArrayList<CmsLink>();
        CmsLink link = new CmsLink();
        link.setUrl("http://www.google.com");
        link.setDescription("Main feature description lorem ipsum lorem ipsum");
        link.setLinkText("Feature lorem ipsum lorem ipsum lorem ipsum 1");
        featureLinks.add(link);
        link = new CmsLink();
        link.setUrl("http://www.yahoo.com");
        link.setLinkText("Feature lorem ipsum lorem ipsum lorem ipsum 2");
        featureLinks.add(link);
        link = new CmsLink();
        link.setUrl("http://www.jquery.org");
        link.setLinkText("Feature lorem ipsum lorem ipsum lorem ipsum 3");
        featureLinks.add(link);
        link = new CmsLink();
        link.setUrl("http://www.greatschools.net");
        link.setLinkText("Feature lorem ipsum lorem ipsum lorem ipsum 4");
        featureLinks.add(link);
        link = new CmsLink();
        link.setUrl("http://www.loremipsum.net");
        link.setLinkText("Feature lorem ipsum lorem ipsum lorem ipsum 5");
        featureLinks.add(link);
        topicCenter.setFeatureLinks(featureLinks);

        List<CmsLink> communityLinks = new ArrayList<CmsLink>();
        link = new CmsLink();
        link.setUrl("http://www.google.com");
        link.setLinkText("Community lorem ipsum lorem ipsum lorem ipsum 1");
        communityLinks.add(link);
        link = new CmsLink();
        link.setUrl("http://www.yahoo.com");
        link.setLinkText("Community lorem ipsum lorem ipsum lorem ipsum 2");
        communityLinks.add(link);
        link = new CmsLink();
        link.setUrl("http://www.jquery.org");
        link.setLinkText("Community lorem ipsum lorem ipsum lorem ipsum 3");
        communityLinks.add(link);
        link = new CmsLink();
        link.setUrl("http://www.greatschools.net");
        link.setLinkText("Community lorem ipsum lorem ipsum lorem ipsum 4");
        communityLinks.add(link);
        link = new CmsLink();
        link.setUrl("http://www.loremipsum.net");
        link.setLinkText("Community lorem ipsum lorem ipsum lorem ipsum 5");
        communityLinks.add(link);
        topicCenter.setCommunityLinks(communityLinks);

        link = new CmsLink();
        link.setUrl("http://community.greatschools.net");
        link.setLinkText("More discussions &gt;");
        topicCenter.setCommunityMoreLink(link);

        if (topicCenter == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView("/status/error404.page");
        }

        try {
            _cmsFeatureEmbeddedLinkResolver.replaceEmbeddedLinks(topicCenter);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        // TODO: Google Ad Manager ad keywords - category breadcrumbs are in incorrectly CmsFeature, not CmsContent
        // TODO: refactor primary category out of CmsFeature into CmsContent
        // TODO: reuse only primary category GAM ad keyword code from CmsFeatureController; no secondary categories here

        model.put("topicCenter", topicCenter);

        return new ModelAndView(_viewName, model);
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