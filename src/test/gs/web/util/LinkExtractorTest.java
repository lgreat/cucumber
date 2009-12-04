package gs.web.util;

import junit.framework.TestCase;

import java.util.List;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.org>
 */
public class LinkExtractorTest extends TestCase {

    public void testExtractor() throws Exception {
        String text = "This is some <a href=\"http://www.foo.com\">sample</a> text";
        List links = LinkExtractor.extractLinks(text);
        assertEquals ("http://www.foo.com", links.get(0));
    }
}
