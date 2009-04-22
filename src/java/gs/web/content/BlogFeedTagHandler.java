/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: BlogFeedTagHandler.java,v 1.19 2009/04/22 21:24:03 jnorton Exp $
 */

package gs.web.content;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.text.SimpleDateFormat;

import gs.web.jsp.Util;

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class BlogFeedTagHandler extends SimpleTagSupport {


    private static final Log _log = LogFactory.getLog(BlogFeedTagHandler.class);
    private static final String TYPE_BILLS_BLOG = "billsBlog";
    private static final String TYPE_GS_BLOG = "gsBlog";
    private static final String TYPE_SPLASH_BLOG = "splashBlog";
    private static final String TYPE_RESEARCH_BLOG = "researchBlog";
    private String _defaultTitle;
    private String _atomUrl;
    private String _defaultUrl;
    private String _type;
    private boolean _showDate;
    private SimpleDateFormat _sdf = null;

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
            SyndFeed feed = input.build(new XmlReader(feedUrl));
            SyndEntry entry = (SyndEntry) feed.getEntries().get(0);
            title = entry.getTitle();
            link = entry.getLink();
            text = entry.getDescription().getValue();
            author = entry.getAuthor();
            if (isShowDate()) {
                date = entry.getPublishedDate();
                if(date == null || date.equals(null)){
                    date = entry.getUpdatedDate();
                }
                if(date == null || date.equals(null)){
                    date = new Date(System.currentTimeMillis());
                }
            }

            if (TYPE_SPLASH_BLOG.equals(_type)) {
                SyndEntry entry2 = (SyndEntry) feed.getEntries().get(1);
                title2 = entry2.getTitle();
                link2 = entry2.getLink();
                text2 = entry2.getDescription().getValue();
                author2 = entry2.getAuthor();
                if (isShowDate()) {
                    date2 = entry2.getPublishedDate();
                    if(date2 == null || date2.equals(null)){
                        date2 = entry2.getUpdatedDate();
                    }
                    if(date2 == null || date2.equals(null)){
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
        }else{
            display(title, link, text, author, date);

        }
    }

    private void displaySplashBlog(String title, String link, String text, String author, Date date) throws IOException {
        JspWriter out = getJspContext().getOut();

        out.print("<div class=\"blogpromo\">");
        out.print("<div class=\"blogpromo_image_wrap\">");
        String authorImage = getAuthorImage(author);
        if(authorImage != null){
            out.print("<a onclick=\"Popup=window.open('" +
                    link +
                    "','Popup','toolbar=yes,location=yes,status=no,menubar=yes,scrollbars=yes,resizable=no, width=917,height=600,left=50,top=50'); return false;\"\n" +
                    " href=\"" +
                    link +
                    "\">");
            out.print("<img class=\"blogpromo_image\" src=\""
                    + authorImage + "\" alt=\"" + author + "\"/>") ;
            out.print("</a>");
        }else{
            out.print("<img class=\"blogpromo_image\" src=\"/res/img/pixel.gif\" alt=\"\" + author + \"\"/>") ;
        }
        out.print("</div>");
        out.print("<div class=\"blogpromo_entry\">");
        out.print("<a onclick=\"Popup=window.open('" +
                link +
                "','Popup','toolbar=yes,location=yes,status=no,menubar=yes,scrollbars=yes,resizable=no, width=917,height=600,left=50,top=50'); return false;\"\n" +
                " href=\"" +
                link +
                "\">" +
                title +
                "</a> ");
        if (!StringUtils.isBlank(text)) {
            String strippedtext = text;
            if(text.indexOf("<div class=\"feedflare\"") > 0){
                strippedtext = text.substring(0,text.indexOf("<div class=\"feedflare\""));
            }
            //strippedtext = strippedtext.substring(0,70-title.length());
            strippedtext = Util.abbreviateAtWhitespace(strippedtext, 68-title.length());
            out.print("<span class=\"blogpromo_description\">" + strippedtext + "</span>");
        }
        out.print("</div>");
        out.print("</div>");
    }

    private String getAuthorImage(String author){
        Map imageAuthor = new HashMap();
        imageAuthor.put("GreatSchools","/res/img/blog_bill.jpg");
        imageAuthor.put("Bill Jackson","/res/img/blog_billjackson.jpg");
        imageAuthor.put("Kelsey Parker","/res/img/blog_kelseyparker.jpg");
        imageAuthor.put("Dave Steer","/res/img/blog_davesteer.jpg");
        imageAuthor.put("Jim Daly", "/res/img/blog_jimdaly.jpg");

        /* Uncomment this to use community gifs
        imageAuthor.put("Bill Jackson","http://community.greatschools.net/avatar?id=1000&height=94&width=94");
        imageAuthor.put("Kelsey Parker","http://community.greatschools.net/avatar?id=2694028&height=94&width=94");
        imageAuthor.put("Dave Steer","http://community.greatschools.net/avatar?id=3090256&height=94&width=94");
        */
        
        return imageAuthor.get(author) != null ? imageAuthor.get(author).toString() : "/res/img/pixel.gif";

    }

    private void display(String title, String link, String text, String author, Date date) throws IOException {
        JspWriter out = getJspContext().getOut();

        if (TYPE_BILLS_BLOG.equals(_type)) {
            out.println("<div class=\"blogTop\">");
            out.println("<div class=\"blogPhoto\">");
            out.println("<a onclick=\"Popup=window.open('" +
                    "http://blogs.greatschools.net" +
                    "','Popup','toolbar=yes,location=yes,status=no,menubar=yes,scrollbars=yes,resizable=no, width=917,height=600,left=50,top=50'); return false;\" "
                    + " href=\"" + "http://blogs.greatschools.net" +"\">"
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

            <img width="1" height="6" src="http://www.greatschools.net/res/img/header/clear_pix.gif" alt=""/>
            Since President Barack Obama assumed office two months ago, he's put forth an...
            <div style="padding-top: 9px; padding-bottom: 9px;">
            <a href="http://feedproxy.google.com/~r/typepad/GreatSchools/billsblog/~3/kC3FHFt9l7c/obamas-call-for-parent-involvement.html" onclick="Popup=window.open('http://feedproxy.google.com/~r/typepad/GreatSchools/billsblog/~3/kC3FHFt9l7c/obamas-call-for-parent-involvement.html','Popup','toolbar=yes,location=yes,status=no,menubar=yes,scrollbars=yes,resizable=no, width=917,height=600,left=50,top=50'); return false;">Read more about "ObamaÕs call for parent involvement" ></a>
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
}
