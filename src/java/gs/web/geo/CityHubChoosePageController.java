package gs.web.geo;

/**
 * Created with IntelliJ IDEA.
 * User: sarora
 * Date: 8/30/13
 * Time: 3:38 PM
 * To change this template use File | Settings | File Templates.
 */

import gs.data.hubs.HubConfig;
import gs.web.hub.AdditionalResourcesModel;
import gs.web.hub.FeaturedResourcesModel;
import gs.web.hub.StepModel;
import gs.data.state.State;
import org.apache.commons.lang.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the City Hub Choose  Pages.
 * @author sarora@greatschools.org Shomi Arora.
 */

@Controller
public class CityHubChoosePageController {


    private  static final String PARAM_CITY = "city";
    @Autowired
    private CityHubHelper _cityHubHelper;

    @RequestMapping(method= RequestMethod.GET)
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        ModelAndView modelAndView = new ModelAndView("/cityHub/choosePage");
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

        modelAndView.addObject("city", WordUtils.capitalizeFully(city));
        modelAndView.addObject("state", state);
        modelAndView.addObject("hubId", getCityHubHelper().getHubID(city, state));

        /**
         * Get the important events
         */
        ModelMap importantEventsMap = getModelMap(state, city);
        modelAndView.addObject(CityHubHelper.IMPORTANT_EVENT_KEY_PREFIX, importantEventsMap);


        /**
         * Get Step Info  .
         */
        List<StepModel> stepsInfo = getStepFacades();
        modelAndView.addObject("stepsInfo", stepsInfo);



