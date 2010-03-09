package gs.web.content.cms;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gs.data.content.cms.*;
import gs.data.cms.IPublicationDao;
import gs.data.util.CmsUtil;
import gs.web.util.PageHelper;

public class CmsTopicCenterController2010 extends AbstractController {
    private static final Logger _log = Logger.getLogger(CmsTopicCenterController.class);

    /** Spring Bean ID */
    public static final String BEAN_ID = "/content/cms/topicCenter.page";

    public static final String GAM_AD_ATTRIBUTE_KEY = "editorial";

    private CmsContentLinkResolver _cmsFeatureEmbeddedLinkResolver;
    private String _viewName;
    private IPublicationDao _publicationDao;
    private ICmsDiscussionBoardDao _cmsDiscussionBoardDao;
    private Boolean _useAdKeywords = true;
    private Long _topicCenterContentID;

    private static final Pattern MORE_ON_THIS_TOPIC_REGEX = Pattern.compile("More on this topic", Pattern.CASE_INSENSITIVE);
    private static final Pattern BROWSE_TOPICS_REGEX = Pattern.compile("Browse topics", Pattern.CASE_INSENSITIVE);

    private static final String MODEL_BROWSE_BY_GRADE_SUBTOPICS = "browseByGradeSubtopics";
    private static final String MODEL_MORE_ON_THIS_TOPIC_SECTIONS = "moreOnThisTopicSections";
    private static final String MODEL_BROWSE_TOPICS_SECTIONS = "browseTopicsSections";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> model = new HashMap<String, Object>();

        if (CmsUtil.isCmsEnabled()) {
            String uri = request.getRequestURI();
            boolean showSample = (BEAN_ID.equals(uri));
            CmsTopicCenter topicCenter;

            Long contentId = null;
            if (showSample) {
                topicCenter = getSampleTopicCenter();
            } else {
                if (getTopicCenterContentID() == null) {
                    try {
                        contentId = new Long(request.getParameter("content"));
                    } catch (Exception e) {
                        _log.warn("contentId \"" + request.getParameter("content") + "\" is not a Long");
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        return new ModelAndView("/status/error404.page");
                    }
                } else {
                    contentId = getTopicCenterContentID();
                }

                topicCenter = _publicationDao.populateByContentId(contentId, new CmsTopicCenter());
            }

            if (topicCenter == null) {
                _log.warn("Error locating topic center with contentId=" + contentId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return new ModelAndView("/status/error404.page");
            }

            if (topicCenter.getDiscussionBoardId() != null) {
                model.put("discussionBoard", _cmsDiscussionBoardDao.get(topicCenter.getDiscussionBoardId()));
            }

            try {
                _cmsFeatureEmbeddedLinkResolver.replaceEmbeddedLinks(topicCenter);
            } catch(Exception e) {
                throw new RuntimeException(e);
            }

            if (isUseAdKeywords()) {
                // Google Ad Manager ad keywords
                PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
                for (CmsCategory category : topicCenter.getUniqueKategoryBreadcrumbs()) {
                    pageHelper.addAdKeywordMulti(GAM_AD_ATTRIBUTE_KEY, category.getName());
                }
                pageHelper.addAdKeyword("topic_center_id", String.valueOf(topicCenter.getContentKey().getIdentifier()));
            }

            model.put("omnitureTopicCenterName", topicCenter.getTitle().replaceAll(",", "").replaceAll("\"", ""));

            populateBrowseSidebarModel(topicCenter, model);

            model.put("topicCenter", topicCenter);
        }

        return new ModelAndView(_viewName, model);
    }

