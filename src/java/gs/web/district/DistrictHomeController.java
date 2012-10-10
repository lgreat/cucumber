package gs.web.district;

import gs.data.geo.City;
import gs.data.geo.ICounty;
import gs.data.geo.IGeoDao;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.census.CensusDataType;
import gs.data.school.census.CensusInfoFactory;
import gs.data.school.census.DistrictCensusValue;
import gs.data.school.census.ICensusDataSetDao;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.data.test.rating.DistrictRating;
import gs.data.test.rating.IDistrictRatingDao;
import gs.data.url.DirectoryStructureUrlFactory;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableRow;
import gs.web.geo.StateSpecificFooterHelper;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.request.RequestInfo;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.web.tracking.OmnitureTracking;
import gs.web.util.*;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.util.google.GoogleSpreadsheetDao;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author droy@greatschools.org
 * @author npatury@greatschools.org
 */
public class DistrictHomeController extends AbstractController  implements IDirectoryStructureUrlController {
    private static Logger _log = Logger.getLogger(DistrictHomeController.class);

    public static final String PARAM_DISTRICT_ID = "district_id";
    public static final String PARAM_SCHOOL_ID = "schoolId";
    String _viewName;
    private ITableDao _boilerPlateTableDao;
    private ITableDao _definitionsTableDao;
    private IDistrictDao _districtDao;
    private ISchoolDao _schoolDao;
    private IGeoDao _geoDao;
    private IDistrictRatingDao _districtRatingDao;
    private ICensusDataSetDao _censusDataSetDao;
    private StateSpecificFooterHelper _stateSpecificFooterHelper;

    public static final String MODEL_NUM_ELEMENTARY_SCHOOLS = "numElementarySchools";
    public static final String MODEL_NUM_MIDDLE_SCHOOLS = "numMiddleSchools";
    public static final String MODEL_NUM_HIGH_SCHOOLS = "numHighSchools";
    public static final String MODEL_COMPARE_E_CHECKED = "compare_e_checked";
    public static final String MODEL_COMPARE_M_CHECKED = "compare_m_checked";
    public static final String MODEL_COMPARE_H_CHECKED = "compare_h_checked";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        Map<String, Object> pageModel = new HashMap<String, Object>();
        final SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        State state = sessionContext.getStateOrDefault();

        District district = null;
        City city = null;

        DirectoryStructureUrlFields fields = (DirectoryStructureUrlFields) request.getAttribute(IDirectoryStructureUrlController.FIELDS);
        if (fields != null) {
            String cityName = fields.getCityName();
            String districtName = fields.getDistrictName();
            district = _districtDao.findDistrictByNameAndCity(state, districtName, cityName);
            if (district != null) {
                // Try to find the city in the state from the URL first
                city = _geoDao.findCity(state, cityName);
                if (city == null) {
                    // If that doesn't work try to find the city in the district's phyiscal address state
                    // There are some districts that are physically in CA but are part of the AZ school system, for
                    // example.
                    city = _geoDao.findCity(district.getPhysicalAddress().getState(), cityName);
                }
            } else {
                // GS-10800
                return new ModelAndView(new RedirectView(DirectoryStructureUrlFactory.createNewCityBrowseURIRoot(state, cityName)));
            }
        } else {
            String districtIdStr = request.getParameter(PARAM_DISTRICT_ID);
            if (!StringUtils.isBlank(districtIdStr)) {
                try {
                    int districtId = Integer.parseInt(districtIdStr);
                    district = _districtDao.findDistrictById(state, districtId);
                } catch (ObjectRetrievalFailureException orfe) {
                    // Do nothing, district remains null and will be handled below
                } catch (NumberFormatException nfe) {
                    // Do nothing, district remains null and will be handled below                    
                }
                if (district != null && district.getPhysicalAddress() != null) {
                    city = _geoDao.findCity(state, district.getPhysicalAddress().getCity());
                }
            }
        }

        if (district == null) {
            String stateStr    = (state == null)    ? "null" : state.toString();
            String cityStr     = (city == null)     ? "null" : city.getName();
            String districtStr = (district == null) ? "null" : district.getName();

            String error = "District not found where state = '" + stateStr + "', city = '" + cityStr + "', district = '" + districtStr + "'";
            BadRequestLogger.logBadRequest(_log, request, error);
            model.put("showSearchControl", Boolean.TRUE);
            model.put("title", "District not found");
            return new ModelAndView("status/error", model);
        }

        if (city == null) {
            String stateStr    = (state == null)    ? "null" : state.toString();
            String cityStr     = (city == null)     ? "null" : city.getName();
            String districtStr = (district == null) ? "null" : district.getName();

            String error = "District not found where state = '" + stateStr + "', city = '" + cityStr + "', district = '" + districtStr + "'";
            BadRequestLogger.logBadRequest(_log, request, error);
            model.put("showSearchControl", Boolean.TRUE);
            model.put("title", "City not found in state");
            return new ModelAndView("status/error", model);
        }

