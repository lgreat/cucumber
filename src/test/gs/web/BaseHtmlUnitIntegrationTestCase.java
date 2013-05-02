package gs.web;

import junit.framework.TestCase;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.nutrun.xhtml.validator.XhtmlValidator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.experimental.categories.Category;

/**
 * @author thuss
 */
@Category(IntegrationTest.class)
public class BaseHtmlUnitIntegrationTestCase extends TestCase implements IntegrationTestCase {
    protected final Log _log = LogFactory.getLog(getClass());
    protected WebClient _webClient = null;

    public void setUp() {
        // We default to Firefox 2 because minmax.js causes rhino javascript issues
        _webClient = new WebClient(BrowserVersion.FIREFOX_2);
        // Turning on javascript should be a conscious decision since it has side-effects
        // such as hitting Omniture, etc...
        _webClient.setJavaScriptEnabled(false);
        // Same for Cookies since they can affect how a page is rendered
        _webClient.setCookiesEnabled(false);
    }

    /**
     * assert that an XHTML page is valid XHTML
     *
     * @param url
     */
    protected void assertValidXhtml(String url) {
        WebClient webClient = new WebClient();
        // Disable javascript since our JS invalidates the DOM
        webClient.setJavaScriptEnabled(false);
        HtmlPage page = null;
        try {
            page = (HtmlPage) webClient.getPage(url);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals(200, page.getWebResponse().getStatusCode());
        String source = page.asXml();
        // page.asXML for some reason doesn't include the DOCTYPE declaration that's in the source
        source = source.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>",
                "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML+RDFa 1.0//EN\" \"http://www.w3.org/MarkUp/DTD/xhtml-rdfa-1.dtd\">");
        // Since we usually put article lists in a <ul> and the sample database only contains
        // some articles, we often get empty ul's on the localhost so we fill in empty ul's
        Pattern pattern = Pattern.compile("(<ul[^>]*)/>", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(source);
        source = matcher.replaceAll("$1><li>placeholder</li></ul>");
//        try {
//            File outFile = new File("/tmp/out.html"); // Change folder to be valid for your system
//            FileWriter out = new FileWriter(outFile);
//            out.write(source);
//            out.close();
//        } catch (Exception e) { _log.warn("Failed to output html: " + e.getMessage()); }
        XhtmlValidator validator = new XhtmlValidator();
        boolean valid = false;
        try {
            valid = validator.isValid(new ByteArrayInputStream(source.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
        }
        assertTrue(url + " is not valid Xhtml, errors are: " + Arrays.deepToString(validator.getErrors()), valid);
    }
}
