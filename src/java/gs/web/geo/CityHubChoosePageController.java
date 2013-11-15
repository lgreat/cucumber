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
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.data.url.DirectoryStructureUrlFactory;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the City Hub Choose  Pages.
 * @author sarora@greatschools.org Shomi Arora.
 */



@Controller
public class CityHubChoosePageController  implements IDirectoryStructureUrlController {


    private  static final String PARAM_CITY = "city";



    @Autowired
    private CityHubHelper _cityHubHelper;



    @RequestMapping(method= RequestMethod.GET)
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        ModelAndView modelAndView = new ModelAndView("/cityHub/choosing-schools");
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
        modelAndView.addObject("city", WordUtils.capitalizeFully(city));
        modelAndView.addObject("state", state);
        modelAndView.addObject("hubId", collectionId);
        modelAndView.addObject("collectionId", collectionId);

        List<HubConfig> configList = getCityHubHelper().getConfigListFromCollectionId(collectionId);
        /**
         * Get the important events
         */
        ModelMap importantEventsMap = getCityHubHelper().getImportantModuleMap(configList);
        modelAndView.addObject(CityHubHelper.IMPORTANT_EVENT_KEY_PREFIX, importantEventsMap);

        final String collectionNickname = getCityHubHelper().getCollectionNicknameFromConfigList(configList, collectionId);


        /**
         * Get Step Info  .
         */
        List<StepModel> stepsInfo = getStepFacades(collectionNickname);
        modelAndView.addObject("stepsInfo", stepsInfo);

        modelAndView.addObject(CityHubHelper.COLLECTION_NICKNAME_MODEL_KEY,
                collectionNickname);

