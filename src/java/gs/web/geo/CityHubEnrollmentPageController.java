package gs.web.geo;

/**
 * Created with IntelliJ IDEA.
 * User: sarora
 * Date: 9/23/13
 * Time: 10:39 AM
 * To change this template use File | Settings | File Templates.
 */


import gs.data.hubs.HubConfig;
import gs.data.school.LevelCode;
import gs.data.school.SchoolType;
import gs.data.state.State;
import gs.data.url.DirectoryStructureUrlFactory;
import gs.web.hub.EnrollmentModel;
import gs.web.hub.MoreInformationModel;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.util.PageHelper;
import gs.web.util.UrlUtil;
import gs.web.util.list.Anchor;
import gs.web.util.list.AnchorListModelFactory;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.ImmutableList;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller for the City Hub Enrollment  Pages.
 * @author sarora@greatschools.org Shomi Arora.
 */
@Controller
public class CityHubEnrollmentPageController   implements IDirectoryStructureUrlController{

    private static final String PRESCHOOLS_TAB_NAME = "Preschools";
    private static final String ELEMENTARY_SCHOOLS_TAB_NAME = "Elementary schools";
    private static final String MIDDLE_SCHOOLS_TAB_NAME = "Middle schools";
    private static final String HIGH_SCHOOLS_TAB_NAME = "High schools";
    private static final List<String> tabs = ImmutableList.of(PRESCHOOLS_TAB_NAME, ELEMENTARY_SCHOOLS_TAB_NAME, MIDDLE_SCHOOLS_TAB_NAME, HIGH_SCHOOLS_TAB_NAME);

    private static final String PARAM_CITY = "city";




    @Autowired
    private CityHubHelper _cityHubHelper;
    @Autowired
    private AnchorListModelFactory _anchorListModelFactory;

    @RequestMapping(method= RequestMethod.GET)
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        ModelAndView modelAndView = new ModelAndView("/cityHub/enrollment");
        DirectoryStructureUrlFields fields = (DirectoryStructureUrlFields) request.getAttribute(IDirectoryStructureUrlController.FIELDS);
        final String city =  fields !=  null ? fields.getCityName() : null;
        final State  state =  fields !=  null ? fields.getState() : null;
        // Validate those inputs and give up if we can't build a reasonable page.
        if (state == null) {
            // no state name found on city page, so redirect to /
            View redirectView = new RedirectView("/");
            return new ModelAndView(redirectView);
        }

        if (city == null) {
            // no city name found, so redirect to /california or whichever state they did provide
            View redirectView = new RedirectView(DirectoryStructureUrlFactory.createNewStateBrowseURIRoot(state));
            return new ModelAndView(redirectView);
        }

        PageHelper pageHelper = (PageHelper) request.getAttribute(PageHelper.REQUEST_ATTRIBUTE_NAME);
        if (pageHelper != null) {
            pageHelper.setHideAds(true);
            pageHelper.clearHubCookiesForNavBar(request, response);
            pageHelper.setHubCookiesForNavBar(request, response, state.getAbbreviation(), WordUtils.capitalizeFully(city));
            pageHelper.setHubUserCookie(request, response);

        }

        final Integer collectionId = getCityHubHelper().getCollectionId(city, state);
        final List<HubConfig> configList = getCityHubHelper().getConfigListFromCollectionId(collectionId);

        final ModelMap enrollmentPageModelMap = getCityHubHelper().getFilteredConfigMap(configList,
                CityHubHelper.ENROLLMENT_PAGE_KEY_PREFIX);
        /**
         * The under heading Text will be templatized for every city in Data Base -Shomi
         */

        String subHeadingModelKey = CityHubHelper.ENROLLMENT_PAGE_KEY_PREFIX + "_" +
                CityHubHelper.ENROLLMENT_PAGE_SUBHEADING_MODEL_KEY;
        JSONObject subHeading = (JSONObject) enrollmentPageModelMap.get(subHeadingModelKey);

