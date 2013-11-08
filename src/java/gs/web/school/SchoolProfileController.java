package gs.web.school;

import gs.data.community.FavoriteSchool;
import gs.data.community.User;
import gs.data.hubs.HubCityMapping;
import gs.data.hubs.IHubCityMappingDao;
import gs.data.school.ISchoolDao;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.review.Review;
import gs.data.school.review.TopicalSchoolReview;
import gs.data.state.State;
import gs.web.ControllerFamily;
import gs.web.IControllerFamilySpecifier;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.request.RequestInfo;
import gs.web.util.AdUtil;
import gs.web.util.PageHelper;
import gs.web.util.RedirectView301;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author Dave Roy <mailto:droy@greatschools.org>
 */
public class SchoolProfileController extends AbstractSchoolController implements IDirectoryStructureUrlController, IControllerFamilySpecifier {
    protected static final Log _log = LogFactory.getLog(SchoolProfileController.class.getName());

    private String _viewName;
    private ControllerFamily _controllerFamily;
    private SchoolProfileDataHelper _schoolProfileDataHelper;
    private SchoolProfileHelper _schoolProfileHelper;
    public static final String SCHOOL_CALENDAR_ENABLED = "schoolCalendarEnabled";
    @Autowired
    private ISchoolDao _schoolDao;

    @Autowired
    private IHubCityMappingDao _hubCityMappingDao;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        School school = _requestAttributeHelper.getSchool(request);




        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        school.getMetadataValue("gs_rating"); // force lazy initialization
        model.put("school", school);

        final Integer zillowRegionId=_schoolProfileDataHelper.getRegionIdForZillow(school.getCity(),school.getDatabaseState().getAbbreviation());
        if (school.getCity() !=null && school.getDatabaseState()!=null && zillowRegionId!=null  )
        {
             final String formattedURLForZillowIntegration= StringUtils.lowerCase(StringUtils.replace(school.getCity(), " ", "-") + "-" + school.getDatabaseState()) ;
             model.put("formattedUrl",formattedURLForZillowIntegration)  ;
             model.put("regionID",zillowRegionId)  ;
        }

        SessionContext sc = SessionContextUtil.getSessionContext(request);
        User user = sc.getUser();
        if (user != null) {
            Set<FavoriteSchool> favoriteSchools = user.getFavoriteSchools();
            if (favoriteSchools != null && favoriteSchools.size() > 0) {
                for (FavoriteSchool fave: favoriteSchools) {
                    if (fave.getState().equals(school.getDatabaseState())
                            && fave.getSchoolId().equals(school.getId())) {
                        model.put("schoolInMSL", true);
                    }
                }
            }
        }
        // Create a map on the request used to save references to data that is to be reused.
        // This needs to be done early in the request cycle or the item attached to the request can be lost.
        AbstractDataHelper.initialize( request );

        // TODO: we could refactor this to reuse same urlbuilder as the one in SchoolProfileHelper's handlePinItButton,
        //       but this has not been done because the new profile currently does not call handlePinItButton
        //       since it's not yet in use
        UrlBuilder urlBuilder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
        String fullCanonicalUrl = urlBuilder.asFullUrl(request);
        model.put("relCanonical", fullCanonicalUrl);

