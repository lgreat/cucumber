package gs.web.school;

import gs.data.school.IKindercareLeadGenDao;
import gs.data.school.ISchoolDao;
import gs.data.school.KindercareLeadGen;
import gs.data.school.School;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.easymock.IArgumentMatcher;

import java.io.IOException;
import java.util.Date;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class KindercareLeadGenAjaxControllerTest extends BaseControllerTestCase {
    private KindercareLeadGenAjaxController _controller;

    private ISchoolDao _schoolDao;
    private IKindercareLeadGenDao _kindercareLeadGenDao;
    private HttpClient _client;

    public void setUp() throws Exception {
        super.setUp();

        _controller = new KindercareLeadGenAjaxController();

        _schoolDao = createStrictMock(ISchoolDao.class);
        _kindercareLeadGenDao = createStrictMock(IKindercareLeadGenDao.class);
        _client = createStrictMock(HttpClient.class);

        _controller.setSchoolDao(_schoolDao);
        _controller.setKindercareLeadGenDao(_kindercareLeadGenDao);
        _controller.setHttpClient(_client);

        getRequest().setServerName("www.greatschools.org");
    }

    private void replayAllMocks() {
        replayMocks(_schoolDao, _kindercareLeadGenDao, _client);
    }

    private void verifyAllMocks() {
        verifyMocks(_schoolDao, _kindercareLeadGenDao, _client);
    }

    public void testBasics() {
        assertSame(_schoolDao, _controller.getSchoolDao());
        assertSame(_kindercareLeadGenDao, _controller.getKindercareLeadGenDao());
        assertSame(_client, _controller.getHttpClient());
    }

    protected KindercareLeadGenCommand getCommand(String firstName, String lastName, String email,
                                                  boolean informed, boolean offers) {
        KindercareLeadGenCommand command = new KindercareLeadGenCommand();
        command.setFirstName(firstName);
        command.setLastName(lastName);
        command.setEmail(email);
        command.setInformed(informed);
        command.setOffers(offers);

        command.setSchoolId(1);
        command.setState(State.CA);

        return command;
    }

    public void testValidate() {
        School school = new School();
        school.setVendorId("vendorId");
        School schoolNoVendorId = new School();
  
        assertFalse("Expect no school to cause validation error",
                    _controller.validate(getCommand("firstName", "lastName", "email@greatschools.org", false, false), null));
        assertFalse("Expect school with no vendor ID to cause validation error",
                    _controller.validate(getCommand("firstName", "lastName", "email@greatschools.org", false, false), schoolNoVendorId));
        assertTrue("Expect reasonable command to pass validation",
                   _controller.validate(getCommand("firstName", "lastName", "email@greatschools.org", false, false), school));
        assertTrue("Expect reasonable command to pass validation",
                   _controller.validate(getCommand(null,null,null, false, false), school));

    }

    public void testValidateSOAPRequest() {
        School school = new School();
        school.setVendorId("venderId");
        School schoolNoVendorId = new School();
        assertFalse("Expect no first name to cause validation error",
                    _controller.validateSubmitRequest(getCommand("", "lastName", "email@greatschools.org", false, false), school));
        assertFalse("Expect no first name to cause validation error",
                    _controller.validateSubmitRequest(getCommand(null, "lastName", "email@greatschools.org", true, false), school));
        assertFalse("Expect no last name to cause validation error",
                    _controller.validateSubmitRequest(getCommand("firstName", "", "email@greatschools.org", false, true), school));
        assertFalse("Expect no last name to cause validation error",
                    _controller.validateSubmitRequest(getCommand("firstName", null, "email@greatschools.org", true, true), school));
        assertFalse("Expect no email to cause validation error",
                    _controller.validateSubmitRequest(getCommand("firstName", "lastName", "", false, false), school));
        assertFalse("Expect no email to cause validation error",
                    _controller.validateSubmitRequest(getCommand("firstName", "lastName", null, false, false), school));
        assertFalse("Expect invalid email to cause validation error",
                    _controller.validateSubmitRequest(getCommand("firstName", "lastName", "noneOfYourBusiness", false, false), school));
        assertFalse("Expect no school to cause validation error",
                    _controller.validateSubmitRequest(getCommand("firstName", "lastName", "email@greatschools.org", false, false), null));
        assertFalse("Expect school with no vendor ID to cause validation error",
                    _controller.validateSubmitRequest(getCommand("firstName", "lastName", "email@greatschools.org", false, false), schoolNoVendorId));
        assertTrue("Expect reasonable command to pass validation",
                   _controller.validateSubmitRequest(getCommand("firstName", "lastName", "email@greatschools.org", false, false), school));
    }

    public void testSuccess() throws Exception {
        KindercareLeadGenCommand command = getCommand("firstName", "lastName", "email@greatschools.org", true, false);

        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);
        school.setVendorId("1234");
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school);

        _kindercareLeadGenDao.save(eqKindercareLeadGen(new KindercareLeadGen(1, State.CA, new Date(), "firstName", 
                                                                             "lastName", "email@greatschools.org",
                                                                             true, false, "1234")));

        expect(_client.executeMethod(isA(PostMethod.class))).andReturn(200);

        replayAllMocks();
        _controller.generateLead(command, getRequest(), getResponse());
        verifyAllMocks();
    }

    public void testSuccessOnClose() throws Exception {
        KindercareLeadGenCommand command = getCommand(null,null,null, true, false);

        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);
        school.setVendorId("1234");
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school);

        _kindercareLeadGenDao.save(eqKindercareLeadGen(new KindercareLeadGen(1, State.CA, new Date(), null,
                                                                            null,null,
                                                                             true, false, "1234")));

        replayAllMocks();
        _controller.generateLead(command, getRequest(), getResponse());
        verifyAllMocks();
    }

    public void testSuccessOnLiveSite() throws Exception {
        getRequest().setServerName("www.greatschools.org");

        KindercareLeadGenCommand command = getCommand("firstName", "lastName", "email@greatschools.org", true, false);

        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);
        school.setVendorId("1234");
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school);

        _kindercareLeadGenDao.save(eqKindercareLeadGen(new KindercareLeadGen(1, State.CA, new Date(), "firstName",
                                                                             "lastName", "email@greatschools.org",
                                                                             true, false, "1234")));

        expect(_client.executeMethod(isA(PostMethod.class))).andReturn(200);

        replayAllMocks();
        _controller.generateLead(command, getRequest(), getResponse());
        verifyAllMocks();
    }

    public void testFailureOnValidationAbortsLeadGen() throws Exception {
        KindercareLeadGenCommand command = getCommand("", "lastName", "email@greatschools.org", true, false);

        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);
        school.setVendorId("1234");
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school);

        _kindercareLeadGenDao.save(eqKindercareLeadGen(new KindercareLeadGen(1, State.CA, new Date(), "",
                                                                             "lastName", "email@greatschools.org",
                                                                             true, false, "1234")));

        replayAllMocks();
        _controller.generateLead(command, getRequest(), getResponse());
        verifyAllMocks();
    }

    public void testFailureOnSubmitStillLogs() throws Exception {
        KindercareLeadGenCommand command = getCommand("firstName", "lastName", "email@greatschools.org", true, false);

        School school = new School();
        school.setId(1);
        school.setDatabaseState(State.CA);
        school.setVendorId("1234");
        expect(_schoolDao.getSchoolById(State.CA, 1)).andReturn(school);

        _kindercareLeadGenDao.save(eqKindercareLeadGen(new KindercareLeadGen(1, State.CA, new Date(), "firstName",
                                                                             "lastName", "email@greatschools.org",
                                                                             true, false, "1234")));

        _client.executeMethod(isA(PostMethod.class));
        expectLastCall().andThrow(new IOException("Testing failure"));

        replayAllMocks();
        _controller.generateLead(command, getRequest(), getResponse());
        verifyAllMocks();
    }

    // DO NOT UNCOMMENT - execute this code only if you know what you are doing
