/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: UrlBuilderSaTest.java,v 1.13 2006/04/12 19:47:12 apeterson Exp $
 */

package gs.web.util;

import gs.data.content.Article;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.state.State;
import gs.data.geo.ICity;
import gs.data.geo.LatLon;
import gs.web.GsMockHttpServletRequest;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
        article.setInsider(false);
        UrlBuilder builder12 = new UrlBuilder(article, State.CA, true);
        assertEquals("/cgi-bin/showarticlefeature/ca/5", builder12.toString());
        UrlBuilder builder11 = new UrlBuilder(article, State.CA, false);
        assertEquals("/cgi-bin/showarticle/ca/5", builder11.toString());
        UrlBuilder builder10 = new UrlBuilder(article, State.WY, true);
        assertEquals("/cgi-bin/showarticlefeature/wy/5", builder10.toString());
        UrlBuilder builder9 = new UrlBuilder(article, State.WY, false);
        assertEquals("/cgi-bin/showarticle/wy/5", builder9.toString());

        article.setInsider(true);
        UrlBuilder builder8 = new UrlBuilder(article, State.CA, true);
        assertEquals("/cgi-bin/showpartarticle/ca/5", builder8.toString());
        UrlBuilder builder7 = new UrlBuilder(article, State.CA, false);
        assertEquals("/cgi-bin/showpartarticle/ca/5", builder7.toString());
        UrlBuilder builder6 = new UrlBuilder(article, State.WY, true);
        assertEquals("/cgi-bin/showarticlefeature/wy/5", builder6.toString());
        UrlBuilder builder5 = new UrlBuilder(article, State.WY, false);
        assertEquals("/cgi-bin/showarticle/wy/5", builder5.toString());
    }

    public void testUrlBuilder() {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setMethod("GET");
        request.setProtocol("http");
        request.setServerName("www.myserver.com");
        request.setServerPort(80);
        request.setRequestURI("/index.page");
        UrlBuilder builder = new UrlBuilder(request, "/index.page");
        assertEquals("/index.page", builder.asSiteRelative(null));
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
        assertEquals("/index.page", builder.asSiteRelative(null));
        assertEquals("/gs-web/index.page", builder.asSiteRelative(request));
        assertEquals("http://www.myserver.com/gs-web/index.page", builder.asFullUrl(request));

        builder = new UrlBuilder(request, null); // suck page path automatically from the request
        assertEquals("/index.page", builder.asSiteRelative(null));
        assertEquals("/gs-web/index.page", builder.asSiteRelative(request));
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
        assertEquals("/index.page?a=1&amp;b=2", builder.asSiteRelative(null));
        assertEquals("http://www.myserver.com/index.page?a=1&b=2", builder.asFullUrl(request));

        // Encoding
        builder = new UrlBuilder(request, "/index.page");
        builder.setParameter("city", "Batin Rooj");
        assertEquals("/index.page?city=Batin+Rooj", builder.asSiteRelative(request));

        builder.setParameter("city", "Crow's Neck");
        assertEquals("/index.page?city=Crow%27s+Neck", builder.asSiteRelative(request));
    }

    public void testUrlBuilder8080() {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setMethod("GET");
        request.setProtocol("http");
        request.setServerName("www.myserver.com");
        request.setServerPort(8080);
        request.setRequestURI("/index.page");
        UrlBuilder builder = new UrlBuilder(request, "/index.page");
        assertEquals("/index.page", builder.asSiteRelative(null));
        assertEquals("http://www.myserver.com:8080/index.page", builder.asFullUrl(request));
    }

    public void testSchoolBuilder() {
        School school = new School();
        school.setDatabaseState(State.WY);
        school.setId(new Integer(8));
        UrlBuilder builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PARENT_REVIEWS);
        assertEquals("/modperl/parents/wy/8", builder.asSiteRelative(null));

        try {
            builder = new UrlBuilder(school, null);
            fail("Shouldn't allow null VPage");
        } catch (IllegalArgumentException e) {
            // OK
        }


        school.setType(SchoolType.PUBLIC);
        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
        assertEquals("/modperl/browse_school/wy/8", builder.asSiteRelative(null));

        school.setType(SchoolType.CHARTER);
        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
        assertEquals("/modperl/browse_school/wy/8", builder.asSiteRelative(null));

        school.setType(SchoolType.PRIVATE);
        builder = new UrlBuilder(school, UrlBuilder.SCHOOL_PROFILE);
        assertEquals("/cgi-bin/wy/private/8", builder.asSiteRelative(null));


    }

    public void testCityPageBuilder() {
        ICity city = new ICity() {
            public String getName() {
                return "Talahasi";
            }

            public State getState() {
                return State.FL;
            }

            public float getLat() {
                return 0;
            }

            public float getLon() {
                return 0;
            }

            public LatLon getLatLon() {
                return null;
            }

            public String getCountyFips() {
                return null;
            }

            public Long getPopulation() {
                return null;
            }
        };

        UrlBuilder builder = new UrlBuilder(city, UrlBuilder.CITY_PAGE);
        assertEquals("/city.page?city=Talahasi&amp;state=FL", builder.asSiteRelative(null));

        builder = new UrlBuilder(UrlBuilder.CITY_PAGE, State.WY,  "Laramee");
        assertEquals("/city.page?city=Laramee&amp;state=WY", builder.asSiteRelative(null));
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
        assertEquals("/index.page?city=a", builder.asSiteRelative(request));
        builder.addParameter("city", "b");
        assertEquals("/index.page?city=a&amp;city=b", builder.asSiteRelative(request));

        // Encoding
        builder = new UrlBuilder(request, "/index.page");
        builder.addParameter("city", "Batin Rooj");
        assertEquals("/index.page?city=Batin+Rooj", builder.asSiteRelative(request));
        builder.addParameter("city", "Gobber");
        assertEquals("/index.page?city=Batin+Rooj&amp;city=Gobber", builder.asSiteRelative(request));

        builder.addParameter("place", "Crow's Neck");
        assertEquals("/index.page?city=Batin+Rooj&amp;city=Gobber&amp;place=Crow%27s+Neck", builder.asSiteRelative(request));

// Bulk adding
        request.setParameter("a", "1");
        request.setParameter("b", "2");
        request.setParameter("c", "bill gates");
        request.setParameter("d", "steve's shop");
        builder = new UrlBuilder(request, "/index.page");
        builder.addParametersFromRequest(request);
        assertEquals("/index.page?a=1&amp;b=2&amp;c=bill+gates&amp;d=steve%27s+shop", builder.asSiteRelative(null));
        assertEquals("http://www.myserver.com/index.page?a=1&b=2&c=bill+gates&d=steve%27s+shop", builder.asFullUrl(request));

    }

}
