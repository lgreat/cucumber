package gs.web.compare;

import gs.data.compare.*;
import gs.data.school.*;
import gs.data.source.DataSetContentType;
import gs.data.state.State;
import gs.data.school.census.*;
import gs.data.test.Subject;
import org.apache.commons.lang.StringUtils;
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
    private ICompareLabelInfoDao _compareLabelInfoDao;
    private static Map<Integer, Integer> _dataTypeIdToOrderMap = new HashMap<Integer, Integer>() {
        {
            int order = 1;
            put(35, order++);
            put(5, order++);
            put(9, order++);
            put(95, order++);
            put(16, order++);
            put(6, order++);
            put(13, order++);
            put(15, order++);
            put(22, order++);
            put(14, order++);
            put(97, order++);
            put(7, order++);
            put(10, order++);
            put(112, order++);
            put(11, order++);
            put(114, order++);
            put(33, order++);
            put(26, order++);
            put(111, order++);
            put(12, order++);
            put(103, order++);
            put(1, order++);
            put(30, order++);
        }
    };

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

    protected List<CompareConfigStruct[]> getSchoolCensusData(State state, List<School> schools, String tab) {
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
            return new ArrayList<CompareConfigStruct[]>();
        }
        _log.warn("Found " + compareConfigs.size() + " compare configuration rows");

        // 2) for each config row, retrieve the data set and label
        // also populate the 3 maps
        List<CensusDataSet> censusDataSets =
                getCensusDataSets(state, compareConfigs, censusDataSetToLabel, rowLabelToOrder, censusDataSetToSchoolType);
        if (censusDataSets == null || censusDataSets.size() == 0) {
            _log.error("Can't find census data sets for " + state + ", " + tab);
            return new ArrayList<CompareConfigStruct[]>();
        }
        _log.warn("Found " + censusDataSets.size() + " census data sets");

        // Aroy: Temporarily disable info dialog code
        // 2.5) bulk query: Fetch out the label info for each label (e.g. info dialog URL)
