/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: BlogFeedTagHandler.java,v 1.39 2011/08/23 23:46:40 droy Exp $
 */

package gs.web.content;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import gs.web.util.UrlUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.text.SimpleDateFormat;

import gs.web.jsp.Util;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
 */
public class BlogFeedTagHandler extends SimpleTagSupport {
    private static final Log _log = LogFactory.getLog(BlogFeedTagHandler.class);
    private static final String TYPE_BILLS_BLOG = "billsBlog";
    private static final String TYPE_GS_BLOG = "gsBlog";
    private static final String TYPE_SPLASH_BLOG = "splashBlog";
    private static final String TYPE_RESEARCH_BLOG = "researchBlog";
    private static final String TYPE_MOM_BLOG = "momOnFire";
    private static final String TYPE_ABOUT_BLOG = "aboutBlog";
    private String _defaultTitle;
    private String _atomUrl;
    private String _defaultUrl;
    private String _type;
    private boolean _showDate;
    private boolean _hideAuthorImages = false;
    private SimpleDateFormat _sdf = null;

    private static final String HTTP_PROXY_HOSTNAME_PROPERTY = "http.proxy.hostname";
    private static final String HTTP_PROXY_PORT_PROPERTY = "http.proxy.port";

    // GS-9667 get xml reader for url directly or via proxy if proxy system properties not set
    private static XmlReader getXmlReader(URL feedUrl) throws IOException {
        String hostname = System.getProperty(HTTP_PROXY_HOSTNAME_PROPERTY);
        String portStr = System.getProperty(HTTP_PROXY_PORT_PROPERTY);
        int port = (portStr != null ? Integer.parseInt(portStr) : -1);

        HttpURLConnection conn;

        // use proxy if hostname is specified and port is specified and positive number
        if (hostname != null && port > 0) {
            InetSocketAddress address = new InetSocketAddress(hostname, port);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
            conn = (HttpURLConnection)feedUrl.openConnection(proxy);
        // otherwise, don't use proxy
        } else {
            conn = (HttpURLConnection)feedUrl.openConnection();
        }

        // pretend to be a web browser
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; Greatschoolsbot/1.1; +http://www.greatschools.org/cgi-bin/feedback/CA)");

        return new XmlReader(conn);
    }

    public void doTag() throws JspException, IOException {
        super.doTag();
        String link = _defaultUrl;
        String text = "";
        String author = "";
        String title = _defaultTitle;
        Date date = null;
        String link2 = _defaultUrl;
        String text2 = "";
        String author2 = "";
        String title2 = _defaultTitle;
        Date date2 = null;
        URL feedUrl = new URL(_atomUrl);
        try {
            System.setProperty("sun.net.client.defaultConnectTimeout", "5000");
            System.setProperty("sun.net.client.defaultReadTimeout", "5000");
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(getXmlReader(feedUrl));
            SyndEntry entry = (SyndEntry) feed.getEntries().get(0);
            title = entry.getTitle();
            link = entry.getLink();
            text = entry.getDescription().getValue();
            author = entry.getAuthor();
            if (isShowDate()) {
                date = entry.getPublishedDate();
                if (date == null){
                    date = entry.getUpdatedDate();
                }
                if (date == null){
                    date = new Date(System.currentTimeMillis());
                }
            }

            if (TYPE_SPLASH_BLOG.equals(_type) || TYPE_ABOUT_BLOG.equals(_type)) {
                SyndEntry entry2 = (SyndEntry) feed.getEntries().get(1);
                title2 = entry2.getTitle();
                link2 = entry2.getLink();
                text2 = entry2.getDescription().getValue();
                author2 = entry2.getAuthor();
                if (isShowDate()) {
                    date2 = entry2.getPublishedDate();
                    if (date2 == null){
                        date2 = entry2.getUpdatedDate();
                    }
                    if (date2 == null){
                        date2 = new Date(System.currentTimeMillis());
                    }
                }
            }

        } catch (Exception e) {
            _log.error("Unable to access feed at " + feedUrl);
            // ignore inability to get feed download
        }
        if (TYPE_SPLASH_BLOG.equals(_type)) {
            displaySplashBlog(title, link, text, author, date);
            displaySplashBlog(title2, link2, text2, author2, date2);
        } else if (TYPE_ABOUT_BLOG.equals(_type)) {
            displayAboutBlog(title, link, text, author, date);
            displayAboutBlog(title2, link2, text2, author2, date2);
        }else{
            display(title, link, text, author, date);

        }
    }