        String subHeadingText;
        if(subHeading != null && subHeading.has(CityHubHelper.CONTENT_JSON_OBJECT_KEY)) {
            subHeadingText = (String) subHeading.get(CityHubHelper.CONTENT_JSON_OBJECT_KEY);
        } else {
            subHeadingText = "No Data Found - " + subHeadingModelKey;
        }

        modelAndView.addObject("city", WordUtils.capitalizeFully(city));
        modelAndView.addObject("state", state);
        modelAndView.addObject("hubId", collectionId);
        modelAndView.addObject("collectionId", collectionId);
        modelAndView.addObject("subHeadingText", subHeadingText);
        modelAndView.addObject("defaultTab", PRESCHOOLS_TAB_NAME);
        modelAndView.addObject("tabs", tabs);

        /**
         * Get the important events
         */
        ModelMap importantEventsMap = getCityHubHelper().getImportantModuleMap(configList);
        modelAndView.addObject(CityHubHelper.IMPORTANT_EVENT_KEY_PREFIX, importantEventsMap);
        /**
         * Key Enrollment module
         */
        ModelMap keyEnrollmentDatesMap = getCityHubHelper().getKeyEnrollmentDatesMap(configList);
        modelAndView.addObject(CityHubHelper.KEY_ENROLLMENT_DATES_KEY_PREFIX, keyEnrollmentDatesMap);


        /**
         * Get Enrollment Model Info  .
         */
        List<EnrollmentModel> enrollmentInfo = getEnrollmentFacade(request, configList, enrollmentPageModelMap, collectionId, state, city);
        modelAndView.addObject("enrollmentsInfo", enrollmentInfo);

        modelAndView.addObject("showAds", getCityHubHelper().showAds(configList, collectionId));

        modelAndView.addObject(CityHubHelper.COLLECTION_NICKNAME_MODEL_KEY,
                getCityHubHelper().getCollectionNicknameFromConfigList(configList, collectionId));

