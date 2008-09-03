/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: BlogFeedTagHandler.java,v 1.11 2008/09/03 17:48:47 yfan Exp $
 */

package gs.web.content;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
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

/**
 * Provides...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class BlogFeedTagHandler extends SimpleTagSupport {


    private static final Log _log = LogFactory.getLog(BlogFeedTagHandler.class);
    private static final String TYPE_BILLS_BLOG = "billsBlog";
    private static final String TYPE_GS_BLOG = "gsBlog";
    private String _defaultTitle;
    private String _atomUrl;
    private String _defaultUrl;
    private String _type;

    public void doTag() throws JspException, IOException {
        super.doTag();
        String link = _defaultUrl;
        String text = "";
        String title = _defaultTitle;
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


        } catch (Exception e) {
            _log.error("Unable to access feed at " + feedUrl);
            // ignore inability to get feed download
        }

        display(title, link, text);
    }

    private void display(String title, String link, String text) throws IOException {
        JspWriter out = getJspContext().getOut();

        if (TYPE_BILLS_BLOG.equals(_type)) {
            out.print(text);
            out.print("<div style='padding-top: 9px;padding-bottom: 9px;'> ");
            out.print("<a onclick=\"Popup=window.open('" +
                    link +
                    "','Popup','toolbar=yes,location=yes,status=no,menubar=yes,scrollbars=yes,resizable=no, width=800,height=600,left=50,top=50'); return false;\"\n" +
                    " href=\"" +
                    link +
                    "\">Read more about \"" + title + "\" &gt;</a>");
            out.print("</div> ");
        } else if (TYPE_GS_BLOG.equals(_type)) {
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
}