    private void displaySplashBlog(String title, String link, String text, String author, Date date) throws IOException {
        JspWriter out = getJspContext().getOut();

        out.print("<div class=\"blogpromo\">");
        if (!isHideAuthorImages()) {
            out.print("<div class=\"blogpromo_image_wrap fltlft\">");
            String authorImage = getAuthorImage(author);
            if (authorImage != null) {
                out.print("<a onclick=\"Popup=window.open('" +
                        link +
                        "','Popup','toolbar=yes,location=yes,status=no,menubar=yes,scrollbars=yes,resizable=no, width=917,height=600,left=50,top=50'); return false;\"\n" +
                        " href=\"" +
                        link +
                        "\">");
                out.print("<img class=\"blogpromo_image\" src=\""
                        + authorImage + "\" alt=\"" + author + "\"/>");
                out.print("</a>");
            } else {
                out.print("<img class=\"blogpromo_image\" src=\"/res/img/pixel.gif\" alt=\"\" + author + \"\"/>");
            }
            out.print("</div>");// end blogpromo_image_wrap
        }
        out.print("<div class=\"blogpromo_entry\">");
        out.print("<a onclick=\"Popup=window.open('" +
                link +
                "','Popup','toolbar=yes,location=yes,status=no,menubar=yes,scrollbars=yes,resizable=no, width=917,height=600,left=50,top=50'); return false;\"\n" +
                " href=\"" +
                link +
                "\">" +
                title +
                "</a> ");
        out.print("<div class=\"authorDate\">");
        out.print("<span class=\"smallAlertTextBold-333\">");
        out.print(author);
        out.print("</span>");
        if (date != null) {
            out.print("<span class=\"smallText1\"> ");
            out.print(Util.detailedPeriodBetweenDates(date, new Date()));
            out.print("</span>");
        }
        out.print("</div>");// end authorDate
        if (!StringUtils.isBlank(text)) {
            String strippedtext = text;
            if(text.indexOf("<div class=\"feedflare\"") > 0){
                strippedtext = text.substring(0,text.indexOf("<div class=\"feedflare\""));
            }
            //strippedtext = strippedtext.substring(0,70-title.length());
            strippedtext = Util.abbreviateAtWhitespace(strippedtext, 68-title.length());
            out.print("<span class=\"blogpromo_description Text3\">" + strippedtext + "</span>");
        }
        out.print("</div>");// end blogpromo_entry

        out.print("</div>");// end blogpromo

        out.print("<br class=\"clearfloat\"/>");
    }

    private void displayAboutBlog(String title, String link, String text, String author, Date date) throws IOException {
        JspWriter out = getJspContext().getOut();

        out.print("<div class=\"blogpromo\">");
        if (!isHideAuthorImages()) {
            out.print("<div class=\"blogpromo_image_wrap\">");
            String authorImage = getAuthorImage(author);
            if (authorImage != null) {
                out.print("<a onclick=\"Popup=window.open('" +
                        link +
                        "','Popup','toolbar=yes,location=yes,status=no,menubar=yes,scrollbars=yes,resizable=no, width=917,height=600,left=50,top=50'); return false;\"\n" +
                        " href=\"" +
                        link +
                        "\">");
                out.print("<img class=\"blogpromo_image\" src=\"" + authorImage + "\" alt=\"" + author + "\"/>");
                out.print("</a>");
            } else {
                out.print("<img class=\"blogpromo_image\" src=\"/res/img/pixel.gif\" alt=\"\" + author + \"\"/>");
            }
            out.print("</div>");// end blogpromo_image_wrap
        }
        out.print("<div class=\"blogpromo_entry text3\">");
        out.print("<a onclick=\"Popup=window.open('" +
                link +
                "','Popup','toolbar=yes,location=yes,status=no,menubar=yes,scrollbars=yes,resizable=no, width=917,height=600,left=50,top=50'); return false;\"\n" +
                " href=\"" +
                link +
                "\">" +
                title +
                "</a> ");
        out.print("<div class=\"authorDate\">");
        out.print("<span class=\"smallAlertTextBoldNeutral\">");
        out.print(author);
        out.print("</span>");
        if (date != null) {
            out.print("<span class=\"smallText1\"> ");
            out.print(Util.detailedPeriodBetweenDates(date, new Date()));
            out.print("</span>");
        }
        out.print("</div>");// end authorDate
        if (!StringUtils.isBlank(text)) {
            String strippedtext = text;
            if(text.indexOf("<div class=\"feedflare\"") > 0){
                strippedtext = text.substring(0,text.indexOf("<div class=\"feedflare\""));
            }
            strippedtext = Util.abbreviateAtWhitespace(strippedtext, 45);
            out.print("<span class=\"blogpromo_description\">" + strippedtext + "</span>");
        }
        out.print("</div>");// end blogpromo_entry
        out.print("</div>");// end blogpromo
    }