        return modelAndView;
    }



    /**
     * Get Step facade for the UX .
     * @return stepsInfo List of StepInfo passed to model.
     */
    private List<EnrollmentModel> getEnrollmentFacade(HttpServletRequest request, List<HubConfig> configList,
                                                      ModelMap enrollmentPageModelMap, Integer collectionId, State state,
                                                      String city) {
        List<EnrollmentModel> enrollmentInfo = new ArrayList<EnrollmentModel>();

        String collectionNickname = _cityHubHelper.getCollectionNicknameFromConfigList(configList, collectionId);
        String keyPrefix;
        /**
         * Public Preschool Data Start
         */
        Object[] solrQueryFilter = new Object[]{LevelCode.PRESCHOOL, SchoolType.PUBLIC, SchoolType.CHARTER};

        Anchor publicPreschoolsAnchor = _anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, solrQueryFilter, state,
                city, getBrowseLinkAnchorText(collectionNickname, SchoolType.PUBLIC.getSchoolTypeName(), PRESCHOOLS_TAB_NAME));
        EnrollmentModel publicPreschools= new EnrollmentModel(PRESCHOOLS_TAB_NAME, LevelCode.PRESCHOOL, SchoolType.PUBLIC);
       //Browse Link
        publicPreschools.setBrowseLink(publicPreschoolsAnchor);
       // Description Text
        keyPrefix = CityHubHelper.ENROLLMENT_PAGE_KEY_PREFIX
                + "_" + publicPreschools.getSchoolType().getSchoolTypeName().toLowerCase() + "_"
                + publicPreschools.getLevelCode().getLowestLevel().getLongName().toLowerCase();
        String descriptionKey = keyPrefix  + "_" + CityHubHelper.DESCRIPTION_CONFIG_KEY_SUFFIX;
        JSONObject jsonDescription = (JSONObject) enrollmentPageModelMap.get(descriptionKey);
        setDescriptionFromJsonObject(publicPreschools, jsonDescription, descriptionKey);

        // Tips
        String tipsKey = keyPrefix + "_" + CityHubHelper.TIPS_CONFIG_KEY_SUFFIX;
        Object tipsObject = enrollmentPageModelMap.get(tipsKey);
        publicPreschools.setTipsInfoModel(getTipsFromObject(tipsObject, tipsKey));


        // More Info
        String moreInfoKey = keyPrefix + "_" + CityHubHelper.MORE_INFO_CONFIG_KEY_SUFFIX;
        Object moreInfoObject = enrollmentPageModelMap.get(moreInfoKey);
        publicPreschools.setMoreInfo(getMoreInfoFromJSONObject(moreInfoObject, moreInfoKey));

        /**
         * Public Preschool Data End
         *
         */

        /**
         * Private   Preschool Data Start
         */

        EnrollmentModel privatePreschools= new EnrollmentModel(PRESCHOOLS_TAB_NAME, LevelCode.PRESCHOOL, SchoolType.PRIVATE);
        //Browse Link
        solrQueryFilter = new Object[]{LevelCode.PRESCHOOL, SchoolType.PRIVATE};
        Anchor privatePreschoolsBrowseLinks = _anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId,
                solrQueryFilter, state, city, getBrowseLinkAnchorText(collectionNickname, SchoolType.PRIVATE.getSchoolTypeName(), PRESCHOOLS_TAB_NAME));
        privatePreschools.setBrowseLink(privatePreschoolsBrowseLinks);

        // Description Text
        keyPrefix = CityHubHelper.ENROLLMENT_PAGE_KEY_PREFIX
                + "_" + privatePreschools.getSchoolType().getSchoolTypeName().toLowerCase() + "_"
                + privatePreschools.getLevelCode().getLowestLevel().getLongName().toLowerCase();
        descriptionKey = keyPrefix + "_" + CityHubHelper.DESCRIPTION_CONFIG_KEY_SUFFIX;
        jsonDescription = (JSONObject) enrollmentPageModelMap.get(descriptionKey);
        setDescriptionFromJsonObject(privatePreschools, jsonDescription, descriptionKey);

       // More Info
        moreInfoKey = keyPrefix + "_" + CityHubHelper.MORE_INFO_CONFIG_KEY_SUFFIX;
        moreInfoObject = enrollmentPageModelMap.get(moreInfoKey);
        privatePreschools.setMoreInfo(getMoreInfoFromJSONObject(moreInfoObject, moreInfoKey));

        tipsKey = keyPrefix + "_" + CityHubHelper.TIPS_CONFIG_KEY_SUFFIX;
        tipsObject = enrollmentPageModelMap.get(tipsKey);

        privatePreschools.setTipsInfoModel(getTipsFromObject(tipsObject, tipsKey));

        /**
         * Private   Preschool Data End
         */


        /**
         * Public Elementary School Data Start
         */
        EnrollmentModel publicElementarySchool= new EnrollmentModel(ELEMENTARY_SCHOOLS_TAB_NAME, LevelCode.ELEMENTARY, SchoolType.PUBLIC);


        //Browse Link
        solrQueryFilter = new Object[]{LevelCode.ELEMENTARY, SchoolType.PUBLIC, SchoolType.CHARTER};
        Anchor publicElementaryBrowseLink = _anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId,
                solrQueryFilter, state, city, getBrowseLinkAnchorText(collectionNickname, SchoolType.PUBLIC.getSchoolTypeName(),
                ELEMENTARY_SCHOOLS_TAB_NAME));
        publicElementarySchool.setBrowseLink(publicElementaryBrowseLink);


        // Description Text
        keyPrefix = CityHubHelper.ENROLLMENT_PAGE_KEY_PREFIX
                + "_" + publicElementarySchool.getSchoolType().getSchoolTypeName().toLowerCase() + "_"
                + publicElementarySchool.getLevelCode().getLowestLevel().getLongName().toLowerCase();
        descriptionKey = keyPrefix + "_" + CityHubHelper.DESCRIPTION_CONFIG_KEY_SUFFIX;
        jsonDescription = (JSONObject) enrollmentPageModelMap.get(descriptionKey);
        setDescriptionFromJsonObject(publicElementarySchool, jsonDescription, descriptionKey);

        // Tips
        tipsKey = keyPrefix + "_" + CityHubHelper.TIPS_CONFIG_KEY_SUFFIX;
        tipsObject = enrollmentPageModelMap.get(tipsKey);
        publicElementarySchool.setTipsInfoModel(getTipsFromObject(tipsObject, tipsKey));

        // More Info
        moreInfoKey = keyPrefix + "_" + CityHubHelper.MORE_INFO_CONFIG_KEY_SUFFIX;
        moreInfoObject = enrollmentPageModelMap.get(moreInfoKey);
        publicElementarySchool.setMoreInfo(getMoreInfoFromJSONObject(moreInfoObject, moreInfoKey));

        ArrayList<Anchor>  moreInfoLinksPublicElementarySchool= new ArrayList<Anchor>();
        Anchor linkPublicElementarySchool = new Anchor("http://dcps.dc.gov/DCPS/Learn+About+Schools/Prepare+to+Enroll/Find+Your+Assigned+Schools", "Find your assigned school");
        moreInfoLinksPublicElementarySchool.add(linkPublicElementarySchool);
        /**
         * Public Elementary School Data End
         */

        /**
         * Private Elementary School Data Start
         */
        EnrollmentModel privateElementarySchool= new EnrollmentModel(ELEMENTARY_SCHOOLS_TAB_NAME, LevelCode.ELEMENTARY, SchoolType.PRIVATE);


        //Browse Link
        solrQueryFilter = new Object[]{LevelCode.ELEMENTARY, SchoolType.PRIVATE};
        Anchor privateElementaryBrowseLink = _anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId,
                solrQueryFilter, state, city, getBrowseLinkAnchorText(collectionNickname, SchoolType.PRIVATE.getSchoolTypeName(),
                ELEMENTARY_SCHOOLS_TAB_NAME));
        privateElementarySchool.setBrowseLink(privateElementaryBrowseLink);


        // Description Text
        keyPrefix = CityHubHelper.ENROLLMENT_PAGE_KEY_PREFIX
                + "_" + privateElementarySchool.getSchoolType().getSchoolTypeName().toLowerCase() + "_"
                + privateElementarySchool.getLevelCode().getLowestLevel().getLongName().toLowerCase();
        descriptionKey = keyPrefix + "_" + CityHubHelper.DESCRIPTION_CONFIG_KEY_SUFFIX;
        jsonDescription = (JSONObject) enrollmentPageModelMap.get(descriptionKey);
        setDescriptionFromJsonObject(privateElementarySchool, jsonDescription, descriptionKey);

        // Tips
        tipsKey = keyPrefix + "_" + CityHubHelper.TIPS_CONFIG_KEY_SUFFIX;
        tipsObject = enrollmentPageModelMap.get(tipsKey);
        privateElementarySchool.setTipsInfoModel(getTipsFromObject(tipsObject, tipsKey));

        // More Info
        moreInfoKey = keyPrefix + "_" + CityHubHelper.MORE_INFO_CONFIG_KEY_SUFFIX;
        moreInfoObject = enrollmentPageModelMap.get(moreInfoKey);
        privateElementarySchool.setMoreInfo(getMoreInfoFromJSONObject(moreInfoObject, moreInfoKey));
        /**
         * Private Elementary School Data End
         */

         /**
         * Public Middle  School Data Start
         */
         EnrollmentModel publicMiddleSchool= new EnrollmentModel(MIDDLE_SCHOOLS_TAB_NAME, LevelCode.MIDDLE, SchoolType.PUBLIC);


        //Browse Link
        solrQueryFilter = new Object[]{LevelCode.MIDDLE, SchoolType.PUBLIC, SchoolType.CHARTER};
        Anchor publicMiddleBrowseLink = _anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId,
                solrQueryFilter, state, city, getBrowseLinkAnchorText(collectionNickname, SchoolType.PUBLIC.getSchoolTypeName(),
                MIDDLE_SCHOOLS_TAB_NAME));
        publicMiddleSchool.setBrowseLink(publicMiddleBrowseLink);


        // Description Text
        keyPrefix = CityHubHelper.ENROLLMENT_PAGE_KEY_PREFIX
                + "_" + publicMiddleSchool.getSchoolType().getSchoolTypeName().toLowerCase() + "_"
                + publicMiddleSchool.getLevelCode().getLowestLevel().getLongName().toLowerCase();
        descriptionKey = keyPrefix + "_" + CityHubHelper.DESCRIPTION_CONFIG_KEY_SUFFIX;
        jsonDescription = (JSONObject) enrollmentPageModelMap.get(descriptionKey);
        setDescriptionFromJsonObject(publicMiddleSchool, jsonDescription, descriptionKey);

        // Tips
        tipsKey = keyPrefix + "_" + CityHubHelper.TIPS_CONFIG_KEY_SUFFIX;
        tipsObject = enrollmentPageModelMap.get(tipsKey);
        publicMiddleSchool.setTipsInfoModel(getTipsFromObject(tipsObject, tipsKey));

        // More Info
        moreInfoKey = keyPrefix + "_" + CityHubHelper.MORE_INFO_CONFIG_KEY_SUFFIX;
        moreInfoObject = enrollmentPageModelMap.get(moreInfoKey);
        publicMiddleSchool.setMoreInfo(getMoreInfoFromJSONObject(moreInfoObject, moreInfoKey));
        /**
         * Public Middle School Data End
         */

        /**
         * Private Middle School Data Start
         */
        EnrollmentModel privateMiddleSchool= new EnrollmentModel(MIDDLE_SCHOOLS_TAB_NAME, LevelCode.MIDDLE, SchoolType.PRIVATE);


        //Browse Link
        solrQueryFilter = new Object[]{LevelCode.MIDDLE, SchoolType.PRIVATE};
        Anchor privateMiddleBrowseLink = _anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId,
                solrQueryFilter, state, city, getBrowseLinkAnchorText(collectionNickname, SchoolType.PRIVATE.getSchoolTypeName(),
                MIDDLE_SCHOOLS_TAB_NAME));
        privateMiddleSchool.setBrowseLink(privateMiddleBrowseLink);


        // Description Text
        keyPrefix = CityHubHelper.ENROLLMENT_PAGE_KEY_PREFIX
                + "_" + privateMiddleSchool.getSchoolType().getSchoolTypeName().toLowerCase() + "_"
                + privateMiddleSchool.getLevelCode().getLowestLevel().getLongName().toLowerCase();
        descriptionKey = keyPrefix + "_" + CityHubHelper.DESCRIPTION_CONFIG_KEY_SUFFIX;
        jsonDescription = (JSONObject) enrollmentPageModelMap.get(descriptionKey);
        setDescriptionFromJsonObject(privateMiddleSchool, jsonDescription, descriptionKey);

        // Tips
        tipsKey = keyPrefix + "_" + CityHubHelper.TIPS_CONFIG_KEY_SUFFIX;
        tipsObject = enrollmentPageModelMap.get(tipsKey);
        privateMiddleSchool.setTipsInfoModel(getTipsFromObject(tipsObject, tipsKey));

        // More Info
        moreInfoKey = keyPrefix + "_" + CityHubHelper.MORE_INFO_CONFIG_KEY_SUFFIX;
        moreInfoObject = enrollmentPageModelMap.get(moreInfoKey);
        privateMiddleSchool.setMoreInfo(getMoreInfoFromJSONObject(moreInfoObject, moreInfoKey));
        /**
         * Private Middle School Data End
         */


        /**
         * Public High  School Data Start
         */
        EnrollmentModel publicHighSchool= new EnrollmentModel(HIGH_SCHOOLS_TAB_NAME, LevelCode.HIGH, SchoolType.PUBLIC);


        //Browse Link
        solrQueryFilter = new Object[]{LevelCode.HIGH, SchoolType.PUBLIC, SchoolType.CHARTER};
        Anchor publicHighBrowseLink = _anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId,
                solrQueryFilter, state, city, getBrowseLinkAnchorText(collectionNickname, SchoolType.PUBLIC.getSchoolTypeName(),
                HIGH_SCHOOLS_TAB_NAME));
        publicHighSchool.setBrowseLink(publicHighBrowseLink);


        // Description Text
        keyPrefix = CityHubHelper.ENROLLMENT_PAGE_KEY_PREFIX
                + "_" + publicHighSchool.getSchoolType().getSchoolTypeName().toLowerCase() + "_"
                + publicHighSchool.getLevelCode().getLowestLevel().getLongName().toLowerCase();
        descriptionKey = keyPrefix + "_" + CityHubHelper.DESCRIPTION_CONFIG_KEY_SUFFIX;
        jsonDescription = (JSONObject) enrollmentPageModelMap.get(descriptionKey);
        setDescriptionFromJsonObject(publicHighSchool, jsonDescription, descriptionKey);

        // Tips
        tipsKey = keyPrefix + "_" + CityHubHelper.TIPS_CONFIG_KEY_SUFFIX;
        tipsObject = enrollmentPageModelMap.get(tipsKey);
        publicHighSchool.setTipsInfoModel(getTipsFromObject(tipsObject, tipsKey));

        // More Info
        moreInfoKey = keyPrefix + "_" + CityHubHelper.MORE_INFO_CONFIG_KEY_SUFFIX;
        moreInfoObject = enrollmentPageModelMap.get(moreInfoKey);
        publicHighSchool.setMoreInfo(getMoreInfoFromJSONObject(moreInfoObject, moreInfoKey));
        /**
         * Public High School Data End
         */

        /**
         * Private High School Data Start
         */
        EnrollmentModel privateHighSchool= new EnrollmentModel(HIGH_SCHOOLS_TAB_NAME, LevelCode.HIGH, SchoolType.PRIVATE);


        //Browse Link
        solrQueryFilter = new Object[]{LevelCode.HIGH, SchoolType.PRIVATE};
        Anchor privateHighBrowseLink = _anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId,
                solrQueryFilter, state, city, getBrowseLinkAnchorText(collectionNickname, SchoolType.PRIVATE.getSchoolTypeName(),
                HIGH_SCHOOLS_TAB_NAME));
        privateHighSchool.setBrowseLink(privateHighBrowseLink);


        // Description Text
        keyPrefix = CityHubHelper.ENROLLMENT_PAGE_KEY_PREFIX
                + "_" + privateHighSchool.getSchoolType().getSchoolTypeName().toLowerCase() + "_"
                + privateHighSchool.getLevelCode().getLowestLevel().getLongName().toLowerCase();
        descriptionKey = keyPrefix + "_" + CityHubHelper.DESCRIPTION_CONFIG_KEY_SUFFIX;
        jsonDescription = (JSONObject) enrollmentPageModelMap.get(descriptionKey);
        setDescriptionFromJsonObject(privateHighSchool, jsonDescription, descriptionKey);

        // Tips
        tipsKey = keyPrefix + "_" + CityHubHelper.TIPS_CONFIG_KEY_SUFFIX;
        tipsObject = enrollmentPageModelMap.get(tipsKey);
        privateHighSchool.setTipsInfoModel(getTipsFromObject(tipsObject, tipsKey));

        // More Info
        moreInfoKey = keyPrefix + "_" + CityHubHelper.MORE_INFO_CONFIG_KEY_SUFFIX;
        moreInfoObject = enrollmentPageModelMap.get(moreInfoKey);
        privateHighSchool.setMoreInfo(getMoreInfoFromJSONObject(moreInfoObject, moreInfoKey));
        /**
         * Private High School Data End
         */



        enrollmentInfo.add(publicPreschools);
        enrollmentInfo.add(privatePreschools);
        enrollmentInfo.add(publicElementarySchool);
        enrollmentInfo.add(privateElementarySchool);
        enrollmentInfo.add(publicMiddleSchool);
        enrollmentInfo.add(privateMiddleSchool);
        enrollmentInfo.add(publicHighSchool);
        enrollmentInfo.add(privateHighSchool);

        return   enrollmentInfo;

    }

    public String getBrowseLinkAnchorText(final String collectionNickname, final String schoolType, final String tabName) {
        StringBuilder anchorText = null;
            anchorText = new StringBuilder("Browse");
            anchorText.append(" ");
            anchorText.append(collectionNickname != null ? collectionNickname : "");
            anchorText.append(" ");
            anchorText.append(schoolType != null ? schoolType.toLowerCase(): "");
            anchorText.append(" ");
            anchorText.append(tabName.toLowerCase());
            return anchorText.toString();
    }

    public void setDescriptionFromJsonObject(EnrollmentModel enrollmentModel, JSONObject jsonDescription,
                                             String descriptionKey) {
        String description;
        if(jsonDescription != null && jsonDescription.has(CityHubHelper.CONTENT_JSON_OBJECT_KEY) &&
                jsonDescription.has(CityHubHelper.HEADER_JSON_OBJECT_KEY)) {
            description = (String) jsonDescription.get(CityHubHelper.CONTENT_JSON_OBJECT_KEY);
            enrollmentModel.setHeader((String) jsonDescription.get(CityHubHelper.HEADER_JSON_OBJECT_KEY));
        } else {
            description = CityHubHelper.NO_DATA_FOUND_PREFIX + descriptionKey;
        }
        enrollmentModel.setDescription(description);
    }

    public ArrayList<String> getTipsFromObject(Object tipsObject, String tipsKey) {
        ArrayList<String> tips= new ArrayList<String>();
        if(tipsObject != null && tipsObject instanceof JSONObject &&
                ((JSONObject) tipsObject).has(CityHubHelper.CONTENT_JSON_OBJECT_KEY)) {
            Object jsonTips = ((JSONObject) tipsObject).get(CityHubHelper.CONTENT_JSON_OBJECT_KEY);
            if (jsonTips != null && jsonTips instanceof JSONArray && !((JSONArray)jsonTips).isEmpty()) {
                JSONArray tipsArr = (JSONArray)jsonTips;
                for(int i = 0; i < tipsArr.size(); i++) {
                    String tip = tipsArr.getString(i);
                    tips.add(tip);
                }
            }
            else {
                String tip = ((JSONObject) tipsObject).getString(CityHubHelper.CONTENT_JSON_OBJECT_KEY);
                tips.add(tip);
            }
        }
        else {
            tips.add(CityHubHelper.NO_DATA_FOUND_PREFIX + tipsKey);
        }

        return tips;
    }

    public MoreInformationModel getMoreInfoFromJSONObject(Object moreInfoObject, String moreInfoKey) {
        MoreInformationModel moreInfoModel = new MoreInformationModel();
        if(moreInfoObject != null && moreInfoObject instanceof JSONObject &&
                ((JSONObject) moreInfoObject).has(CityHubHelper.MORE_INFO_JSON_OBJECT_KEY)) {
            Object jsonMoreInfo = ((JSONObject) moreInfoObject).get(CityHubHelper.MORE_INFO_JSON_OBJECT_KEY);
            if (jsonMoreInfo != null && jsonMoreInfo instanceof JSONObject &&
                    ((JSONObject) jsonMoreInfo).get(CityHubHelper.LINK_JSON_OBJECT_KEY) instanceof JSONArray) {
                JSONArray moreLinksArr = ((JSONObject) jsonMoreInfo).getJSONArray(CityHubHelper.LINK_JSON_OBJECT_KEY);
                for(int i = 0; i < moreLinksArr.size(); i++) {
                    JSONObject linkInfo = moreLinksArr.getJSONObject(i);
                    addLinkInfoFromJsonObjectToModel(linkInfo, moreInfoModel);
                }
            }
            else if(jsonMoreInfo != null && jsonMoreInfo instanceof JSONObject &&
                    ((JSONObject) jsonMoreInfo).has(CityHubHelper.LINK_JSON_OBJECT_KEY)) {
                JSONObject linkInfo = (JSONObject) ((JSONObject) jsonMoreInfo).get(CityHubHelper.LINK_JSON_OBJECT_KEY);
                addLinkInfoFromJsonObjectToModel(linkInfo, moreInfoModel);
            }
        }
        else {
            Anchor anchor = new Anchor(null, CityHubHelper.NO_DATA_FOUND_PREFIX + moreInfoKey);
            MoreInformationModel.InfoLinkSource infoLinkSource = moreInfoModel.new InfoLinkSource();
            infoLinkSource.setLink(anchor);
            moreInfoModel.getInfoLinkSources().add(infoLinkSource);
        }

        return moreInfoModel;
    }

    public void addLinkInfoFromJsonObjectToModel(JSONObject linkInfo, MoreInformationModel moreInfoModel) {
        MoreInformationModel.InfoLinkSource infoLinkSource = moreInfoModel.new InfoLinkSource();
        if (isNotEmptyJSONObjectValue(linkInfo, CityHubHelper.LINK_NAME_JSON_OBJECT_KEY)) {
            Anchor anchor = new Anchor((isNotEmptyJSONObjectValue(linkInfo, CityHubHelper.LINK_PATH_JSON_OBJECT_KEY) ?
                    UrlUtil.formatUrl(linkInfo.getString(CityHubHelper.LINK_PATH_JSON_OBJECT_KEY)) : null),
                    linkInfo.getString(CityHubHelper.LINK_NAME_JSON_OBJECT_KEY));
            boolean isNewWindow = isNotEmptyJSONObjectValue(linkInfo, CityHubHelper.LINK_NEWWINDOW_JSON_OBJECT_KEY) &&
                    "true".equalsIgnoreCase(linkInfo.getString(CityHubHelper.LINK_NEWWINDOW_JSON_OBJECT_KEY));
            anchor.setTarget(isNewWindow ? Anchor.Target._blank : Anchor.Target._self);
            infoLinkSource.setLink(anchor);

            if(isNotEmptyJSONObjectValue(linkInfo, CityHubHelper.CONTACT_JSON_OBJECT_KEY)) {
                infoLinkSource.setContact(linkInfo.getString(CityHubHelper.CONTACT_JSON_OBJECT_KEY));
            }

            moreInfoModel.getInfoLinkSources().add(infoLinkSource);
        }
    }

    /**
     * an empty json object key, say name: {}, will be treated as a string with value "{}" by getString method
     * So checking whether the object returned by get is String or another JSON object or JSON array
     */
    public boolean isNotEmptyJSONObjectValue(JSONObject jsonObject, String key) {
        return (jsonObject != null && ((jsonObject.get(key) instanceof String && !"".equals(jsonObject.getString(key).trim()))
                || (jsonObject.get(key) instanceof JSONObject && ((JSONObject) jsonObject.get(key)).size() == 0)
                || (jsonObject.get(key) instanceof JSONArray && ((JSONArray) jsonObject.get(key)).size() == 0)));
    }

    public boolean shouldHandleRequest(final DirectoryStructureUrlFields fields) {
        return fields == null ? false : fields.hasState() && fields.hasCityName() && fields.hasEnrollmentPage()  && !fields.hasDistrictName() && !fields.hasLevelCode() && !fields.hasSchoolName();

    }

    public CityHubHelper getCityHubHelper() {
        return _cityHubHelper;
    }

    public void setCityHubHelper(final CityHubHelper _cityHubHelper) {
        this._cityHubHelper = _cityHubHelper;
    }

    public void setAnchorListModelFactory(AnchorListModelFactory _anchorListModelFactory) {
        this._anchorListModelFactory = _anchorListModelFactory;
    }
}
