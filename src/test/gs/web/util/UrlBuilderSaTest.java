/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: UrlBuilderSaTest.java,v 1.140 2011/10/08 03:35:24 ssprouse Exp $
 */

package gs.web.util;

import gs.data.content.Article;
import gs.data.content.cms.ContentKey;
import gs.data.content.cms.Publication;
import gs.data.content.cms.CmsConstants;
import gs.data.geo.City;
import gs.data.geo.ICity;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.school.district.District;
import gs.data.state.State;
import gs.data.util.Address;
import gs.data.util.CmsUtil;
import gs.web.BaseTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.request.RequestInfo;
import gs.web.request.Subdomain;
import gs.web.widget.CustomizeSchoolSearchWidgetController;
import gs.web.widget.SchoolSearchWidgetController;
import junit.framework.TestCase;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

/**
 * Tests UrlBuilder.
 *
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
 */
public class UrlBuilderSaTest extends BaseTestCase {

    private GsMockHttpServletRequest getMockRequest() {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setMethod("GET");
        request.setProtocol("http");
        String serverName = "www.myserver.com";
        request.setServerName(serverName);
        request.setServerPort(80);
        request.setRequestURI("/index.page");
        RequestInfo requestInfo = new RequestInfo(request);
        request.setAttribute(RequestInfo.REQUEST_ATTRIBUTE_NAME, requestInfo);
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
        UrlBuilder builder12 = new UrlBuilder(article.getId(), true);
        assertEquals("/cgi-bin/showarticlefeature/5", builder12.toString());
        UrlBuilder builder11 = new UrlBuilder(article.getId(), false);
        assertEquals("/cgi-bin/showarticle/5", builder11.toString());
        UrlBuilder builder10 = new UrlBuilder(article.getId(), true);
        assertEquals("/cgi-bin/showarticlefeature/5", builder10.toString());
        UrlBuilder builder9 = new UrlBuilder(article.getId(), false);
        assertEquals("/cgi-bin/showarticle/5", builder9.toString());
    }

    public void testArticleTagReturnsNewPublicationUrlWhenCmsEnabled() {
        boolean cmsEnabled = CmsUtil.isCmsEnabled();
        CmsUtil.enableCms();

        UrlBuilder builder = new UrlBuilder(8, false) {
            @Override
            public Publication getPublication(Integer legacyId) {
                Publication pub = new Publication();
                pub.setLegacyId(Long.valueOf(legacyId));
                pub.setContentKey(new ContentKey("Article", 35L));
                pub.setFullUri("/Topic/Category/Title");
                return pub;
            }
        };

        assertEquals("/Topic/Category/35-Title.gs", builder.asSiteRelative(null));

        CmsUtil.setCmsEnabled(cmsEnabled);
    }

    public void testArticleTagReturnsLegacyUrlForFallbackBehavior() {
        boolean cmsEnabled = CmsUtil.isCmsEnabled();
        CmsUtil.enableCms();

        int legacyArticleId = CmsConstants.getArticlesServedByLegacyCms().iterator().next().intValue();

        UrlBuilder builder = new UrlBuilder(legacyArticleId, false);

        assertEquals("/cgi-bin/showarticle/" + legacyArticleId, builder.asSiteRelative(null));

        CmsUtil.setCmsEnabled(cmsEnabled);
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
        school.setName("Wowochocho High School");
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
        assertEquals("/wyoming/cityname/8-Wowochocho-High-School/", builder.asSiteRelativeXml(null));

        school.setType(SchoolType.CHARTER);
        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
        assertEquals("/wyoming/cityname/8-Wowochocho-High-School/", builder.asSiteRelativeXml(null));

        school.setType(SchoolType.PRIVATE);
        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
        assertEquals("/wyoming/cityname/8-Wowochocho-High-School/", builder.asSiteRelativeXml(null));

        School school2 = new School();
        school2.setDatabaseState(State.CA);
        school2.setCity("San Francisco");
        school2.setName("School/Name #123-45");
        school2.setId(123);
        school2.setLevelCode(LevelCode.PRESCHOOL);
        builder = new UrlBuilder(school2, UrlBuilder.SCHOOL_PROFILE);
        assertEquals("/california/san-francisco/preschools/School-Name-123-45/123/", builder.asSiteRelative(null));

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

        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE_ESP);
        assertEquals("/cgi-bin/ny/pqview/8", builder.asSiteRelativeXml(null));
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
        assertEquals("/florida/talahasi/", builder.asSiteRelativeXml(null));

