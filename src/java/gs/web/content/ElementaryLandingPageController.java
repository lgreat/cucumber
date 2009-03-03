package gs.web.content;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.data.util.NameValuePair;
import gs.data.state.State;
import gs.web.util.google.GoogleSpreadsheetDao;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.context.SessionContext;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ElementaryLandingPageController extends AbstractController {
    protected final Log _log = LogFactory.getLog(getClass());

    private String _viewName;
    private ITableDao _tableDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        injectWorksheetName(request);

        Map<String, Object> model = new HashMap<String, Object>();
        try {
            List<ITableRow> rows = getTableDao().getAllRows();
            for (ITableRow row: rows) {
                String key = row.getString("key");
                String text = row.getString("text");
                String url = row.getString("url");
                if (StringUtils.contains(key, "articleLink")) {
                    text = StringUtils.replace(text, " ", "&nbsp;");
                    NameValuePair<String, String> textUrl = new NameValuePair<String, String>(text, url);
                    List<NameValuePair<String, String>> textUrls = (List<NameValuePair<String, String>>) model.get(key);
                    if (textUrls == null) {
                        textUrls = new ArrayList<NameValuePair<String, String>>();
                    }
                    textUrls.add(textUrl);
                    model.put(key, textUrls);
                } else {
                    model.put(key, text);
                    model.put(key + "Url", url);
                }
            }
            //model.put("kTeaserText", getTableDao().getFirstRowByKey("key", "kTeaserText").getString("text"));
        } catch (Exception e) {
            _log.error(e, e);
        }

        SessionContext context = SessionContextUtil.getSessionContext(request);
        String userCityName = "Los Angeles";
        State userState = context.getStateOrDefault();
        if (context.getCity() != null) {
            userCityName = context.getCity().getName();
        }
        model.put("userCity", userCityName);
        model.put("userState", userState);
        return new ModelAndView(getViewName(), model);
    }

    /**
     * This could be spring configured, except that it varies depending on what hostname this request
     * is running off of
     * @see gs.web.community.CommunityQuestionPromoController
     */
    protected void injectWorksheetName(HttpServletRequest request) {
        GoogleSpreadsheetDao castDao = (GoogleSpreadsheetDao) getTableDao();
        castDao.getSpreadsheetInfo().setWorksheetName(getWorksheet(request));
    }

    protected String getWorksheet(HttpServletRequest request) {
        String worksheetName;
        if (UrlUtil.isDevEnvironment(request.getServerName()) && !UrlUtil.isStagingServer(request.getServerName())) {
            worksheetName = "od6";
        } else if (UrlUtil.isStagingServer(request.getServerName())) {
            worksheetName = "od6";
        } else {
            worksheetName = "od6";
        }

        return worksheetName;
    }
    

    public ITableDao getTableDao() {
        return _tableDao;
    }

    public void setTableDao(ITableDao tableDao) {
        _tableDao = tableDao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
