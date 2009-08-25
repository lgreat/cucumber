package gs.web.content.cms;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import gs.web.util.UrlBuilder;
import gs.data.cms.IPublicationDao;
import gs.data.util.SpringUtil;
import gs.data.content.cms.ContentKey;
import gs.data.content.cms.CmsLink;
import gs.data.content.cms.CmsCategory;
import gs.data.content.cms.CmsTopicCenter;

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

    private static final long GREAT_PARENTING_CATEGORY_ID = 123;
    private static final long HEALTH_AND_NUTRITION_CATEGORY_ID = 154;
    private static final long ACADEMICS_AND_ACTIVITIES_CATEGORY_ID = 127;
    private static final long HOMEWORK_HELP_CATEGORY_ID = 140;
    private static final long PREP_FOR_COLLEGE_CATEGORY_ID = 151;
    private static final long MEDIA_AND_KIDS_CATEGORY_ID = 162;
    private static final long LEARNING_DISABILITIES_CATEGORY_ID = 130;
    private static final long IMPROVE_YOUR_SCHOOL_CATEGORY_ID = 125;
    private static final long FIND_A_SCHOOL_CATEGORY_ID = 143;
    private static final long MOVING_CATEGORY_ID = 144;
    private static final long BACK_TO_SCHOOL_ID = 141;

    private static final Map<Long,String> CATEGORY_MICROSITE_LINK_TEXT_MAP = new HashMap<Long,String>();
    static {
        CATEGORY_MICROSITE_LINK_TEXT_MAP.put(MEDIA_AND_KIDS_CATEGORY_ID, "Media & kids");
    }

    private static final Map<Long,UrlBuilder> CATEGORY_MICROSITE_URLBUILDER_MAP = new HashMap<Long,UrlBuilder>();
    static {
        CATEGORY_MICROSITE_URLBUILDER_MAP.put(HEALTH_AND_NUTRITION_CATEGORY_ID, new UrlBuilder(UrlBuilder.getVPage("HEALTHY_KIDS")));
        CATEGORY_MICROSITE_URLBUILDER_MAP.put(MEDIA_AND_KIDS_CATEGORY_ID, new UrlBuilder(UrlBuilder.getVPage("MEDIA_CHOICES")));
        CATEGORY_MICROSITE_URLBUILDER_MAP.put(FIND_A_SCHOOL_CATEGORY_ID, new UrlBuilder(UrlBuilder.getVPage("SCHOOL_CHOICE_CENTER")));
        CATEGORY_MICROSITE_URLBUILDER_MAP.put(MOVING_CATEGORY_ID, new UrlBuilder(UrlBuilder.getVPage("MOVING_WITH_KIDS")));
        CATEGORY_MICROSITE_URLBUILDER_MAP.put(BACK_TO_SCHOOL_ID, new UrlBuilder(UrlBuilder.getVPage("BACK_TO_SCHOOL")));
    }

    private static final Map<Long, ContentKey> CATEGORY_TOPIC_CENTER_CONTENT_KEY_MAP = new HashMap<Long,ContentKey>();
    static {
        CATEGORY_TOPIC_CENTER_CONTENT_KEY_MAP.put(GREAT_PARENTING_CATEGORY_ID, new ContentKey("TopicCenter",1539L));
        CATEGORY_TOPIC_CENTER_CONTENT_KEY_MAP.put(ACADEMICS_AND_ACTIVITIES_CATEGORY_ID, new ContentKey("TopicCenter",1540L));
        CATEGORY_TOPIC_CENTER_CONTENT_KEY_MAP.put(HOMEWORK_HELP_CATEGORY_ID, new ContentKey("TopicCenter",1544L));
        CATEGORY_TOPIC_CENTER_CONTENT_KEY_MAP.put(PREP_FOR_COLLEGE_CATEGORY_ID, new ContentKey("TopicCenter",1542L));
        CATEGORY_TOPIC_CENTER_CONTENT_KEY_MAP.put(LEARNING_DISABILITIES_CATEGORY_ID, new ContentKey("TopicCenter",1541L));
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
                    builder = new UrlBuilder(UrlBuilder.CMS_CATEGORY_BROWSE, String.valueOf(category.getId()), null, null, language);
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
}
