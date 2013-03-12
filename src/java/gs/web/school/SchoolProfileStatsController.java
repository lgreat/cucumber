package gs.web.school;


import gs.data.admin.IPropertyDao;
import gs.data.school.EspResponse;
import gs.data.school.School;
import gs.data.school.census.*;
import gs.data.util.CollectionUtils;
import gs.data.util.ListUtils;
import gs.web.util.ReadWriteAnnotationController;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;

@Controller
@RequestMapping("/school/profileStats.page")
public class SchoolProfileStatsController extends AbstractSchoolProfileController implements ReadWriteAnnotationController {

    @Autowired
    SchoolProfileDataHelper _schoolProfileDataHelper;
    @Autowired
    SchoolProfileCensusHelper _schoolProfileCensusHelper;
    @Autowired
    ICensusCacheDao _censusCacheDao;
    @Autowired
    IPropertyDao _propertyDao;

    public static final String MODEL_STUDENTS_TAB_KEY = "studentsTab";
    public static final String MODEL_TEACHERS_TAB_KEY = "teachersTab";
    public static final String MODEL_FOOTNOTES_MAP_KEY = "footnotesMap";
    public static final String MODEL_ESP_RESULTS_MAP_KEY = "espResults";

    public enum Tab {
        Students,
        Teachers
        ;
    }

    /**
     * Maps a census Tab to the Groups of census data on that tab
     */
    public static class TabToGroupsView implements Serializable {

        TabToGroupsView(Tab tab, Map<CensusGroup, GroupOfStudentTeacherViewRows> rows) {
            _tab = tab;
            _groupToGroupOfRowsMap = rows;
        }

        private Tab _tab;
        private CensusGroup[] _possibleGroups;
        private Map<CensusGroup, GroupOfStudentTeacherViewRows> _groupToGroupOfRowsMap;

        /**
         * @return true if there's any census data available for the tab
         */
        public boolean isAnyData() {
            return !_groupToGroupOfRowsMap.isEmpty();
        }
        public Set<CensusGroup> getGroups() {
            return _groupToGroupOfRowsMap.keySet();
        }
        public Tab getTab() {
            return _tab;
        }
        public Map<CensusGroup, GroupOfStudentTeacherViewRows> getGroupToGroupOfRowsMap() {
            return _groupToGroupOfRowsMap;
        }
    }

    /**
     * @return A map of census Tab to the list of CensusGroups that should be shown on a tab, and according to the
     * ordering here
     */
    public Map<Tab, List<CensusGroup>> defaultTabGroupsConfig() {
        Map<Tab, List<CensusGroup>> map = new LinkedHashMap<Tab, List<CensusGroup>>();

        map.put(
            Tab.Students, ListUtils.newArrayList(
                CensusGroup.Student_Ethnicity,
                CensusGroup.Home_Languages_of_English_Learners,
                CensusGroup.Student_Subgroups,
                CensusGroup.Attendance,
                CensusGroup.Attendance_and_Completion,
                CensusGroup.Graduation_Rate)
        );

        map.put(
            Tab.Teachers, ListUtils.newArrayList(
                CensusGroup.Student_Teacher_Ratio,
                CensusGroup.Average_Class_Size,
                CensusGroup.Teacher_Experience,
                CensusGroup.Teacher_Education_Levels,
                CensusGroup.Teacher_Credentials)
        );

        return map;
    }

    Logger _log = Logger.getLogger(SchoolProfileStatsController.class);


