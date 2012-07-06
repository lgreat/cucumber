package gs.web.school;

import gs.data.community.User;
import gs.data.school.*;
import gs.data.school.district.District;
import gs.data.school.review.IReviewDao;
import gs.data.school.review.Ratings;
import gs.data.state.State;
import gs.data.util.CommunityUtil;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
@RequestMapping("/school/profileOverview.page")
public class SchoolProfileOverviewController extends AbstractSchoolProfileController {
    protected static final Log _log = LogFactory.getLog(SchoolProfileOverviewController.class.getName());
    public static final String VIEW = "school/profileOverview";

    // Model Key constants
    private static final String BEST_KNOWN_FOR_MODEL_KEY = "bestKnownFor";
    private static final String RATINGS_BREAKDOWN_MODEL_KEY = "ratingsBreakdown";
    private static final String PHOTOS_MODEL_KEY = "photos";
    private static final String VIDEO_MODEL_KEY = "video";
    private static final String COMMUNITY_RATING_MODEL_KEY = "communityRating";
    private static final String REVIEWS_MODEL_KEY = "reviews";
    private static final String DIVERSITY_MODEL_KEY = "diversity";
    private static final String SPECIAL_EDUCATION_MODEL_KEY = "specialEd";
    private static final String EXTENDED_CARE_MODEL_KEY = "extendedCare";
    private static final String TRANSPORTATION_MODEL_KEY = "transportation";
    private static final String PROGRAMS_MODEL_KEY = "programs";
    private static final String APPL_INFO_MODEL_KEY = "applInfo";
    private static final String LOCAL_INFO_MODEL_KEY = "localInfo";
    private static final String RELATED_CONTENT_MODEL_KEY = "related";
    private static final String SPORTS_MODEL_KEY = "sports";
//    private static final String _MODEL_KEY = "";
//    private static final String _MODEL_KEY = "";
//    private static final String _MODEL_KEY = "";
//    private static final String _MODEL_KEY = "";
//    private static final String _MODEL_KEY = "";


    public enum NoneHandling{ ALWAYS_SHOW, SHOW_IF_ONLY_VALUE, HIDE_IF_ONLY_NONE }

    String _viewName;

    @Autowired
    private RatingHelper _ratingHelper;
    @Autowired
//    private IEspResponseDao _espResponseDao;
    private SchoolProfileDataHelper _schoolProfileDataHelper;
    @Autowired
    private IReviewDao _reviewDao;
    @Autowired
    private SchoolHelper _schoolHelper;


    @RequestMapping(method= RequestMethod.GET)
    public String handle(ModelMap modelMap, HttpServletRequest request,
        @RequestParam(value = "schoolId", required = false) Integer schoolId,
        @RequestParam(value = "state", required = false) State state
    ) {

        School school = getSchool(request, state, schoolId);
        modelMap.put( "school", school );

        // There are two versions of this page, one if there is OSP (aka ESP) data available or not.
        // This can be determined by retrieving the esp data and seeing it if any data is returned.
        // Then execute the Esp or non-Esp code
        Map<String, List<EspResponse>> espResults = _schoolProfileDataHelper.getEspDataForSchool( request );
        modelMap.put( "espData", espResults );

        if( espResults != null && !espResults.isEmpty() ) {
            // OSP case
            handleEspPage( modelMap, request, school, espResults );
        }
        else {
            // Non-OSP case
            handleNonEspPage( modelMap, request, school );
        }

        return VIEW;
    }

