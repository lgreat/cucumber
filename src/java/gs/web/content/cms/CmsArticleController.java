package gs.web.content.cms;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gs.data.content.cms.ICmsArticleDao;
import gs.data.content.cms.CmsArticle;
import gs.web.util.UrlBuilder;

public class CmsArticleController extends AbstractController {
    private static final Logger _log = Logger.getLogger(CmsArticleController.class);

    /** Spring Bean ID */
    public static final String BEAN_ID = "/content/cms/article.page";
    public static final String VIEW_NAME = "content/cms/article";

    public static final String URL_PREFIX = "gs://";
    public static final String URL_PAGE_PATTERN = "[^\"\\?]*";
    public static final String URL_PARAM_PATTERN = "(\\?[^\"]+)?";

    // pattern = /gs:\/\/([^"\?]*(\?[^"]+)?)/
    private Pattern _pattern =
            Pattern.compile(URL_PREFIX  + "(" + URL_PAGE_PATTERN + URL_PARAM_PATTERN + ")");

    private ICmsArticleDao _articleDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();

        Map<String, Object> model = new HashMap<String, Object>();

        CmsArticle article = _articleDao.get(uri);

        replaceGreatSchoolsUrls(article, request);

        model.put("article", article);
        return new ModelAndView(VIEW_NAME, model);
    }

    /**
     * Replace GS URL codes in body with relative paths to the respective pages. This depends on
     * UrlBuilder.
     */
    protected void replaceGreatSchoolsUrls(CmsArticle article, HttpServletRequest request) {
        String body = article.getBody();
        if (StringUtils.isNotBlank(body)) {
            Matcher matcher = _pattern.matcher(body);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String vpagePattern = matcher.group(1);
                UrlBuilder urlBuilder = new UrlBuilder(vpagePattern);
                matcher.appendReplacement(sb, urlBuilder.asSiteRelative(request));
            }
            matcher.appendTail(sb);
            article.setBody(sb.toString());
        }
    }

    public void setCmsArticleDao(ICmsArticleDao articleDao) {
        _articleDao = articleDao;
    }
}