        try {
            RequestInfo requestInfo = RequestInfo.getRequestInfo(request);
            if (requestInfo != null && requestInfo.isShouldRenderMobileView()) {
                OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
                ot.addProp(new OmnitureTracking.Prop(OmnitureTracking.PropNumber.IncomingMobileQuery, "District Home"));
                UrlBuilder districtBrowse = new UrlBuilder(district, UrlBuilder.SCHOOLS_IN_DISTRICT);
                return new ModelAndView(new RedirectView(districtBrowse.asSiteRelative(request)));
            }
        } catch (Exception e) {
            _log.error("Error rendering mobile redirect, defaulting to desktop view.");
        }
        model.put("hasMobileView", true);

        model.put("district", district);
        pageModel.put("city", district.getPhysicalAddress().getCity());

        School school = null;
        String schoolIdStr = request.getParameter(PARAM_SCHOOL_ID);
        if (!StringUtils.isBlank(schoolIdStr)) {
            UrlBuilder relCanonical = new UrlBuilder(district, UrlBuilder.DISTRICT_HOME);
            model.put("relCanonical", relCanonical.asFullUrl(request));
            try {
                int schoolId = Integer.parseInt(schoolIdStr);
                school = _schoolDao.getSchoolById(state, schoolId);
                if(!school.getDistrictId().equals(district.getId())){
                    model.put("showSearchControl", Boolean.TRUE);
                    model.put("title", "School not found");
                    return new ModelAndView("status/error", model);
                }
                model.put("school", school);
            } catch (ObjectRetrievalFailureException orfe) {
                // Do nothing, school remains null and will be handled below
            } catch (NumberFormatException nfe) {
                // Do nothing, school remains null and will be handled below
            }
        }
        processSchoolData(school, pageModel);

        getBoilerPlateForDistrict(state.getAbbreviation(),district.getId(),pageModel,request);
        getBoilerPlateForState(state.getAbbreviation(),pageModel,request);
        if(pageModel.get("acronym")!= null && !"".equals(pageModel.get("acronym"))){
            pageModel.put("acronymOrName", pageModel.get("acronym"));
        }else{
            pageModel.put("acronymOrName", district.getName());
        }
        loadDistrictRating(district, pageModel);
        loadDistrictEnrollment(district, pageModel);
        loadTopRatedSchools(city,pageModel);
        loadNumberofGradeLevelSchools(state,district.getId().toString(),pageModel);
        pageModel.put("googleMapLink","http://maps.google.com?oi=map&amp;q="+URLEncoder.encode(district.getPhysicalAddress().getStreet() + " "+district.getPhysicalAddress().getCity()+ ", " +district.getPhysicalAddress().getState().getAbbreviationLowerCase(), "UTF-8"));
        model.put("model", pageModel);

        // Google ad keywords
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        pageHelper.addAdKeyword("state", state.getAbbreviation());
        pageHelper.addAdKeyword("city", city.getName());
        if (city.getCountyFips() != null) {
            ICounty county = _geoDao.findCountyByFipsCode(city.getCountyFips());
            if (county != null) {
                pageHelper.addAdKeyword("county", county.getName());
            }
        }
        pageHelper.addAdKeyword("district_name", district.getName());
        pageHelper.addAdKeyword("district_id", district.getId().toString());

        _stateSpecificFooterHelper.displayPopularCitiesForState(state, model);

