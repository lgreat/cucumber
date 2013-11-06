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

        ArrayList<Anchor>  moreInfoLinksPublicPreschools= new ArrayList<Anchor>();
        Anchor link = new Anchor("http://dcps.dc.gov/DCPS/Learn+About+Schools/Prepare+to+Enroll/Preschool,+Pre-Kindergarten+and+Out-of-Boundary+Lottery", "DCPS Preschool and Pre-K information");
        moreInfoLinksPublicPreschools.add(link);
        MoreInformationModel  infoPublicPreschools= new MoreInformationModel(moreInfoLinksPublicPreschools);
        publicPreschools.setMoreInfo(infoPublicPreschools);

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
       ArrayList<Anchor>  moreInfoLinksPrivatePreschools= new ArrayList<Anchor>();
       Anchor link1PrivatePreschools = new Anchor("http://site.adw.org/catholic-schools", "Find Archdiocese schools");
       Anchor link2PrivatePreschools = new Anchor("http://site.adw.org/tuition-assistance", "Learn about tuition assistance at Archdiocese schools.");
       Anchor link3PrivatePreschools = new Anchor("http://www.independenteducation.org/", "Independent Private Schools");
       Anchor link4PrivatePreschools = new Anchor("http://www.independenteducation.org/families/common-recommendation-forms-for-student-applicants", "Common recommendation form for select independent schools");
       Anchor link5PrivatePreschools = new Anchor("https://www.independenteducation.org/File%20Library/Unassigned/Admission-Dates-Survey-Results-in-pre-pdf-format.pdf", "Independent Education school&#180;s admission details");

        moreInfoLinksPrivatePreschools.add(link1PrivatePreschools);
        moreInfoLinksPrivatePreschools.add(link2PrivatePreschools);
        moreInfoLinksPrivatePreschools.add(link3PrivatePreschools);
        moreInfoLinksPrivatePreschools.add(link4PrivatePreschools);
        moreInfoLinksPrivatePreschools.add(link5PrivatePreschools);
        final MoreInformationModel  moreInfoPrivatePreschools= new MoreInformationModel(moreInfoLinksPrivatePreschools);
        privatePreschools.setMoreInfo(moreInfoPrivatePreschools);

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

        ArrayList<Anchor>  moreInfoLinksPublicElementarySchool= new ArrayList<Anchor>();
        Anchor linkPublicElementarySchool = new Anchor("http://dcps.dc.gov/DCPS/Learn+About+Schools/Prepare+to+Enroll/Find+Your+Assigned+Schools", "Find your assigned school");
        moreInfoLinksPublicElementarySchool.add(linkPublicElementarySchool);
        MoreInformationModel  infoPublicElementarySchool= new MoreInformationModel(moreInfoLinksPublicElementarySchool);
        publicElementarySchool.setMoreInfo(infoPublicElementarySchool);
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

        ArrayList<Anchor>  moreInfoLinksPrivateElementarySchool= new ArrayList<Anchor>();
        Anchor linkPrivateElementarySchool1 = new Anchor("http://site.adw.org/catholic-schools", "Find Archdiocese schools");
        Anchor linkPrivateElementarySchool2 = new Anchor("http://site.adw.org/tuition-assistance", "Learn about tuition assistance at Archdiocese schools");
        Anchor linkPrivateElementarySchool3 = new Anchor("http://www.independenteducation.org/", "Learn about Independent Private Schools ");
        Anchor linkPrivateElementarySchool4 = new Anchor("http://www.independenteducation.org/families/common-recommendation-forms-for-student-applicants", "Common recommendation form for select independent schools");
        Anchor linkPrivateElementarySchool5 = new Anchor("https://www.independenteducation.org/File%20Library/Unassigned/Admission-Dates-Survey-Results-in-pre-pdf-format.pdf", "Independent Education school's admission details");
        Anchor linkPrivateElementarySchool6 = new Anchor("http://www.latinostudentfund.org/", "Latino Student Fund");
        Anchor linkPrivateElementarySchool7 = new Anchor("http://blackstudentfund.org/wordpress/", "Black Student Fund");
        Anchor linkPrivateElementarySchool8 = new Anchor("http://www.dcscholarships.org/ ", "Opportunity Scholarship Program ");




        moreInfoLinksPrivateElementarySchool.add(linkPrivateElementarySchool1);
        moreInfoLinksPrivateElementarySchool.add(linkPrivateElementarySchool2);
        moreInfoLinksPrivateElementarySchool.add(linkPrivateElementarySchool3);
        moreInfoLinksPrivateElementarySchool.add(linkPrivateElementarySchool4);
        moreInfoLinksPrivateElementarySchool.add(linkPrivateElementarySchool5);
        moreInfoLinksPrivateElementarySchool.add(linkPrivateElementarySchool6);
        moreInfoLinksPrivateElementarySchool.add(linkPrivateElementarySchool7);
        moreInfoLinksPrivateElementarySchool.add(linkPrivateElementarySchool8);

        MoreInformationModel  infoPrivateElementarySchool= new MoreInformationModel(moreInfoLinksPrivateElementarySchool);
        privateElementarySchool.setMoreInfo(infoPrivateElementarySchool);
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

        ArrayList<Anchor>  moreInfoLinksPublicMiddleSchool= new ArrayList<Anchor>();
        Anchor linkPublicMiddleSchool = new Anchor("http://dcps.dc.gov/DCPS/Learn+About+Schools/Prepare+to+Enroll/Find+Your+Assigned+Schools", "Find your assigned school");
        moreInfoLinksPublicMiddleSchool.add(linkPublicMiddleSchool);
        MoreInformationModel  infoPublicMiddleSchool= new MoreInformationModel(moreInfoLinksPublicMiddleSchool);
        publicMiddleSchool.setMoreInfo(infoPublicMiddleSchool);
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

        ArrayList<Anchor>  moreInfoLinksPrivateMiddleSchool= new ArrayList<Anchor>();
        Anchor linkPrivateMiddleSchool1 = new Anchor("http://site.adw.org/catholic-schools", "Find Archdiocese schools");
        Anchor linkPrivateMiddleSchool2 = new Anchor("http://site.adw.org/tuition-assistance", "Learn about tuition assistance at Archdiocese schools");
        Anchor linkPrivateMiddleSchool3 = new Anchor("http://www.independenteducation.org/", "Learn about Independent Private Schools ");
        Anchor linkPrivateMiddleSchool4 = new Anchor("http://www.independenteducation.org/families/common-recommendation-forms-for-student-applicants", "Common recommendation form for select independent schools");
        Anchor linkPrivateMiddleSchool5 = new Anchor("https://www.independenteducation.org/File%20Library/Unassigned/Admission-Dates-Survey-Results-in-pre-pdf-format.pdf", "Independent Education school&#180;s admission details");
        Anchor linkPrivateMiddleSchool6 = new Anchor("http://www.latinostudentfund.org/", "Latino Student Fund");
        Anchor linkPrivateMiddleSchool7 = new Anchor("http://blackstudentfund.org/wordpress/", "Black Student Fund");
        Anchor linkPrivateMiddleSchool8 = new Anchor("http://www.dcscholarships.org/ ", "Opportunity Scholarship Program ");




        moreInfoLinksPrivateMiddleSchool.add(linkPrivateMiddleSchool1);
        moreInfoLinksPrivateMiddleSchool.add(linkPrivateMiddleSchool2);
        moreInfoLinksPrivateMiddleSchool.add(linkPrivateMiddleSchool3);
        moreInfoLinksPrivateMiddleSchool.add(linkPrivateMiddleSchool4);
        moreInfoLinksPrivateMiddleSchool.add(linkPrivateMiddleSchool5);
        moreInfoLinksPrivateMiddleSchool.add(linkPrivateMiddleSchool6);
        moreInfoLinksPrivateMiddleSchool.add(linkPrivateMiddleSchool7);
        moreInfoLinksPrivateMiddleSchool.add(linkPrivateMiddleSchool8);

        MoreInformationModel  infoPrivateMiddleSchool= new MoreInformationModel(moreInfoLinksPrivateMiddleSchool);
        privateMiddleSchool.setMoreInfo(infoPrivateMiddleSchool);
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

        ArrayList<Anchor>  moreInfoLinksPublicHighSchool= new ArrayList<Anchor>();
        Anchor linkPublicHighSchool1 = new Anchor("http://dcps.dc.gov/DCPS/Learn+About+Schools/Prepare+to+Enroll/Find+Your+Assigned+Schools", "Find your assigned school");
        Anchor linkPublicHighSchool2 = new Anchor("http://www.dcps.dc.gov/DCPS/highschoolapp", "Learn more about specialized high schools");

        moreInfoLinksPublicHighSchool.add(linkPublicHighSchool1);
        moreInfoLinksPublicHighSchool.add(linkPublicHighSchool2);
        MoreInformationModel  infoPublicHighSchool= new MoreInformationModel(moreInfoLinksPublicHighSchool);
        publicHighSchool.setMoreInfo(infoPublicHighSchool);
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

        ArrayList<Anchor>  moreInfoLinksPrivateHighSchool= new ArrayList<Anchor>();
        Anchor linkPrivateHighSchool1 = new Anchor("http://site.adw.org/catholic-schools", "Find Archdiocese schools");
        Anchor linkPrivateHighSchool2 = new Anchor("http://site.adw.org/tuition-assistance", "Learn about tuition assistance at Archdiocese schools");
        Anchor linkPrivateHighSchool3 = new Anchor("http://site.adw.org/HS-Open-Houses", "Archdiocese open house schedule");
        Anchor linkPrivateHighSchool4 = new Anchor("http://site.adw.org/hspt-registration-link", "Register for Archdiocese HS placement tests ");
        Anchor linkPrivateHighSchool5 = new Anchor("https://www.independenteducation.org/", "Learn about Independent Private Schools ");
        Anchor linkPrivateHighSchool6 = new Anchor("http://www.independenteducation.org/families/common-recommendation-forms-for-student-applicants", "Common recommendation form for select independent schools");
        Anchor linkPrivateHighSchool7 = new Anchor("https://www.independenteducation.org/File%20Library/Unassigned/Admission-Dates-Survey-Results-in-pre-pdf-format.pdf", "Independent Education school&#180;s admission details");
        Anchor linkPrivateHighSchool8 = new Anchor("http://www.latinostudentfund.org/ ", "Latino Student Fund");




        moreInfoLinksPrivateHighSchool.add(linkPrivateHighSchool1);
        moreInfoLinksPrivateHighSchool.add(linkPrivateHighSchool2);
        moreInfoLinksPrivateHighSchool.add(linkPrivateHighSchool3);
        moreInfoLinksPrivateHighSchool.add(linkPrivateHighSchool4);
        moreInfoLinksPrivateHighSchool.add(linkPrivateHighSchool5);
        moreInfoLinksPrivateHighSchool.add(linkPrivateHighSchool6);
        moreInfoLinksPrivateHighSchool.add(linkPrivateHighSchool7);
        moreInfoLinksPrivateHighSchool.add(linkPrivateHighSchool8);

        MoreInformationModel  infoPrivateHighSchool= new MoreInformationModel(moreInfoLinksPrivateHighSchool);
        privateHighSchool.setMoreInfo(infoPrivateHighSchool);
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
