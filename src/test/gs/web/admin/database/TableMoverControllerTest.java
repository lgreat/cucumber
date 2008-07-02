package gs.web.admin.database;

import gs.web.BaseControllerTestCase;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;

import java.util.List;
import java.util.Set;

public class TableMoverControllerTest extends BaseControllerTestCase {
    private TableMoverController _controller;

    protected void setUp() throws Exception {
        super.setUp();
        _request.setMethod("GET");
        _controller = (TableMoverController) getApplicationContext().getBean(TableMoverController.BEAN_ID);
    }

    public void testRequestToDevSucceeds() throws Exception {
        _sessionContext.setHostName("dev.greatschools.net");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected view for dev", _controller.getFormView(), modelAndView.getViewName());
    }

    public void testRequestToProductionFails() throws Exception {
        _sessionContext.setHostName("www.greatschools.net");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected view for production", _controller._errorViewName, modelAndView.getViewName());
    }

    public void testRequiredStateAndTableSetsValidation() {
        TableMoverCommand cmd = new TableMoverCommand();
        cmd.setTablesets(new String[0]);
        cmd.setStates(new String[0]);
        BindException errors = new BindException(cmd, "");
        _controller.onBindAndValidate(_request, cmd, errors);
        assertEquals("There should be an error for missing state and table sets",
                2, errors.getAllErrors().size());

        cmd.setStates(new String[]{"CA"});
        errors = new BindException(cmd, "");
        _controller.onBindAndValidate(_request, cmd, errors);
        assertEquals("There should be an error for missing table sets",
                1, errors.getAllErrors().size());

        cmd.setTablesets(new String[]{"school"});
        errors = new BindException(cmd, "");
        _controller.onBindAndValidate(_request, cmd, errors);
        assertEquals("Should not be an error because state and table sets provided",
                0, errors.getAllErrors().size());
    }

    public void testTableSets() {
        TableMoverCommand cmd = new TableMoverCommand();
        BindException errors = new BindException(cmd, "");

        // Test duplicate tables with the database specified
        cmd.setTablesets(new String[]{"us_geo.city,us_geo.city", "us_geo.city"});
        cmd.setStates(new String[]{});
        ModelAndView mv = _controller.onSubmit(_request, _response, cmd, errors);
        List tables = (List) mv.getModel().get("tables");
        assertEquals(1, tables.size());
        assertEquals("us_geo.city", tables.get(0));

        // Test state specific tables and dups
        cmd.setTablesets(new String[]{"school,us_geo.city", "us_geo.city,school"});
        cmd.setStates(new String[]{"WY", "OR"});
        mv = _controller.onSubmit(_request, _response, cmd, errors);
        tables = (List) mv.getModel().get("tables");
        assertEquals(3, tables.size());
        assertTrue(tables.contains("us_geo.city"));
        assertTrue(tables.contains("_or.school"));
        assertTrue(tables.contains("_wy.school"));

        // Test no dups
        cmd.setTablesets(new String[]{"school", "us_geo.city"});
        cmd.setStates(new String[]{"WY", "OR"});
        mv = _controller.onSubmit(_request, _response, cmd, errors);
        tables = (List) mv.getModel().get("tables");
        assertEquals(3, tables.size());
        assertTrue(tables.contains("us_geo.city"));
        assertTrue(tables.contains("_or.school"));
        assertTrue(tables.contains("_wy.school"));

        // Test all states
        cmd.setTablesets(new String[]{"school", "us_geo.city"});
        cmd.setStates(new String[]{"", "OR"});
        mv = _controller.onSubmit(_request, _response, cmd, errors);
        tables = (List) mv.getModel().get("tables");
        assertEquals(52, tables.size());
        assertTrue(tables.contains("us_geo.city"));
        assertTrue(tables.contains("_or.school"));
        assertTrue(tables.contains("_wy.school"));
    }
}