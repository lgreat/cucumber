package gs.web.district;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;

import gs.data.util.table.ITableRow;
import gs.data.util.table.ITableDao;
import gs.web.util.google.GoogleSpreadsheetDao;
import gs.web.util.UrlUtil;

import java.util.*;

/**
 * @author droy@greatschools.net
 * @author npatury@greatschools.net
 */
public class DistrictHomeController extends AbstractController {
    public static final String PARAM_DISTRICT_ID = "district_id";
    String _viewName;
    private ITableDao _tableDao;
    private IDistrictDao _districtDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        injectWorksheetName(request);
        final SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        State state = sessionContext.getStateOrDefault();
        String districtIdStr = request.getParameter(PARAM_DISTRICT_ID);
        if (!StringUtils.isBlank(districtIdStr) && StringUtils.isNumeric(districtIdStr)) {
            int districtId = Integer.parseInt(districtIdStr);
            District district = _districtDao.findDistrictById(state, districtId);
            model.put("district", district);
            getSpreadSheetRow("CA","717",model);
//            getSpreadSheetRow(state.getAbbreviation(),districtIdStr,model);
        }
        
        return new ModelAndView(getViewName(), model);
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
            worksheetName = "od7";
        } else {
            worksheetName = "od4";
        }

        return worksheetName;
    }

    protected HashMap getSpreadSheetRow(String state,String districtId, Map model){
        List<ITableRow> rows = getTableDao().getRowsByKey("id",districtId);
        HashMap<String,String> map = new HashMap<String, String>();
        for(ITableRow row :rows){
            if(row.get("state").equals(state)){
                for (Object columnName : row.getColumnNames()) {
                    model.put(columnName.toString(),row.get(columnName).toString());
                }
                return map;
            }
        }
        return null;
    }

    public IDistrictDao getDistrictDao() {
        return _districtDao;
    }

    public void setDistrictDao(IDistrictDao districtDao) {
        _districtDao = districtDao;
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