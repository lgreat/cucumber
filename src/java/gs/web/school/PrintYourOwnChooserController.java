package gs.web.school;

import gs.data.school.*;
import gs.data.school.census.CensusDataSet;
import gs.data.state.State;
import gs.data.test.TestDataSetDisplayTarget;
import gs.web.PdfView;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RequestMapping("/my-school-list")
@Component("printYourOwnChooserController")
public class PrintYourOwnChooserController implements BeanFactoryAware, ServletContextAware {

    private ServletContext _servletContext;

    private BeanFactory _beanFactory;

    @Autowired
    private IEspResponseDao _espResponseDao;

    @Autowired
    private SchoolProfileCensusHelper _schoolProfileCensusHelper;

    @Autowired
    private ISchoolDao _schoolDaoHibernate;

    @Autowired
    private SchoolProfileDataHelper _schoolProfileDataHelper;

    @Autowired
    private InternalResourceViewResolver _viewResolver;

    public static final int MAX_ALLOWED_SCHOOLS = 100;

    private static final String VIEW_NAME = "printYourOwnChooser";

    private static final String PATH_TO_PRESCHOOL_CHECKLIST_PDF = "/res/pdf/schoolchooser/preschool-checklist.pdf";
    private static final String PATH_TO_ELEMENTARY_CHECKLIST_PDF = "/res/pdf/schoolchooser/elementary-school-checklist.pdf";
    private static final String PATH_TO_MIDDLE_CHECKLIST_PDF = "/res/pdf/schoolchooser/middle-school-checklist.pdf";
    private static final String PATH_TO_HIGH_CHECKLIST_PDF = "/res/pdf/schoolchooser/high-school-checklist.pdf";

    private static Logger _logger = Logger.getLogger(PrintYourOwnChooserController.class);

    public static final String DATA_OVERALL_ACADEMIC_RATING = "overallAcademicRating"; // TestDataType.id = 167
    public static final String DATA_OVERALL_ACADEMIC_RATING_TEXT = "overallAcademicRatingText"; // TestDataType.id = 167
    public static final String DATA_OVERALL_CLIMATE_RATING = "overallClimateRating"; // TestDataType.id = 173
    public static final String DATA_OVERALL_CLIMATE_RATING_TEXT = "overallClimateRatingText"; // TestDataType.id = 173

    private static final String MODEL_KEY_BEST_KNOWN_FOR = "bestKnownFor";
    private static final String MODEL_KEY_ETHNICITY_MAP = "ethnicityMap";

    @RequestMapping(method= RequestMethod.POST)
    public View post(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response,
                      @RequestParam(value="states", required = false) String statesCsv,
                      @RequestParam(value="schoolIds", required = false) String schoolIdsCsv,
                      @RequestParam(value="appendChecklist", required = false) Boolean appendChecklist) {
        return get(modelMap, request, response, statesCsv, schoolIdsCsv, appendChecklist);
    }

    @RequestMapping(method= RequestMethod.GET)
    public View get(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response,
                      @RequestParam(value="states", required = false) String statesCsv,
                      @RequestParam(value="schoolIds", required = false) String schoolIdsCsv,
                      @RequestParam(value="appendChecklist", required = false) Boolean appendChecklist) {

        AbstractDataHelper.initialize(request);

        List<School> schools = new ArrayList<School>();
        try {
            schools = getSchoolsFromParams(statesCsv, schoolIdsCsv);

            if (schools.isEmpty()) {
                _logger.debug("No schools found. Redirecting to 404 page");
                return new RedirectView("/status/error404.page");
            }
        } catch (Exception e) {
            if (statesCsv != null && schoolIdsCsv != null) {
                _logger.debug("Exception while getting School objects with given params. States: " + statesCsv + " school IDs " + schoolIdsCsv, e);
            } else {
                _logger.debug("Exception while getting School objects with given params.", e);
            }
            return new RedirectView("/status/error404.page");
        }

        modelMap.put("schools", schools);

        Map<String,Object> schoolData = new HashMap<String,Object>();

        for (School school : schools) {
            Map<String, Object> data = new HashMap<String, Object>();

            addOspDataToModel(school, data);

            addEthnicityDataToModel(school, data);

            schoolData.put(school.getStateAbbreviation().getAbbreviationLowerCase() + String.valueOf(school.getId()), data);

            Set<String> displayTarget = new HashSet<String>();
            displayTarget.add(TestDataSetDisplayTarget.ratings.name());
            Map<String, Object> dataMap = _schoolProfileDataHelper.getDataMap(school, displayTarget);
            if (dataMap == null) {
                dataMap = new HashMap<String,Object>();
            }

            Object climateRating = dataMap.get(DATA_OVERALL_CLIMATE_RATING);

            if (climateRating != null) {
                data.put(DATA_OVERALL_CLIMATE_RATING, climateRating);
                data.put(DATA_OVERALL_CLIMATE_RATING_TEXT, formatRating((Integer) climateRating));
            }
            Object academicRating = dataMap.get(DATA_OVERALL_ACADEMIC_RATING);
            if (academicRating != null) {
                data.put(DATA_OVERALL_ACADEMIC_RATING, academicRating);
                data.put(DATA_OVERALL_ACADEMIC_RATING_TEXT, formatRating((Integer) academicRating));
            }
        }
        modelMap.put("schoolData", schoolData);

        try {
            View viewToWrap = _viewResolver.resolveViewName(VIEW_NAME, Locale.getDefault());

            if (Boolean.TRUE == appendChecklist) {
                String[] pdfsToAppend = getPdfsToAppend(schools);
                return new PdfView(viewToWrap,pdfsToAppend);
            } else {
                return new PdfView(viewToWrap);
            }
        } catch (Exception e) {
            return new RedirectView("/status/error404.page");
        }
    }

