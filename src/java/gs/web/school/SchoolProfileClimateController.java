package gs.web.school;

import gs.data.school.School;
import gs.data.school.census.CensusDataSet;
import gs.data.school.census.CensusDataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author aroy@greatschools.org
 */
@Controller
@RequestMapping("/school/profileClimate.page")
public class SchoolProfileClimateController extends AbstractSchoolProfileController {
    public static final String VIEW = "school/profileClimate";
    @Autowired
    SchoolProfileCensusHelper _schoolProfileCensusHelper;

    @RequestMapping(method = RequestMethod.GET)
    public String handle(ModelMap modelMap, HttpServletRequest request) {
        School school = getSchool(request);
        modelMap.put("school", school );

        CensusDataHolder censusDataHolder = _schoolProfileCensusHelper.getCensusDataHolder(request);

        // make the census data holder load school, district, and state data onto the census data sets
        Map<Integer, CensusDataSet> censusDataSetMap = censusDataHolder.retrieveDataSetsAndAllData();

        Map<Long, CensusDataSet> dataTypeToDataSetMap = new HashMap<Long, CensusDataSet>(5);

        for (Integer dataSetId: censusDataSetMap.keySet()) {
            CensusDataSet censusDataSet = censusDataSetMap.get(dataSetId);
            if (isDataTypeForClimate(censusDataSet.getDataType())
                    && (censusDataSet.getStateCensusValue() != null
                    || (censusDataSet.getSchoolData() != null && censusDataSet.getSchoolData().size() > 0))) {
                dataTypeToDataSetMap.put(censusDataSet.getDataType().getId().longValue(), censusDataSet);
            }
        }

        if (dataTypeToDataSetMap.size() > 0) {
            modelMap.put("climateData", dataTypeToDataSetMap);
//            modelMap.put("footnoteHelper", getCensusSourceHelper(dataTypeToDataSetMap.values()));
        }

        return VIEW;
    }

    protected static boolean isDataTypeForClimate(CensusDataType censusDataType) {
        return censusDataType != null && (
                censusDataType.equals(CensusDataType.CLIMATE_NUMBER_OF_RESPONSES_PARENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_SAFETY_RESPECT_SCORE_PARENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_COMMUNICATION_SCORE_PARENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_ENGAGEMENT_SCORE_PARENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_SCORE_PARENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_RESPONSE_RATE_PARENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_NUMBER_OF_RESPONSES_STUDENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_SAFETY_RESPECT_SCORE_STUDENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_COMMUNICATION_SCORE_STUDENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_ENGAGEMENT_SCORE_STUDENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_SCORE_STUDENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_RESPONSE_RATE_STUDENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_NUMBER_OF_RESPONSES_TEACHER) ||
                        censusDataType.equals(CensusDataType.CLIMATE_SAFETY_RESPECT_SCORE_TEACHER) ||
                        censusDataType.equals(CensusDataType.CLIMATE_COMMUNICATION_SCORE_TEACHER) ||
                        censusDataType.equals(CensusDataType.CLIMATE_ENGAGEMENT_SCORE_TEACHER) ||
                        censusDataType.equals(CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_SCORE_TEACHER) ||
                        censusDataType.equals(CensusDataType.CLIMATE_RESPONSE_RATE_TEACHER) ||
                        censusDataType.equals(CensusDataType.CLIMATE_SAFETY_RESPECT_SCORE_TOTAL) ||
                        censusDataType.equals(CensusDataType.CLIMATE_COMMUNICATION_SCORE_TOTAL) ||
                        censusDataType.equals(CensusDataType.CLIMATE_ENGAGEMENT_SCORE_TOTAL) ||
                        censusDataType.equals(CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_SCORE_TOTAL) ||
                        censusDataType.equals(CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_PERCENT_AGREE_TOTAL) ||
                        censusDataType.equals(CensusDataType.CLIMATE_FAMILY_ENGAGEMENT_PERCENT_AGREE_TOTAL) ||
                        censusDataType.equals(CensusDataType.CLIMATE_RESPECT_RELATIONSHIPS_PERCENT_AGREE_TOTAL) ||
                        censusDataType.equals(CensusDataType.CLIMATE_SAFETY_CLEANLINESS_PERCENT_AGREE_TOTAL) ||
                        censusDataType.equals(CensusDataType.CLIMATE_TEACHER_COLLABORATION_SUPPORT_PERCENT_AGREE_TOTAL) ||
                        censusDataType.equals(CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_PERCENT_AGREE_PARENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_FAMILY_ENGAGEMENT_PERCENT_AGREE_PARENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_SAFETY_CLEANLINESS_PERCENT_AGREE_PARENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_ACADEMIC_EXPECTATIONS_PERCENT_AGREE_STUDENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_RESPECT_RELATIONSHIPS_PERCENT_AGREE_STUDENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_SAFETY_CLEANLINESS_PERCENT_AGREE_STUDENT) ||
                        censusDataType.equals(CensusDataType.CLIMATE_FAMILY_ENGAGEMENT_PERCENT_AGREE_SCHOOL_EMPLOYEE) ||
                        censusDataType.equals(CensusDataType.CLIMATE_RESPECT_RELATIONSHIPS_PERCENT_AGREE_SCHOOL_EMPLOYEE) ||
                        censusDataType.equals(CensusDataType.CLIMATE_SAFETY_CLEANLINESS_PERCENT_AGREE_SCHOOL_EMPLOYEE) ||
                        censusDataType.equals(CensusDataType.CLIMATE_TEACHER_COLLABORATION_SUPPORT_PERCENT_AGREE_SCHOOL_EMPLOYEE) ||
                        censusDataType.equals(CensusDataType.CLIMATE_NUMBER_OF_RESPONSES_SCHOOL_EMPLOYEE) ||
                        censusDataType.equals(CensusDataType.CLIMATE_RESPONSE_RATE_SCHOOL_EMPLOYEE));
    }

}
