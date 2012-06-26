package gs.web.school;

import gs.data.school.EspResponse;
import gs.data.school.IEspResponseDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * The request mapping does not have a trailing slash. This handles the url with and without a trailing slash.
 */
@Controller
@RequestMapping("/school/profilePrograms.page")
public class SchoolProfileProgramsController extends AbstractSchoolProfileController {
    private static final Log _log = LogFactory.getLog(SchoolProfileProgramsController.class);
    public static final String VIEW = "school/profilePrograms";
    /*  RJR - Comment out for now as this will be needed later
    public static final String MODEL_SUPERUSER_ERROR = "superUserError";
    */

    /*
    @Autowired
    private ISchoolDao _schoolDao;
    */

    // The following is a prefix for the Model items specific to this page
    private static final String [] MODEL_PREFIXES = {"highlights", "application_info", "programs_resources", "extracurriculars"};

    // The following drives creation of the display
    private static final List<SchoolProfileDisplayBean> DISPLAY_CONFIG = new ArrayList<SchoolProfileDisplayBean>();
    static {
        buildHighlightsDisplayStructure();
        buildApplicationInfoDisplayStructure();
        buildProgResDisplayStructure();
        buildExtrasDisplayStructure();
    }

    // The following structure contains the key_value's to return from the database
    // this information is available in DISPLAY_CONFIG structure created above
    private static final Set<String> _keyValuesToExtract = new HashSet<String>();
    static {
        for( SchoolProfileDisplayBean bean : DISPLAY_CONFIG) {
            _keyValuesToExtract.addAll(bean.getEspResponseKeys());
//            if( bean.getSupportEspResponseKeys() != null && bean.getSupportEspResponseKeys().size() > 0 ) {
//                _keyValuesToExtract.addAll(bean.getSupportEspResponseKeys());   // Support keys are in addition
//            }
        }
    }

    public static final String PARAM_STATE = "state";
    public static final String PARAM_SCHOOL_ID = "schoolId";

    @Autowired
    private IEspResponseDao _espResponseDao;

    @RequestMapping(method = RequestMethod.GET)
    public String showHighlightsPage(ModelMap modelMap, HttpServletRequest request,
                                  @RequestParam(value=PARAM_SCHOOL_ID, required=false) Integer schoolId,
                                 @RequestParam(value=PARAM_STATE, required=false) State state ) {

        // Get School
        School school = getSchool(request, state, schoolId);
        // Do I need this???
        if (state != null) {
            SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
            if (sessionContext != null) {
                sessionContext.setState(state);
            }
        }
        modelMap.put("school", school);

        // Add the display structure to the model for use in building the page structure
        modelMap.put( "ProfileDisplayStructure", DISPLAY_CONFIG);

        // Get Data
        if (school != null) {
            List<EspResponse> results = _espResponseDao.getResponsesByKeys( school, _keyValuesToExtract );

            if( results != null && !results.isEmpty() ) {

                // For performance reasons convert the results to a HashMap.  The key will be the espResponseKey
                // and the value will be the corresponding list of EspResponse objects
                Map<String, List<EspResponse>> dbResultsMap = dbResultsToMap(results);

                // The following builds the display data based on the DB results and the display requirements.
                // The key is the ModelKey and the value is a list of the values for that key
                Map<String, List<String>> resultsModel = buildDisplayData( dbResultsMap, DISPLAY_CONFIG);

                Map<String, List<String>> supportResultsModel = buildSupplementalDisplayData(dbResultsMap, DISPLAY_CONFIG);

                resultsModel.putAll( supportResultsModel );

                // Now handle the special cases where one row gets data from multiple response_keys
                // This data is specified in the ADDITIONAL_PAGE_DATA structure
                //Map<String, List<String>> additionalResults = buildDisplayData( resultsMap, ADDITIONAL_PAGE_DATA);

                // Finally, merge the second set of data into the first
                //mergeResults( resultsModel, additionalResults );

                // Perform unique data manipulation rules
                applyUniqueDataRules( resultsModel, DISPLAY_CONFIG );

                // Put the data into the model so it will be passed to the jspx page
                modelMap.put( "ProfileData", resultsModel );

                // One last thing...  Build the model for the display.
                // Each section becomes a separate table on the display and therefore needs to be in the model as such.
                // And, if a section is empty it should not be displayed.  The following function takes care of those requirements.
                buildDisplayModel(MODEL_PREFIXES, resultsModel, DISPLAY_CONFIG, modelMap );
            }

        }

        return VIEW;
    }

