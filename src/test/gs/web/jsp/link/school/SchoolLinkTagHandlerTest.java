package gs.web.jsp.link.school;

import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.state.State;
import gs.data.util.Address;
import gs.web.BaseTestCase;
import gs.web.jsp.MockPageContext;
import gs.web.util.UrlBuilder;

/**
 * Test school links
 *
 * @author <a href="mailto:dlee@greatschools.org">David Lee</a>
 */
public class SchoolLinkTagHandlerTest extends BaseTestCase {

    public void testSchoolProfileTags() {
        School school = new School();
        school.setName("Wowochocho High School");
        school.setDatabaseState(State.WY);
        school.setId(Integer.valueOf("8"));

        Address address = new Address("123 way", "CityName", State.WY, "12345");
        school.setLevelCode(LevelCode.ELEMENTARY);
        school.setPhysicalAddress(address);

        // Tests for private schools
        school.setType(SchoolType.PRIVATE);

        BaseSchoolTagHandler tagHandler = new CensusTagHandler();
        tagHandler.setSchool(school);
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/wy/otherprivate/8", builder.asSiteRelative(null));

        tagHandler = new PrivateQuickFactsTagHandler();
        tagHandler.setSchool(school);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/modperl/quickprivate/wy/8", builder.asSiteRelative(null));

        // Tests for public schools
        school.setType(SchoolType.PUBLIC);

        tagHandler = new CensusTagHandler();
        tagHandler.setSchool(school);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/wy/other/8", builder.asSiteRelative(null));


        tagHandler = new OverviewTagHandler();
        tagHandler.setSchool(school);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/wyoming/cityname/8-Wowochocho-High-School/", builder.asSiteRelative(null));

        tagHandler = new ParentReviewTagHandler();
        tagHandler.setSchool(school);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/school/parentReviews.page?id=8&state=WY", builder.asSiteRelative(null));

        tagHandler = new PrincipalViewTagHandler();
        tagHandler.setSchool(school);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/wy/pqview/8", builder.asSiteRelative(null));

        tagHandler = new TestScoreTagHandler();
        tagHandler.setSchool(school);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/modperl/achievement/wy/8", builder.asSiteRelative(null));

        tagHandler = new AddParentReviewTagHandler();
        tagHandler.setSchool(school);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/school/addComments.page?id=8&state=WY", builder.asSiteRelative(null));

        tagHandler = new RatingsTagHandler();
        school.setDatabaseState(State.CA);
        tagHandler.setSchool(school);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/school/rating.page?id=8&state=CA", builder.asSiteRelative(null));

        tagHandler = new RatingsTagHandler();
        school.setDatabaseState(State.NY);
        tagHandler.setSchool(school);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/school/rating.page?id=8&state=NY", builder.asSiteRelative(null));

        tagHandler = new CharterAuthorizerTagHandler();
        school.setDatabaseState(State.NY);
        tagHandler.setSchool(school);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/school/authorizers.page?school=8&state=NY", builder.asSiteRelative(null));

        tagHandler = new EspTagHandler();
        tagHandler.setSchool(school);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/ny/pqview/8", builder.asSiteRelative(null));
    }

    public void testCompareSchoolLinkTagHandler() {
        School school = new School();
        school.setDatabaseState(State.WY);
        school.setId(Integer.valueOf("8"));
        school.setType(SchoolType.PUBLIC);

        Address address = new Address("123 way", "CityName", State.WY, "12345");
        school.setLevelCode(LevelCode.ELEMENTARY);

        CompareSchoolTagHandler tagHandler = new CompareSchoolTagHandler();
        tagHandler.setPageContext(new MockPageContext());

        // test without an address
        tagHandler.setSchool(school);
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/cs_compare/wy/?area=m&miles=1000&school_selected=8&showall=1&sortby=distance&tab=over",
                builder.asSiteRelative(null));

        // test defaults with an address
        school.setPhysicalAddress(address);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/cs_compare/wy/?area=m&city=CityName&level=e&miles=1000&school_selected=8&showall=1&sortby=distance&street=123+way&tab=over&zip=12345",
                builder.asSiteRelative(null));

        // test setting tab and sortby
        tagHandler = new CompareSchoolTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        school.setLevelCode(LevelCode.MIDDLE_HIGH);
        tagHandler.setSchool(school);
        tagHandler.setSortBy("stars_newrating");

        builder = tagHandler.createUrlBuilder();
        _log.debug(builder.asSiteRelativeXml(null));
        assertEquals("/cgi-bin/cs_compare/wy/?area=m&amp;city=CityName&amp;level=m&amp;miles=1000&amp;school_selected=8&amp;showall=1&amp;sortby=stars_newrating&amp;street=123+way&amp;tab=over&amp;zip=12345",
                builder.asSiteRelativeXml(null));

    }
    
    public void testEspFormTagHandler() {
        EspFormTagHandler tagHandler = new EspFormTagHandler();
        
        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);
        tagHandler.setSchool(school);
        tagHandler.setPage(2);

        UrlBuilder urlBuilder = tagHandler.createUrlBuilder();
        assertEquals("/school/esp/form.page?page=2&schoolId=1&state=ca", urlBuilder.asSiteRelative(null));
    }

    public void testEspDashboardTagHandler() {
        EspDashboardTagHandler tagHandler = new EspDashboardTagHandler();
        
        School school = new School();
        school.setId(2);
        school.setDatabaseState(State.CA);
        tagHandler.setSchool(school);

        UrlBuilder urlBuilder = tagHandler.createUrlBuilder();
        assertEquals("/official-school-profile/dashboard/?schoolId=2&state=ca", urlBuilder.asSiteRelative(null));
    }

    public void testEspSignInTagHandler() {
        EspSignInTagHandler tagHandler = new EspSignInTagHandler();

        UrlBuilder urlBuilder = tagHandler.createUrlBuilder();
        assertEquals("/official-school-profile/signin.page", urlBuilder.asSiteRelative(null));
    }

    public void testEspRegisterTagHandler() {
        EspRegisterTagHandler tagHandler = new EspRegisterTagHandler();

        UrlBuilder urlBuilder = tagHandler.createUrlBuilder();
        assertEquals("/official-school-profile/register.page", urlBuilder.asSiteRelative(null));
    }

    public void testPrincipalReviewTagHandler() {
        PrincipalReviewTagHandler tagHandler = new PrincipalReviewTagHandler();
        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);
        tagHandler.setSchool(school);

        UrlBuilder urlBuilder = tagHandler.createUrlBuilder();
        assertEquals("/school/principalComments.page?id=1&state=ca", urlBuilder.asSiteRelative(null));
    }
}
