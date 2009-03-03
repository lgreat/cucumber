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
import gs.web.util.google.GoogleSpreadsheetDao;
import gs.web.util.UrlUtil;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 *  @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class MiddleLandingPageController extends AbstractController {

    private String _viewName;
    private ITableDao _tableDao;
    protected final Log _log = LogFactory.getLog(getClass());

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        injectWorksheetName(request);
        Map<String, Object> model = new HashMap<String, Object>();
        try {
            ITableRow teaserRow = getTableDao().getFirstRowByKey("key","teaserText_ms_college");
            ITableRow callTopActionRow = getTableDao().getFirstRowByKey("key","callToAction_ms_college");
            model.put("teaserText_ms_college", teaserRow.getString("text"));
            model.put("callToAction_ms_college", callTopActionRow.getString("text"));
            List <ITableRow> articleLinkRows = getTableDao().getRowsByKey("key","articleLink_ms_college");
            for (ITableRow row: articleLinkRows) {
                String key = row.getString("key");
                String text = row.getString("text");
                String url = row.getString("url");
                text = StringUtils.replace(text, " ", "&nbsp;");
                    NameValuePair<String, String> textUrl = new NameValuePair<String, String>(text, url);
                    List<NameValuePair<String, String>> textUrls = (List<NameValuePair<String, String>>) model.get(key);
                    if (textUrls == null) {
                        textUrls = new ArrayList<NameValuePair<String, String>>();
                    }
                    textUrls.add(textUrl);
                    model.put(key, textUrls);
            }
        } catch (Exception e) {
            _log.error(e, e);
        }
        return new ModelAndView(_viewName,model);
    }

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

    public String getViewName() {
            return _viewName;
        }

    public void setViewName(String viewName) {
            _viewName = viewName;
        }

    public ITableDao getTableDao() {
        return _tableDao;
    }

    public void setTableDao(ITableDao tableDao) {
        _tableDao = tableDao;
    }
}