    private void handleEspPage(Map model, HttpServletRequest request, School school, Map<String,List<EspResponse>> espData ) {

        //Map<String,Object> model = new HashMap<String, Object>();

        // First row is "Best know for quote", get it and add to model
        model.put(BEST_KNOWN_FOR_MODEL_KEY, getBestKnownForQuoteTile(school, espData));

        // Tile 1 - Default: Ratings, Substitute 1: Awards, Substitute 2: Autotext(About this school)
        //  Waiting for research team to investigate Ratings
        model.put( RATINGS_BREAKDOWN_MODEL_KEY, getGsRatingsTile(request, school, espData) );

        // Title 2 - Default: Photos, Substitute 1: Principal CTA
        model.put(PHOTOS_MODEL_KEY, getPhotosTile(request, school));

        // Title 3 - Default: Videos, Substitute 1: Information from CMS
        model.put( VIDEO_MODEL_KEY, getVideoTile(request, school, espData) );

        // Title 4 - Default: Community ratings, Substitute 1: Review CTA
        model.put( COMMUNITY_RATING_MODEL_KEY, getCommunityRatingTile(request, school) );

        // Titles 5&6 - Default: Reviews carousel, Substitute 1: Review CTA
        model.put( REVIEWS_MODEL_KEY, getReviewsTile( request, school ) );

        // Title 7 - Default: Student diversity, Substitute 1: Generic text about diversity
        model.put( DIVERSITY_MODEL_KEY, getDiversityTile(request, school, espData) );

        // Title 8 - Default: Special education/extended care, Substitute 1: Nearby schools teaser
        // This is all ESP data and complicated rules.  DO SOON
        model.put( SPECIAL_EDUCATION_MODEL_KEY, getSpecialEdTile(request, school, espData) );
        model.put( EXTENDED_CARE_MODEL_KEY, getExtendedCareTile(request, school, espData) );

        // Title 9 - Default: Transportation, Substitute 1: Students per teacher / average class size, Substitute 2: School boundry tool promo
        model.put( TRANSPORTATION_MODEL_KEY, getTransportationTile(request, school, espData) );

        // Title 10 - Default: Programs, Substitute 1: Parent involvement, Substitute 2: Highlights (data from School object)
        model.put( PROGRAMS_MODEL_KEY, getProgramsTile(request, school, espData) );

        // Titles 11&12 - Default: Application info version 1, Substitute 1: Application info version 2, Substitute 2: School visit checklist
        model.put( APPL_INFO_MODEL_KEY, getApplInfoTile(request, school, espData) );

        // Title 13 - Default: Local info (TBD), Substitute 1: District info, Substitute 2: Neighborhood info
        model.put( LOCAL_INFO_MODEL_KEY, getLocalInfoTile(request, school, espData) );

        // Titles 14&15 - Default: Related content, Substitute 1:
        model.put( RELATED_CONTENT_MODEL_KEY, getRelatedTile(request, school, espData) );

        // 7th row is Sports/Arts/Music
        model.put(SPORTS_MODEL_KEY, getSportsArtsMusicTile(espData));
    }

    private Map<String, Object> getReviewsTile(HttpServletRequest request, School school) {
        Map<String, Object> reviewsModel = new HashMap<String, Object>(2);
        reviewsModel.put( "reviews", _schoolProfileDataHelper.getNonPrincipalReviews(request, 5) );

        return reviewsModel;
    }

    private Map<String, Object> getCommunityRatingTile(HttpServletRequest request, School school) {
        Map<String, Object> communityModel = new HashMap<String, Object>(3);
        communityModel.put( "school", school );
        communityModel.put( "ratings", _schoolProfileDataHelper.getSchoolRatings( request ) );
        communityModel.put( "numberOfReviews", _schoolProfileDataHelper.getCountPublishedNonPrincipalReviews(request) );

        return communityModel;
    }

    private Map getPhotosTile(HttpServletRequest request, School school) {

        Map<String, Object> photosModel = new HashMap<String, Object>(2);

        // Default action is to add photos
        List<SchoolMedia> photoGalleryImages = _schoolProfileDataHelper.getSchoolMedia( request );
        photosModel.put("basePhotoPath", CommunityUtil.getMediaPrefix());
        photosModel.put("photoGalleryImages",photoGalleryImages);
        //used to support the "Report It" links in recent reviews list
        if(PageHelper.isMemberAuthorized(request)){
            SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
            User user = sessionContext.getUser();
            if (user != null) {
                photosModel.put("validUser", user);
            }
        }
        if (photosModel.get("validUser") != null){
            User user = (User) photosModel.get("validUser");
            photosModel.put("photoReports", _schoolProfileDataHelper.getReportsForSchoolMedia(request, user, photoGalleryImages));
        }

        // Substitute is static text which will be handled in the view and will be triggered if photoGalleryImages is null

        return photosModel;

    }

