package gs.web.compare;

import gs.data.compare.CompareConfig;
import gs.data.compare.CompareLabel;
import gs.data.compare.ICompareConfigDao;
import gs.data.compare.ICompareLabelDao;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.state.State;
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
    private ICompareLabelDao _compareLabelDao;
    private ICompareConfigDao _compareConfigDao;



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

   public List<CompareConfig> getCompareConfig(State state,String tabName){
       List<CompareConfig> compareConfigs = _compareConfigDao.getConfig(state,tabName,CensusDataSetType.SCHOOL);
       return compareConfigs;
   }

    public void buildDataStructs(List<CompareConfig> compareConfigs){
        List <CensusDataSet> censusDataSets = new ArrayList<CensusDataSet>();
        Map<CensusDataSet, CompareLabel>  censusDataSetToLabel = new HashMap<CensusDataSet, CompareLabel>();
        Map<CompareLabel,Integer> rowLabelToOrder = new HashMap<CompareLabel,Integer>();
        Map<CensusDataSet,SchoolType> censusDataSetToSchoolType = new HashMap<CensusDataSet,SchoolType>();
        for(CompareConfig config : compareConfigs){
            int dataTypeId = config.getDataTypeId();
            CensusDataType censusDataType = CensusDataType.getEnum(dataTypeId);
            Breakdown breakdown = new Breakdown(config.getBreakdownId());
            CensusDataSet censusDataSet = _censusDataSetDao.findDataSet(config.getState(),censusDataType,config.getYear(),breakdown,config.getSubject(),config.getLevelCode(),config.getGradeLevels());
            censusDataSets.add(censusDataSet);
            CompareLabel label = _compareLabelDao.findLabel(config.getState(),censusDataType,config.getTabName(),config.getGradeLevels(),breakdown,config.getLevelCode(),config.getSubject());
            censusDataSetToLabel.put(censusDataSet,label);
            rowLabelToOrder.put(label,config.getOrderNum());
            censusDataSetToSchoolType.put(censusDataSet,config.getSchoolType());
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

    /**
     * 4) Populate return struct
     */
    // TODO: Verify that schoolCensusValue.valueFloat is the only value we're interested in, vs. valueText
    protected Map<String, CensusStruct[]> populateStructs
            (List<School> schools,
             List<SchoolCensusValue> schoolCensusValues,
             Map<CensusDataSet, SchoolType> censusDataSetToSchoolTypeMap,
             Map<CensusDataSet, CompareLabel> censusDataSetToRowLabelMap)
    {
        // map of row label to list of cells (school values)
        Map<String, CensusStruct[]> rval = new HashMap<String, CensusStruct[]>();
        if (schools == null || schoolCensusValues == null || schools.isEmpty() || schoolCensusValues.isEmpty()) {
            return rval; // early exit
        }
        // construct map of school to column for later ordering of cells
        Map<Integer, Integer> schoolIdToIndex = new HashMap<Integer, Integer>();
        int index = 1;
        for (School school : schools) {
            schoolIdToIndex.put(school.getId(), index++);
        }

        // foreach schoolCensusValue: schoolCensusValues {
        for(SchoolCensusValue schoolCensusValue : schoolCensusValues){
            SchoolType schoolTypeOverride = censusDataSetToSchoolTypeMap.get(schoolCensusValue.getDataSet());
            //  if (dataSet's schoolType is defined and not equal to school's type) {
            if (schoolTypeOverride != null &&
                    !schoolTypeOverride.equals(schoolCensusValue.getSchool().getType())) {
                System.out.println("School already has value, not overriding");
                // do nothing!
                continue;
            }
            // look up row and value label in censusDataSetToLabelMap
            CompareLabel label = censusDataSetToRowLabelMap.get(schoolCensusValue.getDataSet());
            // get array of cells from rowLabelToCellList map
            CensusStruct[] cells = rval.get(label.getRowLabel());
            // if null, create new cell list
            if (cells == null) {
                // create static sized array with schools.size()+1 elements
                cells = new CensusStruct[(schools.size() + 1)];
                // add header cell using row label to position 0
                CensusStruct headerCell = new CensusStruct();
                headerCell.setIsHeaderCell(true);
                headerCell.setHeaderText(label.getRowLabel());
                cells[0]=headerCell;
                // add list to rowLabelToCellList
                rval.put(label.getRowLabel(), cells);
            }
            int cellIndex = schoolIdToIndex.get(schoolCensusValue.getSchool().getId());
            CensusStruct cell = cells[cellIndex];
            //  Check list for existing cell in position -- if exists and dataSet's schoolType == null, continue
            //  This prevents a default value from overwriting a schoolType value
            if (cell != null && schoolTypeOverride == null && label.getBreakdownLabel() == null) {
                continue;
            }
            if (cell == null) {
                cell = new CensusStruct();
            }
            if (label.getBreakdownLabel() != null) {
                // TODO: How to handle breakdowns?
                // Initial proposed logic:
                // set cell.isSimpleCell = false
                // if cell.breakdownMap is null
                //   instantiate new map
                // cell.breakdownMap.put(label.breakdownLabel, schoolCensusValue.valueFloat)

                // Problem: This puts them in out-of-order
                // Question: Why is cell.breakdownMap a map anyway?
                // Seems like we want it to be an ordered list.
                // Could it instead be an ordered list of key/value pairs (e.g. gs.data.util.NameValuePair)?
                // If it were defined as a List<NameValuePair>, then a sort would be trivial based
                // on the NameValuePair's .value.
                // In that case, the modified logic would be:
                // set cell.isSimpleCell = false
                // if cell.breakdownList is null
                //   instantiate new list
                // cell.breakdownList.add(new NameValuePair(label.breakdownLabel, schoolCensusValue.valueFloat))
                // at some point after this loop, we'd loop through each cell in every row, and if it is
                // a breakdown cell call Collections.sort(cell.breakdownList, Comparator<NameValuePair>)
                cell.setIsSimpleCell(false);
                List<BreakdownNameValue> breakdowns = new ArrayList();

                if(cell.getBreakdownList() != null){
                    breakdowns = cell.getBreakdownList();
                }
                BreakdownNameValue breakdown = new BreakdownNameValue();
                breakdown.setName(label.getBreakdownLabel());
                breakdown.setValue(String.valueOf(Math.round(schoolCensusValue.getValueFloat())));
                breakdowns.add(breakdown);
                cell.setBreakdownList(breakdowns);
            } else {
                // populate cell with value and label (from censusDataSetToLabel map)
                // TODO: Does how we format the value depend on schoolCensusValue.dataSet.dataType.valueType?
                cell.setValue(String.valueOf(Math.round(schoolCensusValue.getValueFloat())));
                cell.setIsSimpleCell(true);
            }
            // add cell to cell list in position from schoolIdToIndex
            cells[cellIndex]=cell;
        }

        return rval;
    }

    //TODO:better way to sort a Map based on another Map?
    public LinkedHashMap<String, CensusStruct[]> sortRows(Map<String, CensusStruct[]> rowLabelToCells, final Map<String, String> rowLabelToOrder) {
        List<String> rowLabels = new LinkedList<String>(rowLabelToCells.keySet());
        Collections.sort(rowLabels, new Comparator<String>() {
            public int compare(String label1, String label2) {
                return (rowLabelToOrder.get(label1).compareTo(rowLabelToOrder.get(label2)));
            }
        });
        //put sorted list into map again
        LinkedHashMap<String, CensusStruct[]> sortedMap = new LinkedHashMap<String, CensusStruct[]>();
        for (String label : rowLabels) {
            sortedMap.put(label, rowLabelToCells.get(label));
        }
        return sortedMap;
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
    
     public ICompareLabelDao getCompareLabelDao() {
        return _compareLabelDao;
    }

    public void setCompareLabelDao(ICompareLabelDao compareLabelDao) {
        _compareLabelDao = compareLabelDao;
    }

    public ICompareConfigDao getCompareConfigDao() {
        return _compareConfigDao;
    }

    public void setCompareConfigDao(ICompareConfigDao compareConfigDao) {
        _compareConfigDao = compareConfigDao;
    }
    
//    protected static class CompareLabel{
//        private String rowLabel;
//        private String breakdownLabel;
//
//        public String getRowLabel() {
//            return rowLabel;
//        }
//
//        public void setRowLabel(String rowLabel) {
//            this.rowLabel = rowLabel;
//        }
//
//        public String getBreakdownLabel() {
//            return breakdownLabel;
//        }
//
//        public void setBreakdownLabel(String breakdownLabel) {
//            this.breakdownLabel = breakdownLabel;
//        }
//    }

}
