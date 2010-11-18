package gs.web.compare;

import gs.data.school.School;
import gs.data.school.census.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Teachers & Students
 * 
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CompareStudentTeacherController extends AbstractCompareSchoolController {
    public static final String TAB_NAME = "studentTeacher";
    private String _successView;
    private ICensusDataSetDao _censusDataSetDao;
    private ICensusInfo _censusInfo;

    @Override
    protected void handleCompareRequest(HttpServletRequest request, HttpServletResponse response,
                                        List<ComparedSchoolBaseStruct> schools, Map<String, Object> model) throws
                                                                                                           Exception {
        model.put(MODEL_TAB, TAB_NAME);

        handleEthnicities(schools);
    }

    // TEMPORARY METHOD PENDING FINAL IMPLEMENTATION
    protected void handleEthnicities(List<ComparedSchoolBaseStruct> structs) {
        for (ComparedSchoolBaseStruct baseStruct: structs) {
            ComparedSchoolStudentTeacherStruct struct = (ComparedSchoolStudentTeacherStruct) baseStruct;
            School school = baseStruct.getSchool();
            List<AbstractCensusValue> values = getCensusInfo().getLatestSchoolValues
                    (school, CensusDataType.STUDENTS_ETHNICITY);
            Collections.sort(values, new Comparator<AbstractCensusValue>() {
                public int compare(AbstractCensusValue o1, AbstractCensusValue o2) {
                    return o2.getValueFloat().compareTo(o1.getValueFloat());
                }
            });
            Map<String, String> ethnicities = new LinkedHashMap<String, String>();

            for (AbstractCensusValue value: values) {
                String ethnicityName = value.getDataSet().getBreakdown().getEthnicity().getName();
                Float ethnicityValue = value.getValueFloat();

                int nVal = Math.round(ethnicityValue);
                if (nVal < 1) {
                    ethnicities.put(ethnicityName, "<1");
                } else {
                    ethnicities.put(ethnicityName, String.valueOf(nVal));
                }
            }

            struct.setEthnicities(ethnicities);
        }
    }

    @Override
    public String getSuccessView() {
        return _successView;
    }

    public void setSuccessView(String successView) {
        _successView = successView;
    }

    @Override
    protected ComparedSchoolBaseStruct getStruct() {
        return new ComparedSchoolStudentTeacherStruct();
    }

    public ICensusDataSetDao getCensusDataSetDao() {
        return _censusDataSetDao;
    }

    public void setCensusDataSetDao(ICensusDataSetDao censusDataSetDao) {
        _censusDataSetDao = censusDataSetDao;
    }

    public ICensusInfo getCensusInfo() {
        if (_censusInfo == null) {
            CensusInfoFactory censusInfoFactory = new CensusInfoFactory(getCensusDataSetDao());
            _censusInfo = censusInfoFactory.getCensusInfo();
        }
        return _censusInfo;
    }

    public void setCensusInfo(ICensusInfo censusInfo) {
        _censusInfo = censusInfo;
    }
}
