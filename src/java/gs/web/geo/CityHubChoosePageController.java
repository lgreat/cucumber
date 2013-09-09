package gs.web.geo;

/**
 * Created with IntelliJ IDEA.
 * User: sarora
 * Date: 8/30/13
 * Time: 3:38 PM
 * To change this template use File | Settings | File Templates.
 */

import gs.data.hubs.HubConfig;
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
        importantEventsMap.put(getCityHubHelper().CONFIG_KEY_PREFIXES_WITH_INDEX_MODEL_KEY, configKeyPrefixesSortedByDate);
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