    private void buildSupportDisplayData() {
    }

    private Map<String, List<EspResponse>> dbResultsToMap(List<EspResponse> results) {

        Map<String, List<EspResponse>> resultsMap = new HashMap<String, List<EspResponse>>();

        // Loop over the incoming results and construct the Map
        for( EspResponse r : results ) {
            String key = r.getKey();
            List<EspResponse> existingList = resultsMap.get( key );
            if( existingList != null ) {
                // add to existing list
                existingList.add( r );
            }
            else {
                // Create new list and add to HashMap
                List<EspResponse> newList = new ArrayList<EspResponse>();
                newList.add( r );
                resultsMap.put( key, newList );
            }
        }
        return resultsMap;
    }

    // Build the display data by merging the display structure and DB results data
    private Map<String, List<String>> buildDisplayData(Map<String, List<EspResponse>> resultsMap, List<SchoolProfileDisplayBean> displayConfig) {

        // Create a Map to store the data in
        Map<String, List<String>> resultsModel = new HashMap<String, List<String>>();

        // Build by row by iterating over the display bean
        for( SchoolProfileDisplayBean display : displayConfig ) {
            List<String> rowData = getValuesForSectionAndKey(resultsMap, display );
            // Check this displayConfig instance to see if this is the special additional data case in which case there will be a
            // linkedResultKey in which case it should be used.
            String key = display.getModelKey();
            resultsModel.put( key, rowData );
        }

        return resultsModel;
    }

    private List<String> getValuesForSectionAndKey(Map<String, List<EspResponse>> espResponseMap, SchoolProfileDisplayBean bean ) {

        List<String> results = new ArrayList<String>();

        // Process all keys for this bean
        String [] keys = bean.getEspResponseKeys().toArray(new String[0]);
        for( String key : keys ) {
            // Get results list for this key
            List<EspResponse> espResponses = espResponseMap.get( key );

            // If there are some results then select the allowed values
            if( espResponses != null && espResponses.size() > 0 ) {
                // Loop through the responses and gather the ones in the allowed list
                for( EspResponse espResponse : espResponses ) {
                    String value = null;
                    if( bean.getAllowedResponseValues() != null && key.equals(bean.getEspResponseKeyWithAllowed())){
                        // There is a set of permitted values then check to see if this value is allowed
                        // the allowed values only applies to the first key added as indicated by getEspResponseKeyWithAllowed()
                        if( bean.getAllowedResponseValues().contains(espResponse.getValue())) {
                            value = espResponse.getPrettyValue();
                        }
                    }
                    else {
                        // There is no list of allowed values so just use what we have
                        value = espResponse.getPrettyValue();
                    }
                    if( value != null ) {
                        results.add( value );
                    }
                }
            }
        }

        return results;
    }

    /**
     * Build the data necessary for the "support", urlDesc, urlValue.
     * @param dbResultsMap
     * @param displayConfig
     * @return
     */
    private Map<String,List<String>> buildSupplementalDisplayData(Map<String, List<EspResponse>> dbResultsMap, List<SchoolProfileDisplayBean> displayConfig) {

        // Create a Map to store the data in
        Map<String, List<String>> resultsModel = new HashMap<String, List<String>>();

        // Build model data by iterating over the display beans
        for( SchoolProfileDisplayBean display : displayConfig ) {
            // Build support model data
//            List<String> supportEspResponseKeys = display.getSupportEspResponseKeys();
//            List<String> supportModelKeys = display.getSupportModelKeys();
//            if( supportEspResponseKeys != null ) {
//                // For each supportEspResponseKey get the data and add it to the model
//                for( int i = 0; i < supportEspResponseKeys.size(); i++ ) {
//                    List<EspResponse> espResponseData = dbResultsMap.get(supportEspResponseKeys.get(i) );
//                    if( espResponseData != null && espResponseData.size() > 0 ) {
//                        List<String> data = new ArrayList<String>( espResponseData.size() );
//                        for( EspResponse e : espResponseData ) {
//                            data.add( e.getPrettyValue() );
//                        }
//                    resultsModel.put( supportModelKeys.get(i), data );
//                    }
//                }
//            }

            // Build any required "Additional" data for this bean
            List<SchoolProfileDisplayBean.AdditionalData> additionalData = display.getAdditionalData();
            if( additionalData != null ) {
                // For each supportEspResponseKey get the data and add it to the model
                for( SchoolProfileDisplayBean.AdditionalData ad : additionalData ) {
                    List<EspResponse> espResponseData = dbResultsMap.get(ad.getEspResponseKey() );
                    if( espResponseData != null && espResponseData.size() > 0 ) {
                        List<String> data = new ArrayList<String>( espResponseData.size() );
                        for( EspResponse e : espResponseData ) {
                            data.add( e.getPrettyValue() );
                        }
                        resultsModel.put( ad.getModelKey(), data );
                    }
                }
            }



        }

        return resultsModel;
    }