        return new ModelAndView(getViewName(), model);
    }

    protected void getBoilerPlateForDistrict(String state,Integer districtId, Map<String, Object> model,HttpServletRequest request){

        GoogleSpreadsheetDao boilerPlateCastDao = (GoogleSpreadsheetDao) getBoilerPlateTableDao();

        Map<String,Object> row = boilerPlateCastDao.getBoilerPlateForDistrict(
                state, districtId, UrlUtil.isDevEnvironment(request.getServerName()),
                UrlUtil.isStagingServer(request.getServerName())
        );

        String boilerplate = (String) row.get("boilerplate");

        model.put("isDistrictBoilerplatePresent", !StringUtils.isEmpty(boilerplate));

        model.putAll(row);
    }

    protected void getBoilerPlateForState(String state, Map<String, Object> model,HttpServletRequest request){
        GoogleSpreadsheetDao boilerPlateCastDao = (GoogleSpreadsheetDao) getBoilerPlateTableDao();
        boilerPlateCastDao.getSpreadsheetInfo().setWorksheetName(
                boilerPlateCastDao.getWorksheetForBoilerPlates(
                        UrlUtil.isDevEnvironment(request.getServerName()),
                        UrlUtil.isStagingServer(request.getServerName())
                )
        );

        List<ITableRow> rows = getBoilerPlateTableDao().getRowsByKey("state",state);
        if(rows != null && rows.size() >0){
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
        DistrictCensusValue districtCensusValue = cif.getCensusInfo().getLatestValue(district, CensusDataType.STUDENTS_ENROLLMENT);
        if (districtCensusValue != null) {
            DecimalFormat myFormatter = new DecimalFormat("###,###");
            model.put("formattedDistrictEnrollment",myFormatter.format(districtCensusValue.getValueInteger()));
            model.put("districtEnrollment",districtCensusValue.getValueInteger());
        }
    }

    protected void loadDistrictRating(District district,Map<String, Object> model){
        DistrictRating districtRating = getDistrictRatingDao().getDistrictRatingByDistrict(district);
        if (districtRating != null) {
            model.put("rating", districtRating.getRating());
        }
    }

    protected void loadNumberofGradeLevelSchools(State state,String districtId,Map<String, Object> model){
        int num_elementary_schools = _schoolDao.countSchoolsInDistrict(state, null, LevelCode.ELEMENTARY, districtId);
        int num_middle_schools = _schoolDao.countSchoolsInDistrict(state, null, LevelCode.MIDDLE, districtId);
        int num_high_schools = _schoolDao.countSchoolsInDistrict(state, null, LevelCode.HIGH, districtId);
        model.put(MODEL_NUM_ELEMENTARY_SCHOOLS, num_elementary_schools);
        model.put(MODEL_NUM_MIDDLE_SCHOOLS, num_middle_schools);
        model.put(MODEL_NUM_HIGH_SCHOOLS, num_high_schools);

        // now that we know how many schools in each grade level, we can properly set compare_[e|m|h]_checked
        // to make sure at least one e/m/h checkbox is checked
        // note: amazingly, the num_*_schools is -1 if there are 0!!!
        if (num_elementary_schools <= 0) {
            if (num_middle_schools <= 0) {
                model.put(MODEL_COMPARE_H_CHECKED, "true");
            } else {
                model.put(MODEL_COMPARE_M_CHECKED, "true");
            }
        }
    }

    protected void loadTopRatedSchools(City userCity,Map<String, Object> model) {
           model.put("cityObject", userCity);
           List<ISchoolDao.ITopRatedSchool> topRatedSchools =
                   getSchoolDao().findTopRatedSchoolsInCityNewGSRating(userCity, 1, null, 5);
           if (topRatedSchools.size() > 0) {
               model.put("topRatedSchools", topRatedSchools);
               List<School> schools = new ArrayList<School>(topRatedSchools.size());
               for (ISchoolDao.ITopRatedSchool s: topRatedSchools) {
                   schools.add(s.getSchool());
               }
               model.put("topSchools", schools);
           }
       }


    protected void processSchoolData(School school, Map<String, Object> model) {
        if (school == null) {
            model.put("school_level_code_e", 1);
            model.put("school_level_code_m", 1);
            model.put("school_level_code_h", 1);
            model.put(MODEL_COMPARE_E_CHECKED, "true");
            model.put(MODEL_COMPARE_M_CHECKED, "");
            model.put(MODEL_COMPARE_H_CHECKED, "");
        } else {
            boolean needsCompareCheck = true;

            if (school.getLevelCode().containsLevelCode(LevelCode.Level.ELEMENTARY_LEVEL)) {
                model.put("school_level_code_e", 1);
                model.put(MODEL_COMPARE_E_CHECKED, "true");
                needsCompareCheck = false;
            }

            if (school.getLevelCode().containsLevelCode(LevelCode.Level.MIDDLE_LEVEL)) {
                model.put("school_level_code_m", 1);
                if (needsCompareCheck) {
                    model.put(MODEL_COMPARE_M_CHECKED, "true");
                    needsCompareCheck = false;
                }
            }

            if (school.getLevelCode().containsLevelCode(LevelCode.Level.HIGH_LEVEL)) {
                model.put("school_level_code_h", 1);
                if (needsCompareCheck) {
                    model.put(MODEL_COMPARE_H_CHECKED, "true");
                }
            }
        }
    }

    // required to implement IDirectoryStructureUrlController
    public boolean shouldHandleRequest(DirectoryStructureUrlFields fields) {
        if (fields == null) {
            return false;
        }

        return fields.hasState() && fields.hasCityName() && fields.hasDistrictName() && !fields.hasSchoolsLabel();
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

    public StateSpecificFooterHelper getStateSpecificFooterHelper() {
        return _stateSpecificFooterHelper;
    }

    public void setStateSpecificFooterHelper(StateSpecificFooterHelper stateSpecificFooterHelper) {
        _stateSpecificFooterHelper = stateSpecificFooterHelper;
    }
}