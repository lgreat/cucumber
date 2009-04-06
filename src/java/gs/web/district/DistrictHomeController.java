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
import gs.data.school.census.CensusInfoFactory;
import gs.data.school.census.ICensusDataSetDao;
import gs.data.school.census.CensusDataType;
import gs.data.school.census.DistrictCensusValue;
import gs.data.state.State;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;

import gs.data.util.table.ITableRow;
import gs.data.util.table.ITableDao;
import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.test.rating.DistrictRating;
import gs.data.test.rating.IDistrictRatingDao;
import gs.web.util.google.GoogleSpreadsheetDao;
import gs.web.util.UrlUtil;

import java.util.*;

/**
 * @author droy@greatschools.net
 * @author npatury@greatschools.net
 */
public class DistrictHomeController extends AbstractController {
    public static final String PARAM_DISTRICT_ID = "district_id";
    public static final String PARAM_SCHOOL_ID = "school_id";
    String _viewName;
    private ITableDao _boilerPlateTableDao;
    private ITableDao _definitionsTableDao;
    private IDistrictDao _districtDao;
    private ISchoolDao _schoolDao;
    private IGeoDao _geoDao;
    private IDistrictRatingDao _districtRatingDao;
    private ICensusDataSetDao _censusDataSetDao;
    public static final String DEV_DISTRICT_BOILERPLATE_TAB = "od6";
    public static final String STAGING_DISTRICT_BOILERPLATE_TAB = "od6";
    public static final String LIVE_DISTRICT_BOILERPLATE_TAB = "od6";
    public static final String DEV_STATE_BOILERPLATE_TAB = "od4";
    public static final String STAGING_STATE_BOILERPLATE_TAB = "od4";
    public static final String LIVE_STATE_BOILERPLATE_TAB = "od4";
    public boolean _isDistrictPresent;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        Map<String, Object> pageModel = new HashMap<String, Object>();
        final SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        State state = sessionContext.getStateOrDefault();
        String districtIdStr = request.getParameter(PARAM_DISTRICT_ID);

        // TODO: What should the behavior be if there is no district_id specified
        if (!StringUtils.isBlank(districtIdStr) && StringUtils.isNumeric(districtIdStr)) {
            int districtId = Integer.parseInt(districtIdStr);
            District district = _districtDao.findDistrictById(state, districtId);
            model.put("district", district);

            String schoolIdStr = request.getParameter(PARAM_SCHOOL_ID);
            if (!StringUtils.isBlank(schoolIdStr) && StringUtils.isNumeric(schoolIdStr)) {
                int schoolId = Integer.parseInt(schoolIdStr);
                School school = _schoolDao.getSchoolById(state, schoolId);
                model.put("school", school);
            }

            getBoilerPlateForDistrict(state.getAbbreviation(),districtIdStr,pageModel,request);
            getBoilerPlateForState(state.getAbbreviation(),pageModel,request);
            if(pageModel.get("acronym")!= null && !"".equals(pageModel.get("acronym"))){
                pageModel.put("acronymOrName", pageModel.get("acronym"));
            }else{
                pageModel.put("acronymOrName", district.getName());
            }
            loadDistrictRating(district, pageModel);
            loadDistrictEnrollment(district, pageModel);
        }

        loadTopRatedSchools(pageModel,sessionContext);
        pageModel.put("isDistrictPresent",_isDistrictPresent);

        model.put("model", pageModel);
        return new ModelAndView(getViewName(), model);
    }

    protected void getBoilerPlateForDistrict(String state,String districtId, Map<String, Object> model,HttpServletRequest request){
        GoogleSpreadsheetDao boilerPlateCastDao = (GoogleSpreadsheetDao) getBoilerPlateTableDao();
        boilerPlateCastDao.getSpreadsheetInfo().setWorksheetName(getWorksheetForBoilerPlates(request,true));

        _isDistrictPresent = false;
        List<ITableRow> rows = getBoilerPlateTableDao().getRowsByKey("id",districtId);
        if(rows!= null &&rows.size() > 0){
            for(ITableRow row :rows){
                if(row.get("state").equals(state)){
                    for (Object columnName : row.getColumnNames()) {
                        model.put(columnName.toString(),(row.get(columnName)) == null ? "":row.get(columnName));
                    }
                    model.put("boilerplate",model.get("boilerplate").toString().replaceAll("\n","<br/>"));
                    _isDistrictPresent = true;
                }
            }
        }else{
            model.put("id","");
            model.put("state","");
            model.put("name","");
            model.put("acronym","");
            model.put("choicelink","");
            model.put("locatorlink","");
            model.put("superintendent","");
            model.put("boilerplate","");
            model.put("distrctBoilerplateHeading","");
        }

    }

    protected void getBoilerPlateForState(String state, Map<String, Object> model,HttpServletRequest request){
        GoogleSpreadsheetDao boilerPlateCastDao = (GoogleSpreadsheetDao) getBoilerPlateTableDao();
        boilerPlateCastDao.getSpreadsheetInfo().setWorksheetName(getWorksheetForBoilerPlates(request,false));

        List<ITableRow> rows = getBoilerPlateTableDao().getRowsByKey("state",state);
        if(rows.size() >0){
            model.put("stateBoilerplate",(rows.get(0).get("boilerplate")) == null ? "": rows.get(0).get("boilerplate").toString().replaceAll("\n","<br/>"));
            model.put("stateBoilerplateHeading",(rows.get(0).get("stateboilerplateheading")) == null ? "": rows.get(0).get("stateboilerplateheading").toString());
        }else{
            model.put("stateBoilerplate","");
            model.put("stateBoilerplateHeading","");
        }
    }

    protected void loadDistrictEnrollment(District district,Map<String, Object> model){

        CensusInfoFactory cif = new CensusInfoFactory(_censusDataSetDao);
        cif.setCensusDataSetDao(_censusDataSetDao);
        DistrictCensusValue dicv = cif.getCensusInfo().getLatestValue(district, CensusDataType.STUDENTS_ENROLLMENT);
        if (dicv != null) {
            model.put("districtEnrollment",dicv.getValueInteger());
        }

    }

    protected void loadDistrictRating(District district,Map<String, Object> model){
        DistrictRating districtRating = getDistrictRatingDao().getDistrictRatingByDistrict(district);
        if (districtRating != null) {
            model.put("rating", districtRating.getRating());
        }
    }

    protected void loadTopRatedSchools(Map<String, Object> model, SessionContext context) {
           City userCity = null;
           if (context.getCity() != null) {
               userCity = context.getCity();
           } else {
                District district = (District)model.get("district");
                userCity = new City(district.getPhysicalAddress().getCity(), context.getStateOrDefault());
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

    public IDistrictRatingDao getDistrictRatingDao() {
        return _districtRatingDao;
    }

    public void setDistrictRatingDao(IDistrictRatingDao districtRatingDao) {
        _districtRatingDao = districtRatingDao;
    }
    public ICensusDataSetDao getCensusDataSetDao() {
        return _censusDataSetDao;
    }

    public void setCensusDataSetDao(ICensusDataSetDao censusDataSetDao) {
        _censusDataSetDao = censusDataSetDao;
    }


}