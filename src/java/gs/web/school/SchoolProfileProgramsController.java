package gs.web.school;

import gs.data.school.*;
import gs.data.school.census.CensusDataType;
import gs.data.school.census.SchoolCensusValue;
import gs.web.school.usp.EspResponseData;
import gs.web.school.usp.EspStatusManager;
import gs.web.search.ICmsFeatureSearchResult;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The request mapping does not have a trailing slash. This handles the url with and without a trailing slash.
 */
@Controller
@RequestMapping("/school/profilePrograms.page")
public class SchoolProfileProgramsController extends AbstractSchoolProfileController implements BeanFactoryAware {
    private static final Log _log = LogFactory.getLog(SchoolProfileProgramsController.class);
    private BeanFactory _beanFactory;
    public static final String VIEW = "school/profilePrograms";

    // The following is a prefix for the Model items specific to this page
    //* private static final String [] MODEL_PREFIXES = {"highlights", "application_info", "programs_resources", "extracurriculars"};
    private static final String [] MODEL_PREFIXES = {"highlights", "programs_resources", "extracurriculars"};


    public static final String PARAM_STATE = "state";
    public static final String PARAM_SCHOOL_ID = "schoolId";

    public static final String AF_PROGRAM_TYPE = "After school";
    public static final String AF_PROGRAM_NAME_KEY_PREFIX = "after_school_name_";
    public static final String AF_PROGRAM_GRADES_KEY_PREFIX = "after_school_grade_";
    public static final String AF_PROGRAM_WEBSITE_KEY_PREFIX = "after_school_website_";
    public static final String AF_PROGRAM_DAYS_KEY_PREFIX = "after_school_day_";
    public static final String AF_PROGRAM_TEMP_TITLE_PREFIX = "After school program ";
    public static final String AF_PROGRAM_ACTIVITIES_KEY_PREFIX = "after_school_activities_";
    public static final String AF_PROGRAM_FEE_LEARN_MORE_PREFIX = "after_school_fee_learn_more_";

    public static final String SUMMER_PROGRAM_TYPE = "Summer";
    public static final String SUMMER_PROGRAM_NAME_KEY_PREFIX = "summer_program_name_";
    public static final String SUMMER_PROGRAM_GRADES_KEY_PREFIX = "summer_program_grade_";
    public static final String SUMMER_PROGRAM_WEBSITE_KEY_PREFIX = "summer_program_website_";
    public static final String SUMMER_PROGRAM_DAYS_KEY_PREFIX = "summer_program_day_";
    public static final String SUMMER_PROGRAM_TEMP_TITLE_PREFIX = "Summer program ";
    public static final String SUMMER_PROGRAM_DATES_KEY_PREFIX = "summer_program_date_";
    public static final String SUMMER_PROGRAM_ACTIVITIES_KEY_PREFIX = "summer_program_activities_";
    public static final String SUMMER_PROGRAM_CARE_KEY_PREFIX = "summer_program_before_after_care_";
    public static final String SUMMER_PROGRAM_FEE_LEARN_MORE_PREFIX = "summer_program_fee_learn_more_";

    public static final String HIGHLIGHTS_AWARDS_ABBREV = "Awards";
    public static final String HIGHLIGHTS_SPECIAL_ED_ABBREV = "SpecEd";
    public static final String HIGHLIGHTS_STEM_ABBREV = "STEM";
    public static final String HIGHLIGHTS_ARTS_ABBREV = "Arts";
    public static final String HIGHLIGHTS_LANG_ABBREV = "Language";
    public static final String HIGHLIGHTS_HEALTH_ABBREV = "Health";
    public static final String HIGHLIGHTS_GIFTED_ABBREV = "Gifted";
    public static final String PROGRAMS_BASICS_ABBREV = "Basics";
    public static final String PROGRAMS_PROG_ABBREV = "Programs";
    public static final String RESOURCES_ABBREV = "Resources";
    public static final String EXTRA_SPORTS_ABBREV = "Sports";
    public static final String EXTRA_ARTS_ABBREV = "Arts";
    public static final String EXTRA_CLUBS_ABBREV = "Clubs";

    //comparing only the abbrev title. after school and summer program will never have this abbrev
    public static final Set<String> staticStructureKeys = new HashSet<String>() {{
        add(HIGHLIGHTS_AWARDS_ABBREV);
        add(HIGHLIGHTS_SPECIAL_ED_ABBREV);
        add(HIGHLIGHTS_STEM_ABBREV);
        add(HIGHLIGHTS_ARTS_ABBREV);
        add(HIGHLIGHTS_LANG_ABBREV);
        add(HIGHLIGHTS_HEALTH_ABBREV);
        add(HIGHLIGHTS_GIFTED_ABBREV);
        add(PROGRAMS_BASICS_ABBREV);
        add(PROGRAMS_PROG_ABBREV);
        add(RESOURCES_ABBREV);
        add(EXTRA_SPORTS_ABBREV);
        add(EXTRA_ARTS_ABBREV);
        add(EXTRA_CLUBS_ABBREV);
    }};

    /*
         * LinkedHashMap is used to preserve the order how the rows will be displayed. Key is the osp response key prefix,
         * value is the row title.
         * Some keys require special processing -
         * days offered response should not be sorted at the end - to maintain Monday-Sunday order
         * grade response is converted to range string
         * website as link
         * dates offered converted to date range string
         * activities have an additional other text field
         */
    public final Map<String, String> afterSchoolKeysPrefix = new LinkedHashMap<String, String>() {{
        put("after_school_description_", "About the program");
        put(AF_PROGRAM_ACTIVITIES_KEY_PREFIX, "Types of activities");
        put("after_school_operator_", "Operated by");
        put(AF_PROGRAM_DAYS_KEY_PREFIX, "Days offered");
        put("after_school_start_", "Start time");
        put("after_school_end_", "End time");
        put("after_school_attendance_restriction_", "Is enrollment limited to students who attend the school?");
        put(AF_PROGRAM_GRADES_KEY_PREFIX, "What grades can participate?");
        put("after_school_fee_", "Is there a fee for the program?");
        put("after_school_financial_aid_", "Is financial aid available?");
        put(AF_PROGRAM_FEE_LEARN_MORE_PREFIX, "How can parents find out more about fees?");
        put(AF_PROGRAM_WEBSITE_KEY_PREFIX, "Website");
        put("after_school_phone_", "Phone number");
    }};

    public final Map<String, String> summerProgramKeysPrefix = new LinkedHashMap<String, String>() {{
        put("summer_program_description_", "About the program");
        put(SUMMER_PROGRAM_ACTIVITIES_KEY_PREFIX, "Types of activities");
        put("summer_program_operator_", "Operated by");
        put(SUMMER_PROGRAM_DATES_KEY_PREFIX, "Dates offered");
        put(SUMMER_PROGRAM_DAYS_KEY_PREFIX, "Days offered");
        put("summer_program_start_", "Start time");
        put("summer_program_end_", "End time");
        put("summer_program_attendance_restriction_", "Is enrollment limited to students who attend the school?");
        put(SUMMER_PROGRAM_GRADES_KEY_PREFIX, "What grades can participate?");
        put("summer_program_fee_", "Is there a fee for the program?");
        put("summer_program_financial_aid_", "Is financial aid available?");
        put(SUMMER_PROGRAM_FEE_LEARN_MORE_PREFIX, "How can parents find out more about fees?");
        put(SUMMER_PROGRAM_CARE_KEY_PREFIX, "Before / after care");
        put(SUMMER_PROGRAM_WEBSITE_KEY_PREFIX, "Website");
        put("summer_program_phone_", "Phone number");
    }};

//    @Autowired
//    private IEspResponseDao _espResponseDao;

    @Autowired
    private SchoolProfileDataHelper _schoolProfileDataHelper;

    @Autowired
    private SchoolProfileCultureController _schoolProfileCultureController;

