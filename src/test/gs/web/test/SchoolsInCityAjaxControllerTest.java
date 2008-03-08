package gs.web.test;

import gs.web.BaseControllerTestCase;
import gs.data.school.LevelCode;
import org.springframework.web.servlet.ModelAndView;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class SchoolsInCityAjaxControllerTest extends BaseControllerTestCase {
    private SchoolsInCityAjaxController _controller;

    public void setUp() throws Exception {
        super.setUp();
        _controller = (SchoolsInCityAjaxController)getApplicationContext().
                getBean(SchoolsInCityAjaxController.BEAN_ID);
    }

    public void testHandleRequest() throws Exception {
        getRequest().setParameter("state", "AK");
        getRequest().setParameter("city", "Anchorage");
        ModelAndView mAndV = _controller.handleRequest(getRequest(), getResponse());
        assertNull(mAndV);
        String anchorageOutput = getResponse().getContentAsString();
        assertTrue (anchorageOutput.contains("<option value=\"116\">Abbott Loop Elementary School</option>"));

        getRequest().setParameter("city", "Fairbanks");
        _controller.handleRequest(getRequest(), getResponse());
        String fairbanksOutput = getResponse().getContentAsString();
        assertTrue (fairbanksOutput.contains("<option value=\"383\">West Valley High School</option>"));
    }

    public void testOutputSchoolSelect() throws Exception {
        try {
            _controller.outputSchoolSelect(getRequest(), getResponse().getWriter(), null);
            fail ("Method should not accept request without a 'state' attribute");
        } catch (Exception e) {
            assertTrue (true);
        }

        StringWriter sWriter = new StringWriter();
        PrintWriter pWriter = new PrintWriter(sWriter);

        getRequest().setParameter("state", "AK");
        getRequest().setParameter("city", "Anchorage");
        _controller.outputSchoolSelect(getRequest(), pWriter, null);
        String output = sWriter.toString();
        assertTrue(output.contains("Willow Crest"));
        assertTrue(output.contains("Romig"));
        assertTrue(output.contains("West High"));

        sWriter = new StringWriter();
        pWriter = new PrintWriter(sWriter);        
        _controller.outputSchoolSelect(getRequest(), pWriter, LevelCode.ELEMENTARY);
        output = sWriter.toString();
        assertTrue(output.contains("Willow Crest"));
        assertFalse(output.contains("Romig"));
        assertFalse(output.contains("West High"));

        sWriter = new StringWriter();
        pWriter = new PrintWriter(sWriter);
        _controller.outputSchoolSelect(getRequest(), pWriter, LevelCode.MIDDLE);
        output = sWriter.toString();
        assertFalse(output.contains("Willow Crest"));
        assertTrue(output.contains("Romig"));
        assertFalse(output.contains("West High"));

        sWriter = new StringWriter();
        pWriter = new PrintWriter(sWriter);
        _controller.outputSchoolSelect(getRequest(), pWriter, LevelCode.HIGH);
        output = sWriter.toString();
        assertFalse(output.contains("Willow Crest"));
        assertFalse(output.contains("Romig"));
        assertTrue(output.contains("West High"));

        sWriter = new StringWriter();
        pWriter = new PrintWriter(sWriter);
        _controller.outputSchoolSelect(getRequest(), pWriter, LevelCode.ELEMENTARY_MIDDLE);
        output = sWriter.toString();
        assertTrue(output.contains("Willow Crest"));
        assertTrue(output.contains("Romig"));
        assertFalse(output.contains("West High"));

        sWriter = new StringWriter();
        pWriter = new PrintWriter(sWriter);
        _controller.outputSchoolSelect(getRequest(), pWriter, LevelCode.MIDDLE_HIGH);
        output = sWriter.toString();
        assertFalse(output.contains("Willow Crest"));
        assertTrue(output.contains("Romig"));
        assertTrue(output.contains("West High"));
    }
}
