package gs.web.admin.database;

import gs.web.BaseControllerTestCase;
import gs.web.admin.gwt.server.TableCopyServiceImpl;
import gs.web.admin.gwt.client.ServiceException;
import gs.web.admin.gwt.client.TableData;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;

import java.util.List;
import java.util.Arrays;
import java.io.IOException;

import static org.easymock.classextension.EasyMock.*;
import org.apache.commons.lang.ArrayUtils;

public class TableMoverControllerTest extends BaseControllerTestCase {
    private TableMoverController _controller;

    private TableCopyServiceImpl _tableCopyServiceBackup;

    protected void setUp() throws Exception {
        super.setUp();
        _request.setMethod("GET");
        _controller = (TableMoverController) getApplicationContext().getBean(TableMoverController.BEAN_ID);
        // We back this up because it will often be mocked
        _tableCopyServiceBackup = _controller._tableCopyService;
    }

    protected void tearDown() throws Exception {
        _controller.setTableCopyService(_tableCopyServiceBackup);
    }

    public void testRequestToDevSucceeds() throws Exception {
        _sessionContext.setHostName("dev.greatschools.net");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected view for dev", _controller.getFormView(), modelAndView.getViewName());
    }

    public void testRequestToProductionFails() throws Exception {
        _sessionContext.setHostName("www.greatschools.net");
        ModelAndView modelAndView = _controller.handleRequest(_request, _response);
        assertEquals("Unexpected view for production", _controller._errorView, modelAndView.getViewName());
    }

    public void testValidation() {
        TableMoverCommand cmd = new TableMoverCommand();
        cmd.setTarget("dev");
        cmd.setMode("preview");

        cmd.setTablesets(new String[0]);
        cmd.setStates(new String[0]);
        BindException errors = new BindException(cmd, "");
        _controller.onBindAndValidate(_request, cmd, errors);
        assertEquals("There should be an error for missing table sets",
                1, errors.getAllErrors().size());

        cmd.setTablesets(new String[]{"test"});
        cmd.setStates(new String[0]);
        errors = new BindException(cmd, "");
        _controller.onBindAndValidate(_request, cmd, errors);
        assertEquals("There should be an error for missing state because no database prefix means state specific table",
                1, errors.getAllErrors().size());

        cmd.setTablesets(new String[]{"gs_schooldb.test"});
        cmd.setStates(new String[0]);
        errors = new BindException(cmd, "");
        _controller.onBindAndValidate(_request, cmd, errors);
        assertEquals("There should be no error because gs_schooldb.test doesn't require a state",
                0, errors.getAllErrors().size());

        cmd.setTablesets(new String[]{"school"});
        cmd.setStates(new String[]{"CA"});
        errors = new BindException(cmd, "");
        _controller.onBindAndValidate(_request, cmd, errors);
        assertEquals("Should not be an error because state and table sets provided",
                0, errors.getAllErrors().size());

        cmd.setTablesets(new String[]{"gs_schooldb.operator"});
        errors = new BindException(cmd, "");
        _controller.onBindAndValidate(_request, cmd, errors);
        assertEquals("Should be an error because gs_schooldb.operator is blocked by the blacklist",
                1, errors.getAllErrors().size());

        cmd.setTablesets(new String[]{"gs_schooldb.operator"});
        errors = new BindException(cmd, "");
        _controller.onBindAndValidate(_request, cmd, errors);
        assertEquals("Should be an error because gs_schooldb.operator is blocked by the blacklist",
                1, errors.getAllErrors().size());

        cmd.setTarget("staging");
        cmd.setTablesets(new String[]{"gs_schooldb.foo"});
        errors = new BindException(cmd, "");
        _controller.onBindAndValidate(_request, cmd, errors);
        assertEquals("Should be an error because gs_schooldb.operator is not on the whitelist",
                1, errors.getAllErrors().size());

        cmd.setMode("move");
        cmd.setTables(new String[]{"gs_schooldb.foo"});
        errors = new BindException(cmd, "");
        _controller.onBindAndValidate(_request, cmd, errors);
        assertEquals("Should not be any validation errors",
                0, errors.getAllErrors().size());

        cmd.setMode("move");
        cmd.setTables(new String[]{});
        errors = new BindException(cmd, "");
        _controller.onBindAndValidate(_request, cmd, errors);
        assertEquals("Should be an error because a list of tables are required when mode is to move tables",
                1, errors.getAllErrors().size());
    }

