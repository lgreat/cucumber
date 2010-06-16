package gs.web.content.cms;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gs.data.content.IArticleDao;
import gs.data.content.ArticleComment;
import gs.data.content.cms.*;
import gs.data.cms.IPublicationDao;
import gs.web.util.UrlBuilder;
import gs.web.util.PageHelper;
import gs.web.util.RedirectView301;

public class CmsFeatureController extends AbstractController {
    private static final Logger _log = Logger.getLogger(CmsFeatureController.class);

    /** Spring Bean ID */
    public static final String BEAN_ID = "/content/cms/feature.page";
    public static final String ARTICLE_SLIDESHOW_BEAN_ID = "/content/cms/slideshow.page";

    public static final String GAM_AD_ATTRIBUTE_KEY = "editorial";
    public static final String GAM_AD_ATTRIBUTE_REFERRING_TOPIC_CENTER_ID = "referring_topic_center_id";

    private static final Pattern TOPIC_CENTER_URL_PATTERN = Pattern.compile("^.*\\.topic\\?content=(\\d+)");

    private ICmsFeatureDao _featureDao;
    private IArticleDao _legacyArticleDao;
    private IPublicationDao _publicationDao;
    private CmsContentLinkResolver _cmsFeatureEmbeddedLinkResolver;
    private String _viewName;
    private boolean _unitTest = false;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();

        Map<String, Object> model = new HashMap<String, Object>();

        CmsFeature feature = null;
        boolean showSampleSlideshow = uri.contains("slideshows/sample-slideshow");

        if (!showSampleSlideshow) {
            Long contentId;
            try {
                contentId = new Long(request.getParameter("content"));
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return new ModelAndView("/status/error404.page");
            }

            UrlBuilder redirect = get301Redirect(contentId);
            if (redirect != null) {
                return new ModelAndView(new RedirectView301(redirect.asSiteRelative(request)));
            }

            feature = _featureDao.get(contentId);

            if (!_unitTest && feature != null) {
                // if requested url is not canonical url (e.g. due to CMS recategorization), 301-redirect to canonical url
                UrlBuilder builder = new UrlBuilder(feature.getContentKey());
                // make sure no endless loops ever happen
                if (!StringUtils.equals(builder.asSiteRelative(request), uri + "?content=" + contentId)) {
                    return new ModelAndView(new RedirectView301(builder.asSiteRelative(request)));
                }
            }

        } else {
            feature = getSampleArticleSlideshow(null);
            String slideUri = uri.replaceAll("^.*/(.*)\\.gs","$1");
            feature.setCurrentSlideIndex(feature.findSlideIndex(slideUri));
        }

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

        if (CmsConstants.ARTICLE_SLIDESHOW_CONTENT_TYPE.equals(feature.getContentKey().getType()) ||
            CmsConstants.ARTICLE_SLIDE_CONTENT_TYPE.equals(feature.getContentKey().getType())) {
            if (CmsConstants.ARTICLE_SLIDE_CONTENT_TYPE.equals(feature.getContentKey().getType())) {
                feature = getSampleArticleSlideshow(feature.getContentKey().getIdentifier());
            }
            processSlideshow(feature, request.getParameter("page"));
            List<CmsFeature> slides;
            if (print) {
                slides = feature.getSlides();
            } else {
                slides = new ArrayList<CmsFeature>();
                slides.add(feature.getCurrentSlide());
            }
            model.put("currentSlides", slides);
        }

        // paginate after transforms have been done on entire body
        if (CmsConstants.ARTICLE_CONTENT_TYPE.equals(feature.getContentKey().getType()) || CmsConstants.ASK_THE_EXPERTS_CONTENT_TYPE.equals(feature.getContentKey().getType())) {
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
        }

        List<ArticleComment> comments;
        if (feature.getLegacyId() != null) {
            comments = _legacyArticleDao.getArticleComments(feature.getLegacyId());
        } else {
            comments = _legacyArticleDao.getArticleComments(feature.getContentKey());
        }

