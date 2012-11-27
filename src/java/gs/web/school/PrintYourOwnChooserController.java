package gs.web.school;

import gs.data.school.*;
import gs.data.school.census.CensusDataSet;
import gs.data.state.State;
import gs.data.test.TestDataSetDisplayTarget;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RequestMapping("/print-your-own-chooser/chooser")
@Component("printYourOwnChooserController")
public class PrintYourOwnChooserController implements BeanFactoryAware {

    private BeanFactory _beanFactory;

    @Autowired
    private IEspResponseDao _espResponseDao;

    @Autowired
    private SchoolProfileCensusHelper _schoolProfileCensusHelper;

    @Autowired
    private ISchoolDao _schoolDaoHibernate;

    @Autowired
    private SchoolProfileDataHelper _schoolProfileDataHelper;

    private static Logger _logger = Logger.getLogger(PrintYourOwnChooserController.class);

    public static final String DATA_OVERALL_RATING = "overallRating"; // TestDataType.id = 174
    public static final String DATA_OVERALL_RATING_TEXT = "overallRatingText"; // TestDataType.id = 174
    public static final String DATA_OVERALL_ACADEMIC_RATING = "overallAcademicRating"; // TestDataType.id = 167
    public static final String DATA_OVERALL_ACADEMIC_RATING_TEXT = "overallAcademicRatingText"; // TestDataType.id = 167
    public static final String DATA_OVERALL_CLIMATE_RATING = "overallClimateRating"; // TestDataType.id = 173
    public static final String DATA_OVERALL_CLIMATE_RATING_TEXT = "overallClimateRatingText"; // TestDataType.id = 173

    private static final String MODEL_KEY_BEST_KNOWN_FOR = "bestKnownFor";
    private static final String MODEL_KEY_ETHNICITY_MAP = "ethnicityMap";