    private Map getGsRatingsTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

        Map<String, Object> _Model = new HashMap<String, Object>(2);

        // TODO - This is a stub - the appropriate code needs to be inserted below
        // Default action

        // Substitute action 1

        return _Model;
    }

    private Map getVideoTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

        Map<String, Object> _Model = new HashMap<String, Object>(2);

        // TODO - This is a stub - the appropriate code needs to be inserted below
        // Default action

        // Substitute action 1

        return _Model;
    }

    private Map getDiversityTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

        Map<String, Object> _Model = new HashMap<String, Object>(2);

        // TODO - This is a stub - the appropriate code needs to be inserted below
        // Default action

        // Substitute action 1

        return _Model;
    }

    private Map getSpecialEdTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

        Map<String, Object> specialEdModel = new HashMap<String, Object>(2);

        // Get all of the date needed to make the required decisions
        List<EspResponse> specEdLevel = espData.get("spec_ed_level");
        boolean hasSpecEdLevelSpecified = checkEspResponseListForValue(specEdLevel, new String[]{"basic", "moderate", "intensive", "none"});
        boolean hasSpecEdLevelNotNone = checkEspResponseListForValue( specEdLevel, new String[]{"basic", "moderate", "intensive"} );
        boolean hasSpecEdLevelNoneOrBlank = checkEspResponseListForValue(specEdLevel, new String[]{"none"});
        boolean hasSpecEdLevelNone = (isNotEmpty(specEdLevel) && hasSpecEdLevelNoneOrBlank);

        List<EspResponse> specEdPgmsExist = espData.get("special_ed_programs_exists");
        boolean specEdPgmsExistYes = checkEspResponseListForValue( specEdPgmsExist, new String[]{"yes"} );
        boolean specEdPgmsExistNoOrBlank = (isEmpty(specEdPgmsExist) || (checkEspResponseListForValue(specEdPgmsExist, new String[]{"no"}) )) ;  // Contains 'no' or blank
        boolean specEdPgmsExistNo = checkEspResponseListForValue(specEdPgmsExist, new String[]{"no"});  // Contains 'no'

        List<EspResponse> specEdPgms = espData.get("special_ed_programs");
        List<EspResponse> academicFocus = espData.get("academic_focus");
        boolean hasAcademicFocus = checkEspResponseListForValue(academicFocus, new String[]{"special_education", "special_ed"});

        boolean  hasSpecEdCoord = checkEspResponseListForValue(espData.get("staff_resources"), new String[]{"special_ed_coordinator"});

        List<EspResponse> beforeAfterCare = espData.get("before_after_care");
        List<EspResponse> beforeAfterCareStart = espData.get("before_after_care_start");
        List<EspResponse> beforeAfterCareEnd = espData.get("before_after_care_end");

        // Now decide if displaying default or substitute content
        boolean displayDefault;
        if( hasSpecEdLevelSpecified || isNotEmpty(specEdPgmsExist) || isNotEmpty(specEdPgms) || hasAcademicFocus ||
                hasSpecEdCoord || isNotEmpty(beforeAfterCare) || isNotEmpty(beforeAfterCareStart) ||
                isNotEmpty(beforeAfterCareEnd) ) {
            displayDefault = true;
            specialEdModel.put( "SpecEdDisplaySelected", "default" ); // To help with unit tests
        }
        else {
            displayDefault = false;
            specialEdModel.put( "SpecEdDisplaySelected", "substitute" ); // To help with unit tests
        }

        if( displayDefault ) {
            // ========== Display Default content ==========
            // ------------- Special Education -------------
            // For the default content there are 4 display options and if none of those conditions are met there is static text to display
            if( hasSpecEdLevelSpecified || isNotEmpty(specEdPgmsExist) || isNotEmpty(specEdPgms) || hasAcademicFocus ||
                    hasSpecEdCoord  ) {
                // Content is to be displayed, next determine what
                // Check for first option
                if( specEdPgmsExistYes && isNotEmpty(specEdPgms) ) {
                    // Display option 'a' - return a list of service offered
                    List<String> specEdPgmsList = new ArrayList<String>();
                    for( EspResponse r : specEdPgms ) {
                        specEdPgmsList.add( r.getPrettyValue() );
                    }
                    specialEdModel.put( "SpecEdPgms", specEdPgmsList );
                    specialEdModel.put( "SpecEdPgmsOptSelected", "a" ); // To help with unit tests
                }
                else if( specEdPgmsExistYes && (specEdPgms==null) || isNotEmpty(specEdPgms) ) {
                    // Display option 'b' - according to the spec this shouldn't happen
                    // but if it does display a static message that services are provided
                    specialEdModel.put( "SpecEdPgmsProvided", "yes" );
                    specialEdModel.put( "SpecEdPgmsOptSelected", "b" ); // To help with unit tests
                }
                else if( specEdPgmsExistNoOrBlank && (hasSpecEdLevelNotNone || hasAcademicFocus || hasSpecEdCoord) ) {
                    // Display option 'c' - Services are provided
                    specialEdModel.put( "SpecEdPgmsProvided", "yes" );
                    specialEdModel.put( "SpecEdPgmsOptSelected", "c" ); // To help with unit tests
                }
                else if ( (specEdPgmsExistNo || hasSpecEdLevelNone) && !hasAcademicFocus && !hasSpecEdCoord ) {
                    // Display option 'd' - Services are not provided
                    specialEdModel.put( "SpecEdPgmsProvided", "no" );
                    specialEdModel.put( "SpecEdPgmsOptSelected", "d" ); // To help with unit tests
                }
                else {
                    // Spec doesn't specify what happens here - Services are not provided???
                    specialEdModel.put( "SpecEdPgmsProvided", "call" );  // Is this right
                    specialEdModel.put( "SpecEdPgmsOptSelected", "unspecified" ); // To help with unit tests
                }
            }
            else {
                // No specific content is available - show call school message
                specialEdModel.put( "SpecEdPgmsProvided", "call" );  // In the end - can we get rid of this
            }
            // ------------- Extended Care -------------
            if( isNotEmpty(beforeAfterCare) || isNotEmpty(beforeAfterCareEnd) || isNotEmpty(beforeAfterCareStart) ) {
                // Content is to be displayed, next determine what
                // handle display based on contents of beforeAfterCare
                boolean hasBeforeCare = checkEspResponseListForValue(beforeAfterCare, new String[]{"before"});
                boolean hasAfterCare  = checkEspResponseListForValue(beforeAfterCare, new String[]{"after"});
                if( isEmpty(beforeAfterCare) ) {
                    // before or after not specified
                    if( isNotEmpty(beforeAfterCareStart) ) {
                        specialEdModel.put( "ExtdCareBefore", "Starts: " + beforeAfterCareStart.get(0).getPrettyValue() );
                    }
                    if( isNotEmpty(beforeAfterCareEnd) ) {
                        specialEdModel.put( "ExtdCareAfter", "Ends: " + beforeAfterCareEnd.get(0).getPrettyValue() );
                    }
                }
                if( hasBeforeCare ) {
                    // before care is specified
                    StringBuffer sb = new StringBuffer("Before school");
                    if( isNotEmpty(beforeAfterCareStart) ) {
                        // Add start time
                        sb.append(": Starts ").append( beforeAfterCareStart.get(0).getPrettyValue() );
                    }
                    specialEdModel.put( "ExtdCareBefore", sb.toString() );
                }
                if( hasAfterCare ) {
                    // before care is specified
                    StringBuffer sb = new StringBuffer("After school");
                    if( isNotEmpty(beforeAfterCareEnd) ) {
                        // Add end time
                        sb.append(": Ends ").append(beforeAfterCareEnd.get(0).getPrettyValue());
                    }
                    specialEdModel.put( "ExtdCareAfter", sb.toString() );
                }
            }
            else {
                // No specific content is available - show call school message
                specialEdModel.put( "ExtdCareProvided", "call" );  // In the end - can we get rid of this

            }
            // Now determine what the header should be based on the type of school
            LevelCode lc = school.getLevelCode();
            if( lc != null ) {
                if( lc.containsLevelCode(LevelCode.Level.HIGH_LEVEL) ||
                        (lc.containsLevelCode(LevelCode.Level.MIDDLE_LEVEL) && lc.containsLevelCode(LevelCode.Level.HIGH_LEVEL) ) ) {
                    specialEdModel.put( "ExtdCareTitle", "Extended programs" );
                }
                else {
                    specialEdModel.put( "ExtdCareTitle", "Extended care" );
                }
            }
        }
        else {
            // ========== Substitute action ==========
            // ------------- Special Education -------------

            // ------------- Extended Care -------------

        }





        return specialEdModel;
    }

    private Map getExtendedCareTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

        Map<String, Object> _Model = new HashMap<String, Object>(2);

        // TODO - This is a stub - the appropriate code needs to be inserted below
        // Default action

        // Substitute action 1

        return _Model;
    }

    private Map getTransportationTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

        Map<String, Object> _Model = new HashMap<String, Object>(2);

        // TODO - This is a stub - the appropriate code needs to be inserted below
        // Default action

        // Substitute action 1

        return _Model;
    }

    private Map getProgramsTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

        Map<String, Object> _Model = new HashMap<String, Object>(2);

        // TODO - This is a stub - the appropriate code needs to be inserted below
        // Default action

        // Substitute action 1

        return _Model;
    }

    private Map getApplInfoTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

        Map<String, Object> _Model = new HashMap<String, Object>(2);

        // TODO - This is a stub - the appropriate code needs to be inserted below
        // Default action

        // Substitute action 1

        return _Model;
    }

    private Map getLocalInfoTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

        Map<String, Object> _Model = new HashMap<String, Object>(2);

        // TODO - This is a stub - the appropriate code needs to be inserted below
        // Default action

        // Substitute action 1

        return _Model;
    }

    private Map getRelatedTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

        Map<String, Object> _Model = new HashMap<String, Object>(2);

        // TODO - This is a stub - the appropriate code needs to be inserted below
        // Default action

        // Substitute action 1

        return _Model;
    }

    private Map<String, Object> getSportsArtsMusicTile(Map<String, List<EspResponse>> espData) {

        Map<String, Object> sportsModel = new HashMap<String, Object>(4);
        // Sports
        List<String> boysSports = getEspDataByKey( "boys_sports", NoneHandling.SHOW_IF_ONLY_VALUE, espData );
        Collections.sort(boysSports);
        setMaxLength( boysSports, 4 );
        sportsModel.put("boys_sports", boysSports);
        List<String> girlsSports = getEspDataByKey("girls_sports", NoneHandling.SHOW_IF_ONLY_VALUE, espData);
        Collections.sort(girlsSports);
        setMaxLength( girlsSports, 4 );
        sportsModel.put("girls_sports", girlsSports);

        // Arts
        List<String> artsMedia = getEspDataByKey("arts_media", NoneHandling.SHOW_IF_ONLY_VALUE, espData);
        List<String> artsPerformingWritten = getEspDataByKey("arts_performing_written", NoneHandling.SHOW_IF_ONLY_VALUE, espData);
        List<String> artsVisual = getEspDataByKey("arts_visual", NoneHandling.SHOW_IF_ONLY_VALUE, espData);
        // Check if all are none
        if( artsMedia.size() == 1 && artsMedia.get(0).equalsIgnoreCase("none") &&
                artsPerformingWritten.size() == 1 && artsPerformingWritten.get(0).equalsIgnoreCase("none") &&
                artsVisual.size() == 1 && artsVisual.get(0).equalsIgnoreCase("none") ) {
            // All are none, just need one of them
            sportsModel.put("arts", artsMedia);  // Can return any of the lists since they are all the same and contain one "none" entry
        }
        else {
            // Combine all into one list and return it
            List<String> arts = new ArrayList<String>();
            if( artsMedia.size() == 1 && artsMedia.get(0).equalsIgnoreCase("none") ) {
                // nothing to add to results
            }
            else {
                arts.addAll( artsMedia );
            }
            if( artsPerformingWritten.size() == 1 && artsPerformingWritten.get(0).equalsIgnoreCase("none") ) {
                // nothing to add to results
            }
            else {
                arts.addAll( artsPerformingWritten );
            }
            if( artsVisual.size() == 1 && artsVisual.get(0).equalsIgnoreCase("none") ) {
                // nothing to add to results
            }
            else {
                arts.addAll( artsVisual );
            }
            Collections.sort(arts);
            setMaxLength( arts, 5 );
            sportsModel.put("arts", arts);
        }

        // Music
        List<String> music = getEspDataByKey("arts_music", NoneHandling.SHOW_IF_ONLY_VALUE, espData);
        Collections.sort( music );
        setMaxLength( music, 5 );
        sportsModel.put("music", music);

        // Check if no data and flag that Substitute content is to be displayed
        int total = boysSports.size() + girlsSports.size() + artsMedia.size() + artsPerformingWritten.size() +
                artsVisual.size() + music.size();
        if( total == 0 ) {
            sportsModel.put("SportsArtsMusic", "Hide");
        }

        return sportsModel;
    }

    /**
     * If the list contains more than len items truncate the list to len-1 items and add an item "More..."
     * @param list the list to check
     * @param len the max len
     */
    private void setMaxLength(List<String> list, int len) {
        if( list.size() > len ) {
            while( list.size() >= len ) {
                list.remove( len - 1 );
            }
            list.add( "More..." );
        }
    }

    /**
     * Helper function to go through a list of EspResponse objects looking for one of the specified values
     * @param espResponses The EspResponse objects to check
     * @param valuesToLookFor The values to look for
     * @return True if any value is found in the EspResponses
     */
    private boolean checkEspResponseListForValue(List<EspResponse> espResponses, String[] valuesToLookFor) {
        if( (espResponses==null) || (espResponses.size()==0) ) {
            return false;
        }

        for( String val : valuesToLookFor ) {
            for( EspResponse r : espResponses ) {
                if( r.getValue().equals( val ) ) {
                    return true;    // Found, we are done
                }
            }
        }
        return false;   // If we get here the answer no match was found
    }

    private List<String> getEspDataByKey(String key, NoneHandling noneHandling, Map<String, List<EspResponse>> espData) {
        List<String> results = new ArrayList<String>();
        List<EspResponse> espResponses = espData.get( key );
        if (espResponses != null && espResponses.size() > 0) {
            // Check for none
            if( espResponses.size() == 1 && espResponses.get(0).getValue().equals("none")) {
                if( noneHandling == NoneHandling.HIDE_IF_ONLY_NONE ) {
                    return results;
                }
                else if( noneHandling == NoneHandling.SHOW_IF_ONLY_VALUE ) {
                    results.add( espResponses.get(0).getPrettyValue() );
                    return results;
                }
            }
            for( EspResponse espResponse : espResponses ) {
                if( espResponse.getSafeValue().equals("none")) {
                    if( noneHandling == NoneHandling.ALWAYS_SHOW ) {
                        results.add( espResponse.getPrettyValue() );
                    }
                    else {
                        // don't include none
                    }
                }
                else {
                    results.add( espResponse.getPrettyValue() );
                }
            }
            Collections.sort( results );
        }
        return results;
    }

    private void handleNonEspPage(ModelMap modelMap, HttpServletRequest request, School school) {




        return;
    }

    public String getBestKnownForQuoteTile(School school, Map<String, List<EspResponse>> espData) {

        String bestKnownFor = null;
        List<EspResponse> espResponses = espData.get( "best_known_for" );
        if (espResponses != null && espResponses.size() > 0) {
            bestKnownFor = espResponses.get(0).getSafeValue();
            if (StringUtils.isNotBlank(bestKnownFor)) {
                if (!StringUtils.endsWith(bestKnownFor, ".")) {
                    bestKnownFor += ".";
                }
            }
        }
        return bestKnownFor;
    }


    boolean isNotEmpty( Collection c ) {
        return ( (c!=null) && c.size()>0 );
    }


    boolean isEmpty( Collection c ) {
        return ((c==null) || (c.size()==0));
    }

    // The following setter dependency injection is just for the tester
    public void setSchoolProfileDataHelper( SchoolProfileDataHelper schoolProfileDataHelper ) {
        _schoolProfileDataHelper = schoolProfileDataHelper;
    }


    /* **************************************************************************************************
           This is the original code to be deleted when new development is complete
       **************************************************************************************************
     */

    public Map<String,Object> oldHandlerToBeDeleted(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Integer schoolId,
                                     State state
    ) {

        Map<String,Object> model = new HashMap<String, Object>();
        School school = getSchool(request, state, schoolId);



        // school's quote
//        model.put("bestKnownFor", getBestKnownForQuoteTile(school, espData));



        // GreatSchools rating
        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        boolean useCache = (null != pageHelper && pageHelper.isDevEnvironment() && !pageHelper.isStagingServer());
        Integer gsRating = _ratingHelper.getGreatSchoolsOverallRating(school, useCache);
        if (gsRating != null && gsRating > 0 && gsRating < 11) {
            pageHelper.addAdKeyword("gs_rating", String.valueOf(gsRating));
        }
        model.put("gs_rating", gsRating);



        // photos
        //addSchoolPhotosToModel(request, school, model);


        // videos



        // Community ratings
        Ratings ratings = _reviewDao.findRatingsBySchool(school);
        model.put("ratings", ratings);
        model.put("noIndexFlag", school != null);



        // User reviews
        /*List<Review> reviews = _reviewDao.findPublishedNonPrincipalReviewsBySchool(school, MAX_SCHOOL_REVIEWS);
        Long numberOfReviews = _reviewDao.countPublishedNonPrincipalReviewsBySchool(school);
        model.put("reviews", reviews);
        model.put("numberOfReviews", numberOfReviews);
        // find and expose last modified date
        model.put("lastModifiedDate", _schoolHelper.getLastModifiedDate(school, reviews, numberOfReviews));
        */



        // Student ethnicity



        // Special education
        Set<String> ospKeys = new HashSet<String>(1);
        ospKeys.add("special_ed_programs");
        ospKeys.add("before_after_care");
        ospKeys.add("before_after_care_start");
        ospKeys.add("before_after_care_end");

//        List<EspResponse> espResponses = _espResponseDao.getResponsesByKeys(school, ospKeys);
//        List<String> specialEducationItems = new ArrayList<String>();
//        if (espResponses != null && espResponses.size() > 0) {
//            for (EspResponse r : espResponses) {
//                if (r.getKey().equals("special_ed_programs")) {
//                    specialEducationItems.add(r.getSafeValue());
//                }
//            }
//        }
//        model.put("specialEducationItems", specialEducationItems);




//        // Transportation
//        if (espResponses != null && espResponses.size() > 0) {
//            for (EspResponse r : espResponses) {
//                if (r.getKey().contains("_care")) {
//                    if (r.getKey().equals("before_after_care")) {
//                        if (r.getSafeValue().equals("before")) {
//                            model.put("before_care", true);
//                        } else if (r.getSafeValue().equals("after")) {
//                            model.put("after_care", true);
//                        }
//                    } else {
//                        model.put(r.getKey(), r.getSafeValue());
//                    }
//                }
//            }
//        }




        // Programs




        // Public schools




        // District information
        District district = school.getDistrict();
        model.put("district",district);





        // OSP data (sports, arts, music)









        return model;
    }





    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }


}