    private void populateBrowseSidebarModel(CmsTopicCenter topicCenter, Map<String, Object> model) {
        // TODO-9458
        List<CmsSubtopic> browseByGradeSubtopics = new ArrayList<CmsSubtopic>();
        for (CmsSubtopic subtopic : topicCenter.getSubtopics()) {
            boolean moreInThisTopicMatches = MORE_ON_THIS_TOPIC_REGEX.matcher(subtopic.getTitle()).matches();
            boolean browseTopicsMatches = BROWSE_TOPICS_REGEX.matcher(subtopic.getTitle()).matches();
            if (moreInThisTopicMatches) {
                // if there is more than one subtopic matching the title "More on this topic", we'll just use the last one
                List<CmsSubSubtopic> moreOnThisTopicSections = new ArrayList<CmsSubSubtopic>();
                for (CmsSubSubtopic section : subtopic.getSubSubtopics()) {
                    moreOnThisTopicSections.add(section);
                }
                model.put(MODEL_MORE_ON_THIS_TOPIC_SECTIONS, moreOnThisTopicSections);
            } else if (browseTopicsMatches) {
                // if there is more than one subtopic matching the title "Browse topics", we'll just use the last one
                List<CmsSubSubtopic> browseTopicsSections = new ArrayList<CmsSubSubtopic>();
                for (CmsSubSubtopic section : subtopic.getSubSubtopics()) {
                    browseTopicsSections.add(section);
                }
                model.put(MODEL_BROWSE_TOPICS_SECTIONS, browseTopicsSections);
            } else {
                browseByGradeSubtopics.add(subtopic);
            }
        }
        model.put(MODEL_BROWSE_BY_GRADE_SUBTOPICS, browseByGradeSubtopics);
    }

    // START sample topic center methods
    private CmsTopicCenter getSampleTopicCenter() {
        CmsTopicCenter topicCenter = new CmsTopicCenter();
        ContentKey contentKey = new ContentKey();
        contentKey.setType("TopicCenter");
        contentKey.setIdentifier(123L);

        topicCenter.setContentKey(contentKey);
        topicCenter.setTitle("title");
        topicCenter.setMetaDescription("meta description goes here");
        topicCenter.setMetaKeywords("meta keywords");
        topicCenter.setImageUrl("/res/img/feature_image.jpg");
        topicCenter.setImageAltText("feature image alt text");
        topicCenter.setContentProviderLogoUrl("/res/img/mostPopularContent/most_popular_sm_thumb.jpg");
        topicCenter.setContentProviderLogoAltText("content provider logo alt text");

        CmsCategory firstCat = new CmsCategory();
        firstCat.setId(1);
        firstCat.setName("Category 1");
        CmsCategory secondCat = new CmsCategory();
        secondCat.setId(2);
        secondCat.setName("Category 2");
        CmsCategory thirdCat = new CmsCategory();
        thirdCat.setId(3);
        thirdCat.setName("Category 3");
        List<CmsCategory> secondaryKategories = new ArrayList<CmsCategory>();
        secondaryKategories.add(thirdCat);

        topicCenter.setSecondaryKategories(secondaryKategories);
        List<CmsCategory> categoryBreadcrumbs = Arrays.asList(firstCat, secondCat, thirdCat);
        List<List<CmsCategory>> breadcrumbs = new ArrayList<List<CmsCategory>>();
        breadcrumbs.add(categoryBreadcrumbs);
        topicCenter.setSecondaryKategoryBreadcrumbs(breadcrumbs);

        List<CmsLink> featureLinks = new ArrayList<CmsLink>();
        CmsLink link = new CmsLink();
        link.setTitle("Lorem Ipsum Dolor Sit Amet");
        link.setUrl("http://www.google.com");
        link.setDescription("Quisque eu velit a libero pellentesque ullamcorper.");
        link.setLinkText("In semper purus eget justo");
        featureLinks.add(link);
        link = new CmsLink();
        link.setUrl("http://www.yahoo.com");
        link.setLinkText("Morbi eu sollicitudin augue");
        featureLinks.add(link);
        link = new CmsLink();
        link.setUrl("http://www.jquery.org");
        link.setLinkText("Nunc sed turpis nisl, ac lacinia sem");
        featureLinks.add(link);
        link = new CmsLink();
        link.setUrl("http://www.greatschools.org");
        link.setLinkText("Nulla sit amet libero orci, sed euismod nisl");
        featureLinks.add(link);
        link = new CmsLink();
        link.setUrl("http://www.loremipsum.net");
        link.setLinkText("In semper purus eget justo");
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
        link.setUrl("http://www.greatschools.org");
        link.setLinkText("Community lorem ipsum lorem ipsum lorem ipsum 4");
        communityLinks.add(link);
        link = new CmsLink();
        link.setUrl("http://www.loremipsum.net");
        link.setLinkText("Community lorem ipsum lorem ipsum lorem ipsum 5");
        communityLinks.add(link);
        topicCenter.setCommunityLinks(communityLinks);

        link = new CmsLink();
        link.setUrl("http://community.greatschools.org");
        link.setLinkText("More discussions &gt;");
        topicCenter.setCommunityMoreLink(link);

        List<CmsSubtopic> subtopics = new ArrayList<CmsSubtopic>();
        for (int i = 0; i < 3; i++) {
            subtopics.add(getSampleSubtopic(i));
        }
        topicCenter.setSubtopics(subtopics);

        return topicCenter;
    }

