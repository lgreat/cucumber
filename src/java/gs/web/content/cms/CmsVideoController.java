package gs.web.content.cms;


import gs.data.content.ArticleComment;
import gs.data.content.IArticleDao;
import gs.data.content.cms.*;
import gs.web.util.PageHelper;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CmsVideoController extends AbstractController {

    private ICmsFeatureDao _featureDao;
    private IArticleDao _legacyArticleDao;
    private boolean _unitTest = false;
    private String _viewName;

    public static final String GAM_AD_ATTRIBUTE_KEY = "editorial";
    public static final String GAM_AD_ATTRIBUTE_REFERRING_TOPIC_CENTER_ID = "referring_topic_center_id";
    private static final Pattern TOPIC_CENTER_URL_PATTERN = Pattern.compile("^.*\\.topic\\?content=(\\d+)");

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();

        Map<String, Object> model = new HashMap<String, Object>();
        CmsFeature feature = null;

        Long contentId = null;

        boolean notFound = false;
        String contentIdParam = request.getParameter("content");
        if (contentIdParam != null) {
            // GS-11495 - this is the old version of article urls, that have foo-bar.gs?content=[content ID] as the url
            // let's grab the content ID and allow code further down to do the 301-redirect
            try {
                contentId = new Long(contentIdParam);
            } catch (Exception e) {
                notFound = true;
            }
        } else {
            // this is the new version of article urls, that have [content ID]-foo-bar.gs as the url
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
        }

        if (notFound) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView("/status/error404.page");
        }

        // some content should be 301 redirected because the original articles were replaced with new ones
        UrlBuilder redirect = get301Redirect(contentId);
        if (redirect != null) {
            return new ModelAndView(new RedirectView301(redirect.asSiteRelative(request)));
        }

        feature = _featureDao.get(contentId);

        /*if (!_unitTest && feature != null) {
            // if requested url is not canonical url (e.g. due to CMS recategorization), 301-redirect to canonical url
            UrlBuilder builder = new UrlBuilder(feature.getContentKey());
            // make sure no endless loops ever happen
            String canonicalUrl = builder.asSiteRelative(request);
            if (!StringUtils.equals(canonicalUrl, uri)) {
                return new ModelAndView(new RedirectView301(canonicalUrl));
            }
        }*/

        List<ArticleComment> comments;
        if (feature.getLegacyId() != null) {
            comments = _legacyArticleDao.getArticleComments(feature.getLegacyId());
        } else {
            comments = _legacyArticleDao.getArticleComments(feature.getContentKey());
        }

        addGamAttributes(request, feature);


        UrlBuilder urlBuilder = new UrlBuilder(feature.getContentKey(), feature.getFullUri());

        // for Omniture tracking - commas and double quotes removed
        model.put("commaSeparatedPrimaryKategoryNames", StringEscapeUtils.escapeHtml(feature.getCommaSeparatedPrimaryKategoryNames()));
        model.put("titleForOmniture", StringEscapeUtils.escapeHtml(feature.getTitle().replaceAll(",","").replaceAll("\"","")));

        model.put("contentUrl", urlBuilder.asFullUrl(request));
        model.put("comments", comments);
        model.put("feature", feature);

        model.put("breadcrumbs", CmsContentUtils.getBreadcrumbs(feature.getPrimaryKategoryBreadcrumbs(), feature.getLanguage(), request));

        // add an "article" or "askTheExperts" variable to the model
        String type = feature.getContentKey().getType();
        type = type.substring(0, 1).toLowerCase() + type.substring(1);
        model.put(type, feature);
        model.put("type", type);

        model.put("uri", uri + "?content=" + feature.getContentKey().getIdentifier());
        model.put("almondNetCategory", CmsContentUtils.getAlmondNetCategory(feature));

        // GS-11430 Allow for companion ads on articles with Delve Networks videos
        if (StringUtils.contains(feature.getCurrentPage(), "http://assets.delvenetworks.com/player/loader.swf")) {
            model.put("showCompanionAd", true);
        }

        return new ModelAndView(_viewName, model);
    }

    protected void addGamAttributes(HttpServletRequest request, CmsFeature feature) {
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        for (CmsCategory category : feature.getUniqueKategoryBreadcrumbs()) {
            pageHelper.addAdKeywordMulti(GAM_AD_ATTRIBUTE_KEY, category.getName());
        }
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

    protected UrlBuilder get301Redirect(Long contentId) {
        UrlBuilder builder = null;
        // GS-8490
        if (contentId == 522L) {
            builder = new UrlBuilder(new ContentKey("Article", 868L));
        }
        // GS-9914
        else if (contentId == 87L || contentId == 1151L) {
            builder = new UrlBuilder(UrlBuilder.CMS_CATEGORY_BROWSE, "220", (String)null, (String)null, "EN");
        } else if (contentId == 403L) {
            builder = new UrlBuilder(new ContentKey("TopicCenter",2220L));
        } else if (contentId == 133L || contentId == 1107L) {
            builder = new UrlBuilder(UrlBuilder.CMS_CATEGORY_BROWSE, "219", (String)null, (String)null, "EN");
        } else if (contentId == 2078L) {
            builder = new UrlBuilder(new ContentKey("TopicCenter",1539L));
        } else if (contentId == 1192L) {
            builder = new UrlBuilder(new ContentKey("ArticleSlideshow",2402L));
        } else if (contentId == 2422L) { // GS-10148
            builder = new UrlBuilder(new ContentKey("Article", 338L));
        }

        return builder;
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
}