        // Google Ad Manager ad keywords
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

        UrlBuilder urlBuilder = new UrlBuilder(feature.getContentKey(), feature.getFullUri());

        // insert current page into model
        model.put("currentPage", insertSpansIntoListItems(insertSidebarIntoPage(feature.getCurrentPage(), feature)));
        model.put("answer", insertSpansIntoListItems(feature.getAnswer()));

        List<String> authorBios = feature.getAuthorBios();
        if (authorBios != null) {
            for (int i = 0; i < authorBios.size(); i++) {
                authorBios.set(i, insertSpansIntoListItems(authorBios.get(i)));
            }
            model.put("authorBios", authorBios);
        }

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

        return new ModelAndView(_viewName, model);
        //return new ModelAndView(getViewName(feature), model);
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

    protected void processSlideshow(CmsFeature slideshow, String page) {
        // populate slideshow with slide objects using slide IDs;
        // if any of the slides have not been published to the database in cms.properties,
        // they will be null here and omitted
        List<CmsFeature> slides = new ArrayList<CmsFeature>();
        for (Long slideId : slideshow.getSlideIds()) {
            CmsFeature slide = _featureDao.get(slideId);
            if (slide != null) {
                slides.add(slide);
            }
        }
        slideshow.setSlides(slides);

        // selected slide based on page parameter
        int slideIndex = 0;
        if (page != null) {
            try {
                slideIndex = Integer.parseInt(page) - 1;
                if (slideIndex < 0 || slideIndex + 1 > slideshow.getTotalSlides()) {
                    slideIndex = 0;
                }
            } catch (NumberFormatException e) {
                slideIndex = 0;
            }
        }
        slideshow.setCurrentSlideIndex(slideIndex);

        // process all slides because they'll be needed for the print view
        CmsFeature currentSlide = slideshow.getCurrentSlide();
        for (CmsFeature slide : slideshow.getSlides()) {
            // handle list item tags in slide body
            slide.setBody(insertSpansIntoListItems(slide.getBody()));

            // replace embedded links in slide
            try {
                _cmsFeatureEmbeddedLinkResolver.replaceEmbeddedLinks(slide);
            } catch (Exception e) {
                throw new RuntimeException(e);
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

    // START sample methods

    private static CmsFeature getSampleArticleSlideshow(Long slideId) {
        CmsFeature slideshow = new CmsFeature();
        slideshow.setLanguage("EN");
        ContentKey contentKey = new ContentKey();
        contentKey.setType("ArticleSlideshow");

        if (slideId != null) {
            contentKey.setIdentifier(slideId);
        } else {
            contentKey.setIdentifier(123L);
        }

        slideshow.setContentKey(contentKey);
        slideshow.setFullUri("/parenting/behavior-discipline/discipline-decisions/slideshows/sample-slideshow");
        slideshow.setSummary("The deck goes here");
        slideshow.setTitle("Article slideshow title");
        slideshow.setMetaDescription("meta description goes here");
        slideshow.setMetaKeywords("meta keywords");
        slideshow.setAuthors(Arrays.asList("GreatSchools Staff"));
        /*
        slideshow.setImageUrl("/res/img/feature_image.jpg");
        slideshow.setImageAltText("feature image alt text");
        */

        CmsCategory firstCat = new CmsCategory();
        firstCat.setId(4);
        firstCat.setName("Category A");
        CmsCategory secondCat = new CmsCategory();
        secondCat.setId(5);
        secondCat.setName("Category B");
        CmsCategory thirdCat = new CmsCategory();
        thirdCat.setId(123);
        thirdCat.setName("Category C");
        thirdCat.setType(CmsCategory.TYPE_TOPIC);
        List<CmsCategory> secondaryKategories = new ArrayList<CmsCategory>();
        secondaryKategories.add(thirdCat);

        slideshow.setPrimaryKategory(thirdCat);
        List<CmsCategory> categoryBreadcrumbs = Arrays.asList(firstCat, secondCat, thirdCat);
        slideshow.setPrimaryKategoryBreadcrumbs(categoryBreadcrumbs);

        firstCat = new CmsCategory();
        firstCat.setId(1);
        firstCat.setName("Category 1");
        secondCat = new CmsCategory();
        secondCat.setId(2);
        secondCat.setName("Category 2");
        thirdCat = new CmsCategory();
        thirdCat.setId(3);
        thirdCat.setName("Category 3");
        secondaryKategories = new ArrayList<CmsCategory>();
        secondaryKategories.add(thirdCat);

        slideshow.setSecondaryKategories(secondaryKategories);
        categoryBreadcrumbs = Arrays.asList(firstCat, secondCat, thirdCat);
        List<List<CmsCategory>> breadcrumbs = new ArrayList<List<CmsCategory>>();
        breadcrumbs.add(categoryBreadcrumbs);
        slideshow.setSecondaryKategoryBreadcrumbs(breadcrumbs);

        if (slideId != null) {
            List<Long> slideIDs = new ArrayList<Long>();
            slideIDs.add(slideId);
            slideshow.setSlideIds(slideIDs);
        } else {
            List<CmsFeature> slides = new ArrayList<CmsFeature>();
            List<Long> slideIDs = new ArrayList<Long>();
            for (int i = 1; i <= 10; i++) {
                CmsFeature slide = getSampleArticleSlide(i);
                slides.add(slide);
                slideIDs.add(slide.getContentKey().getIdentifier());
            }

            slideshow.setSlides(slides);
            slideshow.setSlideIds(slideIDs);
        }

        List<CmsLink> links = new ArrayList<CmsLink>();
        CmsLink link = new CmsLink();
        link.setTitle("Lorem Ipsum Dolor Sit Amet");
        link.setUrl("http://www.google.com");
        link.setDescription("Quisque eu velit a libero pellentesque ullamcorper.");
        link.setLinkText("In semper purus eget justo");
        links.add(link);
        link = new CmsLink();
        link.setUrl("http://www.yahoo.com");
        link.setLinkText("Morbi eu sollicitudin augue");
        links.add(link);
        link = new CmsLink();
        link.setUrl("http://www.jquery.org");
        link.setLinkText("Nunc sed turpis nisl, ac lacinia sem");
        links.add(link);
        link = new CmsLink();
        link.setUrl("http://www.greatschools.org");
        link.setLinkText("Nulla sit amet libero orci, sed euismod nisl");
        links.add(link);
        link = new CmsLink();
        link.setUrl("http://www.loremipsum.net");
        link.setLinkText("In semper purus eget justo");
        links.add(link);
        slideshow.setExternalLinks(links);

        return slideshow;
    }

    private static CmsFeature getSampleArticleSlide(int i) {
        CmsFeature slide = new CmsFeature();
        ContentKey contentKey = new ContentKey();
        contentKey.setType("ArticleSlide");
        contentKey.setIdentifier(10000L + i);

        slide.setContentKey(contentKey);
        slide.setUri("slide-" + i);
        slide.setTitle("article slide title " + i);
        slide.setMetaDescription("meta description goes here " + i);
        slide.setMetaKeywords("meta keywords " + i);

        slide.setBody("<p>slide " + i + " body lorem ipsum lorem ipsum blah blah blah blah random sample text goes here</p>");

        if (i % 2 == 0) {
            slide.setImageUrl("/res/img/content/backtoschool/photo4kids.jpg");
            slide.setImageAltText("four kids");
        } else {
            slide.setImageUrl("/res/img/content/backtoschool/photoOfBoy.jpg");
            slide.setImageAltText("boy");
        }

        return slide;
    }
    // END sample methods

    public void setCmsFeatureDao(ICmsFeatureDao featureDao) {
        _featureDao = featureDao;
    }

    public void setArticleDao(IArticleDao legacyArticleDao) {
        _legacyArticleDao = legacyArticleDao;
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

    public boolean isUnitTest() {
        return _unitTest;
    }

    public void setUnitTest(boolean unitTest) {
        _unitTest = unitTest;
    }
}