//        List<String> rowLabels = new ArrayList<String>(rowLabelToOrder.keySet());
//        Map<String, CompareLabelInfo> rowLabelToInfo = _compareLabelInfoDao.findLabelInfos(state, rowLabels);
        Map<String, CompareLabelInfo> rowLabelToInfo = new HashMap<String, CompareLabelInfo>();

        // 3) bulk query: retrieve school values for each school and data set
        Collection<SchoolCensusValue> schoolCensusValues = retrieveSchoolCensusValues(state, censusDataSets, schools, censusDataSetToSchoolType);
        if (schoolCensusValues == null || schoolCensusValues.size() == 0) {
            _log.error("Can't find school census values for " + state + ", " + tab);
            return new ArrayList<CompareConfigStruct[]>();
        }

        // 4) Populate return struct
        // map is used here because all we have when populating each cell is a SchoolCensusValue. From that
        // we can get the data set, and from that we can look up the row label where it is supposed to live.
        // With the row label, we use the map to pull out the specific row needed.
        Map<String, CompareConfigStruct[]> rowLabelToCellList =
                populateStructs(schools, schoolCensusValues, censusDataSetToLabel, rowLabelToInfo);
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
//    protected List<CensusDataSet> getCensusDataSets(
//            State state,
//            List<CompareConfig> compareConfigs,
//            Map<CensusDataSet, CompareLabel> censusDataSetToLabel,
//            Map<String, Integer> rowLabelToOrder,
//            Map<CensusDataSet, SchoolType> censusDataSetToSchoolType)
//    {
//        List <CensusDataSet> censusDataSets = new ArrayList<CensusDataSet>();
//        // foreach compareConfig
//        for(CompareConfig config : compareConfigs){
//            int dataTypeId = config.getDataTypeId();
//            CensusDataType censusDataType = CensusDataType.getEnum(dataTypeId);
//            Breakdown breakdown = null;
//            if (config.getBreakdownId() != null) {
//                breakdown = new Breakdown(config.getBreakdownId());
//            }
//            Grades grades = null;
//            if (config.getGrade() != null) {
//                grades = Grades.createGrades(config.getGrade());
//            }
//
//            CompareLabel label = _compareLabelDao.findLabel(state,dataTypeId,config.getTabName(),config.getGrade(),breakdown,config.getLevelCode(),config.getSubject());
//            if (label == null) {
//                _log.error("Can't find label corresponding to config row: " + config.getId());
//                continue;
//            }
//
//            CensusDataSet censusDataSet = _censusDataSetDao.findDataSet(state,censusDataType,config.getYear(),breakdown,config.getSubject(),config.getLevelCode(),
//                    grades);
//            if (censusDataSet != null) {
//                // add censusDataSet to list
//                censusDataSets.add(censusDataSet);
//                //  Populate censusDataSetToLabel, rowLabelToOrder, censusDataSetToSchoolType
//                censusDataSetToLabel.put(censusDataSet,label);
//            }
//            // GS-13037-Add manual override data - that would be a dataset with year=0
//            CensusDataSet censusDataSetOverride = _censusDataSetDao.findDataSet(state,censusDataType,0,breakdown,config.getSubject(),config.getLevelCode(),
//                    grades);
//            if( censusDataSetOverride != null ) {
//                censusDataSets.add(censusDataSetOverride);
//                censusDataSetToLabel.put(censusDataSetOverride,label);
//            }
//            if (censusDataSet == null && censusDataSetOverride == null) {
//                _log.warn("Can't find data set corresponding to config row: " + config.getId());
//                continue;
//            }
//
////            rowLabelToOrder.put(label.getRowLabel(),config.getOrderNum());
//            // GS-10784: use static mapping across all states. Ignore config's order_num
//            rowLabelToOrder.put(label.getRowLabel(), getOrderForDataType(config.getDataTypeId()));
//            if (config.getSchoolType() != null) {
//                censusDataSetToSchoolType.put(censusDataSet,config.getSchoolType());
//            }
//        }
//        return censusDataSets;
//    }

    /**
     * 2) for each config row, retrieve the data set and label.
     * Also populate the 3 maps
     * This version uses a bulk query
     */
    protected List<CensusDataSet> getCensusDataSets(
            State state,
            List<CompareConfig> compareConfigs,
            Map<CensusDataSet, CompareLabel> censusDataSetToLabel,
            Map<String, Integer> rowLabelToOrder,
            Map<CensusDataSet, SchoolType> censusDataSetToSchoolType)
    {
        List <CensusDataSet> censusDataSets = new ArrayList<CensusDataSet>();

        // get all of the dataTypeId's and years
        Set<CensusDataType> censusDataTypes = new HashSet<CensusDataType>();
        Set<Integer> censusDataTypeIds = new HashSet<Integer>();
        Set<Integer> years = new HashSet<Integer>();
        boolean hasNullYear = false;
        for(CompareConfig config : compareConfigs){
            Integer dataTypeId = config.getDataTypeId();
            censusDataTypeIds.add(dataTypeId);
            CensusDataType censusDataType = CensusDataType.getEnum(dataTypeId);
            censusDataTypes.add(censusDataType);
            Integer year = config.getYear();
            if( year == null ) {
                hasNullYear = true;
            }
            else {
                years.add(year);
            }
        }
        if( hasNullYear ) {
            years = null;
        } else {
            years.add(0); // always inlude manual override data sets
        }

        if (censusDataTypes.isEmpty()) {
            return censusDataSets;
        }
        // Get all of the CensusDataSets
        List<CensusDataSet> allCensusDataSets = _censusDataSetDao.findDataSets( state, censusDataTypes, years );

        if (allCensusDataSets == null || allCensusDataSets.isEmpty()) {
            return censusDataSets;
        }

        // Get all labels
        List<CompareLabel> labels = _compareLabelDao.findLabels(state, compareConfigs.get(0).getTabName(), censusDataTypeIds);
        if (labels == null || labels.isEmpty()) {
            _log.error("Can't find any census compare labels for " + state);
            return censusDataSets;
        }

        // foreach compareConfig
        for(CompareConfig config : compareConfigs){
            // year is set to -1 to override a national config row and exclude the data type
            if (config.getYear() != null && config.getYear() == -1) {
                continue;
            }

            CompareLabel label = findCompareLabel( config, labels );
            if (label == null) {
                _log.error("Can't find label corresponding to config row: " + config.getId());
                continue;
            }

            CensusDataSet censusDataSet = findCensusDataSet(config, allCensusDataSets, null);
            if (censusDataSet != null) {
                censusDataSets.add(censusDataSet);
                censusDataSetToLabel.put(censusDataSet,label);
            }

            CensusDataSet censusDataSetOverride = findCensusDataSet(config, allCensusDataSets, 0);
            if( censusDataSetOverride != null ) {
                censusDataSets.add(censusDataSetOverride);
                censusDataSetToLabel.put(censusDataSetOverride,label);
            }
            if (censusDataSet == null && censusDataSetOverride == null) {
                _log.warn("Can't find data set corresponding to config row: " + config.getId());
                continue;
            }

//            rowLabelToOrder.put(label.getRowLabel(),config.getOrderNum());
            // GS-10784: use static mapping across all states. Ignore config's order_num
            rowLabelToOrder.put(label.getRowLabel(), getOrderForDataType(config.getDataTypeId()));
            if (config.getSchoolType() != null) {
                censusDataSetToSchoolType.put(censusDataSet,config.getSchoolType());
            }
        }

        return censusDataSets;
    }

    private CensusDataSet findCensusDataSet(CompareConfig config, List<CensusDataSet> censusDataSets, Integer year) {
        // Datatype ID - which is required
        int dataTypeId = config.getDataTypeId();
        CensusDataType censusDataType = CensusDataType.getEnum(dataTypeId);
        Integer configBreakdownId = config.getBreakdownId();
        Integer configYear = (year != null) ? year : config.getYear();
        Subject configSubject = config.getSubject();
        LevelCode configLevelCode = config.getLevelCode();
        Grade configGrade = config.getGrade();

        for( CensusDataSet censusDataSet : censusDataSets ) {
            if( ! censusDataType.equals(censusDataSet.getDataType()) ) {
                continue;
            }

            // If configBreakdownId is present it must match
            if( configBreakdownId != null ) {
                Integer censusBreakdownId = censusDataSet.getBreakdown().getId();
                if( censusBreakdownId == null || !censusBreakdownId.equals(configBreakdownId)) {
                    continue;
                }
            }

            // year
            if( configYear != null ) {
                Integer censusYear = censusDataSet.getYear();
                if( ! censusYear.equals(configYear) ) {
                    continue;
                }
            }

            // If subject is present ...
            if( configSubject != null ) {
                Subject censusSubject = censusDataSet.getSubject();
                if( censusSubject == null || !censusSubject.equals(configSubject)) {
                    continue;
                }
            }

            // If configLevelCode is present ...
            if( configLevelCode != null ) {
                LevelCode censusLevelCode = censusDataSet.getLevelCode();
                if( censusLevelCode == null || !censusLevelCode.equals(configLevelCode)) {
                    continue;
                }
            }

            // If configGrade is present then have to find matching label grade
            if( configGrade != null ) {
                Grades censusGrades = censusDataSet.getGradeLevels();
                if( censusGrades ==  null || !censusGrades.contains(configGrade) ) {
                    continue;  // No match
                }
            }

            return censusDataSet;
        }

        return null;  // No match
    }

    private CompareLabel findCompareLabel(CompareConfig config, List<CompareLabel> labels) {
        Integer configBreakdownId = config.getBreakdownId();
        Grade configGrade = config.getGrade();
        LevelCode configLevelCode = config.getLevelCode();
        Subject configSubject = config.getSubject();

        for( CompareLabel label : labels ) {
            // Datatype ID - which is required
            if( ! config.getDataTypeId().equals(label.getDataTypeId()) ) {
                continue;
            }

            // If configBreakdownId is present it must match the label breakdownId
            if( configBreakdownId != null ) {
                Integer labelBreakdownId = label.getBreakdownId();
                if( labelBreakdownId == null || !labelBreakdownId.equals(configBreakdownId)) {
                    continue;
                }
            }

            // If configGrade is present then have to find matching label grade
            if( configGrade != null ) {
                Grade labelGrade = label.getGrade();
                if( labelGrade ==  null || !labelGrade.equals(configGrade) ) {
                    continue;  // No match
                }
            }

            // If configLevelCode is present ...
            if( configLevelCode != null ) {
                LevelCode labelLevelCode = label.getLevelCode();
                if( labelLevelCode == null || !labelLevelCode.equals(configLevelCode)) {
                    continue;
                }
            }

            // If subject is present ...
            if( configSubject != null ) {
                Subject labelSubject = label.getSubject();
                if( labelSubject == null || !labelSubject.equals(configSubject)) {
                    continue;
                }
            }

            return label;
        }

        return null;  // No match
    }

    /**
     * 3) retrieve the SchoolCensusValues for the specified datasets and return only the set with the correct years
     * The general approach is to sort SchoolCensusValues descending and then pick the first one of each type.
     * Datasets with breakdowns are an added complication because each breakdown must have the same timestamp.
     */
    protected Collection<SchoolCensusValue> retrieveSchoolCensusValues(State state, List<CensusDataSet> censusDataSets, List<School> schools, Map<CensusDataSet, SchoolType> censusDataSetToSchoolTypeMap ) {
        List<SchoolCensusValue> schoolCensusValues =
                _censusDataSchoolValueDao.findSchoolCensusValues(state, censusDataSets, schools);
        _log.warn("Found " + schoolCensusValues.size() + " school census values");
        Collections.sort(schoolCensusValues, SCHOOL_CENSUS_VALUE_DESCENDING);
        // Put the values into a map that has a unique key for each cell and breakdown
        Map<String, SchoolCensusValue> desired = new HashMap<String, SchoolCensusValue>();
        // The following Map is used to make sure each breakdown value come from a SCV with the same time
        Map<String, Date> breakdownIdentifierMap = new HashMap<String, Date>();
        for( SchoolCensusValue scv : schoolCensusValues ) {
            SchoolType schoolTypeOverride = censusDataSetToSchoolTypeMap.get(scv.getDataSet());
            // if the dataset is restricted to a school type, make sure it is applied only to schools
            // of that type.
            // Example: we want public schools to use 2009 data, private schools continue using 2008.
            // To prevent public schools from falling back on 2008 data, we mark that data set "private".
            // That way, if a public school has no value in 2009 data, it will get "N/A" rather than 2008 data.
            if (schoolTypeOverride != null &&
                    !schoolTypeOverride.equals(scv.getSchool().getType())) {
                // do nothing!
                continue;
            }
            String key = scv.getSchool().getId() + ":" + scv.getDataSet().getDataType().getId();
            if( scv.getDataSet().getBreakdown() != null ) {
                // Since this SCV has a breakdown make sure it is from the latest set
                String breakdownKey = new String(key);
                Date effectiveDate = scvEffectiveDate(scv);
                if( breakdownIdentifierMap.containsKey(breakdownKey) ) {
                    // Make sure this SCV has the same identifier
                    if( ! breakdownIdentifierMap.get(breakdownKey).equals(effectiveDate))  {
                        continue;       // Not from this set
                    }
                }
                else {
                    breakdownIdentifierMap.put(breakdownKey, effectiveDate);
                }
                key += ":" + scv.getDataSet().getBreakdown().getId();
            }
            // If the Map already contains a value for this key then it is the latest and skip this one
            if( !desired.containsKey(key) ) {
                desired.put(key, scv);
            }
        }
        // Now the Map contains the latest values, just return the values
        return desired.values();
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
             Collection<SchoolCensusValue> schoolCensusValues,
             Map<CensusDataSet, CompareLabel> censusDataSetToRowLabelMap,
             Map<String, CompareLabelInfo> rowLabelToInfoMap)
    {
        // map of row label to list of cells (school values)
        Map<String, CompareConfigStruct[]> rval = new HashMap<String, CompareConfigStruct[]>();
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
            // look up row and value label in censusDataSetToLabelMap
            CompareLabel label = censusDataSetToRowLabelMap.get(schoolCensusValue.getDataSet());
            // get array of cells from rowLabelToCellList map
            CompareConfigStruct[] cells = rval.get(label.getRowLabel());
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
                }
                // header cell's year should be the most recent year represented in the row
                // start it off as the first cell's year, then below we will update it if we
                // find a more recent year
                headerCell.setYear(schoolCensusValue.getDataSet().getYear());
                cells[0]=headerCell;
                // add list to rowLabelToCellList
                rval.put(label.getRowLabel(), cells);
            }
            int cellIndex = schoolIdToIndex.get(schoolCensusValue.getSchool().getId());
            CompareConfigStruct cell = cells[cellIndex];
            if (cell == null) {
                // If the cell is null then create it and populate it with the code below.
                cell = new CompareConfigStruct();
            }
            if (StringUtils.isNotBlank(label.getBreakdownLabel())) {
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
                setCellDates(cell, schoolCensusValue);
            } else {
                // populate cell with value and label (from censusDataSetToLabel map)
                cell.setValue(getValueAsText(schoolCensusValue));
                cell.setYear(schoolCensusValue.getDataSet().getYear());
                setCellDates(cell, schoolCensusValue);
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

    private void setCellDates(CompareConfigStruct cell, SchoolCensusValue schoolCensusValue) {
        cell.setYear(schoolCensusValue.getDataSet().getYear());
        Date lastModified = schoolCensusValue.getModified();
        if( cell.getYear() == 0 && lastModified != null ) {
            Calendar lastModifiedCal = Calendar.getInstance();
            lastModifiedCal.setTime(lastModified);
            cell.setYear(lastModifiedCal.get(Calendar.YEAR));
        }
        cell.setLastModified(lastModified);
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
        // at the same time, we'll sort any breakdown cells by value desc
        List<CompareConfigStruct[]> rows = new ArrayList<CompareConfigStruct[]>();
        for (String label : rowLabels) {
            CompareConfigStruct[] row = rowLabelToCells.get(label);
            rows.add(row);
            for (CompareConfigStruct cell: row) {
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

    protected Integer getOrderForDataType(Integer dataTypeId) {
        Integer rval = _dataTypeIdToOrderMap.get(dataTypeId);
        return (rval == null) ? 0 : rval;
    }

    // Used to sort SchoolCensusValue descending and considers if there is a modified and if should be used.
    public static Comparator<SchoolCensusValue> SCHOOL_CENSUS_VALUE_DESCENDING = new Comparator<SchoolCensusValue>() {
        public int compare(SchoolCensusValue schoolCensusValue1, SchoolCensusValue schoolCensusValue2) {
            Date modified1 = scvEffectiveDate( schoolCensusValue1 );
            Date modified2 = scvEffectiveDate( schoolCensusValue2 );
            return modified2.compareTo(modified1);
        }

    };

    private static Date scvEffectiveDate(SchoolCensusValue schoolCensusValue) {

        if( schoolCensusValue.getModified() != null ) {
            return  schoolCensusValue.getModified();
        }
        // have to calc a date based on the year.  When comparing SCV with modified date the comparison is the the 10/1/(yr-1)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, schoolCensusValue.getDataSet().getYear() - 1);
        cal.set(Calendar.MONTH, Calendar.OCTOBER);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        //noinspection MagicNumber
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
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

    public ICompareLabelInfoDao getCompareLabelInfoDao() {
        return _compareLabelInfoDao;
    }

    public void setCompareLabelInfoDao(ICompareLabelInfoDao compareLabelInfoDao) {
        _compareLabelInfoDao = compareLabelInfoDao;
    }

    // For unit tests
    protected void setDataTypeIdToOrderMap(Map<Integer, Integer> dataTypeIdToOrderMap) {
        _dataTypeIdToOrderMap = dataTypeIdToOrderMap;
    }

    @Override
    protected String getTabName() {
        return TAB_NAME;
    }
}