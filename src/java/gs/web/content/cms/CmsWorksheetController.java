package gs.web.content.cms;


import gs.data.cms.IPublicationDao;
import gs.data.content.ArticleComment;
import gs.data.content.IArticleDao;
import gs.data.content.cms.*;
import gs.data.util.SpringUtil;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CmsWorksheetController extends AbstractController {

    private ICmsFeatureDao _featureDao;
    private ICmsCategoryDao _cmsCategoryDao;
    private IArticleDao _legacyArticleDao;
    private String _viewName;

    public static final String GAM_AD_ATTRIBUTE_KEY = "editorial";
    public static final String GAM_AD_ATTRIBUTE_REFERRING_TOPIC_CENTER_ID = "referring_topic_center_id";
    private static final Pattern TOPIC_CENTER_URL_PATTERN = Pattern.compile("^.*\\.topic\\?content=(\\d+)");

    private static final long WORKSHEET_TOPIC_CENTER_ID = 4313l;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();

        Map<String, Object> model = new HashMap<String, Object>();
        CmsFeature feature = null;

        Long contentId = null;

        boolean notFound = false;
        try {
            // first hyphen after last slash in uri
            int firstHyphenIndex = -1;
            // index of last slash in uri
            int lastSlashIndex = uri.lastIndexOf("/");
            // part of uri after last slash
            String lastComponent = null;
            if (lastSlashIndex > -1) {
                lastComponent = uri.substring(lastSlashIndex + 1);
                // the part of the lastComponent before the first hyphen would be the content ID
                firstHyphenIndex = lastComponent.indexOf("-");
                if (lastComponent != null && firstHyphenIndex > -1) {
                    String contentIdStr = lastComponent.substring(0, firstHyphenIndex);
                    contentId = new Long(contentIdStr);
                } else {
                    notFound = true;
                }
            } else {
                // shouldn't ever get here because request uri always begins with a slash
                notFound = true;
            }
        } catch (Exception e) {
            notFound = true;
        }

        if (notFound) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView("/status/error404.page");
        }

        feature = _featureDao.get(contentId);

        List<ArticleComment> comments = _legacyArticleDao.getArticleComments(feature.getContentKey());

        addGamAttributes(request, feature);

        //ContentKey contentKey = new ContentKey("TopicCenter",WORKSHEET_TOPIC_CENTER_ID);
        //ContentKey contentKey = new ContentKey("TopicCenter",contentId);
        CmsTopicCenter topicCenter = getPublicationDao().populateByContentId(contentId, new CmsTopicCenter());

        UrlBuilder urlBuilder = new UrlBuilder(feature.getContentKey(), feature.getFullUri());

        // for Omniture tracking - commas and double quotes removed
        model.put("commaSeparatedPrimaryKategoryNames", StringEscapeUtils.escapeHtml(feature.getCommaSeparatedPrimaryKategoryNames()));
        model.put("titleForOmniture", StringEscapeUtils.escapeHtml(feature.getTitle().replaceAll(",","").replaceAll("\"","")));

        model.put("contentUrl", urlBuilder.asFullUrl(request));
        model.put("comments", comments);
        model.put("feature", feature);
        model.put("topicCenter", topicCenter);

        // add an "article" or "askTheExperts" variable to the model
        String type = feature.getContentKey().getType();
        type = type.substring(0, 1).toLowerCase() + type.substring(1);
        model.put(type, feature);
        model.put("type", type);

        model.put("uri", uri + "?content=" + feature.getContentKey().getIdentifier());
        model.put("almondNetCategory", CmsContentUtils.getAlmondNetCategory(feature));

        return new ModelAndView(_viewName, model);
    }

    // TODO-11818 maybe this should be refactored out since it's copy-pasted in CmsFeatureController, CmsVideoController, and CmsWorksheetController?
    protected void addGamAttributes(HttpServletRequest request, CmsFeature feature) {
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        for (CmsCategory category : feature.getUniqueKategoryBreadcrumbs()) {
            pageHelper.addAdKeywordMulti(GAM_AD_ATTRIBUTE_KEY, category.getName());
        }

        // GS-12103 - tag all worksheets with "Back to School" google ad attribute so that BTS ads will show up here
        pageHelper.addAdKeywordMulti(GAM_AD_ATTRIBUTE_KEY, "Back to School");

        pageHelper.addAdKeyword("article_id", String.valueOf(feature.getContentKey().getIdentifier()));

        // note: "referer" is a typo in the HTTP spec -- don't fix it here
        String referrer = request.getHeader("referer");
        if (referrer != null) {
            Matcher matcher = TOPIC_CENTER_URL_PATTERN.matcher(referrer);
            if (matcher.find()) {
                String referringTopicCenterID = matcher.group(1);
                pageHelper.addAdKeyword(GAM_AD_ATTRIBUTE_REFERRING_TOPIC_CENTER_ID, referringTopicCenterID);
            } else if (referrer.endsWith("/preschool/")) {
                pageHelper.addAdKeyword(GAM_AD_ATTRIBUTE_REFERRING_TOPIC_CENTER_ID, String.valueOf(CmsConstants.PRESCHOOL_TOPIC_CENTER_ID));
            } else if (referrer.endsWith("/elementary-school/")) {
                pageHelper.addAdKeyword(GAM_AD_ATTRIBUTE_REFERRING_TOPIC_CENTER_ID, String.valueOf(CmsConstants.ELEMENTARY_SCHOOL_TOPIC_CENTER_ID));
            } else if (referrer.endsWith("/middle-school/")) {
                pageHelper.addAdKeyword(GAM_AD_ATTRIBUTE_REFERRING_TOPIC_CENTER_ID, String.valueOf(CmsConstants.MIDDLE_SCHOOL_TOPIC_CENTER_ID));
            } else if (referrer.endsWith("/high-school/")) {
                pageHelper.addAdKeyword(GAM_AD_ATTRIBUTE_REFERRING_TOPIC_CENTER_ID, String.valueOf(CmsConstants.HIGH_SCHOOL_TOPIC_CENTER_ID));
            }
        }
    }

    private static IPublicationDao getPublicationDao() {
        return (IPublicationDao) SpringUtil.getApplicationContext().getBean("publicationDao");
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public ICmsFeatureDao getCmsFeatureDao() {
        return _featureDao;
    }

    public void setCmsFeatureDao(ICmsFeatureDao featureDao) {
        _featureDao = featureDao;
    }

    public void setArticleDao(IArticleDao legacyArticleDao) {
        _legacyArticleDao = legacyArticleDao;
    }

    public ICmsCategoryDao getCmsCategoryDao() {
        return _cmsCategoryDao;
    }

    public void setCmsCategoryDao(ICmsCategoryDao cmsCategoryDao) {
        _cmsCategoryDao = cmsCategoryDao;
    }
}
