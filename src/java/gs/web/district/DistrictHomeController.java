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
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;

import gs.data.util.table.ITableRow;
import gs.data.util.table.ITableDao;
import gs.data.geo.City;
import gs.data.geo.IGeoDao;
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
    private ISchoolDao _schoolDao;
    private IGeoDao _geoDao;
    public static final String DEV_DEFINITIONS_TAB = "od6";
    public static final String STAGING_DEFINITIONS_TAB = "od6";
    public static final String LIVE_DEFINITIONS_TAB = "od6";
    public static final String DEV_DISTRICT_BOILERPLATE_TAB = "od6";
    public static final String STAGING_DISTRICT_BOILERPLATE_TAB = "od6";
    public static final String LIVE_DISTRICT_BOILERPLATE_TAB = "od6";
    public static final String DEV_STATE_BOILERPLATE_TAB = "od4";
    public static final String STAGING_STATE_BOILERPLATE_TAB = "od4";
    public static final String LIVE_STATE_BOILERPLATE_TAB = "od4";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        final SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        State state = sessionContext.getStateOrDefault();
        String districtIdStr = request.getParameter(PARAM_DISTRICT_ID);
        HashMap definitions = getKeyTermDefinitionsFromSpreadSheet(request);

        // TODO: What should the behavior be if there is no district_id specified
        if (!StringUtils.isBlank(districtIdStr) && StringUtils.isNumeric(districtIdStr)) {
            int districtId = Integer.parseInt(districtIdStr);
            District district = _districtDao.findDistrictById(state, districtId);
            model.put("district", district);
            getBoilerPlateForDistrict("CA","717",model,request);
            getBoilerPlateForState("CA",model,request);
//            getSpreadSheetRow(state.getAbbreviation(),districtIdStr,model);
            if(model.get("acronym")!= null && !"".equals(model.get("acronym"))){
                model.put("acronymOrName",model.get("acronym"));
            }else{
                model.put("acronymOrName",district.getName());
            }
        }
        getBoilerPlateWithKeyTerms(model,definitions);
        loadTopRatedSchools(model,sessionContext);
        return new ModelAndView(getViewName(), model);
    }

    protected void getBoilerPlateForDistrict(String state,String districtId, Map model,HttpServletRequest request){
        GoogleSpreadsheetDao boilerPlateCastDao = (GoogleSpreadsheetDao) getBoilerPlateTableDao();
        boilerPlateCastDao.getSpreadsheetInfo().setWorksheetName(getWorksheetForBoilerPlates(request,true));

        List<ITableRow> rows = getBoilerPlateTableDao().getRowsByKey("id",districtId);
        if(rows.size() > 0){
            for(ITableRow row :rows){
                if(row.get("state").equals(state)){
                    for (Object columnName : row.getColumnNames()) {
                        model.put(columnName.toString(),row.get(columnName).toString());
                    }
                }
            }
        }

    }

    protected void getBoilerPlateForState(String state, Map model,HttpServletRequest request){
        GoogleSpreadsheetDao boilerPlateCastDao = (GoogleSpreadsheetDao) getBoilerPlateTableDao();
        boilerPlateCastDao.getSpreadsheetInfo().setWorksheetName(getWorksheetForBoilerPlates(request,false));

        List<ITableRow> rows = getBoilerPlateTableDao().getRowsByKey("state",state);
        if(rows.size() >0){
            model.put("stateBoilerPlate",rows.get(0).get("boilerplate"));    
        }

    }


    protected HashMap getKeyTermDefinitionsFromSpreadSheet(HttpServletRequest request){
        GoogleSpreadsheetDao definitionsCastDao = (GoogleSpreadsheetDao) getDefinitionsTableDao();
        definitionsCastDao.getSpreadsheetInfo().setWorksheetName(getWorksheetForDefinition(request));

        List<ITableRow> rows = getDefinitionsTableDao().getAllRows();
        HashMap<String,String> map = new HashMap<String, String>();
        if(rows.size() >0){
            for(ITableRow row :rows){
                map.put(row.get("key").toString(),row.get("value").toString());
            }
        }

        return map;
    }

    protected void getBoilerPlateWithKeyTerms(Map model,HashMap definitions){

        String boilerPlate = model.get("boilerplate").toString().replaceAll("\n","<br/>");
        Set s = definitions.keySet();
        Iterator i = s.iterator();
        StringBuffer definitionsDiv = new StringBuffer();

        while(i.hasNext()){
            String key = i.next().toString();
            String span = "<span class=\"keyTerms\" onmouseout=\"hidePopup('"+key.replaceAll(" ","")+"');\"  onmouseover=\"showPopup(event,'"+key.replaceAll(" ","")+"');\">"+key+"</span>";
            int length = boilerPlate.length();
            boilerPlate = boilerPlate.replaceAll("\\b"+key+"\\b",span);
            if(length < boilerPlate.length()){
                definitionsDiv.append("<div id=\""+key.replaceAll(" ","")+"\" class=\"transparent\"><div class=\"keyTermsWrapper\"><h3>Key Terms</h3><div class=\"keyTermDefinition\">"+key+"</div>"+definitions.get(key)+"</div></div>");
            }                       
        }
        model.put("boilerplate",boilerPlate);
        model.put("definitionsDiv",definitionsDiv);
    }

    protected void loadTopRatedSchools(Map<String, Object> model, SessionContext context) {
           City userCity = null;
           if (context.getCity() != null) {
               userCity = context.getCity();
           }
           model.put("cityObject", userCity);

           List<ISchoolDao.ITopRatedSchool> topRatedSchools =
                   getSchoolDao().findTopRatedSchoolsInCity(userCity, 1, null, 5);
           if (topRatedSchools.size() > 0) {
               model.put("topRatedSchools", topRatedSchools);
               List<School> schools = new ArrayList<School>(topRatedSchools.size());
               for (ISchoolDao.ITopRatedSchool s: topRatedSchools) {
                   schools.add(s.getSchool());
               }
               model.put("topSchools", schools);
           }
       }


     
    protected String getWorksheetForBoilerPlates(HttpServletRequest request,boolean isDistrict) {
        String worksheetName ="";
        if (UrlUtil.isDevEnvironment(request.getServerName()) && !UrlUtil.isStagingServer(request.getServerName())) {
            if(isDistrict){
                worksheetName = DEV_DISTRICT_BOILERPLATE_TAB;
            }else{
                worksheetName = DEV_STATE_BOILERPLATE_TAB;
            }
        } else if (UrlUtil.isStagingServer(request.getServerName())) {
            if(isDistrict){
                worksheetName = STAGING_DISTRICT_BOILERPLATE_TAB;
            }else{
                worksheetName = STAGING_STATE_BOILERPLATE_TAB;
            }
        } else {
            if(isDistrict){
                worksheetName = LIVE_DISTRICT_BOILERPLATE_TAB;
            }else{
                worksheetName = LIVE_STATE_BOILERPLATE_TAB;
            }
        }

        return worksheetName;
    }

     protected String getWorksheetForDefinition(HttpServletRequest request) {
        String worksheetName ="";
        if (UrlUtil.isDevEnvironment(request.getServerName()) && !UrlUtil.isStagingServer(request.getServerName())) {
            worksheetName = DEV_DEFINITIONS_TAB;

        } else if (UrlUtil.isStagingServer(request.getServerName())) {
            worksheetName = STAGING_DEFINITIONS_TAB;
        } else {
            worksheetName = LIVE_DEFINITIONS_TAB;
        }

        return worksheetName;
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

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }


}