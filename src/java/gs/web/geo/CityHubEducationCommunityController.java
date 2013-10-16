package gs.web.geo;

import gs.data.hubs.HubConfig;
import gs.data.state.State;
import gs.web.hub.EduCommunityModel;
import gs.web.util.list.Anchor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
@RequestMapping("/cityHub/educationCommunity.page")
public class CityHubEducationCommunityController {
    public static final String EDUCATION_COMMUNITY_VIEW = "/cityHub/educationCommunity";

    @Autowired
    private CityHubHelper _cityHubHelper;

    @RequestMapping(method = RequestMethod.GET)
    public String showPage(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        State state= State.MI;
        String city= "detroit";

        modelMap.put("city", city);
        modelMap.put("state", state);

        Integer collectionId = getCityHubHelper().getCollectionId(city, state);
        modelMap.put("collectionId", collectionId);

        List<HubConfig> configList = getCityHubHelper().getConfigListFromCollectionId(collectionId);
        modelMap.put(CityHubHelper.IMPORTANT_EVENT_KEY_PREFIX, getCityHubHelper().getImportantModuleMap(configList));


        /**
         * Get Step Info  .
         */
        ArrayList<EduCommunityModel> partnersInfo = getPartnerFacades(collectionId);
        modelMap.put("partnersInfo", partnersInfo);

        return EDUCATION_COMMUNITY_VIEW;
    }

    public CityHubHelper getCityHubHelper() {
        return _cityHubHelper;
    }

    public void setCityHubHelper(CityHubHelper _cityHubHelper) {
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
}
