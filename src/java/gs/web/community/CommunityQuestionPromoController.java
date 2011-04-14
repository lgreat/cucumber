package gs.web.community;

import gs.data.util.google.GoogleSpreadsheetDao;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class CommunityQuestionPromoController extends AbstractController {
    public static final String BEAN_ID = "/community/communityQuestionPromo.module";
    public static final String WORKSHEET_PRIMARY_ID_COL = "code";
    public static final String DEFAULT_CODE = "school/rating.page";
    public static final String DEFAULT_QUESTION_LINK_TEXT = "Join the discussion in our Parent Community >";
    public static final String MODEL_QUESTION_TEXT = "questionText";
    public static final String MODEL_QUESTION_LINK = "questionLink";
    public static final String MODEL_QUESTION_LINK_TEXT = "questionLinkText";
    public static final String MODEL_USERNAME = "username";
    public static final String MODEL_USER_ID = "userId";
    public static final String MODEL_MEMBER_URL = "memberUrl";
    public static final String MODEL_AVATAR_ALT = "avatarAlt";
    public static final String MODEL_AVATAR_URL = "avatarUrl";

    private String _viewName;
    private ITableDao _tableDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        injectWorksheetName(request);
        Map<String, Object> model = new HashMap<String, Object>();
        loadSpreadsheetDataIntoModel(request, model);
        addExtraInfoToModel(model, request);
        return new ModelAndView(_viewName, model);
    }

    /**
     * This could be spring configured, except that it varies depending on what hostname this request
     * is running off of
     */
    protected void injectWorksheetName(HttpServletRequest request) {
        GoogleSpreadsheetDao castDao = (GoogleSpreadsheetDao) getTableDao();
        castDao.getSpreadsheetInfo().setWorksheetName(getWorksheet(request));
    }

    /**
     * Moved some logic out of the module into the controller, broken out into this method
     * for ease of testing. Basically adds or modifies a couple values in the model
     */
    protected void addExtraInfoToModel(Map<String, Object> model, HttpServletRequest request) {
        String comLandingUrl = "http://" +
                SessionContextUtil.getSessionContext(request).getSessionContextUtil().getCommunityHost(request);
        String link = (String) model.get(MODEL_QUESTION_LINK);
        if (StringUtils.isBlank(link)) {
            // defaults to com landing
            model.put(MODEL_QUESTION_LINK, comLandingUrl);
        } else if (link.startsWith("/")) {
            // update relative links to absolute
            link = comLandingUrl + link;
            model.put(MODEL_QUESTION_LINK, link);
        }

        String linkText = (String) model.get(MODEL_QUESTION_LINK_TEXT);
        if (StringUtils.isBlank(linkText)) {
            model.put(MODEL_QUESTION_LINK_TEXT, DEFAULT_QUESTION_LINK_TEXT);
        }

        // set avatar image alt tag
        String username = (String) model.get(MODEL_USERNAME);
        if (StringUtils.isBlank(username)) {
            model.put(MODEL_AVATAR_ALT, "Avatar");
            model.put(MODEL_MEMBER_URL, comLandingUrl + "/members");
        } else {
            model.put(MODEL_AVATAR_ALT, username);
            model.put(MODEL_MEMBER_URL, comLandingUrl + "/members/" + username);
        }

        // set avatar image link
        String userId = (String) model.get(MODEL_USER_ID);
        if (StringUtils.isBlank(userId)) {
            model.put(MODEL_AVATAR_URL, "/res/img/community/avatar_40x40.gif");
        } else {
            model.put(MODEL_AVATAR_URL, comLandingUrl + "/avatar?id=" + userId + "&width=40&height=40");
        }

    }

    protected void loadSpreadsheetDataIntoModel(HttpServletRequest request, Map<String, Object> model) {
        String code = getCode(request);
        ITableRow row = getTableDao().getRandomRowByKey
                (WORKSHEET_PRIMARY_ID_COL, code);

        fillModel(request, model, row);
    }

    protected void fillModel(HttpServletRequest request, Map<String, Object> model, ITableRow row) {
        if (row != null) {
            model.put(MODEL_QUESTION_TEXT, row.get("text"));
            model.put(MODEL_QUESTION_LINK, row.get("link"));
            model.put(MODEL_QUESTION_LINK_TEXT, row.get("linktext"));
            model.put(MODEL_USERNAME, row.get("username"));
            model.put(MODEL_USER_ID, row.get("memberid"));
        }
    }

    /**
     * Allows the worksheet to be overridden by the request. If not, it returns od6 (first worksheet)
     * for dev and developer boxes, oda for staging, and od4 for production and all else
     */
    public String getWorksheet(HttpServletRequest request) {
        String worksheet = request.getParameter("worksheet");
        if (StringUtils.isBlank(worksheet)) {
            UrlUtil util = new UrlUtil();
            if (util.isDevEnvironment(request.getServerName()) && !util.isStagingServer(request.getServerName())) {
                worksheet = "od6"; // od6 is always the first worksheet
            } else if (util.isStagingServer(request.getServerName())) {
                worksheet = "oda";
            } else {
                worksheet = "od4";
            }
        }
        return worksheet;
    }

    /**
     * Allows the code defining which row in the worksheet to use to be overridden by the request.
     */
    public String getCode(HttpServletRequest request) {
        String code = request.getParameter(WORKSHEET_PRIMARY_ID_COL);
        if (StringUtils.isBlank(code)) {
            code =  DEFAULT_CODE;
        }
        return code;
    }

    public ITableDao getTableDao() {
        return _tableDao;
    }

    public void setTableDao(ITableDao tableDao) {
        _tableDao = tableDao;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public String getViewName() {
        return _viewName;
    }
}