    private CmsSubtopic getSampleSubtopic(int i) {
        CmsSubtopic subtopic = new CmsSubtopic();
        subtopic.setTitle("subtopic title " + i);
        subtopic.setDescription("subtopic description " + i);
        subtopic.setImageUrl("/res/img/");
        subtopic.setImageAltText("subtopic image alt text " + i);

        List<CmsLink> links = new ArrayList<CmsLink>();
        for (int m = 0; m < 4; m++) {
            CmsLink link = new CmsLink();
            link.setUrl("http://www.google.com");
            link.setLinkText("sub link lorem ipsum lorem ipsum " + i + "." + m);
            links.add(link);
        }
        subtopic.setLinks(links);

        List<CmsSubSubtopic> subs = new ArrayList<CmsSubSubtopic>();
        for (int j = 0; j < 1; j++) {
            subs.add(getSampleSubSubtopic(i,j));
        }
        subtopic.setSubSubtopics(subs);

        return subtopic;
    }

    private CmsSubSubtopic getSampleSubSubtopic(int i, int j) {
        CmsSubSubtopic sub = new CmsSubSubtopic();
        sub.setTitle("sub title " + i + "." + j);

        CmsCategory cat = new CmsCategory();
        cat.setName("sub category " + i + "." + j);
        cat.setType("topic");
        List<CmsCategory> cats = new ArrayList<CmsCategory>();
        cats.add(cat);
        sub.setKategories(cats);

        List<CmsLink> links = new ArrayList<CmsLink>();
        for (int k = 0; k < 4; k++) {
            CmsLink link = new CmsLink();
            link.setUrl("http://www.google.com");
            link.setLinkText("sub link lorem ipsum lorem ipsum " + i + "." + j + "." + k);
            links.add(link);
        }
        sub.setLinks(links);

        CmsLink link = new CmsLink();
        link.setUrl("http://www.google.com");
        link.setLinkText("sub more link lorem ipsum lorem ipsum " + i + "." + j);
        sub.setMoreLink(link);

        sub.setMoreLinkText("sub more link text " + i + "." + j);
        return sub;
    }
    // END sample topic center methods

    public void setPublicationDao(IPublicationDao publicationDao) {
        _publicationDao = publicationDao;
    }

    public void setCmsDiscussionBoardDao(ICmsDiscussionBoardDao cmsDiscussionBoardDao) {
        _cmsDiscussionBoardDao = cmsDiscussionBoardDao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public Boolean isUseAdKeywords() {
        return _useAdKeywords;
    }

    public void setUseAdKeywords(Boolean useAdKeywords) {
        _useAdKeywords = useAdKeywords;
    }

    public Long getTopicCenterContentID() {
        return _topicCenterContentID;
    }

    public void setTopicCenterContentID(Long topicCenterContentID) {
        _topicCenterContentID = topicCenterContentID;
    }

    public CmsContentLinkResolver getCmsFeatureEmbeddedLinkResolver() {
        return _cmsFeatureEmbeddedLinkResolver;
    }

    public void setCmsFeatureEmbeddedLinkResolver(CmsContentLinkResolver cmsFeatureEmbeddedLinkResolver) {
        _cmsFeatureEmbeddedLinkResolver = cmsFeatureEmbeddedLinkResolver;
    }
}