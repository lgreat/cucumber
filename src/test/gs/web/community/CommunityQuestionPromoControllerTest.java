package gs.web.community;

import gs.web.BaseControllerTestCase;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CommunityQuestionPromoControllerTest extends BaseControllerTestCase {
    private CommunityQuestionPromoController _controller;
    private String _worksheetUrl;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new CommunityQuestionPromoController();
        _worksheetUrl = CommunityQuestionPromoController.WORKSHEET_PREFIX + "/" +
                CommunityQuestionPromoController.WORKSHEET_KEY + "/" +
                CommunityQuestionPromoController.WORKSHEET_VISIBILITY + "/" +
                CommunityQuestionPromoController.WORKSHEET_PROJECTION + "/";
        _worksheetUrl += "od7";
    }

    public void testLoadSpreadsheetData() {
        Map<String, Object> model = new HashMap<String, Object>();
        _controller.loadSpreadsheetDataIntoModel(model, _worksheetUrl, "school/rating.page");

        assertEquals("Other than the education one receives - what makes a school \"great\"?",
                model.get(CommunityQuestionPromoController.MODEL_QUESTION_TEXT));
        assertEquals("http://community.greatschools.net/q-and-a/123301/Other-than-the-education-one-receives-what-makes-a-school-great",
                model.get(CommunityQuestionPromoController.MODEL_QUESTION_LINK));
        assertEquals("Queserasera",
                model.get(CommunityQuestionPromoController.MODEL_USERNAME));
        assertEquals("2093577",
                model.get(CommunityQuestionPromoController.MODEL_USER_ID));

        model.clear();
        _controller.loadSpreadsheetDataIntoModel(model, _worksheetUrl, "school/parentReviews.page");

        assertEquals("What's the difference?",
                model.get(CommunityQuestionPromoController.MODEL_QUESTION_TEXT));
        assertEquals("http://community.greatschools.net/q-and-a",
                model.get(CommunityQuestionPromoController.MODEL_QUESTION_LINK));
        assertEquals("Anthony",
                model.get(CommunityQuestionPromoController.MODEL_USERNAME));
        assertEquals("1",
                model.get(CommunityQuestionPromoController.MODEL_USER_ID));
    }

    public void testGetWorksheetUrl() {
        String baseWorksheet = CommunityQuestionPromoController.WORKSHEET_PREFIX + "/" +
                CommunityQuestionPromoController.WORKSHEET_KEY + "/" +
                CommunityQuestionPromoController.WORKSHEET_VISIBILITY + "/" +
                CommunityQuestionPromoController.WORKSHEET_PROJECTION + "/";

        getRequest().setServerName("localhost");
        assertEquals(baseWorksheet + "od6", _controller.getWorksheetUrl(getRequest()));

        getRequest().setServerName("dev.greatschools.net");
        assertEquals(baseWorksheet + "od6", _controller.getWorksheetUrl(getRequest()));

        getRequest().setServerName("yahooed.dev.greatschools.net");
        assertEquals(baseWorksheet + "od6", _controller.getWorksheetUrl(getRequest()));

        getRequest().setServerName("staging.greatschools.net");
        assertEquals(baseWorksheet + "od4", _controller.getWorksheetUrl(getRequest()));

        getRequest().setServerName("rithmatic.greatschools.net");
        assertEquals(baseWorksheet + "od4", _controller.getWorksheetUrl(getRequest()));

        getRequest().setServerName("sfgate.staging.greatschools.net");
        assertEquals(baseWorksheet + "od4", _controller.getWorksheetUrl(getRequest()));

        getRequest().setServerName("sfgate.greatschools.net");
        assertEquals(baseWorksheet + "od4", _controller.getWorksheetUrl(getRequest()));

        getRequest().setServerName("www.greatschools.net");
        assertEquals(baseWorksheet + "od4", _controller.getWorksheetUrl(getRequest()));
    }
}
