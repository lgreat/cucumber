/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: BlogFeedTagHandler.java,v 1.1 2006/07/03 22:14:45 apeterson Exp $
 */

package gs.web.content;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private String _defaultTitle;
    private String _atomUrl;
    private String _defaultUrl;

    public void doTag() throws JspException, IOException {
        super.doTag();
        String link = _defaultUrl;
        String text = "";
        String title = _defaultTitle;
        URL feedUrl = new URL(_atomUrl);
        try {
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

        JspWriter out = getJspContext().getOut();
        out.print(text);
        out.print("<a onclick=\"Popup=window.open('" +
                link +
                "','Popup','toolbar=no,location=yes,status=no,menubar=no,scrollbars=yes,resizable=no, width=800,height=600,left=50,top=50'); return false;\"\n" +
                " href=\"" +
                link +
                "\">Read More about " + title + "</a>");

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
}
