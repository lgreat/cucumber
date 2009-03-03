package gs.web.content;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.data.util.NameValuePair;
import gs.web.util.google.GoogleSpreadsheetDao;
import gs.web.util.UrlUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by IntelliJ IDEA.
 * User: npatury
 * Date: Mar 3, 2009
 * Time: 2:53:06 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractGradeLevelLandingPageController extends AbstractController {
    private ITableDao _tableDao;
    private String _viewName;
    protected final Log _log = LogFactory.getLog(getClass());
    
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        injectWorksheetName(request);
        Map<String, Object> model = new HashMap<String, Object>();
        try {
              populateModel(model,request);
        } catch (Exception e) {
            _log.error(e, e);
        }
        return new ModelAndView(getViewName(),model);
    }
    public void loadTableRowsIntoModel(Map<String, Object> model,String keySuffix) {
        ITableRow teaserCollegeRow = getTableDao().getFirstRowByKey("key", "teaserText_"+keySuffix);
        ITableRow callToActionCollegeRow = getTableDao().getFirstRowByKey("key", "callToAction_"+keySuffix);
        model.put("teaserText_"+keySuffix, teaserCollegeRow.getString("text"));
        model.put("callToAction_"+keySuffix, callToActionCollegeRow.getString("text"));
        List<ITableRow> articleLinkCollegeRows = getTableDao().getRowsByKey("key", "articleLink_"+keySuffix);
        for (ITableRow row : articleLinkCollegeRows) {
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
    protected abstract void populateModel(Map<String,Object> model,HttpServletRequest request);
    
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
