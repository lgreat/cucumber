/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: UrlBuilderSaTest.java,v 1.31 2006/06/21 00:32:53 apeterson Exp $
 */

package gs.web.util;

import gs.data.content.Article;
import gs.data.geo.City;
import gs.data.geo.ICity;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.state.State;
import gs.web.GsMockHttpServletRequest;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Tests UrlBuilder.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class UrlBuilderSaTest extends TestCase {
    private static final Log _log = LogFactory.getLog(UrlBuilderSaTest.class);

    public void testArticleLinkBuilder() {
        Article article = new Article();
        article.setId(new Integer(5));
        article.setActive(true);
        UrlBuilder builder12 = new UrlBuilder(article, State.CA, true);
        assertEquals("/cgi-bin/showarticlefeature/ca/5", builder12.toString());
        UrlBuilder builder11 = new UrlBuilder(article, State.CA, false);
        assertEquals("/cgi-bin/showarticle/ca/5", builder11.toString());
        UrlBuilder builder10 = new UrlBuilder(article, State.WY, true);
        assertEquals("/cgi-bin/showarticlefeature/wy/5", builder10.toString());
        UrlBuilder builder9 = new UrlBuilder(article, State.WY, false);
        assertEquals("/cgi-bin/showarticle/wy/5", builder9.toString());
    }

    public void testUrlBuilder() {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setMethod("GET");
        request.setProtocol("http");
        request.setServerName("www.myserver.com");
        request.setServerPort(80);
        request.setRequestURI("/index.page");
        UrlBuilder builder = new UrlBuilder(request, "/index.page");
        assertEquals("/index.page", builder.asSiteRelativeXml(null));
        assertEquals("http://www.myserver.com/index.page", builder.asFullUrl(request));
    }

    public void testUrlBuilderContext() {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setMethod("GET");
        request.setProtocol("http");
        request.setServerName("www.myserver.com");
        request.setServerPort(80);
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
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setMethod("GET");
        request.setProtocol("http");
        request.setServerName("www.myserver.com");
        request.setServerPort(80);
        request.setRequestURI("/index.page");
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
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setMethod("GET");
        request.setProtocol("http");
        request.setServerName("www.myserver.com");
        request.setServerPort(8080);
        request.setRequestURI("/index.page");
        UrlBuilder builder = new UrlBuilder(request, "/index.page");
        assertEquals("/index.page", builder.asSiteRelativeXml(null));
        assertEquals("http://www.myserver.com:8080/index.page", builder.asFullUrl(request));
    }

    public void testSchoolBuilder() {
        School school = new School();
        school.setDatabaseState(State.WY);
        school.setId(new Integer(8));
        UrlBuilder builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PARENT_REVIEWS);
        assertEquals("/modperl/parents/wy/8", builder.asSiteRelativeXml(null));

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
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setMethod("GET");
        request.setProtocol("http");
        request.setServerName("www.myserver.com");
        request.setServerPort(80);
        request.setRequestURI("/index.page");

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
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setMethod("GET");
        request.setProtocol("http");
        request.setServerName("www.myserver.com");
        request.setServerPort(80);
        request.setRequestURI("/index.page");

        UrlBuilder builder = new UrlBuilder(UrlBuilder.ARTICLE_LIBRARY, State.WY);
        assertEquals("/content/allArticles.page?state=WY", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.MY_SCHOOL_LIST, State.WY);
        assertEquals("/cgi-bin/msl_confirm/WY/", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.NEWSLETTER_CENTER, State.WY);
        assertEquals("/cgi-bin/newsletters/WY/", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.NEWSLETTER_CENTER, State.WY);
        assertEquals("/cgi-bin/newsletters/WY/", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.CITIES, State.WY);
        assertEquals("/modperl/cities/WY/", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.CITY_PAGE, State.WY, "Xyz");
        assertEquals("/city/Xyz/WY", builder.asSiteRelativeXml(request));

        builder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, State.WY, "Xyz", LevelCode.HIGH, "Type");
        assertEquals("/schools.page?city=Xyz&lc=h&st=Type&state=WY", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, State.WY, "Xyz", null, null);
        assertEquals("/schools.page?city=Xyz&state=WY", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, State.WY, "Xyz", null, "Type");
        assertEquals("/schools.page?city=Xyz&st=Type&state=WY", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, State.WY, "Xyz", LevelCode.HIGH, null);
        assertEquals("/schools.page?city=Xyz&lc=h&state=WY", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.SCHOOLS_IN_CITY, State.WY, "Xyz", LevelCode.MIDDLE_HIGH, "Type,Type2");
        assertEquals("/schools.page?city=Xyz&lc=m&lc=h&st=Type&st=Type2&state=WY", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.PRIVACY_POLICY, State.WY, "Xyz");
        assertEquals("/about/privacyStatement.page?state=WY", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.CONTACT_US, State.WY, "Xyz");
        assertEquals("/cgi-bin/feedback/WY", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.NEWSLETTER_MANAGEMENT, State.WY);
        final String email = "dlee@greatschools.net";
        String encodedEmail = URLEncoder.encode(email,"UTF-8");
        builder.addParameter("email",email);
        assertEquals("/cgi-bin/newsletterSubscribe?email="+encodedEmail +"&state=WY", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.BETA_SIGNUP, State.WY, null);
        assertEquals("/community/beta.page?state=WY", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.RESEARCH, State.WY, null);
        assertEquals("/modperl/go/WY", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.RESEARCH, null, null);
        assertEquals("/test/research.page", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.ADD_PARENT_REVIEW_SEARCH, State.AZ, null);
        assertEquals("/cgi-bin/regSearch/AZ", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.SIGN_IN, State.AZ, null);
        assertEquals("/cgi-bin/site/signin.cgi/AZ", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.SIGN_OUT, State.AZ, null);
        assertEquals("/cgi-bin/logout/AZ", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.SIGN_OUT, State.AZ, "1001");
        assertEquals("/cgi-bin/logout/AZ?mid=1001", builder.asSiteRelative(request));
        builder = new UrlBuilder(UrlBuilder.SIGN_OUT, State.AZ, "eford@gs.net");
        assertEquals("/cgi-bin/logout/AZ?email=eford%40gs.net", builder.asSiteRelative(request));
    }

    public void testAdminPages() {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setMethod("GET");
        request.setProtocol("http");
        request.setServerName("www.myserver.com");
        request.setServerPort(80);
        request.setRequestURI("/index.page");

        UrlBuilder builder = new UrlBuilder(UrlBuilder.ADMIN_NEWS_ITEMS, null);
        assertEquals("/admin/news/list.page", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.ADMIN_NEWS_ITEMS_CREATE, null);
        assertEquals("/admin/news/create.page", builder.asSiteRelative(request));

        builder = new UrlBuilder(UrlBuilder.ADMIN_NEWS_ITEMS_DELETE, null, new Integer(1).toString() );
        assertEquals("/admin/news/delete.page?id=1", builder.asSiteRelative(request));


    }
}
