/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: LinkTagHandlerTest.java,v 1.52 2008/12/18 17:36:54 aroy Exp $
 */

package gs.web.jsp.link;

import gs.data.content.Article;
import gs.data.school.Grade;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseTestCase;
import gs.web.jsp.MockJspWriter;
import gs.web.jsp.MockPageContext;
import gs.web.jsp.link.microsite.*;
import gs.web.util.UrlBuilder;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.io.IOException;

/**
 * Tests all the tags in the link package.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class LinkTagHandlerTest extends BaseTestCase {

    public void testGeneratesAttributes() throws JspException {

        LinkTagHandler handler = new LinkTagHandler() {
            protected UrlBuilder createUrlBuilder() {
                return new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST, State.WY);
            }

            protected String getDefaultLinkText() {
                return "MSL";
            }
        };
        MockPageContext pc = new MockPageContext();
        handler.setPageContext(pc);
        handler.setTarget("theTarget");
        handler.setRel("nofollow");
        handler.setOnclick("return false;");

        handler.doStartTag();
        handler.doAfterBody();
        handler.doEndTag();
        MockJspWriter out = (MockJspWriter) pc.getOut();
        assertEquals("<a rel=\"nofollow\" target=\"theTarget\" onclick=\"return false;\" href=\"/mySchoolList.page?command=add&amp;state=WY\"></a>",
                out.getOutputBuffer().toString());
        handler.setTarget(null);

        //
        pc = new MockPageContext();
        handler.setPageContext(pc);
        handler.setStyleClass("theStyle");
        handler.setOnclick(null);

        handler.doStartTag();
        handler.doAfterBody();
        handler.doEndTag();
        out = (MockJspWriter) pc.getOut();
        assertEquals("<a rel=\"nofollow\" class=\"theStyle\" href=\"/mySchoolList.page?command=add&amp;state=WY\"></a>",
                out.getOutputBuffer().toString());
    }

    public void testJavascriptAttributes() throws JspException {
        LinkTagHandler handler = new LinkTagHandler() {
            protected UrlBuilder createUrlBuilder() {
                return new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST, State.WY);
            }
            protected String getDefaultLinkText() {
                return "MSL";
            }
        };
        // test mouseover
        MockPageContext pc = new MockPageContext();
        handler.setPageContext(pc);
        handler.setOnMouseOver("mouseover();");

        handler.doStartTag();
        handler.doAfterBody();
        handler.doEndTag();
        MockJspWriter out = (MockJspWriter) pc.getOut();
        assertEquals("<a onmouseover=\"mouseover();\" href=\"/mySchoolList.page?command=add&amp;state=WY\"></a>",
                out.getOutputBuffer().toString());
        handler.setOnMouseOver(null); // clear

        // test mouseout
        pc = new MockPageContext();
        handler.setPageContext(pc);
        handler.setOnMouseOut("mouseout();");

        handler.doStartTag();
        handler.doAfterBody();
        handler.doEndTag();
        out = (MockJspWriter) pc.getOut();
        assertEquals("<a onmouseout=\"mouseout();\" href=\"/mySchoolList.page?command=add&amp;state=WY\"></a>",
                out.getOutputBuffer().toString());
        handler.setOnMouseOut(null); // clear

        // test all
        pc = new MockPageContext();
        handler.setPageContext(pc);
        handler.setOnMouseOut("mouseout();");
        handler.setOnMouseOver("mouseover();");

        handler.doStartTag();
        handler.doAfterBody();
        handler.doEndTag();
        out = (MockJspWriter) pc.getOut();
        assertEquals("<a onmouseover=\"mouseover();\" onmouseout=\"mouseout();\" href=\"/mySchoolList.page?command=add&amp;state=WY\"></a>",
                out.getOutputBuffer().toString());
    }

    public void testAnchorandIdAttribute() throws JspException {
        LinkTagHandler handler = new LinkTagHandler() {
            protected UrlBuilder createUrlBuilder() {
                return new UrlBuilder(new MockHttpServletRequest(), "somepage");
            }
        };

        PageContext pc = new MockPageContext();
        handler.setPageContext(pc);
        handler.setAnchor("#anchor");
        handler.doStartTag();
        handler.doAfterBody();
        handler.doEndTag();
        MockJspWriter out = (MockJspWriter) pc.getOut();
        assertEquals("<a href=\"somepage#anchor\"></a>", out.getOutputBuffer().toString());

        pc = new MockPageContext();
        handler.setPageContext(pc);
        handler.setAnchor("anchor");
        handler.doStartTag();
        handler.doAfterBody();
        handler.doEndTag();
        out = (MockJspWriter) pc.getOut();
        assertEquals("<a href=\"somepage#anchor\"></a>", out.getOutputBuffer().toString());


        handler = new LinkTagHandler() {
            protected UrlBuilder createUrlBuilder() {
                return new UrlBuilder(new MockHttpServletRequest(), "somepage#withanchor");
            }
        };
        pc = new MockPageContext();
        handler.setPageContext(pc);
        handler.setAnchor("anchorShouldHaveNoEffect");
        handler.doStartTag();
        handler.doAfterBody();
        handler.doEndTag();
        out = (MockJspWriter) pc.getOut();
        assertEquals("<a href=\"somepage#withanchor\"></a>", out.getOutputBuffer().toString());

        handler = new LinkTagHandler() {
            protected UrlBuilder createUrlBuilder() {
                return new UrlBuilder(new MockHttpServletRequest(), "somepage#withanchor");
            }
        };
        pc = new MockPageContext();
        handler.setPageContext(pc);
        handler.setAnchor("anchorShouldHaveNoEffect");
        handler.setId("myID");
        handler.doStartTag();
        handler.doAfterBody();
        handler.doEndTag();
        out = (MockJspWriter) pc.getOut();
        assertEquals("<a id=\"myID\" href=\"somepage#withanchor\"></a>", out.getOutputBuffer().toString());

    }

    public void testAbsoluteLink() throws JspException {
        LinkTagHandler handler = new LinkTagHandler() {
            protected UrlBuilder createUrlBuilder() {
                return new UrlBuilder(new MockHttpServletRequest(), "/somepage");
            }
        };

        PageContext pc = new MockPageContext();

        handler.setPageContext(pc);
        handler.setAbsolute(true);
        handler.doStartTag();
        handler.doAfterBody();
        handler.doEndTag();
        MockJspWriter out = (MockJspWriter) pc.getOut();
        assertTrue("Expect link (" + out.getOutputBuffer().toString() + ") to be absolute",
                out.getOutputBuffer().toString().startsWith("<a href=\"http://"));

        pc = new MockPageContext();

        handler.setPageContext(pc);
        handler.setAbsolute(false);
        handler.doStartTag();
        handler.doAfterBody();
        handler.doEndTag();
        out = (MockJspWriter) pc.getOut();
        assertTrue("Expect link (" + out.getOutputBuffer().toString() + ") to be relative",
                out.getOutputBuffer().toString().startsWith("<a href=\"/somepage"));
    }

    public void testArticleLibrary() {
        ArticleLibraryTagHandler tagHandler = new ArticleLibraryTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/education-topics/", builder.asSiteRelative(null));
    }

    public void testArticleTag() {
        ArticleTagHandler tagHandler = new ArticleTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        Article a = new Article();
        a.setId(new Integer(8));
        tagHandler.setArticle(a);
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/showarticle/8", builder.asSiteRelative(null));

        tagHandler.setFeatured(Boolean.TRUE);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/showarticlefeature/8", builder.asSiteRelative(null));

        tagHandler.setCampaignId("123456");
        builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/showarticlefeature/8?s_cid=123456", builder.asSiteRelative(null));
    }

    public void testCity() {
        CityTagHandler tagHandler = new CityTagHandler();
        tagHandler.setCity("Stockton");
        tagHandler.setState(State.AZ);
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/city/Stockton/AZ", builder.asSiteRelative(null));
    }

    public void testMySchoolList() {
        MySchoolListTagHandler tagHandler = new MySchoolListTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/mySchoolList.page", builder.asSiteRelative(null));

        School s = new School();
        s.setId(new Integer(234));
        s.setDatabaseState(State.AK);

        tagHandler.setSchool(s);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/mySchoolList.page?command=add&ids=234&state=AK", builder.asSiteRelative(null));
    }

    public void testNewsletterCenter() {
        NewsletterCenterTagHandler tagHandler = new NewsletterCenterTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/newsletters/CA/", builder.asSiteRelative(null));

        tagHandler.setEmail("whoever@whatever.how");
        builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/newsletters/CA/?email=whoever%40whatever.how", builder.asSiteRelative(null));
    }

    public void testNewsletterManagementandAnchors() {
        NewsletterManagementTagHandler tagHandler = new NewsletterManagementTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/newsletterSubscribe?state=CA", builder.asSiteRelative(null));

        tagHandler.setEmail("whoever@whatever.how");
        builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/newsletterSubscribe?email=whoever%40whatever.how&state=CA", builder.asSiteRelative(null));
    }

    public void testPrivacyPolicy() {
        PrivacyPolicyTagHandler tagHandler = new PrivacyPolicyTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/about/privacyStatement.page?state=CA", builder.asSiteRelative(null));
    }

    public void testPressRoom() {
        PressRoomTagHandler tagHandler = new PressRoomTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/about/pressRoom.page", builder.asSiteRelative(null));
    }

    public void testContactUs() {
        ContactUsTagHandler tagHandler = new ContactUsTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/feedback/CA", builder.asSiteRelative(null));
    }


    public void testFeedback(){
        FeedbackTagHandler tagHandler = new FeedbackTagHandler();
        assertNotNull("Expected an instance of FeedbackTagHandler to exist", tagHandler);
        tagHandler.setPageContext(new MockPageContext());

        String relativeUrl = "/cgi-bin/feedback_faq/CA?fbtype=gen"  ;
        UrlBuilder builder;

        // Variation 1: No topicOption defined
        builder = tagHandler.createUrlBuilder();
        assertEquals(relativeUrl,builder.asSiteRelative(null))    ;

        //Variation 2: topicOption is set
        tagHandler.setTopicOption("Parent_survey");
        builder = tagHandler.createUrlBuilder();
        assertEquals(relativeUrl + "&topicOption=Parent_survey",builder.asSiteRelative(null));

        //Variation 3: redirect is set
        tagHandler.setRedirect("http://www.greatschools.net/survey/results.page?id=1&state=ca&level=h&page=0");
        tagHandler.setTopicOption(null);
        builder = tagHandler.createUrlBuilder();
        assertEquals(relativeUrl + "&redirect=http%3A%2F%2Fwww.greatschools.net%2Fsurvey%2Fresults.page%3Fid%3D1%26state%3Dca%26level%3Dh%26page%3D0",builder.asSiteRelative(null));
    }

    public void testResearch() {
        ResearchTagHandler tagHandler = new ResearchTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        tagHandler.setState(State.AK);
        UrlBuilder builder = tagHandler.createUrlBuilder();
        UrlBuilder refBuilder = new UrlBuilder(UrlBuilder.RESEARCH, State.AK);
        assertEquals(refBuilder.asSiteRelative(null), builder.asSiteRelative(null));
    }

    public void testAddParentReviewSearch() {
        AddParentReviewSearchTagHandler tagHandler = new AddParentReviewSearchTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        UrlBuilder refBuilder = new UrlBuilder(UrlBuilder.ADD_PARENT_REVIEW_SEARCH, State.CA);
        assertEquals(refBuilder.asSiteRelative(null), builder.asSiteRelative(null));
    }

    public void testHome() {
        HomeTagHandler tagHandler = new HomeTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/", builder.asSiteRelative(null));
    }

    public void testSignIn() {
        SignInTagHandler tagHandler = new SignInTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/msl_login/ca", builder.asSiteRelative(null));
    }

    public void testSignOut() {
        SignOutTagHandler tagHandler = new SignOutTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/logout/CA", builder.asSiteRelative(null));
    }

    public void testDistricts() {
        DistrictsTagHandler tagHandler = new DistrictsTagHandler();
        tagHandler.setState(State.AK);
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/schools/districts/Alaska/AK", builder.asSiteRelative(null));
    }

    public void testSchools() {
        SchoolsTagHandler tagHandler = new SchoolsTagHandler();

        tagHandler.setCityName("Alameda");
        tagHandler.setLevelCode("e");
        tagHandler.setPage(2);
        tagHandler.setSchoolType("private");
        tagHandler.setShowAll(true);
        tagHandler.setPageContext(new MockPageContext());

        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/california/alameda/private/elementary-schools/?p=2&showall=1", builder.asSiteRelative(null));

        tagHandler.setSchoolType("public,charter");
        builder = tagHandler.createUrlBuilder();
        assertEquals("/california/alameda/public-charter/elementary-schools/?p=2&showall=1", builder.asSiteRelative(null));
    }

    public void testBetaTags() {
        BetaSignupTagHandler signupTag = new BetaSignupTagHandler();
        signupTag.setPageContext(new MockPageContext());
        String url = signupTag.createUrlBuilder().asSiteRelative(null);
        assertEquals("/community/beta.page?state=CA", url);

        BetaUnsubscribeTagHandler unsubscribeTag = new BetaUnsubscribeTagHandler();
        unsubscribeTag.setPageContext(new MockPageContext());
        url = unsubscribeTag.createUrlBuilder().asSiteRelative(null);
        assertEquals("/community/betaUnsubscribe.page?state=CA", url);

        BetaUnsubscribeTagHandler unsubscribeTagWithEmail = new BetaUnsubscribeTagHandler();
        unsubscribeTagWithEmail.setPageContext(new MockPageContext());
        unsubscribeTagWithEmail.setEmail("foo@bar.com");
        url = unsubscribeTagWithEmail.createUrlBuilder().asSiteRelative(null);
        assertEquals("/community/betaUnsubscribe.page?email=foo%40bar.com&state=CA", url);
    }

    public void testRegistration() {
        RegistrationTagHandler tagHandler = new RegistrationTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/community/registration.page", builder.asSiteRelative(null));

        tagHandler.setEmail("test@gs.net");
        builder = tagHandler.createUrlBuilder();
        assertEquals("/community/registration.page?email=test%40gs.net", builder.asSiteRelative(null));

        tagHandler.setRedirect("/somepage");
        builder = tagHandler.createUrlBuilder();
        assertEquals("/community/registration.page?email=test%40gs.net&redirect=%2Fsomepage", builder.asSiteRelative(null));

        tagHandler.setCpn("Some value");
        builder = tagHandler.createUrlBuilder();
        assertEquals("/community/registration.page?cpn=Some+value&email=test%40gs.net&redirect=%2Fsomepage", builder.asSiteRelative(null));
    }

    public void testLoginOrRegister() {
        LoginOrRegisterTagHandler tagHandler = new LoginOrRegisterTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/community/loginOrRegister.page", builder.asSiteRelative(null));

        tagHandler.setEmail("email@example.org");
        builder = tagHandler.createUrlBuilder();
        assertEquals("/community/loginOrRegister.page?email=email%40example.org", builder.asSiteRelative(null));

        tagHandler.setRedirect("/community/accountInfo.page");
        builder = tagHandler.createUrlBuilder();
        assertEquals("/community/loginOrRegister.page" +
                "?email=email%40example.org" +
                "&redirect=%2Fcommunity%2FaccountInfo.page", builder.asSiteRelative(null));
    }

    public void testForgotPassword() {
        ForgotPasswordTagHandler tagHandler = new ForgotPasswordTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/community/forgotPassword.page", builder.asSiteRelative(null));
    }

    public void testForgotPasswordWithEmail() {
        ForgotPasswordTagHandler tagHandler = new ForgotPasswordTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        tagHandler.setEmail("email@address.org");
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/community/forgotPassword.page?email=email%40address.org", builder.asSiteRelative(null));
    }

    public void testResetPassword() {
        ResetPasswordTagHandler tagHandler = new ResetPasswordTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/community/resetPassword.page", builder.asSiteRelative(null));
    }

    public void testAccountInfo() {
        AccountInfoTagHandler tagHandler = new AccountInfoTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/community/accountInfo.page", builder.asSiteRelative(null));
    }

    public void testRequestEmailValidation() {
        RequestEmailValidationTagHandler tagHandler = new RequestEmailValidationTagHandler();
        tagHandler.setEmail("testRequestEmailValidation@greatschools.net");
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/community/requestEmailValidation.page?email=testRequestEmailValidation%40greatschools.net",
                builder.asSiteRelative(null));
    }

    public void testTermsOfUse() {
        TermsOfUseTagHandler tagHandler = new TermsOfUseTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/static/terms.html/CA", builder.asSiteRelative(null));
    }

    public void testChangeEmail() {
        ChangeEmailTagHandler tagHandler = new ChangeEmailTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/community/changeEmail.page", builder.asSiteRelative(null));
    }

    public void testMicrositeTags() {
        setupAndTestMicrositeTag(new SchoolChoiceCenterTagHandler(), new UrlBuilder(UrlBuilder.SCHOOL_CHOICE_CENTER));
        setupAndTestMicrositeTag(new SpecialNeedsTagHandler(), new UrlBuilder(UrlBuilder.SPECIAL_NEEDS));
        setupAndTestMicrositeTag(new MediaChoicesTagHandler(), new UrlBuilder(UrlBuilder.MEDIA_CHOICES));
        setupAndTestMicrositeTag(new MovingWithKidsTagHandler(), new UrlBuilder(UrlBuilder.MOVING_WITH_KIDS));
        setupAndTestMicrositeTag(new HealthyKidsTagHandler(), new UrlBuilder(UrlBuilder.HEALTHY_KIDS));
        setupAndTestMicrositeTag(new CountdownToCollegeTagHandler(), new UrlBuilder(UrlBuilder.COUNTDOWN_TO_COLLEGE));
        setupAndTestMicrositeTag(new StateStandardsTagHandler(), new UrlBuilder(UrlBuilder.STATE_STANDARDS));
    }

    private void setupAndTestMicrositeTag(LinkTagHandler tagHandler, UrlBuilder urlBuilder) {
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals(urlBuilder.asSiteRelative(null), builder.asSiteRelative(null));
    }

    public void testAllSchoolsInDistrict() {
        DistrictsAllSchoolsTagHandler tagHandler = new DistrictsAllSchoolsTagHandler();
        tagHandler.setDistrictId(new Integer(1));
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/schools.page?district=1&state=CA", builder.asSiteRelative(null));
    }

    public void testDistrictProfile() {
        DistrictProfileTagHandler tagHandler = new DistrictProfileTagHandler();
        tagHandler.setDistrictId(new Integer(123));
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/ca/district_profile/123", builder.asSiteRelative(null));
        tagHandler.setState(State.AK);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/ak/district_profile/123", builder.asSiteRelative(null));
    }

    public void testSchoolSearchTagHandler() throws IOException {
        SchoolSearchTagHandler handler = new SchoolSearchTagHandler();
        handler.setPageContext(new MockPageContext());
        handler.setQuery("alameda");
        handler.setSchoolType("public,charter");
        handler.setLevelCode("m");

        UrlBuilder builder = handler.createUrlBuilder();
        assertEquals("/search/search.page?c=school&lc=m&q=alameda&search_type=0&st=public&st=charter&state=CA",
                builder.asSiteRelative(null));
    }

    public void testArticleSearchTagHandler() throws IOException {
        ArticleSearchTagHandler handler = new ArticleSearchTagHandler();
        handler.setPageContext(new MockPageContext());
        handler.setQuery("bullying");

        UrlBuilder builder = handler.createUrlBuilder();
        assertEquals("/search/search.page?c=topic&q=bullying&search_type=0",
                builder.asSiteRelative(null));
    }

    public void testGradeSelector() throws IOException {
        PageContext pc = new MockPageContext();
        MockJspWriter out = (MockJspWriter) pc.getOut();
        GradeSelectorTagHandler tagHandler = new GradeSelectorTagHandler();
        tagHandler.setJspContext(pc);
        tagHandler.setName("name");
        tagHandler.setOnChange("onchange");
        tagHandler.setStyleClass("class");
        tagHandler.setStyleId("id");
        tagHandler.setUseNoGrade(true);
        tagHandler.setNoGradeLabel("--");

        tagHandler.doTag();

        assertNotNull(out.getOutputBuffer());
        String results = out.getOutputBuffer().toString();
        assertNotNull(results);
        assertTrue(results.indexOf("name=\"name\"") > -1);
        assertTrue(results.indexOf("onchange=\"onchange\"") > -1);
        assertTrue(results.indexOf("class=\"class\"") > -1);
        assertTrue(results.indexOf("id=\"id\"") > -1);
        assertTrue(results.indexOf("<option value=\"\">--</option>") > -1);
        assertEquals(-1, results.indexOf("selected"));
    }

    public void testGradeSelectorAlternate() throws IOException {
        PageContext pc = new MockPageContext();
        MockJspWriter out = (MockJspWriter) pc.getOut();
        GradeSelectorTagHandler tagHandler = new GradeSelectorTagHandler();
        tagHandler.setJspContext(pc);
        tagHandler.setUseAlternateNames(true);
        tagHandler.setGrade(Grade.G_10);

        tagHandler.doTag();

        assertNotNull(out.getOutputBuffer());
        String results = out.getOutputBuffer().toString();
        assertNotNull(results);
        assertTrue(results.indexOf("<option value=\"10\" selected=\"selected\">10</option>") > -1);
        assertTrue(results.indexOf("<option value=\"KG\">K</option>") > -1);
    }

    public void testParentRatingsExplainedTagHandler() throws IOException {
        ParentRatingsExplainedTagHandler handler = new ParentRatingsExplainedTagHandler();
        handler.setPageContext(new MockPageContext());

        UrlBuilder builder = handler.createUrlBuilder();
        assertEquals("/definitions/parent_rating_categories.html",
                builder.asSiteRelative(null));

        handler.setPreschool(true);
        builder = handler.createUrlBuilder();
        assertEquals("/definitions/preschool_rating_categories.html",
                builder.asSiteRelative(null));
    }
}