    // Merge the data from additionalResultsModel resultsModel
    private void mergeResults(Map<String,List<String>> resultsModel, Map<String,List<String>> additionalResultsModel) {

        for( String key : additionalResultsModel.keySet() ) {
            List<String> additionalResultsModelData = additionalResultsModel.get( key );  // data from additionalResultsModel
            if( additionalResultsModelData != null && additionalResultsModelData.size() > 0 ) {
                List<String> resultsModelData = resultsModel.get( key );  // data from resultsModel
                if( resultsModelData == null ) {
                    // No data, create an empty list
                    resultsModelData = new ArrayList<String>();
                    resultsModel.put( key, resultsModelData );
                }
                resultsModelData.addAll( additionalResultsModelData );   // Done!
            }
        }

    }

    // Some rows need special manipulation for the display.  Those rules are applied here
    private void applyUniqueDataRules(Map<String, List<String>> resultsModel, List<SchoolProfileDisplayBean> displayConfig) {
        // In the Application Info tab the application process data possibly consists of two data results.
        // All results are to be displayed but with different text than in the database
        // If the results include Yes then reply with "Call the school"
        // Other entries can assume to be URL's which should be prefixed with "Visit the school's website: "
        List<String> admissions_contact_school_list = resultsModel.get( "application_info/AppEnroll/admissions_contact_school" );
        if( admissions_contact_school_list != null && admissions_contact_school_list.size() > 0 ) {
            List<String> result = new ArrayList<String>(1);
            for( String s : admissions_contact_school_list ) {
                if( s.equalsIgnoreCase("Yes") ) {
                    result.add( "Call the school" );
                }
                else {
                    // Assume this is a url.  The pretty print will have capitalized the first letter, chang back to lowercase
                    StringBuilder sb = new StringBuilder(s);
                    sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
                    sb.insert(0, "Visit the school's website: ");
                    result.add( sb.toString() );
                }
            }
            resultsModel.put( "application_info/AppEnroll/admissions_contact_school", result );
        }

        // For the Programs & Resources tab, Basics, Shuttle Info we may need to combine 2 results.
        // If ModelKey: "program_resources/Basics/transportation_shuttle" contains "yes" then show "Yes"
        // And, if there is a value in program_resources/Basics/transportation_shuttle_other add:
        // "Nearby lines: " and the value
        List<String> shuttle_list = resultsModel.get( "programs_resources/Basics/transportation_shuttle" );
        if( shuttle_list != null && shuttle_list.size() > 0 ) {
            String result = null;
            String shuttle = shuttle_list.get(0);
            if( shuttle.equalsIgnoreCase("yes")) {
                // Now see if there the additional data
                List<String> nearby_list = resultsModel.get( "programs_resources/Basics/transportation_shuttle_other" );
                if( nearby_list != null && nearby_list.size() > 0 ) {
                    result = "Yes. Nearby lines: " + nearby_list.get(0);
                }
                else {
                    result = "Yes.";
                }
                List<String> results = new ArrayList<String>(1);
                results.add( result );
                resultsModel.put("programs_resources/Basics/transportation_shuttle", results);
            }
            else {
                // Since answer must be no, remove existing result so the line wil not show
                resultsModel.remove( "program_resources/Basics/transportation_shuttle" );
            }
        }

        // Special handling for immersion and immersion_language
        // immersion is a Yes/No field.
        // If there are no immersion_language's then:
        //   if yes, show Yes.
        //   if no, don't show the line
        // Otherwise don't show immersion and only show immersion_language
        List<String> immersion = resultsModel.get( "programs_resources/Programs/immersion" );
        List<String> immersionLanguage = resultsModel.get( "programs_resources/Programs/immersion_language" );
        if( immersionLanguage == null || immersionLanguage.size() == 0 ) {
            // No immersion languages were supplied.  Use rules for immersion
            if( immersion != null && immersion.size() > 0 && immersion.get(0).equalsIgnoreCase("yes") ) {
                // Then show Yes which is already what should be there so no additional processing is needed
            }
            else {
                // immersion has to be no, remove it
                resultsModel.remove( "programs_resources/Programs/immersion" );
            }
        } else {
            // Need to show the immersion languages which can be done by replacing the contents of immersion with those of immersion_language
            resultsModel.put( "programs_resources/Programs/immersion", immersionLanguage );
        }

        // Special handling for programs_resources/Programs/advanced_placement_exams
        // TODO - Bob to ask Anthony where to get the data
        List<String> l = Arrays.asList( new String[] {"Bob to ask Anthony where to get the data"} );
        resultsModel.put( "programs_resources/Programs/advanced_placement_exams", l );

        // Handle special rules related to displaying or suppressing the display of the "none" result
        // This has to be done by looking at each display element and seeing if special processing is required
        for( SchoolProfileDisplayBean display : displayConfig ) {
            if( display.getRequiresNoneHandling() ) {
                SchoolProfileDisplayBean.NoneHandling noneHandling = display.getShowNone();
                if( noneHandling == SchoolProfileDisplayBean.NoneHandling.ALWAYS_SHOW ) {
                    // This means if the None value is in the results it should always be displayed.
                    // This is the the default behaviour
                }
                else {
                    // The following cases will require more data
                    List<String> modelData = resultsModel.get( display.getModelKey() );
                    if( modelData != null ) {
                        if( noneHandling == SchoolProfileDisplayBean.NoneHandling.HIDE_IF_ONLY_NONE ) {
                            // This means if the only value is none then the remove the data from the model
                            if( modelData.size() == 1 ) {
                                if( modelData.get(0).equalsIgnoreCase( "none" ) ) {
                                    // This is the situation where we need to remove that result
                                    resultsModel.remove( display.getModelKey() );
                                }
                            }
                        }
                        else {
                            // This has to be the SHOW_IF_ONLY_VALUE case
                            // This means if there are more than one db result values and one of them is "none" then just that value should be removed
                            if( modelData.size() > 1 ) {
                                // iterate through the data and remove none if found
                                for( int i = 0; i < modelData.size(); i++ ) {
                                    if( modelData.get(i).equalsIgnoreCase("none") ) {
                                        modelData.remove(i);
                                    }
                                }
                            }
                        }

                    }
                }
            }   // for


        }
    }

