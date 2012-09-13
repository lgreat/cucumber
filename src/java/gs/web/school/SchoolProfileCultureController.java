package gs.web.school;

import gs.data.school.EspResponse;
import gs.data.school.School;
import gs.data.school.SchoolMedia;
import gs.data.util.CommunityUtil;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Controller
@RequestMapping("/school/profileCulture.page")
public class SchoolProfileCultureController extends AbstractSchoolProfileController {
    private static final Log _log = LogFactory.getLog(SchoolProfileCultureController.class);

    private static final String PHOTOS_MODEL_KEY = "photos";
    private static final String VIDEO_MODEL_KEY = "video";
    private static final String CLIMATE_RATING_MODEL_KEY = "climateRating";
    private static final String FACEBOOK_MODEL_KEY = "facebook";

    @Autowired
    private SchoolProfileDataHelper _schoolProfileDataHelper;

      //If this controller needs to be called directly then add a handler mapping for RequestMethod.GET and create a VIEW page.
//    @RequestMapping(method = RequestMethod.GET)
//    public String getCulturePage(ModelMap modelMap, HttpServletRequest request) {
//        return VIEW;
//    }

    private static final String [] MODEL_PREFIXES = {"culture"};

    private static final List<SchoolProfileDisplayBean> DISPLAY_CONFIG = new ArrayList<SchoolProfileDisplayBean>();
    static {
        buildCultureDisplayStructure();
    }

    /**
     * This is the entry point for this controller.
     * As of 8/7/12 it is invoked from the SchoolProfileProgramsController
     * @param modelMap
     * @param request
     */
    protected void getCultureDetails(ModelMap modelMap, HttpServletRequest request) {

        // Get the espData for use below
        Map<String, List<EspResponse>> espResults = _schoolProfileDataHelper.getEspDataForSchool(request);

        // Get the climate ratings data - Much of the remainder of this tile depends on if ratings are available
        Map<String, Object> climateRatings = getSchoolClimateRatings(request);
        boolean hasClimateRating = false;
        if( climateRatings.get("content").equals("default") ) {
            hasClimateRating = true;
        }

        // Space 1 - show photos or hide the row
        modelMap.put(PHOTOS_MODEL_KEY, getSchoolPhotos( request));

        // Spaces 2 & 5 - Facebook can appear in in space 2 or 5 depending on other data availability
        Map<String, Object> facebookModel = getFacebook( espResults, hasClimateRating );
        modelMap.put( FACEBOOK_MODEL_KEY, facebookModel );
        // The facebook ID needs to be put directly into the model so the frontend code can go to facebook to verify it is a valid facebook user
        modelMap.put( "schoolFacebookId", facebookModel.get("facebookId") );

        // Space 3 - Videos or Principal CTA
        modelMap.put( VIDEO_MODEL_KEY, getSchoolVideos(espResults) );

        // Space 4 - Climate Rating or Tips
        modelMap.put( CLIMATE_RATING_MODEL_KEY, climateRatings );

        // Space 6 - show OSP data or hide the row - This is NOT a tile but is a table like the other tabs on this page
        if (espResults != null && espResults.size() > 0) {
            Map<String, List<String>> resultsModel = SchoolProfileProgramsController.buildDisplayData(espResults, DISPLAY_CONFIG);
            SchoolProfileProgramsController.sortResults(resultsModel, DISPLAY_CONFIG);
            SchoolProfileProgramsController.applyNoneHandlingRule( resultsModel, DISPLAY_CONFIG );
            modelMap.put("cultureProfileData", resultsModel);
            SchoolProfileProgramsController.buildDisplayModel(MODEL_PREFIXES, resultsModel, DISPLAY_CONFIG, modelMap);
        }
    }

    protected Map<String, Object> getSchoolPhotos(HttpServletRequest request) {
        Map<String, Object> photosModel = new HashMap<String, Object>(2);

        List<SchoolMedia> photoGalleryImages = _schoolProfileDataHelper.getSchoolMedia(request);
        if (photoGalleryImages != null && photoGalleryImages.size() > 0) {
            photosModel.put("basePhotoPath", CommunityUtil.getMediaPrefix());
            photosModel.put("photoGalleryImages", photoGalleryImages);
        }

        return photosModel;
    }

    protected Map<String, Object> getSchoolVideos(  Map<String, List<EspResponse>> espData ) {

        Map<String, Object> model = new HashMap<String, Object>(2);

        // Get all of the data needed to make the required decisions
        if( espData != null && isNotEmpty( espData.get("school_video") ) ) {
            model.put( "content", "video" );
            model.put( "videoUrl", espData.get("school_video").get(0).getSafeValue() );
        }
        else {
            // Substitute action, Principal CTA
            model.put( "content", "principalCTA" );
        }

        return model;
    }

