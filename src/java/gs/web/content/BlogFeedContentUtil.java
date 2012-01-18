package gs.web.content;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import gs.web.jsp.Util;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rramachandran
 * Date: 1/11/12
 * Time: 10:57 AM
 * To change this template use File | Settings | File Templates.
 */

public class BlogFeedContentUtil {
    private static int displayBlogLinksCount;
    private static final Log _log = LogFactory.getLog(BlogFeedContentUtil.class);
    private static String _atomUrl;

    private static final String HTTP_PROXY_HOSTNAME_PROPERTY = "http.proxy.hostname";
    private static final String HTTP_PROXY_PORT_PROPERTY = "http.proxy.port";

    // GS-12398 BlogEntry class with required fields for displaying blog links in the sidebar blog module of index, pressRoom and articles pages
    public class BlogEntry {
        private String _title;
        private String _author;
        private String _date;
        private String _link;
        private String _text;
        private String _authorImage;

        public String getTitle() {
            return _title;
        }

        public void setTitle(String title) {
            _title = title;
        }

        public String getAuthor() {
            return _author;
        }

        public void setAuthor(String author) {
            _author = author;
        }

        public String getDate() {
            return _date;
        }

        public void setDate(String date) {
            _date = date;
        }

        public String getLink() {
            return _link;
        }

        public void setLink(String link) {
            _link = link;
        }

        public String getText() {
            return _text;
        }

        public void setText(String text) {
            _text = text;
        }

        public String getAuthorImage() {
            return _authorImage;
        }

        public void setAuthorImage(String authorImage) {
            _authorImage = authorImage;
        }
    }

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

    // GS-12398 gets a list of BlogEntry objects (number based on value of blogLinksCount) that will have their fields set based on whether this is called from an article page or index/pressRoom page (isArticleSidebar)
    // Returns an empty list in case of exception.
    public static List<BlogEntry> getBlogEntries(String atomURL, int blogLinksCount, boolean fetchAuthorImage, boolean fetchBlogEntryText) {
        _atomUrl = atomURL;
        displayBlogLinksCount = blogLinksCount;
        List<BlogEntry> blogEntries = new ArrayList<BlogEntry>();
        try {
            URL feedUrl = new URL(_atomUrl);
            for(int i = 0; i < displayBlogLinksCount; i++){
                System.setProperty("sun.net.client.defaultConnectTimeout", "5000");
                System.setProperty("sun.net.client.defaultReadTimeout", "5000");
                Date date = null;
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(getXmlReader(feedUrl));
                SyndEntry entry = (SyndEntry) feed.getEntries().get(i);
                BlogFeedContentUtil blogFeedContent = new BlogFeedContentUtil();
                BlogEntry blogEntry = blogFeedContent.new BlogEntry();
                blogEntry.setTitle(entry.getTitle());
                blogEntry.setLink(entry.getLink());
                blogEntry.setAuthor(entry.getAuthor());
                if (entry.getPublishedDate() != null){
                    date = entry.getPublishedDate();
                }
                else if(date == null && entry.getUpdatedDate() != null){
                    date = entry.getUpdatedDate();
                }
                else{
                    date = new Date(System.currentTimeMillis());
                }
                blogEntry.setDate(Util.detailedPeriodBetweenDates(date,new Date()));
                if(fetchBlogEntryText){
                    String text = entry.getDescription().getValue();
                    if (!StringUtils.isBlank(text)) {
                        String strippedText = text;
                        if(text.indexOf("<div class=\"feedflare\"") > 0){
                            strippedText = text.substring(0,text.indexOf("<div class=\"feedflare\""));
                        }
                        strippedText = Util.abbreviateAtWhitespace(strippedText, 45);
                        blogEntry.setText(strippedText);
                    }
                }
                if(fetchAuthorImage) {
                    String imageSource = BlogFeedContentUtil.generateAuthorImageURL(blogEntry.getAuthor());
                    blogEntry.setAuthorImage(imageSource);
                }
                blogEntries.add(blogEntry);
            }
        } catch (Exception e) {
            _log.error("Unable to access feed at " + _atomUrl);
            // ignore inability to get feed download. render the page without errors even if unable to display any of the blog entries due to exception.
        }
        return blogEntries;
    }

    // GS-12398 Method to return the source of the image file to be displayed
    public static String generateAuthorImageURL(String author){
        String imgSrc;
        if (StringUtils.isBlank(author)) {
            imgSrc = "/res/img/pixel.gif";
        }
        else {
            imgSrc = "/catalog/images/blog/" + author.replaceAll("[^a-zA-Z0-9]", "").toLowerCase() + "_40x40.png";
        }
        return imgSrc;
    }
}
