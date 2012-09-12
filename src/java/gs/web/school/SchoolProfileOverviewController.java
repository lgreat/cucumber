package gs.web.school;

import gs.data.cms.IPublicationDao;
import gs.data.community.User;
import gs.data.content.cms.*;
import gs.data.geo.bestplaces.BpZip;
import gs.data.school.*;
import gs.data.school.census.CensusDataSet;
import gs.data.school.census.CensusDataType;
import gs.data.school.census.SchoolCensusValue;
import gs.data.school.district.District;
import gs.data.school.review.Review;
import gs.data.search.GsSolrQuery;
import gs.data.search.SearchException;
import gs.data.search.SearchResultsPage;
import gs.data.search.fields.CmsFeatureFields;
import gs.data.search.fields.DocumentType;
import gs.data.util.CommunityUtil;
import gs.web.content.cms.CmsContentLinkResolver;
import gs.web.i18n.LanguageToggleHelper;
import gs.web.search.CmsFeatureSearchService;
import gs.web.search.ICmsFeatureSearchResult;
import gs.web.util.PageHelper;
import gs.web.util.UrlBuilder;
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

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping("/school/profileOverview.page")
public class SchoolProfileOverviewController extends AbstractSchoolProfileController {
    protected static final Log _log = LogFactory.getLog(SchoolProfileOverviewController.class.getName());
    public static final String VIEW = "school/profileOverview";

    // Model Key constants
    private static final String BEST_KNOWN_FOR_MODEL_KEY = "bestKnownFor";
    private static final String RATINGS_BREAKDOWN_MODEL_KEY = "ratings";
    private static final String PHOTOS_MODEL_KEY = "photos";
    private static final String VIDEO_MODEL_KEY = "video";
    private static final String COMMUNITY_RATING_MODEL_KEY = "communityRating";
    private static final String REVIEWS_MODEL_KEY = "reviews";
    private static final String REVIEWS_TOTAL_KEY = "reviewsTotal";
    private static final String DIVERSITY_MODEL_KEY = "diversity";
    private static final String SPECIAL_EDUCATION_MODEL_KEY = "specialEd";
    private static final String TRANSPORTATION_MODEL_KEY = "transportation";
    private static final String PROGRAMS_MODEL_KEY = "programs";
    private static final String APPL_INFO_MODEL_KEY = "applInfo";
    private static final String LOCAL_INFO_MODEL_KEY = "localInfo";
    private static final String RELATED_CONTENT_MODEL_KEY = "related";
    private static final String FACEBOOK_MODEL_KEY = "facebook";
    private static final String SPORTS_MODEL_KEY = "sports";
    public static final String LAST_MODIFIED_DATE_MODEL_KEY = "schoolLastModifiedDate";

    private static final String SCHOOL_VISIT_CHECKLIST_MODEL_KEY = "schoolVisit";
    private static final String VIDEO_TOUR_MODEL_KEY = "videoTour";
    private static final String BOUNDARY_TOOL_MODEL_KEY = "boundaryTool";

    public static final String VIDEO_ELEMENTARY = "6857";
    public static final String VIDEO_MIDDLE = "6856";
    public static final String VIDEO_HIGH = "6855";


    public enum NoneHandling{ ALWAYS_SHOW, SHOW_IF_ONLY_VALUE, HIDE_IF_ONLY_NONE }

    public  static final  String MODEL_OVERALL_RATING = "overallRating";
    public  static final  String MODEL_CLIMATE_RATING = "climateRating";
    public  static final  String MODEL_ACADEMIC_RATING = "academicRating";
    public  static final  String CLIMATE_RATING_NO_DATA_TEXT = "Data not available";
    public  static final  String CLIMATE_RATING_NO_DATA_TEXT_DC = "Coming 2013";
    public  static final  String CLIMATE_RATING_NO_DATA_TEXT_IN = "Coming soon";

    String _viewName;

    @Autowired
    private SchoolProfileDataHelper _schoolProfileDataHelper;

    @Autowired
    private SchoolProfileCensusHelper _schoolProfileCensusHelper;

//    @Autowired
//    private CmsFeatureDao _cmsFeatureDao;
//    @Autowired
//    private IPublicationDao _publicationDao;

    @Autowired
    private CmsContentLinkResolver _cmsFeatureEmbeddedLinkResolver;

    @Autowired
    private CmsFeatureSearchService _cmsFeatureSearchService;

    @RequestMapping(method= RequestMethod.GET)
    public String handle(ModelMap modelMap, HttpServletRequest request
    ) {

        School school = getSchool(request);
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

        // GS-13116 Handle last modified
        modelMap.put(LAST_MODIFIED_DATE_MODEL_KEY, getLastModifiedDateForSchool(request, school));

        return VIEW;
    }

    protected Date getLastModifiedDateForSchool(HttpServletRequest request, School school) {
        List<Review> reviews = _schoolProfileDataHelper.getNonPrincipalReviews(request, 1);
        Review latestNonPrincipalReview = null;
        if (reviews.size() > 0) {
            latestNonPrincipalReview = reviews.get(0);
        }
        return SchoolProfileHelper.getSchoolLastModified(school, latestNonPrincipalReview);
    }

    private void handleEspPage(Map<String, Object> model, HttpServletRequest request, School school, Map<String,List<EspResponse>> espData ) {

        // First row is "Best know for quote", get it and add to model
        model.put(BEST_KNOWN_FOR_MODEL_KEY, getBestKnownForQuoteEspTile(school, espData));

        // Tile 1 - Default: Ratings, Substitute 1: Awards, Substitute 2: Autotext(About this school)
        model.put( RATINGS_BREAKDOWN_MODEL_KEY, getGsRatingsEspTile(request, school, espData) );

        // Title 2 - Default: Photos, Substitute 1: Principal CTA
        model.put(PHOTOS_MODEL_KEY, getPhotosEspTile(request, school));

        // Title 3 - Default: Videos, Substitute 1: Boundary Tool Promo
        model.put( VIDEO_MODEL_KEY, getVideoEspTile(request, school, espData) );

        // Title 4 - Default: Community ratings, Substitute 1: Review CTA
        model.put( COMMUNITY_RATING_MODEL_KEY, getCommunityRatingEspTile(request, school) );

        // Titles 5&6 - Default: Reviews carousel, Substitute 1: Review CTA
        model.put( REVIEWS_MODEL_KEY, getReviewsEspTile(request, school) );

        // Title 7 - Default: Student diversity, Substitute 1: Generic text about diversity
        model.put( DIVERSITY_MODEL_KEY, getDiversityEspTile(request, school) );

        // Title 8 - Default: Special education/extended care, Substitute 1: Nearby schools teaser
        model.put( SPECIAL_EDUCATION_MODEL_KEY, getSpecialEdEspTile(request, school, espData) );

        // Title 9 - Default: Transportation, Substitute 1: Students per teacher / average class size, Substitute 2: School boundry tool promo
        model.put( TRANSPORTATION_MODEL_KEY, getTransportationEspTile(request, school, espData) );

        // Title 10 - Default: Programs, Substitute 1: Parent involvement, Substitute 2: Highlights (data from School object)
        model.put( PROGRAMS_MODEL_KEY, getProgramsEspTile(request, school, espData) );

        // Titles 11&12 - Default: Application info version 1, Substitute 1: Application info version 2, Substitute 2: School visit checklist
        model.put( APPL_INFO_MODEL_KEY, getApplInfoEspTile(request, school, espData) );

        // Title 13 - Default: Local info (TBD), Substitute 1: District info, Substitute 2: Neighborhood info
        model.put( LOCAL_INFO_MODEL_KEY, getLocalInfoEspTile(request, school) );

        // Titles 14&15 - Default: Related content, Substitute 1:
        model.put( RELATED_CONTENT_MODEL_KEY, getRelatedEspTile(request, school, 5) );

        // 7th row is Facebook integration
        model.put(FACEBOOK_MODEL_KEY, getFacebookTile( request, school, espData) );

        // 8th row is Sports/Arts/Music
        model.put(SPORTS_MODEL_KEY, getSportsArtsMusicEspTile(espData));
    }

