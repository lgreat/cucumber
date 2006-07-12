/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: LinkTagHandlerTest.java,v 1.9 2006/07/12 21:37:49 dlee Exp $
 */

package gs.web.jsp.link;

import gs.data.content.Article;
import gs.data.geo.City;
import gs.data.school.LevelCode;
import gs.data.state.State;
import gs.web.BaseTestCase;
import gs.web.jsp.MockJspWriter;
import gs.web.jsp.MockPageContext;
import gs.web.util.UrlBuilder;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

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

        handler.doStartTag();
        handler.doAfterBody();
        handler.doEndTag();
        MockJspWriter out = (MockJspWriter) pc.getOut();
        assertEquals("<a target=\"theTarget\" href=\"/cgi-bin/msl_confirm/WY/\"></a>",
                out.getOutputBuffer().toString());
        handler.setTarget(null);

        //
        pc = new MockPageContext();
        handler.setPageContext(pc);
        handler.setStyleClass("theStyle");

        handler.doStartTag();
        handler.doAfterBody();
        handler.doEndTag();
        out = (MockJspWriter) pc.getOut();
        assertEquals("<a class=\"theStyle\" href=\"/cgi-bin/msl_confirm/WY/\"></a>",
                out.getOutputBuffer().toString());
    }

    public void testAnchorAttribute() throws JspException {
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
    }

    public void testArticleLibrary() {
        ArticleLibraryTagHandler tagHandler = new ArticleLibraryTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/content/allArticles.page?state=CA", builder.asSiteRelative(null));
    }

    public void testArticleTag() {
        ArticleTagHandler tagHandler = new ArticleTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        Article a = new Article();
        a.setId(new Integer(8));
        tagHandler.setArticle(a);
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/showarticle/ca/8", builder.asSiteRelative(null));

        tagHandler.setFeatured(Boolean.TRUE);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/showarticlefeature/ca/8", builder.asSiteRelative(null));
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
        assertEquals("/cgi-bin/msl_confirm/CA/", builder.asSiteRelative(null));
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

    public void testContactUs() {
        ContactUsTagHandler tagHandler = new ContactUsTagHandler();
        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/cgi-bin/feedback/CA", builder.asSiteRelative(null));
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

    public void testSchools() {
        SchoolsTagHandler tagHandler = new SchoolsTagHandler();
        tagHandler.setCity(new City("New York", State.NY));

        tagHandler.setPageContext(new MockPageContext());
        UrlBuilder builder = tagHandler.createUrlBuilder();
        assertEquals("/schools.page?city=New+York&state=NY", builder.asSiteRelative(null));

        tagHandler.setSchoolTypes("public");
        builder = tagHandler.createUrlBuilder();
        assertEquals("/schools.page?city=New+York&st=public&state=NY", builder.asSiteRelative(null));

        tagHandler.setLevelCode(LevelCode.HIGH);
        builder = tagHandler.createUrlBuilder();
        assertEquals("/schools.page?city=New+York&lc=h&st=public&state=NY", builder.asSiteRelative(null));

        tagHandler.setSchoolTypes("private");
        builder = tagHandler.createUrlBuilder();
        assertEquals("/schools.page?city=New+York&lc=h&st=private&state=NY", builder.asSiteRelative(null));

        tagHandler.setLevelCode(LevelCode.MIDDLE_HIGH);
        tagHandler.setSchoolTypes("public,charter");
        builder = tagHandler.createUrlBuilder();
        assertEquals("/schools.page?city=New+York&lc=m&lc=h&st=public&st=charter&state=NY", builder.asSiteRelative(null));


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
}
