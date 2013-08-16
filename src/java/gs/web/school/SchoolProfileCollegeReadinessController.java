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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author aroy@greatschools.org
 */
@Controller
@RequestMapping("/school/profileCollegeReadiness.page")
public class SchoolProfileCollegeReadinessController extends AbstractSchoolProfileController {
    public static final String VIEW = "school/profileCollegeReadiness";
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
            if (isDataTypeForCollegeReadiness(censusDataSet.getDataType())
                    && (censusDataSet.getStateCensusValue() != null
                    || (censusDataSet.getSchoolData() != null && censusDataSet.getSchoolData().size() > 0))) {
                dataTypeToDataSetMap.put(censusDataSet.getDataType().getId().longValue(), censusDataSet);
            }
        }

        if (dataTypeToDataSetMap.size() > 0) {
            modelMap.put("collegeReadinessData", dataTypeToDataSetMap);
            modelMap.put("footnoteHelper", getCensusSourceHelper(dataTypeToDataSetMap.values()));
        }

        return VIEW;
    }

    protected SchoolProfileCensusSourceHelper getCensusSourceHelper(Collection<CensusDataSet> censusDataSets) {
        if (censusDataSets == null || censusDataSets.size() == 0) {
            return null;
        }
        SchoolProfileCensusSourceHelper sourceHelper = new SchoolProfileCensusSourceHelper();
        for (CensusDataSet censusDataSet: censusDataSets) {
            sourceHelper.recordSource(censusDataSet);
        }
        return sourceHelper;
    }

    protected boolean isDataTypeForCollegeReadiness(CensusDataType censusDataType) {
        return censusDataType != null && (
                censusDataType.equals(CensusDataType.PERCENT_ENROLLED_IN_COLLEGE_FOLLOWING_HIGH_SCHOOL) ||
                censusDataType.equals(CensusDataType.PERCENT_NEEDING_REMEDIATION_FOR_COLLEGE) ||
                censusDataType.equals(CensusDataType.AVERAGE_GPA_FIRST_YEAR_OF_COLLEGE) ||
                censusDataType.equals(CensusDataType.AVERAGE_NUMBER_UNITS_COMPLETED_FIRST_YEAR_OF_COLLEGE) ||
                censusDataType.equals(CensusDataType.PERCENT_ENROLLED_IN_COLLEGE_AND_RETURNED_FOR_SECOND_YEAR));
    }

}
