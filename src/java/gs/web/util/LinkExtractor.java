package gs.web.util;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class LinkExtractor {

    public static List extractLinks(String s) {
        List links = new ArrayList();
        Pattern hrefPattern = Pattern.compile("href=\".*\"");
        Matcher matcher = hrefPattern.matcher(s);
        while(matcher.find()) {
            String match = matcher.group();
            links.add (match.substring(6, match.length()-1));
        }
        return links;
    }
}