    public void testPreview() throws Exception {
        TableMoverCommand cmd = new TableMoverCommand();
        cmd.setTarget("staging");
        BindException errors = new BindException(cmd, "");

        // Override checkWikiForSelectedTables so the test doesn't actually go out to the wiki
        TableCopyServiceImpl tcs = new TableCopyServiceImpl() {
            public String checkWikiForSelectedTables(TableData.DatabaseDirection direction, List<String> selectedTables) throws IOException {
                return "SomeWarningText";
            }
        };
        _controller.setTableCopyService(tcs);

        // Test duplicate tables with the database specified
        cmd.setMode("preview");
        cmd.setTablesets(new String[]{"us_geo.city,us_geo.city", "us_geo.city"});
        cmd.setStates(new String[]{"CA"});
        _controller.onBindAndValidate(_request, cmd, errors);
        ModelAndView mv = _controller.onSubmit(_request, _response, cmd, errors);
        List<String> tables = Arrays.asList(cmd.getTables());
        assertNotNull(tables);
        assertEquals(1, tables.size());
        assertEquals("us_geo.city", tables.get(0));
        assertEquals("SomeWarningText", mv.getModel().get("warnings"));

        // Test state specific tables and dups
        cmd.setTablesets(new String[]{"school,us_geo.city", "us_geo.city,school"});
        cmd.setStates(new String[]{"WY", "OR"});
        _controller.onBindAndValidate(_request, cmd, errors);
        _controller.onSubmit(_request, _response, cmd, errors);
        tables = Arrays.asList(cmd.getTables());
        assertEquals(3, tables.size());
        assertTrue(tables.contains("us_geo.city"));
        assertTrue(tables.contains("_or.school"));
        assertTrue(tables.contains("_wy.school"));

        // Test no dups
        cmd.setTablesets(new String[]{"school", "us_geo.city"});
        cmd.setStates(new String[]{"WY", "OR"});
        _controller.onBindAndValidate(_request, cmd, errors);
        _controller.onSubmit(_request, _response, cmd, errors);
        tables = Arrays.asList(cmd.getTables());
        assertEquals(3, tables.size());
        assertTrue(tables.contains("us_geo.city"));
        assertTrue(tables.contains("_or.school"));
        assertTrue(tables.contains("_wy.school"));

        // Test all states
        cmd.setTablesets(new String[]{"school", "us_geo.city"});
        cmd.setStates(new String[]{"", "OR"});
        _controller.onBindAndValidate(_request, cmd, errors);
        _controller.onSubmit(_request, _response, cmd, errors);
        tables = Arrays.asList(cmd.getTables());
        assertEquals(52, tables.size());
        assertTrue(tables.contains("us_geo.city"));
        assertTrue(tables.contains("_or.school"));
        assertTrue(tables.contains("_wy.school"));

        // Test table filtering, foo is not whitelisted for dev to staging
        cmd.setTablesets(new String[]{"us_geo.city", "gs_schooldb.foo", "gs_schooldb.configuration"});
        cmd.setStates(new String[0]);
        _controller.onBindAndValidate(_request, cmd, errors);
        _controller.onSubmit(_request, _response, cmd, errors);
        tables = Arrays.asList(cmd.getTables());
        assertEquals(2, tables.size());
        assertTrue(tables.contains("us_geo.city"));
        assertTrue(tables.contains("gs_schooldb.configuration"));
        assertTrue(ArrayUtils.contains(cmd.getTablesFilteredOut(), "gs_schooldb.foo"));

        // Test table filtering, us_geo.city is blacklisted for live to dev
        cmd.setTarget("dev");
        cmd.setTablesets(new String[]{"us_geo.city", "_ca.test"});
        cmd.setStates(new String[0]);
        _controller.onBindAndValidate(_request, cmd, errors);
        _controller.onSubmit(_request, _response, cmd, errors);
        tables = Arrays.asList(cmd.getTables());
        assertEquals(1, tables.size());
        assertTrue(tables.contains("_ca.test"));
    }

    public void testMove() throws Exception {
        TableMoverCommand cmd = new TableMoverCommand();
        cmd.setTarget("staging");
        BindException errors = new BindException(cmd, "");
        cmd.setMode("move");
        cmd.setTables(new String[]{"us_geo.city", "gs_schooldb.test"});
        TableCopyServiceImpl tcs = createMock(TableCopyServiceImpl.class);
        expect(tcs.copyTables(cmd.getDirection(), cmd.getTables(), true, cmd.getInitials(), cmd.getJira(), cmd.getNotes())).andReturn("ABC");
        expect(tcs.copyTables(cmd.getDirection(), cmd.getTables(), true, cmd.getInitials(), cmd.getJira(), cmd.getNotes())).andThrow(new ServiceException("XYZ"));
        replay(tcs);
        _controller.setTableCopyService(tcs);

        // Check normal operation
        ModelAndView mv = _controller.onSubmit(_request, _response, cmd, errors);
        assertEquals("ABC", mv.getModel().get("wikiText"));

        // Check when an exception is thrown
        mv = _controller.onSubmit(_request, _response, cmd, errors);
        assertTrue(((String) mv.getModel().get("errors")).startsWith("XYZ"));
        verify(tcs);
    }

    public void testGetBasedFormSubmission() {
        assertFalse(_controller.isFormSubmission(_request));
        _request.addParameter("mode", "preview");
        assertTrue(_controller.isFormSubmission(_request));
    }

    public void testIsFormChangeRequest() {
        TableMoverCommand cmd = new TableMoverCommand();
        cmd.setMode("edit");
        assertTrue(_controller.isFormChangeRequest(_request, cmd));
        cmd.setMode("preview");
        assertFalse(_controller.isFormChangeRequest(_request, cmd));
    }

    public void testTableMoverCommand() {
        TableMoverCommand cmd = new TableMoverCommand();
        cmd.setInitials("TH");
        cmd.setJira("ABC");
        cmd.setNotes("123");
        cmd.setTarget("dev");
        assertEquals(TableData.PRODUCTION_TO_DEV, cmd.getDirection());
        cmd.setTarget("staging");
        assertEquals(TableData.DEV_TO_STAGING, cmd.getDirection());
        assertEquals("TH", cmd.getInitials());
        assertEquals("ABC", cmd.getJira());
        assertEquals("123", cmd.getNotes());
    }
}