    public String[] getPdfsToAppend(List<School> schools) {

        SortedSet<LevelCode.Level> levelCodes = new TreeSet<LevelCode.Level>();
        List<String> paths = new ArrayList<String>();

        for (School school : schools) {
            levelCodes.addAll(school.getLevelCode().getIndividualLevelCodes());
        }

        if (levelCodes.contains(LevelCode.Level.PRESCHOOL_LEVEL)) {
            paths.add(_servletContext.getRealPath(PATH_TO_PRESCHOOL_CHECKLIST_PDF));
        }
        if (levelCodes.contains(LevelCode.Level.ELEMENTARY_LEVEL)) {
            paths.add(_servletContext.getRealPath(PATH_TO_ELEMENTARY_CHECKLIST_PDF));
        }
        if (levelCodes.contains(LevelCode.Level.MIDDLE_LEVEL)) {
            paths.add(_servletContext.getRealPath(PATH_TO_MIDDLE_CHECKLIST_PDF));
        }
        if (levelCodes.contains(LevelCode.Level.HIGH_LEVEL)) {
            paths.add(_servletContext.getRealPath(PATH_TO_HIGH_CHECKLIST_PDF));
        }

        return paths.toArray(new String[0]);
    }

    private String formatRating(int rating) {
        if (rating > 7) {
            return "Above average";
        } else if (rating > 3) {
            return "Average";
        } else {
            return "Below average";
        }
    }

    public void addOspDataToModel(School school, Map<String, Object> data) {
        Map<String, List<EspResponse>> espData = getEspData(school);

        data.put("tuition_low", getSinglePrettyValue(espData, "tuition_low"));
        data.put("tuition_high", getSinglePrettyValue(espData, "tuition_high"));
        data.put("financial_aid", getSinglePrettyValue(espData, "financial_aid"));
        data.put("students_vouchers", getSinglePrettyValue(espData, "students_vouchers"));
        data.put("ell_level", getSinglePrettyValue(espData, "ell_level"));


        // esp data - "best known for" quote
        String bestKnownFor = null;
        List<EspResponse> espResponses = espData.get( "best_known_for" );
        if (espResponses != null && espResponses.size() > 0) {
            bestKnownFor = espResponses.get(0).getSafeValue();
            data.put(MODEL_KEY_BEST_KNOWN_FOR, bestKnownFor);
        }


        // application deadline
        String applicationDeadline = getSingleValue(espData, "application_deadline_date");
        Date applicationDeadlineDate = null;
        if (applicationDeadline != null) {
            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
            try {
                applicationDeadlineDate = format.parse(applicationDeadline);
            } catch (ParseException e) {
                _logger.debug("Problem parsing date: "+ applicationDeadline);
            }
            data.put("application_deadline_date", applicationDeadlineDate);
        }


        // College destinations (where kids go after graduating)
        String collegeDestination1 = getSinglePrettyValue(espData, "college_destination_1");
        String collegeDestination2 = getSinglePrettyValue(espData, "college_destination_2");
        String collegeDestination3 = getSinglePrettyValue(espData, "college_destination_3");
        data.put("college_destination_1", collegeDestination1);
        data.put("college_destination_2", collegeDestination2);
        data.put("college_destination_3", collegeDestination3);
        String collegeDestinations = "";
        if (collegeDestination1 != null) {
            collegeDestinations = collegeDestination1;
        }
        if (collegeDestination2 != null) {
            collegeDestinations += "; " + collegeDestination2;
        }
        if (collegeDestination3 != null) {
            collegeDestinations += "; " + collegeDestination3;
        }
        data.put("college_destinations", collegeDestinations);


        // before and after care
        addBeforeAfterCareToModel(data, espData);


        // class hours
        data.put("start_time", getSinglePrettyValue(espData, "start_time"));
        data.put("end_time", getSinglePrettyValue(espData, "end_time"));


        // special ed services
        addSpecialEdToModel(data, espData);


        // dress code
        String dressCode = getSingleValue(espData, "dress_code");
        if (dressCode != null) {
            data.put("dress_code",
                    (dressCode.equalsIgnoreCase("dress_code") || dressCode.equalsIgnoreCase("uniform"))? "Yes":"No"
            );
        }


        // transportation
        addTransportationToModel(data, espData);
    }

