package gs.web.content.cms;

import gs.data.cms.IPublicationDao;
import gs.data.community.Subscription;
import gs.data.community.SubscriptionProduct;
import gs.data.community.User;
import gs.data.content.ArticleComment;
import gs.data.content.IArticleDao;
import gs.data.content.cms.*;
import gs.web.ads.AdTagHandler;
import gs.web.util.CookieUtil;
import gs.web.util.PageHelper;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CmsFeatureController extends AbstractController {
    private static final Logger _log = Logger.getLogger(CmsFeatureController.class);

    /** Spring Bean ID */
    public static final String BEAN_ID = "/content/cms/feature.page";
    public static final String ARTICLE_SLIDESHOW_BEAN_ID = "/content/cms/slideshow.page";

    public static final String GAM_AD_ATTRIBUTE_KEY = "editorial";
    public static final String GAM_AD_ATTRIBUTE_REFERRING_TOPIC_CENTER_ID = "referring_topic_center_id";

    private static final Pattern TOPIC_CENTER_URL_PATTERN = Pattern.compile("^.*\\.topic\\?content=(\\d+)");

    // GS-11227
    private static final Set<Long> CATEGORIES_FOR_CONTEXTUAL_ADS = new HashSet<Long>();
    static {
        CATEGORIES_FOR_CONTEXTUAL_ADS.add(CmsConstants.GREAT_GIFTS_CATEGORY_ID);
        CATEGORIES_FOR_CONTEXTUAL_ADS.add(CmsConstants.HEALTH_AND_DEVELOPMENT_CATEGORY_ID);
        CATEGORIES_FOR_CONTEXTUAL_ADS.add(CmsConstants.ACADEMICS_AND_ACTIVITIES_CATEGORY_ID);
        CATEGORIES_FOR_CONTEXTUAL_ADS.add(CmsConstants.SPECIAL_EDUCATION_CATEGORY_ID);
        CATEGORIES_FOR_CONTEXTUAL_ADS.add(CmsConstants.FIND_A_SCHOOL_CATEGORY_ID);
    }

    private CmsVideoController _cmsVideoController;
    private CmsWorksheetController _cmsWorksheetController;
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

        //GS-12260 if "showNew" param is present then show the new page.
        String showNewParam =  request.getParameter("showNew");
        boolean isShowNew = (StringUtils.isNotBlank(showNewParam)) ? ("true".equalsIgnoreCase(showNewParam) ? true : false) : false;

        if (!showSampleSlideshow) {
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

            if (feature != null) {
                if (CmsConstants.VIDEO_CONTENT_TYPE.equals(feature.getContentKey().getType())) {
                    return _cmsVideoController.handleRequestInternal(request,response); //EARLY EXIT
                } else if (CmsConstants.WORKSHEET_CONTENT_TYPE.equals(feature.getContentKey().getType())) {
                    return _cmsWorksheetController.handleRequestInternal(request,response); //EARLY EXIT
                }
            }

            if (!_unitTest && feature != null) {
                // if requested url is not canonical url (e.g. due to CMS recategorization), 301-redirect to canonical url
                UrlBuilder builder = new UrlBuilder(feature.getContentKey());
                // make sure no endless loops ever happen
                if (!(uri.indexOf("/print-view/") >= 0) && !StringUtils.equals(builder.asSiteRelative(request), uri)) {
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

        UrlBuilder urlBuilder = new UrlBuilder(feature.getContentKey(), feature.getFullUri());

        // GS-11485
        // if the user has already been on the website during this browser session,
        boolean showAll = false;
        if (CmsConstants.ARTICLE_CONTENT_TYPE.equals(feature.getContentKey().getType()) && request.getParameter("page") == null) {
            if (CookieUtil.hasCookie(request, SessionContextUtil.TRACKING_NUMBER) && !(uri.indexOf("/print-view/") >= 0)) {
                String queryString = request.getQueryString();
                return new ModelAndView(new RedirectView(urlBuilder.asSiteRelative(request) +
                        (queryString != null ? "?" + queryString + "&page=1" : "?page=1")));
            }
            showAll = true;
        }

        try {
            _cmsFeatureEmbeddedLinkResolver.replaceEmbeddedLinks(feature);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        boolean print = "true".equals(request.getParameter("print")) || (uri.indexOf("/print-view/") >= 0);
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

        // GS-11664 insert BTS list ad  and GS-12091 insert ad into non-bts articles.
        setAdsInFeature(feature, request);

        // paginate after transforms have been done on entire body
        if (CmsConstants.ARTICLE_CONTENT_TYPE.equals(feature.getContentKey().getType()) || CmsConstants.ASK_THE_EXPERTS_CONTENT_TYPE.equals(feature.getContentKey().getType())) {
            String pageNum = request.getParameter("page");
            // GS-11485
            if (StringUtils.equals("all", pageNum) || print || showAll) {
                pageNum = "-1";
            }
            if (StringUtils.isNotBlank(pageNum) && StringUtils.isNumeric(pageNum) ||
                    StringUtils.equals("-1", pageNum)) {
                try {
                    int p = Integer.parseInt(pageNum);
                    feature.setCurrentPageNum(p);
                    if (!StringUtils.equals("-1", pageNum)) {
                        int prevPage = p-1;
                        int nextPage = p+1;
                        model.put("prevPageNum",prevPage );
                        model.put("nextPageNum", nextPage);
                        if((nextPage > feature.getNumPages())){
                            model.put("nextPageNum", "-1");
                        }
                         if(!(prevPage >= 1)){
                            model.put("prevPageNum", "-1");
                         }
                    }

                } catch (NumberFormatException e) {
                    _log.warn("Invalid page number " + pageNum + " for feature uri " + uri);
                } catch(IllegalArgumentException iae) {
                    return new ModelAndView(new RedirectView301(urlBuilder.asSiteRelative(request)));
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

        // GS-11227
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        String contentExcludes = sessionContext.getContextualAdsContentExcludes();
        boolean isAdFree = pageHelper.isAdFree();
        boolean isCobrand = sessionContext.isCobranded();
        if (sessionContext != null) {
            model.put("showContextualAds", getShowContextualAds(feature, contentExcludes, isAdFree, isCobrand));
        }

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

        // insert current page into model
        model.put("currentPage", insertSpansIntoListItems(feature.getCurrentPage()));

        model.put("answer", insertSpansIntoListItems(feature.getAnswer()));

        populatePhotosForGallery(model, feature);

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

        model.put("uri", uri);
        model.put("almondNetCategory", CmsContentUtils.getAlmondNetCategory(feature));

        // GS-11430 Allow for companion ads on articles with Delve Networks videos
        if (StringUtils.contains(feature.getCurrentPage(), "http://assets.delvenetworks.com/player/loader.swf")) {
            model.put("showCompanionAd", true);
        }


        //Check whether to show the NL subscription Hover or not.
        boolean isUserSubscribedToParentAdvisor = false;
        if (PageHelper.isMemberAuthorized(request)) {
            User user = sessionContext.getUser();
            if (user != null) {
                Subscription sub = user.findSubscription(SubscriptionProduct.PARENT_ADVISOR);
                if (sub != null) {
                    isUserSubscribedToParentAdvisor = true;
                }
            }
        }
        model.put("isUserSubscribedToParentAdvisor", isUserSubscribedToParentAdvisor);

        if (CmsConstants.ARTICLE_CONTENT_TYPE.equals(feature.getContentKey().getType())) {
            if (print) {
                return new ModelAndView("/content/cms/articlePrint", model);
            } else {
                return new ModelAndView("/content/cms/articleNew", model);
            }
        }

        if (CmsConstants.ASK_THE_EXPERTS_CONTENT_TYPE.equals(feature.getContentKey().getType()) && isShowNew) {
            if (print) {
                return new ModelAndView("/content/cms/askTheExpertsPrint", model);
            } else {
                return new ModelAndView("/content/cms/askTheExpertsNew", model);
            }
        }

        if ((CmsConstants.ARTICLE_SLIDESHOW_CONTENT_TYPE.equals(feature.getContentKey().getType()) ||
            CmsConstants.ARTICLE_SLIDE_CONTENT_TYPE.equals(feature.getContentKey().getType())) && isShowNew) {
            if (print) {
                return new ModelAndView("/content/cms/articleSlideshowPrint", model);
            } else {
                return new ModelAndView("/content/cms/articleSlideshowNew", model);
            }
        }

        return new ModelAndView(_viewName, model);
        //return new ModelAndView(getViewName(feature), model);
    }

    /* Set the ad in the feature.We support only 1 ad per feature.*/
    protected void setAdsInFeature(CmsFeature feature, HttpServletRequest request) {
        boolean isBts = CmsConstants.isBtsList(feature.getContentKey().getIdentifier());
        boolean hasAd = isBts ? feature.hasBtsListAd() : feature.hasFeatureAd();

        if (hasAd) {
            AdTagHandler adTagHandler = new AdTagHandler();
            adTagHandler.setPosition("Sponsor_610x225");
            adTagHandler.setShowOnPrintView(true);
            try {
                // must first set ad slot prefix
                request.setAttribute(AdTagHandler.REQUEST_ATTRIBUTE_SLOT_PREFIX_NAME, "Library_Article_Page_");
                // then generate the ad code
                if (isBts) {
                    // GS-11664 insert BTS list ad
                    feature.setBtsListAdCode(adTagHandler.getContent(request, SessionContextUtil.getSessionContext(request), null));
                } else {
                    // GS-12091 insert ad into non-bts articles.
                    feature.setFeatureAdCode(adTagHandler.getContent(request, SessionContextUtil.getSessionContext(request), null));
                }

            } catch (Exception e) {
                _log.warn("Error setting ad code for content " + feature.getContentKey());
            }
        }
    }

    static boolean getShowContextualAds(CmsFeature feature, String contentExcludes, boolean isAdFree, boolean isCobrand) {
        if (isAdFree || isCobrand) {
            return false;
        }

        Set<String> excludeIds = new HashSet<String>();
        if (contentExcludes != null) {
            excludeIds.addAll(Arrays.asList(StringUtils.split(contentExcludes, ',')));
        }

        Set<Long> breadcrumbCategoryIds = new HashSet<Long>();
        for (CmsCategory category : feature.getUniqueKategoryBreadcrumbs()) {
            breadcrumbCategoryIds.add(category.getId());
        }
        return CmsConstants.ARTICLE_CONTENT_TYPE.equals(feature.getContentKey().getType()) &&
                !excludeIds.contains(String.valueOf(feature.getContentKey().getIdentifier())) &&
                CollectionUtils.containsAny(CATEGORIES_FOR_CONTEXTUAL_ADS, breadcrumbCategoryIds);
    }

    protected UrlBuilder get301Redirect(Long contentId) {
        UrlBuilder builder = null;
        // GS-8490
        if (contentId == 522L) {
            builder = new UrlBuilder(new ContentKey("Article", 868L));
        }
        // GS-9914
        else if (contentId == 87L || contentId == 1151L) {
            builder = new UrlBuilder(UrlBuilder.CMS_CATEGORY_BROWSE, "220", (String)null, (String)null, (String)null, (String)null, "EN");
        } else if (contentId == 403L) {
            builder = new UrlBuilder(new ContentKey("TopicCenter",2220L));
        } else if (contentId == 133L || contentId == 1107L) {
            builder = new UrlBuilder(UrlBuilder.CMS_CATEGORY_BROWSE, "219", (String)null, (String)null, (String)null, (String)null, "EN");
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

    protected void populatePhotosForGallery(Map model, CmsFeature feature) {
        if (feature.getPhotos() != null && feature.getPhotos().size() > 0) {
            List smallPhotos = new ArrayList();
            List mediumPhotos = new ArrayList();
            List largePhotos = new ArrayList();
            List altTexts = new ArrayList();
            List photoCaptions = new ArrayList();
            for (CmsPhoto photo : feature.getPhotos()) {
                if (StringUtils.isNotBlank(photo.getSmallImageUrl())) {
                    smallPhotos.add(photo.getSmallImageUrl());
                }
                if (StringUtils.isNotBlank(photo.getMediumImageUrl())) {
                    mediumPhotos.add(photo.getMediumImageUrl());
                }
                if (StringUtils.isNotBlank(photo.getLargeImageUrl())) {
                    largePhotos.add(photo.getLargeImageUrl());
                }

                altTexts.add(StringUtils.isNotBlank(photo.getAltText()) ? photo.getAltText() : "");
                photoCaptions.add(StringUtils.isNotBlank(photo.getCaption()) ? photo.getCaption() : "");
            }

            if (smallPhotos.size() > 0 && mediumPhotos.size() > 0 && largePhotos.size() > 0 && altTexts.size() > 0
                    && photoCaptions.size() > 0 && photoCaptions.size() == altTexts.size()
                    && smallPhotos.size() == mediumPhotos.size() && mediumPhotos.size() == largePhotos.size()
                    && largePhotos.size() == altTexts.size()) {
                model.put("smallPhotos", smallPhotos);
                model.put("mediumPhotos", mediumPhotos);
                model.put("largePhotos", largePhotos);
                model.put("altTexts", altTexts);
                model.put("photoCaptions", photoCaptions);
            }
        }
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

    public CmsVideoController getCmsVideoController() {
        return _cmsVideoController;
    }

    public void setCmsVideoController(CmsVideoController cmsVideoController) {
        _cmsVideoController = cmsVideoController;
    }

    public CmsWorksheetController getCmsWorksheetController() {
        return _cmsWorksheetController;
    }

    public void setCmsWorksheetController(CmsWorksheetController cmsWorksheetController) {
        _cmsWorksheetController = cmsWorksheetController;
    }
}