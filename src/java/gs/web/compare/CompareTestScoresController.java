package gs.web.compare;

import gs.data.compare.*;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.school.census.Breakdown;
//import gs.data.school.census.CensusDataSetType;
import gs.data.source.DataSetContentType;
import gs.data.state.State;
import gs.data.test.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CompareTestScoresController extends AbstractCompareSchoolController {
    private final Log _log = LogFactory.getLog(getClass());
    public static final String TAB_NAME = "testScores";
    public static final String COMPARE_CONFIG_TAB_NAME = "test";
    public static final String MODEL_TEST_ROWS = "testRows";
    private String _successView;
    private ICompareLabelDao _compareLabelDao;
    private ICompareConfigDao _compareConfigDao;
    private ITestDataTypeDao _testDataTypeDao;
    private ITestDataSetDao _testDataSetDao;
    private ITestDataSchoolValueDao _testDataSchoolValueDao;
    private ICompareLabelInfoDao _compareLabelInfoDao;

    @Override
    protected void handleCompareRequest(HttpServletRequest request, HttpServletResponse response,
                                        List<ComparedSchoolBaseStruct> structs, Map<String, Object> model) throws
                                                                                                           Exception {
        model.put(MODEL_TAB, TAB_NAME);

        if (structs.size() == 0) {
            return;
        }

        handleGSRating(request, structs);

        List<School> schools = new ArrayList<School>(structs.size());
        for (ComparedSchoolBaseStruct baseStruct: structs) {
            ComparedSchoolTestScoresStruct struct = (ComparedSchoolTestScoresStruct) baseStruct;
            schools.add(struct.getSchool());
            struct.setHasTestScores(determineTestScores(struct.getSchool()));
        }

        model.put(MODEL_TEST_ROWS, getSchoolTestData(schools.get(0).getDatabaseState(), schools,
                                                     COMPARE_CONFIG_TAB_NAME));
    }

    protected List<CompareConfigStruct[]> getSchoolTestData(State state, List<School> schools, String tab) {
        // initialize some maps necessary for this process
        // testDataSet to compareLabel
        Map<TestDataSet, CompareLabel> testDataSetToLabel = new HashMap<TestDataSet, CompareLabel>();
        // label level1 to orderNum from CompareConfig
        Map<String, Integer> rowLabelToOrder = new HashMap<String, Integer>();
        // testDataSet to schoolType from CompareConfig
        Map<TestDataSet, SchoolType> testDataSetToSchoolType = new HashMap<TestDataSet, SchoolType>();

        // 1) select out config rows
        List<CompareConfig> compareConfigs = getCompareConfigs(state, tab);
        if (compareConfigs == null || compareConfigs.size() == 0) {
            _log.error("Can't find compare config rows for " + state + ", " + tab);
            return new ArrayList<CompareConfigStruct[]>();
        }
        _log.warn("Found " + compareConfigs.size() + " compare configuration rows");

        // 2) for each config row, retrieve the data set and label
        // also populate the 3 maps
        List<TestDataSet> testDataSets =
                getTestDataSets(state, compareConfigs, testDataSetToLabel, rowLabelToOrder, testDataSetToSchoolType);
        if (testDataSets == null || testDataSets.size() == 0) {
            _log.error("Can't find test data sets for " + state + ", " + tab);
            return new ArrayList<CompareConfigStruct[]>();
        }
        _log.warn("Found " + testDataSets.size() + " test data sets");

        // 2.5) bulk query: Fetch out the label info for each label (e.g. info dialog URL)
        List<CompareLabel> rowLabels = new ArrayList<CompareLabel>(testDataSetToLabel.values());
        List<String> rowLabelStrings = new ArrayList<String>(rowLabels.size());
        for (CompareLabel label: rowLabels) {
            rowLabelStrings.add(label.getRowLabel());
        }
        Map<String, CompareLabelInfo> rowLabelToInfo = _compareLabelInfoDao.findLabelInfos(state, rowLabelStrings);
        _log.warn("Found " + rowLabelToInfo.size() + " row label info dialogs");

        // 3) bulk query: retrieve school values for each school and data set
        List<SchoolTestValue> schoolTestValues = _testDataSchoolValueDao.findSchoolTestValues(state, testDataSets,
                                                                                              schools);
        if (schoolTestValues == null || schoolTestValues.size() == 0) {
            _log.error("Can't find school test values for " + state + ", " + tab);
            return new ArrayList<CompareConfigStruct[]>();
        }
        _log.warn("Found " + schoolTestValues.size() + " school test values");

        // 4) Populate return struct
        // map is used here because all we have when populating each cell is a SchoolTestValue. From that
        // we can get the data set, and from that we can look up the row label where it is supposed to live.
        // With the row label, we use the map to pull out the specific row needed.
        Map<String, CompareConfigStruct[]> rowLabelToCellList =
                populateStructs(schools, schoolTestValues, testDataSetToSchoolType, testDataSetToLabel, rowLabelToInfo);
        _log.warn("Created " + rowLabelToCellList.size() + " rows");

        // 5) Sort the rows
        // 6) return
        return sortRows(rowLabelToCellList, rowLabelToOrder);
    }

    /**
     * 1) select out config rows
     */
    protected List<CompareConfig> getCompareConfigs(State state, String tab) {
        List<CompareConfig> compareConfigs = _compareConfigDao.getConfig(state, tab, DataSetContentType.getInstance("school"));
        if (compareConfigs == null || compareConfigs.size() == 0) {
            return null;
        }
        // process these rows, removing duplicates.
        // e.g. a state-specific configuration should override a default state configuration
        // create a map of a unique key (not using state) to the compare config
        Map<String, CompareConfig> uniqueConfigMap = new LinkedHashMap<String, CompareConfig>(compareConfigs.size());
        for (CompareConfig compareConfig: compareConfigs) {
            // copy all the compare configs in
            String uniqueKey = compareConfig.getDataTypeId() + ":" + compareConfig.getBreakdownId() + ":" +
                    compareConfig.getGrade() + ":" + compareConfig.getLevelCode() + ":" +
                    compareConfig.getSubject() + ":" + compareConfig.getSchoolType();
            CompareConfig existing = uniqueConfigMap.get(uniqueKey);
            // making sure not overwrite a value unless the new one has a state
            if (existing == null || compareConfig.getState() != null) {
                uniqueConfigMap.put(uniqueKey, compareConfig);
            }
        }

        // convert the map values back into a list and return
        return new ArrayList<CompareConfig>(uniqueConfigMap.values());
    }

    /**
     * 2) for each config row, retrieve the data set and label.
     * Also populate the 3 maps
     */
    protected List<TestDataSet> getTestDataSets(
            State state,
            List<CompareConfig> compareConfigs,
            Map<TestDataSet, CompareLabel> testDataSetToLabel,
            Map<String, Integer> rowLabelToOrder,
            Map<TestDataSet, SchoolType> testDataSetToSchoolType)
    {
        List <TestDataSet> testDataSets = new ArrayList<TestDataSet>();
        // foreach compareConfig
        for(CompareConfig config : compareConfigs){
            int dataTypeId = config.getDataTypeId();
            TestDataType testDataType = _testDataTypeDao.getDataType(dataTypeId);
            if (testDataType == null) {
                _log.warn("Can't find testDataType " + dataTypeId + " for compare_config row " + config.getId());
                continue;
            }
            Breakdown breakdown = null;
            if (config.getBreakdownId() != null) {
                breakdown = new Breakdown(config.getBreakdownId());
            }

            TestDataSet testDataSet = _testDataSetDao
                    .findDataSet(state, config.getYear(), testDataType.getId(), config.getSubject(), config.getGrade(),
                                 config.getBreakdownId(), null, true, config.getLevelCode());
            if (testDataSet == null) {
                _log.warn("Can't find data set corresponding to config row: " + config.getId() + ", data type " + testDataType.getName() + " " + testDataType.getDescription());
                continue;
            } else {
                _log.warn("Found data set corresponding to config row: " + config.getId() + ", data type " + testDataType.getName() + " " + testDataType.getDescription());
            }
            CompareLabel label = _compareLabelDao.findLabel(state,dataTypeId,config.getTabName(),config.getGrade(),breakdown,config.getLevelCode(),config.getSubject());
            if (label == null) {
                _log.warn("Can't find label corresponding to config row: " + config.getId());
                continue;
            }
            // add testDataSet to list
            testDataSets.add(testDataSet);
            // Populate testDataSetToLabel, rowLabelToOrder, testDataSetToSchoolType
            testDataSetToLabel.put(testDataSet,label);
            rowLabelToOrder.put(label.getRowLabel() + (label.getBreakdownLabel()!=null?label.getBreakdownLabel():""),config.getOrderNum());
            if (config.getSchoolType() != null) {
                testDataSetToSchoolType.put(testDataSet,config.getSchoolType());
            }
        }
        return testDataSets;
    }

    /**
     * 4) Populate return struct
     * map is used here because all we have when populating each cell is a SchoolCensusValue. From that
     * we can get the data set, and from that we can look up the row label where it is supposed to live.
     * With the row label, we use the map to pull out the specific row needed.
     * Although we ultimately need this to be ordered, it's easier to deal with right now as a map.
     */
    protected Map<String, CompareConfigStruct[]> populateStructs
            (List<School> schools,
             List<SchoolTestValue> schoolTestValues,
             Map<TestDataSet, SchoolType> testDataSetToSchoolTypeMap,
             Map<TestDataSet, CompareLabel> testDataSetToRowLabelMap,
             Map<String, CompareLabelInfo> rowLabelToInfoMap)
    {
        // map of row label to list of cells (school values)
        Map<String, CompareConfigStruct[]> rval = new HashMap<String, CompareConfigStruct[]>();
        if (schools == null || schoolTestValues == null || schools.isEmpty() || schoolTestValues.isEmpty()) {
            return rval; // early exit
        }
        // construct map of school to column for later ordering of cells
        // this is what tells us that when we have a schoolCensusValue for school #315 we should put the value
        // into column 2 for example
        Map<Integer, Integer> schoolIdToIndex = new HashMap<Integer, Integer>();
        int index = 1;
        for (School school : schools) {
            schoolIdToIndex.put(school.getId(), index++);
        }

        for(SchoolTestValue schoolTestValue : schoolTestValues){
            _log.warn("Processing schoolTestValue for school " + schoolTestValue.getSchool().getId() + " with value " +
                              getValueAsText(schoolTestValue) + " and year " + schoolTestValue.getDataSet().getYear());
            SchoolType schoolTypeOverride = testDataSetToSchoolTypeMap.get(schoolTestValue.getDataSet());
            // if the dataset is restricted to a school type, make sure it is applied only to schools
            // of that type.
            // Example: we want public schools to use 2009 data, private schools continue using 2008.
            // To prevent public schools from falling back on 2008 data, we mark that data set "private".
            // That way, if a public school has no value in 2009 data, it will get "N/A" rather than 2008 data.
            if (schoolTypeOverride != null &&
                    !schoolTypeOverride.equals(schoolTestValue.getSchool().getType())) {
                // do nothing!
                continue;
            }
            // look up row and value label in censusDataSetToLabelMap
            CompareLabel label = testDataSetToRowLabelMap.get(schoolTestValue.getDataSet());
            // get array of cells from rowLabelToCellList map
            CompareConfigStruct[] cells = rval.get(label.getRowLabel() + (label.getBreakdownLabel()!=null?label.getBreakdownLabel():""));
            // if null, create new cell list
            if (cells == null) {
                // create static sized array with schools.size()+1 elements
                cells = new CompareConfigStruct[(schools.size() + 1)];
                // add header cell using row label to position 0
                CompareConfigStruct headerCell = new CompareConfigStruct();
                headerCell.setIsHeaderCell(true);
                headerCell.setHeaderText(label.getRowLabel());
                CompareLabelInfo labelInfo = rowLabelToInfoMap.get(label.getRowLabel());
                if (labelInfo != null) {
                    headerCell.setExtraInfo(labelInfo.getLink());
                    _log.warn("Setting \"" + label.getRowLabel() + "\" to " + labelInfo.getLink());
                } else {
                    _log.warn("Can't find info for \"" + label.getRowLabel() + "\"");
                }
                headerCell.setBreakdownText(label.getBreakdownLabel());
                // header cell's year should be the most recent year represented in the row
                // start it off as the first cell's year, then below we will update it if we
                // find a more recent year
                headerCell.setYear(schoolTestValue.getDataSet().getYear());
                cells[0]=headerCell;
                // add list to rowLabelToCellList
                rval.put(label.getRowLabel() + (label.getBreakdownLabel()!=null?label.getBreakdownLabel():""), cells);
            }
            int cellIndex = schoolIdToIndex.get(schoolTestValue.getSchool().getId());
            CompareConfigStruct cell = cells[cellIndex];
            // Check list for existing cell in position -- if exists and it is more recent than the
            // current value, don't overwrite it
            if (cell != null && cell.getYear() > schoolTestValue.getDataSet().getYear()) {
                continue;
            }
            if (cell == null) {
                cell = new CompareConfigStruct();
            }
            // populate cell with value
            cell.setValue(getValueAsText(schoolTestValue));
            cell.setYear(schoolTestValue.getDataSet().getYear());
            cell.setIsSimpleCell(true);
            if (cell.getYear() > cells[0].getYear()) {
                cells[0].setYear(cell.getYear());
            }
            // add cell to cell list in position from schoolIdToIndex
            cells[cellIndex]=cell;
        }

        return rval;
    }

    /**
     * 5) Sort the rows
     * Sorts the CompareConfigStruct[]'s in the map per the order in rowLabelToOrder.
     * Also sorts each breakdown cell by value in descending order
     */
    public List<CompareConfigStruct[]> sortRows(Map<String, CompareConfigStruct[]> rowLabelToCells,
                                         final Map<String, Integer> rowLabelToOrder) {
        List<String> rowLabels = new LinkedList<String>(rowLabelToCells.keySet());
        Collections.sort(rowLabels, new Comparator<String>() {
            public int compare(String label1, String label2) {
                return (rowLabelToOrder.get(label1).compareTo(rowLabelToOrder.get(label2)));
            }
        });
        // now we know the order, let's populate a list with the rows in correct order
        List<CompareConfigStruct[]> rows = new ArrayList<CompareConfigStruct[]>();
        for (String label : rowLabels) {
            rows.add(rowLabelToCells.get(label));
        }
        return rows;
    }

    /**
     * Converts a value to a display format.
     */
    protected String getValueAsText(SchoolTestValue value) {
        if (value.getValueText() != null) {
            return value.getValueText();
        } else {
            TestDataType valueType = _testDataTypeDao.getDataType(value.getDataSet().getDataTypeId());
            if (valueType.isPercent()) {
                if (value.getValueFloat() >= 1f) {
                    return String.valueOf(Math.round(value.getValueFloat())) + "%";
                } else {
                    return "&lt;1%";
                }
            } else if (valueType.isNumeric()) {
                return String.valueOf(Math.round(value.getValueFloat()));
            } else {
                return String.valueOf(value.getValueFloat());
            }
        }
    }

    public boolean determineTestScores(School school) {
        boolean hasTestScores = true;
        if (StringUtils.equals("private", school.getType().getSchoolTypeName())) {
            hasTestScores = school.getStateAbbreviation().isPrivateTestScoresState() &&
                    _testDataSetDao.hasDisplayableData(school);
        } else if (LevelCode.PRESCHOOL.equals(school.getLevelCode())) {
            hasTestScores = false;
        }
        return hasTestScores;
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
        return new ComparedSchoolTestScoresStruct();
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

    public ITestDataTypeDao getTestDataTypeDao() {
        return _testDataTypeDao;
    }

    public void setTestDataTypeDao(ITestDataTypeDao testDataTypeDao) {
        _testDataTypeDao = testDataTypeDao;
    }

    public ITestDataSetDao getTestDataSetDao() {
        return _testDataSetDao;
    }

    public void setTestDataSetDao(ITestDataSetDao testDataSetDao) {
        _testDataSetDao = testDataSetDao;
    }

    public ITestDataSchoolValueDao getTestDataSchoolValueDao() {
        return _testDataSchoolValueDao;
    }

    public void setTestDataSchoolValueDao(ITestDataSchoolValueDao testDataSchoolValueDao) {
        _testDataSchoolValueDao = testDataSchoolValueDao;
    }

    public ICompareLabelInfoDao getCompareLabelInfoDao() {
        return _compareLabelInfoDao;
    }

    public void setCompareLabelInfoDao(ICompareLabelInfoDao compareLabelInfoDao) {
        _compareLabelInfoDao = compareLabelInfoDao;
    }
}
