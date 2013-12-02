package gs.web.geo;

import gs.data.hubs.HubConfig;
import gs.data.state.State;
import gs.data.url.DirectoryStructureUrlFactory;
import gs.web.hub.EduCommunityModel;
import gs.web.path.DirectoryStructureUrlFields;
import gs.web.path.IDirectoryStructureUrlController;
import gs.web.util.PageHelper;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.list.Anchor;
import org.apache.commons.lang.WordUtils;
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
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 9/25/13
 * Time: 10:40 AM
 * To change this template use File | Settings | File Templates.
 */
@Controller
public class CityHubEducationCommunityController  implements IDirectoryStructureUrlController {
    public static final String EDUCATION_COMMUNITY_VIEW = "/cityHub/education-community";

    @Autowired
    private CityHubHelper _cityHubHelper;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView(EDUCATION_COMMUNITY_VIEW);
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
        modelAndView.addObject("city", WordUtils.capitalizeFully(city));
        modelAndView.addObject("state", state);

        Integer collectionId = getCityHubHelper().getCollectionId(city, state);
        modelAndView.addObject("collectionId", collectionId);

        List<HubConfig> configList = getCityHubHelper().getConfigListFromCollectionId(collectionId);
        modelAndView.addObject(CityHubHelper.IMPORTANT_EVENT_KEY_PREFIX, getCityHubHelper().getImportantModuleMap(configList));

        ModelMap educationCommunityModelMap = getCityHubHelper().getFilteredConfigMap(configList, CityHubHelper.EDUCATIONCOMMUNITY_PAGE_KEY_PREFIX);

        modelAndView.addObject(CityHubHelper.EDUCATIONCOMMUNITY_PAGE_SUBHEADING_MODEL_KEY,
                educationCommunityModelMap.get(CityHubHelper.EDUCATIONCOMMUNITY_PAGE_KEY_PREFIX + "_" + CityHubHelper.EDUCATIONCOMMUNITY_PAGE_SUBHEADING_MODEL_KEY));


        ArrayList<EduCommunityModel> partnersInfo = getPartnerFacades(collectionId);
        modelAndView.addObject("partnersInfo", partnersInfo);

        modelAndView.addObject(CityHubHelper.COLLECTION_NICKNAME_MODEL_KEY,
                getCityHubHelper().getCollectionNicknameFromConfigList(configList, collectionId));

        return modelAndView;
    }

    public CityHubHelper getCityHubHelper() {
        return _cityHubHelper;
    }

    public void setCityHubHelper(final CityHubHelper _cityHubHelper) {
        this._cityHubHelper = _cityHubHelper;
    }

    private ArrayList<EduCommunityModel> getPartnerFacades(final Integer collectionId){

        final String logoNameBase = "_EduPage_logo_partner_";
        final String baseLogoLocation = "/res/img/cityHubs/";

        ArrayList<EduCommunityModel> partnersInfo = new ArrayList<EduCommunityModel>();




        StringBuffer logoLocationPartner3 = new StringBuffer(baseLogoLocation).append(collectionId).append(logoNameBase).append("03").append(".png");
        final String descriptionPartner3 =  "PCSB is an independent seven-member board with the sole authority to open new charter schools in the\n" +
                                            " District of Columbia. PCSB currently oversees 60 public charter schools on 108 campuses serving nearly\n" +
                                            " 35,000 students living in all eight Wards of the city. PCSB evaluates DC public charter schools for\n" +
                                            " academic results, compliance with applicable local and federal laws and financial health, holding them\n" +
                                            " accountable for results. PCSB can close charter schools for financial reasons and also for failing to meet\n" +
                                            " the goals established in the charter agreement between PCSB and the school.";

        ArrayList<Anchor> linksPartner3 = new ArrayList<Anchor>();
        Anchor link1Partner3 = new Anchor("http://www.dcpcsb.org/", "Learn more about state report cards");
        linksPartner3.add(link1Partner3);


        EduCommunityModel partnerInfo3 = new EduCommunityModel("District of Columbia Public Charter School Board (PCSB)", descriptionPartner3, logoLocationPartner3.toString(), linksPartner3);


        partnersInfo.add(partnerInfo3);

        StringBuffer logoLocationPartner2 = new StringBuffer(baseLogoLocation).append(collectionId).append(logoNameBase).append("02").append(".png");
        final String descriptionPartner2 = "DCPS is the traditional public school system in the district. It operates schools serving students at all grade levels.";

        ArrayList<Anchor> linksPartner2 = new ArrayList<Anchor>();
        Anchor link1Partner2 = new Anchor("http://dcps.dc.gov/portal/site/DCPS/", "Visit website");
        Anchor link2Partner2 = new Anchor("http://www.dc.gov/DCPS/In+the+Classroom/Special+Education", "DCPS - Special Education");
        linksPartner2.add(link1Partner2);
        linksPartner2.add(link2Partner2);


        EduCommunityModel partnerInfo2 = new EduCommunityModel("District of Columbia Public Schools (DCPS)", descriptionPartner2, logoLocationPartner2.toString(), linksPartner2);

        partnersInfo.add(partnerInfo2);

        StringBuffer logoLocationPartner1 = new StringBuffer(baseLogoLocation).append(collectionId).append(logoNameBase).append("01").append(".png");
        final String descriptionPartner1 = "As the State Education Agency (SEA) for DC, the OSSE sets statewide policies, provides resources and support,\n" +
                "and exercises accountability for ALL public education in DC, by monitoring the local education agencies (LEA).\n" +
                " In addition, since many public charter schools are established as an LEA, OSSE often monitors public charter schools\n" +
                "directly. As part of the support they provide, the OSSE runs all transportation for special education students.";

        ArrayList<Anchor> linksPartner1 = new ArrayList<Anchor>();
        Anchor link1Partner1 = new Anchor("http://www.learndc.org/schoolprofiles/search", "Learn more about state report cards");
        Anchor link2Partner1 = new Anchor("http://osse.dc.gov/service/specialized-education", "OSSE - Special Education");
        linksPartner1.add(link1Partner1);
        linksPartner1.add(link2Partner1);


        EduCommunityModel partnerInfo1 = new EduCommunityModel("Office of the State Superintendent of Education (OSSE)", descriptionPartner1, logoLocationPartner1.toString(), linksPartner1);

        partnersInfo.add(partnerInfo1);


        return partnersInfo;

    }

    public boolean shouldHandleRequest(final DirectoryStructureUrlFields fields) {
        return fields == null ? false : fields.hasState() && fields.hasCityName() && fields.hasEducationCommunityPage()  && !fields.hasDistrictName() && !fields.hasLevelCode() && !fields.hasSchoolName();

    }
}
