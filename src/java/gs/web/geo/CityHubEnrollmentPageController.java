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

    private  static final String PARAM_CITY = "city";
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

        modelAndView.addObject("city", WordUtils.capitalizeFully(city));
        modelAndView.addObject("state", state);
        modelAndView.addObject("hubId", collectionId);
        modelAndView.addObject("collectionId", collectionId);

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
        List<EnrollmentModel> enrollmentInfo = getEnrollmentFacade(request, collectionId, state, city);
        modelAndView.addObject("enrollmentsInfo", enrollmentInfo);

        return modelAndView;
    }



    /**
     * Get Step facade for the UX .
     * @return stepsInfo List of StepInfo passed to model.
     */
    private List<EnrollmentModel> getEnrollmentFacade(HttpServletRequest request, Integer collectionId, State state,
                                                      String city) {
        List<EnrollmentModel> enrollmentInfo = new ArrayList<EnrollmentModel>();

        Object[] solrQueryFilter = new Object[]{LevelCode.PRESCHOOL, SchoolType.PUBLIC, SchoolType.CHARTER};
        Anchor anchor = _anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, solrQueryFilter, state,
                city, "Browse DC public schools");
        EnrollmentModel enrollmentInfo1= new EnrollmentModel("Preschools", LevelCode.PRESCHOOL, SchoolType.PUBLIC ,
                "Public schools(neighborhood or district)", "blahh blahh ", anchor);
        // Tips
        ArrayList<String> tips1= new ArrayList<String>();
        tips1.add("tip1enrollmentInfo1");
        tips1.add("tip2enrollmentInfo1");
        enrollmentInfo1.setTipsInfoModel(tips1);


        // More Info

        MoreInformationModel  moreinfo1= new MoreInformationModel("3333 14th Street NW<br/> Suite 410</br> Washington D.C 20010</br><b>Phone :</b><br/>(202) 328-2660</br><b>Fax :</b>(202) 442-5026</br>");
        enrollmentInfo1.setMoreInfo(moreinfo1);

        solrQueryFilter = new Object[]{LevelCode.PRESCHOOL, SchoolType.CHARTER};
        anchor = _anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, solrQueryFilter, state, city,
                "Browse DC public charter schools");
        EnrollmentModel enrollmentInfo2= new EnrollmentModel("Preschools", LevelCode.PRESCHOOL, SchoolType.CHARTER ,
                "Public Charter School", "blahh blahh Public Charter School", anchor);
        // Tips
        ArrayList<String> tips2= new ArrayList<String>();
        tips2.add("tip1enrollmentInfo2");
        tips2.add("tip2enrollmentInfo2");
        enrollmentInfo2.setTipsInfoModel(tips2);

        // More Info
       ArrayList<Anchor>  links= new ArrayList<Anchor>();
       Anchor link = new Anchor("www.greatschools.org", "PLease Contact us 1");
       Anchor link1 = new Anchor("www.greatschools.org", "PLease Contact us 1");
       links.add(link);
       links.add(link1);


        MoreInformationModel  moreinfo2= new MoreInformationModel(links);
        enrollmentInfo2.setMoreInfo(moreinfo2);

        solrQueryFilter = new Object[]{LevelCode.PRESCHOOL, SchoolType.PRIVATE};
        anchor = _anchorListModelFactory.createBrowseLinksWithFilter(request, collectionId, solrQueryFilter, state, city,
                "Browse DC private schools");
        EnrollmentModel enrollmentInfo3= new EnrollmentModel("Preschools", LevelCode.PRESCHOOL, SchoolType.PRIVATE ,
                "Private School", "blahh blahh Private School", anchor);
        // Tips

        ArrayList<String> tips3= new ArrayList<String>();
        tips3.add("tip1enrollmentInfo3");
        tips3.add("tip2enrollmentInfo3");
        enrollmentInfo3.setTipsInfoModel(tips3);

        MoreInformationModel  moreinfo3= new MoreInformationModel("3312 14th Street DC<br/> Suite 210</br> Washington D.C 20010</br><b>Phone :</b><br/>(202) 328-2660</br><b>Fax :</b>(202) 442-5026</br>");
        enrollmentInfo3.setMoreInfo(moreinfo3);



        enrollmentInfo.add(enrollmentInfo1);
        enrollmentInfo.add(enrollmentInfo2);
        enrollmentInfo.add(enrollmentInfo3);

        return   enrollmentInfo;

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
