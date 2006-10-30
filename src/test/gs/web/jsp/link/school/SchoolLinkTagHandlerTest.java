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
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
 */
public class SchoolLinkTagHandlerTest extends BaseTestCase {

    public void testSchoolProfileTags() {
        School school = new School();
        school.setDatabaseState(State.WY);
        school.setId(Integer.valueOf("8"));
        school.setType(SchoolType.PUBLIC);

        Address address = new Address("123 way", "CityName", State.WY, "12345");
        school.setLevelCode(LevelCode.ELEMENTARY);
        school.setPhysicalAddress(address);

        BaseSchoolTagHandler tagHandler = new CensusTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        tagHandler.setSchool(school);

        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/wy/other/8", builder.asSiteRelative(null));

        tagHandler = new OverviewTagHandler();
        tagHandler.setSchool(school);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/modperl/browse_school/wy/8", builder.asSiteRelative(null));

        tagHandler = new ParentReviewTagHandler();
        tagHandler.setSchool(school);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/modperl/parents/wy/8", builder.asSiteRelative(null));

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
        assertEquals("/cgi-bin/addcomments/wy/8", builder.asSiteRelative(null));

        tagHandler = new RatingsTagHandler();
        school.setDatabaseState(State.CA);
        tagHandler.setSchool(school);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/school/rating.page?id=8&state=CA", builder.asSiteRelative(null));

        tagHandler = new RatingsTagHandler();
        school.setDatabaseState(State.NY);
        tagHandler.setSchool(school);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/ny/rankings/8", builder.asSiteRelative(null));

        tagHandler = new CharterAuthorizerTagHandler();
        school.setDatabaseState(State.NY);
        tagHandler.setSchool(school);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/school/authorizers.page?school=8&state=NY", builder.asSiteRelative(null));

    }

    public void testCompareSchoolLinkTagHandler() {
        School school = new School();
        school.setDatabaseState(State.WY);
        school.setId(Integer.valueOf("8"));
        school.setType(SchoolType.PUBLIC);

        Address address = new Address("123 way", "CityName", State.WY, "12345");
        school.setLevelCode(LevelCode.ELEMENTARY);
        school.setPhysicalAddress(address);

        CompareSchoolTagHandler tagHandler = new CompareSchoolTagHandler();
        tagHandler.setPageContext(new MockPageContext());

        //test defaults
        tagHandler.setSchool(school);
        UrlBuilder builder = tagHandler.createUrlBuilder();
        _log.debug(builder.asSiteRelative(null));
        assertEquals("/cgi-bin/cs_compare/wy/?area=m&city=CityName&level=e&miles=1000&school_selected=8&showall=1&sortby=distance&street=123+way&tab=over&zip=12345",
                builder.asSiteRelative(null));



        //test setting tab and sortby
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
}