//    public void testLiveSubmit() throws IOException {
//        getRequest().setServerName("www.greatschools.org");
//        _controller.setHttpClient(null);
//        replayAllMocks();
//        _controller.submitKindercareLeadGen(getRequest(), "Anthony", "Roy", "aroy@greatschools.org",
//                                            "1234", true, false);
//        verifyAllMocks();
//    }

    public KindercareLeadGen eqKindercareLeadGen(KindercareLeadGen in) {
        reportMatcher(new KindercareLeadGenEquals(in));
        return null;
    }

    private class KindercareLeadGenEquals implements IArgumentMatcher {
        private KindercareLeadGen _expected;
        
        public KindercareLeadGenEquals(KindercareLeadGen leadGen) {
            _expected = leadGen;
        }

        public boolean matches(Object o) {
            KindercareLeadGen actual = (KindercareLeadGen) o;
            if (!StringUtils.equals(_expected.getFirstName(), actual.getFirstName())) {
                return false;
            }
            if (!StringUtils.equals(_expected.getLastName(), actual.getLastName())) {
                return false;
            }
            if (!StringUtils.equals(_expected.getEmail(), actual.getEmail())) {
                return false;
            }
            if (_expected.isKinderCareOptIn() != actual.isKinderCareOptIn()) {
                return false;
            }
            if (_expected.isKinderCarePartnersOptIn() != actual.isKinderCarePartnersOptIn()) {
                return false;
            }
            if (!StringUtils.equals(_expected.getCenterId(), actual.getCenterId())) {
                return false;
            }
            if (!_expected.getSchoolId().equals(actual.getSchoolId())) {
                return false;
            }
            if (_expected.getState() != actual.getState()) {
                return false;
            }
            return true;
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("eqKindercareLeadGen()");
        }
    }
}
 