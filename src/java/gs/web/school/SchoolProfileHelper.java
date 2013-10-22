package gs.web.school;

import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.hubs.HubConfig;
import gs.data.hubs.IHubConfigDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.review.Review;
import gs.web.geo.CityHubHelper;
import gs.web.geo.StateSpecificFooterHelper;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

@Component("schoolProfileHelper")
public class SchoolProfileHelper {
    public static final String BEAN_ID = "schoolProfileHelper";
    private static final Log _log = LogFactory.getLog(SchoolProfileHelper.class);

    @Autowired
    private IGeoDao _geoDao;

    @Autowired
    private StateSpecificFooterHelper _stateSpecificFooterHelper;
    @Autowired
    private IHubConfigDao _hubConfigDao;

    // ===================== main methods ===================================

    public void updateModel(HttpServletRequest request, HttpServletResponse response,
                            School school, Map<String, Object> model, Integer overallRating) {
        try {
            if (school != null) {
                handleAdKeywords(request, school, overallRating);
                handleCityCookie(request, response, school);
                handleStateSpecificFooter(request, school, model);

                // GS-13114 commenting out pin-it button code for now because getting school summary hits the database,
                //          the URL builder call needs refactoring because it's the same as the relCanonical URL builder,
                //          and the pin-it button is not needed for the initial roll-out of the new school profile
                //handlePinItButton(request, school, model);
            }
        } catch (Exception e) {
            _log.error("Error fetching data for school profile: " + e, e);
        }

    }

    public boolean isHubAdFree(School school) {
        Integer collectionId = null;
        try {
            String collectionIdAsString = school.getMetadataValue(School.METADATA_COLLECTION_ID_KEY);
           if(collectionIdAsString != null) collectionId = new Integer(collectionIdAsString);
        }
        catch (NumberFormatException ex) {
            _log.error("School Profile Helper - unable to convert the collection id meta value in string to integer " +
                    "for the school id " + school.getId() + " in state " + school.getDatabaseState().getAbbreviation()
                    + "\n", ex.fillInStackTrace());
        }

        HubConfig hubConfig = getHubConfigDao().getConfigFromCollectionIdAndKey(collectionId, CityHubHelper.SHOW_ADS_KEY);
        return  (hubConfig != null && "false".equals(hubConfig.getValue()));
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

    protected static Date getSchoolLastModified(School school, Review latestNonPrincipalReview) {
        // get the most recent of these two dates: school.getModified(), and the most recent published non-principal review
        // see similar logic in ParentReviewController.java, SchoolOverview2010Controller.java
        Date lastModifiedDate = school.getModified();
        if (latestNonPrincipalReview != null) {
            Date mostRecentPublishedNonPrincipalReview = latestNonPrincipalReview.getPosted();
            if (lastModifiedDate == null ||
                    (mostRecentPublishedNonPrincipalReview != null &&
                            lastModifiedDate.compareTo(mostRecentPublishedNonPrincipalReview) < 0)) {
                lastModifiedDate = mostRecentPublishedNonPrincipalReview;
            }
        }
        return lastModifiedDate;
    }


    // ===================== setters for unit tests ===================================


    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public void setStateSpecificFooterHelper(StateSpecificFooterHelper stateSpecificFooterHelper) {
        _stateSpecificFooterHelper = stateSpecificFooterHelper;
    }

    public IHubConfigDao getHubConfigDao() {
        return _hubConfigDao;
    }

    public void setHubConfigDao(IHubConfigDao _hubConfigDao) {
        this._hubConfigDao = _hubConfigDao;
    }
}
