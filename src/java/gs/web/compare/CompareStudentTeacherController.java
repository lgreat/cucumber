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

//    public List<List<CensusStruct>> getSchoolCensusData(State state, List<School> schools, String tab) {
        // 1) select out config rows
        // List<CompareConfig> compareConfigs = _compareConfigDao.find(state, tab, 'school')

        // 2) for each config row, retrieve the data set and label
        // List censusDataSets;
        // Map censusDataSetToLabel; // censusDataSet to compareLabel
        // Map rowLabelToOrder; // label level1 to orderNum from CompareConfig
        // Map censusDataSetToSchoolType; // censusDataSet to schoolType from CompareConfig
        // foreach compareConfig : compareConfigs {
        //  CensusDataSet censusDataSet = _censusDataSetDao.findDataSet
        //                              (state, censusDataType, grade, breakdown, levelCode, subject, year)
        //  CompareLabel compareLabel = _compareLabelDao.findLabel
        //                      (state, tabname, censusDataType, grade, breakdown, levelCode, subject);
        //  Populate censusDataSetToLabel, rowLabelToOrder, censusDataSetToSchoolType
        //  censusDataSets .add censusDataSet; // add censusDataSet to list
        // }

        // 3) bulk query: retrieve school values for each school and data set
        // List<SchoolCensusValue> schoolCensusValues =
        //              _censusDataSchoolValueDao.findSchoolCensusValues(state, censusDataSets, schools);
        //

        // 4) Populate return struct
        // Map schoolIdToIndex;
        // int index=1;
        // foreach (school: schools) {
        //  schoolIdToIndex.put(school.getId(), index++);
        // }
        // Map rowLabelToCellList; // map of row label to list of cells
        // foreach schoolCensusValue: schoolCensusValues {
        //  if (dataSet's schoolType is defined and not equal to school's type) {
        //      do nothing!
        //  } else {
        //      look up row and value label in censusDataSetToCompareLabelMap
        //      get list of cells from rowLabelToCellList map
        //      if null, create new cell list
        //          create static sized list with schools.size()+1 elements
        //          add header cell using row label to position 0
        //          add list to rowLabelToCellList
        //      Check list for existing cell in position -- if exists and dataSet's schoolType == null, continue
        //          This prevents a default value from overwriting a schoolType value
        //      How to handle breakdowns?
        //      populate cell with value and label (from censusDataSetToLabel map)
        //      add cell to cell list in position from schoolIdToIndex
        //  }
        // }

        // 5) Sort the rows
        // Collections.sort(rowLabelToCellList, new Comparator<String>() {
        //  int compare(String s1, String s2) {
        //      return rowLabelToOrder.get(s1).compareTo(rowLabelToOrder.get(s2);
        //  }
        // }

        // 6) return
        // return new ArrayList<List<CensusStruct>>(rowLabelToCellList.values());
//    }


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
