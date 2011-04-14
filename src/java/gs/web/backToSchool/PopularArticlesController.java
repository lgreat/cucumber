package gs.web.backToSchool;

import gs.data.util.NameValuePair;
import gs.data.util.google.GoogleSpreadsheetDao;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.web.util.UrlUtil;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author npatury@greatschools.org
 */

public class PopularArticlesController  extends AbstractController {

    private ITableDao _articleTableDao;

    public static final String DEV_POPULARARTICLE = "od6";
    public static final String STAGING_POPULARTICLE = "od4";
    public static final String LIVE_POPULARARTICLE = "od7";

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
            int count = 0;
            for (int i = 0; i < size; i++) {
                if(rows.get(i).get("text") != null && rows.get(i).get("url") != null){
                    String article = rows.get(i).get("text").toString();
                    String link = rows.get(i).get("url").toString();
                    NameValuePair<String, String> articleLink = new NameValuePair<String, String>(article, link);
                    articleLinks.add(articleLink);
                    count++;
                }
                
            }
            model.put("popularArticles", articleLinks);
            //the count variable is used to check that there is text and a url....
            //otherwise the module displays one dotted line after another if there is a
            // row for  "backToSchool_popularArticles" and no text and url
            model.put("popularArticlesSize", count);
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