    @RequestMapping(method = RequestMethod.GET)
    public String showHighlightsPage(ModelMap modelMap, HttpServletRequest request) {
        // The following drives creation of the display
        List<SchoolProfileDisplayBean> displayConfig = new ArrayList<SchoolProfileDisplayBean>();
        buildHighlightsDisplayStructure(displayConfig);
        buildProgResDisplayStructure(displayConfig);
        buildExtrasDisplayStructure(displayConfig);

        // The following structure contains the key_value's to return from the database
        // this information is available in DISPLAY_CONFIG structure created above
        Set<String> _keyValuesToExtract = new HashSet<String>();
        for( SchoolProfileDisplayBean bean : displayConfig) {
            _keyValuesToExtract.addAll(bean.getEspResponseKeys());
        }

        // Get School
        School school = getSchool(request);
        modelMap.put("school", school);

        // Add the display structure to the model for use in building the page structure
        modelMap.put( "ProfileDisplayStructure", displayConfig);

        //Add culture info to the model
        _schoolProfileCultureController.getCultureDetails(modelMap,request);

        // Get Data
        if (school != null) {
            Map<String, List<EspResponse>> espResults = _schoolProfileDataHelper.getEspDataForSchool( request );
            EspResponseData espResponseData = getEspResponseData(espResults);
            EspStatusManager statusManager = (EspStatusManager) _beanFactory.getBean(
                EspStatusManager.BEAN_NAME,
                school,
                espResponseData
            );
            modelMap.put("espStatus", statusManager.getEspStatus());

            boolean hasEspData = (espResults != null && espResults.size() > 0) ? true : false;
            //  There is some data in the school table that can be used to enhance EspResults
            espResults = enhanceEspResultsFromSchool( school, espResults );

            if( espResults != null && !espResults.isEmpty() ) {
                for(int i = 1; i < 6; i++) {
                    List<EspResponse> responses = espResults.get(AF_PROGRAM_NAME_KEY_PREFIX + i);
                    EspResponse response = responses != null ? responses.get(0) : null;
                    String title = response != null && response.getValue() != null ? response.getValue() + " (after school program)" : AF_PROGRAM_TEMP_TITLE_PREFIX + i;
                    displayConfig.addAll(buildAfterSchoolSummerPrograms(AF_PROGRAM_TYPE, "extracurriculars", title, afterSchoolKeysPrefix.keySet(), i));
                }
                for(int i = 1; i < 6; i++) {
                    List<EspResponse> responses = espResults.get(SUMMER_PROGRAM_NAME_KEY_PREFIX + i);
                    EspResponse response = responses != null ? responses.get(0) : null;
                    String title = response != null && response.getValue() != null ? response.getValue() + " (summer program)" : SUMMER_PROGRAM_TEMP_TITLE_PREFIX + i;
                    displayConfig.addAll(buildAfterSchoolSummerPrograms(SUMMER_PROGRAM_TYPE, "extracurriculars", title, summerProgramKeysPrefix.keySet(), i));
                }

                // The following builds the display data based on the DB results and the display requirements.
                // The key is the ModelKey and the value is a list of the values for that key
                Map<String, List<String>> resultsModel = buildDisplayData( espResults, displayConfig);

                Map<String, List<String>> supportResultsModel = buildSupplementalDisplayData(espResults, displayConfig);
                resultsModel.putAll( supportResultsModel );

                // Sort the results
                sortResults( resultsModel, displayConfig );
                // Perform unique data manipulation rules
                applyUniqueDataRules( request, school, resultsModel, displayConfig, espResults, hasEspData );

                // Put the data into the model so it will be passed to the jspx page
                modelMap.put( "ProfileData", resultsModel );

                // One last thing...  Build the model for the display.
                // Each section becomes a separate table on the display and therefore needs to be in the model as such.
                // And, if a section is empty it should not be displayed.  The following function takes care of those requirements.
                buildDisplayModel(MODEL_PREFIXES, resultsModel, displayConfig, modelMap );

                // Build the CMS links for the rows that have a CMS article that provides an explanation of the row
                Map<String, String> articleIdToLinkMap = buildRowTitleCmsMap( request, displayConfig, _schoolProfileDataHelper );
                modelMap.put( "ArticleIdToLinkMap", articleIdToLinkMap);
            }

        }

        return VIEW;
    }

    protected EspResponseData getEspResponseData(Map<String, List<EspResponse>> responsesByKey) {
        return new EspResponseData(responsesByKey);
    }

    /**
     * Enhance EspResponse from the school table but only for cases where espResponse does not already contain that data
     * @param school
     * @param espResults
     */

    private Map<String, List<EspResponse>> enhanceEspResultsFromSchool(School school, Map<String, List<EspResponse>> espResults) {

        // if there is no espData create the structure
        if( espResults == null ) {
            espResults = new HashMap<String, List<EspResponse>>();
        }

        // This item isn't really about adding school data but about cleaning up one espResults item...
        // key academic_focus can have values special_ed and _special_education that are the same.
        // they should be merged into special_education
        boolean specEd = espDataContains( espResults, "academic_focus", "special_ed");
        boolean specEducation = espDataContains( espResults, "academic_focus", "special_eduction" );
        if( !specEducation && specEd ) {
            // need to add "special_education"
            List<EspResponse> academicFocus = espResults.get( "academic_focus" );
            EspResponse r = createEspResponse( "academic_focus", "special_eduction" );
            academicFocus.add(r);
        }


        // affiliation which comes from the school table.
        // there are 3 possible places that we can get affiliation.  Look in the following places and stop when the first is found
        // 1. espData key='school_type_affiliation_other'
        // 2. school affiliation
        // 3. espData key='school_type_affiliation'
        String schoolAffiliation = school.getAffiliation();
        List<EspResponse> espAffiliation = espResults.get("school_type_affiliation");
        List<EspResponse> espAffiliationOther = espResults.get("school_type_affiliation_other");
        if( espAffiliationOther != null ) {
            // 1st choice data is available but it needs to be moved to espData key='school_type_affiliation'
            espResults.put( "school_type_affiliation", espAffiliationOther );
        }
        else if( schoolAffiliation != null && schoolAffiliation.length() > 0 ) {
            // 2nd choice is available, need to convert to EspResponse
            List<EspResponse> e = new ArrayList<EspResponse>(1);
            e.add( createEspResponse( "school_type_affiliation",  schoolAffiliation ) );
            espResults.put( "school_type_affiliation", e );
        }
        // This covers the alternatives.  No need to check #3 because it is already stored with the correct key

        // association which comes from the school table
        String associationCommaSep = school.getAssociation();
        if( associationCommaSep!=null && associationCommaSep.length()>0 ) {
            String [] associations = associationCommaSep.split(", ");
            List<EspResponse> associationList = new ArrayList<EspResponse>(associations.length);
            for( String v : associations ) {
                EspResponse r = new EspResponse();
                r.setKey("school.association");
                String trimmed = v.trim();
                r.setValue(trimmed);
                r.setPrettyValue(trimmed);
                r.setActive(false); // This can be used as a flag that this is enhanced data
                associationList.add( r );
            }
            espResults.put( "school.association", associationList );
        }

        // Sometimes there is data in the school table that can be used to enhance the display results.  Those cases are handled below
        enhanceEspResultValue( espResults, "academic_focus", "special_education", school, "special_education_program" );
        enhanceEspResultValue( espResults, "academic_focus", "special_education", school, "special_education" );
        enhanceEspResultValue( espResults, "academic_focus", "vocational", school, "vocational_technical" );
        enhanceEspResultValue( espResults, "academic_focus", "religious", school, "religious" );
        enhanceEspResultValue( espResults, "instructional_model", "adult_ed", school, "adult_education");
        enhanceEspResultValue( espResults, "instructional_model", "waldorf", school, "Waldorf");
        enhanceEspResultValue( espResults, "instructional_model", "independent_study", school, "independent_study");
        enhanceEspResultValue( espResults, "instructional_model", "virtual", school, "virtual_online");
        enhanceEspResultValue( espResults, "instructional_model", "continuation", school, "continuation");
        enhanceEspResultValue( espResults, "instructional_model", "core_knowledge", school, "Core_Knowledge");
        enhanceEspResultValue( espResults, "instructional_model", "gifted", school, "gifted_talented");
        enhanceEspResultValue( espResults, "instructional_model", "montessori", school, "Montessori");
        enhanceEspResultValue( espResults, "boarding", "boarding", school, "boarding" );
        enhanceEspResultValue( espResults, "boarding", "day/boarding", school, "boarding_and_day" );
        enhanceEspResultKey( espResults, "coed", "all_girls", school, "all_female" );
        enhanceEspResultKey( espResults, "coed", "all_boys", school, "all_male" );
        enhanceEspResultKey( espResults, "coed", "coed", school, "coed" );
        enhanceEspResultValue( espResults, "schedule", "yearround", school, "year_round" );

        return espResults;
    }

