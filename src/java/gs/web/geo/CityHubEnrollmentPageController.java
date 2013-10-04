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
import gs.web.hub.EnrollmentModel;
import gs.web.hub.MoreInformationModel;
import gs.web.util.list.Anchor;
import gs.web.util.list.AnchorListModelFactory;
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

/**
 * Controller for the City Hub Enrollment  Pages.
 * @author sarora@greatschools.org Shomi Arora.
 */
@Controller
public class CityHubEnrollmentPageController {

    public static final String PRESCHOOLS_TAB_NAME = "Preschools";
    public static final String HIGH_SCHOOLS_TAB_NAME = "High schools";
    private  static final String PARAM_CITY = "city";
    public static final String ELEMENTARY_SCHOOLS_TAB_NAME = "Elementary schools";
    public static final String MIDDLE_SCHOOLS_TAB_NAME = "Middle schools";
    @Autowired
    private CityHubHelper _cityHubHelper;
    @Autowired
    private AnchorListModelFactory _anchorListModelFactory;

    @RequestMapping(method= RequestMethod.GET)
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        ModelAndView modelAndView = new ModelAndView("/cityHub/enrollmentInformation");
        State state= State.DC;
        String city= "washington";


        // Should be commented out once the Connical URL for Choose Page is in Place _Shomi Revert
//        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
//        final State state = sessionContext.getState();
//        DirectoryStructureUrlFields fields = (DirectoryStructureUrlFields) request.getAttribute(IDirectoryStructureUrlController.FIELDS);
//        String city = StringUtils.defaultIfEmpty(request.getParameter(PARAM_CITY), fields.getCityName());
//        // Validate those inputs and give up if we can't build a reasonable page.
//        if (state == null) {
//            // no state name found on city page, so redirect to /
//            View redirectView = new RedirectView("/");
//            return new ModelAndView(redirectView);
//        }
//
//        if (StringUtils.isEmpty(city)) {
//            // no city name found, so redirect to /california or whichever state they did provide
//            View redirectView = new RedirectView(DirectoryStructureUrlFactory.createNewStateBrowseURIRoot(state));
//            return new ModelAndView(redirectView);
//        }
        final Integer collectionId = getCityHubHelper().getHubID(city, state);
        /**
         * The under heading Text will be templatized for every city in Data Base -Shomi
         */

        final String  underHeadindText= "In DC, parents can choose to send their kids to either a public or private school.<br/><br/>" +
                                        "Public schools include both district and charter schools and are free to any resident of DC. " +
                                        "Over 80,000 students are enrolled in the public system, with approximately 50% attending district and 50% attending charter schools." +
                                        "<br/><br/>Private schools include independent private schools, faith-based schools, and nonpublic schools that are certified as a special education facility.";

        modelAndView.addObject("city", WordUtils.capitalizeFully(city));
        modelAndView.addObject("state", state);
        modelAndView.addObject("hubId", collectionId);
        modelAndView.addObject("collectionId", collectionId);
        modelAndView.addObject("underHeadindText", underHeadindText);

        final List<HubConfig> configList = getCityHubHelper().getConfigListFromCollectionId(collectionId);
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
        List<EnrollmentModel> enrollmentInfo = getEnrollmentFacade(request, configList, collectionId, state, city);
        modelAndView.addObject("enrollmentsInfo", enrollmentInfo);