    /**
     *
     */
    @RequestMapping(method= RequestMethod.GET)
    public String get(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response,
                      @RequestParam(value="states") String statesCsv,
                      @RequestParam(value="schoolIds") String schoolIdsCsv) {

        AbstractDataHelper.initialize(request);

        List<School> schools = new ArrayList<School>();
        try {
            schools = getSchoolsFromParams(statesCsv, schoolIdsCsv);
        } catch (IllegalArgumentException e ) {

        }

        modelMap.put("schools", schools);

        Map<String,Object> schoolData = new HashMap<String,Object>();

        for (School school : schools) {
            Map<String, Object> data = new HashMap<String, Object>();

            addBestKnownForQuoteToModel(school, data);

            addEthnicityDataToModel(school, data);

            schoolData.put(school.getStateAbbreviation().getAbbreviationLowerCase() + String.valueOf(school.getId()), data);

            Set<String> displayTarget = new HashSet<String>();
            displayTarget.add(TestDataSetDisplayTarget.ratings.name());
            Map<String, Object> dataMap = _schoolProfileDataHelper.getDataMap(school, displayTarget);


            data.put(DATA_OVERALL_RATING, dataMap.get(DATA_OVERALL_RATING));
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

        return "printYourOwnChooser";
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

    private void addBestKnownForQuoteToModel(School school, Map<String, Object> data) {
        Map<String, List<EspResponse>> espData = getEspData(school);

        // esp data - "best known for" quote
        String bestKnownFor = null;
        List<EspResponse> espResponses = espData.get( "best_known_for" );
        if (espResponses != null && espResponses.size() > 0) {
            bestKnownFor = espResponses.get(0).getSafeValue();
            data.put(MODEL_KEY_BEST_KNOWN_FOR, bestKnownFor);
        }

        data.put("tuition_low", getSinglePrettyValue(espData, "tuition_low"));
        data.put("tuition_high", getSinglePrettyValue(espData, "tuition_high"));
        data.put("financial_aid", getSinglePrettyValue(espData, "financial_aid"));
        String applicationDeadline = getSingleValue(espData, "application_deadline_date");
        Date applicationDeadlineDate = null;
        if (applicationDeadline != null) {
            SimpleDateFormat format = new SimpleDateFormat("mm/dd/yyyy");
            try {
                applicationDeadlineDate = format.parse(applicationDeadline);
            } catch (ParseException e) {
                _logger.debug("Problem parsing date: "+ applicationDeadline);
            }
            data.put("application_deadline_date", applicationDeadlineDate);
        }

        String destinationSchool1 = getSinglePrettyValue(espData, "destination_school_1");
        String destinationSchool2 = getSinglePrettyValue(espData, "destination_school_2");
        String destinationSchool3 = getSinglePrettyValue(espData, "destination_school_3");
        data.put("destination_school_1", destinationSchool1);
        data.put("destination_school_2", destinationSchool2);
        data.put("destination_school_3", destinationSchool3);
        String destinationSchools = "";
        if (destinationSchool1 != null) {
            destinationSchools = destinationSchool1;
        }
        if (destinationSchool2 != null) {
            destinationSchools += "; " + destinationSchool2;
        }
        if (destinationSchool3 != null) {
            destinationSchools += "; " + destinationSchool3;
        }
        data.put("destination_schools", destinationSchools);

        data.put("students_vouchers", getSinglePrettyValue(espData, "students_vouchers"));
        data.put("ell_level", getSingleValue(espData, "ell_level"));

        data.put("before_after_care_start", getSinglePrettyValue(espData, "before_after_care_start"));
        data.put("before_after_care_end", getSinglePrettyValue(espData, "before_after_care_end"));

        // class hours
        data.put("start_time", getSinglePrettyValue(espData, "start_time"));
        data.put("end_time", getSinglePrettyValue(espData, "end_time"));


        //special ed services
        List<EspResponse> responses = espData.get("special_ed_services");
        StringBuffer specialEdServices = new StringBuffer();
        if (responses != null) {
            for (EspResponse response : responses) {
                if (specialEdServices.length() > 0) {
                    specialEdServices.append("; ");
                }
                specialEdServices.append(response.getPrettyValue());
            }
        }
        data.put("special_ed_services", specialEdServices.toString());

        String dressCode = getSingleValue(espData, "dress_code");
        if (dressCode != null) {
            data.put("dress_code",
                    (dressCode.equalsIgnoreCase("dress_code") || dressCode.equalsIgnoreCase("uniform"))? "Yes":"No"
            );
        }

        String transportation = getSingleValue(espData, "transportation");
        data.put("transportation", (transportation != null && !transportation.equalsIgnoreCase("none"))? "Yes":"No");


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

    public Map<String, List<EspResponse>> getEspData(School school) {
        List<EspResponse> results = _espResponseDao.getResponses( school );

        // For performance reasons convert the results to a HashMap.  The key will be the espResponseKey
        // and the value will be the corresponding list of EspResponse objects
        Map<String, List<EspResponse>> espData = EspResponse.rollup(results);

        return espData;
    }

    public List<School> getSchoolsFromParams(String statesCsv, String schoolIdsCsv) {
        List<School> schools = new ArrayList<School>();

        String[] states = StringUtils.split(statesCsv,',');
        String[] schoolIds = StringUtils.split(schoolIdsCsv,',');

        if (states == null || schoolIds == null || states.length == 0 || schoolIds.length == 0) {
            throw new IllegalArgumentException("Neither states nor schoolIds arrays may be null or empty");
        }
        if (states.length > 1 && (states.length != schoolIds.length)) {
            throw new IllegalArgumentException("If more than one state is provided, the number of states and IDs must be equal");
        }

        for (int i = 0; i < schoolIds.length; i++) {
            String stateString;
            if (states.length > 1) {
                stateString = states[i];
            } else {
                stateString = states[0];
            }

            try {
                State state = State.fromString(stateString);
                School s = _schoolDaoHibernate.getSchoolById(state, new Integer(schoolIds[i]));
                schools.add(s);
            } catch (IllegalArgumentException e) {

            }
        }

        return schools;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        _beanFactory = beanFactory;
    }
}
