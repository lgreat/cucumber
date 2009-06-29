package gs.web.backToSchool.pledge;

import gs.web.BaseControllerTestCase;
import gs.data.pledge.IPledgeDao;
import gs.data.pledge.Pledge;
import gs.data.util.NameValuePair;
import gs.data.state.State;
import gs.data.geo.IGeoDao;
import gs.data.geo.bestplaces.BpZip;

import static org.easymock.EasyMock.*;
import org.easymock.IArgumentMatcher;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class PledgeAPIControllerTest extends BaseControllerTestCase {
    private PledgeAPIController _controller;
    private IPledgeDao _pledgeDao;
    private IGeoDao _geoDao;

    public void setUp() throws Exception {
        super.setUp();

        _controller = new PledgeAPIController();
        _pledgeDao = createStrictMock(IPledgeDao.class);
        _geoDao = createStrictMock(IGeoDao.class);

        _controller.setPledgeDao(_pledgeDao);
        _controller.setGeoDao(_geoDao);

        // default method
        getRequest().setMethod("GET");
    }

    public void replayMocks() {
        replayMocks(_pledgeDao, _geoDao);
    }

    public void verifyMocks() {
        verifyMocks(_pledgeDao, _geoDao);
    }

    public void testBasics() {
        assertSame(_pledgeDao, _controller.getPledgeDao());
        assertSame(_geoDao, _controller.getGeoDao());
        _controller.setFunction("function");
        assertEquals("function", _controller.getFunction());
    }

    /*
     * TEST GET NUM PLEDGES
     */

    public void testGetNumPledges() throws Exception {
        expect(_pledgeDao.getNumPledges()).andReturn(180l);
        replayMocks();
        _controller.getNumPledges(getResponse().getWriter());
        verifyMocks();
        assertEquals("{total:180}", getResponse().getContentAsString());
    }

    public void testGetNumPledgesFromTop() throws Exception {
        _controller.setFunction(PledgeAPIController.GET_NUM_PLEDGES_FUNCTION);
        expect(_pledgeDao.getNumPledges()).andReturn(180l);
        replayMocks();
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks();
        assertEquals("{total:180}", getResponse().getContentAsString());
    }

    public void testErrorOnGetNumPledges() throws Exception {
        expect(_pledgeDao.getNumPledges()).andThrow(new RuntimeException("Sample error for unit tests"));
        replayMocks();
        _controller.setFunction(PledgeAPIController.GET_NUM_PLEDGES_FUNCTION);
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks();
        assertEquals("{error:{code:1,message:'Service not available.'}}", getResponse().getContentAsString());
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
        _controller.getNumPledgesByState(getResponse().getWriter());
        verifyMocks();
        assertEquals("{pledgesByState:[{state:'California',pledges:55000},{state:'Maryland',pledges:12000}]}", 
                getResponse().getContentAsString());
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
        assertEquals("{pledgesByState:[{state:'California',pledges:55000},{state:'Maryland',pledges:12000}]}",
                getResponse().getContentAsString());
    }

    public void testGetNumPledgesByStateOneResult() throws Exception {
        List<NameValuePair<State,Long>> list = new ArrayList<NameValuePair<State, Long>>();
        list.add(new NameValuePair<State, Long>(State.CA, 55000l));
        expect(_pledgeDao.getSortedNumPledgesByState()).andReturn(list);
        replayMocks();
        _controller.getNumPledgesByState(getResponse().getWriter());
        verifyMocks();
        assertEquals("{pledgesByState:[{state:'California',pledges:55000}]}",
                getResponse().getContentAsString());
    }

    public void testGetNumPledgesByStateNoResults() throws Exception {
        List<NameValuePair<State,Long>> list = new ArrayList<NameValuePair<State, Long>>();
        expect(_pledgeDao.getSortedNumPledgesByState()).andReturn(list);
        replayMocks();
        _controller.getNumPledgesByState(getResponse().getWriter());
        verifyMocks();
        assertEquals("Expect empty array", "{pledgesByState:[]}",
                getResponse().getContentAsString());
    }
    
    public void testErrorOnGetNumPledgesByState() throws Exception {
        expect(_pledgeDao.getSortedNumPledgesByState()).andThrow(new RuntimeException("Sample error for unit tests"));
        replayMocks();
        _controller.setFunction(PledgeAPIController.GET_NUM_PLEDGES_BY_STATE_FUNCTION);
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks();
        assertEquals("{error:{code:1,message:'Service not available.'}}", getResponse().getContentAsString());
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
        _controller.getNumPledgesByCity(State.CA, getResponse().getWriter());
        verifyMocks();
        assertEquals("{pledgesByCity:[{city:'Alameda',pledges:1500},{city:'Oakland',pledges:1000}]}",
                getResponse().getContentAsString());
    }

    public void testGetNumPledgesByCityFromTop() throws Exception {
        _controller.setFunction(PledgeAPIController.GET_NUM_PLEDGES_BY_CITY_FUNCTION);
        getRequest().setParameter(PledgeAPIController.STATE_PARAM, "California");
        List<NameValuePair<String,Long>> list = new ArrayList<NameValuePair<String, Long>>();
        list.add(new NameValuePair<String, Long>("Alameda", 1500l));
        list.add(new NameValuePair<String, Long>("Oakland", 1000l));
        expect(_pledgeDao.getTop5CitiesByState(State.CA)).andReturn(list);
        replayMocks();
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks();
        assertEquals("{pledgesByCity:[{city:'Alameda',pledges:1500},{city:'Oakland',pledges:1000}]}",
                getResponse().getContentAsString());
    }

    public void testGetNumPledgesByCityOneResult() throws Exception {
        List<NameValuePair<String,Long>> list = new ArrayList<NameValuePair<String, Long>>();
        list.add(new NameValuePair<String, Long>("Alameda", 1500l));
        expect(_pledgeDao.getTop5CitiesByState(State.CA)).andReturn(list);
        replayMocks();
        _controller.getNumPledgesByCity(State.CA, getResponse().getWriter());
        verifyMocks();
        assertEquals("{pledgesByCity:[{city:'Alameda',pledges:1500}]}",
                getResponse().getContentAsString());
    }

    public void testGetNumPledgesByCityNoResults() throws Exception {
        List<NameValuePair<String,Long>> list = new ArrayList<NameValuePair<String, Long>>();
        expect(_pledgeDao.getTop5CitiesByState(State.CA)).andReturn(list);
        replayMocks();
        _controller.getNumPledgesByCity(State.CA, getResponse().getWriter());
        verifyMocks();
        assertEquals("Expect empty array", "{pledgesByCity:[]}",
                getResponse().getContentAsString());
    }

    public void testErrorOnGetNumPledgesByCity() throws Exception {
        expect(_pledgeDao.getTop5CitiesByState(State.CA)).andThrow(new RuntimeException("Sample error for unit tests"));
        replayMocks();
        _controller.setFunction(PledgeAPIController.GET_NUM_PLEDGES_BY_CITY_FUNCTION);
        getRequest().setParameter("state", "California");
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks();
        assertEquals("{error:{code:1,message:'Service not available.'}}", getResponse().getContentAsString());
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
        _controller.submitDefaultPledge("92130", getResponse().getWriter());
        verifyMocks();
        assertEquals("{id:1234}", getResponse().getContentAsString());
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
        assertEquals("{id:1234}", getResponse().getContentAsString());
    }

    public void testSubmitDefaultPledgeInvalidZip() throws Exception {
        getRequest().setMethod("POST");
        _controller.setFunction(PledgeAPIController.SUBMIT_DEFAULT_PLEDGE_FUNCTION);
        getRequest().setParameter(PledgeAPIController.ZIP_PARAM, "92130");

        expect(_geoDao.findZip("92130")).andReturn(null);

        replayMocks();
        _controller.handleRequest(getRequest(), getResponse());
        verifyMocks();
        assertEquals("{error:{code:2,message:'Invalid zip code.'}}", getResponse().getContentAsString());
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
            actual.setId(1234); // this mimics the save call in the dao
            if (StringUtils.equals(actual.getZip(), _expected.getZip())
                    && StringUtils.equals(actual.getCity(), _expected.getCity())
                    && actual.getState().equals(_expected.getState())
                    && StringUtils.equals(actual.getPledge(), _expected.getPledge())) {
                if ((_expected.getUser() == null && actual.getUser() == null)
                    || _expected.getUser().getId().equals(actual.getUser().getId())) {
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
