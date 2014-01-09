package gs.web.geo;

import com.google.common.collect.ImmutableList;
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
    private static final String TAB1 = "Community";
    private static final String TAB2 = "Education";
    private static final String TAB3 = "Funders";

    private static final List<String> tabs = ImmutableList.of(TAB1, TAB2, TAB3);


    @Autowired
    private CityHubHelper _cityHubHelper;
    @Autowired
    private StateSpecificFooterHelper _stateSpecificFooterHelper;

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
        modelAndView.addObject("isHubUserSet", "y");
        modelAndView.addObject("city", WordUtils.capitalizeFully(city));
        modelAndView.addObject("state", state);

        Integer collectionId = getCityHubHelper().getCollectionId(city, state);
        modelAndView.addObject("collectionId", collectionId);
        _stateSpecificFooterHelper.displayPopularCitiesForState(state, modelAndView);

        List<HubConfig> configList = getCityHubHelper().getConfigListFromCollectionId(collectionId);
        modelAndView.addObject(CityHubHelper.IMPORTANT_EVENT_KEY_PREFIX, getCityHubHelper().getImportantModuleMap(configList));

        ModelMap educationCommunityModelMap = getCityHubHelper().getFilteredConfigMap(configList, CityHubHelper.EDUCATIONCOMMUNITY_PAGE_KEY_PREFIX);

        modelAndView.addObject(CityHubHelper.EDUCATIONCOMMUNITY_PAGE_SUBHEADING_MODEL_KEY,
                educationCommunityModelMap.get(CityHubHelper.EDUCATIONCOMMUNITY_PAGE_KEY_PREFIX + "_" + CityHubHelper.EDUCATIONCOMMUNITY_PAGE_SUBHEADING_MODEL_KEY));
        modelAndView.addObject(CityHubHelper.EDUCATIONCOMMUNITY_PAGE_TABDATA_MODEL_KEY,
                educationCommunityModelMap.get(CityHubHelper.EDUCATIONCOMMUNITY_PAGE_KEY_PREFIX + "_" + CityHubHelper.EDUCATIONCOMMUNITY_PAGE_TABDATA_MODEL_KEY));

        modelAndView.addObject(CityHubHelper.EDUCATIONCOMMUNITY_PAGE_SHOWTABS_MODEL_KEY,
                educationCommunityModelMap.get(CityHubHelper.EDUCATIONCOMMUNITY_PAGE_KEY_PREFIX + "_" + CityHubHelper.EDUCATIONCOMMUNITY_PAGE_SHOWTABS_MODEL_KEY));


        ArrayList<EduCommunityModel> partnersInfo = getPartnerFacades(collectionId);
        modelAndView.addObject("partnersInfo", partnersInfo);

        modelAndView.addObject(CityHubHelper.COLLECTION_NICKNAME_MODEL_KEY,
                getCityHubHelper().getCollectionNicknameFromConfigList(configList, collectionId));
        modelAndView.addObject("defaultTab", TAB1);
        modelAndView.addObject("tabs", tabs);


        response.setHeader("Cache-Control", "no-cache, max-age=0, must-revalidate, no-store");
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
        final String descriptionPartner3 =  "Founded by Cardinal Adam Maida, who asked that the community &#34;help build cornerstones for the city&#34;. " +
                                            "One cornerstone was to be a Christ-centered schooling alternative that would provide the children of Detroit with access to a &#34;high-quality&#34;\n" +
                                            "education. Cornerstone Independent Schools were named a top primary school by Excellent Schools Detroit in 2013.";

        ArrayList<Anchor> linksPartner3 = new ArrayList<Anchor>();
        Anchor link1Partner3 = new Anchor("http://www.cornerstonecharters.org", "Learn more about Cornerstone Charters");
        linksPartner3.add(link1Partner3);


        EduCommunityModel partnerInfo3 = new EduCommunityModel("Cornerstone Charters ", descriptionPartner3, logoLocationPartner3.toString(), linksPartner3);


        partnersInfo.add(partnerInfo3);

        StringBuffer logoLocationPartner2 = new StringBuffer(baseLogoLocation).append(collectionId).append(logoNameBase).append("02").append(".png");
        final String descriptionPartner2 = "Detroit Public Schools, the largest school system in Michigan, runs 97 schools in Detroit." +
                                           " The district is made up of 21 application schools, 13 authorized charters, nine schools that are a part of DPS&#8217; Office of " +
                                           "Innovation &#34;Rising&#34; schools program, and the remainder of schools are considered traditional open enrollment programs. Amongst the DPS&#8217; schools," +
                                           " 12 schools are listed in Excellent Schools Detroit&#8217;s Scorecard in the top 20 K-8 schools in Detroit. In partnership with the Detroit Parent Network," +
                                           " there are eight DPS Parent Resource Centers located within schools across the city that offer year round parent training, support groups, and play areas for children. ";

        ArrayList<Anchor> linksPartner2 = new ArrayList<Anchor>();
        Anchor link1Partner2 = new Anchor("http://www.detroitk12.org",
                                          "Learn more about Detroit Public Schools");
        linksPartner2.add(link1Partner2);



        EduCommunityModel partnerInfo2 = new EduCommunityModel("Detroit Public Schools", descriptionPartner2, logoLocationPartner2.toString(), linksPartner2);

        partnersInfo.add(partnerInfo2);

        StringBuffer logoLocationPartner1 = new StringBuffer(baseLogoLocation).append(collectionId).append(logoNameBase).append("01").append(".png");
        final String descriptionPartner1 =  "The Education Achievement Authority (EAA) is a statewide school district (currently with 15 schools in Detroit) created in " +
                                            "2011 by Michigan&#8217;s governor to address the most poorly performing public schools. In the EAA, the most challenged public schools in the state can be " +
                                            "transferred into this new district to turnaround educational outcomes for children. The Detroit Parent Network currently runs two Parent Empowerment Centers" +
                                            " in EAA schools to offer families courses in closing the achievement gap, job training and more. ";

        ArrayList<Anchor> linksPartner1 = new ArrayList<Anchor>();
        Anchor link1Partner1 = new Anchor("http://www.eaaschools.org", "Visit website");
        linksPartner1.add(link1Partner1);


        EduCommunityModel partnerInfo1 = new EduCommunityModel("Education Achievement Authority", descriptionPartner1, logoLocationPartner1.toString(), linksPartner1);

        partnersInfo.add(partnerInfo1);


        return partnersInfo;

    }

    public boolean shouldHandleRequest(final DirectoryStructureUrlFields fields) {
        return fields == null ? false : fields.hasState() && fields.hasCityName() && fields.hasEducationCommunityPage()  && !fields.hasDistrictName() && !fields.hasLevelCode() && !fields.hasSchoolName();

    }
}
