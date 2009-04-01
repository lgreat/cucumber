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
    private ITableDao _boilerPlateTableDao;
    private ITableDao _definitionsTableDao;
    private IDistrictDao _districtDao;
    public static final String DEV_DEFINITIONS = "od6";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        final SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        State state = sessionContext.getStateOrDefault();
        String districtIdStr = request.getParameter(PARAM_DISTRICT_ID);
        HashMap definitions = getKeyTermDefinitionsFromSpreadSheet(request);

        if (!StringUtils.isBlank(districtIdStr) && StringUtils.isNumeric(districtIdStr)) {
            int districtId = Integer.parseInt(districtIdStr);
            District district = _districtDao.findDistrictById(state, districtId);
            model.put("district", district);
            getBoilerPlateForDistrict("CA","717",model,request);
            getBoilerPlateForState("CA",model,request);
//            getSpreadSheetRow(state.getAbbreviation(),districtIdStr,model);
            if(model.get("acronym")!= null && !"".equals(model.get("acronym"))){
                model.put("arconymOrName",model.get("acronym"));
            }else{
                model.put("arconymOrName",district.getName());
            }
        }
        getBoilerPlateWithKeyTerms(model,definitions);
        return new ModelAndView(getViewName(), model);
    }

    protected void injectWorksheetNameForBoilerPlates(HttpServletRequest request,boolean isDistrict) {
        GoogleSpreadsheetDao boilerPlateCastDao = (GoogleSpreadsheetDao) getBoilerPlateTableDao();
        boilerPlateCastDao.getSpreadsheetInfo().setWorksheetName(getWorksheetForBoilerPlates(request,isDistrict));

    }

    protected String getWorksheetForBoilerPlates(HttpServletRequest request,boolean isDistrict) {
        String worksheetName ="";
        if (UrlUtil.isDevEnvironment(request.getServerName()) && !UrlUtil.isStagingServer(request.getServerName())) {
            if(isDistrict){
                worksheetName = DEV_DEFINITIONS;
            }else{
                worksheetName = "od4";
            }
        } else if (UrlUtil.isStagingServer(request.getServerName())) {
            worksheetName = "od7";
        } else {
            worksheetName = "od4";
        }

        return worksheetName;
    }

     protected String getWorksheetForDefinition(HttpServletRequest request) {
        String worksheetName ="";
        if (UrlUtil.isDevEnvironment(request.getServerName()) && !UrlUtil.isStagingServer(request.getServerName())) {
            worksheetName = DEV_DEFINITIONS;

        } else if (UrlUtil.isStagingServer(request.getServerName())) {
            worksheetName = "od7";
        } else {
            worksheetName = "od4";
        }

        return worksheetName;
    }

    protected void injectWorksheetNameForDefinitions(HttpServletRequest request) {
        GoogleSpreadsheetDao definitionsCastDao = (GoogleSpreadsheetDao) getDefinitionsTableDao();
        definitionsCastDao.getSpreadsheetInfo().setWorksheetName(getWorksheetForDefinition(request));
    }

    protected void getBoilerPlateForDistrict(String state,String districtId, Map model,HttpServletRequest request){
        injectWorksheetNameForBoilerPlates(request,true);

        List<ITableRow> rows = getBoilerPlateTableDao().getRowsByKey("id",districtId);
        for(ITableRow row :rows){
            if(row.get("state").equals(state)){
                for (Object columnName : row.getColumnNames()) {
                    model.put(columnName.toString(),row.get(columnName).toString());
                 }
             }
        }
    }

    protected void getBoilerPlateForState(String state, Map model,HttpServletRequest request){
        injectWorksheetNameForBoilerPlates(request,false);
        
        List<ITableRow> rows = getBoilerPlateTableDao().getRowsByKey("state",state);
        model.put("stateBoilerPlate",rows.get(0).get("boilerplate"));
    }


    protected HashMap getKeyTermDefinitionsFromSpreadSheet(HttpServletRequest request){
        injectWorksheetNameForDefinitions(request);

        List<ITableRow> rows = getDefinitionsTableDao().getAllRows();
         HashMap<String,String> map = new HashMap<String, String>();
        for(ITableRow row :rows){
            map.put(row.get("key").toString(),row.get("value").toString());
        }
        return map;
    }

    protected void getBoilerPlateWithKeyTerms(Map model,HashMap definitions){

        String boilerplate = model.get("boilerplate").toString();
        Set s = definitions.keySet();
        Iterator i = s.iterator();
        StringBuffer definitionsDiv = new StringBuffer();

        while(i.hasNext()){
            String key = i.next().toString();
            String span = "<span class=\"keyTerms\" onmouseout=\"hidePopup('"+key.replaceAll(" ","")+"');\"  onmouseover=\"showPopup(event,'"+key.replaceAll(" ","")+"');\">"+key+"</span>";
            String boilerPlateWithDefinitions = boilerplate.replaceAll("\\b"+key+"\\b",span);
            definitionsDiv.append("<div id=\""+key.replaceAll(" ","")+"\" class=\"transparent\"><div class=\"keyTermsWrapper\"><h3>Key Terms</h3><div class=\"keyTermDefinition\">"+key+"</div>"+definitions.get(key)+"</div></div>");
            model.put("boilerplate",boilerPlateWithDefinitions);
        }
        model.put("definitionsDiv",definitionsDiv);
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
    public ITableDao getBoilerPlateTableDao() {
        return _boilerPlateTableDao;
    }

    public void setBoilerPlateTableDao(ITableDao boilerPlateTableDao) {
        _boilerPlateTableDao = boilerPlateTableDao;
    }

    public ITableDao getDefinitionsTableDao() {
        return _definitionsTableDao;
    }

    public void setDefinitionsTableDao(ITableDao definitionsTableDao) {
        _definitionsTableDao = definitionsTableDao;
    }


}