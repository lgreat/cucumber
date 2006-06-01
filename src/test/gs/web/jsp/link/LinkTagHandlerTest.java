/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: LinkTagHandlerTest.java,v 1.3 2006/06/01 18:46:36 aroy Exp $
 */

package gs.web.jsp.link;

import gs.data.state.State;
import gs.data.content.Article;
import gs.data.school.LevelCode;
import gs.data.geo.ICity;
import gs.data.geo.LatLon;
import gs.web.BaseTestCase;
import gs.web.jsp.MockJspWriter;
import gs.web.jsp.MockPageContext;
import gs.web.util.UrlBuilder;

import javax.servlet.jsp.JspException;

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
    public void testNewsletterManagement() {
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
    public void testSchools() {
        SchoolsTagHandler tagHandler = new SchoolsTagHandler();
        tagHandler.setCity(new ICity() {
            public String getName() {
                return "New York";
            }

            public State getState() {
                return State.NY;
            }

            public float getLat() {
                return 0;
            }

            public float getLon() {
                return 0;
            }

            public String getCountyFips() {
                return null;
            }

            public Long getPopulation() {
                return null;
            }

            public LatLon getLatLon() {
                return null;
            }
        });

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



    }
}
