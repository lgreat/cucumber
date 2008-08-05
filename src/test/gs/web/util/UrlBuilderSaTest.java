/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: UrlBuilderSaTest.java,v 1.88 2008/08/05 18:13:38 chriskimm Exp $
 */

package gs.web.util;

import gs.data.content.Article;
import gs.data.geo.City;
import gs.data.geo.ICity;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.state.State;
import gs.data.util.Address;
import gs.web.GsMockHttpServletRequest;
import junit.framework.TestCase;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests UrlBuilder.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class UrlBuilderSaTest extends TestCase {

    private GsMockHttpServletRequest getMockRequest() {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setMethod("GET");
        request.setProtocol("http");
        request.setServerName("www.myserver.com");
        request.setServerPort(80);
        request.setRequestURI("/index.page");
        return request;
    }

    /**
     * Written by Anthony to test a specific case. I modified my webapp to run from the root context
     * instead of from gs-web, but as a result UrlBuilder was building local absolute urls like
     * "/index.page" as "//index.page". This is because it prepends the context path. It worked before
     * because the context path was "/gs-web" and had no trailing slash. But once it became root,
     * it ended up breaking all local links. So here I test that my fix works and UrlBuilder creates
     * the appropriate link even for a context path of root.
     *
     * Note that I don't test that /gs-web still works since that is already tested in
     * "testUrlBuilderContext"
     */
    public void testContextPath() {
        GsMockHttpServletRequest request = getMockRequest();
        request.setContextPath("/");
        UrlBuilder builder = new UrlBuilder(request, "/index.page");
        assertEquals("/index.page", builder.asSiteRelativeXml(null));
        assertEquals("http://www.myserver.com/index.page", builder.asFullUrl(request));
        assertEquals("/index.page", builder.asSiteRelativeXml(request));
    }

    public void testArticleLinkBuilder() {
        Article article = new Article();
        article.setId(new Integer(5));
        article.setActive(true);
        UrlBuilder builder12 = new UrlBuilder(article, State.CA, true);
        assertEquals("/cgi-bin/showarticlefeature/5", builder12.toString());
        UrlBuilder builder11 = new UrlBuilder(article, State.CA, false);
        assertEquals("/cgi-bin/showarticle/5", builder11.toString());
        UrlBuilder builder10 = new UrlBuilder(article, State.WY, true);
        assertEquals("/cgi-bin/showarticlefeature/5", builder10.toString());
        UrlBuilder builder9 = new UrlBuilder(article, State.WY, false);
        assertEquals("/cgi-bin/showarticle/5", builder9.toString());
    }

    /**
     * Tests the Article Link Builder
     *
     * This test that the correct urls are being built.  The Urls are being
     * transitioned from the old style
     *
     *        /cgi-bin/showarticle/ca/700
     * 
     * to the new style.
     *
     *        /cgi-bin/showarticle/700
     *
     * The transition is being done incrementally, so each iteration, the lower
     * boundry is decreased.
     *
     * GS-4929 lb=700, GS-5037 lb=1, GS-5142 Transition complete
     */
    public void testArticleLinkBuilder_NewURL() {
        Article article = new Article();

        Integer lowerLimitId = 1;
        //Integer underId = lowerLimitId - 1;
        UrlBuilder builder;

        /**
         * Test Obsolete
        // GS-4929
        article.setId(underId);
        article.setActive(true);
        UrlBuilder builder = new UrlBuilder(article, State.CA, false);
        assertEquals("/cgi-bin/showarticle/ca/" + underId.toString(), builder.toString());
         */

        // Lower Boundry
        article.setId(lowerLimitId);
        builder = new UrlBuilder(article, State.CA, false);
        assertEquals("/cgi-bin/showarticle/" + lowerLimitId.toString(), builder.toString());

        /**
         * Test Obsolete
        // GS-4929
        article.setId(699);
        UrlBuilder builder = new UrlBuilder(article, State.CA, false);
        assertEquals("/cgi-bin/showarticle/ca/699", builder.toString());
        */

        // Above the lower boundry
        article.setId(700);
        builder = new UrlBuilder(article, State.CA, false);
        assertEquals("/cgi-bin/showarticle/700", builder.toString());

        // Feature Article is true
        article.setId(1000);
        builder = new UrlBuilder(article, State.CA, true);
        assertEquals("/cgi-bin/showarticlefeature/1000", builder.toString());
    }

    public void testUrlBuilder() {
        GsMockHttpServletRequest request = getMockRequest();
        UrlBuilder builder = new UrlBuilder(request, "/index.page");
        assertEquals("/index.page", builder.asSiteRelativeXml(null));
        assertEquals("http://www.myserver.com/index.page", builder.asFullUrl(request));
    }

    public void testUrlBuilderContext() {
        GsMockHttpServletRequest request = getMockRequest();
        request.setContextPath("/gs-web");
        request.setRequestURI("/gs-web/index.page");
        UrlBuilder builder = new UrlBuilder(request, "/index.page");
        assertEquals("/index.page", builder.asSiteRelativeXml(null));
        assertEquals("/gs-web/index.page", builder.asSiteRelativeXml(request));
        assertEquals("http://www.myserver.com/gs-web/index.page", builder.asFullUrl(request));

        builder = new UrlBuilder(request, null); // suck page path automatically from the request
        assertEquals("/index.page", builder.asSiteRelativeXml(null));
        assertEquals("/gs-web/index.page", builder.asSiteRelativeXml(request));
        assertEquals("http://www.myserver.com/gs-web/index.page", builder.asFullUrl(request));
    }

    public void testUrlBuilderParams() {
        GsMockHttpServletRequest request = getMockRequest();
        request.setParameter("a", "1");
        request.setParameter("b", "2");
        UrlBuilder builder = new UrlBuilder(request, "/index.page");
        builder.addParametersFromRequest(request);
        assertEquals("/index.page?a=1&amp;b=2", builder.asSiteRelativeXml(null));
        assertEquals("http://www.myserver.com/index.page?a=1&b=2", builder.asFullUrl(request));

        // Encoding
        builder = new UrlBuilder(request, "/index.page");
        builder.setParameter("city", "Batin Rooj");
        assertEquals("/index.page?city=Batin+Rooj", builder.asSiteRelativeXml(request));

        builder.setParameter("city", "Crow's Neck");
        assertEquals("/index.page?city=Crow%27s+Neck", builder.asSiteRelativeXml(request));
    }

    public void testUrlBuilder8080() {
        GsMockHttpServletRequest request = getMockRequest();
        request.setServerPort(8080);
        UrlBuilder builder = new UrlBuilder(request, "/index.page");
        assertEquals("/index.page", builder.asSiteRelativeXml(null));
        assertEquals("http://www.myserver.com:8080/index.page", builder.asFullUrl(request));
    }

    public void testSchoolBuilder() {
        School school = new School();
        school.setDatabaseState(State.WY);
        school.setId(new Integer(8));
        Address address = new Address("123 way", "CityName", State.WY, "12345");
        school.setLevelCode(LevelCode.ELEMENTARY);
        school.setPhysicalAddress(address);

        UrlBuilder builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PARENT_REVIEWS);
        assertEquals("/school/parentReviews.page?id=8&amp;state=WY", builder.asSiteRelativeXml(null));

        try {
            builder = new UrlBuilder(school, null);
            fail("Shouldn't allow null VPage");
        } catch (IllegalArgumentException e) {
            // OK
        }


        school.setType(SchoolType.PUBLIC);
        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
        assertEquals("/modperl/browse_school/wy/8", builder.asSiteRelativeXml(null));

        school.setType(SchoolType.CHARTER);
        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
        assertEquals("/modperl/browse_school/wy/8", builder.asSiteRelativeXml(null));

        school.setType(SchoolType.PRIVATE);
        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
        assertEquals("/cgi-bin/wy/private/8", builder.asSiteRelativeXml(null));

        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_CENSUS);
        assertEquals("/cgi-bin/wy/other/8", builder.asSiteRelativeXml(null));

        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_CENSUS_PRIVATE);
        assertEquals("/cgi-bin/wy/otherprivate/8", builder.asSiteRelativeXml(null));

        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_PRIVATE_QUICK_FACTS);
        assertEquals("/modperl/quickprivate/wy/8", builder.asSiteRelativeXml(null));

        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_PRINCIPAL_VIEW);
        assertEquals("/cgi-bin/wy/pqview/8", builder.asSiteRelativeXml(null));

        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_TEST_SCORE);
        assertEquals("/modperl/achievement/wy/8", builder.asSiteRelativeXml(null));

        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_ADD_PARENT_REVIEW);
        assertEquals("/school/addComments.page?id=8&amp;state=WY", builder.asSiteRelativeXml(null));

        builder = new UrlBuilder(school, UrlBuilder.COMPARE_SCHOOL);
        assertEquals("/cgi-bin/cs_compare/wy/",
                builder.asSiteRelativeXml(null));

        school.setDatabaseState(State.CA);
        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_RATINGS);
        assertEquals("/school/rating.page?id=8&amp;state=CA", builder.asSiteRelativeXml(null));

        school.setDatabaseState(State.NY);
        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_RATINGS);
        assertEquals("/school/rating.page?id=8&amp;state=NY", builder.asSiteRelativeXml(null));

        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_AUTHORIZER);
        assertEquals("/school/authorizers.page?school=8&amp;state=NY", builder.asSiteRelativeXml(null));

        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_MAP);
        assertEquals("/school/mapSchool.page?id=8&amp;state=NY", builder.asSiteRelativeXml(null));

        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_ESP_LOGIN);
        assertEquals("/cgi-bin/pq_start.cgi/ny/8", builder.asSiteRelativeXml(null));

        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_TAKE_SURVEY);
        assertEquals("/survey/form.page?id=8&amp;state=NY", builder.asSiteRelativeXml(null));

        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_START_SURVEY);
        assertEquals("/survey/start.page?id=8&amp;state=NY", builder.asSiteRelativeXml(null));
    }

    public void testSurveyResultsBuilder() throws Exception {

        School school = new School();
        school.setDatabaseState(State.CA);
        school.setId(1);
        school.setLevelCode(LevelCode.HIGH);

        UrlBuilder builder = new UrlBuilder(school, UrlBuilder.START_SURVEY_RESULTS);
        assertEquals("/survey/startResults.page?id=1&amp;state=CA", builder.asSiteRelativeXml(null));

        builder = new UrlBuilder(school, UrlBuilder.SURVEY_RESULTS);
        assertEquals("/survey/results.page?id=1&amp;state=CA", builder.asSiteRelativeXml(null));
    }

    public void testCityPageBuilder() {
        ICity city = new City("Talahasi", State.FL);

        UrlBuilder builder = new UrlBuilder(city, UrlBuilder.CITY_PAGE);
        assertEquals("/city/Talahasi/FL", builder.asSiteRelativeXml(null));

        builder = new UrlBuilder(UrlBuilder.CITY_PAGE, State.WY,  "Laramee");
        assertEquals("/city/Laramee/WY", builder.asSiteRelativeXml(null));

        builder = new UrlBuilder(UrlBuilder.CITY_PAGE, State.WY,  "Lar a Me");
        assertEquals("/city/Lar_a_Me/WY", builder.asSiteRelativeXml(null));

        builder = new UrlBuilder(UrlBuilder.CITY_PAGE, State.WY,  "L'aramee");
        assertEquals("/city/L'aramee/WY", builder.asSiteRelativeXml(null));

        builder = new UrlBuilder(UrlBuilder.CITY_PAGE, State.WY,  "Lar-a-mee");
        assertEquals("/city/Lar-a-mee/WY", builder.asSiteRelativeXml(null));
    }

    public void testSwitchingCitiesEncoding() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.CITY_PAGE, State.WY, "Lar a Me");
        assertEquals("/city/Lar_a_Me/WY", builder.asSiteRelativeXml(null));
        builder.setParameter("city", "Lar a You");
        assertEquals("/city/Lar_a_You/WY", builder.asSiteRelativeXml(null));
        builder.setParameter("city",  "L'aramee");
        assertEquals("/city/L'aramee/WY", builder.asSiteRelativeXml(null));
        builder.setParameter("city",  "Lar-a-mee");
        assertEquals("/city/Lar-a-mee/WY", builder.asSiteRelativeXml(null));
    }


    public void testEncodeForXml() {
        assertEquals("X &amp; Y", UrlBuilder.encodeForXml("X & Y"));
        assertEquals("X &gt; Y", UrlBuilder.encodeForXml("X > Y"));
        assertEquals("X &lt; Y", UrlBuilder.encodeForXml("X < Y"));
    }


    public void testAddParameter() {
        GsMockHttpServletRequest request = getMockRequest();

        // Adding
        UrlBuilder builder = new UrlBuilder(request, "/index.page");
        builder.addParameter("city", "a");
        assertEquals("/index.page?city=a", builder.asSiteRelativeXml(request));
        builder.addParameter("city", "b");
        assertEquals("/index.page?city=a&amp;city=b", builder.asSiteRelativeXml(request));
        assertEquals("/index.page?city=a&city=b", builder.asSiteRelative(request));

        // Encoding
        builder = new UrlBuilder(request, "/index.page");
        builder.addParameter("city", "Batin Rooj");
        assertEquals("/index.page?city=Batin+Rooj", builder.asSiteRelativeXml(request));
        builder.addParameter("city", "Gobber");
        assertEquals("/index.page?city=Batin+Rooj&amp;city=Gobber", builder.asSiteRelativeXml(request));
        assertEquals("/index.page?city=Batin+Rooj&city=Gobber", builder.asSiteRelative(request));

        builder.addParameter("place", "Crow's Neck");
        assertEquals("/index.page?city=Batin+Rooj&amp;city=Gobber&amp;place=Crow%27s+Neck", builder.asSiteRelativeXml(request));

// Bulk adding
        request.setParameter("a", "1");
        request.setParameter("b", "2");
        request.setParameter("c", "bill gates");
        request.setParameter("d", "steve's shop");
        builder = new UrlBuilder(request, "/index.page");
        builder.addParametersFromRequest(request);
        assertEquals("/index.page?a=1&amp;b=2&amp;c=bill+gates&amp;d=steve%27s+shop", builder.asSiteRelativeXml(null));
        assertEquals("http://www.myserver.com/index.page?a=1&b=2&c=bill+gates&d=steve%27s+shop", builder.asFullUrl(request));

    }


    public void testVPages() throws UnsupportedEncodingException {
        GsMockHttpServletRequest request = getMockRequest();

        UrlBuilder builder = new UrlBuilder(UrlBuilder.ARTICLE_LIBRARY, State.WY);
        assertEquals("/content/allArticles.page", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST);
        assertEquals("/mySchoolList.page", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST, State.WY, "234");
        assertEquals("/mySchoolList.page?command=add&ids=234&state=WY", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST_LOGIN);
        assertEquals("/mySchoolListLogin.page", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.NEWSLETTER_CENTER, State.WY);
        assertEquals("/cgi-bin/newsletters/WY/", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.NEWSLETTER_CENTER, State.WY);
        assertEquals("/cgi-bin/newsletters/WY/", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.CITY_PAGE, State.WY, "Xyz");
        assertEquals("/city/Xyz/WY", builder.asSiteRelativeXml(request));

        builder = new UrlBuilder(UrlBuilder.PRIVACY_POLICY, State.WY, "Xyz");
        assertEquals("/about/privacyStatement.page?state=WY", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.CONTACT_US, State.WY, "Xyz");
        assertEquals("/cgi-bin/feedback/WY", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.TERMS_OF_USE, State.WY, null);
        assertEquals("/cgi-bin/static/terms.html/WY", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.NEWSLETTER_MANAGEMENT, State.WY);
        final String email = "dlee@greatschools.net";
        String encodedEmail = URLEncoder.encode(email,"UTF-8");
        builder.addParameter("email",email);
        assertEquals("/cgi-bin/newsletterSubscribe?email="+encodedEmail +"&state=WY", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.BETA_SIGNUP, State.WY, null);
        assertEquals("/community/beta.page?state=WY", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.BETA_UNSUBSCRIBE, State.CA, null);
        assertEquals("/community/betaUnsubscribe.page?state=CA", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.RESEARCH, State.WY, null);
        assertEquals("/modperl/go/WY", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.RESEARCH, null, null);
        assertEquals("/school/research.page", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.ADD_PARENT_REVIEW_SEARCH, State.AZ, null);
        assertEquals("/cgi-bin/regSearch/AZ", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.SIGN_IN, State.AZ, null);
        assertEquals("/cgi-bin/msl_login/az", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.SIGN_OUT, State.AZ, null);
        assertEquals("/cgi-bin/logout/AZ", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.SIGN_OUT, State.AZ, "1001");
        assertEquals("/cgi-bin/logout/AZ?mid=1001", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.SIGN_OUT, State.AZ, "eford@gs.net");
        assertEquals("/cgi-bin/logout/AZ?email=eford%40gs.net", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, null);
        assertEquals("/community/registration.page", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, "email@address.org");
        assertEquals("/community/registration.page?email=email%40address.org", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, null, "redirect");
        assertEquals("/community/registration.page?redirect=redirect", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.REGISTRATION_VALIDATION, null, "myParam");
        assertEquals("/community/registrationConfirm.page?id=myParam", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.REGISTRATION_REMOVE, null, "myParam2");
        assertEquals("/community/registrationRemove.page?id=myParam2", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.FORGOT_PASSWORD, null, null);
        assertEquals("/community/forgotPassword.page", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.FORGOT_PASSWORD, null, "myEmail");
        assertEquals("/community/forgotPassword.page?email=myEmail", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.RESET_PASSWORD, null, "myParam");
        assertEquals("/community/resetPassword.page?id=myParam", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.REQUEST_EMAIL_VALIDATION, null, "myEmail");
        assertEquals("/community/requestEmailValidation.page?email=myEmail", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null, "myParam");
        assertEquals("/community/loginOrRegister.page?redirect=myParam", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.CHANGE_EMAIL, null, null);
        assertEquals("/community/changeEmail.page", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.COMMUNITY_LANDING, null, null);
        assertEquals("/community/communityLanding.page", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.ACCOUNT_INFO, null, null);
        assertEquals("/community/accountInfo.page", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.DISTRICT_PROFILE, State.CA, "135");
        assertEquals("/cgi-bin/ca/district_profile/135", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.WEBBY_AWARD_THANKS, null, null);
        assertEquals("/promo/webbyAwardWinner.page", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.PARENT_RATING_EXPLAINED, State.CA, null);
        assertEquals("/definitions/parent_rating_categories.html", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.PARENT_RATING_PRESCHOOL_EXPLAINED, State.CA, null);
        assertEquals("/definitions/preschool_rating_categories.html", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.PARENT_REVIEW_GUIDELINES, State.CA, null);
        assertEquals("/cgi-bin/static/guidelines.html/ca/", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.PARENT_REVIEW_LEARN_MORE, State.CA, null);
        assertEquals("/cgi-bin/static/parentcomments.html/ca/", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_STATE, State.CA);
        assertEquals("/schools/California/CA", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_STATE, State.NC);
        assertEquals("/schools/North_Carolina/NC", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.DISTRICTS_PAGE, State.CA);
        assertEquals("/schools/districts/California/CA", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.DISTRICTS_PAGE, State.NC);
        assertEquals("/schools/districts/North_Carolina/NC", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.CITIES, State.WY);
        assertEquals("/schools/cities/Wyoming/WY", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.CITIES, State.CA);
        assertEquals("/schools/cities/California/CA", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.CITIES, State.NC);
        assertEquals("/schools/cities/North_Carolina/NC", builder.asSiteRelative(request));
    }

    public void testMicroSitePages() {
        GsMockHttpServletRequest request = getMockRequest();
        UrlBuilder builder = new UrlBuilder(UrlBuilder.SCHOOL_CHOICE_CENTER);
        assertEquals("/content/schoolChoiceCenter.page", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.HEALTHY_KIDS);
        assertEquals("/content/healthyKids.page", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.SPECIAL_NEEDS);
        assertEquals("/content/specialNeeds.page", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.MEDIA_CHOICES);
        assertEquals("/content/mediaChoices.page", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.MOVING_WITH_KIDS);
        assertEquals("/content/movingWithKids.page", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.COUNTDOWN_TO_COLLEGE);
        assertEquals("/content/countdownToCollege.page", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.HOLIDAY_LEARNING);
        assertEquals("/content/holidayLearning.page", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.STATE_STANDARDS);
        assertEquals("/content/stateStandards.page", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.PRESCHOOL);
        assertEquals("/preschool/", builder.asSiteRelative(request));        
    }

    public void testAdminPages() {
        GsMockHttpServletRequest request = getMockRequest();

        UrlBuilder builder = new UrlBuilder(UrlBuilder.ADMIN_NEWS_ITEMS, null);
        assertEquals("/admin/news/list.page", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.ADMIN_NEWS_ITEMS_CREATE, null);
        assertEquals("/admin/news/create.page", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.ADMIN_NEWS_ITEMS_DELETE, null, new Integer(1).toString() );
        assertEquals("/admin/news/delete.page?id=1", builder.asSiteRelative(request));
    }

    public void testUrlBuilderBiReg() {
        GsMockHttpServletRequest request = getMockRequest();

        State state = State.CA;
        Integer schoolId = new Integer(15);
        Integer agentId = new Integer(321);
        UrlBuilder builder = new UrlBuilder(UrlBuilder.GET_BIREG, state, schoolId, agentId);
        assertEquals("/cgi-bin/getBIReg/CA/15/321", builder.asSiteRelative(request));
    }

    public void testGlossaryTermBuilder() {
        GsMockHttpServletRequest request = getMockRequest();
        State state = State.AZ;
        String glossaryId = "123";
        UrlBuilder builder = new UrlBuilder(UrlBuilder.GLOSSARY_TERM, state, glossaryId);
        assertEquals("Unexpected URL for glossary term", "/cgi-bin/glossary_single/AZ/?id=123", builder.asSiteRelative(request));
    }

    public void testCityBrowseUrlBuilder() {
        GsMockHttpServletRequest request = getMockRequest();
        Set<SchoolType> schoolTypes = new HashSet<SchoolType>();
        schoolTypes.add(SchoolType.PUBLIC);
        schoolTypes.add(SchoolType.CHARTER);
        UrlBuilder builder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, State.AK, "Anchorage", schoolTypes, LevelCode.ELEMENTARY);
        assertEquals("Unexpected URL", "/alaska/anchorage/public-charter/elementary-schools/", builder.asSiteRelative(request));        
    }
}