    public String getBestKnownForQuoteEspTile(School school, Map<String, List<EspResponse>> espData) {

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

    private Map<String, Object> getReviewsEspTile(HttpServletRequest request, School school) {
        Map<String, Object> reviewsModel = new HashMap<String, Object>(2);
        List<Review> reviews = _schoolProfileDataHelper.getNonPrincipalReviews(request, 5);
        if( reviews!=null && reviews.size() > 0 ) {
            reviewsModel.put( "reviews", reviews );
            reviewsModel.put( "content", "reviews" );
            reviewsModel.put( REVIEWS_TOTAL_KEY, _schoolProfileDataHelper.getNonPrincipalReviews(request).size());
        }
        else {
            reviewsModel.put( "content", "reviewsCTA" );
        }

        return reviewsModel;
    }

    private Map<String, Object> getCommunityRatingEspTile(HttpServletRequest request, School school) {
        Map<String, Object> communityModel = new HashMap<String, Object>(3);
        communityModel.put( "school", school );
        communityModel.put( "ratings", _schoolProfileDataHelper.getCommunityRatings( request ) );
        // communityModel.put( "numberOfReviews", _schoolProfileDataHelper.getCountPublishedNonPrincipalReviews(request) );

        return communityModel;
    }

    private Map getPhotosEspTile(HttpServletRequest request, School school) {

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

    Map getGsRatingsEspTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

        // Try default
        Map<String, Object> model = getGsRatingsModel( request, school );
        if( model != null && model.size()>0 ) {
            return model;
        }

        // default did not return a model so try awards
        model = getAwardsModel(request, school, espData );
        if( model != null && model.size()>0 ) {
            return model;
        }

        // Awards didn't return anything so do school Autotext it is just text from the school object
        model = getSchoolAutotext( request, school );

        return model;
    }

    /**
     * Generate data for the GS Rating tile when there is no espData
     *
     * @param request
     * @param school
     * @return
     */
    private Map getGsRatingsTileNoOsp(HttpServletRequest request, School school) {

        // Try default
        Map<String, Object> model = getGsRatingsModel( request, school );
        if( model != null && model.size()>0 ) {
            return model;
        }

        // Awards didn't return anything so do school Autotext it is just text from the school object
        model = getSchoolAutotext( request, school );

        return model;
    }

    private Map<String, Object> getSchoolAutotext(HttpServletRequest request, School school) {

        Map<String, Object> model = new HashMap<String, Object>(2);

        // Substitute action 2 - This is an Autotext field
        // It has the following format if all of the data is present.  If data is not present pieces get dropped.
        // “school.city’s school.name is a school.type school serving ENROLLMENT students in grades X – Y.
        // It is school.subtype and school.affiliation affiliated. The school belongs to the following
        // associations: school.association.�?
        StringBuilder sentence = new StringBuilder();
        // From the spec create sentence 1.1 if the data is present
        if( school.getCity()!=null && school.getCity().length()>0 ) {
            sentence.append( school.getCity() ).append( "'s " );
        }

        // From the spec create sentence 1.2 if the data is present
        /* It is better to get the enrollment from the census data because school.getEnrollment causes a database lookup
        Integer enrollment = school.getEnrollment();
         */
        // Enrollment is from the census data
        SchoolCensusValue enrollmentSCV = _schoolProfileDataHelper.getSchoolCensusValue( request, CensusDataType.STUDENTS_ENROLLMENT );
        Integer enrollment = null;
        if( enrollmentSCV != null ) {
                enrollment = enrollmentSCV.getValueInteger();
        }
        if( school.getName()!=null && school.getName().length()>0 &&
                school.getType()!=null && school.getType().getSchoolTypeName().length()>0 ) {
            String enrollmentStr = " ";  // if no enrollment use a space
            if( enrollment!=null ) {
                enrollmentStr = Integer.toString( enrollment.intValue() );
            }
            sentence.append( school.getName() ).append( " is a " ).append( school.getType().getSchoolTypeName() ).
                    append( " school serving " ).append( enrollmentStr ).append( " students" );
        }
        // From the spec create sentence 1.3 if the data is present
        if( school.getGradeLevels()!= null ) {
            String level = school.getGradeLevels().getRangeString();
            if( level == null || level.length()==0 || "AE".equals(level) ) {
                // ignore
            }
            else if( "UG".equals(level) || "ungraded".equals(level) || "n/a".equals(level) ) {
                // Ungraded is returned as "n/a"
                sentence.append( " and is ungraded" );
            }
            else if( level.indexOf('-') > 0 ) {      // If the level contains a dash there are multiple grades
                sentence.append( " in grades " ).append( level );
            }
            else {
                sentence.append( " in grade " ).append( level );
            }
        }

        // Add period at end of sentence 1 if not empty
        if( sentence.length()>0 ) {
            sentence.append( ". " );
        }

        // Sentence 2
        // Create a copy of the School.Subtype so it can be changed without impacting the school object
        String subtypeStr = school.getSubtype().asCommaSeparatedString();
        SchoolSubtype subtype = SchoolSubtype.create(subtypeStr);
        List<String> sentence2Attributes = new ArrayList<String>();
        if( subtype !=  null ) {
            // remove subtypes not to appear in result
            subtype.remove( "preschool_early_childhood_center" );
            subtype.remove( "elementary" );
            subtype.remove( "middle" );
            subtype.remove( "high" );
            subtype.remove( "combined_elementary_and_secondary" );
            subtype.remove( "secondary" );

            // all_female, all_male, coed are to be printed first
            if( subtype.contains("all_female") ) {
                sentence2Attributes.add("all female");
                subtype.remove( "all_female" );
            }
            else if( subtype.contains("all_male") ) {
                sentence2Attributes.add("all male");
                subtype.remove( "all_male" );
            }
            else if( subtype.contains("coed") ) {
                sentence2Attributes.add("coed");
                subtype.remove( "coed" );
            }

            // Now get remaining subtypes which can only be as a comma separated string
            String subtypesStr = subtype.asPrettyCommaSeparatedString();
            if( subtypesStr!=null && subtypesStr.length()>0 ) {
                String [] subTypeArray = subtypesStr.split( ", " );
                List<String> subtypesList = Arrays.asList( subTypeArray );
                sentence2Attributes.addAll(subtypesList);
            }

            // Add affiliation
            String affiliation = school.getAffiliation();
            if( affiliation!=null && affiliation.length()>0 ) {
                sentence2Attributes.add( affiliation );
            }

            // Finally build the sentence if any attributes are present
            if( sentence2Attributes.size() > 0 ) {
                sentence.append( " It is " );
                sentence.append( prettyCommaSeparatedString(sentence2Attributes) );
                // has affiliation need to add "affiliated.", otherwise remove last space and add period
                if( affiliation!=null && affiliation.length()>0 ) {
                    sentence.append( " affiliated. " );
                }
                else {
                    //sentence.deleteCharAt( sentence.length()-1 );
                    sentence.append( ". " );
                }

            }
        }

        // Sentence 3
        String associations = school.getAssociation();
        if( associations!=null && associations.length()>0 ) {
            sentence.append( "The school belongs to the following associations: " ).append( associations ).append(".");
        }
        model.put("autotext", sentence.toString());
        model.put( "content", "schoolAutotext" );

        return model;
    }

    private Map<String, Object> getGsRatingsModel(HttpServletRequest request, School school) {

        // Default action
        Map<String, Object> model = null;

        Map<String, Object> ratingsMap = _schoolProfileDataHelper.getGsRatings(request);
        //Only display ratings tile if there is overall rating and academic rating.
        if (ratingsMap != null && !ratingsMap.isEmpty()
                && ratingsMap.get(_schoolProfileDataHelper.DATA_OVERALL_RATING) != null
                && ratingsMap.get(_schoolProfileDataHelper.DATA_OVERALL_ACADEMIC_RATING) != null) {
            model = new HashMap<String, Object>(5);
            model.put(MODEL_OVERALL_RATING, ratingsMap.get(_schoolProfileDataHelper.DATA_OVERALL_RATING));
            model.put(MODEL_ACADEMIC_RATING, ratingsMap.get(_schoolProfileDataHelper.DATA_OVERALL_ACADEMIC_RATING));

            Object climateRating = ratingsMap.get(_schoolProfileDataHelper.DATA_OVERALL_CLIMATE_RATING);
            if (climateRating == null) {
                String climateRatingNoDataText = school.getDatabaseState().getName().equals("DC") ? CLIMATE_RATING_NO_DATA_TEXT_DC :
                        school.getDatabaseState().getName().equals("IN") ? CLIMATE_RATING_NO_DATA_TEXT_IN : CLIMATE_RATING_NO_DATA_TEXT;
                model.put(MODEL_CLIMATE_RATING, climateRatingNoDataText);
            }else{
                model.put(MODEL_CLIMATE_RATING, climateRating);
            }

            model.put("content", "GsRatings");
        }
        return model;
    }


    private Map<String, Object> getAwardsModel(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

        Map<String, Object> model = null;

        // Need to decide which substitute to do which requires checking all of the academic and service awards.
        List<String> awards = new ArrayList<String>(3);
        if( isNotEmpty( espData.get("academic_award_1") ) ) {
                awards.add(espData.get("academic_award_1").get(0).getPrettyValue());
        }
                if( isNotEmpty( espData.get("academic_award_2") ) ) {
                awards.add(espData.get("academic_award_2").get(0).getPrettyValue());
        }
                if( isNotEmpty( espData.get("academic_award_3") ) ) {
                awards.add(espData.get("academic_award_3").get(0).getPrettyValue());
        }
                if( isNotEmpty( espData.get("service_award_1") ) && awards.size()<3 ) {
                awards.add(espData.get("service_award_1").get(0).getPrettyValue());
        }
                if( isNotEmpty( espData.get("service_award_2") ) && awards.size()<3 ) {
                awards.add(espData.get("service_award_2").get(0).getPrettyValue());
        }
                if( isNotEmpty( espData.get("service_award_3") ) && awards.size()<3 ) {
                awards.add(espData.get("service_award_3").get(0).getPrettyValue());
        }

        if( isNotEmpty(awards) ) {
            // Substitute action 1
            model = new HashMap<String, Object>(2);
            model.put( "awards", awards );
            model.put( "content", "awards" );
        }

        return model;
    }


    Map<String, Object> getVideoEspTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

        Map<String, Object> model = null;

        // Get all of the data needed to make the required decisions
        List<EspResponse> video = espData.get("school_video");
        if( isNotEmpty( video ) ) {
            model = new HashMap<String, Object>(2);
            model.put( "content", "video" );
            model.put( "videoUrl", video.get(0).getSafeValue() );
        }
        else {
            // Substitute action, school boundary tool promo
            model = getBoundaryToolModel();
        }

        return model;
    }

