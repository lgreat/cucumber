package gs.web.school;

import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.web.geo.StateSpecificFooterHelper;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component("schoolProfileHelper")
public class SchoolProfileHelper {
    public static final String BEAN_ID = "schoolProfileHelper";
    private static final Log _log = LogFactory.getLog(SchoolProfileHelper.class);

    @Autowired
    private IGeoDao _geoDao;

    @Autowired
    private StateSpecificFooterHelper _stateSpecificFooterHelper;

    // ===================== main methods ===================================

    public void updateModel(HttpServletRequest request, HttpServletResponse response,
                            School school, Map<String, Object> model, Integer overallRating) {
        try {
            if (school != null) {
                handleAdKeywords(request, school, overallRating);
                handleCityCookie(request, response, school);
                handleStateSpecificFooter(request, school, model);

                // TODO-13114 Chuck will check with Liana on if it's needed for new profile. let's put it in the model just in case, which means this is todo
                handlePinItButton(request, school, model);
            }
        } catch (Exception e) {
            _log.error("Error fetching data for school profile: " + e, e);
        }

    }

    // ===================== helper methods ===================================


    protected void handleAdKeywords(HttpServletRequest request, School school) {
        handleAdKeywords(request, school, null);
    }

    protected void handleAdKeywords(HttpServletRequest request, School school, Integer overallRating) {
        try {
            PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
            String levelAbbrev = school.getLevelCode().getLowestLevel().getName();
            String schoolType = school.getType().getSchoolTypeName();
            request.setAttribute("schoolType", schoolType);

            String adPageName = "school/" + schoolType + '/' + levelAbbrev;
            request.setAttribute("adPageName", adPageName);

            // GS-5064
            String county = school.getCounty();
            String city = school.getCity();

            if (null != pageHelper) {
                pageHelper.addAdKeyword("type", schoolType);
                for (LevelCode.Level level : school.getLevelCode().getIndividualLevelCodes()) {
                    pageHelper.addAdKeywordMulti("level", level.getName());
                }
                pageHelper.addAdKeyword("county", county);
                pageHelper.addAdKeyword("city", city);
                pageHelper.addAdKeyword("school_id", school.getId().toString());
                pageHelper.addAdKeyword("zipcode", school.getZipcode());

                // set district name and id ad attributes only if there's a district and school is not preschool-only
                if (school.getDistrictId() != 0 && school.getLevelCode() != null
                        && !school.getLevelCode().toString().equals("p")) {
                    pageHelper.addAdKeyword("district_name", school.getDistrict().getName());
                    pageHelper.addAdKeyword("district_id", String.valueOf(school.getDistrictId()));
                }

                if (overallRating != null && overallRating > 0 && overallRating < 11) {
                    pageHelper.addAdKeyword("gs_rating", String.valueOf(overallRating));
                }
            }
        } catch (Exception e) {
            _log.warn("Error constructing ad keywords in school profile header");
        }
    }

    // for new 2012 school profile
    protected void handleCityCookie(HttpServletRequest request, HttpServletResponse response, School school) {
        City city = _geoDao.findCity(school.getDatabaseState(), school.getCity());
        handleCityCookie(request, response, city);
    }

    // for old 2010 school profile
    protected void handleCityCookie(HttpServletRequest request, HttpServletResponse response, City city) {
        if (city != null && response != null) {
            PageHelper.setCityIdCookie(request, response, city);
        }
    }

    protected void handleStateSpecificFooter(HttpServletRequest request, School school, Map<String, Object> model) {
        _stateSpecificFooterHelper.displayPopularCitiesForState(school.getDatabaseState(), model);
    }

    protected void handlePinItButton(HttpServletRequest request, School school, Map<String, Object> model) {
        String schoolSummary = school.getSchoolSummary();
        if (schoolSummary != null) {
            // remove links and other HTML tags to convert summary to plain text
            model.put("plainTextSummary", schoolSummary.replaceAll("\\<.*?>",""));
        }

        // always provide school overview URL for Pin It button, even if on different school profile page
        UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
        model.put("schoolOverviewUrl", urlBuilder.asFullUrl(request));
    }



    // ===================== setters for unit tests ===================================


    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public void setStateSpecificFooterHelper(StateSpecificFooterHelper stateSpecificFooterHelper) {
        _stateSpecificFooterHelper = stateSpecificFooterHelper;
    }
}
