package gs.web.backToSchool;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.data.util.NameValuePair;
import gs.web.util.google.GoogleSpreadsheetDao;
import gs.web.util.UrlUtil;

/**
 * @author npatury@greatschools.net
 */

public class BackToSchoolHomeController extends AbstractController {
    public String _viewName;
    private ITableDao _communityTableDao;
    private ITableDao _articleTableDao;
    public static final String DEV_COMMUNITY = "od6";
    public static final String STAGING_COMMUNITY = "od8";
    public static final String LIVE_COMMUNITY = "od4";
    public static final String DEV_POPULARARTICLE = "od6";
    public static final String STAGING_POPULARTICLE = "od8";
    public static final String LIVE_POPULARARTICLE = "od4";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
     Map<String, Object> model = new HashMap<String, Object>();
        getArticlesAndLinks(model,request);
    getCommunityTopicsAndLinks(model,request);
     return new ModelAndView(getViewName(), model);
    }

     protected void getArticlesAndLinks(Map<String, Object> model,HttpServletRequest request){
        GoogleSpreadsheetDao communityTableDao = (GoogleSpreadsheetDao) getCommunityTableDao();
        communityTableDao.getSpreadsheetInfo().setWorksheetName(getWorksheetName(request,true));
        List<NameValuePair<String, String>> articleLinks = new ArrayList<NameValuePair<String, String>>();
        List<ITableRow> rows = getCommunityTableDao().getRowsByKey("page","backToSchool_popularArticles");
         if(rows != null && rows.size() > 0){
             int size = rows.size();
             for(int i=0;i<size;i++){
                 String article = rows.get(i).get("text").toString();
                 String link = rows.get(i).get("url").toString();
                 NameValuePair<String, String> articleLink = new NameValuePair<String, String>(article,link);
                articleLinks.add(articleLink);

             }
              model.put("popularArticles", articleLinks);
              model.put("popularArticlesSize", size);
         }
        
     }
    protected void getCommunityTopicsAndLinks(Map<String, Object> model,HttpServletRequest request){
        GoogleSpreadsheetDao communityTableDao = (GoogleSpreadsheetDao) getCommunityTableDao();
        communityTableDao.getSpreadsheetInfo().setWorksheetName(getWorksheetName(request,true));
        List<NameValuePair<String, String>> articleLinks = new ArrayList<NameValuePair<String, String>>();
        List<ITableRow> rows = getCommunityTableDao().getRowsByKey("page","backToSchool_popularArticles");
         if(rows != null && rows.size() > 0){
             int size = rows.size();
             for(int i=0;i<size;i++){
                 String article = rows.get(i).get("text").toString();
                 String link = rows.get(i).get("url").toString();
                 NameValuePair<String, String> articleLink = new NameValuePair<String, String>(article,link);
                articleLinks.add(articleLink);
             }
              model.put("communityTopics", articleLinks);
              model.put("communityTopicsSize", size);
         }

     }

     protected String getWorksheetName(HttpServletRequest request,boolean isCommunity) {
        String worksheetName ="";
        if (UrlUtil.isDevEnvironment(request.getServerName()) && !UrlUtil.isStagingServer(request.getServerName())) {
            if(isCommunity){
                worksheetName = DEV_COMMUNITY;
            }else{
                worksheetName = DEV_POPULARARTICLE;
            }
        } else if (UrlUtil.isStagingServer(request.getServerName())) {
            if(isCommunity){
                worksheetName = STAGING_COMMUNITY;
            }else{
                worksheetName = STAGING_POPULARTICLE;
            }
        } else {
            if(isCommunity){
                worksheetName = LIVE_COMMUNITY;
            }else{
                worksheetName = LIVE_POPULARARTICLE;
            }
        }
         return worksheetName;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public ITableDao getCommunityTableDao() {
        return _communityTableDao;
    }

    public void setCommunityTableDao(ITableDao boilerPlateTableDao) {
        _communityTableDao = boilerPlateTableDao;
    }
    
     public ITableDao getArticleTableDao() {
        return _articleTableDao;
    }

    public void setArticleTableDao(ITableDao articleTableDao) {
        _articleTableDao = articleTableDao;
    }
}