        // Preschool pages should be hosted from pk.greatschools.org (GS-12127). Redirect if needed
        if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
            RequestInfo hostnameInfo = (RequestInfo) request.getAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME);
            if (!hostnameInfo.isOnPkSubdomain() && hostnameInfo.isPkSubdomainSupported()) {
                return new ModelAndView(new RedirectView301(fullCanonicalUrl));
            }
        }

        Map<String, Object> ratingsMap =  _schoolProfileDataHelper.getGsRatings(request);
        Integer overallRating = null;
        if (ratingsMap != null) {
            overallRating = (Integer)ratingsMap.get(_schoolProfileDataHelper.DATA_OVERALL_RATING);
            model.put(_schoolProfileDataHelper.DATA_OVERALL_RATING, overallRating);
        }

        List<Review> nonPrincipalReviews = _schoolProfileDataHelper.getNonPrincipalReviews(request);
        List<TopicalSchoolReview> nonPrincipalTopicalReviews = _schoolProfileDataHelper.getNonPrincipalTopicalReviews(request);
        int numNormalReviews = (nonPrincipalReviews == null)?0:nonPrincipalReviews.size();
        int numTopicalReviews = (nonPrincipalTopicalReviews == null)?0:nonPrincipalTopicalReviews.size();
        model.put("numberReviews", numNormalReviews + numTopicalReviews);
        Integer currentPage = _schoolProfileDataHelper.getReviewsCurrentPage( request );
        Integer totalPages = _schoolProfileDataHelper.getReviewsTotalPages( request );

        if (currentPage<1 || currentPage>totalPages) {
            urlBuilder.addParametersFromRequest(request);
            // filter out certain parameters we don't want passed through
            urlBuilder.removeParameter("state");
            urlBuilder.removeParameter("id");
            urlBuilder.removeParameter("page");
            return new ModelAndView(new RedirectView301(urlBuilder.asSiteRelative(request)));
        }

        _schoolProfileHelper.updateModel(request, response, school, model, overallRating);
        model.put("nearbySchools", _schoolProfileDataHelper.getNearbySchools(request));

        model.put("schoolEnrollment", _schoolProfileDataHelper.getEnrollment(request));

        model.put("facebook", _schoolProfileDataHelper.getFacebookTile(school));

        // allow turning on/off debugging via request parameter;
        // pass-through needed here for individual modules to have access to request params,
        // since e.g. profileTestScores.jspx doesn't have direct access to the request params of original/parent request
        model.put("debug", request.getParameter("gs_debug"));



        if (pageHelper != null) {

            final Integer collectionID= _schoolProfileHelper.getCollectionIdForSchool(school);
            final HubCityMapping hubInfo= _hubCityMappingDao.getMappingObjectByCollectionID(collectionID);
            // WARNING: AdTagHandler and PageHelper checks the value of this template keyword when writing out JS calls on the page
//            pageHelper.clearHubUserCookie(request, response);
            pageHelper.addAdKeywordMulti("template", "SchoolProf");
            pageHelper.setHideAds(_schoolProfileHelper.isSchoolInAdFreeHub(school));
            if (collectionID != null && hubInfo != null)  {
            pageHelper.clearHubCookiesForNavBar(request, response);
            pageHelper.setHubCookiesForNavBar(request, response, hubInfo.getState(), hubInfo.getCity());
            pageHelper.setHubUserCookie(request, response);

            }
        }

        if (school.getIsNewGSRating()) {
            pageHelper.addAdKeywordMulti("template", "NewRating");
        } else if (school.getIsOldGSRating()) {
            pageHelper.addAdKeywordMulti("template", "OldRating");
        }

        String k12AffiliateUrl = AdUtil.getK12AffiliateLinkForSchool(school, "sp");
        if (k12AffiliateUrl != null) {
            model.put("k12AffiliateUrl", k12AffiliateUrl);
        }

        model.put(SCHOOL_CALENDAR_ENABLED, _schoolProfileDataHelper.isSchoolCalendarEnabled(school));

        return new ModelAndView(_viewName, model);
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }


    public ControllerFamily getControllerFamily() {
        return _controllerFamily;
    }

    public void setControllerFamily(ControllerFamily controllerFamily) {
        _controllerFamily = controllerFamily;
    }

    public SchoolProfileDataHelper getSchoolProfileDataHelper() {
        return _schoolProfileDataHelper;
    }

    public void setSchoolProfileDataHelper(SchoolProfileDataHelper schoolProfileDataHelper) {
        _schoolProfileDataHelper = schoolProfileDataHelper;
    }

    public SchoolProfileHelper getSchoolProfileHelper() {
        return _schoolProfileHelper;
    }

    public void setSchoolProfileHelper(SchoolProfileHelper schoolProfileHelper) {
        _schoolProfileHelper = schoolProfileHelper;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }
}