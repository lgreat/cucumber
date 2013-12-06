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
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.lang.WordUtils;
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

    public static final String DATE_PATTERN_MMddyyyy = "MM-dd-yyyy";
    public static final String DATE_PATTERN_MMMdyyyy = "MMM d, yyyy";
    public static final String CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY =  "configKeyPrefixListWithIndex";
    public static final String CONFIG_KEY_PREFIXES_MAP_MODEL_KEY =  "configKeyPrefixesMap";

    public static int MAX_NO_OF_DAYS_BACK_REVIEWS_PUBLISHED = 90;

    public static int COUNT_OF_REVIEWS_TO_BE_DISPLAYED = 2;
    public static int MAX_IMPORTANT_EVENTS_TO_DISPLAYED = 2;
    public static final String IMPORTANT_EVENT_KEY_PREFIX = "importantEvent";
    public static final String KEY_ENROLLMENT_DATES_KEY_PREFIX = "keyEnrollmentDates";
    public static final String COLLECTION_NICKNAME_KEY = "collection_nickname";
    public static final String SHOW_ADS_KEY = "showAds";
    public static final String COLLECTION_NICKNAME_MODEL_KEY = "collectionNickname";
    public static final String HUB_HOME_CHOOSE_SCHOOL_MODEL_KEY = "chooseSchool";
    public static final String HUB_CITY_ARTICLE_MODEL_KEY = "cityArticle";
    public static final String HUB_PARTNER_CAROUSEL_MODEL_KEY = "partnerCarousel";
    public static final String HUB_HOME_KEY_PREFIX = "hubHome";
    public static final String HUB_HOME_ANNOUNCEMENT_MODEL_KEY = "announcement";
    public static final String HUB_SPONSOR_MODEL_KEY = "sponsor";

    public static final String ENROLLMENT_PAGE_KEY_PREFIX = "enrollmentPage";
    public static final String ENROLLMENT_PAGE_SUBHEADING_MODEL_KEY = "subHeading";

    public static final String CHOOSE_PAGE_KEY_PREFIX = "choosePage";

    public static final String CONTENT_JSON_OBJECT_KEY = "content";
    public static final String HEADER_JSON_OBJECT_KEY = "header";
    public static final String MORE_INFO_JSON_OBJECT_KEY = "moreInfo";
    public static final String CONTACT_JSON_OBJECT_KEY = "contact";
    public static final String LINK_JSON_OBJECT_KEY = "link";
    public static final String LINK_NAME_JSON_OBJECT_KEY = "name";
    public static final String LINK_PATH_JSON_OBJECT_KEY = "path";
    public static final String LINK_NEWWINDOW_JSON_OBJECT_KEY = "newWindow";

    public static final String TIPS_CONFIG_KEY_SUFFIX = "tips";
    public static final String DESCRIPTION_CONFIG_KEY_SUFFIX = "description";
    public static final String MORE_INFO_CONFIG_KEY_SUFFIX = "moreInfo";
    public static final String LOCAL_LINKS_CONFIG_KEY_SUFFIX = "localLinks";


    public static final String EDUCATIONCOMMUNITY_PAGE_KEY_PREFIX = "eduCommPage";
    public static final String EDUCATIONCOMMUNITY_PAGE_SUBHEADING_MODEL_KEY = "subHeading";
    public static final String EDUCATIONCOMMUNITY_PAGE_TABDATA_MODEL_KEY = "partnerData";

    public static final String NO_DATA_FOUND_PREFIX = "No Data Found - ";

    @Autowired
    private IHubCityMappingDao _hubCityMappingDao;
    @Autowired
    private IHubConfigDao _hubConfigDao;
    @Autowired
    private AnchorListModelFactory _anchorListModelFactory;


    public List<HubConfig> getConfigListFromCollectionId(final Integer collectionId) {
        return collectionId != null ?  _hubConfigDao.getAllConfigFromCollectionId(collectionId) : new ArrayList<HubConfig>();
    }

    public ModelMap getImportantModuleMap(List<HubConfig> configList) {
        ModelMap importantEventsMap = getFilteredConfigMap(configList,  CityHubHelper.IMPORTANT_EVENT_KEY_PREFIX);
        List<String> configKeyPrefixesSortedByDate = getConfigKeyPrefixesSortedByDate(importantEventsMap);
        importantEventsMap.put(CityHubHelper.CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY, configKeyPrefixesSortedByDate);
        importantEventsMap.put("maxImportantEventsToDisplay",  CityHubHelper.MAX_IMPORTANT_EVENTS_TO_DISPLAYED);
        return importantEventsMap;
    }

    public ModelMap getKeyEnrollmentDatesMap(final List<HubConfig> configList) {
        ModelMap keyEnrollmentDatesMap = getFilteredConfigMap(configList,  CityHubHelper.KEY_ENROLLMENT_DATES_KEY_PREFIX);
        List<String> configKeyPrefixesSortedByDate = getConfigKeyPrefixesSortedByDate(keyEnrollmentDatesMap);

        Map<String, List<String>> sortedConfigKeyPrefixesMap = getSortedKeyPrefixWithLevelCodeAndSchoolTypeMap(KEY_ENROLLMENT_DATES_KEY_PREFIX, configKeyPrefixesSortedByDate);

        keyEnrollmentDatesMap.put(CityHubHelper.CONFIG_KEY_PREFIXES_MAP_MODEL_KEY, sortedConfigKeyPrefixesMap);
        return keyEnrollmentDatesMap;
    }

    public Integer getCollectionId(final String city, final State state)
    {
        Integer collectionId = _hubCityMappingDao.getCollectionIdFromCityAndState(city, state);
        return  collectionId;
    }

    public List<HubConfig> getHubConfig(final String city, final State state) {
        Integer collectionId = getCollectionId(city, state);
        return collectionId != null ?  _hubConfigDao.getAllConfigFromCollectionId(collectionId) : new ArrayList<HubConfig>();
    }

    public ModelMap getFilteredConfigMap(final List<HubConfig> configList, final String keyPrefix) {
        ModelMap filteredConfig = new ModelMap();
        List<String> configKeyPrefixListWithIndex = new ArrayList<String>();

        if (configList != null && keyPrefix != null) {
            for (HubConfig hubConfig : configList) {
                String key = hubConfig != null ? hubConfig.getQuay() : null;
                if (key != null && key.startsWith(keyPrefix)) {
                    String note = hubConfig.getNote();
                    if(note != null && note.toLowerCase().startsWith("json string")) {
                        try {
                            filteredConfig.put(key, convertJSONStringToMap(hubConfig));
                        }
                        catch (JSONException ex) {
                            _logger.error("CityHubHelper - unable to convert string value " + hubConfig.getValue() +
                                    " of the hub config key " + hubConfig.getQuay() + " to JSON object. Please check if " +
                                    "the value is a well defined json in the hub_config table.\n", ex.fillInStackTrace());
                        }
                        continue;
                    }
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
                            Date date = new SimpleDateFormat(DATE_PATTERN_MMddyyyy).parse(hubConfig.getValue());
                            Date today = new Date();
                            if(today.after(date)) continue;
                            calendar.setTime(date);
                            filteredConfig.put(key + "_year", calendar.get(Calendar.YEAR));
                            filteredConfig.put(key + "_dayOfMonth", calendar.get(Calendar.DAY_OF_MONTH));
                            filteredConfig.put(key + "_month", calendar.get(Calendar.MONTH) + 1);
                            // the date added to map here is used later to sort the keys
                            filteredConfig.put(key, date);
                            filteredConfig.put(key + "_MMMdyyyy", new SimpleDateFormat(DATE_PATTERN_MMMdyyyy).format(date));

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
            Object[] filter = {filterLevelCode};
            // content for preschools is just "Preschools", for other types that will be "Elementary Schools",
            // "Middle Schools", "High Schools"
//            if(filterLevelCode.hasMultipleLevelCodes()) {}
//            else {}
            String anchorContent = WordUtils.capitalize(filterLevelCode.getLowestLevel().getLongName()) +
                    (filterLevelCode.equals(LevelCode.PRESCHOOL) ? "s" : " Schools");
            Anchor anchor = _anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, filter, state,
                    city, anchorContent);
            if(anchor != null) {
                browseLinks.add(anchor);
            }
        }

        List<SchoolType> schoolTypes = SchoolType.sortOrder;

        for(SchoolType filterSchoolType : schoolTypes) {
            Object[] filter = {filterSchoolType};
            // implementing GS-13231 lucene changes
            if((SchoolType.PUBLIC).equals(filterSchoolType)) {
                filter = new Object[]{SchoolType.PUBLIC, SchoolType.CHARTER};
            }
            String anchorContent = WordUtils.capitalize(filterSchoolType.getSchoolTypeName()) + " Schools";
            Anchor anchor = _anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, filter, state,
                    city, anchorContent);
            if(anchor != null) {
                browseLinks.add(anchor);
            }
        }

        return browseLinks;
    }

    public Map<String, List<String>> getSortedKeyPrefixWithLevelCodeAndSchoolTypeMap(final String keyPrefix, final List<String> configKeyPrefixesSortedByDate) {
        Map<String, List<String>> sortedConfigKeyPrefixesMap = new HashMap<String, List<String>>();

        if(keyPrefix != null && configKeyPrefixesSortedByDate != null) {
            List<LevelCode> levelCodes = new ArrayList<LevelCode>(){{
                add(LevelCode.PRESCHOOL);
                add(LevelCode.ELEMENTARY);
                add(LevelCode.MIDDLE);
                add(LevelCode.HIGH);
            }};

            List<SchoolType> schoolTypes = SchoolType.sortOrder;

            for(SchoolType filterSchoolType : schoolTypes) {
                for(LevelCode levelCode : levelCodes) {
                    String key = keyPrefix + "_" + filterSchoolType.getSchoolTypeName().toLowerCase()
                            + "_" + levelCode.getLowestLevel().getLongName().toLowerCase() + "_";
                    sortedConfigKeyPrefixesMap.put(key, null);
                }
            }

            Set<String> keys = sortedConfigKeyPrefixesMap.keySet();
            for(String configKeyPrefix : configKeyPrefixesSortedByDate) {
                for(String key : keys) {
                    if(configKeyPrefix.startsWith(key)) {
                        List<String> configKeyPrefixes = sortedConfigKeyPrefixesMap.get(key);
                        if(configKeyPrefixes == null) {
                            configKeyPrefixes = new ArrayList<String>();
                            configKeyPrefixes.add(configKeyPrefix);
                            sortedConfigKeyPrefixesMap.put(key, configKeyPrefixes);
                        }
                        else {
                            configKeyPrefixes.add(configKeyPrefix);
                            sortedConfigKeyPrefixesMap.put(key, configKeyPrefixes);
                        }
                    }
                }
            }
        }

        return sortedConfigKeyPrefixesMap;
    }

    public String getCollectionNicknameFromConfigList(List<HubConfig> configList, Integer collectionId) {
        String nickname = null;

        if(configList != null && collectionId != null) {
            for(HubConfig config : configList) {
                if(COLLECTION_NICKNAME_KEY.equals(config.getQuay()) && config.getHubCityMapping() != null &&
                        collectionId.equals(config.getHubCityMapping().getCollectionId())) {
                    nickname = config.getValue();
                }
            }
        }

        return nickname;
    }

    public boolean showAds(List<HubConfig> configList, Integer collectionId) {
        boolean showAds = false;

        if(configList != null && collectionId != null) {
            for(HubConfig config : configList) {
                if(SHOW_ADS_KEY.equals(config.getQuay()) && "true".equals(config.getValue())) {
                    showAds = true;
                }
            }
        }

        return showAds;
    }

    public Map convertJSONStringToMap(HubConfig hubConfig) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        if(hubConfig != null && hubConfig.getValue() != null) {
            jsonObject = (JSONObject) JSONSerializer.toJSON(hubConfig.getValue());
        }
        return jsonObject;
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
