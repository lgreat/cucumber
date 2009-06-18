package gs.web.content.cms;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gs.web.util.UrlBuilder;

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
}
