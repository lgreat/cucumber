package gs.web.school;

import gs.data.cms.IPublicationDao;
import gs.data.community.User;
import gs.data.content.cms.*;
import gs.data.geo.bestplaces.BpZip;
import gs.data.school.*;
import gs.data.school.census.Breakdown;
import gs.data.school.census.CensusDataSet;
import gs.data.school.census.CensusDataType;
import gs.data.school.census.SchoolCensusValue;
import gs.data.school.district.District;
import gs.data.school.review.Review;
import gs.data.util.CommunityUtil;
import gs.web.content.cms.CmsContentLinkResolver;
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
    private static final String DIVERSITY_MODEL_KEY = "diversity";
    private static final String SPECIAL_EDUCATION_MODEL_KEY = "specialEd";
    private static final String TRANSPORTATION_MODEL_KEY = "transportation";
    private static final String PROGRAMS_MODEL_KEY = "programs";
    private static final String APPL_INFO_MODEL_KEY = "applInfo";
    private static final String LOCAL_INFO_MODEL_KEY = "localInfo";
    private static final String RELATED_CONTENT_MODEL_KEY = "related";
    private static final String SPORTS_MODEL_KEY = "sports";

    private static final String SCHOOL_VISIT_CHECKLIST_MODEL_KEY = "schoolVisit";

    private static final int VIDEO_ELEMENTARY = 6857;
    private static final int VIDEO_MIDDLE = 6856;
    private static final int VIDEO_HIGH = 6855;


    public enum NoneHandling{ ALWAYS_SHOW, SHOW_IF_ONLY_VALUE, HIDE_IF_ONLY_NONE }

    String _viewName;

    @Autowired
    private SchoolProfileDataHelper _schoolProfileDataHelper;

    @Autowired
    private CmsFeatureDao _cmsFeatureDao;

    @Autowired
    private CmsContentLinkResolver _cmsFeatureEmbeddedLinkResolver;

    @Autowired
    private IPublicationDao _publicationDao;

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

        return VIEW;
    }

    private void handleEspPage(Map model, HttpServletRequest request, School school, Map<String,List<EspResponse>> espData ) {

        // First row is "Best know for quote", get it and add to model
        model.put(BEST_KNOWN_FOR_MODEL_KEY, getBestKnownForQuoteEspTile(school, espData));

        // Tile 1 - Default: Ratings, Substitute 1: Awards, Substitute 2: Autotext(About this school)
        //  Waiting for research team to investigate Ratings
        model.put( RATINGS_BREAKDOWN_MODEL_KEY, getGsRatingsEspTile(request, school, espData) );

        // Title 2 - Default: Photos, Substitute 1: Principal CTA
        model.put(PHOTOS_MODEL_KEY, getPhotosEspTile(request, school));

        // Title 3 - Default: Videos, Substitute 1: Information from CMS
        model.put( VIDEO_MODEL_KEY, getVideoEspTile(request, school, espData) );

        // Title 4 - Default: Community ratings, Substitute 1: Review CTA
        model.put( COMMUNITY_RATING_MODEL_KEY, getCommunityRatingEspTile(request, school) );

        // Titles 5&6 - Default: Reviews carousel, Substitute 1: Review CTA
        model.put( REVIEWS_MODEL_KEY, getReviewsEspTile(request, school) );

        // Title 7 - Default: Student diversity, Substitute 1: Generic text about diversity
        model.put( DIVERSITY_MODEL_KEY, getDiversityEspTile(request, school) );

        // Title 8 - Default: Special education/extended care, Substitute 1: Nearby schools teaser
        // This is all ESP data and complicated rules.  DO SOON
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
        model.put( RELATED_CONTENT_MODEL_KEY, getRelatedEspTile(request, school) );

        // 7th row is Sports/Arts/Music
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
        }
        else {
            reviewsModel.put( "content", "reviewsCTA" );
        }

        return reviewsModel;
    }

    private Map<String, Object> getCommunityRatingEspTile(HttpServletRequest request, School school) {
        Map<String, Object> communityModel = new HashMap<String, Object>(3);
        communityModel.put( "school", school );
        communityModel.put( "ratings", _schoolProfileDataHelper.getSchoolRatings( request ) );
        communityModel.put( "numberOfReviews", _schoolProfileDataHelper.getCountPublishedNonPrincipalReviews(request) );

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

    private Map getGsRatingsEspTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

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

    private Map getSchoolAutotext(HttpServletRequest request, School school) {

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
        //Integer enrollment = school.getEnrollment();
        Integer enrollment = 512;  // TODO - Need help from Anthony figuring out how to mock this???
        if( school.getName()!=null && school.getName().length()>0 &&
                school.getType()!=null && school.getType().getSchoolTypeName().length()>0 &&
                enrollment!=null ) {
            sentence.append( school.getName() ).append( " is a " ).append( school.getType().getSchoolTypeName() ).
                    append( " school serving " ).append( enrollment.intValue() ).append( " students" );
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

    private Map getGsRatingsModel(HttpServletRequest request, School school) {

        Map<String, Object> model = null;

        // When spec is ready with default action update this set of variables as needed
        boolean doDefault = false;

        if( doDefault ) {
            // Default action
            model = new HashMap<String, Object>(2);
            // TODO - Default action code needs to be added when spec is ready
            model.put( "content", "GsRatings" );
        }

        return model;
    }


    private Map getAwardsModel(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

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


    private Map getVideoEspTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

        Map<String, Object> model = null;

        // Get all of the date needed to make the required decisions
        List<EspResponse> video = espData.get("school_video");
        if( isNotEmpty( video ) ) {
            model = new HashMap<String, Object>(2);
            model.put( "content", "video" );
            model.put( "videoUrl", video.get(0).getSafeValue() );
        }
        else {
            // Substitute action
            model = getTourVideoModel( request, school );

        }

        return model;
    }

    private Map getTourVideoModel(HttpServletRequest request, School school) {

        Map<String, Object> model = new HashMap<String, Object>(2);

        // Which video to display will depend on the lowest level taught at the school.  For instance if level_code is e,m,h then just show for e
        if( school.getLevelCode() != null ) {
            String levelCodeList = school.getLevelCode().getCommaSeparatedString();
            String [] levelCodes = levelCodeList.split(",");
            // Pick smallest level code that is not p (preschool).  If p is the only level code use e
            String levelCode = levelCodes[0];
            if( "p".equals( levelCode ) ) {
                if( levelCodes.length > 1 ) {
                    levelCode = levelCodes[1];
                }
                else {
                    levelCode = "e";
                }
            }
            model.put( "schoolLevel", levelCode );
            // determine which video to show
            int videoId = 0;
            if( "e".equals(levelCode) ) {
                videoId = VIDEO_ELEMENTARY;
            }
            else if( "m".equals(levelCode) ) {
                videoId = VIDEO_MIDDLE;
            }
            else if( "h".equals(levelCode) ) {
                videoId = VIDEO_HIGH;
            }
            // videoId = 4910; // Debug, this is an existing Id in dev-cms

            try {
                // Implementation based on code in CmsVideoController
                /* TODO this implementation is based on using CmsFeature which causes a database access the
                   alternative would be to add a method to CmsFeatureSearchService.java (and
                   CmsFeatureSearchServiceSolrImpl.java) and use Solr to fetch a search result for the video you're
                   interested in, and call getImageUrl() from that search result instead of from the object fetched
                   from the database.   */

                CmsFeature feature = _cmsFeatureDao.get(new Long(videoId));
                if( feature == null ) {
                    // This should not happen unless the article is not present in CMS
                    model.put( "content", "none" );
                    return  model;
                }

                // it would be simpler to call UrlBuilder(feature.getContentKey()) but that calls publicationDao
                // which can not overridden in UrlBuilder and thus can not be unit tested
                Publication pub = _publicationDao.findByContentKey(feature.getContentKey());
                if( pub == null ) {
                    model.put( "content", "none" );
                    return  model;
                }
                String fullUri = pub.getFullUri();
                UrlBuilder urlBuilder = new UrlBuilder(feature.getContentKey(), fullUri);

                model.put( "content", "schoolTourVideo" );
                model.put( "contentUrl", urlBuilder.asSiteRelativeXml(request));
                model.put( "videoIconUrl", feature.getImageUrl() );
                model.put( "videoIconAltText", feature.getImageAltText() );
            }
            catch (UnsupportedOperationException e ) {
                // This happens when CMS is down which shouldn't happen now.  But if it does happen don't try to display a video
                model.put( "content", "none" );
                return  model;
            }

        }

        return model;
    }


    private Map getDiversityEspTile(HttpServletRequest request, School school) {

        Map<String, Object> model = new HashMap<String, Object>(2);

        // Default action
        boolean defaultDisplay = false;
        Map<CensusDataType, List<CensusDataSet>> censusValues = _schoolProfileDataHelper.getSchoolCensusValues(request);
        if( censusValues!=null ) {
            List<CensusDataSet> ethnicities = censusValues.get( CensusDataType.STUDENTS_ETHNICITY );
            if( isNotEmpty( ethnicities) ) {
                try {
                    Map<String, String> ethnicityMap = new HashMap<String, String>();
                    Float largestValue = new Float(0.0);
                    String largestLabel = "";
                    for( CensusDataSet ethnicity : ethnicities ) {
                        Breakdown b = ethnicity.getBreakdownOnly();
                        String name = b.getEthnicity().getName();
                        SchoolCensusValue [] schoolCensusValue = (SchoolCensusValue [])ethnicity.getSchoolData().toArray(new SchoolCensusValue[1]);
                        if( schoolCensusValue.length >0 && schoolCensusValue[0]!=null && schoolCensusValue[0].getValueFloat()!=null ) {
                            Float value = schoolCensusValue[0].getValueFloat();
                            if( value.compareTo( largestValue ) > 0 ) {
                                largestValue = value;
                                largestLabel = name;
                            }
                            ethnicityMap.put( name, formatValueAsString(value, CensusDataType.STUDENTS_ETHNICITY.getValueType()) );
                        }
                    }
                    if( ethnicityMap.size() > 0 ) {
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

    private Map getSpecialEdEspTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

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
                        specialEdModel.put( "ExtdCareBefore", "Starts: " + beforeAfterCareStart.get(0).getSafeValue() );
                    }
                    if( isNotEmpty(beforeAfterCareEnd) ) {
                        specialEdModel.put( "ExtdCareAfter", "Ends: " + beforeAfterCareEnd.get(0).getSafeValue() );
                    }
                }
                if( hasBeforeCare ) {
                    // before care is specified
                    StringBuffer sb = new StringBuffer("Before school");
                    if( isNotEmpty(beforeAfterCareStart) ) {
                        // Add start time
                        sb.append(": Starts ").append( beforeAfterCareStart.get(0).getSafeValue() );
                    }
                    specialEdModel.put( "ExtdCareBefore", sb.toString() );
                }
                if( hasAfterCare ) {
                    // before care is specified
                    StringBuffer sb = new StringBuffer("After school");
                    if( isNotEmpty(beforeAfterCareEnd) ) {
                        // Add end time
                        sb.append(": Ends ").append(beforeAfterCareEnd.get(0).getSafeValue());
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
            specialEdModel.put( "content", "default" );
        }
        else {
            // 
            // Substitute 1 - Teachers and staff Autotext
            List<EspResponse> administrator = espData.get("administrator_name");
            List<EspResponse> staffResources = espData.get("staff_resources");
            List<EspResponse> staffResourcesWoNone = copyAndRemove( staffResources, new String[]{"none"} );

            if( isNotEmpty(administrator) || isNotEmpty(staffResourcesWoNone) ){
                StringBuilder sentence = new StringBuilder();
                // Sentence 1, about the school administrator
                if( isNotEmpty( administrator ) ) {
                    sentence.append( administrator.get(0).getPrettyValue() );
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
        }

        return specialEdModel;
    }

    private Map getTransportationEspTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

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

        if( isNotEmpty(transp) || isNotEmpty(transpShuttle) ) {
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
                model.put( "transMsg", "Shuttles are provide to local Metro stops" );
            }
            else if( isTranspShuttleNo && isEmpty(transp) && isEmpty(transpOther) ) {
                // Option c from spec - Display Walking person icon and static message
                model.put( "icon", "walking" );
                model.put( "transMsg", "No transportation provided" );
            }
            else if( checkEspResponseListForValue(transp, new String[]{"passes"}) ) {
                // Option d from spec - Display static message
                model.put( "icon", "passes" );
                model.put( "transMsg", "Passes/tokens for public transportation" );
            }
            else if( checkEspResponseListForValue(transp, new String[]{"special_ed_only"}) ){
                // Option e from spec - Display handicapped icon & static message
                model.put( "icon", "handicapped" );
                model.put( "transMsg", "Transportation provided for special education students only" );
            }
            else if( checkEspResponseListForValue(transp, new String[]{"busses"}) ) {
                // Option f from spec - Display bus icon static & message
                model.put( "icon", "bus" );
                model.put( "transMsg", "Busses/vans for our students only" );
            }
            else if( checkEspResponseListForValue(transp, new String[]{"shared_bus"}) ) {
                // Option g from spec - Display handicapped icon & static message
                model.put( "icon", "bus" );
                model.put( "transMsg", "School shares bus/van with other schools" );
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
            /* Wait until Samson enhances the data helper */
            boolean substitute1Ok = false;
            Map<CensusDataType, List<CensusDataSet>> censusValues = _schoolProfileDataHelper.getSchoolCensusValues(request);
            if( censusValues!=null ) {
                List<CensusDataSet> classSize = censusValues.get( CensusDataType.CLASS_SIZE );
                List<CensusDataSet> studentsPerTeacher = censusValues.get( CensusDataType.STUDENT_TEACHER_RATIO );
                // Substitute action 1
                // There are a lot of places an NPE can occur so just do a try/catch and see what happens
                if( isNotEmpty(classSize) ) {
                    try {
                        SchoolCensusValue [] csv = (SchoolCensusValue [])classSize.get(0).getSchoolData().toArray(new SchoolCensusValue[1]);
                        model.put("substitute1ClassSize", csv[0].getValueInteger());
                        model.put( "content", "substitute1" );
                        substitute1Ok = true;
                    }
                    catch( NullPointerException e ) {
                        // Nothing to do
                    }
                }
                else if( isNotEmpty(studentsPerTeacher) ) {
                    try {
                        SchoolCensusValue [] csv = (SchoolCensusValue [])studentsPerTeacher.get(0).getSchoolData().toArray(new SchoolCensusValue[1]);
                        model.put("substitute1StudentsPerTeacher", csv[0].getValueInteger());
                        model.put( "content", "substitute1" );
                        substitute1Ok = true;
                    }
                    catch( NullPointerException e ) {
                        // Nothing to do
                    }
                }
                else {
                    model.put( "content", "substitute2" );
                    // Static test will be displayed.
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

    private Map getProgramsEspTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

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

            // Instructional model
            String instructionalModel = "";
            if( instrModelWoNone.size()>0 ){
                instructionalModel = createCommaList(instrModelWoNone);
            }
            if( instrModelOther!=null && instrModelOther.size()>0 ){
                // append other
                if( instructionalModel.length() > 0 ) {
                    instructionalModel += ", " + instrModelOther.get(0).getPrettyValue();
                }
                else {
                    instructionalModel = instrModelOther.get(0).getPrettyValue();
                }
            }
            if( instructionalModel.length() > 0 ) {
                results.add(  instructionalModel );
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

    private Map getApplInfoEspTile(HttpServletRequest request, School school, Map<String, List<EspResponse>> espData) {

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
                int acceptanceRatePercent = (int) ((studentsAccepted/applicationsReceived)*100);
                String acceptanceRatePercentStr = Integer.toString( acceptanceRatePercent );
                model.put( "acceptanceRatePercent", acceptanceRatePercentStr );
                model.put( "acceptanceRateYear", studentsAcceptedYearList.get(0).getValue() );

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
                model.put( "content", "applInfoV2" );
                getApplInfoDeadlineInfo(espData, model);
            }
            else {
                // Substitute action 2, school visit checklist
                model.put( "content", "visitChecklist" );
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
        List<EspResponse> vouchersList = espData.get("vouchers");
        boolean hasVouchersYes = checkEspResponseListForValue(vouchersList, new String[]{"yes"});
        boolean hasVouchersNo = checkEspResponseListForValue( vouchersList, new String[]{"no"} );

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

    private Map getLocalInfoEspTile(HttpServletRequest request, School school) {

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
    private Map getDistrictInfoModel(HttpServletRequest request, School school) {

        Map<String, Object> model = new HashMap<String, Object>(2);

        // Check if this can be done or need to do substitute 2
        SchoolType schoolType = school.getType();
        int districtId = school.getDistrictId();
        if( districtId > 0 ) {
            District district = school.getDistrict();
            if( district != null ) {
                if( schoolType.equals(SchoolType.PUBLIC) || (schoolType.equals(SchoolType.CHARTER) && district.getNumberOfSchools()>0 ) ) {
                    model.put( "districtName", district.getName() );
                    model.put( "districtNumSchools", new Integer(district.getNumberOfSchools()) );
                    model.put( "districtGrades", district.getGradeLevels().getRangeString() );
                    model.put( "districtNumStudents", "Samson is getting" );
                    model.put( "content", "districtInfo" );
                    return model;
                }
            }
        }

        // Build neighborhood info
        BpZip sperlings = _schoolProfileDataHelper.getSperlingsInfo( request );
        if( sperlings != null ) {
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

    private Map getRelatedEspTile(HttpServletRequest request, School school) {

        Map<String, Object> model = new HashMap<String, Object>(2);

        // TODO - This is a stub - the appropriate code needs to be inserted below
        // Default action

        return model;
    }

    private Map<String, Object> getSportsArtsMusicEspTile(Map<String, List<EspResponse>> espData) {

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

    // ===================== Non ESP page ==============================
    private void handleNonEspPage(ModelMap model, HttpServletRequest request, School school) {

        // Tile 1 - Default: Ratings, Substitute: Autotext(About this school)
        //  Waiting for research team to investigate Ratings
        model.put( RATINGS_BREAKDOWN_MODEL_KEY, getGsRatingsTileNoOsp(request, school) );

        // Title 2 - Default: Photos, Substitute 1: Principal CTA
        model.put(PHOTOS_MODEL_KEY, getPhotosEspTile(request, school));

        // Title 3 - Default: Videos, Substitute 1: Information from CMS
        model.put( VIDEO_MODEL_KEY, getTourVideoModel(request, school) );

        // Title 4 - Default: Community ratings, Substitute 1: Review CTA
        model.put( COMMUNITY_RATING_MODEL_KEY, getCommunityRatingEspTile(request, school) );

        // Titles 5&6 - Default: Reviews carousel, Substitute 1: Review CTA
        model.put( REVIEWS_MODEL_KEY, getReviewsEspTile(request, school) );

        // Title 7 - Default: Student diversity, Substitute 1: Generic text about diversity
        model.put( DIVERSITY_MODEL_KEY, getDiversityEspTile(request, school) );

        // Title 8 - Default: School visit checklist
        model.put( SCHOOL_VISIT_CHECKLIST_MODEL_KEY, getSchoolVisitChecklistTile(request, school) );

        // Title 9 - Default: Transportation, Substitute 1: Students per teacher / average class size, Substitute 2: School boundry tool promo
        model.put( LOCAL_INFO_MODEL_KEY, getLocalInfoEspTile(request, school) );

        // Row 4 - Default: Related content
        model.put( RELATED_CONTENT_MODEL_KEY, getRelatedEspTile(request, school) );

        return;
    }

    private Map getSchoolVisitChecklistTile(HttpServletRequest request, School school) {

        Map<String, Object> model = new HashMap<String, Object>(1);

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
     * Utility to convert a list of EspResponse objects into a sort comma separated String
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
    public void setCmsFeatureDao( CmsFeatureDao cmsFeatureDao ) {
        _cmsFeatureDao = cmsFeatureDao;
    }

    public void setCmsFeatureEmbeddedLinkResolver(CmsContentLinkResolver cmsFeatureEmbeddedLinkResolver) {
        _cmsFeatureEmbeddedLinkResolver = cmsFeatureEmbeddedLinkResolver;
    }

    public  void setPublicationDao( IPublicationDao publicationDao ) {
        _publicationDao = publicationDao;
    }


}