package gs.web.backToSchool;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.web.util.google.GoogleSpreadsheetDao;

/**
 * @author npatury@greatschools.net
 */

public class BackToSchoolHomeController extends AbstractController {
    public String _viewName;
    private ITableDao _communityTableDao;
    public static final String DEV_COMMUNITY = "od6";
    public static final String STAGING_COMMUNITY = "od8";
    public static final String LIVE_COMMUNITY = "od4";
    public static final String DEV_POPULARARTICLE = "od6";
    public static final String STAGING_POPULARTICLE = "od8";
    public static final String LIVE_POPULARARTICLE = "od4";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
     Map<String, Object> model = new HashMap<String, Object>();
        GoogleSpreadsheetDao communityTableDao = (GoogleSpreadsheetDao) getCommunityTableDao();
        communityTableDao.getSpreadsheetInfo().setWorksheetName("od6");

        List<ITableRow> rows = getCommunityTableDao().getRowsByKey("id","467");
        System.out.println("----------------------------------------------"+rows.size());
        
       
     return new ModelAndView(getViewName(), model);
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
}