    @RequestMapping(method= RequestMethod.GET)
    public Map<String,Object> handle(HttpServletRequest request) {
        Map<String,Object> model = new HashMap<String,Object>();

        School school = getSchool(request);

        Map<String,Object> statsModel = null;

        boolean censusCacheEnabled = isCachingEnabled();

        if (censusCacheEnabled) {
            statsModel = _censusCacheDao.getMapForSchool(school);
        }

        if (statsModel == null) {
            statsModel = new HashMap<String,Object>();

            CensusDataHolder censusDataHolder = _schoolProfileCensusHelper.getCensusDataHolder(request);

            // make the census data holder load school, district, and state data onto the census data sets
            censusDataHolder.retrieveDataSetsAndAllData();

            // Build map of CensusGroup to view rows from list of data sets and census config
            Map<CensusGroup, GroupOfStudentTeacherViewRows> groupToGroupOfViewRows =
                _schoolProfileCensusHelper.buildDisplayRows(
                    _schoolProfileCensusHelper.getCensusStateConfig(request),
                    censusDataHolder.getAllCensusDataSets()
                );

            TabToGroupsView studentsTab = buildTabToGroupsView(Tab.Students, groupToGroupOfViewRows);
            TabToGroupsView teachersTab = buildTabToGroupsView(Tab.Teachers, groupToGroupOfViewRows);
            statsModel.put(MODEL_STUDENTS_TAB_KEY, studentsTab);
            statsModel.put(MODEL_TEACHERS_TAB_KEY, teachersTab);
            statsModel.put(MODEL_FOOTNOTES_MAP_KEY, getFootnotesMap(groupToGroupOfViewRows));

            if (censusCacheEnabled) {
                cacheStatsModel(statsModel, school);
            }
        }

        Map<String, List<EspResponse>> espResults = _schoolProfileDataHelper.getEspDataForSchool(request);
        statsModel.put(MODEL_ESP_RESULTS_MAP_KEY, espResults);

        model.putAll(statsModel);
        return model;
    }

    protected boolean isCachingEnabled() {
        String prop = _propertyDao.getProperty(IPropertyDao.CENSUS_CACHE_ENABLED_KEY);
        boolean censusCacheEnabled = "true".equalsIgnoreCase(prop);

        return censusCacheEnabled;
    }

    protected TabToGroupsView buildTabToGroupsView(Tab tab, Map<CensusGroup, GroupOfStudentTeacherViewRows> groupIdToStatsRows) {
        // create default object in case something goes wrong...
        TabToGroupsView tabView =
                new TabToGroupsView(tab, new LinkedHashMap<CensusGroup, GroupOfStudentTeacherViewRows>());

        try {
            tabView = new TabToGroupsView(
                tab,
                CollectionUtils.subMap(
                    groupIdToStatsRows,
                    defaultTabGroupsConfig().get(tab),
                    true
                )
            );
        } catch (Exception e) {
            _log.debug("Problem creating a submap from Map<CensusGroup, List<SchoolProfileStatsDisplayRow>>");
        }
        return tabView;
    }

    /**
     * Pre-calculate all footnotes
     */
    protected Map<Long, SchoolProfileCensusSourceHelper> getFootnotesMap(Map<CensusGroup, GroupOfStudentTeacherViewRows> groupIdToStatsRows) {
        Map<Long, SchoolProfileCensusSourceHelper> rval = new HashMap<Long, SchoolProfileCensusSourceHelper>();
        if (groupIdToStatsRows == null) {
            return rval;
        }
        for (CensusGroup censusGroup: groupIdToStatsRows.keySet()) {
            SchoolProfileCensusSourceHelper sourceHelper = new SchoolProfileCensusSourceHelper();
            for (SchoolProfileStatsDisplayRow row: groupIdToStatsRows.get(censusGroup)) {
                sourceHelper.recordSource(row);
            }
            rval.put(censusGroup.getId(), sourceHelper);
        }
        return rval;
    }

    public void cacheStatsModel(Map<String,Object> statsModel, School school) {
        try {
            _censusCacheDao.insert(school, statsModel);
        } catch (IOException e) {
            _log.debug("Error while attempting to cache stats model. ", e);
            // all is lost. don't cache
        }
    }

    public void setSchoolProfileDataHelper(SchoolProfileDataHelper schoolProfileDataHelper) {
        _schoolProfileDataHelper = schoolProfileDataHelper;
    }

    public void setSchoolProfileCensusHelper(SchoolProfileCensusHelper schoolProfileCensusHelper) {
        _schoolProfileCensusHelper = schoolProfileCensusHelper;
    }

    public void setCensusCacheDao(ICensusCacheDao censusCacheDao) {
        _censusCacheDao = censusCacheDao;
    }

    public void setPropertyDao(IPropertyDao propertyDao) {
        _propertyDao = propertyDao;
    }
}