        builder = new UrlBuilder(UrlBuilder.CITY_PAGE, State.WY,  "Laramee");
        assertEquals("/wyoming/laramee/", builder.asSiteRelativeXml(null));

        builder = new UrlBuilder(UrlBuilder.CITY_PAGE, State.WY,  "Lar a Me");
        assertEquals("/wyoming/lar-a-me/", builder.asSiteRelativeXml(null));

        builder = new UrlBuilder(UrlBuilder.CITY_PAGE, State.WY,  "L'aramee");
        assertEquals("/wyoming/l'aramee/", builder.asSiteRelativeXml(null));

        builder = new UrlBuilder(UrlBuilder.CITY_PAGE, State.WY,  "Lar-a-mee");
        assertEquals("/wyoming/lar_a_mee/", builder.asSiteRelativeXml(null));
    }

    public void xtestSwitchingCitiesEncoding() {
        // City switching no longer works after the new urls were implemented in GS-8801
        UrlBuilder builder = new UrlBuilder(UrlBuilder.CITY_PAGE, State.WY, "Lar a Me");
        assertEquals("/wyoming/lar-a-me/", builder.asSiteRelativeXml(null));
        builder.setParameter("city", "Lar a You");
        assertEquals("/wyoming/lar-a-you/", builder.asSiteRelativeXml(null));
        builder.setParameter("city",  "L'aramee");
        assertEquals("/wyoming/l'aramee/", builder.asSiteRelativeXml(null));
        builder.setParameter("city",  "Lar-a-mee");
        assertEquals("/wyoming/lar_a_mee/", builder.asSiteRelativeXml(null));
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
        assertEquals("/education-topics/", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST);
        assertEquals("/mySchoolList.page", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST, State.WY, "234");
        assertEquals("/mySchoolList.page?command=add&ids=234&state=WY", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST_LOGIN);
        assertEquals("/mySchoolListLogin.page", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.CITY_PAGE, State.WY, "Xyz");
        assertEquals("/wyoming/xyz/", builder.asSiteRelativeXml(request));

        builder = new UrlBuilder(UrlBuilder.PRIVACY_POLICY, State.WY, "Xyz");
        assertEquals("/privacy/?state=WY", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.CONTACT_US);
        assertEquals("/about/feedback.page", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.CONTACT_US, "incorrectSchoolDistrictInfo_incorrectSchool", "Carson", 2438);
        assertEquals("/about/feedback.page?city=Carson&feedbackType=incorrectSchoolDistrictInfo_incorrectSchool&schoolId=2438", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.TERMS_OF_USE, State.WY, null);
        assertEquals("/terms/?state=WY", builder.asSiteRelative(request));

        final String email = "dlee@greatschools.org";
        builder = new UrlBuilder(UrlBuilder.NEWSLETTER_MANAGEMENT, State.WY, email);
        String encodedEmail = URLEncoder.encode(email,"UTF-8");
        assertEquals("/email/management.page?email="+encodedEmail, builder.asSiteRelative(request));

        final String email2 = "dlee@greatschools.org";
        builder = new UrlBuilder(UrlBuilder.NEWSLETTER_UNSUBSCRIBE, State.WY, email2);
        String encodedEmail2 = URLEncoder.encode(email2,"UTF-8");
        assertEquals("/email/unsubscribe.page?email="+encodedEmail2, builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.BETA_SIGNUP, State.WY, null);
        assertEquals("/community/beta.page?state=WY", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.BETA_UNSUBSCRIBE, State.CA, null);
        assertEquals("/community/betaUnsubscribe.page?state=CA", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.RESEARCH, State.WY, null);
        assertEquals("/wyoming/", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.RESEARCH, null, (String)null);
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

        builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, (String)null);
        assertEquals("/community/registration.page", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, "email@address.org");
        assertEquals("/community/registration.page?email=email%40address.org", builder.asSiteRelative(request));
        //builder = new UrlBuilder(UrlBuilder.REGISTRATION, null, null, "redirect");
        //assertEquals("/community/registration.page?redirect=redirect", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.REGISTRATION_VALIDATION, null, "myParam");
        assertEquals("/community/registrationConfirm.page?id=myParam", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.REGISTRATION_REMOVE, null, "myParam2");
        assertEquals("/community/registrationRemove.page?id=myParam2", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.FORGOT_PASSWORD, null, (String)null);
        assertEquals("/community/forgotPassword.page", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.FORGOT_PASSWORD, null, "myEmail");
        assertEquals("/community/forgotPassword.page?email=myEmail", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.RESET_PASSWORD, null, "myParam");
        assertEquals("/community/resetPassword.page?id=myParam", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.REQUEST_EMAIL_VALIDATION, null, "myEmail");
        assertEquals("/community/requestEmailValidation.page?email=myEmail", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.LOGIN_OR_REGISTER, null, "myParam");
        assertEquals("/community/loginOrRegister.page?redirect=myParam", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.CHANGE_EMAIL, null, (String)null);
        assertEquals("/community/changeEmail.page", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.COMMUNITY_LANDING, null, (String)null);
        assertEquals("/community/", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.ACCOUNT_INFO, null, (String)null);
        assertEquals("/account/", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.DISTRICT_PROFILE, State.CA, "135");
        assertEquals("/cgi-bin/ca/district-profile/135", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.WEBBY_AWARD_THANKS, null, (String)null);
        assertEquals("/promo/webbyAwardWinner.page", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.PARENT_RATING_EXPLAINED, State.CA, null);
        assertEquals("/definitions/parent_rating_categories.html", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.PARENT_RATING_PRESCHOOL_EXPLAINED, State.CA, null);
        assertEquals("/definitions/preschool_rating_categories.html", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.PARENT_REVIEW_GUIDELINES, State.CA, null);
        assertEquals("/about/guidelines.page", builder.asSiteRelative(request));
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

        builder = new UrlBuilder(UrlBuilder.BROWSE_PRESCHOOLS, State.AK, "anaktuvik");
        assertEquals("/alaska/anaktuvik/preschools/", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.BROWSE_PRESCHOOLS, State.CA, "San Francisco");
        assertEquals("/california/san-francisco/preschools/", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.BROWSE_PRESCHOOLS, State.CA, "Bangle-Deshmir");
        assertEquals("/california/bangle_deshmir/preschools/", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.BROWSE_PRESCHOOLS, State.NH, "East Hampstead");
        assertEquals("/new-hampshire/east-hampstead/preschools/", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.SCHOOL_FINDER_CUSTOMIZATION);
        assertEquals(CustomizeSchoolSearchWidgetController.BEAN_ID, builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.SCHOOL_FINDER_WIDGET);
        assertEquals(SchoolSearchWidgetController.BEAN_ID, builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.FIND_A_SCHOOL);
        assertEquals("/find-schools/", builder.asSiteRelative(request));
    }

    public void testMicroSitePages() {
        GsMockHttpServletRequest request = getMockRequest();
        UrlBuilder builder = new UrlBuilder(UrlBuilder.SCHOOL_CHOICE_CENTER);
        assertEquals("/school-choice/", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.HEALTHY_KIDS);
        assertEquals("/healthy-kids.topic?content=2504", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.SPECIAL_NEEDS);
        assertEquals("/LD.topic?content=1541", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.MEDIA_CHOICES);
        assertEquals("/media-choices.topic?content=2439", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.MOVING_WITH_KIDS);
        assertEquals("/moving.topic?content=2220", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.COUNTDOWN_TO_COLLEGE);
        assertEquals("/college/", builder.asSiteRelative(request));

        // Commented out because unit test hits database
        /*
        CmsUtil.enableCms();
        builder = new UrlBuilder(new ContentKey("TopicCenter", CmsConstants.COLLEGE_TOPIC_CENTER_ID));
        assertEquals("/college/", builder.asSiteRelative(request));
        CmsUtil.disableCms();
        */

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

    public void testDirectoryStructureUrlBuilder() {
        GsMockHttpServletRequest request = getMockRequest();
        Set<SchoolType> schoolTypes = new HashSet<SchoolType>();
        schoolTypes.add(SchoolType.PUBLIC);
        schoolTypes.add(SchoolType.CHARTER);
        UrlBuilder builder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, State.AK, "Anchorage", schoolTypes, LevelCode.ELEMENTARY);
        assertEquals("Unexpected URL", "/alaska/anchorage/public-charter/elementary-schools/", builder.asSiteRelative(request));
    }

    public void testVpageEquality() {
        UrlBuilder.VPage vpage = new UrlBuilder.VPage("vpage:home");
        assertEquals(UrlBuilder.HOME, vpage);
    }

    public void testStringConstructor() {
        // home page
        UrlBuilder urlBuilder = new UrlBuilder("home");
        assertEquals("/", urlBuilder.asSiteRelative(getMockRequest()));

        urlBuilder = new UrlBuilder("home?a=b&c=d");
        assertEquals("/", urlBuilder.asSiteRelative(getMockRequest()));

        // research & compare
        // national
        urlBuilder = new UrlBuilder("research");
        assertEquals("/school/research.page", urlBuilder.asSiteRelative(getMockRequest()));

        // Maryland
        urlBuilder = new UrlBuilder("research?state=MD");
        assertEquals("/maryland/", urlBuilder.asSiteRelative(getMockRequest()));

        // Bogus
        urlBuilder = new UrlBuilder("research?state=Bogus");
        assertEquals("/school/research.page", urlBuilder.asSiteRelative(getMockRequest()));

        // district profile
        urlBuilder = new UrlBuilder("districtProfile?state=NC&id=566");
        assertEquals("/cgi-bin/nc/district-profile/566", urlBuilder.asSiteRelative(getMockRequest()));

        // city page
        urlBuilder = new UrlBuilder("city?state=CA&city=San+Francisco");
        assertEquals("/california/san-francisco/", urlBuilder.asSiteRelative(getMockRequest()));

        // school profile page
        urlBuilder = new UrlBuilder("schoolProfile?state=AK&id=20");
        assertEquals("/alaska/newtok/20-Ayaprun-School/", urlBuilder.asSiteRelative(getMockRequest()));

        // test score page
        urlBuilder = new UrlBuilder("testScoreLanding?state=CA&tid=2");
        assertEquals("/test/landing.page?state=CA&tid=2", urlBuilder.asSiteRelative(getMockRequest()));

        // microsites
        urlBuilder = new UrlBuilder("schoolChoiceCenter");
        assertEquals("/school-choice/", urlBuilder.asSiteRelative(getMockRequest()));

        try {
            new UrlBuilder("notAMicrosite");
        } catch (IllegalArgumentException iae) {
            // ok
        }

    }

    public void testDistrictHomePage() {
        GsMockHttpServletRequest request = getMockRequest();
        District district = new District();
        district.setId(1);
        district.setDatabaseState(State.CA);
        district.setName("A Test-District");
        Address address = new Address();
        address.setCity("A City");
        district.setPhysicalAddress(address);

        UrlBuilder builder = new UrlBuilder(district, UrlBuilder.DISTRICT_HOME);
        assertEquals("/california/a-city/A-Test_District/", builder.asSiteRelative(request));
    }

}