    Map<String, Object> getBoundaryToolModel() {

        Map<String, Object> model = new HashMap<String, Object>(2);
        model.put( "content", "schoolBoundaryToolPromo" );
        return model;
    }

    Map<String, Object> getTourVideoModel(HttpServletRequest request, School school) {

        Map<String, Object> model = new HashMap<String, Object>(3);

        model.put("content", "none");

        return model;

//        // Which video to display will depend on the lowest level taught at the school.  For instance if level_code is e,m,h then just show for e
//        LevelCode levelCode = school.getLevelCode();
//        if( levelCode != null ) {
//            LevelCode.Level level = levelCode.getLowestNonPreSchoolLevel();
//            // determine which video to show
//            String videoId;
//            if (request.getParameter("_tourVideoId") != null) {
//                videoId = request.getParameter("_tourVideoId");
//            } else if( level.equals( LevelCode.Level.MIDDLE_LEVEL) ) {
//                videoId = VIDEO_MIDDLE;
//            } else if( level.equals( LevelCode.Level.HIGH_LEVEL) ) {
//                videoId = VIDEO_HIGH;
//            } else { // fallback on elementary
//                videoId = VIDEO_ELEMENTARY;
//            }
//            // videoId = "5073"; // Debug, this is an existing Id in dev-cms
//
//            model.put( "schoolLevel", level.getName() );
//            model.put( "content", "schoolTourVideo" );
//            model.put( "contentUrl", "/content/cms/translate.page?Video=" + videoId);
////            model.put( "videoIconUrl", result1.getImageUrl() );
////            model.put( "videoIconAltText", result1.getImageAltText() );
//        } else {
//            model.put( "content", "none" );
//        }
//
//        return model;
    }


    private Map<String, Object> getDiversityEspTile(HttpServletRequest request, School school) {

        Map<String, Object> model = new HashMap<String, Object>(2);

        // Default action
        boolean defaultDisplay = false;
        Map<CensusDataType, List<CensusDataSet>> censusValues = _schoolProfileDataHelper.getSchoolCensusValues(request);
        if( censusValues!=null ) {
            List<CensusDataSet> ethnicities = censusValues.get( CensusDataType.STUDENTS_ETHNICITY );
            if( isNotEmpty( ethnicities) ) {
                try {
                    Map<String, String> ethnicityMap = _schoolProfileCensusHelper.getEthnicityLabelValueMap(request);
                    if( ethnicityMap.size() > 0 ) {
                        Map.Entry<String,String> largestEntry = ethnicityMap.entrySet().iterator().next();
                        String largestLabel = largestEntry.getKey();
                        String largestValue = largestEntry.getValue();
                        model.put("diversityMap", ethnicityMap);
                        model.put("largestDiversityValue", largestValue);
                        model.put("largestDiversityLabel", largestLabel);
                        model.put( "content", "default" );
                        defaultDisplay = true;
                    }
                }
                catch( NullPointerException e ) {
                    // Nothing to do
                }

            }
        }

        // Substitute action 1
        if( defaultDisplay == false ) {
            model.put( "content", "substitute" );
        }
        return model;
    }