    public String getAuthorImage(String author){
        String authorImageHrefRelative;

        if (StringUtils.isBlank(author)) {
            authorImageHrefRelative = "/res/img/pixel.gif";
        } else {
            authorImageHrefRelative = "/catalog/images/blog/" + author.replaceAll("[^a-zA-Z0-9]", "").toLowerCase() + "_40x40.png";
        }

        UrlUtil urlUtil = new gs.web.util.UrlUtil();
        return urlUtil.buildUrl(authorImageHrefRelative, getRequest(), true);
    }

    private HttpServletRequest getRequest() {
        PageContext pageContext = (PageContext) getJspContext();
        return (HttpServletRequest) pageContext.getRequest();
    }

    private void display(String title, String link, String text, String author, Date date) throws IOException {
        JspWriter out = getJspContext().getOut();

        if (TYPE_BILLS_BLOG.equals(_type)) {
            out.println("<div class=\"blogTop\">");
            out.println("<div class=\"blogPhoto\">");
            out.println("<a onclick=\"Popup=window.open('" +
                    "http://blogs.greatschools.org" +
                    "','Popup','toolbar=yes,location=yes,status=no,menubar=yes,scrollbars=yes,resizable=no, width=917,height=600,left=50,top=50'); return false;\" "
                    + " href=\"" + "http://blogs.greatschools.org" +"\">"
                    + "<img src=\"/res/img/about/aboutus_blogphoto.jpg\" "
                    + "alt=\"GreatSchools Blog On School Improvement\"/></a>");

            out.println("</div>");
            out.println("<div class=\"blogDescription\">");
            if (!StringUtils.isBlank(text)) {
                String strippedtext = text;
                if(text.indexOf("<div class=\"feedflare\"") > 0){
                    strippedtext = text.substring(0,text.indexOf("<div class=\"feedflare\""));
                }
                strippedtext = Util.abbreviateAtWhitespace(strippedtext, 130-title.length());
                out.println(strippedtext);
            }
            out.println("</div>");
            out.println("</div>");
            out.println("<div class=\"rss\">");
            out.println("<div class=\"blogTitle\">");
            out.println("<a onclick=\"Popup=window.open('" +
                    link +
                    "','Popup','toolbar=yes,location=yes,status=no,menubar=yes,scrollbars=yes,resizable=no, width=917,height=600,left=50,top=50'); return false;\" "
                    + " href=\"" + link +"\">"
                    + "Read more about \"" + title + "\" &gt;"
                    + "</a>");
            out.println("</div>");
            out.println("<div id=\"readBillsBlog\">");
            out.println("<a onclick=\"Popup=window.open('" +
                    link +
                    "','Popup','toolbar=yes,location=yes,status=no,menubar=yes,scrollbars=yes,resizable=no, width=917,height=600,left=50,top=50'); return false;\" "
                    + " href=\"" + link +"\">"
                    + "<img  class=\"readBlogImage\" " +
                    "alt=\"Read Bill's Blog\" src=\"/res/img/button/read.gif\"/></a>");
            out.println("</div>");
            out.println("</div>");
        } else if (TYPE_RESEARCH_BLOG.equals(_type)) {
            /*
            <div class="billsBlogImg">
            Photo of Bill Jackson
            </div>

            <div class="blueTitleText">
            GreatSchools Blog
            </div>

            <img width="1" height="6" src="http://www.greatschools.org/res/img/header/clear_pix.gif" alt=""/>
            Since President Barack Obama assumed office two months ago, he's put forth an...
            <div style="padding-top: 9px; padding-bottom: 9px;">
            <a href="http://feedproxy.google.com/~r/typepad/GreatSchools/billsblog/~3/kC3FHFt9l7c/obamas-call-for-parent-involvement.html" onclick="Popup=window.open('http://feedproxy.google.com/~r/typepad/GreatSchools/billsblog/~3/kC3FHFt9l7c/obamas-call-for-parent-involvement.html','Popup','toolbar=yes,location=yes,status=no,menubar=yes,scrollbars=yes,resizable=no, width=917,height=600,left=50,top=50'); return false;">Read more about "Obama�s call for parent involvement" ></a>
            </div>
             */
            out.println("<div class=\"billsBlogImg\">Photo of Bill Jackson</div>");
            out.println("<div class=\"blueTitleText\">GreatSchools Blog</div>");
            out.println("News, stories and advice about issues impacting parents, kids and schools.");
            out.println("<div class=\"blogBlurb\">");
            if (!StringUtils.isBlank(text)) {
                String strippedtext = text;
                if(text.indexOf("<div class=\"feedflare\"") > 0){
                    strippedtext = text.substring(0,text.indexOf("<div class=\"feedflare\""));
                }
                strippedtext = Util.abbreviateAtWhitespace(strippedtext, 105-title.length());
                out.println(strippedtext);
            }
            out.println("<a onclick=\"Popup=window.open('" +
                    link +
                    "','Popup','toolbar=yes,location=yes,status=no,menubar=yes,scrollbars=yes,resizable=no, width=917,height=600,left=50,top=50'); return false;\" "
                    + " href=\"" + link +"\">"
                    + "Read more about \"" + title + "\" &gt;"
                    + "</a>");

            out.println("</div>");
        } else if (TYPE_GS_BLOG.equals(_type)) {
            if (date != null) {
                if (_sdf == null) {
                    _sdf = new SimpleDateFormat("MMMMM d, yyyy");
                }
                out.print("<div class=\"date\">");
                out.print(_sdf.format(date));
                out.print("</div>");
            }
            out.print("<div class=\"title\">");
            out.print("<a onclick=\"Popup=window.open('" +
                    link +
                    "','Popup','toolbar=yes,location=yes,status=no,menubar=yes,scrollbars=yes,resizable=no, width=917,height=600,left=50,top=50'); return false;\"\n" +
                    " href=\"" +
                    link +
                    "\">" +
                    title +
                    "</a>");
            out.print("</div>");
            if (!StringUtils.isBlank(text)) {
                out.print("<div class=\"body\">");
                out.print(text);
                out.print("</div>");
            }
            out.print("<div class=\"readMore\">");
            out.print("<a onclick=\"Popup=window.open('" +
                    link +
                    "','Popup','toolbar=yes,location=yes,status=no,menubar=yes,scrollbars=yes,resizable=no, width=917,height=600,left=50,top=50'); return false;\"\n" +
                    " href=\"" +
                    link +
                    "\">Read more &gt;</a>");
            out.print("</div> ");
        }

        else if (TYPE_MOM_BLOG.equals(_type)) {
            out.print("<div class=\"blogTitle\">");
            out.print("Sandra Tsing Loh shares<br/> her back-to-school saga"); // done for release 13.0 as per Julia's request.
//            out.print(title);
            out.print("</div>");
            if (!StringUtils.isBlank(text)) {
                out.print("<div class=\"blogContent\">");
                out.print(Util.abbreviateAtWhitespace(text,110));
                out.print("</div>");
            }
            out.print("<div class=\"blogLink\">");
            out.print("<a onclick=\"Popup=window.open('" +
                    link +
                    "','Popup','toolbar=yes,location=yes,status=no,menubar=yes,scrollbars=yes,resizable=no, width=917,height=600,left=50,top=50'); return false;\"\n" +
                    " href=\"" +
                    link +"\">Read the full post &gt;</a>");
            out.print("</div> ");
        }
        
    }

    public void setDefaultTitle(String defaultTitle) {
        _defaultTitle = defaultTitle;
    }

    public void setAtomUrl(String atomUrl) {
        _atomUrl = atomUrl;
    }

    public void setDefaultUrl(String defaultUrl) {
        _defaultUrl = defaultUrl;
    }

    public void setType(String type) {
        _type = type;
    }

    public boolean isShowDate() {
        return _showDate;
    }

    public void setShowDate(boolean showDate) {
        _showDate = showDate;
    }

    public boolean isHideAuthorImages() {
        return _hideAuthorImages;
    }

    public void setHideAuthorImages(boolean hideAuthorImages) {
        _hideAuthorImages = hideAuthorImages;
    }
}