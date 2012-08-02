package gs.web.school;

import gs.data.school.School;
import gs.data.school.census.*;
import gs.data.school.district.District;
import gs.data.util.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * GroupedCensusDataSets = collections of CensusDataSets to be used to obtain school values, district values, and state values
 * <b> -Don't use this class with CensusDataSets that are attached to a hibernate session- </b>
 */
@Component(CensusDataHolder.BEAN_ID)
@Scope(BeanDefinition.SCOPE_PROTOTYPE) // a new instance each time spring is asked for this bean
public class CensusDataHolder {
    public static final String BEAN_ID = "censusDataHandler";

    @Autowired
    ICensusDataDistrictValueDao _censusDataDistrictValueDao;

    @Autowired
    private ICensusDataSchoolValueDao _censusDataSchoolValueDao;

    @Autowired
    ICensusDataStateValueDao _censusDataStateValueDao;

    private final School _school;

    private boolean _schoolDataDoneLoading;
    private boolean _districtDataDoneLoading;
    private boolean _stateDataDoneLoading;

    // CensusDataSet ID --> CensusDataSet
    private Map<Integer, CensusDataSet> _allCensusDataSets;
    private Map<Integer,CensusDataSet> _dataSetsForSchoolData;
    private Map<Integer,CensusDataSet>_dataSetsForDistrictData;
    private Map<Integer,CensusDataSet> _dataSetsForStateData;

    /**
     * When constructed this way, CensusDataHolder will retrieve all data for all provided CensusDataSets
     *
     * @param school
     * @param censusDataSets
     */
    public CensusDataHolder(School school, Map<Integer, CensusDataSet> censusDataSets) {
        this(school, censusDataSets, censusDataSets, censusDataSets, censusDataSets);
    }

    // Enhancement: allow multiple schools to be specified?
    public CensusDataHolder(School school, Map<Integer, CensusDataSet> allCensusDataSets, Map<Integer, CensusDataSet> dataSetsForSchoolData, Map<Integer, CensusDataSet> dataSetsForDistrictData, Map<Integer, CensusDataSet> dataSetsForStateData) {
        _school = school;
        _allCensusDataSets = allCensusDataSets;
        _dataSetsForSchoolData = dataSetsForSchoolData;
        _dataSetsForDistrictData = dataSetsForDistrictData;
        _dataSetsForStateData = dataSetsForStateData;
    }

    /**
     * @return the CensusDataSets which are specified as needing school data, with that respective school data loaded.
     * Returns map of CensusDataSet ID to CensusDataSet
     */
    public Map<Integer, CensusDataSet> retrieveDataSetsAndSchoolData() {
        loadSchoolDataIfNeeded();
        return _dataSetsForSchoolData;
    }

    private void loadSchoolDataIfNeeded() {
        if (!isSchoolDataDoneLoading()) {
            if (_dataSetsForSchoolData != null && !_dataSetsForSchoolData.isEmpty()) {
                _censusDataSchoolValueDao.addInSchoolCensusValues(_school.getDatabaseState(), _dataSetsForSchoolData.values(), ListUtils.newArrayList(_school));
                CensusDataHelper.putSchoolValueOverridesOntoCorrectDatasets(_dataSetsForSchoolData.values());
            }
            setSchoolDataDoneLoading(true);
        }
    }

    /**
     * @return the CensusDataSets which are specified as needing district data, with that respective district data loaded.
     * Returns map of CensusDataSet ID to CensusDataSet
     */
    public Map<Integer, CensusDataSet> retrieveDataSetsAndDistrictData() {
        loadDistrictDataIfNeeded();
        return _dataSetsForDistrictData;
    }

    private void loadDistrictDataIfNeeded() {
        if (!isDistrictDataDoneLoading()) {
            if (_dataSetsForDistrictData != null && !_dataSetsForDistrictData.isEmpty()) {
                District district = _school.getDistrict();
                if (district != null) {
                    _censusDataDistrictValueDao.addDistrictValuesToDataSets(
                        district.getDatabaseState(),
                        _dataSetsForDistrictData.values(),
                        ListUtils.newArrayList(district));
                }
            }
            setDistrictDataDoneLoading(true);
        }
    }

    /**
     * @return the CensusDataSets which are specified as needing state data, with that respective state data loaded.
     * Returns map of CensusDataSet ID to CensusDataSet
     */
    public Map<Integer, CensusDataSet> retrieveDataSetsAndStateData() {
        loadStateDataIfNeeded();
        return _dataSetsForStateData;
    }

    private void loadStateDataIfNeeded() {
        if (!isStateDataDoneLoading()) {
            if (_dataSetsForStateData != null && !_dataSetsForStateData.isEmpty()) {
                _censusDataStateValueDao.addStateValuesToDataSets(_school.getDatabaseState(), _dataSetsForStateData.values());
            }
            setStateDataDoneLoading(true);
        }
    }

    public Map<Integer, CensusDataSet> retrieveDataSetsAndAllData() {
        loadSchoolDataIfNeeded();
        loadSchoolDataIfNeeded();
        loadStateDataIfNeeded();
        return _allCensusDataSets;
    }

    private void setSchoolDataDoneLoading(boolean schoolDataDoneLoading) {
        _schoolDataDoneLoading = schoolDataDoneLoading;
    }

    private void setDistrictDataDoneLoading(boolean districtDataDoneLoading) {
        _districtDataDoneLoading = districtDataDoneLoading;
    }

    private void setStateDataDoneLoading(boolean stateDataDoneLoading) {
        _stateDataDoneLoading = stateDataDoneLoading;
    }

    public boolean isSchoolDataDoneLoading() { return _schoolDataDoneLoading; }
    public boolean isDistrictDataDoneLoading() { return _districtDataDoneLoading; }
    public boolean isStateDataDoneLoading() { return _stateDataDoneLoading; }
    public Map<Integer, CensusDataSet> getAllCensusDataSets() { return _allCensusDataSets; }
}
