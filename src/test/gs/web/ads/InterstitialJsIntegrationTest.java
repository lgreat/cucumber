package gs.web.ads;

import net.sf.jstester.JsTestCase;
import gs.web.IntegrationTestCase;

/**
 * @author thuss
 */
public class InterstitialJsIntegrationTest extends JsTestCase implements IntegrationTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        // browserEnv.js gets us mocked versions of document, window, location, etc... to work with
        eval(loadScript("res/js/test/browserEnv.js"));
        // This is the script under test
        eval(loadScript("res/js/interstitial.js"));
        // Get our javascript a document to work this, this causes a real HTTP request
        eval("window.location = '"+ INTEGRATION_HOST +"/res/js/test/testPage.html';");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMakeInterstitialHref() {
        eval("var href = makeInterstitialHref('X','Y');");
        assertEquals("http://localhost/ads/interstitial.page?adslot=Y&passThroughURI=X", eval("href"));
    }

}
