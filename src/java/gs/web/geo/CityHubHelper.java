package gs.web.geo;

import gs.data.hubs.HubConfig;
import gs.data.hubs.IHubCityMappingDao;
import gs.data.hubs.IHubConfigDao;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.state.State;
import gs.web.util.UrlUtil;
import gs.web.util.list.Anchor;
import gs.web.util.list.AnchorListModel;
import gs.web.util.list.AnchorListModelFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 8/30/13
 * Time: 11:13 AM
 * To change this template use File | Settings | File Templates.
 */
@Component
public class CityHubHelper {
    private static Logger _logger = Logger.getLogger(CityHubHelper.class);

    public static final String DATE_FORMAT = "MM-dd-yyyy";
    public static final String CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY =  "configKeyPrefixListWithIndex";


    public static int MAX_NO_OF_DAYS_BACK_REVIEWS_PUBLISHED = 90;

    public static int COUNT_OF_REVIEWS_TO_BE_DISPLAYED = 2;
    public static int MAX_IMPORTANT_EVENTS_TO_DISPLAYED = 2;
    public static final String IMPORTANT_EVENT_KEY_PREFIX = "importantEvent";


    @Autowired
    private IHubCityMappingDao _hubCityMappingDao;
    @Autowired
    private IHubConfigDao _hubConfigDao;
    @Autowired
    private AnchorListModelFactory _anchorListModelFactory;


    public Integer getHubID(final String city, final State state)
    {
        Integer hubId = _hubCityMappingDao.getHubIdFromCityAndState(city, state);
        return  hubId;
    }

    public List<HubConfig> getHubConfig(final String city, final State state) {
        Integer hubId = getHubID(city, state);
        return hubId != null ?  _hubConfigDao.getAllConfigFromHubId(hubId) : new ArrayList<HubConfig>();
    }

    public ModelMap getFilteredConfigMap(final List<HubConfig> configList, final String keyPrefix) {
        ModelMap filteredConfig = new ModelMap();
        List<String> configKeyPrefixListWithIndex = new ArrayList<String>();

        if (configList != null && keyPrefix != null) {
            for (HubConfig hubConfig : configList) {
                String key = hubConfig.getQuay();
                if (hubConfig != null && key.startsWith(keyPrefix)) {
                    /**
                     * The key should always be in this format - [type_of_key]_[index]_[type_of_value]
                     * an example for the type of key is "importantEvent"
                     * [index] is a number. This should be sequential for each key type - for example there shouldn't be
                     * "importantEvent_2" without "importantEvent_1"
                     * type_of_value identifies what the value is, for importantEvent this could be description, url, date.
                     */
                    String keyPrefixWithIndex = key.substring(0, key.lastIndexOf("_"));
                    /**
                     * If the key is for date, convert the date in string to date object. This is done to sort the events
                     * by date and also to get the day, month and year to apply the appropriate styles for the module.
                     */
                    if(key.endsWith("_date")) {
                        try {
                            Calendar calendar = Calendar.getInstance();
                            Date date = new SimpleDateFormat(DATE_FORMAT).parse(hubConfig.getValue());
                            calendar.setTime(date);
                            filteredConfig.put(key + "_year", calendar.get(Calendar.YEAR));
                            filteredConfig.put(key + "_dayOfMonth", calendar.get(Calendar.DAY_OF_MONTH));
                            filteredConfig.put(key + "_month", calendar.get(Calendar.MONTH) + 1);
                            filteredConfig.put(key, date);

                            configKeyPrefixListWithIndex.add(keyPrefixWithIndex);
                        }
                        catch (ParseException ex) {
                            _logger.error("CityHubController - unable to convert string to java date", ex.getCause());
                        }
                    }
                    else if (key.endsWith("_url")) {
                        filteredConfig.put(key, UrlUtil.formatUrl(hubConfig.getValue()));
                    }
                    else {
                        filteredConfig.put(key, hubConfig.getValue());
                    }
                }
            }
        }

        filteredConfig.put(CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY, configKeyPrefixListWithIndex);

        return filteredConfig;
    }

    public List<String> getConfigKeyPrefixesSortedByDate(final ModelMap filteredConfigMap) {
        List<String> configKeyPrefixList = new ArrayList<String>();

        if(filteredConfigMap != null) {
            Object configKeyPrefixListWithIndex = filteredConfigMap.get(CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY);

            if(configKeyPrefixListWithIndex != null && configKeyPrefixListWithIndex instanceof List) {
                configKeyPrefixList = (List<String>) configKeyPrefixListWithIndex;

                Collections.sort(configKeyPrefixList, new Comparator<String>() {
                    public int compare(String keyPrefix1, String keyPrefix2) {
                        String dateKeyPrefix1 = keyPrefix1 + "_date";
                        String dateKeyPrefix2 = keyPrefix2 + "_date";

                        Object date1 = filteredConfigMap.get(dateKeyPrefix1);
                        Object date2 = filteredConfigMap.get(dateKeyPrefix2);
                        int rval = 0;

                        if (date1 instanceof Date && date2 instanceof Date) {
                            rval = (((Date) date1).after((Date) date2) ? 1 : -1);
                        }
                        return rval;
                    }
                });
            }
        }

        return configKeyPrefixList;
    }

    public AnchorListModel getCollectionBrowseLinks(final HttpServletRequest request, final Integer collectionId, final String city,
                                                    final State state) {
        AnchorListModel browseLinks = new AnchorListModel();

        List<LevelCode> levelCodes = new ArrayList<LevelCode>(){{
            add(LevelCode.PRESCHOOL);
            add(LevelCode.ELEMENTARY);
            add(LevelCode.MIDDLE);
            add(LevelCode.HIGH);
        }};

        for(LevelCode filterLevelCode : levelCodes) {
            Anchor anchor = _anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, filterLevelCode, state, city);
            if(anchor != null) {
                browseLinks.add(anchor);
            }
        }

        List<SchoolType> schoolTypes = SchoolType.sortOrder;

        for(SchoolType filterSchoolType : schoolTypes) {
            Anchor anchor = _anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, filterSchoolType, state, city);
            if(anchor != null) {
                browseLinks.add(anchor);
            }
        }

        return browseLinks;
    }

    public void setHubCityMappingDao(final IHubCityMappingDao _hubCityMappingDao) {
        this._hubCityMappingDao = _hubCityMappingDao;
    }

    public void setHubConfigDao(final IHubConfigDao _hubConfigDao) {
        this._hubConfigDao = _hubConfigDao;
    }

    public void setAnchorListModelFactory(AnchorListModelFactory _anchorListModelFactory) {
        this._anchorListModelFactory = _anchorListModelFactory;
    }
}
