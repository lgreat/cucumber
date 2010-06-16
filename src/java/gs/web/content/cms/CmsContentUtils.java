package gs.web.content.cms;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

import gs.web.util.UrlBuilder;
import gs.data.cms.IPublicationDao;
import gs.data.util.SpringUtil;
import gs.data.content.cms.*;

public class CmsContentUtils {
    public static final String URL_PREFIX = "gs://";
    public static final String URL_PAGE_PATTERN = "[^\"\\?]*";
    public static final String URL_PARAM_PATTERN = "(\\?[^\"]+)?";

    // pattern = /gs:\/\/([^"\?]*(\?[^"]+)?)/
    private static Pattern _pattern = Pattern.compile(URL_PREFIX  + "(" + URL_PAGE_PATTERN + URL_PARAM_PATTERN + ")");

    public static String replaceGreatSchoolsUrlInString(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        Matcher matcher = _pattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String vpagePattern = matcher.group(1);
            UrlBuilder urlBuilder = new UrlBuilder(vpagePattern);
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            matcher.appendReplacement(sb, urlBuilder.asSiteRelative(request));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static IPublicationDao getPublicationDao() {
        return (IPublicationDao) SpringUtil.getApplicationContext().getBean("publicationDao");
    }

    private static final long HEALTH_AND_DEVELOPMENT_CATEGORY_ID = 123;
    private static final long HEALTH_AND_NUTRITION_CATEGORY_ID = 154;
    private static final long ACADEMICS_AND_ACTIVITIES_CATEGORY_ID = 127;
    private static final long HOMEWORK_HELP_CATEGORY_ID = 140;
    private static final long PREP_FOR_COLLEGE_CATEGORY_ID = 151;
    private static final long MEDIA_AND_KIDS_CATEGORY_ID = 162;
    private static final long SPECIAL_EDUCATION_CATEGORY_ID = 225;
    private static final long IMPROVE_YOUR_SCHOOL_CATEGORY_ID = 125;
    private static final long FIND_A_SCHOOL_CATEGORY_ID = 143;
    private static final long MOVING_CATEGORY_ID = 144;
    private static final long BACK_TO_SCHOOL_ID = 141;
    private static final long ELEMENTARY_SCHOOL_ID = 217;

    private static final Map<Long,String> CATEGORY_MICROSITE_LINK_TEXT_MAP = new HashMap<Long,String>();
    static {
        CATEGORY_MICROSITE_LINK_TEXT_MAP.put(ELEMENTARY_SCHOOL_ID, "Elementary School");
    }

    private static final Map<Long,UrlBuilder> CATEGORY_MICROSITE_URLBUILDER_MAP = new HashMap<Long,UrlBuilder>();
    static {
        CATEGORY_MICROSITE_URLBUILDER_MAP.put(HEALTH_AND_NUTRITION_CATEGORY_ID, new UrlBuilder(UrlBuilder.getVPage("HEALTHY_KIDS")));
        CATEGORY_MICROSITE_URLBUILDER_MAP.put(MEDIA_AND_KIDS_CATEGORY_ID, new UrlBuilder(UrlBuilder.getVPage("MEDIA_CHOICES")));
        CATEGORY_MICROSITE_URLBUILDER_MAP.put(FIND_A_SCHOOL_CATEGORY_ID, new UrlBuilder(UrlBuilder.getVPage("SCHOOL_CHOICE_CENTER")));
        CATEGORY_MICROSITE_URLBUILDER_MAP.put(MOVING_CATEGORY_ID, new UrlBuilder(UrlBuilder.getVPage("MOVING_WITH_KIDS")));
        CATEGORY_MICROSITE_URLBUILDER_MAP.put(ELEMENTARY_SCHOOL_ID, new UrlBuilder(UrlBuilder.getVPage("ELEMENTARY_SCHOOL")));
    }

    private static final Map<Long, ContentKey> CATEGORY_TOPIC_CENTER_CONTENT_KEY_MAP = new HashMap<Long,ContentKey>();
    static {
        CATEGORY_TOPIC_CENTER_CONTENT_KEY_MAP.put(HEALTH_AND_DEVELOPMENT_CATEGORY_ID, new ContentKey("TopicCenter",1539L));
        CATEGORY_TOPIC_CENTER_CONTENT_KEY_MAP.put(ACADEMICS_AND_ACTIVITIES_CATEGORY_ID, new ContentKey("TopicCenter",1540L));
        CATEGORY_TOPIC_CENTER_CONTENT_KEY_MAP.put(HOMEWORK_HELP_CATEGORY_ID, new ContentKey("TopicCenter",1544L));
        CATEGORY_TOPIC_CENTER_CONTENT_KEY_MAP.put(PREP_FOR_COLLEGE_CATEGORY_ID, new ContentKey("TopicCenter",1542L));
        CATEGORY_TOPIC_CENTER_CONTENT_KEY_MAP.put(SPECIAL_EDUCATION_CATEGORY_ID, new ContentKey("TopicCenter",1541L));
        CATEGORY_TOPIC_CENTER_CONTENT_KEY_MAP.put(IMPROVE_YOUR_SCHOOL_CATEGORY_ID, new ContentKey("TopicCenter",1543L));
    }

    // GS-8475: note that this cache does not refresh; to reflect changes, restart Tomcat
    private static final Map<Long, CmsLink> CATEGORY_LINK_CACHE = new HashMap<Long,CmsLink>();

    public static List<CmsLink> getBreadcrumbs(List<CmsCategory> categoryBreadcrumbs, String language, HttpServletRequest request) {
        List<CmsLink> breadcrumbs = new ArrayList<CmsLink>();

        for (CmsCategory category : categoryBreadcrumbs) {
            CmsLink link;

            if (CATEGORY_LINK_CACHE.containsKey(category.getId())) {
                link = CATEGORY_LINK_CACHE.get(category.getId());
            } else {
                link = new CmsLink();
                link.setLinkText(category.getName());

                UrlBuilder builder;
                boolean cacheable = true;
                if (CATEGORY_MICROSITE_URLBUILDER_MAP.containsKey(category.getId())) {
                    builder = CATEGORY_MICROSITE_URLBUILDER_MAP.get(category.getId());
                    if (CATEGORY_MICROSITE_LINK_TEXT_MAP.containsKey(category.getId())) {
                        link.setLinkText(CATEGORY_MICROSITE_LINK_TEXT_MAP.get(category.getId()));
                    }
                } else if (CATEGORY_TOPIC_CENTER_CONTENT_KEY_MAP.containsKey(category.getId())) {
                    builder = new UrlBuilder(CATEGORY_TOPIC_CENTER_CONTENT_KEY_MAP.get(category.getId()));

                    ContentKey contentKey = CATEGORY_TOPIC_CENTER_CONTENT_KEY_MAP.get(category.getId());
                    CmsTopicCenter topicCenter = getPublicationDao().populateByContentId(contentKey.getIdentifier(), new CmsTopicCenter());
                    link.setLinkText(topicCenter.getTitle());
                } else {
                    builder = new UrlBuilder(UrlBuilder.CMS_CATEGORY_BROWSE, String.valueOf(category.getId()), (String)null, (String)null, language);
                    cacheable = false;
                }
                if (builder != null) {
                    link.setUrl(builder.asSiteRelative(request));
                }

                if (cacheable) {
                    CATEGORY_LINK_CACHE.put(category.getId(), link);
                }
            }

            breadcrumbs.add(link);
        }

        return breadcrumbs;
    }

    private static final String ALMOND_NET_CATEGORY_PRESCHOOL = "preschool";
    private static final String ALMOND_NET_CATEGORY_ELEMENTARY_SCHOOL = "elementary school";
    private static final String ALMOND_NET_CATEGORY_MIDDLE_SCHOOL = "middle school";
    private static final String ALMOND_NET_CATEGORY_HIGH_SCHOOL = "high school";
    private static final String ALMOND_NET_CATEGORY_COLLEGE_PREP = "college prep";
    private static final String ALMOND_NET_CATEGORY_EDUCATION = "education";

    public static String getAlmondNetCategory(Set<CmsCategory> categories) {
        if (categories == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }

        boolean isCollegePrep = false;
        boolean isPreschool = false;
        boolean isElementary = false;
        boolean isMiddle = false;
        boolean isHigh = false;
        int numGrades = 0;

        for (CmsCategory category : categories) {
            if (category.getId() == CmsConstants.COLLEGE_PREP_CATEGORY_ID) {
                isCollegePrep = true;
            } else if (category.getId() == CmsConstants.PRESCHOOL_CATEGORY_ID) {
                isPreschool = true;
                numGrades++;
            } else if (category.getId() == CmsConstants.ELEMENTARY_SCHOOL_CATEGORY_ID) {
                isElementary = true;
                numGrades++;
            } else if (category.getId() == CmsConstants.MIDDLE_SCHOOL_CATEGORY_ID) {
                isMiddle = true;
                numGrades++;
            } else if (category.getId() == CmsConstants.HIGH_SCHOOL_CATEGORY_ID) {
                isHigh = true;
                numGrades++;
            }
        }

        if (isCollegePrep) {
            return ALMOND_NET_CATEGORY_COLLEGE_PREP;
        } else if (numGrades == 1) {
            if (isPreschool) {
                return ALMOND_NET_CATEGORY_PRESCHOOL;
            } else if (isElementary) {
                return ALMOND_NET_CATEGORY_ELEMENTARY_SCHOOL;
            } else if (isMiddle) {
                return ALMOND_NET_CATEGORY_MIDDLE_SCHOOL;
            } else if (isHigh) {
                return ALMOND_NET_CATEGORY_HIGH_SCHOOL;
            } else {
                // this should never happen
                return ALMOND_NET_CATEGORY_EDUCATION;
            }
        } else {
            return ALMOND_NET_CATEGORY_EDUCATION;
        }
    }

    public static String getAlmondNetCategory(CmsContent content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }

        return getAlmondNetCategory(content.getUniqueKategoryBreadcrumbs());
    }
}
