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

import gs.data.content.IArticleDao;
import gs.data.content.ArticleComment;
import gs.data.content.cms.ICmsFeatureDao;
import gs.data.content.cms.CmsFeature;
import gs.data.content.cms.CmsCategory;
import gs.data.content.cms.ContentKey;
import gs.web.util.UrlBuilder;
import gs.web.util.PageHelper;
import gs.web.content.TargetSupplyList;

public class CmsFeatureController extends AbstractController {
    private static final Logger _log = Logger.getLogger(CmsFeatureController.class);

    /** Spring Bean ID */
    public static final String BEAN_ID = "/content/cms/feature.page";

    public static final String GAM_AD_ATTRIBUTE_KEY = "editorial";

    private ICmsFeatureDao _featureDao;
    private IArticleDao _legacyArticleDao;
    private CmsContentLinkResolver _cmsFeatureEmbeddedLinkResolver;
    private String _viewName;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();

        Map<String, Object> model = new HashMap<String, Object>();

        Long contentId;
        try {
            contentId = new Long(request.getParameter("content"));  
        } catch(Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView("/status/error404.page");
        }

        CmsFeature feature = _featureDao.get(contentId);

        if (feature == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return new ModelAndView("/status/error404.page");
        }

        try {
            _cmsFeatureEmbeddedLinkResolver.replaceEmbeddedLinks(feature);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        boolean print = "true".equals(request.getParameter("print"));
        String fromPageNum = request.getParameter("fromPage");
        boolean validFromPageNum = (StringUtils.isNotBlank(fromPageNum) && (StringUtils.isNumeric(fromPageNum) || "all".equals(fromPageNum)));
        if (print && validFromPageNum) {
            // if going to print view, save current page num so we can return to it
            model.put("fromPage", fromPageNum);
        }

        // paginate after transforms have been done on entire body
        String pageNum = request.getParameter("page");
        if (StringUtils.equals("all", pageNum) || print) {
            pageNum = "-1";
        }
        if (StringUtils.isNotBlank(pageNum) && StringUtils.isNumeric(pageNum) ||
                StringUtils.equals("-1", pageNum)) {
            try {
                feature.setCurrentPageNum(Integer.parseInt(pageNum));
            } catch (NumberFormatException e) {
                _log.warn("Invalid page number " + pageNum + " for feature uri " + uri);
            }
        }

        List<ArticleComment> comments;
        if (feature.getLegacyId() != null) {
            comments = _legacyArticleDao.getArticleComments(feature.getLegacyId());
        } else {
            comments = _legacyArticleDao.getArticleComments(feature.getContentKey());
        }

        // Google Ad Manager ad keywords
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        for (CmsCategory category : feature.getPrimaryKategoryBreadcrumbs()) {
            pageHelper.addAdKeywordMulti(GAM_AD_ATTRIBUTE_KEY, category.getName());
        }
        if (feature.getSecondaryKategoryBreadcrumbs() != null) {
            for (List<CmsCategory> secondaryCategory : feature.getSecondaryKategoryBreadcrumbs()) {
                for (CmsCategory category : secondaryCategory) {
                    pageHelper.addAdKeywordMulti(GAM_AD_ATTRIBUTE_KEY, category.getName());
                }
            }
        }
        pageHelper.addAdKeyword("article_id", String.valueOf(feature.getContentKey().getIdentifier()));

        UrlBuilder urlBuilder = new UrlBuilder(feature.getContentKey(), feature.getFullUri());

        // insert current page into model
        model.put("currentPage", insertSpansIntoListItems(insertSidebarIntoPage(feature.getCurrentPage(), feature)));
        model.put("answer", insertSpansIntoListItems(feature.getAnswer()));
        model.put("bio", insertSpansIntoListItems(feature.getAuthorBio()));

        // for Omniture tracking - commas and double quotes removed
        model.put("commaSeparatedPrimaryKategoryNames", StringEscapeUtils.escapeHtml(feature.getCommaSeparatedPrimaryKategoryNames()));
        model.put("titleForOmniture", StringEscapeUtils.escapeHtml(feature.getTitle().replaceAll(",","").replaceAll("\"","")));

        model.put("contentUrl", urlBuilder.asFullUrl(request));
        model.put("comments", comments);
        model.put("feature", feature);

        // add an "article" or "askTheExperts" variable to the model
        String type = feature.getContentKey().getType();
        type = type.substring(0, 1).toLowerCase() + type.substring(1);
        model.put(type, feature);
        model.put("type", type);

        model.put("uri", uri + "?content=" + contentId);

        checkTargetSupplyList(feature, model);

        return new ModelAndView(_viewName, model);
        //return new ModelAndView(getViewName(feature), model);
    }

    protected void checkTargetSupplyList(CmsFeature feature, Map<String, Object> model) {
        if (feature != null && model != null
                && feature.getContentKey() != null
                && StringUtils.equals("Article", feature.getContentKey().getType())
                && feature.getContentKey().getIdentifier() != null) {
            ContentKey contentKey = feature.getContentKey();
            if (contentKey.getIdentifier() == 1082l) {
                // elementary
                model.put("targetSupplyItems", TargetSupplyList.getRandomElementaryItems());
            } else if (contentKey.getIdentifier() == 1084l) {
                // middle
                model.put("targetSupplyItems", TargetSupplyList.getRandomMiddleItems());
            } else if (contentKey.getIdentifier() == 109l) {
                // generic
                model.put("targetSupplyItems", TargetSupplyList.getRandomGenericItems());
            }
        }
    }

    /**
     * This is done so we can have bullets with a different color than the text color of the list items.
     * We do this for all li tags, not just those within ul, because it's easier and makes no difference
     * for rendering.
     * @param originalPage
     * @return
     */
    protected String insertSpansIntoListItems(String originalPage) {
        if (originalPage != null) {
            return originalPage.replaceAll("<li>","<li><span class=\"darktext\">").replaceAll("</li>","</span></li>");
        }
        return originalPage;
    }

    protected String insertSidebarIntoPage(String originalPage, CmsFeature feature) {
        // pull out current page into variable
        StringBuilder currentPage = new StringBuilder(originalPage);
        // potentially insert sidebar into current page (if sidebar exists and (page == 1 or all))
        if (StringUtils.isNotBlank(feature.getSidebar()) &&
                (feature.getCurrentPageNum() == 1 || feature.getCurrentPageNum() == -1)) {
            // find end of first paragraph
            int insertIndex = currentPage.indexOf("</p>");
            // find end of second paragraph
            // the +4 is to shift the index to the right by the length of the </p> tag
            insertIndex = currentPage.indexOf("</p>", insertIndex + 4);
            // if there is no end of second paragraph, just insert the sidebar at the end of the current page
            if (insertIndex < 0) {
                insertIndex = currentPage.length();
            } else {
                insertIndex += 4;
            }
            currentPage.insert(insertIndex, "<div id=\"cmsArticleSidebar\"><div><img src=\"/res/img/box/sidebar_top.gif\" alt=\"\" width=\"245\" height=\"9\" /></div>" + feature.getSidebar() + "<div><img src=\"/res/img/box/sidebar_bottom.gif\" alt=\"\" width=\"245\" height=\"9\" /></div></div>");
        }
        return currentPage.toString();
    }

    public void setCmsFeatureDao(ICmsFeatureDao featureDao) {
        _featureDao = featureDao;
    }

    public void setArticleDao(IArticleDao legacyArticleDao) {
        _legacyArticleDao = legacyArticleDao;
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