        return modelAndView;
    }



    /**
     * Get Step facade for the UX .
     * @return stepsInfo List of StepInfo passed to model.
     */
    private List<EnrollmentModel> getEnrollmentFacade(HttpServletRequest request, List<HubConfig> configList,
                                                      Integer collectionId, State state, String city) {
        List<EnrollmentModel> enrollmentInfo = new ArrayList<EnrollmentModel>();

        String collectionNickname = _cityHubHelper.getCollectionNicknameFromConfigList(configList, collectionId);

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
        publicPreschools.setDescription("Since preschool (starting at age 3) and pre-kindergarten (starting at age 4) attendance is not mandatory, students are not automatically assigned a school. " +
                                        "To enroll your child, you must apply through a lottery.<br/><br/>" +
                                        "Your child must meet legal requirements for age eligibility in order to enroll at DCPS:<br/>" +
                                        "<ul>" +
                                        "<li>Preschool: Your child must turn 3 years of age on or before September 30 to be eligible for preschool.</li>" +
                                        "<li>Pre-kindergarten: Your child must turn 4 years of age on or before September 30 to be eligible for pre-kindergarten.</li>" +
                                        "</ul><br/><br/>" +
                                        "DCPS does not make exceptions for children born after September 30 due to the popularity of the programs and a limited number of seats." +
                                        " Each child&#180;s date of birth is verified upon enrollment into school.");

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
        privatePreschools.setDescription("There are many options for private early childhood programs as well as pre-schools that feed into private " +
                                            "schools. Private school application deadlines vary by school. You should visit the school directly or check out the GreatSchools official " +
                                            "school profile for more information. \n" +
                                            "During Catholic Schools Week, most Catholic schools hold open houses for prospective students and families. \n");

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
        publicElementarySchool.setDescription("Starting in kindergarten, every child is assigned to and is guaranteed a spot in a neighborhood school " +
                                        "based on your home address. Public schools are free to every child that is a DC resident. To apply to a " +
                                        "different school, there is the 'out of boundary' process.<br/><br/>" +
                                        "There are no required applications to attend your assigned neighborhood DCPS school. You must complete the " +
                                        "enrollment process by contacting the school directly. Proof of residence is required.<br/><br/>" +
                                        "Students entering kindergarten in DCPS must be age 5 by September 30.<br/><br/>" +
                                        "DC 'out of boundary' public schools and DC charter schools have a new lottery system for applying to schools " +
                                        "for the 2014-2015 school year, called My School DC. The first round of applications will be available on Dec. 16, 2013.<br/><br/>" +
                                        "If you are interested in applying to a charter school that is not participating in the My School DC lottery, visit the" +
                                        " GreatSchools Official School Profile or contact the school directly.");

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
        privateElementarySchool.setDescription("Private schools are non-public schools that charge tuition for attendance. Many have school-based aid and/or accept the " +
                                                "opportunity scholarships or other outside forms of financial aid. Some private schools have religious affiliations. Students " +
                                                "who want to attend private schools must apply, and there may be required entrance exams and application fees. <br/><br/>" +
                                                "The archdiocese has a separate application process for tuition assistance. This application is usually due in December and is based " +
                                                "on household income and financial need.<br/><br/>" +
                                                "Most require independent testing for entrance. Check their websites or make sure you ask about testing centers and dates. Most schools " +
                                                "will offer you an approved list of examiners.<br/><br/>" +
                                                "Most private schools are PK-8 or PK-12 so you should take that into consideration when shopping around. Private school application " +
                                                "deadlines vary by school. You should visit the school directly or check out the GreatSchools official school profile for more information.");

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
        publicMiddleSchool.setDescription("Starting in kindergarten, every child is assigned to and is guaranteed a spot in a neighborhood school " +
                "based on your home address. Public schools are free to every child that is a DC resident. To apply to a " +
                "different school, there is the “out of boundary” process.<br/><br/>" +
                "There are no required applications to attend your assigned neighborhood DCPS school. You must complete the " +
                "enrollment process by contacting the school directly. Proof of residence is required.<br/><br/>" +
                "Students entering kindergarten in DCPS must be age 5 by September 30.<br/><br/>" +
                "DC “out of boundary” public schools and DC charter schools have a new lottery system for applying to schools " +
                "for the 2014-2015 school year, called My School DC. The first round of applications will be available on Dec. 16, 2013.<br/><br/>" +
                "If you are interested in applying to a charter school that is not participating in the My School DC lottery, visit the" +
                " GreatSchools Official School Profile or contact the school directly.");

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
        privateMiddleSchool.setDescription("Private schools are non-public schools that charge tuition for attendance. Many have school-based aid and/or accept the " +
                "opportunity scholarships or other outside forms of financial aid. Some private schools have religious affiliations. Students " +
                "who want to attend private schools must apply, and there may be required entrance exams and application fees.Private school application deadlines vary " +
                "by school. You should visit the school directly or check out the GreatSchools official school profile for more information.");

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
        publicHighSchool.setDescription("Starting in kindergarten, every child is assigned to and is guaranteed a spot in a neighborhood school " +
                "based on your home address. Public schools are free to every child that is a DC resident. To apply to a " +
                "different school, there is the “out of boundary” process.<br/><br/>" +
                "There are no required applications to attend your assigned neighborhood DCPS school. You must complete the " +
                "enrollment process by contacting the school directly. Proof of residence is required.<br/><br/>" +
                "Students entering kindergarten in DCPS must be age 5 by September 30.<br/><br/>" +
                "DC “out of boundary” public schools and DC charter schools have a new lottery system for applying to schools " +
                "for the 2014-2015 school year, called My School DC. The first round of applications will be available on Dec. 16, 2013.<br/><br/>" +
                "There are some DCPS specialized citywide high schools that have different application dates.<br/><br/>" +

                "If you are interested in applying to a charter school that is not participating in the My School DC lottery, visit the" +
                " GreatSchools Official School Profile or contact the school directly.");

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
        tipsForPublicMiddleSchool.add("Every elementary school feeds into a middle school, which in turn feeds into a high school. These are called destination schools." +
                " Even if your child attends an elementary school as an out-of-boundary student, he still has the right to attend the destination schools of that school. So you might want to give higher priority to out-of-boundary schools that feed into the destination school of your choice.");

        publicHighSchool.setTipsInfoModel(tipsForPublicMiddleSchool);


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
        privateHighSchool.setDescription("Private schools are non-public schools that charge tuition for attendance. Many have school-based aid and/or accept the " +
                "opportunity scholarships or other outside forms of financial aid. Some private schools have religious affiliations. Students " +
                "who want to attend private schools must apply, and there may be required entrance exams and application fees.Private school application deadlines vary " +
                "by school. You should visit the school directly or check out the GreatSchools official school profile for more information.<br/><br/>" +
                "Many private high schools offer merit based scholarships for academics, fine arts, sports, and more. Make sure you ask about these options when you visit. ");

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

    public String getBrowseLinkAnchorText(String collectionNickname, String schoolType, String tabName) {
        StringBuilder anchorText = null;

        if(collectionNickname != null && !"".equals(collectionNickname.trim())
                && schoolType != null && !"".equals(schoolType.trim())
                && tabName != null && !"".equals(tabName.trim())) {
            anchorText = new StringBuilder("Browse ");
            anchorText.append(collectionNickname + " ");
            anchorText.append(schoolType.toLowerCase() + " ");
            anchorText.append(tabName.toLowerCase());
        }

        return anchorText != null ? anchorText.toString() : null;
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