    private String getSinglePrettyValue(Map<String, List<EspResponse>> espData, String key) {
        List<EspResponse> responses = espData.get(key);
        if (responses != null && responses.size() > 0) {
            return StringUtils.trimToNull(responses.get(0).getPrettyValue());
        }
        return null;
    }

    private String getSingleValue(Map<String, List<EspResponse>> espData, String key) {
        List<EspResponse> responses = espData.get(key);
        if (responses != null && responses.size() > 0) {
            return responses.get(0).getSafeValue();
        }
        return null;
    }

    private void addEthnicityDataToModel(School school, Map<String, Object> data) {
        // ethnicity data
        Set<Integer> censusDataTypeIds = new HashSet<Integer>();
        censusDataTypeIds.add(9); // ethnicity
        Map<Integer, CensusDataSet> censusDataSets = _schoolProfileCensusHelper.getCensusDataSets(school.getDatabaseState(), censusDataTypeIds, school);

        // we just need school data for the census data sets we have
        CensusDataHolder censusDataHolder = (CensusDataHolder) _beanFactory.getBean("censusDataHandler", new Object[] {
                school, censusDataSets, censusDataSets, null, null
        });

        censusDataHolder.retrieveDataSetsAndSchoolData(); // after this operation, the censusDataSets will contain school values

        Map<String, String> ethnicityLabelValueMap = _schoolProfileCensusHelper.getEthnicityLabelValueMap(censusDataSets);
        data.put(MODEL_KEY_ETHNICITY_MAP, ethnicityLabelValueMap);
    }

    public void addBeforeAfterCareToModel(Map<String, Object> data, Map<String, List<EspResponse>> espData) {
        List<EspResponse> beforeAfter = espData.get("before_after_care");
        boolean foundBefore = false;
        boolean foundAfter = false;
        boolean foundNeither = false;

        if (beforeAfter != null && beforeAfter.size() > 0) {
            for (EspResponse e : beforeAfter) {
                if (e.getSafeValue().equalsIgnoreCase("before")) {
                    // Have before now check for time
                    List<EspResponse> start = espData.get("before_after_care_start");
                    if (start != null && start.size() > 0) {
                        data.put("before_care", "Starts " + SchoolProfileProgramsController.formatAmPm(start.get(0).getSafeValue()));
                    }
                    else {
                        data.put("before_care", "Yes");
                    }
                    foundBefore = true;
                }
                if (e.getSafeValue().equalsIgnoreCase("after")) {
                    // Have before now check for time
                    List<EspResponse> end = espData.get("before_after_care_end");
                    if (end != null && end.size() > 0) {
                        data.put("after_care", "Ends " + SchoolProfileProgramsController.formatAmPm(end.get(0).getSafeValue()));
                    }
                    else {
                        data.put("after_care", "Yes");
                    }
                    foundAfter = true;
                }
                if (e.getSafeValue().equalsIgnoreCase("neither")) {
                    foundNeither = true;
                }
            }
        }

        if (foundNeither || (foundBefore && !foundAfter)) {
            data.put("after_care", "No");
        }
        if (foundNeither || (!foundBefore && foundAfter)) {
            data.put("before_care", "No");
        }
    }

    public void addTransportationToModel(Map<String, Object> data, Map<String, List<EspResponse>> espData) {
        String transportation = getSingleValue(espData, "transportation");
        if (transportation != null) {
            String transportationValueForModel = null;
            if (transportation.equalsIgnoreCase("none") || transportation.equalsIgnoreCase("special_ed_only")) {
                transportationValueForModel = "No";
            } else if (transportation.equalsIgnoreCase("passes") ||
                    transportation.equalsIgnoreCase("busses") ||
                    transportation.equalsIgnoreCase("shared_bus")) {
                transportationValueForModel = "Yes";
            }

            if (transportationValueForModel != null) {
                data.put("transportation", transportationValueForModel);
            }
        }
    }