    // The display requires that each section is a separate html table and rows with no data are not displayed.
    // To meet that requirement a List will be built for each section and only the rows with data will be included.
    // And, if a section has no data it should not be displayed at all.
    // As each section is built the first display bean
    // will be added to a master List of all sections to be displayed.  If there is no data for a section the List will be empty.
    private void buildDisplayModel(String[] modelPrefix, Map<String, List<String>> results,
                                   List<SchoolProfileDisplayBean> displayConfig, ModelMap modelMap) {

        List<SchoolProfileDisplayBean> masterSectionList = new ArrayList<SchoolProfileDisplayBean>();
        List<SchoolProfileDisplayBean> sectionDisplayStructure = new ArrayList<SchoolProfileDisplayBean>();
        Map<String, List<SchoolProfileDisplayBean>> sectionDisplayMap = new HashMap<String, List<SchoolProfileDisplayBean>>();
        String lastTabAbbrev = null;
        String lastSectionAbbrev = null;

        // Walk the display config and note when the section changes
        for( SchoolProfileDisplayBean display : displayConfig) {
            // Initialize lastSectionAbbrev if it is null
            if( lastTabAbbrev == null ) {
                lastTabAbbrev = display.getTabAbbreviation();
                lastSectionAbbrev = display.getSectionAbbreviation();
            }

            // Check if we have switched to a new section
            if( !display.getSectionAbbreviation().equals( lastSectionAbbrev ) ) {
                // If the section just finished has data then add the SchoolProfileDisplayBean to the Model and add this
                // list to the master display list.
                if( sectionDisplayStructure.size() > 0 ) {
                    sectionDisplayMap.put( lastSectionAbbrev, sectionDisplayStructure );
                    masterSectionList.add(sectionDisplayStructure.get(0));
                    sectionDisplayStructure = new ArrayList<SchoolProfileDisplayBean>();  // start the one for the next section
                }
                // since this is a new section, update the indicator
                lastSectionAbbrev = display.getSectionAbbreviation();

                // Check if we have also switched tabs.  If so, put the existing data in the model map under the name of this tab
                // and reinitialize the model structures
                if( !display.getTabAbbreviation().equals( lastTabAbbrev ) ) {
                    modelMap.put( lastTabAbbrev, masterSectionList );
                    modelMap.put( lastTabAbbrev + "Map", sectionDisplayMap );
                    masterSectionList = new ArrayList<SchoolProfileDisplayBean>();
                    sectionDisplayMap = new HashMap<String, List<SchoolProfileDisplayBean>>();
                    lastTabAbbrev = display.getTabAbbreviation();
                }
            }

            // Check if this row has data
            String modelKey = display.getModelKey();
            List dataValues = results.get( modelKey );
            if( dataValues != null && dataValues.size() > 0) {
                sectionDisplayStructure.add(display);
            }

        }

        // output data for last section
        if( sectionDisplayStructure.size() > 0 ) {
            sectionDisplayMap.put( lastSectionAbbrev, sectionDisplayStructure );
            masterSectionList.add(sectionDisplayStructure.get(0));
        }

        // This is the list of all sections
        modelMap.put( lastTabAbbrev, masterSectionList );
        modelMap.put( lastTabAbbrev + "Map", sectionDisplayMap );

    }