    Map<String, Object> getSpecialEdEspTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

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
            specialEdModel.put( "content", "default" ); // To help with unit tests
        }
        else {
            displayDefault = false;
            specialEdModel.put( "content", "substitute" ); // To help with unit tests
        }

        if( displayDefault ) {
            // 
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
                else if( specEdPgmsExistNoOrBlank ) {
                    // Display option 'c' - exact message depends on spec_ed_level
                    if( checkEspResponseListForValue(specEdLevel, new String[]{"basic"} ) ) {
                        specialEdModel.put( "SpecEdPgmsProvided", "basic" );
                        specialEdModel.put( "SpecEdPgmsOptSelected", "c-basic" ); // To help with unit tests
                    }
                    else if( checkEspResponseListForValue(specEdLevel, new String[]{"moderate"} ) ) {
                        specialEdModel.put( "SpecEdPgmsProvided", "moderate" );
                        specialEdModel.put( "SpecEdPgmsOptSelected", "c-moderate" ); // To help with unit tests
                    }
                    else if( checkEspResponseListForValue(specEdLevel, new String[]{"intensive"} ) ) {
                        specialEdModel.put( "SpecEdPgmsProvided", "intensive" );
                        specialEdModel.put( "SpecEdPgmsOptSelected", "c-intensive" ); // To help with unit tests
                    }
                    else if( hasAcademicFocus ) {
                        // Display option 'd' - school has a focus
                        specialEdModel.put( "SpecEdPgmsProvided", "focuses" );
                        specialEdModel.put( "SpecEdPgmsOptSelected", "d" ); // To help with unit tests
                    }
                    else if ( hasSpecEdCoord ) {
                        // Display option 'e' - special ed coordinator
                        specialEdModel.put( "SpecEdPgmsProvided", "coordinator" );
                        specialEdModel.put( "SpecEdPgmsOptSelected", "e" ); // To help with unit tests
                    }
                    else if ( specEdPgmsExistNo || (hasSpecEdLevelNone && !hasAcademicFocus && !hasSpecEdCoord) ) {
                        // Display option 'f' - No services
                        specialEdModel.put( "SpecEdPgmsProvided", "no" );
                        specialEdModel.put( "SpecEdPgmsOptSelected", "f" ); // To help with unit tests
                    }
                }
                else {
                    // Call the school
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
                specialEdModel.put( "ExtdCareBefore", new Boolean(hasBeforeCare) );
                specialEdModel.put( "ExtdCareAfter", new Boolean(hasAfterCare) );
                if( isNotEmpty(beforeAfterCareStart) ) {
                    specialEdModel.put( "ExtdCareBeforeTime", beforeAfterCareStart.get(0).getSafeValue() );
                }
                if( isNotEmpty(beforeAfterCareEnd) ) {
                    specialEdModel.put( "ExtdCareAfterTime", beforeAfterCareEnd.get(0).getSafeValue() );
                }
//                if( isEmpty(beforeAfterCare) ) {
//                    // before or after not specified
//                    if( isNotEmpty(beforeAfterCareStart) ) {
//                        specialEdModel.put( "ExtdCareBefore", "Starts: " + beforeAfterCareStart.get(0).getSafeValue() );
//                    }
//                    if( isNotEmpty(beforeAfterCareEnd) ) {
//                        specialEdModel.put( "ExtdCareAfter", "Ends: " + beforeAfterCareEnd.get(0).getSafeValue() );
//                    }
//                }
//                if( hasBeforeCare ) {
//                    // before care is specified
//                    StringBuffer sb = new StringBuffer("Before school");
//                    if( isNotEmpty(beforeAfterCareStart) ) {
//                        // Add start time
//                        sb.append(": Starts ").append( beforeAfterCareStart.get(0).getSafeValue() );
//                    }
//                    specialEdModel.put( "ExtdCareBefore", sb.toString() );
//                }
//                if( hasAfterCare ) {
//                    // before care is specified
//                    StringBuffer sb = new StringBuffer("After school");
//                    if( isNotEmpty(beforeAfterCareEnd) ) {
//                        // Add end time
//                        sb.append(": Ends ").append(beforeAfterCareEnd.get(0).getSafeValue());
//                    }
//                    specialEdModel.put( "ExtdCareAfter", sb.toString() );
//                }
            }
            else {
                // No specific content is available - show call school message
                specialEdModel.put( "ExtdCareProvided", "noInfo" );  // In the end - can we get rid of this

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
            specialEdModel.put( "content", "default" );
        }
        else {
            // 
            // Substitute 1 - Teachers and staff Autotext
//            List<EspResponse> administrator = espData.get("administrator_name");
            SchoolCensusValue administratorSCV = _schoolProfileDataHelper.getSchoolCensusValue( request, CensusDataType.HEAD_OFFICIAL_NAME );
            String administrator = null;
            if( administratorSCV != null ) {
                administrator = administratorSCV.getValueText();
            }
            List<EspResponse> staffResources = espData.get("staff_resources");
            List<EspResponse> staffResourcesWoNone = copyAndRemove( staffResources, new String[]{"none"} );

            if( StringUtils.isNotEmpty(administrator) || isNotEmpty(staffResourcesWoNone) ){
                StringBuilder sentence = new StringBuilder();
                // Sentence 1, about the school administrator
                if( StringUtils.isNotEmpty( administrator ) ) {
                    sentence.append( administrator );
                    sentence.append( " leads this school. " );
                }

                // Sentence 2, about the staff resources
                if( isNotEmpty( staffResourcesWoNone ) ) {
                    sentence.append( "Staff includes ");
                    sentence.append( prettyCommaSeparatedStringOfEspResponse( staffResourcesWoNone ) );
                }

                specialEdModel.put( "teachersStaff", sentence.toString() );
                specialEdModel.put( "content", "teachers/staff" );
            }
            else {
                // Substitute 2 - Static text
                specialEdModel.put( "content", "reviewCta" );
            }
        }

        return specialEdModel;
    }

    Map<String, Object> getTransportationEspTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

        Map<String, Object> model = new HashMap<String, Object>(2);

        // Extract some data to get started
        List<EspResponse> transpShuttle = espData.get("transportation_shuttle");
        boolean isTranspShuttleYes = checkEspResponseListForValue( transpShuttle, new String[]{"yes"} );
        boolean isTranspShuttleNo = checkEspResponseListForValue( transpShuttle, new String[]{"no"} );
        boolean isTranspShuttleNoOrBlank = ( isEmpty(transpShuttle) || isTranspShuttleNo );

        List<EspResponse> transpShuttleOther = espData.get( "transportation_shuttle_other" );

        List<EspResponse> transp = espData.get( "transportation" );
        boolean transpNone = checkEspResponseListForValue( transp, new String[]{"none"} );
        boolean transpNoneOrBlank = (transpNone || isEmpty(transp));

        List<EspResponse> transpOther = espData.get("transportation_other");

        if( isNotEmpty(transp) || isNotEmpty(transpOther) || isNotEmpty(transpShuttle) || isNotEmpty(transpShuttleOther)) {
            // Default action
            model.put( "content", "default" );
            if( isTranspShuttleYes && isNotEmpty(transpShuttleOther) ) {
                // Option a from spec - Display Metro icon and stops
                model.put( "icon", "metro" );
                model.put( "shuttleStops", transpShuttleOther.get(0).getPrettyValue() );
            }
            else if( isTranspShuttleYes && isEmpty(transpShuttleOther)  ) {
                // Option b from spec - Display Metro icon and static message
                model.put( "icon", "metro" );
                model.put( "transMsg", "Shuttles provided to local Metro stops" );
            }
            else if( isTranspShuttleNo && isEmpty(transp) && isEmpty(transpOther) ) {
                // Option c from spec - Display Walking person icon and static message
                model.put( "icon", "walking" );
                model.put( "transMsg", "No transportation provided" );
            }
            else if( checkEspResponseListForValue(transp, new String[]{"passes"}) ) {
                // Option d from spec - Display static message
                model.put( "icon", "passes" );
                model.put( "transMsg", "Passes/tokens provided for public transportation" );
            }
            else if( checkEspResponseListForValue(transp, new String[]{"special_ed_only"}) ){
                // Option e from spec - Display handicapped icon & static message
                model.put( "icon", "handicapped" );
                model.put( "transMsg", "Transportation provided for special education students" );
            }
            else if( checkEspResponseListForValue(transp, new String[]{"busses"}) ) {
                // Option f from spec - Display bus icon static & message
                model.put( "icon", "bus" );
                model.put( "transMsg", "Busses/vans provided for students" );
            }
            else if( checkEspResponseListForValue(transp, new String[]{"shared_bus"}) ) {
                // Option g from spec - Display handicapped icon & static message
                model.put( "icon", "bus" );
                model.put( "transMsg", "Busses/vans shared with other schools" );
            }
            else if( transpNoneOrBlank && isNotEmpty(transpOther) ) {
                // Option h from spec - Display walking person icon & static message
                model.put( "icon", "walking" );
                model.put( "transMsg", "Other transportation provided" );
            }
            else if( transpNone && isEmpty(transpOther) ) {
                // Option i from spec - Display walking person icon & static message
                model.put( "icon", "walking" );
                model.put( "transMsg", "No transportation provided" );
            }
            else {
                // No provided in spec and probably should not get here, but if we do
                model.put( "icon", "walking" );
                model.put( "transMsg", "No transportation provided" );
            }
        }
        else {
            // Substitute action.  Need to decide if 1 or 2
            boolean substitute1Ok = false;
            Map<CensusDataType, List<CensusDataSet>> censusValues = _schoolProfileDataHelper.getSchoolCensusValues(request);
            if( censusValues!=null ) {
                List<CensusDataSet> classSizeCensusDataSet = censusValues.get( CensusDataType.CLASS_SIZE );
                List<CensusDataSet> studentsPerTeacherCensusDataSet = censusValues.get( CensusDataType.STUDENT_TEACHER_RATIO );
                // Substitute action 1
                // There are a lot of places an NPE can occur so just do a try/catch and see what happens
                Integer classSize = -1;
                int classSizeYear = -1;
                Integer studentsPerTeacher = -1;
                int studentsPerTeacherYear = -1;

                if( isNotEmpty(classSizeCensusDataSet) ) {
                    try {
                        SchoolCensusValue [] csv = (SchoolCensusValue [])classSizeCensusDataSet.get(0).getSchoolData().toArray(new SchoolCensusValue[1]);
                        classSize = csv[0].getValueInteger();
                        classSizeYear = classSizeCensusDataSet.get(0).getYear();
                    }
                    catch( NullPointerException e ) {
                        // Nothing to do
                    }
                }
                if( isNotEmpty(studentsPerTeacherCensusDataSet) ) {
                    try {
                        SchoolCensusValue [] csv = (SchoolCensusValue [])studentsPerTeacherCensusDataSet.get(0).getSchoolData().toArray(new SchoolCensusValue[1]);
                        studentsPerTeacher = csv[0].getValueInteger();
                        studentsPerTeacherYear = studentsPerTeacherCensusDataSet.get(0).getYear();
                    }
                    catch( NullPointerException e ) {
                        // Nothing to do
                    }
                }
                String state = school.getStateAbbreviation().getAbbreviation();
                // Special rules for NY and TX.  Only use studentsPerTeacher otherwise do substitute 2
                if( (state.equals("NY") || state.equals("TX")) ) {
                    if( studentsPerTeacher > 0 ) {
                        model.put("substitute1StudentsPerTeacher", studentsPerTeacher);
                        model.put("substitute1StudentsPerTeacherYear", new Integer(studentsPerTeacherYear));
                        model.put( "content", "substitute1" );
                        substitute1Ok = true;
                    }
                    else {
                        // No students/teacher data - fall through to substitute2
                    }
                }
                else if( classSize > 0 && studentsPerTeacher > 0) {
                    // If we have both, use the one with the later year
                    if( classSizeYear >= studentsPerTeacherYear ) {
                        model.put("substitute1ClassSize", classSize);
                        model.put("substitute1ClassSizeYear", new Integer(classSizeYear));
                        model.put( "content", "substitute1" );
                        substitute1Ok = true;
                    }
                    else {
                        model.put("substitute1StudentsPerTeacher", studentsPerTeacher);
                        model.put("substitute1StudentsPerTeacherYear", new Integer(studentsPerTeacherYear));
                        model.put( "content", "substitute1" );
                        substitute1Ok = true;
                    }
                }
                else if( classSize > 0 ) {
                    model.put("substitute1ClassSize", classSize);
                    model.put( "content", "substitute1" );
                    substitute1Ok = true;
                }
                else if( studentsPerTeacher > 0 ) {
                    model.put("substitute1StudentsPerTeacher", studentsPerTeacher);
                    model.put( "content", "substitute1" );
                    substitute1Ok = true;
                }
            }
            if( substitute1Ok == false ) {
                // Substitute action 2
                model.put( "content", "substitute2" );
                // Static test will be displayed.
            }
        }

        return model;
    }

    /**
     * Helper to get specific census value
     *
     *
     * @param censusValues
     * @param censusDataType The dataSet to retrieve
     * @return Null if not found, otherwise the value
     */
    private Integer getCensusDataValue(List<SchoolCensusValue> censusValues, CensusDataType censusDataType) {
        for( SchoolCensusValue v : censusValues ) {
            if( v.getDataSet().equals(censusDataType ) ) {
                return new Integer( censusDataType.getValue() );
            }
        }
        return null;  // No value in results
    }

    Map<String, Object> getProgramsEspTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

        Map<String, Object> model = new HashMap<String, Object>(2);

        // Get useful data
        List<EspResponse> immersion = espData.get( "immersion" );
        boolean hasImmersionYes = checkEspResponseListForValue(immersion, new String[]{"yes"});
        List<EspResponse> immersionLang = espData.get("immersion_language");
        List<EspResponse> immersionLangOther = espData.get("immersion_language_other");

        List<EspResponse> instrModelWoNone = copyAndRemove( espData.get( "instructional_model" ), new String[]{"none"} );

        List<EspResponse> instrModelOther = espData.get( "instructional_model_other" );

        List<EspResponse> academicFocusWoNone = copyAndRemove( espData.get( "academic_focus" ), new String[]{"none"} );

        List<EspResponse> academicFocusOther = espData.get( "academic_focus_other" );

        if( hasImmersionYes || (immersionLang!=null && immersionLang.size()>0) ||
                (immersionLangOther!=null && immersionLangOther.size()>0) ||
                instrModelWoNone.size()>0 ||
                academicFocusWoNone.size()>0 ) {
            // Default action
            model.put( "content", "default" );
            List<String> results = new ArrayList<String>();
            // Immersion
            if( hasImmersionYes ) {
                if( immersionLang!=null && immersionLang.size()>0 ) {
                    results.add( createCommaList( immersionLang ) + " immersion" );
                }
                else {
                    results.add( "Language immersion" );
                }
            }

            // Instructional model -  each item is a separate list item
            String instructionalModel = "";
            if( instrModelWoNone.size()>0 ){
                for( EspResponse r : instrModelWoNone ) {
                    results.add( r.getPrettyValue() );
                }
            }
            if( instrModelOther!=null && instrModelOther.size()>0 ){
                results.add( instrModelOther.get(0).getPrettyValue() );
            }

            // Academic focus
            String academicFocus = "";
            if( academicFocusWoNone.size()>0 ) {
                academicFocus = createCommaList(academicFocusWoNone);
            }
            if( academicFocusOther!=null && academicFocusOther.size()>0 ) {
                // append other
                if( academicFocus.length() > 0 ) {
                    academicFocus += ", " + academicFocusOther.get(0).getPrettyValue();
                }
                else {
                    academicFocus = academicFocusOther.get(0).getPrettyValue();
                }
            }
            if( academicFocus.length() > 0 ) {
                results.add( "School focus: " + academicFocus );
            }
           model. put( "resultsList", results );
        }
        else {
            // Will be a substitute action, determine which one.
            List<EspResponse> parentInvolvement = espData.get("parent_involvement");
            if( parentInvolvement!=null && parentInvolvement.size()>0 ) {
                // Substitute action 1
                model.put( "content", "substitute1" );
                boolean hasParentInvolvementNone = checkEspResponseListForValue( parentInvolvement, new String[]{"none"} );
                List<EspResponse> parentInvolvementWoNone = copyAndRemove(parentInvolvement, new String[]{"none"});
                // Check if 'none' is the only value
                if( hasParentInvolvementNone && parentInvolvementWoNone.size()==0 ) {
                    model.put( "substitute1None", "true" );
                }
                else {
                    model.put( "substitute1List", createSortedList( parentInvolvementWoNone ) );
                }
            }
            else {
                // Substitute action 2
                model.put( "content", "substitute2" );
                // Create a copy of the Subtype so it can be changed without impacting the school object
                String subtypeStr = school.getSubtype().asCommaSeparatedString();
                SchoolSubtype subtype = SchoolSubtype.create( subtypeStr );
                // Per the spec ignore the following subtypes
                subtype.remove("preschool_early_childhood_center");
                subtype.remove("elementary");
                subtype.remove("middle");
                subtype.remove("high");
                subtype.remove("combined_elementary_and_secondary");
                subtype.remove("secondary");
                String commaSepList = subtype.asPrettyCommaSeparatedString();
                List<String> substitute2List = new ArrayList<String>();
                if( commaSepList!=null && commaSepList.length()>0 ) {
                    String[] subTypeItems = commaSepList.split( ", " );
                    for( int i = 0; i < subTypeItems.length; i++ ) {
                        // The pretty substrings only have underscores removed.  Need to cap first letter
                        substitute2List.add( createPrettyValue( subTypeItems[i] ) );
                    }
                }
                String affiliation = school.getAffiliation();
                if( affiliation!=null && affiliation.length()>0 ) {
                    substitute2List.add( "Affiliation: " + affiliation );
                }
                String associations = school.getAssociation();
                if( associations!=null && associations.length()>0 ) {
                    substitute2List.add(( "Associations: " + associations ) );
                }
                if( substitute2List.size() > 0 ) {
                    model.put( "substitute2List", substitute2List );
                }
            }
        }

        return model;
    }

    Map<String, Object> getApplInfoEspTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

        Map<String, Object> model = new HashMap<String, Object>(2);

        // Get useful data
        List<EspResponse> applicationProcess = espData.get( "application_process" );
        boolean hasApplicationProcessYes = checkEspResponseListForValue(applicationProcess, new String[]{"yes"});
        List<EspResponse> studentsAcceptedList = espData.get("students_accepted");
        List<EspResponse> applicationsReceivedList = espData.get("applications_received");
        List<EspResponse> applicationsReceivedYearList = espData.get("applications_received_year");
        List<EspResponse> studentsAcceptedYearList = espData.get("students_accepted_year");

        boolean doSubstitute = false;

        // Check if default action can be done
        if( hasApplicationProcessYes && isNotEmpty(studentsAcceptedList) && isNotEmpty(applicationsReceivedList) &&
                isNotEmpty(applicationsReceivedYearList) && isNotEmpty(studentsAcceptedYearList) &&
                applicationsReceivedYearList.get(0).getValue().equals(studentsAcceptedYearList.get(0).getValue()) ) {
            // The values should be valid numbers, but need to be sure
            try {
                model.put( "content", "applInfoV1" );
                // Acceptance Rate Calc
                float studentsAccepted = Float.parseFloat(studentsAcceptedList.get(0).getValue());
                float applicationsReceived = Float.parseFloat(applicationsReceivedList.get(0).getValue());
                if( studentsAccepted > 0.0 && applicationsReceived > 0.0 ) {
                    int acceptanceRate = Math.round((studentsAccepted/applicationsReceived)*10);
                    // if acceptanceRate is 0 but there were some applications then show 1
                    if( acceptanceRate == 0 && studentsAccepted > 0 ) {
                        acceptanceRate = 1;
                    }
                    else if( acceptanceRate > 10 ) {
                        acceptanceRate = 10;
                    }
                    String acceptanceRateStr = Integer.toString( acceptanceRate );
                    model.put( "acceptanceRate", acceptanceRateStr );
                    model.put( "acceptanceRateYear", studentsAcceptedYearList.get(0).getValue() );
                }
                else {
                    doSubstitute = true;
                }

                // Get the stuff for the right half
                getApplInfoDeadlineInfo(espData, model);
            } catch ( NumberFormatException e ) {
                doSubstitute = true;
            }
        }
        else {
            doSubstitute = true;
        }

        if( doSubstitute ) {
            // Decide which substitute to do
            if( hasApplicationProcessYes ) {
                // Substitute action 1
                // Left content (tile 11) , ApplInfo deadline & vouchers
                getApplInfoDeadlineInfo(espData, model);

                // Right content (tile 12), school video tour
                Map<String, Object> tourVideoModel = getTourVideoModel( request, school );
                model.putAll( tourVideoModel );

                model.put( "content", "applInfoV2" );
            }
            else {
                // Substitute action 2, school visit checklist
                // Left content (tile 11), school visit checklist
                Map<String, Object> checklistModel = getSchoolVisitChecklistTile(request, school);
                model.putAll( checklistModel );

                // Right content (tile 12), school video tour
                Map<String, Object> tourVideoModel = getTourVideoModel( request, school );
                model.putAll( tourVideoModel );

                model.put("content", "applInfoV3");
            }
        }

        return model;
    }

    // This is a helper method for getApplInfoEspTile() and has been moved to a separate tile because it can be used in 2 places
    private void getApplInfoDeadlineInfo(Map<String, List<EspResponse>> espData, Map<String, Object> model) {
        // Application deadline information
        List<EspResponse> applicationDeadlineList = espData.get("application_deadline");

        if( isNotEmpty(applicationDeadlineList) ) {
            String applicationDeadlineType = applicationDeadlineList.get(0).getValue();
            if( "date".equals( applicationDeadlineType ) ) {
                // Date
                List<EspResponse> applicationDeadlineDateList = espData.get("application_deadline_date");
                if( isNotEmpty( applicationDeadlineDateList ) ) {
                    String applicationDeadlineDate = applicationDeadlineDateList.get(0).getValue();
                    model.put( "applicationDeadlineDate", applicationDeadlineDate );
                    model.put( "applicationDeadlineMsg", "apply" );
                }
                else {
                    // No date
                    model.put( "applicationDeadlineMsg", "call" );
                }
            }
            else if( "yearround".equals( applicationDeadlineType ) ) {
                // Yearround
                model.put( "applicationDeadlineMsg", "yearround" );
            }
            else if( "parents_contact".equals( applicationDeadlineType ) ) {
                //
                model.put( "applicationDeadlineMsg", "call" );
            }
            else {
                // This condition is not handled in the spec and should not occur, but call school
                model.put( "applicationDeadlineMsg", "call" );
            }
        }
        else {
            // Application Deadline Type is missing
            model.put( "applicationDeadlineMsg", "call" );
        }

        // Voucher info
        List<EspResponse> vouchersList = espData.get("students_vouchers");
        boolean hasVouchersYes = checkEspResponseListForValue(vouchersList, new String[]{"yes"});
        boolean hasVouchersNo = checkEspResponseListForValue(vouchersList, new String[]{"no"});

        if( hasVouchersYes ) {
            model.put( "vouchers", "yes" );
        }
        else if( hasVouchersNo ) {
            model.put( "vouchers", "no" );
        }
        else {
            model.put( "vouchers", "blank" );
        }

    }

    Map<String, Object> getLocalInfoEspTile(HttpServletRequest request, School school) {

        Map<String, Object> model = new HashMap<String, Object>(2);

        // TODO - Default content is in design.  Will have to display substitute for now

        boolean doSubstitute = true;


        // Default action

        // Substitute action 1
        if( doSubstitute ) {
            model = getDistrictInfoModel(request, school);
        }

        return model;
    }

    /**
     * Builds the model data for the District Info tile if the required data is available, otherwise builds data for Neighborhood Info
     * @param request
     * @param school
     * @return
     */
    private Map<String, Object> getDistrictInfoModel(HttpServletRequest request, School school) {

        Map<String, Object> model = new HashMap<String, Object>(2);

        // Check if this can be done or need to do substitute 2
        SchoolType schoolType = school.getType();
        int districtId = school.getDistrictId();
        if( districtId > 0 ) {
            District district = school.getDistrict();
            if( district != null ) {
                if( schoolType.equals(SchoolType.PUBLIC) || (schoolType.equals(SchoolType.CHARTER) && district.getNumberOfSchools()>1 ) ) {
                    model.put( "districtName", district.getName() );
                    model.put( "districtNumSchools", new Integer(district.getNumberOfSchools()) );
                    model.put( "districtGrades", district.getGradeLevels().getRangeString() );
                    model.put( "content", "districtInfo" );
                    return model;
                }
            }
        }

        // Build neighborhood info
        BpZip sperlings = _schoolProfileDataHelper.getSperlingsInfo( request );
        if( sperlings != null ) {
            // Get city name for title
            String cityName = school.getCity();
            if( StringUtils.isNotEmpty( cityName ) ) {
                model.put( "title", "Living in " + cityName );
            }
            else {
                model.put( "title", "Neighbor Information" );
            }

            // Build content sentence
            StringBuilder sentence = new StringBuilder();
            // sentence 1
            String neighborType = sperlings.getNeighborhoodType();
            if( "Suburban".equals( neighborType ) ) {
                sentence.append( "Situated in a suburban neighborhood. " );
            }
            else if( "Rural".equals( neighborType ) ) {
                sentence.append( "Situated in a rural neighborhood. " );
            }
            else if( "Small Town".equals( neighborType ) ) {
                sentence.append( "Situated in a small town neighborhood. " );
            }
            else if( "City Neighborhood".equals( neighborType ) ) {
                sentence.append( "Situated in an urban neighborhood. " );
            }
            else if( "Inner City".equals( neighborType ) ) {
                sentence.append( "Situated in an inner city neighborhood. " );
            }

            // sentence 2
            Float medianValue = sperlings.getHouseMedianValue();
            if( medianValue != null ) {
                sentence.append("The median home value is $").append( String.format("%,d", medianValue.intValue()) ).append(". ");
            }

            // sentence 3
            Float rent = sperlings.getRentApt2br();
            if( rent != null ) {
                sentence.append("The average monthly rent for a 2 bedroom apartment is $").append( String.format("%,d", rent.intValue()) ).append(". ");
            }
            model.put( "neighborhoodInfo", sentence.toString()  );
            model.put( "content", "neighborhoodInfo" );
        }

        return model;
    }

    private Map<String, Object> getRelatedEspTile(HttpServletRequest request, School school, int numArticles) {

        Map<String, Object> model = new HashMap<String, Object>(2);

        // Default action
        Map<String, List<EspResponse>> espData = _schoolProfileDataHelper.getEspDataForSchool(request);
        List<ICmsFeatureSearchResult> cmsResults = _schoolProfileDataHelper.getCmsRelatedContent(request, espData, numArticles);

        model.put( "content", "default" );

        // if there is a video, then move to top of list
        for (int i = 0; i<cmsResults.size(); i++) {
            if (cmsResults.get(i).getContentType().equals("Video")){
                ICmsFeatureSearchResult videoResult = cmsResults.remove(i);
                cmsResults = new LinkedList<ICmsFeatureSearchResult>(cmsResults);
                ((LinkedList<ICmsFeatureSearchResult>)cmsResults).addFirst(videoResult);
                model.put("videoResult", videoResult);
                break;
            }
        }

        model.put( "cmsResults", cmsResults );

        return model;
    }

    Map<String, Object> getSportsArtsMusicEspTile(Map<String, List<EspResponse>> espData) {

        Map<String, Object> sportsModel = new HashMap<String, Object>(4);
        // Sports
        List<String> boysSports = getEspDataByKey( "boys_sports", NoneHandling.HIDE_IF_ONLY_NONE, espData );
        Collections.sort(boysSports);
        sportsModel.put("boys_sports", boysSports);
        List<String> girlsSports = getEspDataByKey("girls_sports", NoneHandling.HIDE_IF_ONLY_NONE, espData);
        Collections.sort(girlsSports);
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
            sportsModel.put("arts", arts);
        }

        // Music
        List<String> music = getEspDataByKey("arts_music", NoneHandling.SHOW_IF_ONLY_VALUE, espData);
        Collections.sort( music );
        sportsModel.put("music", music);

        // Check if no data and flag that Substitute content is to be displayed
        int total = boysSports.size() + girlsSports.size() + artsMedia.size() + artsPerformingWritten.size() +
                artsVisual.size() + music.size();
        if( total == 0 ) {
            sportsModel.put("content", "Hide");
        }

        return sportsModel;
    }

    private Map<String, Object> getFacebookTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

        Map<String, Object> facebookModel = new HashMap<String, Object>(4);

        List<EspResponse> facebook = espData.get("facebook_url");
        String facebookUrl = null;
        if( isNotEmpty(facebook) ) {
            facebookUrl = SchoolProfileCultureController.cleanUpUrl(facebook.get(0).getSafeValue(), "facebook.com");
        }

        if( facebookUrl != null ) {
            facebookModel.put( "content", "show" );
            facebookModel.put( "facebookUrl", facebookUrl );
        }
        else {
            facebookModel.put( "content", "hide" ); // set this if no data available
        }

        return facebookModel;
    }

    // ===================== Non ESP page ==============================
    private void handleNonEspPage(ModelMap model, HttpServletRequest request, School school) {

        // Tile 1 - Default: Ratings, Substitute: Autotext(About this school)
        model.put( RATINGS_BREAKDOWN_MODEL_KEY, getGsRatingsTileNoOsp(request, school) );

        // Title 2 - Default: Photos, Substitute 1: Principal CTA
        model.put(PHOTOS_MODEL_KEY, getPhotosEspTile(request, school));

        // Title 3 - Default: Boundary tool promo (This is in the profileOverviewVideoTile.tagx because it is substitute on ESP page), Substitute: none
        model.put( VIDEO_MODEL_KEY, getBoundaryToolModel());

        // Title 4 - Default: Community ratings, Substitute 1: Review CTA
        model.put( COMMUNITY_RATING_MODEL_KEY, getCommunityRatingEspTile(request, school) );

        // Titles 5&6 - Default: Reviews carousel, Substitute 1: Review CTA
        model.put( REVIEWS_MODEL_KEY, getReviewsEspTile(request, school) );

        // Title 7 - Default: Student diversity, Substitute 1: Generic text about diversity
        model.put( DIVERSITY_MODEL_KEY, getDiversityEspTile(request, school) );

        // Title 8 - Default: School visit checklist
        model.put( VIDEO_TOUR_MODEL_KEY, getTourVideoModel(request, school) );

        // Title 9 - Default: Transportation, Substitute 1: Students per teacher / average class size, Substitute 2: School boundary tool promo
        model.put( LOCAL_INFO_MODEL_KEY, getLocalInfoEspTile(request, school) );

        // Row 4 - Default: Related content
        model.put( RELATED_CONTENT_MODEL_KEY, getRelatedEspTile(request, school, 6) );

    }

    Map<String, Object> getSchoolVisitChecklistTile(HttpServletRequest request, School school) {

        Map<String, Object> model = new HashMap<String, Object>(1);

        LevelCode levelCode = school.getLevelCode();
        if( levelCode != null ) {
            List<String> levels = new ArrayList<String>();
            if( levelCode.containsLevelCode(LevelCode.Level.PRESCHOOL_LEVEL) )  levels.add( "Preschool" );
            if( levelCode.containsLevelCode(LevelCode.Level.ELEMENTARY_LEVEL) )  levels.add( "Elementary school" );
            if( levelCode.containsLevelCode(LevelCode.Level.MIDDLE_LEVEL) )  levels.add( "Middle school" );
            if( levelCode.containsLevelCode(LevelCode.Level.HIGH_LEVEL) )  levels.add( "High school" );
            if( levels.size() > 0 ) {
                model.put( "checklist", levels );
            }
        }

        model.put( "content", "visitChecklist" );

        return model;
    }

    // =================== Utility functions follow ============================
    /**
     * Helper to copy a list of EspResponses and remove any specified values
     * @param espResponsesIn - The input list
     * @param valuesToRemove - the values to remove
     * @return - the list with the specified values removed
     */
    private List<EspResponse> copyAndRemove(List<EspResponse> espResponsesIn, String[] valuesToRemove) {
        int size = (espResponsesIn==null || espResponsesIn.size()==0) ? 1 : espResponsesIn.size();
        List<EspResponse> espResponsesOut = new ArrayList<EspResponse>( size );

        if( espResponsesIn!=null && espResponsesIn.size()>0 ) {
            for( EspResponse r : espResponsesIn ) {
                if( findInArray( r.getValue(), valuesToRemove ) == false ) {
                    espResponsesOut.add( r );
                }
            }
        }

        return espResponsesOut;
    }

    /**
     * Little helper to look for a matching value in an array
     * @param valueToFind - The value to look for
     * @param valuesToCompare - The array of values to compare
     * @return true if valueToFind is in the array
     */
    private boolean findInArray(String valueToFind, String[] valuesToCompare) {
        if( valueToFind==null || valueToFind.length()==0 | valuesToCompare==null || valuesToCompare.length==0 ) {
            return false;
        }

        for( int i = 0; i < valuesToCompare.length; i++ ) {
            if( valueToFind.equals( valuesToCompare[i]) ) {
                return true;
            }
        }
        return false;  // Not found.
    }


    /**
     * Utility to create a list of display values in sorted order
     * @param listToSort The list to sort
     * @return a List of the pretty print values
     */
    private List<String> createSortedList(List<EspResponse> listToSort) {
        int size = (listToSort==null) ? 0 : listToSort.size();
        List<String> results = new ArrayList<String>(size);

        for( EspResponse r : listToSort ) {
            results.add( r.getPrettyValue() );
        }

        Collections.sort(results);

        return results;
    }

    /**
     * Utility to convert a list of EspResponse objects into a sorted comma separated String
     * @param espResponse - the list to sort and convert to a String
     * @return - The sorted comma separated result
     */
    private String createCommaList(List<EspResponse> espResponse) {
        if( espResponse == null || espResponse.size()==0 ) {
            return "";
        }

        // Convert to list of String and sort
        List<String> l1 = createSortedList( espResponse );

        // Convert to a comma separated list
        StringBuilder sb = new StringBuilder();
        for( int i = 1; i < l1.size(); i++ ) {
            sb.append( l1.get(i-1)).append(", ");
        }
        sb.append(l1.get(l1.size()-1)); // add final member

        return sb.toString();
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

    /**
     * Utility function that extracts the pretty values from an EspResponse List and also handles hiding None values
     * @param key The key to the list that is to be processed
     * @param noneHandling How the none value is to be handled
     * @param espData The raw EspData as a Map by key
     * @return The desired pretty values
     */
    private List<String> getEspDataByKey(String key, NoneHandling noneHandling, Map<String, List<EspResponse>> espData) {
        List<String> results = new ArrayList<String>();
        List<EspResponse> espResponses = espData.get( key );
        if (espResponses != null && espResponses.size() > 0) {
            // Check for none
            if( espResponses.size() == 1 && espResponses.get(0).getValue().equalsIgnoreCase("none")) {
                if( noneHandling == NoneHandling.HIDE_IF_ONLY_NONE ) {
                    return results;
                }
                else if( noneHandling == NoneHandling.SHOW_IF_ONLY_VALUE ) {
                    results.add( espResponses.get(0).getPrettyValue() );
                    return results;
                }
            }
            for( EspResponse espResponse : espResponses ) {
                if( espResponse.getSafeValue().equalsIgnoreCase("none")) {
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


    boolean isNotEmpty( Collection c ) {
        return ( (c!=null) && c.size()>0 );
    }


    boolean isEmpty( Collection c ) {
        return ((c==null) || (c.size()==0));
    }

    // Create a pretty value by capitalizing thr first character and removing underscores
    private String createPrettyValue( String value ) {
        StringBuilder sb = new StringBuilder();

        sb.append( Character.toUpperCase( value.charAt(0) ) );
        for( int i = 1; i < value.length(); i++ ) {
            char c = value.charAt(i);
            if( c == '_' ) {
                sb.append( ' ' );
            }
            else {
                sb.append( c );
            }
        }

        return sb.toString();
    }

    /**
     * Utility routine to convert a List of strings into a comma separated list but with 'and' instead
     * of a comma for the last item
     * @param attributes The list to convert
     * @return the converted list
     */
    private String prettyCommaSeparatedString(List<String> attributes) {
        StringBuilder sentence = new StringBuilder();
        int size = attributes.size();
        for( int i = 0; i < size; i++ ) {
            sentence.append(attributes.get(i));
            if( i < size-2 ) {
                sentence.append(", ");
            }
            else if( i==size-2 ) {
                sentence.append(" and ");
            }
        }
        return sentence.toString();
    }

    private String prettyCommaSeparatedStringOfEspResponse( List<EspResponse> attributes ) {
        List<String> stringList = new ArrayList<String>( attributes.size() );
        for( EspResponse r : attributes ) {
            stringList.add( r.getPrettyValue() );
        }
        String result = prettyCommaSeparatedString( stringList );
        return result;
    }

    // Utility function to convert census data value to appropriate string
    private String formatValueAsString(Float value, CensusDataType.ValueType valueType) {
        String result;
        if (CensusDataType.ValueType.PERCENT.equals(valueType)) {
            result = String.valueOf(Math.round(value)) + "%";
        } else if (CensusDataType.ValueType.MONETARY.equals(valueType)) {
            result = "$" + String.valueOf(value);
        } else {
            result = String.valueOf(Math.round(value));
        }

        return result;
    }

    // The following setter dependency injection is just for the tester
    public void setSchoolProfileDataHelper( SchoolProfileDataHelper schoolProfileDataHelper ) {
        _schoolProfileDataHelper = schoolProfileDataHelper;
    }



    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    // ============== The following setters are just for unit testing ===================
    public void setCmsFeatureSearchService( CmsFeatureSearchService cmsFeatureSearchService ) {
        _cmsFeatureSearchService = cmsFeatureSearchService;
    }

    public void setCmsFeatureEmbeddedLinkResolver(CmsContentLinkResolver cmsFeatureEmbeddedLinkResolver) {
        _cmsFeatureEmbeddedLinkResolver = cmsFeatureEmbeddedLinkResolver;
    }

//    public  void setPublicationDao( IPublicationDao publicationDao ) {
//        _publicationDao = publicationDao;
//    }

}