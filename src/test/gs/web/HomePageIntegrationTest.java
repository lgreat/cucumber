package gs.web;

import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.html.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author thuss
 *         <p/>
 *         To learn more about HtmlUnit see http://htmlunit.sourceforge.net/gettingStarted.html
 */
public class HomePageIntegrationTest extends BaseHtmlUnitIntegrationTestCase {

    protected final String PAGE_URL = INTEGRATION_HOST + "/index.page";

    public void setUp() {
        super.setUp();
    }

    public void testHomePageToSearchTransition() throws Exception {
        // We rarely want to turn on Javascript for testing since it has many side effects (such as
        // hitting Omniture, tacoda, etc... but below I'm testing the search form submission
        // which uses javascript so I'm explicitly turning it on
        _webClient.setJavaScriptEnabled(true);
        final HtmlPage page = (HtmlPage) _webClient.getPage(PAGE_URL + "?cobrand=framed");

        assertTrue(page.getTitleText().contains("GreatSchools"));

        // Test the search form
        HtmlForm form = (HtmlForm) page.getFirstByXPath("//form[@action='/stateLauncher.page']");
        HtmlTextInput textField = (HtmlTextInput) form.getInputByName("q");
        textField.setValueAttribute("newhalen");
        HtmlImageInput submit = (HtmlImageInput) form.getFirstByXPath("./input[@type='image']");

        // Click the search button and verify javascript select state popup
        List collectedAlerts = new ArrayList();
        _webClient.setAlertHandler(new CollectingAlertHandler(collectedAlerts));
        HtmlPage searchPage = (HtmlPage) submit.click();
        assertEquals(collectedAlerts.size(), 1);
        assertEquals("Please select a state.", collectedAlerts.get(0));

        // Now select a state and verify we land on the search page with the result
        HtmlSelect select = (HtmlSelect) form.getHtmlElementById("splashStateSelector");
        select.setSelectedAttribute("AK", true);
        searchPage = (HtmlPage) submit.click();
        assertEquals(INTEGRATION_HOST + "/search/search.page?state=AK&q=newhalen&type=school",
                searchPage.getWebResponse().getUrl().toString());
        assertTrue("Search page should contain the result we searched for",
                searchPage.asText().contains("Newhalen School"));
    }
}
