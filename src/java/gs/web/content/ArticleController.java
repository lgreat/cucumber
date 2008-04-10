package gs.web.content;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.content.IArticleDao;
import gs.data.content.Article;
import gs.data.state.State;
import gs.web.util.context.SessionContextUtil;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * This is the controller for the article page.  A single article and any
 * associated tools are displayed on article.page given an article id.
 *  
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class ArticleController extends AbstractController {
    private static final Logger _log = Logger.getLogger(ArticleController.class);

    /** Spring Bean ID */
    public static final String BEAN_ID = "/content/article.page";

    /** Article id GET request parameter */
    public static final String PARAM_AID = "aid";
    /** Whether this is a new style or old style article */
    public static final String MODEL_NEW_ARTICLE = "newArticle";
    /** Article object itself */
    public static final String MODEL_ARTICLE = "article";

    /** Article title -- after string replacement */
    public static final String MODEL_ARTICLE_TITLE = "articleTitle";
    /** Article abstract -- after string replacement */
    public static final String MODEL_ARTICLE_ABSTRACT = "articleAbstract";
    /** Article text -- after string replacement */
    public static final String MODEL_ARTICLE_TEXT = "articleText";

    /** Provides access to database articles */
    private IArticleDao _articleDao;
    /**
     * Regular expression for state-specific content.
     * (\^gstate=\"[a-z,\!]+\"\^)([^\^]+)(\^\/gstate\^)
     */
    private Pattern _pattern =
            Pattern.compile("(\\^gstate=\\\"[a-z,\\!]+\\\"\\^)([^\\^]+)(\\^\\/gstate\\^)");

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) {
        String articleId = request.getParameter(PARAM_AID);
        Map<String, Object> model = new HashMap<String, Object>();
        State state = SessionContextUtil.getSessionContext(request).getStateOrDefault();

        try {
            Article article = _articleDao.getArticleFromId(Integer.valueOf(articleId));
            model.put(MODEL_NEW_ARTICLE, isArticleNewStyle(article));
            model.put(MODEL_ARTICLE, article);
            model.put(MODEL_ARTICLE_TITLE, processArticleString(state, article.getTitle()));
            model.put(MODEL_ARTICLE_ABSTRACT, processArticleString(state, article.getAbstract()));
            model.put(MODEL_ARTICLE_TEXT, processArticleString(state, article.getArticleText()));
        } catch (NumberFormatException nfe) {
            _log.warn("Bad article id: " + articleId);
        }
        return new ModelAndView("content/article", model);
    }

    /**
     * Performs various string replacements on the given text.
     * Replaces $LONGSTATE with the state's long name.
     * Replaces $STATE with the state abbreviation.
     * Removes nopagebreaks and pagebreak spans.
     * Calls processArticleForStateSubstrings for state-specific content
     */
    protected String processArticleString(State state, String text) {
        text = StringUtils.replace(text, "$LONGSTATE",
                state.getLongName());
        text = StringUtils.replace(text, "$STATE",
                state.getAbbreviation());
        text = StringUtils.replace(text, "<span id=\"nopagebreaks\"/>", "");
        text = StringUtils.replace(text, "<span id=\"pagebreak\"/>", "");
        text = processArticleForStateSubstrings(state, text);
        return text;
    }

    /**
     * Looks for state specific blocks of text, determines if they apply to the current
     * state, and either removes them or leaves them in depending.
     *
     * A state-specific block looks like this:
     *
     * ^gstate="ca"^Text goes here^/gstate^
     *
     * If the current state is CA, then the inner text will be included (enclosing tags removed).
     * Otherwise, the entire block is removed.
     *
     * Pattern is:
     *
     * (\^gstate=\"[a-z,\!]+\"\^)([^\^]+)(\^\/gstate\^)
     *
     * @see java.util.regex.Matcher
     */
    protected String processArticleForStateSubstrings(State state, String text) {
        Matcher matcher = _pattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String firstPart = matcher.group(1); // contains state string

            if (StringUtils.contains(firstPart.substring(8), state.getAbbreviationLowerCase())
                    && !StringUtils.contains(firstPart.substring(8), "!" + state.getAbbreviationLowerCase())) {
                // we've found a match with our state! insert the text
                // $2 is replaced with group(2)
                matcher.appendReplacement(sb, "$2");
            } else {
                // no match found for our state, remove the sequence
                matcher.appendReplacement(sb, "");
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    protected boolean isArticleNewStyle(Article article) {
        return StringUtils.contains(article.getArticleText(),
                "<div id=\"article-main\">");
    }

    public void setArticleDao(IArticleDao articleDao) {
        _articleDao = articleDao;
    }
}