        return modelAndView;


    }

    /**
     * Get Step facade for the UX .
     * @return stepsInfo List of StepInfo passed to model.
     */
    private List<StepModel> getStepFacades() {

        List<StepModel> stepsInfo = new ArrayList<StepModel>();

        //Adding Step Model for Step 1 -Potential to be Templatized in Future

        StepModel step1= new StepModel(1, "Think about your needs", "/res/img/cityHubs/choose_a_school.png",
                                      "Where your child goes to school affects not just your child but your whole family." +
                                       " As you begin thinking about choosing a school, consider what matters most. " +
                                       "What needs does your child have? What needs does your family have? What is " +
                                       "important to you when it comes to education?");

        ArrayList<FeaturedResourcesModel> featuredResourcesModelStep1 = new ArrayList<FeaturedResourcesModel>();

        FeaturedResourcesModel feature1Step1= new FeaturedResourcesModel("Imagining your ideal school",
                                       "http://www.greatschools.org/find-a-school/defining-your-ideal/32-imagining-your-ideal-school-set-your-priorities.gs",
                                        "Article");
        FeaturedResourcesModel feature2Step1= new FeaturedResourcesModel("Download our worksheet", "http://www.greatschools.org/pdf/choosing_imagine.pdf", "Article");

        featuredResourcesModelStep1.add(feature1Step1);
        featuredResourcesModelStep1.add(feature2Step1);


        step1.setFeaturedResourcesModel(featuredResourcesModelStep1);


        ArrayList<AdditionalResourcesModel> additionalResourcesModelStep1 = new ArrayList<AdditionalResourcesModel>();

        AdditionalResourcesModel additionalResources1Step1 =  new AdditionalResourcesModel("When should kids start kindergarten?", "http://www.greatschools.org/students/academic-skills/4165-redshirting-kindergarten.gs", "Article", "1");
        AdditionalResourcesModel additionalResources1Step2 =  new AdditionalResourcesModel("Switch schools or stay?", "http://www.greatschools.org/find-a-school/making-the-right-choice/4904-switch-or-stay-schools-early-in-year.gs", "Article", "1");
        AdditionalResourcesModel additionalResources1Step3 =  new AdditionalResourcesModel("The pros and cons of skipping a grade", "http://www.greatschools.org/students/4151-Skipping-a-grade-pros-and-cons.gs", "Article", "1");
        AdditionalResourcesModel additionalResources1Step4 =  new AdditionalResourcesModel("Preschool philosophies: what are they?", "http://www.greatschools.org/find-a-school/defining-your-ideal/1111-preschool-philosophies.gs", "Article", "2");
        AdditionalResourcesModel additionalResources1Step5 =  new AdditionalResourcesModel("A guide to second-language education ", "http://www.greatschools.org/school-choice/language-oriented-schools/7067-second-language-education-video.gs", "Video", "2");
        AdditionalResourcesModel additionalResources1Step6 =  new AdditionalResourcesModel("Quick guide to special education ", "http://www.greatschools.org/school-choice/special-education/7006-quick-guide-special-education-video.gs", "Video", "2");
        AdditionalResourcesModel additionalResources1Step7 =  new AdditionalResourcesModel("Special needs programs and schools: a primer", "http://www.greatschools.org/school-choice/special-education/6997-special-education-special-needs-learning-disabilities.gs", "Article", "3");
        AdditionalResourcesModel additionalResources1Step8 =  new AdditionalResourcesModel("Moving? Tips to help your child with the transition", "http://www.greatschools.org/find-a-school/moving/173-help-your-child-with-the-transition.gs?page=all", "Article", "3");
        AdditionalResourcesModel additionalResources1Step9 =  new AdditionalResourcesModel("Your child is gifted...now what?", "http://www.greatschools.org/parenting/learning-development/7088-gifted-and-talented-education-and-program.gs", "Article", "3");
        AdditionalResourcesModel additionalResources1Step10 = new AdditionalResourcesModel("Searching for security", "http://www.greatschools.org/find-a-school/defining-your-ideal/1690-sizing-up-school-safety.gs", "Article", "3");


        additionalResourcesModelStep1.add(additionalResources1Step1);
        additionalResourcesModelStep1.add(additionalResources1Step2);
        additionalResourcesModelStep1.add(additionalResources1Step3);
        additionalResourcesModelStep1.add(additionalResources1Step4);
        additionalResourcesModelStep1.add(additionalResources1Step5);
        additionalResourcesModelStep1.add(additionalResources1Step6);
        additionalResourcesModelStep1.add(additionalResources1Step7);
        additionalResourcesModelStep1.add(additionalResources1Step8);
        additionalResourcesModelStep1.add(additionalResources1Step9);
        additionalResourcesModelStep1.add(additionalResources1Step10);

        step1.setAdditionalResourcesModel(additionalResourcesModelStep1);


        stepsInfo.add(step1);


        //Adding Step Model for Step 2 -Potential to be Templatized in Future
        StepModel step2= new StepModel(2, "Understand your options", "/res/img/cityHubs/choose_a_school.png",
                     "There are a variety of schools to choose from &#150; public, public charter, and private." +
                     " To decide which school is best for your child, learn about the types of schools" +
                     " available and the differences between them.");

        ArrayList<FeaturedResourcesModel> featuredResourcesModelStep2 = new ArrayList<FeaturedResourcesModel>();

        FeaturedResourcesModel feature1Step2= new FeaturedResourcesModel("DC education system", "http://www.greatschools.org/", "Local Page");

        FeaturedResourcesModel feature2Step2= new FeaturedResourcesModel("DC enrollment information", "http://www.greatschools.org/", "Local Page");


        featuredResourcesModelStep2.add(feature1Step2);
        featuredResourcesModelStep2.add(feature2Step2);


        step2.setFeaturedResourcesModel(featuredResourcesModelStep2);



        ArrayList<AdditionalResourcesModel> additionalResourcesModelStep2 = new ArrayList<AdditionalResourcesModel>();

        AdditionalResourcesModel additionalResources2Step1 =  new AdditionalResourcesModel("School choice: what are your options?", "http://www.greatschools.org/find-a-school/defining-your-ideal/1163-school-choice-your-options.gs", "Article", "1", "Overview of options");
        AdditionalResourcesModel additionalResources2Step2 =  new AdditionalResourcesModel("Public vs. private vs. charter schools", "http://www.greatschools.org/school-choice/6987-public-private-charter-schools.gs", "Article", "1", "Overview of options");
        AdditionalResourcesModel additionalResources2Step3 =  new AdditionalResourcesModel("Private vs. public", "http://www.greatschools.org/find-a-school/defining-your-ideal/59-private-vs-public-schools.gs", "Article", "1", "Overview of options");
        AdditionalResourcesModel additionalResources2Step4 =  new AdditionalResourcesModel("School choice terminology", "http://www.greatschools.org/find-a-school/2424-school-terminology.gs", "Article", "1", "Overview of options");
        AdditionalResourcesModel additionalResources2Step5 =  new AdditionalResourcesModel("Public schools: fact and fiction", "http://www.greatschools.org/school-choice/public-schools/6979-public-school.gs", "Article", "2", "Public & public charter schools");
        AdditionalResourcesModel additionalResources2Step6 =  new AdditionalResourcesModel("What’s a charter school anyway? ", "http://www.greatschools.org/school-choice/charter-schools/6974-what-is-charter-school-video.gs", "Video", "2", "Public & public charter schools");
        AdditionalResourcesModel additionalResources2Step7 =  new AdditionalResourcesModel("The truth about charter schools", "http://www.greatschools.org/school-choice/charter-schools/6986-charter-schools.gs", "Article", "2", "Public & public charter schools");
        AdditionalResourcesModel additionalResources2Step8 =  new AdditionalResourcesModel("7 essentials about charter schools", "http://www.greatschools.org/find-a-school/defining-your-ideal/192-seven-facts-about-charter-schools.gs", "Article", "2", "Public & public charter schools");
        AdditionalResourcesModel additionalResources2Step9 =  new AdditionalResourcesModel("Are charter schools better?", "http://www.greatschools.org/find-a-school/3706-charter-schools-better-than-traditional.gs", "Article", "2", "Public & public charter schools");
        AdditionalResourcesModel additionalResources2Step10 = new AdditionalResourcesModel("A guide to private schools", "http://www.greatschools.org/school-choice/private-schools/7068-private-schools-video.gs", "Video", "3", "Private schools");
        AdditionalResourcesModel additionalResources2Step11 = new AdditionalResourcesModel("Should I send my child to a private school?", "http://www.greatschools.org/school-choice/private-schools/6995-private-schools-parochial-schools.gs", "Article", "3", "Private schools");


        additionalResourcesModelStep2.add(additionalResources2Step1);
        additionalResourcesModelStep2.add(additionalResources2Step2);
        additionalResourcesModelStep2.add(additionalResources2Step3);
        additionalResourcesModelStep2.add(additionalResources2Step4);
        additionalResourcesModelStep2.add(additionalResources2Step5);
        additionalResourcesModelStep2.add(additionalResources2Step6);
        additionalResourcesModelStep2.add(additionalResources2Step7);
        additionalResourcesModelStep2.add(additionalResources2Step8);
        additionalResourcesModelStep2.add(additionalResources2Step9);
        additionalResourcesModelStep2.add(additionalResources2Step10);
        additionalResourcesModelStep2.add(additionalResources2Step11);

        step2.setAdditionalResourcesModel(additionalResourcesModelStep2);



        stepsInfo.add(step2);


        //Adding Step Model for Step 3 -Potential to be Templatized in Future
        StepModel step3= new StepModel(3 , "Find schools that fit", "/res/img/cityHubs/choose_a_school.png",
                        "As you search for schools that fit your needs, look for information in three key areas &#150; academic " +
                        "performance, culture & climate, and programs offered.");


        ArrayList<FeaturedResourcesModel> featuredResourcesModelStep3 = new ArrayList<FeaturedResourcesModel>();

        FeaturedResourcesModel feature1Step3= new FeaturedResourcesModel("Try our advanced school search", "http://www.greatschools.org/", "Local Page");
        FeaturedResourcesModel feature2Step3= new FeaturedResourcesModel("OSSE Report Cards", "http://www.greatschools.org/", "External Page");
        FeaturedResourcesModel feature3Step3= new FeaturedResourcesModel("PMF Rating", "http://www.dcpcsb.org/SearchSchools.aspx", "External Page");
        FeaturedResourcesModel feature4Step3= new FeaturedResourcesModel("DCPS scorecard", "http://profiles.dcps.dc.gov/", "External Page");



        featuredResourcesModelStep3.add(feature1Step3);
        featuredResourcesModelStep3.add(feature2Step3);
        featuredResourcesModelStep3.add(feature3Step3);
        featuredResourcesModelStep3.add(feature4Step3);



        step3.setFeaturedResourcesModel(featuredResourcesModelStep3);

        ArrayList<AdditionalResourcesModel> additionalResourcesModelStep3 = new ArrayList<AdditionalResourcesModel>();

        AdditionalResourcesModel additionalResources3Step1 =  new AdditionalResourcesModel("Facts (and fiction) about school test scores", "http://www.greatschools.org/school-choice/standardized-testing/7262-school-test-scores-video.gs", "Video", "1");
        AdditionalResourcesModel additionalResources3Step2 =  new AdditionalResourcesModel("How important is cultural diversity at your school?", "http://www.greatschools.org/find-a-school/defining-your-ideal/284-cultural-diversity-at-school.gs", "Article", "1");
        AdditionalResourcesModel additionalResources3Step3 =  new AdditionalResourcesModel("How important is class size?", "http://www.greatschools.org/find-a-school/defining-your-ideal/174-class-size.gs", "Article", "1");
        AdditionalResourcesModel additionalResources3Step4 =  new AdditionalResourcesModel("How important is school size?", "http://www.greatschools.org/find-a-school/defining-your-ideal/528-school-size.gs", "Article", "2");
        AdditionalResourcesModel additionalResources3Step5 =  new AdditionalResourcesModel("The ABCs of picking a preschool", "http://www.greatschools.org/preschool/slideshows/7268-why-preschool.gs", "Article", "2");
        AdditionalResourcesModel additionalResources3Step6 =  new AdditionalResourcesModel("Pitfalls of picking a preschool", "http://www.greatschools.org/find-a-school/defining-your-ideal/3643-mistakes-choosing-preschool.gs", "Article", "2");
        AdditionalResourcesModel additionalResources3Step7 =  new AdditionalResourcesModel("Pitfalls of picking an elementary school", "http://www.greatschools.org/find-a-school/3644-mistakes-choosing-elementary.gs", "Article", "3");
        AdditionalResourcesModel additionalResources3Step8 =  new AdditionalResourcesModel("Pitfalls of picking a middle school", "http://www.greatschools.org/find-a-school/3646-mistakes-choosing-middle-school.gs", "Article", "3");
        AdditionalResourcesModel additionalResources3Step9 =  new AdditionalResourcesModel("Pitfalls of picking a high school", "http://www.greatschools.org/find-a-school/3647-mistake-choosing-highschool.gs", "Article", "3");

        additionalResourcesModelStep3.add(additionalResources3Step1);
        additionalResourcesModelStep3.add(additionalResources3Step2);
        additionalResourcesModelStep3.add(additionalResources3Step3);
        additionalResourcesModelStep3.add(additionalResources3Step4);
        additionalResourcesModelStep3.add(additionalResources3Step5);
        additionalResourcesModelStep3.add(additionalResources3Step6);
        additionalResourcesModelStep3.add(additionalResources3Step7);
        additionalResourcesModelStep3.add(additionalResources3Step8);
        additionalResourcesModelStep3.add(additionalResources3Step9);

        step3.setAdditionalResourcesModel(additionalResourcesModelStep3);


        stepsInfo.add(step3);



        //Adding Step Model for Step 4 -Potential to be Templatized in Future
        StepModel step4= new StepModel(4, "Visit schools you like", "/res/img/cityHubs/choose_a_school.png",
                        "Now that you&#39;ve narrowed your choices, it&#39;s time to visit the schools you like. " +
                        "Visiting is the only way you can really tell whether you and your child will feel safe and" +
                        " comfortable at the school. You will also be able to see whether the teachers are enthusiastic " +
                         "and the students are engaged in learning.");


        ArrayList<FeaturedResourcesModel> featuredResourcesModelStep4 = new ArrayList<FeaturedResourcesModel>();

        FeaturedResourcesModel feature1Step4= new FeaturedResourcesModel("The school visit: what to look for, what to ask",
                                                 "http://www.greatschools.org/find-a-school/school-visit/24-the-school-visit-what-to-look-for-what-to-ask.gs", "Article");
        FeaturedResourcesModel feature2Step4= new FeaturedResourcesModel("Choosing a school from a distance",
                                                  "http://www.greatschools.org/find-a-school/moving/3986-choosing-a-school-from-a-distance.gs", "Article");

        featuredResourcesModelStep4.add(feature1Step4);
        featuredResourcesModelStep4.add(feature2Step4);

        step4.setFeaturedResourcesModel(featuredResourcesModelStep4);


        ArrayList<AdditionalResourcesModel> additionalResourcesModelStep4 = new ArrayList<AdditionalResourcesModel>();

        AdditionalResourcesModel additionalResources4Step1 =   new AdditionalResourcesModel("10 key questions for preschools", "http://www.greatschools.org/school-choice/standardized-testing/7262-school-test-scores-video.gs", "Article", "1", "Preschool resources");
        AdditionalResourcesModel additionalResources4Step2 =   new AdditionalResourcesModel("Insider tricks for assessing preschools", "http://www.greatschools.org/find-a-school/defining-your-ideal/284-cultural-diversity-at-school.gs", "Article", "1", "Preschool resources");
        AdditionalResourcesModel additionalResources4Step3 =   new AdditionalResourcesModel("Finding the right elementary school", "http://www.greatschools.org/find-a-school/defining-your-ideal/174-class-size.gs", "Video", "1", "Elementary school resources");
        AdditionalResourcesModel additionalResources4Step4 =   new AdditionalResourcesModel("10 key questions for elementary schools", "http://www.greatschools.org/find-a-school/defining-your-ideal/174-class-size.gs", "Video", "1", "Elementary school resources");
        AdditionalResourcesModel additionalResources4Step5 =   new AdditionalResourcesModel("Insider tricks for assessing elementary schools", "http://www.greatschools.org/find-a-school/defining-your-ideal/528-school-size.gs", "Article", "1", "Elementary school resources");
        AdditionalResourcesModel additionalResources4Step6 =   new AdditionalResourcesModel("Elementary school visit checklist", "http://www.greatschools.org/preschool/slideshows/7268-why-preschool.gs", "Article", "1", "Elementary school resources");
        AdditionalResourcesModel additionalResources4Step7 =   new AdditionalResourcesModel("How to find the right middle school", "http://www.greatschools.org/find-a-school/defining-your-ideal/3643-mistakes-choosing-preschool.gs", "Video", "1", "Elementary school resources");
        AdditionalResourcesModel additionalResources4Step8 =   new AdditionalResourcesModel("10 key questions for middle schools", "http://www.greatschools.org/find-a-school/3644-mistakes-choosing-elementary.gs", "Article", "2", "Middle school resources");
        AdditionalResourcesModel additionalResources4Step9 =   new AdditionalResourcesModel("Insider tricks for assessing middle schools", "http://www.greatschools.org/find-a-school/3646-mistakes-choosing-middle-school.gs", "Article", "3");
        AdditionalResourcesModel additionalResources4Step10 =  new AdditionalResourcesModel("Middle school visit checklist", "http://www.greatschools.org/find-a-school/3647-mistake-choosing-highschool.gs", "Article", "3");
        AdditionalResourcesModel additionalResources4Step11 =  new AdditionalResourcesModel("How to choose the right high school", "http://www.greatschools.org/find-a-school/3647-mistake-choosing-highschool.gs", "Video", "3");
        AdditionalResourcesModel additionalResources4Step12 =  new AdditionalResourcesModel("10 key questions for high schools", "http://www.greatschools.org/find-a-school/3647-mistake-choosing-highschool.gs", "Article", "3");
        AdditionalResourcesModel additionalResources4Step13 =  new AdditionalResourcesModel("Insider tricks for assessing high schools", "http://www.greatschools.org/find-a-school/3647-mistake-choosing-highschool.gs", "Article", "3");
        AdditionalResourcesModel additionalResources4Step14 =  new AdditionalResourcesModel("High school visit checklist", "http://www.greatschools.org/find-a-school/3647-mistake-choosing-highschool.gs", "Article", "3");

        additionalResourcesModelStep4.add(additionalResources4Step1);
        additionalResourcesModelStep4.add(additionalResources4Step2);
        additionalResourcesModelStep4.add(additionalResources4Step3);
        additionalResourcesModelStep4.add(additionalResources4Step4);
        additionalResourcesModelStep4.add(additionalResources4Step5);
        additionalResourcesModelStep4.add(additionalResources4Step6);
        additionalResourcesModelStep4.add(additionalResources4Step7);
        additionalResourcesModelStep4.add(additionalResources4Step8);
        additionalResourcesModelStep4.add(additionalResources4Step9);
        additionalResourcesModelStep4.add(additionalResources4Step10);
        additionalResourcesModelStep4.add(additionalResources4Step11);
        additionalResourcesModelStep4.add(additionalResources4Step12);
        additionalResourcesModelStep4.add(additionalResources4Step13);
        additionalResourcesModelStep4.add(additionalResources4Step14);

        step4.setAdditionalResourcesModel(additionalResourcesModelStep4);

        stepsInfo.add(step4);


        //Adding Step Model for Step 5 -Potential to be Templatized in Future
        StepModel step5= new StepModel(5 , "Apply and enroll", "/res/img/cityHubs/choose_a_school.png",
                        "Once you&#39;ve picked and ranked your top choices it&#39;s time to find out the details of the enrollment " +
                        "process for each, and submit your applications. Your first choice may not always be available, " +
                        "so you will want to apply to at least two other schools that meet all or most of your needs.");

        ArrayList<FeaturedResourcesModel> featuredResourcesModelStep5 = new ArrayList<FeaturedResourcesModel>();

        FeaturedResourcesModel feature1Step5= new FeaturedResourcesModel("DC Enrollment Info", "http://www.greatschools.org/", "Local Page");
        FeaturedResourcesModel feature2Step5= new FeaturedResourcesModel("DC Common Application", "http://www.greatschools.org/", "Local Page");

        featuredResourcesModelStep5.add(feature1Step5);
        featuredResourcesModelStep5.add(feature2Step5);

        step5.setFeaturedResourcesModel(featuredResourcesModelStep5);
        stepsInfo.add(step5);

        return stepsInfo;
    }


    private ModelMap getModelMap(final State state, final String city) {
        List<HubConfig> configList = getCityHubHelper().getHubConfig(city, state);
        ModelMap importantEventsMap = getCityHubHelper().getFilteredConfigMap(configList,  CityHubHelper.IMPORTANT_EVENT_KEY_PREFIX);
        List<String> configKeyPrefixesSortedByDate = getCityHubHelper().getConfigKeyPrefixesSortedByDate(importantEventsMap);
        importantEventsMap.put(CityHubHelper.CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY, configKeyPrefixesSortedByDate);
        importantEventsMap.put("maxImportantEventsToDisplay",  CityHubHelper.MAX_IMPORTANT_EVENTS_TO_DISPLAYED);
        return importantEventsMap;
    }

    public CityHubHelper getCityHubHelper() {
        return _cityHubHelper;
    }

    public void setCityHubHelper(final CityHubHelper _cityHubHelper) {
        this._cityHubHelper = _cityHubHelper;
    }
}