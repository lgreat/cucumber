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
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.list.Anchor;
import gs.web.util.list.AnchorListModelFactory;
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
        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
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
        String description;
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
        String descriptionKey = CityHubHelper.ENROLLMENT_PAGE_KEY_PREFIX
                + "_" + publicPreschools.getSchoolType().getSchoolTypeName().toLowerCase() + "_"
                + publicPreschools.getLevelCode().getLowestLevel().getLongName().toLowerCase() + "_"
                + "description";
        JSONObject publicPreschoolDescription = (JSONObject) enrollmentPageModelMap.get(descriptionKey);
        if(publicPreschoolDescription != null &&
                publicPreschoolDescription.has(CityHubHelper.CONTENT_JSON_OBJECT_KEY) &&
                publicPreschoolDescription.has(CityHubHelper.HEADER_JSON_OBJECT_KEY)) {
            description = (String) publicPreschoolDescription.get(CityHubHelper.CONTENT_JSON_OBJECT_KEY);
            publicPreschools.setHeader((String) publicPreschoolDescription.get(CityHubHelper.HEADER_JSON_OBJECT_KEY));
        } else {
            description = "No Data Found - " + descriptionKey;
        }
        publicPreschools.setDescription(description);

        // Tips
        ArrayList<String> tipsForPublicPreschools= new ArrayList<String>();
        tipsForPublicPreschools.add("DCPS has about 6,000 seats for 3 year-old (pre-school) and 4 year-old (pre-kindergarten) students. " +
                                    "Preference is given to in-boundary students, students who have a sibling at the school, or students " +
                                    "who live in proximity of the school.");

        publicPreschools.setTipsInfoModel(tipsForPublicPreschools);


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
        descriptionKey = CityHubHelper.ENROLLMENT_PAGE_KEY_PREFIX
                + "_" + privatePreschools.getSchoolType().getSchoolTypeName().toLowerCase() + "_"
                + privatePreschools.getLevelCode().getLowestLevel().getLongName().toLowerCase() + "_"
                + "description";
        JSONObject privatePreschoolDescription = (JSONObject) enrollmentPageModelMap.get(descriptionKey);
        if(privatePreschoolDescription != null &&
                privatePreschoolDescription.has(CityHubHelper.CONTENT_JSON_OBJECT_KEY) &&
                privatePreschoolDescription.has(CityHubHelper.HEADER_JSON_OBJECT_KEY)) {
            description = (String) privatePreschoolDescription.get(CityHubHelper.CONTENT_JSON_OBJECT_KEY);
            privatePreschools.setHeader((String) privatePreschoolDescription.get(CityHubHelper.HEADER_JSON_OBJECT_KEY));
        } else {
            description = "No Data Found - " + descriptionKey;
        }
        privatePreschools.setDescription(description);

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
        descriptionKey = CityHubHelper.ENROLLMENT_PAGE_KEY_PREFIX
                + "_" + publicElementarySchool.getSchoolType().getSchoolTypeName().toLowerCase() + "_"
                + publicElementarySchool.getLevelCode().getLowestLevel().getLongName().toLowerCase() + "_"
                + "description";
        JSONObject publicElementaryDescription = (JSONObject) enrollmentPageModelMap.get(descriptionKey);
        if(publicElementaryDescription != null &&
                publicElementaryDescription.has(CityHubHelper.CONTENT_JSON_OBJECT_KEY) &&
                publicElementaryDescription.has(CityHubHelper.HEADER_JSON_OBJECT_KEY)) {
            description = (String) publicElementaryDescription.get(CityHubHelper.CONTENT_JSON_OBJECT_KEY);
            publicElementarySchool.setHeader((String) publicElementaryDescription.get(CityHubHelper.HEADER_JSON_OBJECT_KEY));
        } else {
            description = "No Data Found - " + descriptionKey;
        }
        publicElementarySchool.setDescription(description);

        // Tips
        ArrayList<String> tipsForPublicElementarySchool= new ArrayList<String>();
        tipsForPublicElementarySchool.add("Every elementary school feeds into a middle school, which in turn feeds into a high school. These are called destination schools. " +
                                          "Even if your child attends an elementary school as an out-of-boundary student, he still has the right to attend the destination schools of " +
                                          "that school. So you might want to give higher priority to out-of-boundary schools that feed into the destination school of your " +
                                          "choice.");
        tipsForPublicElementarySchool.add("Waitlists for high performing schools vary for each grade level. The demand for high performing schools is often higher for the entry-level grades, " +
                                          "so there might be a greater chance of getting into a school of your choice at odd years if you are flexible.");

        tipsForPublicElementarySchool.add("Many schools have open houses throughout the year. Open Houses are your best opportunity to get a feel for the school culture and to ask other parents questions about the school." +
                                           "Some public charter schools also require home visits as part of the enrollment process.");
        tipsForPublicElementarySchool.add("Most DCPS elementary schools run through fifth grade. There are several DCPS education campuses that include elementary and middle grades in one school.  Some public charter middle schools start in the " +
                                          "fifth grade and often offer more seats in the fifth grade so you will want to weigh those factors in your consideration. ");

        publicElementarySchool.setTipsInfoModel(tipsForPublicElementarySchool);


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
        descriptionKey = CityHubHelper.ENROLLMENT_PAGE_KEY_PREFIX
                + "_" + privateElementarySchool.getSchoolType().getSchoolTypeName().toLowerCase() + "_"
                + privateElementarySchool.getLevelCode().getLowestLevel().getLongName().toLowerCase() + "_"
                + "description";
        JSONObject privateElementaryDescription = (JSONObject) enrollmentPageModelMap.get(descriptionKey);
        if(privateElementaryDescription != null &&
                privateElementaryDescription.has(CityHubHelper.CONTENT_JSON_OBJECT_KEY) &&
                privateElementaryDescription.has(CityHubHelper.HEADER_JSON_OBJECT_KEY)) {
            description = (String) privateElementaryDescription.get(CityHubHelper.CONTENT_JSON_OBJECT_KEY);
            privateElementarySchool.setHeader((String) privateElementaryDescription.get(CityHubHelper.HEADER_JSON_OBJECT_KEY));
        } else {
            description = "No Data Found - " + descriptionKey;
        }
        privateElementarySchool.setDescription(description);

        // Tips
        ArrayList<String> tipsForPrivateElementarySchool= new ArrayList<String>();
        tipsForPrivateElementarySchool.add("If you are applying for financial aid or you are an Opportunity Scholarship Program (OSP) applicant to a private school, many schools may waive or defer application fees. " +
                                           "Make sure that you mention that in your interview and essay.");
        tipsForPrivateElementarySchool.add("Many private schools accept common recommendation forms if your child is applying to more than one school.");
        privateElementarySchool.setTipsInfoModel(tipsForPrivateElementarySchool);


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
        descriptionKey = CityHubHelper.ENROLLMENT_PAGE_KEY_PREFIX
                + "_" + publicMiddleSchool.getSchoolType().getSchoolTypeName().toLowerCase() + "_"
                + publicMiddleSchool.getLevelCode().getLowestLevel().getLongName().toLowerCase() + "_"
                + "description";
        JSONObject publicMiddleDescription = (JSONObject) enrollmentPageModelMap.get(descriptionKey);
        if(publicMiddleDescription != null &&
                publicMiddleDescription.has(CityHubHelper.CONTENT_JSON_OBJECT_KEY) &&
                publicMiddleDescription.has(CityHubHelper.HEADER_JSON_OBJECT_KEY)) {
            description = (String) publicMiddleDescription.get(CityHubHelper.CONTENT_JSON_OBJECT_KEY);
            publicMiddleSchool.setHeader((String) publicMiddleDescription.get(CityHubHelper.HEADER_JSON_OBJECT_KEY));
        } else {
            description = "No Data Found - " + descriptionKey;
        }
        publicMiddleSchool.setDescription(description);

        // Tips
        ArrayList<String> tipsForPublicMiddleSchool= new ArrayList<String>();

        tipsForPublicMiddleSchool.add("Waitlists for high performing schools vary for each grade level. The demand for high performing schools is often higher for the entry-level grades, " +
                "so there might be a greater chance of getting into a school of your choice at odd years if you are flexible.");

        tipsForPublicMiddleSchool.add("Most schools have three or four open houses, usually one in the fall and two or three in the spring before enrollment begins." +
                                          "Open Houses are your best opportunity to get a feel for the school culture and to " +
                                          "ask other parents questions about the school. ");
        tipsForPublicMiddleSchool.add("DCPS and most private elementary schools run until the sixth grade. Some public charter middle schools start in the fifth grade " +
                                          "and often offer more seats in the fifth grade so you will want to weigh those " +
                                         "factors in your consideration.");
        tipsForPublicMiddleSchool.add("Every elementary school feeds into a middle school, which in turn feeds into a high school. These are called destination schools." +
                " Even if your child attends an elementary school as an out-of-boundary student, he still has the right to attend the destination schools of that school. So you might want to give higher priority to out-of-boundary schools that feed into the destination school of your choice.");

        publicMiddleSchool.setTipsInfoModel(tipsForPublicMiddleSchool);


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
        descriptionKey = CityHubHelper.ENROLLMENT_PAGE_KEY_PREFIX
                + "_" + privateMiddleSchool.getSchoolType().getSchoolTypeName().toLowerCase() + "_"
                + privateMiddleSchool.getLevelCode().getLowestLevel().getLongName().toLowerCase() + "_"
                + "description";
        JSONObject privateMiddleDescription = (JSONObject) enrollmentPageModelMap.get(descriptionKey);
        if(privateMiddleDescription != null &&
                privateMiddleDescription.has(CityHubHelper.CONTENT_JSON_OBJECT_KEY) &&
                privateMiddleDescription.has(CityHubHelper.HEADER_JSON_OBJECT_KEY)) {
            description = (String) privateMiddleDescription.get(CityHubHelper.CONTENT_JSON_OBJECT_KEY);
            privateMiddleSchool.setHeader((String) privateMiddleDescription.get(CityHubHelper.HEADER_JSON_OBJECT_KEY));
        } else {
            description = "No Data Found - " + descriptionKey;
        }
        privateMiddleSchool.setDescription(description);

        // Tips
        ArrayList<String> tipsForPrivateMiddleSchool= new ArrayList<String>();
        tipsForPrivateMiddleSchool.add("If you are an Opportunity Scholarship Program (OSP) applicant to a private school, " +
                                        "many schools may waive or defer application fees. Make sure that you mention that you are an OSP applicant in your interview and essay.");
        tipsForPrivateMiddleSchool.add("Many private schools accept common recommendation forms if your child is applying to more than one school.");
        privateMiddleSchool.setTipsInfoModel(tipsForPrivateMiddleSchool);


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
        descriptionKey = CityHubHelper.ENROLLMENT_PAGE_KEY_PREFIX
                + "_" + publicHighSchool.getSchoolType().getSchoolTypeName().toLowerCase() + "_"
                + publicHighSchool.getLevelCode().getLowestLevel().getLongName().toLowerCase() + "_"
                + "description";
        JSONObject publicHighDescription = (JSONObject) enrollmentPageModelMap.get(descriptionKey);
        if(publicHighDescription != null &&
                publicHighDescription.has(CityHubHelper.CONTENT_JSON_OBJECT_KEY) &&
                publicHighDescription.has(CityHubHelper.HEADER_JSON_OBJECT_KEY)) {
            description = (String) publicHighDescription.get(CityHubHelper.CONTENT_JSON_OBJECT_KEY);
            publicHighSchool.setHeader((String) publicHighDescription.get(CityHubHelper.HEADER_JSON_OBJECT_KEY));
        } else {
            description = "No Data Found - " + descriptionKey;
        }
        publicHighSchool.setDescription(description);

        // Tips
        ArrayList<String> tipsForPublicHighSchool= new ArrayList<String>();

        tipsForPublicHighSchool.add("DCPS has six specialized citywide high schools with specific admissions requirements and selection criteria.  9th and 10th " +
                                    "grade applicants must submit online applications. The deadline is usually in December.");
        tipsForPublicHighSchool.add("Waitlists for high performing schools vary for each grade level. The demand for high performing schools is often higher for the entry-level grades, " +
                "so there might be a greater chance of getting into a school of your choice at odd years if you are flexible.");

        tipsForPublicHighSchool.add("Most schools have three or four open houses, usually one in the fall and two or three in the spring before enrollment begins." +
                "Open Houses are your best opportunity to get a feel for the school culture and to " +
                "ask other parents questions about the school.");
        tipsForPublicHighSchool.add("Some public charter schools require home visits as part of the enrollment process.");

        publicHighSchool.setTipsInfoModel(tipsForPublicHighSchool);


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
        descriptionKey = CityHubHelper.ENROLLMENT_PAGE_KEY_PREFIX
                + "_" + privateHighSchool.getSchoolType().getSchoolTypeName().toLowerCase() + "_"
                + privateHighSchool.getLevelCode().getLowestLevel().getLongName().toLowerCase() + "_"
                + "description";
        JSONObject privateHighDescription = (JSONObject) enrollmentPageModelMap.get(descriptionKey);
        if(privateHighDescription != null &&
                privateHighDescription.has(CityHubHelper.CONTENT_JSON_OBJECT_KEY) &&
                privateHighDescription.has(CityHubHelper.HEADER_JSON_OBJECT_KEY)) {
            description = (String) privateHighDescription.get(CityHubHelper.CONTENT_JSON_OBJECT_KEY);
            privateHighSchool.setHeader((String) privateHighDescription.get(CityHubHelper.HEADER_JSON_OBJECT_KEY));
        } else {
            description = "No Data Found - " + descriptionKey;
        }
        privateHighSchool.setDescription(description);

        // Tips
        ArrayList<String> tipsForPrivateHighSchool= new ArrayList<String>();
        tipsForPrivateHighSchool.add("If you are an Opportunity Scholarship Program (OSP) applicant to a private school, " +
                                     "many schools may waive or defer application fees. Make sure that you mention that you are an OSP applicant in your interview and essay.");
        tipsForPrivateHighSchool.add("Some private schools accept common recommendation forms if your child is applying to more than one school. Others have their own required documentation. " +
                                     "The application process differs for each school. Make sure you start the process early and register for entrance exams");
        privateHighSchool.setTipsInfoModel(tipsForPrivateHighSchool);


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