    public void addSpecialEdToModel(Map<String, Object> data, Map<String, List<EspResponse>> espData) {
        String level = getSingleValue(espData, "spec_ed_level");
        String exists = getSingleValue(espData, "special_ed_programs_exists");

        if (StringUtils.isNotBlank(level) && (
                level.equalsIgnoreCase("none") ||
                level.equalsIgnoreCase("basic") ||
                level.equalsIgnoreCase("moderate") ||
                level.equalsIgnoreCase("intensive"))) {
            data.put("special_ed", StringUtils.capitalize(level.toLowerCase()));
        } else if (StringUtils.isNotBlank(exists) && (
                exists.equalsIgnoreCase("yes") ||
                exists.equalsIgnoreCase("no"))) {
            data.put("special_ed", StringUtils.capitalize(exists.toLowerCase()));
        }
    }

    public Map<String, List<EspResponse>> getEspData(School school) {
        List<EspResponse> results = _espResponseDao.getResponses( school );

        // For performance reasons convert the results to a HashMap.  The key will be the espResponseKey
        // and the value will be the corresponding list of EspResponse objects
        Map<String, List<EspResponse>> espData = EspResponse.rollup(results);

        return espData;
    }

    public List<School> getSchoolsFromParams(String statesCsv, String schoolIdsCsv) {

        String[] states = StringUtils.split(statesCsv,',');
        String[] schoolIds = StringUtils.split(schoolIdsCsv,',');

        if (states == null || schoolIds == null || states.length == 0 || schoolIds.length == 0) {
            throw new IllegalArgumentException("Neither states nor schoolIds arrays may be null or empty");
        }
        if (states.length > 1 && (states.length != schoolIds.length)) {
            throw new IllegalArgumentException("If more than one state is provided, the number of states and IDs must be equal");
        }

        // this map will keep our school state / IDs ordered and associate with the Schools returned from DAO
        // state + ID --> School
        LinkedHashMap<String, School> orderedSchools = new LinkedHashMap<String, School>();

        // seed the order map
        for (int i = 0; i < schoolIds.length && i < MAX_ALLOWED_SCHOOLS; i++) {
            String stateString;
            if (states.length > 1) {
                stateString = states[i];
            } else {
                stateString = states[0];
            }

            State state = State.fromString(stateString);

            String statePlusId = state.getAbbreviationLowerCase() + "_" + schoolIds[i];
            orderedSchools.put(statePlusId, new School());
        }

        // place the placeholders into per-state buckets
        Map<String,List<String>> statesAndIds = new HashMap<String,List<String>>();
        for (String schoolPlaceholder : orderedSchools.keySet()) {
            List<String> ids = statesAndIds.get(schoolPlaceholder.substring(0,2));
            if (ids == null) {
                ids = new ArrayList<String>();
            }
            ids.add(schoolPlaceholder);
            statesAndIds.put(schoolPlaceholder.substring(0,2), ids);
        }

        // execute one DAO call per state bucket
        Iterator<Map.Entry<String, List<String>>> iterator = statesAndIds.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<String>> bucketEntry = iterator.next();

            StringBuffer ids = new StringBuffer();
            // collect IDs into a csv
            for (String placeholder : bucketEntry.getValue()) {
                if (ids.length() > 0) {
                    ids.append(",");
                }
                ids.append(placeholder.substring(3));
            }

            String state = bucketEntry.getKey();
            // make a DAO call
            List<School> results = _schoolDaoHibernate.getSchoolsByIds(State.fromString(state), ids.toString(), true);

            // associate each result with the correct position in the order map
            for (School result : results) {
                String stateAndId = result.getDatabaseState().getAbbreviationLowerCase() + "_" + result.getId();
                orderedSchools.put(stateAndId, result);
            }
        }

        // get the results from a Map<StateAndId> to List<School>
        List<School> schools = new ArrayList<School>();
        Iterator<Map.Entry<String, School>> schoolIterator = orderedSchools.entrySet().iterator();
        while (schoolIterator.hasNext()) {
            Map.Entry<String, School> entry = schoolIterator.next();
            if (entry.getValue().getId() != null) {
                schools.add(entry.getValue());
            }
        }

        return schools;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        _beanFactory = beanFactory;
    }

    public void setServletContext(ServletContext servletContext) {
        _servletContext = servletContext;
    }
}
