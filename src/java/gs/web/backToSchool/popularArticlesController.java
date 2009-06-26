package gs.web.backToSchool;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import gs.web.util.google.GoogleSpreadsheetDao;
import gs.web.util.UrlUtil;
import gs.data.util.NameValuePair;
import gs.data.util.table.ITableRow;
import gs.data.util.table.ITableDao;

/**
 * @author npatury@greatschools.net
 */

public class popularArticlesController extends AbstractController {

    private ITableDao _articleTableDao;

    public static final String DEV_POPULARARTICLE = "od6";
    public static final String STAGING_POPULARTICLE = "od8";
    public static final String LIVE_POPULARARTICLE = "od4";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ModelAndView modelAndView = new ModelAndView("/backToSchool/popularArticles");
        Map<String, Object> model = modelAndView.getModel();
        getArticlesAndLinks(model, request);
        return modelAndView;

    }

    protected void getArticlesAndLinks(Map<String, Object> model, HttpServletRequest request) {
        GoogleSpreadsheetDao articleTableDao = (GoogleSpreadsheetDao) getArticleTableDao();
        articleTableDao.getSpreadsheetInfo().setWorksheetName(getWorksheetName(request));
        List<NameValuePair<String, String>> articleLinks = new ArrayList<NameValuePair<String, String>>();
        List<ITableRow> rows = getArticleTableDao().getRowsByKey("page", "backToSchool_popularArticles");
        if (rows != null && rows.size() > 0) {
            int size = rows.size();
            for (int i = 0; i < size; i++) {
                String article = rows.get(i).get("text").toString();
                String link = rows.get(i).get("url").toString();
                NameValuePair<String, String> articleLink = new NameValuePair<String, String>(article, link);
                articleLinks.add(articleLink);
            }
            model.put("popularArticles", articleLinks);
            model.put("popularArticlesSize", size);
        }
    }

    protected String getWorksheetName(HttpServletRequest request) {
        String worksheetName = "";
        if (UrlUtil.isDevEnvironment(request.getServerName()) && !UrlUtil.isStagingServer(request.getServerName())) {
            worksheetName = DEV_POPULARARTICLE;
        } else if (UrlUtil.isStagingServer(request.getServerName())) {
            worksheetName = STAGING_POPULARTICLE;
        } else {
            worksheetName = LIVE_POPULARARTICLE;
        }
        return worksheetName;
    }

    public ITableDao getArticleTableDao() {
        return _articleTableDao;
    }

    public void setArticleTableDao(ITableDao articleTableDao) {
        _articleTableDao = articleTableDao;
    }

}
