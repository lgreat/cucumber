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

        replaceGreatSchoolsUrlsInArticle(article, request);

        // paginate after transforms have been done on entire body
        String pageNum = request.getParameter("page");
        if (StringUtils.equals("all", pageNum)) {
            pageNum = "-1";
        }
        if (StringUtils.isNotBlank(pageNum) && StringUtils.isNumeric(pageNum) ||
                StringUtils.equals("-1", pageNum)) {
            try {
                article.setCurrentPageNum(Integer.parseInt(pageNum));
            } catch (NumberFormatException e) {
                _log.warn("Invalid page number " + pageNum + " for article uri " + uri);
            }
        }

        model.put("article", article);
        model.put("uri", uri);
        return new ModelAndView(VIEW_NAME, model);
    }

    /**
     * Replace GS URL codes in body and summary with relative paths to the respective pages.
     */
    protected void replaceGreatSchoolsUrlsInArticle(CmsArticle article, HttpServletRequest request) {
        article.setBody(replaceGreatSchoolsUrlInString(article.getBody(), request));
        article.setSummary(replaceGreatSchoolsUrlInString(article.getSummary(), request));
        article.setSidebar(replaceGreatSchoolsUrlInString(article.getSidebar(), request));
    }

    /**
     * Replace GS URL codes in provided string. This depends on UrlBuilder
     */
    protected String replaceGreatSchoolsUrlInString(String text, HttpServletRequest request) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        Matcher matcher = _pattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String vpagePattern = matcher.group(1);
            UrlBuilder urlBuilder = new UrlBuilder(vpagePattern);
            matcher.appendReplacement(sb, urlBuilder.asSiteRelative(request));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public void setCmsArticleDao(ICmsArticleDao articleDao) {
        _articleDao = articleDao;
    }
}