    // Build the display data by merging the display structure and DB results data
    public static Map<String, List<String>> buildDisplayData(Map<String, List<EspResponse>> resultsMap, List<SchoolProfileDisplayBean> displayConfig) {

        // Create a Map to store the data in
        Map<String, List<String>> resultsModel = new HashMap<String, List<String>>();

        // Build by row by iterating over the display bean
        for( SchoolProfileDisplayBean display : displayConfig ) {

            List<String> rowData = buildDisplayDataForRow(resultsMap, display);
            String key = display.getModelKey();
            resultsModel.put( key, rowData );
        }

        return resultsModel;
    }

    private static List<String> buildDisplayDataForRow(Map<String, List<EspResponse>> espResponseMap, SchoolProfileDisplayBean bean) {

        List<String> results = new ArrayList<String>();
        Map<String, String> dateRange = new HashMap<String, String>();

        final String programStartKey = "start";
        final String programEndKey = "end";
        // Process all keys for this bean
        String [] keys = bean.getEspResponseKeys().toArray(new String[0]);
        for( String key : keys ) {
            // Get results list for this key
            List<EspResponse> espResponses = espResponseMap.get( key );

            // If there are some results then select the allowed values
            if( espResponses != null && espResponses.size() > 0 ) {
                if(key.startsWith(AF_PROGRAM_GRADES_KEY_PREFIX) || key.startsWith(SUMMER_PROGRAM_GRADES_KEY_PREFIX)) {
                    results.add(getGradeRangeString(espResponses));
                    return results;
                }

                if(key.startsWith(SUMMER_PROGRAM_DATES_KEY_PREFIX)) {
                    if((key.contains(programStartKey))) {
                        dateRange.put(programStartKey, changeDateFormat(espResponses));
                    }
                    else if((key.contains(programEndKey))) {
                        dateRange.put(programEndKey, changeDateFormat(espResponses));
                    }
                    continue;
                }

                // Loop through the responses and gather the ones in the allowed list
                for( EspResponse espResponse : espResponses ) {
                    // do not add "other" response value. add only the value for _other key.
                    if((key.startsWith(AF_PROGRAM_ACTIVITIES_KEY_PREFIX) ||
                            key.startsWith(SUMMER_PROGRAM_ACTIVITIES_KEY_PREFIX)) &&
                                    "other".equalsIgnoreCase(espResponse.getPrettyValue())) {
                        continue;
                    }

                    // format response for fee learn more and care time
                    String value = null;

                    if (key.startsWith(SUMMER_PROGRAM_FEE_LEARN_MORE_PREFIX) || key.startsWith(AF_PROGRAM_FEE_LEARN_MORE_PREFIX)) {
                        value = espResponse.getValue();
                        if(value != null && "website".equalsIgnoreCase(value)) {
                            results.add("Visit " + value);
                            continue;
                        }
                    }

                    if (key.startsWith(SUMMER_PROGRAM_CARE_KEY_PREFIX + "start")) {
                        value = espResponse.getPrettyValue();
                        if(value != null) {
                            results.add("Before: starts " + value);
                        }
                        continue;
                    }
                    else if (key.startsWith(SUMMER_PROGRAM_CARE_KEY_PREFIX + "end")) {
                        value = espResponse.getPrettyValue();
                        if(value != null) {
                            results.add("After: ends " + value);
                        }
                        continue;
                    }

                    if( bean.getAllowedResponseValues() != null && key.equals(bean.getEspResponseKeyWithAllowed())){
                        // If there is a set of permitted values then check to see if this value is allowed
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

        if(dateRange.size() > 0) {
            results.add(dateRange.get(programStartKey) + " - " + dateRange.get(programEndKey));
            dateRange.clear();
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


   // Some rows need special manipulation for the display.  Those rules are applied here
    private void applyUniqueDataRules(HttpServletRequest request, School school, Map<String, List<String>> resultsModel, List<SchoolProfileDisplayBean> displayConfig, Map<String, List<EspResponse>> espResults, boolean hasEspData) {

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

        // For the Programs & Resources tab, Resources, Shuttle Info we may need to combine 2 results.
        // If ModelKey: "program_resources/Resources/transportation_shuttle" contains "yes" then show "Yes"
        // And, if there is a value in program_resources/Resources/transportation_shuttle_other add:
        // "Nearby lines: " and the value
        List<String> shuttle_list = resultsModel.get( "programs_resources/Resources/transportation_shuttle" );
        if( shuttle_list != null && shuttle_list.size() > 0 ) {
            String result = null;
            String shuttle = shuttle_list.get(0);
            if( shuttle.equalsIgnoreCase("yes")) {
                // Now see if there the additional data
                List<String> nearby_list = resultsModel.get( "programs_resources/Resources/transportation_shuttle_other" );
                if( nearby_list != null && nearby_list.size() > 0 ) {
                    result = "Yes. Nearby lines: " + nearby_list.get(0);
                }
                else {
                    result = "Yes.";
                }
                List<String> results = new ArrayList<String>(1);
                results.add( result );
                resultsModel.put("programs_resources/Resources/transportation_shuttle", results);
            }
            else {
                // Since answer must be no, remove existing result so the line wil not show
                resultsModel.remove( "program_resources/Resources/transportation_shuttle" );
            }
        }

        // Special handling for immersion, immersion_language and immersion_language_other
        // immersion is a Yes/No field.
        // If there are no immersion_language's then:
        //   if yes, show Yes.
        //   if no, don't show the line
        // Otherwise don't show Yes and only show immersion_language
        List<String> immersion = resultsModel.get( "highlights/Language/immersion" );
        if( immersion != null && immersion.contains("Yes") && immersion.size() >= 2 ) {
            // Need to remove Yes
            immersion.remove("Yes");
            // Also, model key "programs_resources/Programs/immersion" should have the same data
            resultsModel.put( "programs_resources/Programs/immersion", immersion );
        }

        // Special handling for programs_resources/Programs/advanced_placement_exams
        // rraker - 8/7/12 - This data comes from the census data.  At this time it has been decided not to
        // include AP exams.  Therefore the following code is currently commented out
        // List<String> l = Arrays.asList( new String[] {"Bob to ask Anthony where to get the data"} );
        // resultsModel.put( "programs_resources/Programs/advanced_placement_exams", l );

        // Apply the none handling (which is in a separate method)
        applyNoneHandlingRule(resultsModel, displayConfig);

        // School administrator comes from the Census Data, not the ESP data.
        SchoolCensusValue administratorSCV = _schoolProfileDataHelper.getSchoolCensusValue( request, CensusDataType.HEAD_OFFICIAL_NAME );
        if( administratorSCV != null ) {
            List<String> administratorList = new ArrayList<String>(1);
            administratorList.add(administratorSCV.getValueText());
            resultsModel.put( "programs_resources/Basics/administrator_name", administratorList );
        }

        if (school != null && !("".equals(school.getFax()))) {
            List<String> faxList = new ArrayList<String>(1);
            faxList.add(school.getFax());
            resultsModel.put( "programs_resources/Basics/school_fax_office_code", faxList );
        }

        // Blue ribbon school data has been removed from the project scope for now.
        // GSData classes (BlueRibbonSchoolDaoHibernate) have been started to get this data but they need some cleaning up.
//        // This is just debug data for testing blue_ribbon_schools
//        String year = "2011";       // This would be from the blue_ribbon_schools table
//        String result = "This school was selected as a Blue Ribbon School in " + year + " by the US Department of Education.";
//        List<String> resultsList = new ArrayList<String>(1);
//        resultsList.add(result);
//        resultsModel.put( "highlights/Awards/blue_ribbon_schools_text", resultsList );
//        List<String> blueRibbonMoreInfo = new ArrayList<String>(1);
//        blueRibbonMoreInfo.add("More about the Blue Ribbon Schools Program");
//        resultsModel.put( "highlights/Awards/blue_ribbon_schools_more_info", blueRibbonMoreInfo );
//        List<String> blueRibbonMoreInfoUrl = new ArrayList<String>(1);
//        blueRibbonMoreInfoUrl.add("/definitions/natl/blueribbon.html");
//        resultsModel.put( "highlights/Awards/blue_ribbon_schools_more_info_url", blueRibbonMoreInfoUrl );

        // academic awards and service awards have to be coerced into "award (year)" format
        List<String> academicsList = new ArrayList<String>(3);
        String academic1 = formatValueAndYear(espResults, "academic_award_1", "academic_award_1_year");
        if( academic1 != null ) {
            academicsList.add( academic1 );
        }
        String academic2 = formatValueAndYear(espResults, "academic_award_2", "academic_award_2_year");
        if( academic2 != null ) {
            academicsList.add( academic2 );
        }
        String academic3 = formatValueAndYear(espResults, "academic_award_3", "academic_award_3_year");
        if( academic3 != null ) {
            academicsList.add( academic3 );
        }
        resultsModel.put("highlights/Awards/academic_award", academicsList);

        List<String> servicesList = new ArrayList<String>(3);
        String service1 = formatValueAndYear(espResults, "service_award_1", "service_award_1_year");
        if( service1 != null ) {
            servicesList.add( service1 );
        }
        String service2 = formatValueAndYear(espResults, "service_award_2", "service_award_2_year");
        if( service2 != null ) {
            servicesList.add( service2 );
        }
        String service3 = formatValueAndYear(espResults, "service_award_3", "service_award_3_year");
        if( service3 != null ) {
            servicesList.add( service3 );
        }
        resultsModel.put("highlights/Awards/service_award", servicesList);

        // format before and after care into: Before school[: starts at xx:xx] and After school[: ends at xx:xx]
        List<EspResponse> beforeAfter = espResults.get("before_after_care");
        List<String> beforeAfterResults = new ArrayList<String>(2);
        if( beforeAfter != null && beforeAfter.size() > 0 ) {
            for( EspResponse e:beforeAfter ) {
                if( e.getSafeValue().equalsIgnoreCase("before") ) {
                    // Have before now check for time
                    List<EspResponse> start = espResults.get("before_after_care_start");
                    if( start != null && start.size() > 0 ) {
                        beforeAfterResults.add( "Before school: starts at " + start.get(0).getSafeValue() );
                    }
                    else {
                        beforeAfterResults.add( "Before school" );
                    }
                }
                if( e.getSafeValue().equalsIgnoreCase("after") ) {
                    // Have before now check for time
                    List<EspResponse> end = espResults.get("before_after_care_end");
                    if( end != null && end.size() > 0 ) {
                        beforeAfterResults.add( "After school: ends at " + end.get(0).getSafeValue() );
                    }
                    else {
                        beforeAfterResults.add( "After school" );
                    }
                }
            }
        }
        if( beforeAfterResults.size() > 0 ) {

            resultsModel.put("programs_resources/Basics/before_after_care", beforeAfterResults);
        }

        // format Summer program before and after care into: Before[: starts at xx:xx] and After[: ends at xx:xx]
        // Have to do this for each of the 5 possible programs
        for (int i = 1; i <= 5; i++ ) {
            List<EspResponse> summerPgmBeforeAfter = espResults.get("summer_program_before_after_care_" + i);
            List<String> summerPgmBeforeAfterResults = new ArrayList<String>(2);
            if( summerPgmBeforeAfter != null && summerPgmBeforeAfter.size() > 0 ) {
                for( EspResponse e:summerPgmBeforeAfter ) {
                    if( e.getSafeValue().equalsIgnoreCase("before") ) {
                        // Have before now check for time
                        List<EspResponse> start = espResults.get("summer_program_before_after_care_start_" + i);
                        if( start != null && start.size() > 0 ) {
                            summerPgmBeforeAfterResults.add( "Before: starts at " + start.get(0).getSafeValue() );
                        }
                        else {
                            summerPgmBeforeAfterResults.add( "Before" );
                        }
                    }
                    if( e.getSafeValue().equalsIgnoreCase("after") ) {
                        // Have before now check for time
                        List<EspResponse> end = espResults.get("summer_program_before_after_care_end_" + i);
                        if( end != null && end.size() > 0 ) {
                            summerPgmBeforeAfterResults.add( "After: ends at " + end.get(0).getSafeValue() );
                        }
                        else {
                            summerPgmBeforeAfterResults.add( "After" );
                        }
                    }
                }
            }
            if( summerPgmBeforeAfterResults.size() > 0 ) {
                resultsModel.put("extracurriculars/" + SUMMER_PROGRAM_TYPE + i + "/summer_program_before_after_care_" + i, summerPgmBeforeAfterResults);
            }
        }

        // format times.  Some places a time is shown as AM or PM which should be turned into a.m. and p.m.
        if( resultsModel.get("programs_resources/Basics/start_time") != null ) {
            resultsModel.put("programs_resources/Basics/start_time", formatAmPm(resultsModel.get("programs_resources/Basics/start_time")) );
        }
        if( resultsModel.get("programs_resources/Basics/end_time") != null ) {
            resultsModel.put("programs_resources/Basics/end_time", formatAmPm(resultsModel.get("programs_resources/Basics/end_time")));
        }
        if( resultsModel.get("programs_resources/Basics/before_after_care") != null ) {
            resultsModel.put("programs_resources/Basics/before_after_care", formatAmPm(resultsModel.get("programs_resources/Basics/before_after_care")));
        }

        // Special formatting for extracurriculars/Clubs/student_clubs
        String extrasClubsKey = "extracurriculars/Clubs/student_clubs";
        List<String> extrasLanguageClubResults = resultsModel.get( extrasClubsKey );
        if( extrasLanguageClubResults == null ) {
            extrasLanguageClubResults = new ArrayList<String>();
        }
        // If student_clubs_language is present add it to the results and remove Foreign language and culture club
        List<EspResponse> languageClub = espResults.get("student_clubs_language");
        if( languageClub != null && languageClub.size() > 0 ) {
            extrasLanguageClubResults.add( "Foreign language club: " + languageClub.get(0).getSafeValue() );
            extrasLanguageClubResults.remove("Foreign language and culture club");
        }
        // If student_clubs_dance is present add it and remove Dance club
        List<EspResponse> danceClub = espResults.get("student_clubs_dance");
        if( danceClub != null && danceClub.size() > 0 ) {
            extrasLanguageClubResults.add( "Dance club: " + danceClub.get(0).getSafeValue() );
            extrasLanguageClubResults.remove("Dance club");
        }
        if( extrasLanguageClubResults.size() > 0 ) {
            Collections.sort(extrasLanguageClubResults);
            resultsModel.put( "extracurriculars/Clubs/student_clubs", extrasLanguageClubResults );
        }

        // Special formatting for highlights/Language/student_clubs
        String highlightsLangKey = "highlights/Language/student_clubs";
        List<String> highlightsLanguageClubResults = resultsModel.get( highlightsLangKey );
        if( highlightsLanguageClubResults == null ) {
            highlightsLanguageClubResults = new ArrayList<String>();
        }
        // If student_clubs_language is present add it to the results and remove Foreign language and culture club
        // languageClub = espResults.get("student_clubs_language");
        if( languageClub != null && languageClub.size() > 0 ) {
            highlightsLanguageClubResults.add( "Foreign language club: " + languageClub.get(0).getSafeValue() );
            highlightsLanguageClubResults.remove("Foreign language and culture club");
        }
        if( highlightsLanguageClubResults.size() > 0 ) {
            Collections.sort(highlightsLanguageClubResults);
            resultsModel.put( highlightsLangKey, highlightsLanguageClubResults );
        }

        // Special formatting for highlights/Clubs/student_clubs
        String highlightsArtsKey = "highlights/Arts/student_clubs";
        List<String> highlightsArtsClubResults = resultsModel.get( highlightsArtsKey );
        if( highlightsArtsClubResults == null ) {
            highlightsArtsClubResults = new ArrayList<String>();
        }
        // If student_clubs_dance is present add it and remove Dance club
        //List<EspResponse> danceClub = espResults.get("student_clubs_dance");
        if( danceClub != null && danceClub.size() > 0 ) {
            highlightsArtsClubResults.add( "Dance club: " + danceClub.get(0).getSafeValue() );
            highlightsArtsClubResults.remove("Dance club");
        }
        if( highlightsArtsClubResults.size() > 0 ) {
            Collections.sort(highlightsArtsClubResults);
            resultsModel.put( highlightsArtsKey, highlightsArtsClubResults );
        }

        // If there is no ESP Data from the esp_response table then remove anything that would show on the Highlights tab.
        // Note: there still might be data in the espResults that was added from the school object
        if( !hasEspData ) {
            List<String> keysToRemove = new ArrayList<String>();
            for( String key : resultsModel.keySet() ) {
                if( key.startsWith("highlights/") ) {
                    keysToRemove.add( key );
                }
            }
            for( String key : keysToRemove ) {
                resultsModel.remove( key );
            }
        }
    }

    static void applyNoneHandlingRule(Map<String, List<String>> resultsModel, List<SchoolProfileDisplayBean> displayConfig) {
        // Handle special rules related to displaying or suppressing the display of the "none" result
        // This has to be done by looking at each display element and seeing if special processing is required
        for( SchoolProfileDisplayBean display : displayConfig ) {
            SchoolProfileDisplayBean.NoneHandling noneHandling = display.getShowNone();
            if( noneHandling == SchoolProfileDisplayBean.NoneHandling.NO_NONE_PROCESSING) {
                // This means do not perform any special none processing.
                // This is the the default behaviour
            }
            else {
                // The following cases will require more data
                List<String> modelData = resultsModel.get( display.getModelKey() );
                if( modelData != null && modelData.size() > 0) {
                    if( noneHandling == SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS) {
                        // Remove none independent of the number of values
                        for( int i = 0; i < modelData.size(); i++ ) {
                            if( modelData.get(i).equalsIgnoreCase("none") ) {
                                modelData.remove(i);
                            }
                        }
                    }
                    else if( noneHandling == SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_IF_NOT_ONLY_VALUE &&
                            modelData.size() > 1 ) {
                        // Remove none if there are multiple values in the list
                        for( int i = 0; i < modelData.size(); i++ ) {
                            if( modelData.get(i).equalsIgnoreCase("none") ) {
                                modelData.remove(i);
                            }
                        }
                    }
                }
            }   // if
        }  // for
    }

    /**
     * Enhances a specific espData value with school.subtype data.
     * Specific rule is that if the school contains the specified school_subtype and if the espResponseValue is not present espData then add the espResponseValue
     * @param espData
     * @param espResponseKey Key in results model that my need to be enhanced
     * @param espResponseValue Database value to match
     * @param school
     * @param school_subtype
     */
    private void enhanceEspResultValue(Map<String,List<EspResponse>> espData, String espResponseKey, String espResponseValue, School school, String school_subtype) {
        if( espData==null || espResponseKey==null || espResponseValue== null || school==null || school_subtype==null ) {
            return;
        }

        // See if the school.subtype contains the specified subtype
        SchoolSubtype subtype = school.getSubtype();
        if( subtype.contains(school_subtype) ) {
            // Have matching subtype, see if it needs to be added to the results
            List<EspResponse> existingValues = espData.get(espResponseKey);
            if( existingValues != null ) {
                // Search the espData results for a match
                for( EspResponse r : existingValues ) {
                    if( r.getValue().equals(espResponseValue ) ) {
                        // If we get here the espData already has the value so bail out
                        return;
                    }
                }
            }
            // Need to add the school subType value
            // make sure there is a set of values, if not create values
            if( existingValues == null ) {
                existingValues = new ArrayList<EspResponse>(1);
            }
            existingValues.add( createEspResponse( espResponseKey, espResponseValue ) );
            espData.put( espResponseKey, existingValues );
        }
    }

    /**
     * Enhances a specific espData value with school.subtype data.
     * Specific rule is that if the school contains the specified school_subtype and if the espData does not contain that key then then add the espResponseValue
     * @param espData
     * @param espResponseKey Key in results model that my need to be enhanced
     * @param espResponseValue Database value to match
     * @param school
     * @param school_subtype
     */
    private void enhanceEspResultKey(Map<String,List<EspResponse>> espData, String espResponseKey, String espResponseValue, School school, String school_subtype) {
        if( espData==null || espResponseKey==null || espResponseValue== null || school==null || school_subtype==null ) {
            return;
        }

        // See if the school.subtype contains the specified subtype
        SchoolSubtype subtype = school.getSubtype();
        if( subtype.contains(school_subtype) ) {
            // Have matching subtype, see if it needs to be added to the results
            List<EspResponse> existingValues = espData.get(espResponseKey);
            if( existingValues == null || existingValues.size() == 0 ) {
                // Since there is no existing value add the specified espResponseValue
                existingValues = new ArrayList<EspResponse>(1);
                existingValues.add( createEspResponse( espResponseKey, espResponseValue ) );
                espData.put( espResponseKey, existingValues );
            }
        }
    }

    /**
     * Create an EspResponse from a key and value
     * @param key
     * @param value
     * @return
     */
    private EspResponse createEspResponse( String key, String value ) {
        EspResponse r = new EspResponse();
        r.setKey(key);
        r.setValue(value);
        EspResponseHelper.fillInPrettyValue(r);
        r.setActive(false); // This can be used as a flag that this is enhanced data

        return r;
    }

    /**
     * Helper to retrieve academic_award (or service_award) and corresponding year and format it into award (year) format
     * @param espResults
     * @param valueKey The award or service key
     * @param yearKey The corresponding year
     * @return The formatted result or null if there is no corresponding data
     */
    private String formatValueAndYear(Map<String,List<EspResponse>> espResults, String valueKey, String yearKey) {
        List<EspResponse> responses = espResults.get( valueKey );
        if( responses!=null && responses.size()>0 ) {
            StringBuilder sb = new StringBuilder();
            sb.append( responses.get(0).getSafeValue() );
            List<EspResponse> responseYears = espResults.get( yearKey );
            if( responseYears!=null && responseYears.size()>0 ) {
                sb.append( " (" ).append(responseYears.get(0).getSafeValue()).append(")");
            }
            return sb.toString();
        }
        return null;
    }

    public static List<String> formatAmPm( List<String> strings ) {
        if( strings == null ) return null;

        List<String> newStrings = new ArrayList<String>(strings.size());

        for( String s : strings ) {
            newStrings.add(formatAmPm(s));
        }

        return newStrings;
    }

    public static String formatAmPm( String timeStr ) {
        if( timeStr == null ) return null;

        timeStr = StringUtils.replace( timeStr, "AM", "a.m." );
        timeStr = StringUtils.replace( timeStr, "PM", "p.m." );

        return timeStr;
    }

    /**
     * Sort the display values for each row
     * @param resultsModel
     * @param displayConfig
     */
    public static void sortResults( Map<String,List<String>> resultsModel, List<SchoolProfileDisplayBean> displayConfig ) {

        for( SchoolProfileDisplayBean display : displayConfig ) {
            String modelKey = display.getModelKey();
            if(modelKey.contains(AF_PROGRAM_DAYS_KEY_PREFIX) || modelKey.contains(SUMMER_PROGRAM_DAYS_KEY_PREFIX) ||
                    modelKey.contains(SUMMER_PROGRAM_DATES_KEY_PREFIX)) {
                continue;
            }
            List<String> values = resultsModel.get( modelKey );
            Collections.sort( values );
        }

    }

    // The display requires that each section is a separate html table and rows with no data are not displayed.
    // To meet that requirement a List will be built for each section and only the rows with data will be included.
    // And, if a section has no data it should not be displayed at all.
    // As each section is built the first display bean
    // will be added to a master List of all sections to be displayed.  If there is no data for a section the List will be empty.
    public static void buildDisplayModel(String[] modelPrefix, Map<String, List<String>> results,
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

    public static String getGradeRangeString(List<EspResponse> espResponses) {
        List<String> gradeList = new ArrayList<String>();

        for(EspResponse espResponse : espResponses) {
            gradeList.add(espResponse.getPrettyValue());
        }


        if(gradeList.size() > 0) {
            String commaSeparatedGrades = StringUtils.join(gradeList.toArray(), ",");
            Grades grades = new Grades(commaSeparatedGrades);
            return grades.getRangeString();
        }

        return "";
    }

    public static String changeDateFormat(List<EspResponse> espResponses) {
        for(EspResponse espResponse : espResponses) {
            String dateValue = espResponse.getPrettyValue();

            try {
                Date date = new SimpleDateFormat("MM/dd/yyyy").parse(dateValue);
                return new SimpleDateFormat("MMMM d, yyyy").format(date);
            }
            catch (ParseException ex) {
                _log.warn("SchoolProfileProgramsController: Unable to parse date " + espResponse.getValue() +
                        " for " + espResponse.getKey());
            }
        }

        return "";
    }

    /**
     * Build the url's for the CMS article ID's specified in the Display Beans
     * @param request
     * @param displayConfig
     * @param schoolProfileDataHelper
     */
    private Map<String, String> buildRowTitleCmsMap(HttpServletRequest request, List<SchoolProfileDisplayBean> displayConfig, SchoolProfileDataHelper schoolProfileDataHelper) {

        Map<String, String> results = new HashMap<String, String>();

        // First get all of the article ID's that need CMS links
        Set<String> idList = new HashSet<String>();
        for( SchoolProfileDisplayBean displayBean : displayConfig ) {
            String articleId = displayBean.getRowTitleCmsArticleId();
            if( articleId != null && articleId.length() > 0 ) {
                idList.add(articleId);
            }
        }

        // If we have some ID's then get the CMS links
        if( idList.size() > 0 ) {
            String[] idsAsArray = idList.toArray(new String[0]);
            List<ICmsFeatureSearchResult> cmsResults = _schoolProfileDataHelper.getCmsArticles(request, idsAsArray );
            for( int i = 0; i < idsAsArray.length; i++ ) {
                for (ICmsFeatureSearchResult result : cmsResults) {
                    if (idsAsArray[i].equals(result.getContentId().toString())){
                        UrlBuilder builder = new UrlBuilder( result.getContentKey(), result.getFullUri() );
                        String fullUrl = builder.asFullUrl(request);
                        String anchor =
                        results.put( idsAsArray[i], fullUrl );
                    }
                }
            }
        }

        return results;
    }

    /* *****************************************************************************************************
       *        Utility Functions
       *****************************************************************************************************
     */
    // Little helper to get the last bean
    public static SchoolProfileDisplayBean getLastDisplayBean(List<SchoolProfileDisplayBean> displayConfig) {
        return displayConfig.get( displayConfig.size() - 1);
    }

    /**
     * Checks the espData for a key containing the specified value
     * @param espData
     * @param key
     * @param value the value to look for using EspResponse.getValue()
     * @return
     */
    private boolean espDataContains( Map<String, List<EspResponse>> espData, String key, String value) {
        if( espData == null || key == null || value == null ) {
            return false;
        }

        List<EspResponse> espResponses = espData.get(key);
        if( espResponses == null || espResponses.size() == 0 ) {
            return false;
        }

        for( EspResponse e : espResponses ) {
            if( e.getValue().equals(value) ) {
                return true;
            }
        }
        return false;
    }

    /* *****************************************************************************************************
       *        Unit test support functions
       *****************************************************************************************************
     */

    // The following setter dependency injection is just for the tester
    public void setSchoolProfileDataHelper( SchoolProfileDataHelper schoolProfileDataHelper ) {
        _schoolProfileDataHelper = schoolProfileDataHelper;
    }

    public void setSchoolProfileCultureController(SchoolProfileCultureController schoolProfileCultureController) {
        _schoolProfileCultureController = schoolProfileCultureController;
    }

    /* *****************************************************************************************************
       *        Functions that build the display structures
       *        They are places her at the bottom to keep them away from the main processing logic
       *****************************************************************************************************
     */
    private static void buildHighlightsDisplayStructure(List<SchoolProfileDisplayBean> displayConfig) {
        String tabAbbrev = "highlights";
        // Awards section
        String awardsAbbrev = HIGHLIGHTS_AWARDS_ABBREV;
        String awardsTitle = "Awards";
        // Blue Ribbon schools have been removed from the project as of 8/1/12 Bob Raker
//        DISPLAY_CONFIG.add( new SchoolProfileDisplayBean( tabAbbrev, awardsAbbrev, awardsTitle, "National Blue Ribbon School",
//                "blue_ribbon_schools_text") );
//        getLastDisplayBean().addUrl("blue_ribbon_schools_text", null);
//        getLastDisplayBean().addUrl("blue_ribbon_schools_more_info", "blue_ribbon_schools_more_info_url");
        // The following 2 beans will get their data created in the applyUniqueDataRules because the award and year fields need to be merged
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, awardsAbbrev, awardsTitle, "Academic awards received in the past 3 years",
                "academic_award"));
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, awardsAbbrev, awardsTitle, "Community service awards received in the past 3 years",
                "service_award"));

        // Special Education section
        String specEdAbbrev = HIGHLIGHTS_SPECIAL_ED_ABBREV;
        String specEdTitle = "Special education / special needs";
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, specEdAbbrev, specEdTitle, "Specific academic themes or areas of focus",
                "academic_focus", new String[]{"special_education"}) );
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, specEdAbbrev, specEdTitle, "Level of special education programming offered",
                "spec_ed_level") );
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, specEdAbbrev, specEdTitle, "Specialized programs for specific types of special education students",
                "special_ed_programs") );
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, specEdAbbrev, specEdTitle, "Extra learning resources offered",
                "extra_learning_resources", new String[]{"differentiated"} ));
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, specEdAbbrev, specEdTitle, "Staff resources available to students",
                "staff_resources", new String[]{"special_ed_coordinator", "speech_therapist"} ) );
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, specEdAbbrev, specEdTitle, "Clubs",
                "student_clubs", new String[]{"special_olympics"} ) );
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);

        // STEM (Science, Technology, Engineering, & Math)
        String stemAbbrev = HIGHLIGHTS_STEM_ABBREV;
        String stemTitle = "Science, Technology, Engineering, & Math (STEM)";
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, stemAbbrev, stemTitle, "Specific academic themes or areas of focus",
                "academic_focus", new String[]{"mathematics", "science", "technology", "medical", "engineering"} ) );
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, stemAbbrev, stemTitle, "Staff resources available to students",
                "staff_resources", new String[]{"computer_specialist", "robotics_teacher", "math_specialist", "garden_teacher"} ) );
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, stemAbbrev, stemTitle, "School facilities",
                "facilities", new String[]{"computer", "garden", "outdoor", "science", "farm", "industrial"} ) );
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, stemAbbrev, stemTitle, "Vocational or skills-based training offered",
                "skills_training", new String[]{"programming", "it_support", "mechanics", "electrical", "hvac", "engineering"} ) );
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, stemAbbrev, stemTitle, "Visual arts",
                "arts_visual", new String[]{"architecture"} ) );
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, stemAbbrev, stemTitle, "Clubs",
                "student_clubs", new String[]{"gardening", "math_club", "recycling", "robotics", "science_club", "tech_club"} ) );
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);

        // Arts and Music
        String artsAbbrev = HIGHLIGHTS_ARTS_ABBREV;
        String artsTitle = "Arts &amp; music";
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, artsAbbrev, artsTitle, "Specific academic themes or areas of focus",
                "academic_focus", new String[]{"all_arts", "music", "performing_arts", "visual_arts"}));
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, artsAbbrev, artsTitle, "Staff resources available to students",
                "staff_resources", new String[]{"art_teacher", "dance_teacher", "music_teacher", "poetry_teacher"}));
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, artsAbbrev, artsTitle, "School facilities",
                "facilities", new String[]{"art", "music", "performance"}));
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, artsAbbrev, artsTitle, "Vocational or skills-based training offered",
                "skills_training", new String[]{"design"}));
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, artsAbbrev, artsTitle, "Visual arts",
                "arts_visual", new String[]{"ceramics", "drawing", "painting", "photography", "none", "architecture", "design", "printmaking", "sculpture", "textiles"}));
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        getLastDisplayBean(displayConfig).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, artsAbbrev, artsTitle, "Music",
                "arts_music"));
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        getLastDisplayBean(displayConfig).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, artsAbbrev, artsTitle, "Performing and written arts",
                "arts_performing_written"));
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        getLastDisplayBean(displayConfig).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, artsAbbrev, artsTitle, "Media arts",
                "arts_media"));
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        getLastDisplayBean(displayConfig).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, artsAbbrev, artsTitle, "Clubs",
                "student_clubs", new String[]{"student_newspaper", "yearbook", "anime", "art_club", "arts_crafts", "dance", "drama_club", "drill_team", "drum_line", "flag_girls", "literary_mag", "marching_band", "mime", "origami", "sewing_knitting", "step_team", "tv_radio_news", "woodshop"} ) );
        // need to combine student_clubs and student_clubs_dance data
        //getLastDisplayBean().addKey("student_clubs_dance");  - This is now handled in applyUniqueDataRules
        //getLastDisplayBean().setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        getLastDisplayBean(displayConfig).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);

        // Language learning
        String langAbbrev = HIGHLIGHTS_LANG_ABBREV;
        String langTitle = "Language learning";
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, langAbbrev, langTitle, "Specific academic themes or areas of focus",
                "academic_focus", new String[]{"foreign_lang"}));
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        // Note - This is repeated for "programs_resources/Programs/immersion", make sure the DisplayBeans stay in sync
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, langAbbrev, langTitle, "Bi-lingual or language immersion programs offered",
                "immersion", new String[]{"yes"} ) );
        getLastDisplayBean(displayConfig).addKey("immersion_language");
        getLastDisplayBean(displayConfig).addKey("immersion_language_other");
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, langAbbrev, langTitle, "Foreign languages taught",
                "foreign_language"));
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        getLastDisplayBean(displayConfig).addKey("foreign_language_other");
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, langAbbrev, langTitle, "Level of ESL/ELL programming offered",
                "ell_level"));
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, langAbbrev, langTitle, "Languages supported by ESL/ELL programs",
                "ell_languages"));
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, langAbbrev, langTitle, "Staff resources available to students",
                "staff_resources", new String[]{"ell_esl_coord", "speech_therapist"}));
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, langAbbrev, langTitle, "Foreign languages spoken by staff",
                "staff_languages"));
        getLastDisplayBean(displayConfig).addKey("staff_languages_other");
        getLastDisplayBean(displayConfig).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, langAbbrev, langTitle, "Clubs",
                "student_clubs", new String[]{"language_club"}));
        // "student_clubs_language" is handled in applyUniqueDataRules()
        // getLastDisplayBean().setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        // getLastDisplayBean().addKey("student_clubs_language");

        // Health & Athletics
        String healthAbbrev = HIGHLIGHTS_HEALTH_ABBREV;
        String healthTitle = "Health &amp; athletics";
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, healthAbbrev, healthTitle, "Instructional and/or curriculum models used",
                "instructional_model", new String[]{"therapeutic"} ) );
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, healthAbbrev, healthTitle, "Specific academic themes or areas of focus",
                "academic_focus", new String[]{"medical"} ) );
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, healthAbbrev, healthTitle, "Staff resources available to students",
                "staff_resources", new String[]{"cooking_teacher", "dance_teacher", "garden_teacher", "instructional_aid", "pe_instructor", "nurse", "school_psychologist"} ) );
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, healthAbbrev, healthTitle, "School facilities",
                "facilities", new String[]{"farm", "sports_fields", "garden", "gym", "kitchen", "multi_purpose", "swimming"} ) );
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, healthAbbrev, healthTitle, "Clubs",
                "student_clubs", new String[]{"cooking", "gardening", "girls_on_the_run", "martial_arts", "sadd", "special_olympics", "step_team", "yoga"} ) );
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);

        // Gifted & Talented
        String giftedAbbrev = HIGHLIGHTS_GIFTED_ABBREV;
        String giftedTitle = "Gifted &amp; talented";
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, giftedAbbrev, giftedTitle, "Instructional and/or curriculum models used",
                "instructional_model", new String[]{"accelerated_credit", "AP_courses", "gifted", "honors", "ib"} ) );
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, giftedAbbrev, giftedTitle, "Extra learning resources offered",
                "extra_learning_resources", new String[]{"acceleration"}));
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, giftedAbbrev, giftedTitle, "Staff resources available to students",
                "staff_resources", new String[]{"gifted_specialist"}));
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, giftedAbbrev, giftedTitle, "College preparation / awareness resources offered",
                "college_prep"));
        getLastDisplayBean(displayConfig).addKey("college_prep_other");
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, giftedAbbrev, giftedTitle, "Clubs",
                "student_clubs", new String[]{"debate", "", "forensics", "its_academic", "nhs"} ) );
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_ALWAYS);
    }

    // 7/18/12 - This is no longer part of this page
    private static void buildApplicationInfoDisplayStructure(List<SchoolProfileDisplayBean> displayConfig) {
        String tabAbbrev = "application_info";
        // Special Education section
        String sectionAbbrev = "AppEnroll";
        String sectionTitle = "Application and enrollment";
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Does this school have an application or enrollment process?",
                "application_process", new String[]{"yes"}) );
        // The following 2 entries will retrieve the data for the following row but the data will be manipulated in
        // applyUniqueDataRules()
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Learn more about this school's application process",
                "admissions_contact_school", new String[]{"yes"}));
        getLastDisplayBean(displayConfig).addKey("admissions_url");
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Application deadline",
                "application_deadline"));
        getLastDisplayBean(displayConfig).addKey("application_deadline_date");
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Number of applications received for the YEAR-YEAR school year",
                "applications_received"));
        getLastDisplayBean(displayConfig).addRowTitleSubstitution("YEAR-YEAR", "applications_received_year");
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Number of students accepted for the YEAR-YEAR school year",
                "students_accepted"));
        getLastDisplayBean(displayConfig).addRowTitleSubstitution("YEAR-YEAR", "students_accepted_year");
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Students typically attend these schools prior to attending this school",
                "feeder_school_1"));
        getLastDisplayBean(displayConfig).addKey("feeder_school_2");
        getLastDisplayBean(displayConfig).addKey("feeder_school_3");
    }

    private static void buildProgResDisplayStructure(List<SchoolProfileDisplayBean> displayConfig) {
        String tabAbbrev = "programs_resources";
        // Basics section
        String sectionAbbrev = PROGRAMS_BASICS_ABBREV;
        String sectionTitle = "School basics";
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "School start time",
                "start_time" ) );
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "School end time",
                "end_time"));
        // Data for the following Before school... item is retrieved in applyUniqueDataRules()
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Before school or after school care / program onsite",
                "before_after_care", new String[]{"after", "before"}));
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "School Leader's name",
                "administrator_name"));
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Best ways for parents to contact the school",
                "contact_method", new String[]{"email", "phone"}));
        getLastDisplayBean(displayConfig).addKey("contact_method_other");
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Age at which early childhood or Pre-K program begins",
                "age_pk_start"));
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Gender",
                "coed"));
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Special schedule",
                "schedule"));
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Boarding options",
                "boarding"));
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Is there an application process?",
                "application_process"));
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Affiliation",
                "school_type_affiliation"));
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Associations",
                "school.association"));

        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Fax number",
                "school_fax_office_code"));

        // Programs section
        sectionAbbrev = PROGRAMS_PROG_ABBREV;
        sectionTitle = "Programs";
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Instructional and/or curriculum models used",
                "instructional_model"));
        getLastDisplayBean(displayConfig).addKey("instructional_model_other");
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_IF_NOT_ONLY_VALUE);
        getLastDisplayBean(displayConfig).addRowTitleCmsArticleIdAndAnchor("7279", "instructional_model", "TermGlossary_profile_instructional_model");
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Specific academic themes or areas of focus",
                "academic_focus"));
        getLastDisplayBean(displayConfig).addRowTitleCmsArticleIdAndAnchor("7279", "academic_focus", "TermGlossary_profile_academic_focus");
        // Note - This is repeated for "highlights/Language/immersion", make sure the DisplayBeans stay in sync
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Bi-lingual or language immersion programs offered",
                "immersion"));
        getLastDisplayBean(displayConfig).addKey("immersion_language");
        getLastDisplayBean(displayConfig).addKey("immersion_language_other");
        getLastDisplayBean(displayConfig).addRowTitleCmsArticleIdAndAnchor("7279", "immersion", "TermGlossary_profile_immersion");
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Level of special education programming offered",
                "spec_ed_level"));
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Specialized programs for specific types of special education students",
                "special_ed_programs"));
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Foreign languages taught",
                "foreign_language"));
        getLastDisplayBean(displayConfig).addKey("foreign_language_other");
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Level of ESL/ELL programming offered",
                "ell_level"));
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Languages supported by ESL/ELL programs",
                "ell_languages"));
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Vocational or skills-based training offered",
                "skills_training"));
        getLastDisplayBean(displayConfig).addKey("skills_training_other");
        displayConfig.add( new SchoolProfileDisplayBean( tabAbbrev, sectionAbbrev, sectionTitle, "Advanced Placement (AP) exams offered",
                "advanced_placement_exams" ) ); // This key is a placeholder because the data does not come from the ESP table.  The data is provided in applyUniqueDataRules

        // Resources section
        sectionAbbrev = RESOURCES_ABBREV;
        sectionTitle = "Resources";
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Staff resources available to students",
                "staff_resources"));
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Foreign languages spoken by staff",
                "staff_languages"));
        getLastDisplayBean(displayConfig).addKey("staff_languages_other");
        getLastDisplayBean(displayConfig).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Extra learning resources offered",
                "extra_learning_resources"));
        getLastDisplayBean(displayConfig).addKey("extra_learning_resources_other");
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "College preparation / awareness resources offered",
                "college_prep"));
        getLastDisplayBean(displayConfig).addKey("college_prep_other");
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "School-run shuttle from nearby metro and bus stops",
                "transportation_shuttle", new String[]{"yes"} ));
        getLastDisplayBean(displayConfig).addSupportInfo("transportation_shuttle_other");
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Transportation provided for students by the school / district",
                "transportation"));
        getLastDisplayBean(displayConfig).addKey("transportation_other");
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "School facilities",
                "facilities"));
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Partnerships with local resources and organizations",
                "partnershipsPlaceholder"));     // This key is just a placeholder
        getLastDisplayBean(displayConfig).addUrl("partnerships_name_1", "partnerships_url_1");
        getLastDisplayBean(displayConfig).addUrl("partnerships_name_2", "partnerships_url_2");
        getLastDisplayBean(displayConfig).addUrl("partnerships_name_3", "partnerships_url_3");
        getLastDisplayBean(displayConfig).addUrl("partnerships_name_4", "partnerships_url_4");
        getLastDisplayBean(displayConfig).addUrl("partnerships_name_5", "partnerships_url_5");
        getLastDisplayBean(displayConfig).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.URL);
    }

    private static void buildExtrasDisplayStructure(List<SchoolProfileDisplayBean> displayConfig) {
        String tabAbbrev = "extracurriculars";
        // Sports section
        String sectionAbbrev = EXTRA_SPORTS_ABBREV;
        String sectionTitle = "Sports";
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Boys sports",
                "boys_sports"));
        getLastDisplayBean(displayConfig).addKey("boys_sports_other");
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Girls sports",
                "girls_sports"));
        getLastDisplayBean(displayConfig).addKey("girls_sports_other");

        // Arts and Music section
        sectionAbbrev = EXTRA_ARTS_ABBREV;
        sectionTitle = "Arts &amp; music";
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Visual arts",
                "arts_visual"));
        getLastDisplayBean(displayConfig).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Music",
                "arts_music"));
        getLastDisplayBean(displayConfig).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Performing arts",
                "arts_performing_written"));
        getLastDisplayBean(displayConfig).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Media arts",
                "arts_media"));
        getLastDisplayBean(displayConfig).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);

        // Clubs section
        sectionAbbrev = EXTRA_CLUBS_ABBREV;
        sectionTitle = "Student clubs";
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbrev, sectionTitle, "Clubs (distinct from courses)",
                "student_clubs"));
        // student_clubs_dance and student_clubs_language are formatted in applyUniqueDataRules so they can have a prefix
        //getLastDisplayBean().addKey("student_clubs_dance");
        //getLastDisplayBean().addKey("student_clubs_language");
        getLastDisplayBean(displayConfig).addKey("student_clubs_other_1");
        getLastDisplayBean(displayConfig).addKey("student_clubs_other_2");
        getLastDisplayBean(displayConfig).addKey("student_clubs_other_3");
        getLastDisplayBean(displayConfig).setShowNone(SchoolProfileDisplayBean.NoneHandling.REMOVE_NONE_IF_NOT_ONLY_VALUE);
        getLastDisplayBean(displayConfig).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
    }

    public List<SchoolProfileDisplayBean> buildAfterSchoolSummerPrograms(String programType, String tabAbbrev, String title, Set<String> programKeys, int index) {
        /*
             * There are 5 after school and summer programs.
             * Program name is the title for the table, do not build row for that key. Name is currently unavailable,
             * set that to a temp value.
             */
        List<SchoolProfileDisplayBean> displayConfig = new ArrayList<SchoolProfileDisplayBean>();
        String sectionAbbr = programType + index;

        if(AF_PROGRAM_TYPE.equals(programType)) {
            for(String key : programKeys) {
                String rowTitle = afterSchoolKeysPrefix.get(key);
                displayConfig.addAll(buildAfterSchoolSummerProgramHelper(tabAbbrev, sectionAbbr, title, rowTitle, key, index));
            }
        }
        else if(SUMMER_PROGRAM_TYPE.equals(programType)) {
            for(String key : programKeys) {
                String rowTitle = summerProgramKeysPrefix.get(key);
                displayConfig.addAll(buildAfterSchoolSummerProgramHelper(tabAbbrev, sectionAbbr, title, rowTitle, key, index));
            }
        }
        return displayConfig;
    }

    public List<SchoolProfileDisplayBean> buildAfterSchoolSummerProgramHelper(String tabAbbrev, String sectionAbbr, String title, String rowTitle,
                                                                              String key, int i) {
        List<SchoolProfileDisplayBean> displayConfig = new ArrayList<SchoolProfileDisplayBean>();
        displayConfig.add(new SchoolProfileDisplayBean(tabAbbrev, sectionAbbr, title, rowTitle,
                key + i));

        // Special formatting requirements -  Note the Summer program before/after is handled in applyUniqueDataRules
        if(AF_PROGRAM_WEBSITE_KEY_PREFIX.equals(key) || SUMMER_PROGRAM_WEBSITE_KEY_PREFIX.equals(key)) {
            getLastDisplayBean(displayConfig).addUrl(key + i, key + i);
            getLastDisplayBean(displayConfig).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.URL);
            return displayConfig;
        }
        else if(key.startsWith(SUMMER_PROGRAM_DATES_KEY_PREFIX)) {
            getLastDisplayBean(displayConfig).addKey(SUMMER_PROGRAM_DATES_KEY_PREFIX + "start_" + i);
            getLastDisplayBean(displayConfig).addKey(SUMMER_PROGRAM_DATES_KEY_PREFIX + "end_" + i);
        }
        else if(key.startsWith(AF_PROGRAM_ACTIVITIES_KEY_PREFIX)) {
            getLastDisplayBean(displayConfig).addKey(AF_PROGRAM_ACTIVITIES_KEY_PREFIX + "other_" + i);
        }
        else if(key.startsWith(SUMMER_PROGRAM_ACTIVITIES_KEY_PREFIX)) {
            getLastDisplayBean(displayConfig).addKey(SUMMER_PROGRAM_ACTIVITIES_KEY_PREFIX + "other_" + i);
        }
        getLastDisplayBean(displayConfig).setDisplayFormat(SchoolProfileDisplayBean.DisplayFormat.TWO_COL);
        return displayConfig;
    }

    /*
     * 5 unique keys for each program, to identify them and set the correct section title later.
     */
    public static final Map<String, String> TEMP_SECTION_TITLES = new HashMap<String, String>() {{
        put(AF_PROGRAM_TEMP_TITLE_PREFIX + "1", "1");
        put(AF_PROGRAM_TEMP_TITLE_PREFIX + "2", "2");
        put(AF_PROGRAM_TEMP_TITLE_PREFIX + "3", "3");
        put(AF_PROGRAM_TEMP_TITLE_PREFIX + "4", "4");
        put(AF_PROGRAM_TEMP_TITLE_PREFIX + "5", "5");
        put(SUMMER_PROGRAM_TEMP_TITLE_PREFIX + "1", "1");
        put(SUMMER_PROGRAM_TEMP_TITLE_PREFIX + "2", "2");
        put(SUMMER_PROGRAM_TEMP_TITLE_PREFIX + "3", "3");
        put(SUMMER_PROGRAM_TEMP_TITLE_PREFIX + "4", "4");
        put(SUMMER_PROGRAM_TEMP_TITLE_PREFIX + "5", "5");
    }};

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        _beanFactory = beanFactory;
    }
}