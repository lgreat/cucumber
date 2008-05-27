package gs.web.content;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.content.IArticleDao;
import gs.data.content.Article;
import gs.data.content.ArticleManager;
import gs.data.content.ArticleCategoryEnum;
import gs.data.state.State;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.PageHelper;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * This is the controller for the article page.  A single article and any
 * associated tools are displayed on article.page given an article id.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class ArticleController extends AbstractController {
    private static final Logger _log = Logger.getLogger(ArticleController.class);

    /** Spring Bean ID */
    public static final String BEAN_ID = "/content/article.page";

    /** Map of states to article ids, initialized in static block. */
    protected static final Map<State, Integer> _achievementMap = new HashMap<State, Integer>();

    /** Article id GET request parameter */
    public static final String PARAM_AID = "aid";
    /** Whether this is a new style or old style article */
    public static final String MODEL_NEW_ARTICLE = "newArticle";
    /** Article object itself */
    public static final String MODEL_ARTICLE = "article";
    /** Article meta description */
    public static final String MODEL_ARTICLE_META_DESCRIPTOR = "articleMetaDescriptor";
    /** Article meta keywords */
    public static final String MODEL_ARTICLE_META_KEYWORDS = "articleMetaKeywords";

    /** Article title -- after string replacement */
    public static final String MODEL_ARTICLE_TITLE = "articleTitle";
    /** Article abstract -- after string replacement */
    public static final String MODEL_ARTICLE_ABSTRACT = "articleAbstract";
    /** Article text -- after string replacement */
    public static final String MODEL_ARTICLE_TEXT = "articleText";
    /** Article hier1 -- after string replacement */
    public static final String MODEL_HIER1 = "articleHier1";
    /** Ad Attribute keyword */
    public static final String GAM_AD_ATTRIBUTE_KEY = "editorial";

    /** Provides access to database articles */
    private IArticleDao _articleDao;

    /** Article category manipulation methods */
    private ArticleManager _articleManager;

    /**
     * Regular expression for state-specific content.
     * (\^gstate=\"[a-z,\!]+\"\^)([^\^]+)(\^\/gstate\^)
     */
    private Pattern _pattern =
            Pattern.compile("(\\^gstate=\\\"[a-z,\\!]+\\\"\\^)([^\\^]+)(\\^\\/gstate\\^)");

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) {
        State state = SessionContextUtil.getSessionContext(request).getStateOrDefault();
        int articleId = parseArticleId(state, request.getParameter(PARAM_AID));
        Map<String, Object> model = new HashMap<String, Object>();

        if (articleId > 0) {
            Article article = _articleDao.getArticleFromId(articleId);
            if (article != null) {
                model.put(MODEL_NEW_ARTICLE, isArticleNewStyle(article));
                model.put(MODEL_ARTICLE, article);
                String articleTitle = processArticleString(state, article.getTitle());
                model.put(MODEL_ARTICLE_TITLE, articleTitle);
                model.put(MODEL_HIER1, processHier1(articleTitle));
                model.put(MODEL_ARTICLE_ABSTRACT, processArticleString(state, article.getAbstract()));
                model.put(MODEL_ARTICLE_TEXT, processArticleString(state, article.getArticleText()));
                model.put(MODEL_ARTICLE_META_DESCRIPTOR, article.getMetaDescriptor());
                model.put(MODEL_ARTICLE_META_KEYWORDS, article.getMetaKeywords());

                PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
                List categories = _articleManager.getCategories(article.getCategory());
                for (Object obj : categories) {
                    ArticleCategoryEnum category = (ArticleCategoryEnum)obj;
                    pageHelper.addAdKeywordMulti(GAM_AD_ATTRIBUTE_KEY, category.getName());
                }
            }
        } else {
            _log.warn("Bad article id: " + request.getParameter(PARAM_AID));
        }
        return new ModelAndView("content/article", model);
    }

    protected int parseArticleId(State state, String articleId) {
        if (StringUtils.equals("achievement", articleId)) {
            return _achievementMap.get(state);
        } else {
            try {
                return new Integer(articleId);
            } catch (NumberFormatException nfe) {
                return -1;
            }
        }
    }

    /**
     * Performs various string replacements on the given text.
     * Replaces $LONGSTATE with the state's long name.
     * Replaces $STATE with the state abbreviation.
     * Removes nopagebreaks and pagebreak spans.
     * Calls processArticleForStateSubstrings for state-specific content
     */
    protected String processArticleString(State state, String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
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
     * Transform the title to be a valid part of an omniture hier1.  Heir1 is comma separated
     * so commas in the title need to be removed.  Hier1 is stored in a double quotes javascript string
     * so the double quotes in the title need to be escaped.
     * @param text Text to convert to valid Hier1 component
     * @return valid hier1 component
     */
    protected String processHier1(String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }

        // Hier1 is comma separated so remove commas
        text = StringUtils.replace(text, ",", "");
        // Hier1 is stored in a double quoted javascript variable so escape double quotes
        text = StringUtils.replace(text, "\"", "\\\"");
        // Collapse multiple spaces to 1
        text = text.replaceAll("\\s+", " ");

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

    public ArticleManager getArticleManager() {
        return _articleManager;
    }

    public void setArticleManager(ArticleManager articleManager) {
        _articleManager = articleManager;
    }


    static {
        _achievementMap.put(State.CA, 866);
        _achievementMap.put(State.TX, 862);
        _achievementMap.put(State.FL, 867);
        _achievementMap.put(State.AZ, 856);
        _achievementMap.put(State.NY, 869);
        _achievementMap.put(State.IL, 870);
        _achievementMap.put(State.OH, 851);
        _achievementMap.put(State.PA, 871);
        _achievementMap.put(State.MI, 872);
        _achievementMap.put(State.NJ, 873);
        _achievementMap.put(State.GA, 874);
        _achievementMap.put(State.NC, 875);
        _achievementMap.put(State.WA, 865);
        _achievementMap.put(State.VA, 864);
        _achievementMap.put(State.CO, 868);
        _achievementMap.put(State.MD, 876);
        _achievementMap.put(State.TN, 877);
        _achievementMap.put(State.IN, 887);
        _achievementMap.put(State.MA, 889);
        _achievementMap.put(State.MO, 854);
        _achievementMap.put(State.LA, 890);
        _achievementMap.put(State.WI, 891);
        _achievementMap.put(State.CT, 892);
        _achievementMap.put(State.MN, 893);
        _achievementMap.put(State.OK, 894);
        _achievementMap.put(State.OR, 895);
        _achievementMap.put(State.SC, 896);
        _achievementMap.put(State.KY, 897);
        _achievementMap.put(State.NV, 898);
        _achievementMap.put(State.NM, 853);
        _achievementMap.put(State.DC, 899);
        _achievementMap.put(State.AL, 904);
        _achievementMap.put(State.KS, 905);
        _achievementMap.put(State.MS, 906);
        _achievementMap.put(State.AR, 907);
        _achievementMap.put(State.UT, 908);
        _achievementMap.put(State.IA, 909);
        _achievementMap.put(State.NE, 910);
        _achievementMap.put(State.NH, 911);
        _achievementMap.put(State.ME, 912);
        _achievementMap.put(State.WV, 913);
        _achievementMap.put(State.HI, 914);
        _achievementMap.put(State.RI, 915);
        _achievementMap.put(State.ID, 916);
        _achievementMap.put(State.MT, 917);
        _achievementMap.put(State.DE, 918);
        _achievementMap.put(State.AK, 919);
        _achievementMap.put(State.VT, 920);
        _achievementMap.put(State.SD, 921);
        _achievementMap.put(State.WY, 922);
        _achievementMap.put(State.ND, 923);
    }
}