    // Little helper to get the last bean
    private static SchoolProfileDisplayBean getLastDisplayBean( List<SchoolProfileDisplayBean> beans ) {
        return beans.get( beans.size() - 1);
    }

    // The following setter dependency injection is just for the tester
    public void setIEspResponseDao( IEspResponseDao espResponseDao ) {
        _espResponseDao = espResponseDao;
    }

    // This function is just to support the tester
    public Set<String> getKeyValuesToExtract() {
        return _keyValuesToExtract;
    }

    /* *****************************************************************************************************
       *        Functions that build the display structures
       *        They are places her at the bottom to keep them away from the main processing logic
       *****************************************************************************************************
     */
    private static void buildHighlightsDisplayStructure() {
        String tabAbbrev = "highlights";
        // Special Education section
        String specEdAbbrev = "SpecEd";
        String specEdTitle = "Special Education/Special Needs";
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, specEdAbbrev, specEdTitle, "Specific academic themes or areas of focus",
                "academic_focus", new String[]{"special_education"}) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, specEdAbbrev, specEdTitle, "Special education programming offered",
                "spec_ed_level") );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, specEdAbbrev, specEdTitle, "Specialized programs for specific types of special education students",
                "special_ed_programs") );
        /* 6/19/12 - Michael Hicks said this one is to be removed, it is not special education related
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, specEdAbbrev, specEdTitle, "Extra learning resources offered",
                "extra_learning_resources", new String[]{"differentiated"} )); */
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, specEdAbbrev, specEdTitle, "Staff resources available to students",
                "staff_resources", new String[]{"special_ed_coordinator", "speech_therapist"} ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, specEdAbbrev, specEdTitle, "Clubs",
                "student_clubs", new String[]{"special_olympics"} ) );

        // STEM (Science, Technology, Engineering, & Math)
        String stemAbbrev = "STEM";
        String stemTitle = "Science, Technology, Engineering, & Math";
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, stemAbbrev, stemTitle, "Specific academic themes or areas of focus",
                "academic_focus", new String[]{"mathematics", "science", "technology", "medical", "engineering"} ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, stemAbbrev, stemTitle, "Staff resources available to students",
                "staff_resources", new String[]{"computer_specialist", "robotics_teacher", "math_specialist", "garden_teacher"} ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, stemAbbrev, stemTitle, "School facilities",
                "facilities", new String[]{"computer ", "garden", "outdoor", "science", "farm", "industrial"} ) );
        getLastDisplayBean( DISPLAY_CONFIG ).setShowNone(SchoolProfileDisplayBean.NoneHandling.HIDE_IF_ONLY_NONE);
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, stemAbbrev, stemTitle, "Vocational or skills-based training offered",
                "skills_training", new String[]{"programming", "it_support", "mechanics", "electrical", "hvac", "engineering"} ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, stemAbbrev, stemTitle, "Visual arts",
                "visual_arts", new String[]{"ceramics", "drawing", "painting", "photography", "none", "architecture", "design", "printmaking", "sculpture", "textiles"} ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, stemAbbrev, stemTitle, "Clubs",
                "student_clubs", new String[]{"gardening", "math_club", "recycling", "robotics", "science_club", "tech_club"} ) );

        // Arts and Music
        String artsAbbrev = "Arts";
        String artsTitle = "Arts & Music";
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, artsAbbrev, artsTitle, "Specific academic themes or areas of focus",
                "academic_focus", new String[]{"all_arts", "music", "performing_arts", "visual_arts"} ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, artsAbbrev, artsTitle, "Staff resources available to students",
                "staff_resources", new String[]{"art_teacher", "dance_teacher", "music_teacher", "poetry_teacher"} ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, artsAbbrev, artsTitle, "School facilities",
                "facilities", new String[]{"art ", "music", "performance"} ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, artsAbbrev, artsTitle, "Vocational or skills-based training offered",
                "skills_training", new String[]{"design"} ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, artsAbbrev, artsTitle, "Visual arts",
                "arts_visual", new String[]{"ceramics", "drawing", "painting", "photography", "none", "architecture", "design", "printmaking", "sculpture", "textiles"} ) );
        getLastDisplayBean( DISPLAY_CONFIG ).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, artsAbbrev, artsTitle, "Music",
                "arts_music"));
        getLastDisplayBean( DISPLAY_CONFIG ).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, artsAbbrev, artsTitle, "Performing arts",
                "arts_performing_written"));
        getLastDisplayBean( DISPLAY_CONFIG ).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, artsAbbrev, artsTitle, "Media arts",
                "arts_media"));
        getLastDisplayBean( DISPLAY_CONFIG ).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, artsAbbrev, artsTitle, "Clubs",
                "student_clubs", new String[]{"student_newspaper", "yearbook", "anime", "art_club", "arts_crafts", "dance", "drama_club", "drill_team", "drum_line", "flag_girls", "literary_mag", "marching_band", "mime", "origami", "sewing_knitting", "step_team", "tv_radio_news", "woodshop"} ) );
        // need to combine student_clubs and student_clubs_dance data
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("student_clubs_dance");
        getLastDisplayBean( DISPLAY_CONFIG ).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);

        // Language learning
        String langAbbrev = "Language";
        String langTitle = "Language Learning";
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, langAbbrev, langTitle, "Specific academic themes or areas of focus",
                "academic_focus", new String[]{"foreign_lang"} ) );
        /* The following is not needed, but show all immersion_language
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, langAbbrev, langTitle, "Bi-lingual or language immersion programs offered",
                "immersion", new String[]{"yes"} ) );
                */
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, langAbbrev, langTitle, "Bi-lingual or language immersion programs offered",
                "immersion_language"));
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, langAbbrev, langTitle, "Foreign languages taught",
                "foreign_language"));
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("foreign_language_other");
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, langAbbrev, langTitle, "ESL/ELL levels offered",
                "ell_level", new String[]{"basic", "moderate", "intensive"}));
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, langAbbrev, langTitle, "ESL/ELL programming offered",
                "ell_languages" ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, langAbbrev, langTitle, "Staff resources available to students",
                "staff_resources", new String[]{"ell_esl_coord", "speech_therapist"} ) );
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, langAbbrev, langTitle, "Languages spoken by staff",
                "staff_languages"));
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("staff_languages_other");
        getLastDisplayBean( DISPLAY_CONFIG ).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, langAbbrev, langTitle, "Clubs",
                "student_clubs", new String[]{"language_club"} ) );
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("student_clubs_language");

        // Health & Athletics
        String healthAbbrev = "Health";
        String healthTitle = "Health &amp; Athletics";
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, healthAbbrev, healthTitle, "Instructional and/or curriculum models used",
                "instructional_model", new String[]{"therapeutic"} ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, healthAbbrev, healthTitle, "Specific academic themes or areas of focus",
                "academic_focus", new String[]{"medical"} ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, healthAbbrev, healthTitle, "Staff resources available to students",
                "staff_resources", new String[]{"cooking_teacher", "dance_teacher", "garden_teacher", "instructional_aid", "pe_instructor", "nurse", "school_psychologist"} ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, healthAbbrev, healthTitle, "School facilities",
                "facilities", new String[]{"farm", "sports_fields", "garden", "gym", "kitchen", "multi_purpose", "swimming"} ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, healthAbbrev, healthTitle, "Clubs",
                "student_clubs", new String[]{"farm", "sports_fields", "garden", "gym", "kitchen", "multi_purpose", "swimming", "sadd"} ) );

        // Gifted & Talented
        String giftedAbbrev = "Gifted";
        String giftedTitle = "Gifted &amp; Talented";
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, giftedAbbrev, giftedTitle, "Instructional and/or curriculum models used",
                "instructional_model", new String[]{"accelerated_credit", "AP_courses", "gifted", "honors", "ib"} ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, giftedAbbrev, giftedTitle, "Extra learning resources offered",
                "extra_learning_resources", new String[]{"acceleration"} ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, giftedAbbrev, giftedTitle, "Staff resources available to students",
                "staff_resources", new String[]{"gifted_specialist"} ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, giftedAbbrev, giftedTitle, "College preparation / awareness resources offered",
                "college_prep" ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, giftedAbbrev, giftedTitle, "Clubs",
                "student_clubs", new String[]{"debate", "", "forensics", "its_academic", "nhs"} ) );
    }

    private static void buildApplicationInfoDisplayStructure() {
        String tabAbbrev = "application_info";
        // Special Education section
        String sectionAbbrev = "AppEnroll";
        String sectionTitle = "Application and Enrollment";
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Does this school have an application or enrollment process?",
                "application_process", new String[]{"yes"}) );
        // The following 2 entries will retrieve the data for the following row but the data will be manipulated in
        // applyUniqueDataRules()
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Learn more about this school's application process",
                "admissions_contact_school", new String[]{"yes"}));
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("admissions_url");
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Application deadline",
                "application_deadline"));
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("application_deadline_date");
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Number of applications received for the YEAR-YEAR school year",
                "applications_received"));
        getLastDisplayBean( DISPLAY_CONFIG ).addRowTitleSubstitution("YEAR-YEAR", "applications_received_year");
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Number of students accepted for the YEAR-YEAR school year",
                "students_accepted"));
        getLastDisplayBean( DISPLAY_CONFIG ).addRowTitleSubstitution("YEAR-YEAR", "students_accepted_year");
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Students typically attend these schools prior to attending this school",
                "feeder_school_1"));
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("feeder_school_2");
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("feeder_school_3");
    }

    private static void buildProgResDisplayStructure() {
        String tabAbbrev = "programs_resources";
        // Basics section
        String sectionAbbrev = "Basics";
        String sectionTitle = "School Basics";
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "School start time",
                "start_time" ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "School end time",
                "end_time" ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "School Leader's name",
                "administrator_name" ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Best ways for parents to contact the school",
                "contact_method", new String[]{"email", "phone", "other_contact"} ) );
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Before school or after school care / program onsite",
                "before_after_care", new String[]{"after", "before"}));
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("before_after_care_start");
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("before_after_care_end");
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Age at which early childhood or Pre-K program begins",
                "age_pk_start"));
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Transportation provided for students by the school / district",
                "transportation" ) );
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("transportation_other");
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "School-run shuttle from nearby metro and bus stops",
                "transportation_shuttle"));
        getLastDisplayBean( DISPLAY_CONFIG ).addSupportInfo("transportation_shuttle_other");
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Special schedule",
                "schedule" ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Is there an application process?",
                "application_process" ) );

        // Programs section
        sectionAbbrev = "Programs";
        sectionTitle = "Programs";
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Instructional and/or curriculum models used",
                "instructional_model" ) );
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("instructional_model_other");
        getLastDisplayBean( DISPLAY_CONFIG ).setShowNone(SchoolProfileDisplayBean.NoneHandling.SHOW_IF_ONLY_VALUE);
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Specific academic themes or areas of focus",
                "academic_focus"));
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Bi-lingual or language immersion programs offered",
                "immersion"));
        getLastDisplayBean( DISPLAY_CONFIG ).addSupportInfo("immersion_language");
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Level of special education programming offered",
                "spec_ed_level"));
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Specialized programs for specific types of special education students",
                "special_ed_programs"));
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Foreign languages taught",
                "foreign_language"));
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("foreign_language_other");
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Level of ESL/ELL programming offered",
                "ell_level"));
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Vocational or skills-based training offered",
                "skills_training"));
        getLastDisplayBean( DISPLAY_CONFIG ).addSupportInfo("skills_training_other");
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Advanced Placement (AP) exams offered",
                "advanced_placement_exams" ) ); // This key is a placeholder because the data does not come from the ESP table.  The data is filled in in applyUniqueDataRules

        // Resources section
        sectionAbbrev = "Resources";
        sectionTitle = "Resources";
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Staff resources available to students",
                "staff_resources" ) );
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Languages spoken by staff",
                "staff_languages" ) );
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("staff_languages_other");
        getLastDisplayBean( DISPLAY_CONFIG ).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Extra learning resources offered",
                "extra_learning_resources"));
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("extra_learning_resources_other");
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Partnerships with local resources and organizations",
                "partnerships"));     // This key is just a placeholder
        getLastDisplayBean( DISPLAY_CONFIG ).addUrl("partnerships_name_1", "partnerships_url_1");
        getLastDisplayBean( DISPLAY_CONFIG ).addUrl("partnerships_name_2", "partnerships_url_2");
        getLastDisplayBean( DISPLAY_CONFIG ).addUrl("partnerships_name_3", "partnerships_url_3");
        getLastDisplayBean( DISPLAY_CONFIG ).addUrl("partnerships_name_4", "partnerships_url_4");
        getLastDisplayBean( DISPLAY_CONFIG ).addUrl("partnerships_name_5", "partnerships_url_5");
        getLastDisplayBean( DISPLAY_CONFIG ).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.URL);
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "School facilities",
                "facilities"));
        getLastDisplayBean( DISPLAY_CONFIG ).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "College preparation / awareness resources offered",
                "college_prep" ) );
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("college_prep_other");
    }

    private static void buildExtrasDisplayStructure() {
        String tabAbbrev = "extracurriculars";
        // Sports section
        String sectionAbbrev = "Sports";
        String sectionTitle = "Sports";
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Boys sports",
                "boys_sports" ) );
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("boys_sports_other");
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Girls sports",
                "girls_sports" ) );
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("girls_sports_other");

        // Arts and Music section
        sectionAbbrev = "Arts";
        sectionTitle = "Arts and Music";
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Visual arts",
                "arts_visual" ) );
        getLastDisplayBean( DISPLAY_CONFIG ).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Music",
                "arts_music"));
        getLastDisplayBean( DISPLAY_CONFIG ).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Performing arts",
                "arts_performing_written"));
        getLastDisplayBean( DISPLAY_CONFIG ).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
        DISPLAY_CONFIG.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Media arts",
                "arts_media"));
        getLastDisplayBean( DISPLAY_CONFIG ).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);

        // Arts and Music section
        sectionAbbrev = "Clubs";
        sectionTitle = "Student clubs";
        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Clubs (distinct from courses)",
                "student_clubs" ) );
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("student_clubs_dance");
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("student_clubs_language");
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("student_clubs_other_1");
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("student_clubs_other_2");
        getLastDisplayBean( DISPLAY_CONFIG ).addKey("student_clubs_other_3");
        getLastDisplayBean( DISPLAY_CONFIG ).setShowNone(SchoolProfileDisplayBean.NoneHandling.SHOW_IF_ONLY_VALUE);
        getLastDisplayBean( DISPLAY_CONFIG ).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
    }


}