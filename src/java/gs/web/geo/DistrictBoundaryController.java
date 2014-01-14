package gs.web.geo;

import gs.data.geo.ISchoolBoundaryDao;
import gs.data.geo.SchoolBoundary;
import gs.data.hubs.HubCityMapping;
import gs.data.hubs.HubConfig;
import gs.data.hubs.IHubCityMappingDao;
import gs.data.hubs.IHubConfigDao;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.school.SchoolProfileHelper;
import gs.web.util.PageHelper;
import gs.web.request.RequestAttributeHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;



/**
 * Created with IntelliJ IDEA.
 * User: sarora
 * Date: 1/10/14
 * Time: 10:49 AM
 * To change this template use File | Settings | File Templates.
 */
@Controller
public class DistrictBoundaryController {

    @Autowired
    private SchoolProfileHelper _schoolProfileHelper;
    @Autowired
    private ISchoolDao _schoolDao;
    @Autowired
    private IHubCityMappingDao _hubCityMappingDao;
    @Autowired
    private IHubConfigDao      _hubConfigDao;
    @Autowired
    private RequestAttributeHelper _requestAttributeHelper;
    @Autowired
    private ISchoolBoundaryDao _schoolBoundaryDao;


    @RequestMapping(method= RequestMethod.GET)
    public ModelAndView handleRequest(   @RequestParam(value = "lat" , required = false) Double lat,
                                         @RequestParam(value = "lon", required = false) Double lon,
                                         @RequestParam(value = "level", required = false) String levelParam, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView("/geo/districtBoundary");
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        final School schoolFromRequest = _requestAttributeHelper.getSchool(request);
        LevelCode.Level level = LevelCode.Level.getLevelCode(levelParam);
        List<SchoolBoundary> schoolBoundaries= new ArrayList<SchoolBoundary>();
        if (lat != null && lon != null){
            schoolBoundaries = _schoolBoundaryDao.getSchoolBoundariesContainingPoint(lat, lon, level);
        }
        String  isHubUserSet= null;

        //Check is User has been cookied so when coming back on Page new nav bar should show and Ads should be hidden.
        boolean isLocalBarCookied= pageHelper.checkHubCookiesForNavBar(request);
        if (isLocalBarCookied && schoolFromRequest == null && schoolBoundaries.isEmpty()){
            final String hubStateCookieValue= pageHelper.getHubStateCookieValue(request);
            final String hubCityCookieValue=  pageHelper.getHubCityCookieValue(request);
            final Integer hubCollectionID = _hubCityMappingDao.getCollectionIdFromCityAndState(hubCityCookieValue, State.fromString(hubStateCookieValue));
            HubConfig adHubConfig = _hubConfigDao.getConfigFromCollectionIdAndKey(hubCollectionID, CityHubHelper.SHOW_ADS_KEY);
            pageHelper.setHideAds(adHubConfig != null && "false".equals(adHubConfig.getValue()));
            modelAndView.addObject("isHubUserSet", "y");
            modelAndView.addObject("isLocal", "y");

        }


        // For SchoolID based District Map

        Integer collectionID= null;
        if (schoolFromRequest != null)
        {
        collectionID= _schoolProfileHelper.getCollectionIdForSchool(schoolFromRequest);
        isHubUserSet = setHubParameters(request, response, pageHelper, isHubUserSet, schoolFromRequest, collectionID);
       }
        // For District Map Search

        List<School> schools = new ArrayList<School>(schoolBoundaries.size());
        for (SchoolBoundary boundary: schoolBoundaries)
        {
            Integer collectionIDForSchool= null;
                if (boundary.getSchoolId() == null) {
                    continue;
                }
                School school = _schoolDao.getSchoolById(boundary.getState(), boundary.getSchoolId());
                if (school == null || !school.isActive() || !school.getLevelCode().containsLevelCode(level)) {
                    continue;
                }
                schools.add(school);
                collectionIDForSchool= _schoolProfileHelper.getCollectionIdForSchool(school);
                if (collectionIDForSchool != null && isHubUserSet == null)  {
                isHubUserSet = setHubParameters(request, response, pageHelper, isHubUserSet, school, collectionIDForSchool);
                }
        }


        if (isHubUserSet != null){
        modelAndView.addObject("isHubUserSet", "y");
        modelAndView.addObject("isLocal", "y");
        }
        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
        return modelAndView;
    }

    private String setHubParameters(HttpServletRequest request, HttpServletResponse response, PageHelper pageHelper, String hubUserSet, School school, Integer collectionID) {
        if (school != null && collectionID != null  && pageHelper != null && hubUserSet == null)   {
           final HubCityMapping hubInfo= _hubCityMappingDao.getMappingObjectByCollectionID(collectionID);
           pageHelper.setHideAds(_schoolProfileHelper.isSchoolInAdFreeHub(school));
           pageHelper.clearHubCookiesForNavBar(request, response);
           pageHelper.setHubCookiesForNavBar(request, response, hubInfo.getState(), hubInfo.getCity());
           pageHelper.setHubUserCookie(request, response);
           hubUserSet = "y";

       }
        return hubUserSet;
    }
}