        return modelAndView;


    }

    /**
     * Get Step facade for the UX .
     * @param  collectionNickName collectionNickName.
     * @return stepsInfo List of StepInfo passed to model.
     */
    private List<StepModel> getStepFacades(final String collectionNickName) {

        List<StepModel> stepsInfo = new ArrayList<StepModel>();

        //Adding Step Model for Step 1 -Potential to be Templatized in Future

        StepModel step1= new StepModel(1, "Think about your needs", "/res/img/cityHubs/choosingStep1.png",
                                          "As you think about choosing a school, consider what matters most to your family. " +
                                          "What do you value the highest when it comes to your child&#39;s education? " +
                                          "How does your choice of school impact your family or your finances? " +
                                          "We&#39;re here to help you take the first step.");

        ArrayList<FeaturedResourcesModel> featuredResourcesModelStep1 = new ArrayList<FeaturedResourcesModel>();

        FeaturedResourcesModel feature1Step1= new FeaturedResourcesModel("Imagining your ideal school",
                                       "/find-a-school/defining-your-ideal/32-imagining-your-ideal-school-set-your-priorities.gs",
                                        "Article");
        FeaturedResourcesModel feature2Step1= new FeaturedResourcesModel("Download our worksheet", "/pdf/choosing_imagine.pdf", "Article");

        featuredResourcesModelStep1.add(feature1Step1);
        featuredResourcesModelStep1.add(feature2Step1);


        step1.setFeaturedResourcesModel(featuredResourcesModelStep1);


        ArrayList<AdditionalResourcesModel> additionalResourcesModelStep1 = new ArrayList<AdditionalResourcesModel>();

        AdditionalResourcesModel additionalResources1Step1 =  new AdditionalResourcesModel("When should kids start kindergarten?", "/students/academic-skills/4165-redshirting-kindergarten.gs", "Article", "1");
        AdditionalResourcesModel additionalResources1Step2 =  new AdditionalResourcesModel("Switch schools or stay?", "/find-a-school/making-the-right-choice/4904-switch-or-stay-schools-early-in-year.gs", "Article", "1");
        AdditionalResourcesModel additionalResources1Step3 =  new AdditionalResourcesModel("The pros and cons of skipping a grade", "/students/4151-Skipping-a-grade-pros-and-cons.gs", "Article", "1");
        AdditionalResourcesModel additionalResources1Step4 =  new AdditionalResourcesModel("Preschool philosophies: what are they?", "/find-a-school/defining-your-ideal/1111-preschool-philosophies.gs", "Article", "2");
        AdditionalResourcesModel additionalResources1Step5 =  new AdditionalResourcesModel("A guide to second-language education ", "/school-choice/language-oriented-schools/7067-second-language-education-video.gs", "Video", "2");
        AdditionalResourcesModel additionalResources1Step6 =  new AdditionalResourcesModel("Quick guide to special education ", "/school-choice/special-education/7006-quick-guide-special-education-video.gs", "Video", "2");
        AdditionalResourcesModel additionalResources1Step7 =  new AdditionalResourcesModel("Special needs programs and schools: a primer", "/school-choice/special-education/6997-special-education-special-needs-learning-disabilities.gs", "Article", "3");
        AdditionalResourcesModel additionalResources1Step8 =  new AdditionalResourcesModel("Moving? Tips to help your child with the transition", "/find-a-school/moving/173-help-your-child-with-the-transition.gs?page=all", "Article", "3");
        AdditionalResourcesModel additionalResources1Step9 =  new AdditionalResourcesModel("Your child is gifted...now what?", "/parenting/learning-development/7088-gifted-and-talented-education-and-program.gs", "Article", "3");
        AdditionalResourcesModel additionalResources1Step10 = new AdditionalResourcesModel("Searching for security", "/find-a-school/defining-your-ideal/1690-sizing-up-school-safety.gs", "Article", "3");


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
        StepModel step2= new StepModel(2, "Understand your options", "/res/img/cityHubs/choosingStep2.png",
                                          "In " + collectionNickName + ", there are a variety of schools to choose from - public, public charter, and " +
                                          "private. When deciding between schools, learn what makes each type of school different, " +
                                          "and how that fits with your overall needs and goals.");

        ArrayList<FeaturedResourcesModel> featuredResourcesModelStep2 = new ArrayList<FeaturedResourcesModel>();

        FeaturedResourcesModel feature1Step2= new FeaturedResourcesModel(collectionNickName+ " education system", "../education-community", "Local Page");

        FeaturedResourcesModel feature2Step2= new FeaturedResourcesModel(collectionNickName+ " enrollment information", "../enrollment", "Local Page");


        featuredResourcesModelStep2.add(feature1Step2);
        featuredResourcesModelStep2.add(feature2Step2);


        step2.setFeaturedResourcesModel(featuredResourcesModelStep2);



        ArrayList<AdditionalResourcesModel> additionalResourcesModelStep2 = new ArrayList<AdditionalResourcesModel>();

        AdditionalResourcesModel additionalResources2Header1 =  new AdditionalResourcesModel(null, null, null, "1", "Overview of options");
        AdditionalResourcesModel additionalResources2Step1 =  new AdditionalResourcesModel("School choice: what are your options?", "/find-a-school/defining-your-ideal/1163-school-choice-your-options.gs", "Article", "1");
        AdditionalResourcesModel additionalResources2Step2 =  new AdditionalResourcesModel("Public vs. private vs. charter schools", "/school-choice/6987-public-private-charter-schools.gs", "Article", "1");
        AdditionalResourcesModel additionalResources2Step3 =  new AdditionalResourcesModel("Private vs. public", "/find-a-school/defining-your-ideal/59-private-vs-public-schools.gs", "Article", "1");
        AdditionalResourcesModel additionalResources2Step4 =  new AdditionalResourcesModel("School choice terminology", "/find-a-school/2424-school-terminology.gs", "Article", "1");

        AdditionalResourcesModel additionalResources2Header2 =  new AdditionalResourcesModel(null, null, null, "2", "Public & public charter schools");
        AdditionalResourcesModel additionalResources2Step5 =  new AdditionalResourcesModel("Public schools: fact and fiction", "/school-choice/public-schools/6979-public-school.gs", "Article", "2");
        AdditionalResourcesModel additionalResources2Step6 =  new AdditionalResourcesModel("What&#39;s a charter school anyway? ", "/school-choice/charter-schools/6974-what-is-charter-school-video.gs", "Video", "2");
        AdditionalResourcesModel additionalResources2Step7 =  new AdditionalResourcesModel("The truth about charter schools", "/school-choice/charter-schools/6986-charter-schools.gs", "Article", "2");
        AdditionalResourcesModel additionalResources2Step8 =  new AdditionalResourcesModel("7 essentials about charter schools", "/find-a-school/defining-your-ideal/192-seven-facts-about-charter-schools.gs", "Article", "2");
        AdditionalResourcesModel additionalResources2Step9 =  new AdditionalResourcesModel("Are charter schools better?", "/find-a-school/3706-charter-schools-better-than-traditional.gs", "Article", "2");

        AdditionalResourcesModel additionalResources2Header3 =  new AdditionalResourcesModel(null, null, null, "3", "Private schools");
        AdditionalResourcesModel additionalResources2Step10 = new AdditionalResourcesModel("A guide to private schools", "/school-choice/private-schools/7068-private-schools-video.gs", "Video", "3");
        AdditionalResourcesModel additionalResources2Step11 = new AdditionalResourcesModel("Should I send my child to a private school?", "/school-choice/private-schools/6995-private-schools-parochial-schools.gs", "Article", "3");

        additionalResourcesModelStep2.add(additionalResources2Header1);
        additionalResourcesModelStep2.add(additionalResources2Step1);
        additionalResourcesModelStep2.add(additionalResources2Step2);
        additionalResourcesModelStep2.add(additionalResources2Step3);
        additionalResourcesModelStep2.add(additionalResources2Step4);
        additionalResourcesModelStep2.add(additionalResources2Header2);
        additionalResourcesModelStep2.add(additionalResources2Step5);
        additionalResourcesModelStep2.add(additionalResources2Step6);
        additionalResourcesModelStep2.add(additionalResources2Step7);
        additionalResourcesModelStep2.add(additionalResources2Step8);
        additionalResourcesModelStep2.add(additionalResources2Step9);
        additionalResourcesModelStep2.add(additionalResources2Header3);
        additionalResourcesModelStep2.add(additionalResources2Step10);
        additionalResourcesModelStep2.add(additionalResources2Step11);

        step2.setAdditionalResourcesModel(additionalResourcesModelStep2);



        stepsInfo.add(step2);


        //Adding Step Model for Step 3 -Potential to be Templatized in Future
        StepModel step3= new StepModel(3 , "Find schools that fit", "/res/img/cityHubs/choosingStep3.png",
                        "As you search for schools that fit your family, look for information in three key areas - " +
                        "academic performance, extracurricular programs, and school culture and community. ");


        ArrayList<FeaturedResourcesModel> featuredResourcesModelStep3 = new ArrayList<FeaturedResourcesModel>();

        FeaturedResourcesModel feature1Step3= new FeaturedResourcesModel("Try our advanced school search", "../schools/", "Local Page", "GreatSchools provides independent ratings based on a mixture of factors. You should consider GreatSchools ratings along with local and state ratings.");
        FeaturedResourcesModel feature2Step3= new FeaturedResourcesModel("OSSE Report Cards", "/", "External Page");
        FeaturedResourcesModel feature3Step3= new FeaturedResourcesModel("PMF Rating", "http://www.dcpcsb.org/SearchSchools.aspx", "External Page");
        FeaturedResourcesModel feature4Step3= new FeaturedResourcesModel("DCPS scorecard", "http://profiles.dcps.dc.gov/", "External Page");



        featuredResourcesModelStep3.add(feature1Step3);
        featuredResourcesModelStep3.add(feature2Step3);
        featuredResourcesModelStep3.add(feature3Step3);
        featuredResourcesModelStep3.add(feature4Step3);



        step3.setFeaturedResourcesModel(featuredResourcesModelStep3);

        ArrayList<AdditionalResourcesModel> additionalResourcesModelStep3 = new ArrayList<AdditionalResourcesModel>();


        AdditionalResourcesModel additionalResources3Step1 =  new AdditionalResourcesModel("Facts (and fiction) about school test scores", "/school-choice/standardized-testing/7262-school-test-scores-video.gs", "Video", "1");
        AdditionalResourcesModel additionalResources3Step2 =  new AdditionalResourcesModel("How important is cultural diversity at your school?", "/find-a-school/defining-your-ideal/284-cultural-diversity-at-school.gs", "Article", "1");
        AdditionalResourcesModel additionalResources3Step3 =  new AdditionalResourcesModel("How important is class size?", "/find-a-school/defining-your-ideal/174-class-size.gs", "Article", "1");
        AdditionalResourcesModel additionalResources3Step4 =  new AdditionalResourcesModel("How important is school size?", "/find-a-school/defining-your-ideal/528-school-size.gs", "Article", "2");
        AdditionalResourcesModel additionalResources3Step5 =  new AdditionalResourcesModel("The ABCs of picking a preschool", "/preschool/slideshows/7268-why-preschool.gs", "Article", "2");
        AdditionalResourcesModel additionalResources3Step6 =  new AdditionalResourcesModel("Pitfalls of picking a preschool", "/find-a-school/defining-your-ideal/3643-mistakes-choosing-preschool.gs", "Article", "2");
        AdditionalResourcesModel additionalResources3Step7 =  new AdditionalResourcesModel("Pitfalls of picking an elementary school", "/find-a-school/3644-mistakes-choosing-elementary.gs", "Article", "3");
        AdditionalResourcesModel additionalResources3Step8 =  new AdditionalResourcesModel("Pitfalls of picking a middle school", "/find-a-school/3646-mistakes-choosing-middle-school.gs", "Article", "3");
        AdditionalResourcesModel additionalResources3Step9 =  new AdditionalResourcesModel("Pitfalls of picking a high school", "/find-a-school/3647-mistake-choosing-highschool.gs", "Article", "3");

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
        StepModel step4= new StepModel(4 , "Visit schools you like", "/res/img/cityHubs/choosingStep4.png",
                        "Take time to visit the schools at the top of your list. Visiting is the best way to tell whether " +
                        "you and your child feel welcomed and comfortable at each school. You also have a chance to observe " +
                        "teachers in action, and see whether students are actively engaged in learning.");


        ArrayList<FeaturedResourcesModel> featuredResourcesModelStep4 = new ArrayList<FeaturedResourcesModel>();

        FeaturedResourcesModel feature1Step4= new FeaturedResourcesModel("The school visit: what to look for, what to ask",
                                                 "/find-a-school/school-visit/24-the-school-visit-what-to-look-for-what-to-ask.gs", "Article");
        FeaturedResourcesModel feature2Step4= new FeaturedResourcesModel("Choosing a school from a distance",
                                                  "/find-a-school/moving/3986-choosing-a-school-from-a-distance.gs", "Article");

        featuredResourcesModelStep4.add(feature1Step4);
        featuredResourcesModelStep4.add(feature2Step4);

        step4.setFeaturedResourcesModel(featuredResourcesModelStep4);


        ArrayList<AdditionalResourcesModel> additionalResourcesModelStep4 = new ArrayList<AdditionalResourcesModel>();

        AdditionalResourcesModel additionalResources4Header1 =   new AdditionalResourcesModel(null, null, null, "1", "Preschool resources");
        AdditionalResourcesModel additionalResources4Step1 =   new AdditionalResourcesModel("10 key questions for preschools", "/find-a-school/slideshows/3457-choosing-a-preschool.gs", "Article", "1");
        AdditionalResourcesModel additionalResources4Step2 =   new AdditionalResourcesModel("Insider tricks for assessing preschools", "/find-a-school/defining-your-ideal/1627-insider-tricks-for-assessing-preschools.gs", "Article", "1");

        AdditionalResourcesModel additionalResources4Header2 =   new AdditionalResourcesModel(null, null, null, "1", "Elementary school resources");
        AdditionalResourcesModel additionalResources4Step3 =   new AdditionalResourcesModel("Finding the right elementary school", "/find-a-school/making-the-right-choice/7055-choose-elementary-school-video.gs", "Video", "1");
        AdditionalResourcesModel additionalResources4Step4 =   new AdditionalResourcesModel("10 key questions for elementary schools", "/find-a-school/slideshows/3469-choosing-an-elementary-school.gs", "Article", "1");
        AdditionalResourcesModel additionalResources4Step5 =   new AdditionalResourcesModel("Insider tricks for assessing elementary schools", "/find-a-school/defining-your-ideal/1628-insider-tricks-for-assessing-elementary-schools.gs", "Article", "1");
        AdditionalResourcesModel additionalResources4Step6 =   new AdditionalResourcesModel("Elementary school visit checklist", "/catalog/pdf/schq_visit_elem.pdf", "Article", "1");

        AdditionalResourcesModel additionalResources4Header3 =   new AdditionalResourcesModel(null, null, null, "2", "Middle school resources");
        AdditionalResourcesModel additionalResources4Step7 =   new AdditionalResourcesModel("How to find the right middle school", "/find-a-school/school-visit/7056-choose-middle-school-video.gs", "Video", "2");
        AdditionalResourcesModel additionalResources4Step8 =   new AdditionalResourcesModel("10 key questions for middle schools", "/find-a-school/slideshows/3436-choosing-a-middle-school.gs", "Article", "2");
        AdditionalResourcesModel additionalResources4Step9 =   new AdditionalResourcesModel("Insider tricks for assessing middle schools", "/find-a-school/defining-your-ideal/1629-insider-tricks-for-assessing-middle-schools.gs", "Article", "2");
        AdditionalResourcesModel additionalResources4Step10 =  new AdditionalResourcesModel("Middle school visit checklist", "/catalog/pdf/schq_visit_middle.pdf", "Article", "2");

        AdditionalResourcesModel additionalResources4Header4 =   new AdditionalResourcesModel(null, null, null, "3", "High school resources");
        AdditionalResourcesModel additionalResources4Step11 =  new AdditionalResourcesModel("How to choose the right high school", "/find-a-school/making-the-right-choice/7066-choose-high-school-video.gs", "Video", "3");
        AdditionalResourcesModel additionalResources4Step12 =  new AdditionalResourcesModel("10 key questions for high schools", "/find-a-school/slideshows/3446-choosing-a-high-school.gs", "Article", "3");
        AdditionalResourcesModel additionalResources4Step13 =  new AdditionalResourcesModel("Insider tricks for assessing high schools", "/find-a-school/defining-your-ideal/1630-insider-tricks-for-assessing-high-schools.gs", "Article", "3");
        AdditionalResourcesModel additionalResources4Step14 =  new AdditionalResourcesModel("High school visit checklist", "/catalog/pdf/schq_visit_high.pdf", "Article", "3");

        additionalResourcesModelStep4.add(additionalResources4Header1);
        additionalResourcesModelStep4.add(additionalResources4Step1);
        additionalResourcesModelStep4.add(additionalResources4Step2);
        additionalResourcesModelStep4.add(additionalResources4Header2);
        additionalResourcesModelStep4.add(additionalResources4Step3);
        additionalResourcesModelStep4.add(additionalResources4Step4);
        additionalResourcesModelStep4.add(additionalResources4Step5);
        additionalResourcesModelStep4.add(additionalResources4Step6);
        additionalResourcesModelStep4.add(additionalResources4Header3);
        additionalResourcesModelStep4.add(additionalResources4Step7);
        additionalResourcesModelStep4.add(additionalResources4Step8);
        additionalResourcesModelStep4.add(additionalResources4Step9);
        additionalResourcesModelStep4.add(additionalResources4Step10);
        additionalResourcesModelStep4.add(additionalResources4Header4);
        additionalResourcesModelStep4.add(additionalResources4Step11);
        additionalResourcesModelStep4.add(additionalResources4Step12);
        additionalResourcesModelStep4.add(additionalResources4Step13);
        additionalResourcesModelStep4.add(additionalResources4Step14);

        step4.setAdditionalResourcesModel(additionalResourcesModelStep4);

        stepsInfo.add(step4);


        //Adding Step Model for Step 5 -Potential to be Templatized in Future
        StepModel step5= new StepModel(5 , "Apply and enroll", "/res/img/cityHubs/choosingStep5.png",
                        "Once you&#39;ve made your choices, find out the details of the enrollment process for each school " +
                        "and give yourself enough time to complete and submit your applications. You may need to gather " +
                        "important documents like a birth certificate or proof of residency.  Also, your first choice " +
                        "school may not be available, so you will want to apply to at least two other schools that meet " +
                        "all or most of your needs.");

        ArrayList<FeaturedResourcesModel> featuredResourcesModelStep5 = new ArrayList<FeaturedResourcesModel>();

        FeaturedResourcesModel feature1Step5= new FeaturedResourcesModel(collectionNickName+ " Enrollment Info", "../enrollment", "Local Page");
        featuredResourcesModelStep5.add(feature1Step5);
        step5.setFeaturedResourcesModel(featuredResourcesModelStep5);
        stepsInfo.add(step5);

        return stepsInfo;
    }


    public boolean shouldHandleRequest(final DirectoryStructureUrlFields fields) {
        return fields == null ? false : fields.hasState() && fields.hasCityName() && fields.hasChoosePage()  && !fields.hasDistrictName() && !fields.hasLevelCode() && !fields.hasSchoolName();

    }


    public CityHubHelper getCityHubHelper() {
        return _cityHubHelper;
    }

    public void setCityHubHelper(final CityHubHelper cityHubHelper) {
        this._cityHubHelper = cityHubHelper;
    }
}
