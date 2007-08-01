package gs.web.util;

import org.apache.commons.lang.StringUtils;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * based on crawler designs found here:
 * http://www.devarticles.com/c/a/Java/Crawling-the-Web-with-Java/
 * 
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class Crawler {

    private int _maxPages = 10;          // default;
    private boolean _respectRobots = true; // default;
    private boolean _respectMeta = false;  // default;

    private List _visitors = new ArrayList();

    /** Default constructor required by Spring */
    public Crawler() {
    }

    public void crawl(String root) {
        crawl(root, _maxPages);
    }

    public void crawl(String root, int maxPages) {
        crawl(getAbsoluteURL(root), maxPages, true);
    }

    protected void crawl (URL startUrl, int maxUrls, boolean limitHost) {
        System.out.println("start " + startUrl);
        HashSet crawled = new HashSet();
        LinkedHashSet toCrawlList = new LinkedHashSet();
        // Add start URL to the to crawl list.
        toCrawlList.add(startUrl);
        /* Perform actual crawling by looping through the To Crawl list. */
        while (toCrawlList.size() > 0) {
            /* Check to see if the max URL count has been reached, if it was specified.*/
            if (maxUrls != -1) {
                if (crawled.size() == maxUrls) {
                    break;
                }
            }
            URL url = (URL)toCrawlList.iterator().next();
            toCrawlList.remove(url);
            // Convert string url to URL object.
            //URL verifiedUrl = getAbsoluteURL(url);
            // Skip URL if robots are not allowed to access it.
            if (!allow(url)) {
                continue;
            }
            // Update crawling stats.
            //updateStats(url, crawledList.size(), toCrawlList.size(), maxUrls);
            // Add page to the crawled list.
            crawled.add(url);
            // Download the page at the given URL.
            String contentString = getContentAsString(url);

            List visitors = getVisitors();
            for (int j = 0; j < visitors.size(); j++) {
                IPageVisitor visitor = (IPageVisitor)visitors.get(j);
                visitor.visit(new Page(url, contentString));
            }

            if (StringUtils.isNotBlank(contentString)) {
                // Retrieve list of valid links from page.
                List links = getLinks(url, contentString, crawled, limitHost);
                toCrawlList.addAll(links);
            }
        }
    }

    public URL getAbsoluteURL(String s) {
        // Only allow HTTP URLs.
        if (!s.toLowerCase().startsWith("http://"))
            return null;
        // Verify format of URL.
        URL verifiedUrl = null;
        try {
            verifiedUrl = new URL(s);
        } catch (MalformedURLException e) {
            return null;
        }
        return verifiedUrl;
    }

    private List getLinks (URL pageUrl, String contentString,
                           HashSet crawledList, boolean limitHost) {
        Pattern p = Pattern.compile("<a\\s+href\\s*=\\s*\"?(.*?)[\"|>]",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(contentString);
        // Create list of link matches.
        ArrayList linkList = new ArrayList();
        while (m.find()) {
            String link = m.group(1).trim();
            // Skip empty links.
            if (link.length() < 1) {
                continue;
            }
            // Skip links that are just page anchors.
            if (link.charAt(0) == '#') {
                continue;
            }
            // Skip mailto links.
            if (link.indexOf("mailto:") != -1) {
                continue;
            }
            // Skip JavaScript links.
            if (link.toLowerCase().indexOf("javascript") != -1) {
                continue;
            }
            // Prefix absolute and relative URLs if necessary.
            if (link.indexOf("://") == -1) {
                // Handle absolute URLs.
                if (link.charAt(0) == '/') {
                    link = "http://" + pageUrl.getHost() + link;
                    // Handle relative URLs.
                } else {
                    String file = pageUrl.getFile();
                    if (file.indexOf('/') == -1) {
                        link = "http://" + pageUrl.getHost() + "/" + link;
                    } else {
                        String path =
                                file.substring(0, file.lastIndexOf('/') + 1);
                        link = "http://" + pageUrl.getHost() + path + link;
                    }
                }
            }
            // Remove anchors from link.
            int index = link.indexOf('#');
            if (index != -1) {
                link = link.substring(0, index);
            }
            // Remove leading "www" from URL's host if present.
            //link = removeWwwFromUrl(link);
            // Verify link and skip if invalid.
            URL verifiedLink = getAbsoluteURL(link);
            if (verifiedLink == null) {
                continue;
            }
            /* If specified, limit links to those having the same host as the start URL. */
            if (limitHost && !pageUrl.getHost().toLowerCase().equals(
                    verifiedLink.getHost().toLowerCase())) {
                continue;
            }
            // Skip link if it has already been crawled.
            if (crawledList.contains(verifiedLink)) {
                continue;
            }
            // Add link to list.
            linkList.add(verifiedLink);
        }
        return (linkList);
        //return new ArrayList();
    }

    String getContentAsString(URL url) {
        String contentString = null;
        if ("http".equalsIgnoreCase(url.getProtocol())) {
            try {
                HttpURLConnection con =
                        (HttpURLConnection)url.openConnection();
                String type = con.getContentType();
                if (type.startsWith("text")) {
                    BufferedReader br = null;
                    try {
                        br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        String line = br.readLine();
                        StringBuffer buffer = new StringBuffer();
                        while (line != null) {
                            buffer.append(line);
                            line = br.readLine();
                        }
                        contentString = buffer.toString();
                    } finally {
                        if (br != null) {
                            br.close();
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println ("e: "+ e);
            }
        } else {
            System.out.println ("only http urls are supported");
        }

        return contentString;
    }

    /**
     * todo
     * @param url The page url to check for "disallow"
     * @return true if there is no restriction on crawling this page
     */
    protected boolean allow(URL url) {
        boolean allow = true;
        if (_respectRobots) {
            if (_respectMeta) {

            }
        }
        return allow;
    }

    public void addPageVisitor(IPageVisitor ipv) {
        _visitors.add(ipv);
    }

    List getVisitors() {
        return _visitors;
    }
}