/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: UrlBuilderSaTest.java,v 1.3 2006/03/23 18:21:38 apeterson Exp $
 */

package gs.web.util;

import junit.framework.TestCase;
import gs.data.content.Article;
import gs.data.state.State;
import gs.web.GsMockHttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides...
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
        UrlBuilder builder12 = new UrlBuilder(State.CA, article, true);
        assertEquals("/cgi-bin/showarticlefeature/ca/5", builder12.toString());
        UrlBuilder builder11 = new UrlBuilder(State.CA, article, false);
        assertEquals("/cgi-bin/showarticle/ca/5", builder11.toString());
        UrlBuilder builder10 = new UrlBuilder(State.WY, article, true);
        assertEquals("/cgi-bin/showarticlefeature/wy/5", builder10.toString());
        UrlBuilder builder9 = new UrlBuilder(State.WY, article, false);
        assertEquals("/cgi-bin/showarticle/wy/5", builder9.toString());

        article.setInsider(true);
        UrlBuilder builder8 = new UrlBuilder(State.CA, article, true);
        assertEquals("/cgi-bin/showpartarticle/ca/5", builder8.toString());
        UrlBuilder builder7 = new UrlBuilder(State.CA, article, false);
        assertEquals("/cgi-bin/showpartarticle/ca/5", builder7.toString());
        UrlBuilder builder6 = new UrlBuilder(State.WY, article, true);
        assertEquals("/cgi-bin/showarticlefeature/wy/5", builder6.toString());
        UrlBuilder builder5 = new UrlBuilder(State.WY, article, false);
        assertEquals("/cgi-bin/showarticle/wy/5", builder5.toString());
    }

    public void testUrlBuilder() {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setMethod("GET");
        request.setProtocol("http");
        request.setServerName("www.myserver.com");
        request.setServerPort(80);
        request.setRequestURI("/index.page");
        UrlBuilder builder = new UrlBuilder(request);
        assertEquals("/index.page", builder.asSiteRelativeUrl());
        assertEquals("http://www.myserver.com/index.page", builder.asFullUrl());
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
        UrlBuilder builder = new UrlBuilder(request);
        builder.addParametersFromRequest(request);
        assertEquals("/index.page?a=1&b=2", builder.asSiteRelativeUrl());
        assertEquals("http://www.myserver.com/index.page?a=1&b=2", builder.asFullUrl());
    }

    public void testUrlBuilder8080() {
        GsMockHttpServletRequest request = new GsMockHttpServletRequest();
        request.setMethod("GET");
        request.setProtocol("http");
        request.setServerName("www.myserver.com");
        request.setServerPort(8080);
        request.setRequestURI("/index.page");
        UrlBuilder builder = new UrlBuilder(request);
        assertEquals("/index.page", builder.asSiteRelativeUrl());
        assertEquals("http://www.myserver.com:8080/index.page", builder.asFullUrl());
    }


}