    protected  Map<String, Object>  getSchoolClimateRatings( HttpServletRequest request ) {

        Map<String, Object> model = new HashMap<String, Object>(2);

        // Get climate rating from datahelper
        Map<String, Object> gsRatings = _schoolProfileDataHelper.getGsRatings(request);
        Object climateRating = null;
        boolean hasClimateRatings = false;
        if( gsRatings != null && !gsRatings.isEmpty() ) {
            hasClimateRatings = true;
            climateRating = gsRatings.get(_schoolProfileDataHelper.DATA_OVERALL_CLIMATE_RATING);
            if( climateRating != null ) {
                model.put( "content", "default" );
                try {
                    int climateRatingInt = ((Integer)climateRating).intValue();
                    double climateRatingDouble = climateRatingInt;
                    String climateRatingLabel = SchoolProfileRatingsController.getLabelForClimateRating(climateRatingDouble);
                    model.put( "climateRating", climateRatingLabel );   // This needs to be acquired from the data helper
                    return model;
                }
                catch (Exception e ) {
                    _log.error("SchoolProfileCultureController: Could not convert numeric overall climate rating to a double");
                }
            }
        }
        // No ratings, show tips
        model.put( "content", "tips" );

        // The following 2 lines are for debug testing until gsRatings works.
//        model.put( "content", "default" );
//        model.put( "climateRating", "AVERAGE QUALITY" );   // This needs to be acquired from the data helper

        return model;
    }

    protected  Map<String, Object>  getFacebook(Map<String, List<EspResponse>> espData, boolean hasClimateRating) {

        Map<String, Object> model = new HashMap<String, Object>(2);

        // Make sure we have espData otherwise there won't be a facebookURL
        if( espData == null ) {
            model.put( "content", "hide" );
            return model;
        }

        // If and where the facebook tile is displayed is dependent on:
        // 1. if there is a facebook link in the espData
        // 2. if climate ratings data is available
        // 3. if a school video is available

        // Gather the required data
        List<EspResponse> facebook = espData.get("facebook_url");
        String facebookUrl = null;
        if( isNotEmpty(facebook) ) {
            facebookUrl = cleanUpUrl( facebook.get(0).getSafeValue(), "facebook.com" );
        }

        // Now decide if facebook data can be shown and where
        if( facebookUrl != null ) {
            if( hasClimateRating ) {
                // This is the criteria to show facebook in space 5
                model.put( "content", "space5" );
                model.put( "facebookUrl", facebookUrl );
            }
            else {
                // This is the criteria to show facebook in space 2\
                model.put( "content", "space2" );
                model.put( "facebookUrl", facebookUrl );
            }
            String facebookId = getFacebookId(facebookUrl);
            if( facebookId != null ) {
                model.put( "facebookId", facebookId );
            }

        }
        else {
            model.put( "content", "hide" );
        }
        return model;
    }

    public static String getFacebookId( String facebookUrl ) {

        // It seems we always want the last part of the path
        try {
            URL url = new URL( facebookUrl );
            String path = url.getPath();
            if(!StringUtils.isEmpty(path)) {
                // It seems we always want the last part of the path
                String [] urlParts = path.split("/");
                if( urlParts.length == 0) {
                    return null;
                }
                String id = urlParts[urlParts.length-1];
                return id;
            }
        }
        catch( MalformedURLException e ) {
            return null;
        }
        return null;
    }

    /**
     * Takes a url (most likely from ESP data) and validates it and cleans it up if possible
     * @param urlToCleanup
     * @param baseUrl
     * @return The full URL or null if it is determined to not be valid
     */
    public static String cleanUpUrl(String urlToCleanup, String baseUrl) {

        if( urlToCleanup == null ) {
            return null;
        }
        else if( urlToCleanup.startsWith("www." + baseUrl) ) {
            return "http://" + urlToCleanup;
        }
        else if( urlToCleanup.startsWith("http://www." + baseUrl) ) {
            return urlToCleanup;
        }
        else if( urlToCleanup.startsWith("https://www." + baseUrl) ) {
            return urlToCleanup;
        }
        else if( urlToCleanup.startsWith(baseUrl) ) {
            return "http://www." + urlToCleanup;
        }
        // We can't positively verify this is a valid URL
        return null;
    }

    private static void buildCultureDisplayStructure() {
        String tabAbbrev = "culture";
        String sectionAbbrev = "SchoolCulture";
        String sectionTitle = "School culture";
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Dress Code",
                "dress_code" ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Bullying policy",
                "bullying_policy" ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Parent involvement",
                "parent_involvement" ) );
        getLastDisplayBean().addKey("parent_involvement_other");
        getLastDisplayBean().setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_IF_NOT_ONLY_VALUE);
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "School colors",
                "school_colors" ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "School mascot",
                "school_mascot" ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "More from this school",
                "anything_else" ) );
    }

    boolean isNotEmpty( Collection c ) {
        return ( (c!=null) && c.size()>0 );
    }


    boolean isEmpty( Collection c ) {
        return ((c==null) || (c.size()==0));
    }

    // Little helper to get the last bean
    public static SchoolProfileDisplayBean getLastDisplayBean() {
        return DISPLAY_CONFIG.get( DISPLAY_CONFIG.size() - 1);
    }

    // The following setter dependency injection is just for the tester
    public void setSchoolProfileDataHelper( SchoolProfileDataHelper schoolProfileDataHelper ) {
        _schoolProfileDataHelper = schoolProfileDataHelper;
    }
}