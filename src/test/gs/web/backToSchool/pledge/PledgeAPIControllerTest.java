package gs.web.backToSchool.pledge;

import gs.web.BaseControllerTestCase;
import gs.data.pledge.IPledgeDao;
import gs.data.pledge.Pledge;
import gs.data.util.NameValuePair;
import gs.data.state.State;
import gs.data.geo.IGeoDao;
import gs.data.geo.bestplaces.BpZip;
import gs.data.community.*;

import static org.easymock.EasyMock.*;
import org.easymock.IArgumentMatcher;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class PledgeAPIControllerTest extends BaseControllerTestCase {
    private PledgeAPIController _controller;
    private IPledgeDao _pledgeDao;
    private IGeoDao _geoDao;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;

    public void setUp() throws Exception {
        super.setUp();

        _controller = new PledgeAPIController();
        _pledgeDao = createStrictMock(IPledgeDao.class);
        _geoDao = createStrictMock(IGeoDao.class);
        _userDao = createStrictMock(IUserDao.class);
        _subscriptionDao = createStrictMock(ISubscriptionDao.class);

        _controller.setPledgeDao(_pledgeDao);
        _controller.setGeoDao(_geoDao);
        _controller.setUserDao(_userDao);
        _controller.setSubscriptionDao(_subscriptionDao);
        _controller.setContentType(PledgeAPIController.ContentType.XML);

        // default method
        getRequest().setMethod("GET");
    }

    public void replayMocks() {
        replayMocks(_pledgeDao, _geoDao, _userDao, _subscriptionDao);
    }

    public void verifyMocks() {
        verifyMocks(_pledgeDao, _geoDao, _userDao, _subscriptionDao);
    }

    public void testBasics() {
        assertSame(_pledgeDao, _controller.getPledgeDao());
        assertSame(_geoDao, _controller.getGeoDao());
        assertSame(_userDao, _controller.getUserDao());
        assertSame(_subscriptionDao, _controller.getSubscriptionDao());
        _controller.setFunction("function");
        assertEquals("function", _controller.getFunction());
        assertEquals(PledgeAPIController.ContentType.XML, _controller.getContentType());
    }

    public void testUnknownFunction() throws Exception {
        _controller.setFunction("foo");
        getRequest().setMethod("GET");
        try {
            _controller.callFunction(getRequest());
            fail("Didn't receive expected IllegalArgumentException with function \"foo\"");
        } catch (IllegalArgumentException iae) {
            // ok
        }
        getRequest().setMethod("POST");
        try {
            _controller.callFunction(getRequest());
            fail("Didn't receive expected IllegalArgumentException with function \"foo\"");
        } catch (IllegalArgumentException iae) {
            // ok
        }
    }

    public void testUnknownRequestMethod() throws PledgeAPIController.PledgeAPIException {
        getRequest().setMethod("PUT");
        try {
            _controller.callFunction(getRequest());
            fail("Didn't receive expected IllegalArgumentException with request method \"PUT\"");
        } catch (IllegalArgumentException iae) {
            // ok
        }
    }

    /*
     * TEST GET NUM PLEDGES
     */

    public void testGetNumPledges() throws Exception {
        expect(_pledgeDao.getNumPledges()).andReturn(180l);
        replayMocks();
        PledgeAPIController.IPledgeResponse rval = _controller.getNumPledges();
        verifyMocks();
        PledgeAPIController.KeyValueResponse expected = new PledgeAPIController.KeyValueResponse("total", "180");
        assertEquals(expected, rval);
    }

    public void testGetNumPledgesFromTop() throws Exception {
        _controller.setFunction(PledgeAPIController.GET_NUM_PLEDGES_FUNCTION);
        expect(_pledgeDao.getNumPledges()).andReturn(180l);
        replayMocks();
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks();
        assertEquals("<response>\n<error code=\"0\" message=\"No errors.\"/>\n<total>180</total>\n</response>",
                getResponse().getContentAsString());
    }

    public void testErrorOnGetNumPledges() throws Exception {
        expect(_pledgeDao.getNumPledges()).andThrow(new RuntimeException("Sample error for unit tests"));
        replayMocks();
        _controller.setFunction(PledgeAPIController.GET_NUM_PLEDGES_FUNCTION);
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks();
        assertEquals(new PledgeAPIController.PledgeAPIException().toContentType(_controller.getContentType()),
                getResponse().getContentAsString());
    }

    /*
     * TEST GET NUM PLEDGES BY STATE
     */

    public void testGetNumPledgesByState() throws Exception {
        List<NameValuePair<State,Long>> list = new ArrayList<NameValuePair<State, Long>>();
        list.add(new NameValuePair<State, Long>(State.CA, 55000l));
        list.add(new NameValuePair<State, Long>(State.MD, 12000l));
        expect(_pledgeDao.getSortedNumPledgesByState()).andReturn(list);
        replayMocks();
        PledgeAPIController.IPledgeResponse rval = _controller.getNumPledgesByState();
        verifyMocks();
        assertEquals(new PledgeAPIController.StatePledgesResponse(list), rval);
    }

    public void testGetNumPledgesByStateFromTop() throws Exception {
        _controller.setFunction(PledgeAPIController.GET_NUM_PLEDGES_BY_STATE_FUNCTION);
        List<NameValuePair<State,Long>> list = new ArrayList<NameValuePair<State, Long>>();
        list.add(new NameValuePair<State, Long>(State.CA, 55000l));
        list.add(new NameValuePair<State, Long>(State.MD, 12000l));
        expect(_pledgeDao.getSortedNumPledgesByState()).andReturn(list);
        replayMocks();
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks();
        assertEquals("<response>\n<error code=\"0\" message=\"No errors.\"/>\n" +
                "<pledgesByState>" +
                "\n  <stateValue state=\"California\" pledges=\"55000\"/>" +
                "\n  <stateValue state=\"Maryland\" pledges=\"12000\"/>" +
                "\n</pledgesByState>" +
                "\n</response>",
                getResponse().getContentAsString());
    }

    public void testGetNumPledgesByStateOneResult() throws Exception {
        List<NameValuePair<State,Long>> list = new ArrayList<NameValuePair<State, Long>>();
        list.add(new NameValuePair<State, Long>(State.CA, 55000l));
        expect(_pledgeDao.getSortedNumPledgesByState()).andReturn(list);
        replayMocks();
        PledgeAPIController.IPledgeResponse rval = _controller.getNumPledgesByState();
        verifyMocks();
        assertEquals(new PledgeAPIController.StatePledgesResponse(list), rval);
    }

    public void testGetNumPledgesByStateNoResults() throws Exception {
        List<NameValuePair<State,Long>> list = new ArrayList<NameValuePair<State, Long>>();
        expect(_pledgeDao.getSortedNumPledgesByState()).andReturn(list);
        replayMocks();
        PledgeAPIController.IPledgeResponse rval = _controller.getNumPledgesByState();
        verifyMocks();
        assertEquals(new PledgeAPIController.StatePledgesResponse(list), rval);
    }
    
    public void testErrorOnGetNumPledgesByState() throws Exception {
        expect(_pledgeDao.getSortedNumPledgesByState()).andThrow(new RuntimeException("Sample error for unit tests"));
        replayMocks();
        _controller.setFunction(PledgeAPIController.GET_NUM_PLEDGES_BY_STATE_FUNCTION);
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks();
        assertEquals(new PledgeAPIController.PledgeAPIException().toContentType(_controller.getContentType()),
                getResponse().getContentAsString());
    }

    /*
     * TEST GET NUM PLEDGES BY CITY
     */

    public void testGetNumPledgesByCity() throws Exception {
        List<NameValuePair<String,Long>> list = new ArrayList<NameValuePair<String, Long>>();
        list.add(new NameValuePair<String, Long>("Alameda", 1500l));
        list.add(new NameValuePair<String, Long>("Oakland", 1000l));
        expect(_pledgeDao.getTop5CitiesByState(State.CA)).andReturn(list);
        replayMocks();
        PledgeAPIController.IPledgeResponse rval = _controller.getNumPledgesByCity(State.CA);
        verifyMocks();
        assertEquals(new PledgeAPIController.CityPledgesResponse(list), rval);
    }

    public void testGetNumPledgesByCityFromTop() throws Exception {
        _controller.setFunction(PledgeAPIController.GET_NUM_PLEDGES_BY_CITY_FUNCTION);
        getRequest().setParameter(PledgeAPIController.STATE_PARAM, "California");
        List<NameValuePair<String,Long>> list = new ArrayList<NameValuePair<String, Long>>();
        list.add(new NameValuePair<String, Long>("Alameda", 1500l));
        list.add(new NameValuePair<String, Long>("San Francisco", 1000l));
        expect(_pledgeDao.getTop5CitiesByState(State.CA)).andReturn(list);
        replayMocks();
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks();
        assertEquals("<response>\n<error code=\"0\" message=\"No errors.\"/>\n" +
                "<pledgesByCity>" +
                "\n  <cityValue city=\"Alameda\" pledges=\"1500\"/>" +
                "\n  <cityValue city=\"San Francisco\" pledges=\"1000\"/>" +
                "\n</pledgesByCity>" +
                "\n</response>",
                getResponse().getContentAsString());

    }

    public void testGetNumPledgesByCityOneResult() throws Exception {
        List<NameValuePair<String,Long>> list = new ArrayList<NameValuePair<String, Long>>();
        list.add(new NameValuePair<String, Long>("Alameda", 1500l));
        expect(_pledgeDao.getTop5CitiesByState(State.CA)).andReturn(list);
        replayMocks();
        PledgeAPIController.IPledgeResponse rval = _controller.getNumPledgesByCity(State.CA);
        verifyMocks();
        assertEquals(new PledgeAPIController.CityPledgesResponse(list), rval);
    }

    public void testGetNumPledgesByCityNoResults() throws Exception {
        List<NameValuePair<String,Long>> list = new ArrayList<NameValuePair<String, Long>>();
        expect(_pledgeDao.getTop5CitiesByState(State.CA)).andReturn(list);
        replayMocks();
        PledgeAPIController.IPledgeResponse rval = _controller.getNumPledgesByCity(State.CA);
        verifyMocks();
        assertEquals(new PledgeAPIController.CityPledgesResponse(list), rval);
    }

    public void testErrorOnGetNumPledgesByCity() throws Exception {
        expect(_pledgeDao.getTop5CitiesByState(State.CA)).andThrow(new RuntimeException("Sample error for unit tests"));
        replayMocks();
        _controller.setFunction(PledgeAPIController.GET_NUM_PLEDGES_BY_CITY_FUNCTION);
        getRequest().setParameter("state", "California");
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks();
        assertEquals(new PledgeAPIController.PledgeAPIException().toContentType(_controller.getContentType()),
                getResponse().getContentAsString());
    }

    /*
     * TEST SUBMIT DEFAULT PLEDGE
     */

    public void testSubmitDefaultPledge() throws Exception {
        BpZip bpzip = new BpZip();
        bpzip.setZip("92130");
        bpzip.setName("San Diego");
        bpzip.setState(State.CA);
        expect(_geoDao.findZip("92130")).andReturn(bpzip);

        Pledge expectedPledge = new Pledge();
        expectedPledge.setZip("92130");
        expectedPledge.setCity("San Diego");
        expectedPledge.setState(State.CA);
        _pledgeDao.savePledge(eqPledge(expectedPledge));

        replayMocks();
        PledgeAPIController.IPledgeResponse rval = _controller.submitDefaultPledge("92130");
        verifyMocks();
        assertEquals(new PledgeAPIController.KeyValueResponse("id", "1234"), rval);
    }

    public void testSubmitDefaultPledgeFromTop() throws Exception {
        getRequest().setMethod("POST");
        _controller.setFunction(PledgeAPIController.SUBMIT_DEFAULT_PLEDGE_FUNCTION);
        getRequest().setParameter(PledgeAPIController.ZIP_PARAM, "92130");

        BpZip bpzip = new BpZip();
        bpzip.setZip("92130");
        bpzip.setName("San Diego");
        bpzip.setState(State.CA);
        expect(_geoDao.findZip("92130")).andReturn(bpzip);

        Pledge expectedPledge = new Pledge();
        expectedPledge.setZip("92130");
        expectedPledge.setCity("San Diego");
        expectedPledge.setState(State.CA);
        _pledgeDao.savePledge(eqPledge(expectedPledge));

        replayMocks();
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks();
        assertEquals("<response>\n<error code=\"0\" message=\"No errors.\"/>\n<id>1234</id>\n</response>",
                getResponse().getContentAsString());
    }

    public void testSubmitDefaultPledgeInvalidZip() throws Exception {
        getRequest().setMethod("POST");
        _controller.setFunction(PledgeAPIController.SUBMIT_DEFAULT_PLEDGE_FUNCTION);
        getRequest().setParameter(PledgeAPIController.ZIP_PARAM, "92130");

        expect(_geoDao.findZip("92130")).andReturn(null);

        replayMocks();
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks();
    }

    /*
     * TEST SUBMIT PERSONAL PLEDGE
     */

    public void testSubmitPersonalPledgeNoPledge() throws Exception {
        expect(_pledgeDao.getPledgeById(15l)).andReturn(null);

        replayMocks();
        try {
            _controller.submitPersonalPledge(15l, "Pledge", "email@example.com", true);
            fail("Expected exception not received");
        } catch (PledgeAPIController.PledgeAPIException e) {
            assertEquals(PledgeAPIController.PledgeAPIException.Code.PARAM_PLEDGE_ID, e.getCode());
            // ok
        }
        verifyMocks();
    }

    public void testSubmitPersonalPledgeNoSignup() throws Exception {
        replayMocks();
        PledgeAPIController.IPledgeResponse rval =
                _controller.submitPersonalPledge(15l, "Pledge", "email@example.com", false);
        verifyMocks();
        assertEquals(new PledgeAPIController.EmptyPledgeResponse(), rval);
    }

    public void testSubmitPersonalPledgeFromTopBadId() {
        _controller.setFunction(PledgeAPIController.SUBMIT_PERSONAL_PLEDGE_FUNCTION);
        getRequest().setMethod("POST");
        try {
            _controller.callFunction(getRequest());
            fail("Didn't receive expected IllegalArgumentException with no pledge id");
        } catch (PledgeAPIController.PledgeAPIException pe) {
            // ok
        }

        getRequest().setParameter(PledgeAPIController.PLEDGE_ID_PARAM, "notANumber");
        try {
            _controller.callFunction(getRequest());
            fail("Didn't receive expected IllegalArgumentException with non-numeric pledge id");
        } catch (PledgeAPIController.PledgeAPIException pe) {
            // ok
        }
    }

    public void testSubmitPersonalPledgeFromTopNew() throws Exception {
        _controller.setFunction(PledgeAPIController.SUBMIT_PERSONAL_PLEDGE_FUNCTION);
        getRequest().setMethod("POST");
        getRequest().setParameter(PledgeAPIController.PLEDGE_ID_PARAM, "1234");
        getRequest().setParameter(PledgeAPIController.PLEDGE_PARAM, "I pledge to read to my kids");
        getRequest().setParameter(PledgeAPIController.EMAIL_PARAM, "email@example.com");
        getRequest().setParameter(PledgeAPIController.SIGNUP_PARAM, "true");

        Pledge pledge = new Pledge();
        pledge.setId(1234l);
        pledge.setZip("92130");
        pledge.setCity("San Diego");
        pledge.setState(State.CA);
        // gets pledge
        expect(_pledgeDao.getPledgeById(1234)).andReturn(pledge);
        // checks for user
        expect(_userDao.findUserFromEmailIfExists("email@example.com")).andReturn(null);
        // saves user and subscription
        _userDao.saveUser(isA(User.class));
        _subscriptionDao.addNewsletterSubscriptions(isA(User.class), isA(List.class));
        // updates pledge
        Pledge expectedPledge = new Pledge();
        expectedPledge.setId(1234l);
        expectedPledge.setZip("92130");
        expectedPledge.setCity("San Diego");
        expectedPledge.setState(State.CA);
        expectedPledge.setPledge("I pledge to read to my kids");
        expectedPledge.setUser(new User());
        _pledgeDao.savePledge(eqPledge(expectedPledge));

        replayMocks();
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks();
        assertEquals(new PledgeAPIController.EmptyPledgeResponse().toContentType(_controller.getContentType()),
                getResponse().getContentAsString());
    }

    public void testHasBadWord() {
        // sanity check
        assertTrue(_controller.hasBadWord("Overreacters should be fucking shot"));
    }

    public void testHasPledgeSubscription() {
        User userWith = new User();
        userWith.setEmail("email@example.com");
        Subscription pledge = new Subscription(userWith, SubscriptionProduct.PLEDGE, (State)null);
        Set<Subscription> subs = new HashSet<Subscription>(1);
        subs.add(pledge);
        userWith.setSubscriptions(subs);

        User userWithout = new User();
        userWithout.setEmail("email@example.com");
        Subscription other = new Subscription(userWithout, SubscriptionProduct.PARENT_ADVISOR, (State)null);
        subs = new HashSet<Subscription>(1);
        subs.add(other);
        userWithout.setSubscriptions(subs);
        User userWithNone = new User();

        assertTrue(_controller.userHasPledgeSubscription(userWith));
        assertFalse(_controller.userHasPledgeSubscription(userWithout));
        assertFalse(_controller.userHasPledgeSubscription(userWithNone));
    }

    public Pledge eqPledge(Pledge pledge) {
        reportMatcher(new PledgeMatcher(pledge));
        return null;
    }

    private class PledgeMatcher implements IArgumentMatcher {
        Pledge _expected;
        PledgeMatcher(Pledge expected) {
            _expected = expected;
        }
        public boolean matches(Object oActual) {
            if (!(oActual instanceof Pledge)) {
                return false;
            }
            Pledge actual = (Pledge) oActual;
            if (actual.getId() == null) {actual.setId(1234);} // this mimics the save call in the dao
            if (StringUtils.equals(actual.getZip(), _expected.getZip())
                    && StringUtils.equals(actual.getCity(), _expected.getCity())
                    && actual.getState().equals(_expected.getState())
                    && StringUtils.equals(actual.getPledge(), _expected.getPledge())) {
                if ((_expected.getUser() == null && actual.getUser() == null)
                    || (_expected.getUser() != null && actual.getUser() != null)) {
                    return true;
                }
            }
            return false;
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("zip:").append(_expected.getZip())
                    .append(", city:").append(_expected.getCity())
                    .append(", state:").append(_expected.getState())
                    .append(", pledge:").append(_expected.getPledge())
                    .append(", user:").append(_expected.getUser()==null?"null":_expected.getUser().getId());
        }
    }

}
