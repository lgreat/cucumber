package gs.web.compare;

import gs.data.compare.CompareConfig;
import gs.data.compare.CompareLabel;
import gs.data.compare.ICompareConfigDao;
import gs.data.compare.ICompareLabelDao;
import gs.data.school.Grades;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.state.State;
import gs.data.school.census.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Teachers & Students tab on the new compare (2010)
 * 
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CompareStudentTeacherController extends AbstractCompareSchoolController {
    private final Log _log = LogFactory.getLog(getClass());
    public static final String TAB_NAME = "studentTeacher";
    public static final String COMPARE_CONFIG_TAB_NAME = "student_teacher";
    public static final String MODEL_CENSUS_ROWS = "censusRows";
    private String _successView;
    private ICensusDataSetDao _censusDataSetDao;
    private ICensusInfo _censusInfo;
    private ICompareLabelDao _compareLabelDao;
    private ICompareConfigDao _compareConfigDao;
    private ICensusDataSchoolValueDao _censusDataSchoolValueDao;

    @Override
    protected void handleCompareRequest(HttpServletRequest request, HttpServletResponse response,
                                        List<ComparedSchoolBaseStruct> structs, Map<String, Object> model) throws
                                                                                                           Exception {
        model.put(MODEL_TAB, TAB_NAME);

        if (structs.size() == 0) {
            return;
        }
        List<School> schools = new ArrayList<School>(structs.size());
        for (ComparedSchoolBaseStruct baseStruct: structs) {
            schools.add(baseStruct.getSchool());
        }

        model.put(MODEL_CENSUS_ROWS, getSchoolCensusData(schools.get(0).getDatabaseState(), schools, COMPARE_CONFIG_TAB_NAME));
    }

    protected List<CensusStruct[]> getSchoolCensusData(State state, List<School> schools, String tab) {
        // initialize some maps necessary for this process
        // censusDataSet to compareLabel
        Map<CensusDataSet, CompareLabel> censusDataSetToLabel = new HashMap<CensusDataSet, CompareLabel>();
        // label level1 to orderNum from CompareConfig
        Map<String, Integer> rowLabelToOrder = new HashMap<String, Integer>();
        // censusDataSet to schoolType from CompareConfig
        Map<CensusDataSet, SchoolType> censusDataSetToSchoolType = new HashMap<CensusDataSet, SchoolType>();

        // 1) select out config rows
        List<CompareConfig> compareConfigs = getCompareConfigs(state, tab);
        if (compareConfigs == null || compareConfigs.size() == 0) {
            _log.error("Can't find compare config rows for " + state + ", " + tab);
            return new ArrayList<CensusStruct[]>();
        }
        _log.warn("Found " + compareConfigs.size() + " compare configuration rows");

        // 2) for each config row, retrieve the data set and label
        // also populate the 3 maps
        List<CensusDataSet> censusDataSets =
                getCensusDataSets(state, compareConfigs, censusDataSetToLabel, rowLabelToOrder, censusDataSetToSchoolType);
        if (censusDataSets == null || censusDataSets.size() == 0) {
            _log.error("Can't find census data sets for " + state + ", " + tab);
            return new ArrayList<CensusStruct[]>();
        }
        _log.warn("Found " + censusDataSets.size() + " census data sets");

        // 3) bulk query: retrieve school values for each school and data set
        List<SchoolCensusValue> schoolCensusValues =
                _censusDataSchoolValueDao.findSchoolCensusValues(state, censusDataSets, schools);
        if (schoolCensusValues == null || schoolCensusValues.size() == 0) {
            _log.error("Can't find school census values for " + state + ", " + tab);
            return new ArrayList<CensusStruct[]>();
        }
        _log.warn("Found " + schoolCensusValues.size() + " school census values");

        // 4) Populate return struct
        // map is used here because all we have when populating each cell is a SchoolCensusValue. From that
        // we can get the data set, and from that we can look up the row label where it is supposed to live.
        // With the row label, we use the map to pull out the specific row needed.
        Map<String, CensusStruct[]> rowLabelToCellList =
                populateStructs(schools, schoolCensusValues, censusDataSetToSchoolType, censusDataSetToLabel);
        _log.warn("Created " + rowLabelToCellList.size() + " rows");

        // 5) Sort the rows
        // 6) return
        return sortRows(rowLabelToCellList, rowLabelToOrder);
    }

    /**
     * 1) select out config rows
     */
    protected List<CompareConfig> getCompareConfigs(State state, String tab) {
        List<CompareConfig> compareConfigs = _compareConfigDao.getConfig(state, tab, CensusDataSetType.SCHOOL);
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
    protected List<CensusDataSet> getCensusDataSets(
            State state,
            List<CompareConfig> compareConfigs,
            Map<CensusDataSet, CompareLabel> censusDataSetToLabel,
            Map<String, Integer> rowLabelToOrder,
            Map<CensusDataSet, SchoolType> censusDataSetToSchoolType)
    {
        List <CensusDataSet> censusDataSets = new ArrayList<CensusDataSet>();
        // foreach compareConfig
        for(CompareConfig config : compareConfigs){
            int dataTypeId = config.getDataTypeId();
            CensusDataType censusDataType = CensusDataType.getEnum(dataTypeId);
            Breakdown breakdown = null;
            if (config.getBreakdownId() != null) {
                breakdown = new Breakdown(config.getBreakdownId());
            }
            Grades grades = null;
            if (config.getGrade() != null) {
                grades = Grades.createGrades(config.getGrade());
            }
            CensusDataSet censusDataSet = _censusDataSetDao.findDataSet(state,censusDataType,config.getYear(),breakdown,config.getSubject(),config.getLevelCode(),
                                                                        grades);
            if (censusDataSet == null) {
                _log.warn("Can't find data set corresponding to config row: " + config.getId());
                continue;
            }
            CompareLabel label = _compareLabelDao.findLabel(state,dataTypeId,config.getTabName(),config.getGrade(),breakdown,config.getLevelCode(),config.getSubject());
            if (label == null) {
                _log.warn("Can't find label corresponding to config row: " + config.getId());
                continue;
            }
            // add censusDataSet to list
            censusDataSets.add(censusDataSet);
            //  Populate censusDataSetToLabel, rowLabelToOrder, censusDataSetToSchoolType
            censusDataSetToLabel.put(censusDataSet,label);
            rowLabelToOrder.put(label.getRowLabel(),config.getOrderNum());
            if (config.getSchoolType() != null) {
                censusDataSetToSchoolType.put(censusDataSet,config.getSchoolType());
            }
        }
        return censusDataSets;
    }

    /**
     * 4) Populate return struct
     * map is used here because all we have when populating each cell is a SchoolCensusValue. From that
     * we can get the data set, and from that we can look up the row label where it is supposed to live.
     * With the row label, we use the map to pull out the specific row needed.
     * Although we ultimately need this to be ordered, it's easier to deal with right now as a map.
     */
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
        // this is what tells us that when we have a schoolCensusValue for school #315 we should put the value
        // into column 2 for example
        Map<Integer, Integer> schoolIdToIndex = new HashMap<Integer, Integer>();
        int index = 1;
        for (School school : schools) {
            schoolIdToIndex.put(school.getId(), index++);
        }

        for(SchoolCensusValue schoolCensusValue : schoolCensusValues){
            SchoolType schoolTypeOverride = censusDataSetToSchoolTypeMap.get(schoolCensusValue.getDataSet());
            // if the dataset is restricted to a school type, make sure it is applied only to schools
            // of that type.
            // Example: we want public schools to use 2009 data, private schools continue using 2008.
            // To prevent public schools from falling back on 2008 data, we mark that data set "private".
            // That way, if a public school has no value in 2009 data, it will get "N/A" rather than 2008 data.
            if (schoolTypeOverride != null &&
                    !schoolTypeOverride.equals(schoolCensusValue.getSchool().getType())) {
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
                // header cell's year should be the most recent year represented in the row
                // start it off as the first cell's year, then below we will update it if we
                // find a more recent year
                headerCell.setYear(schoolCensusValue.getDataSet().getYear());
                cells[0]=headerCell;
                // add list to rowLabelToCellList
                rval.put(label.getRowLabel(), cells);
            }
            int cellIndex = schoolIdToIndex.get(schoolCensusValue.getSchool().getId());
            CensusStruct cell = cells[cellIndex];
            // Check list for existing cell in position -- if exists and it is more recent than the
            // current value, don't overwrite it
            if (cell != null && cell.getYear() > schoolCensusValue.getDataSet().getYear()) {
                continue;
            }
            if (cell == null) {
                cell = new CensusStruct();
            }
            if (label.getBreakdownLabel() != null) {
                // Initial proposed logic:
                // set cell.isSimpleCell = false
                cell.setIsSimpleCell(false);
                // if cell.breakdownList is null
                if(cell.getBreakdownList() == null){
                    // instantiate new list
                    cell.setBreakdownList(new ArrayList<BreakdownNameValue>());
                    if (schoolCensusValue.getDataSet().getDataType().equals(CensusDataType.HOME_LANGUAGE)) {
                        cell.setBreakdownValueMinimum(5);
                        cells[0].setBreakdownValueMinimum(5); // for a display note in the header cell
                    }
                } else if (cell.getYear() < schoolCensusValue.getDataSet().getYear()) {
                    // we found a more recent data set. clear out any values from the older data set
                    // Example: We have a school with two valid data sets (for some reason): 2008, 2009.
                    // As we loop through school values, we start seeing values from 2008 and adding them
                    // to the breakdown list. Once we come across a 2009 value, we realize that we want to
                    // use that data set instead of the 2008 one, so we delete all the 2008 values out to
                    // make room. Below we update the year on the cell to 2009 so we know to ignore any
                    // other 2008 values we come across.
                    cell.getBreakdownList().clear();
                }
                List<BreakdownNameValue> breakdowns = cell.getBreakdownList();
                // cell.breakdownList.add(new NameValuePair(label.breakdownLabel, schoolCensusValue.valueFloat))
                BreakdownNameValue breakdown = new BreakdownNameValue();
                breakdown.setName(label.getBreakdownLabel());
                breakdown.setValue(getValueAsText(schoolCensusValue));
                breakdown.setFloatValue(schoolCensusValue.getValueFloat()); // for sorting
                // Need to check if this label is set already in list
                if (!breakdowns.contains(breakdown)) {
                    breakdowns.add(breakdown);
                } else {
                    _log.warn("Duplicate data value detected for \"" + label.getBreakdownLabel() + "\", ignoring.");
                }
                cell.setYear(schoolCensusValue.getDataSet().getYear());
            } else {
                // populate cell with value and label (from censusDataSetToLabel map)
                cell.setValue(getValueAsText(schoolCensusValue));
                cell.setYear(schoolCensusValue.getDataSet().getYear());
                cell.setIsSimpleCell(true);
            }
            if (cell.getYear() > cells[0].getYear()) {
                cells[0].setYear(cell.getYear());
            }
            // add cell to cell list in position from schoolIdToIndex
            cells[cellIndex]=cell;
        }

        return rval;
    }

    /**
     * Converts a value to a display format.
     */
    protected String getValueAsText(SchoolCensusValue value) {
        if (value.getValueText() != null) {
            return value.getValueText();
        } else {
            CensusDataType.ValueType valueType = value.getDataSet().getDataType().getValueType();
            if (valueType == CensusDataType.ValueType.PERCENT) {
                if (value.getValueFloat() >= 1f) {
                    return String.valueOf(Math.round(value.getValueFloat())) + "%";
                } else {
                    return "&lt;1%";
                }
            } else if (valueType == CensusDataType.ValueType.NUMBER) {
                return String.valueOf(Math.round(value.getValueFloat()));
            } else {
                return String.valueOf(value.getValueFloat());
            }
        }
    }

    /**
     * 5) Sort the rows
     * Sorts the CensusStruct[]'s in the map per the order in rowLabelToOrder.
     * Also sorts each breakdown cell by value in descending order
     */
    public List<CensusStruct[]> sortRows(Map<String, CensusStruct[]> rowLabelToCells,
                                         final Map<String, Integer> rowLabelToOrder) {
        List<String> rowLabels = new LinkedList<String>(rowLabelToCells.keySet());
        Collections.sort(rowLabels, new Comparator<String>() {
            public int compare(String label1, String label2) {
                return (rowLabelToOrder.get(label1).compareTo(rowLabelToOrder.get(label2)));
            }
        });
        // now we know the order, let's populate a list with the rows in correct order
        // at the same time, we'll sort any breakdown cells by value desc
        List<CensusStruct[]> rows = new ArrayList<CensusStruct[]>();
        for (String label : rowLabels) {
            CensusStruct[] row = rowLabelToCells.get(label);
            rows.add(row);
            for (CensusStruct cell: row) {
                if (cell != null && cell.getBreakdownList() != null) {
                    Collections.sort(cell.getBreakdownList(), new Comparator<BreakdownNameValue>() {
                        // sort by value descending
                        public int compare(BreakdownNameValue o1, BreakdownNameValue o2) {
                            if (o1.getFloatValue() != null && o2.getFloatValue() != null) {
                                return o2.getFloatValue().compareTo(o1.getFloatValue());
                            }
                            return o2.getValue().compareTo(o1.getValue());
                        }
                    });
                    // Also, at this point let's enforce the breakdownValueMinimum, if any.
                    // This rule is a display requirement to keep long lists (of, say, home languages)
                    // from getting out of hand. It removes any values below or equal to a cutoff.
                    if (cell.getBreakdownValueMinimum() > 0) {
                        int index = 0;
                        for (BreakdownNameValue pair: cell.getBreakdownList()) {
                            if (Math.round(pair.getFloatValue()) <= cell.getBreakdownValueMinimum()) {
                                break;
                            }
                            index++;
                        }
                        if (index < cell.getBreakdownList().size()) {
                            cell.setBreakdownList(cell.getBreakdownList().subList(0,index));
                        }
                    }
                }
            }
        }
        return rows;
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

    public ICensusDataSchoolValueDao getCensusDataSchoolValueDao() {
        return _censusDataSchoolValueDao;
    }

    public void setCensusDataSchoolValueDao(ICensusDataSchoolValueDao censusDataSchoolValueDao) {
        _censusDataSchoolValueDao = censusDataSchoolValueDao;
    }
}