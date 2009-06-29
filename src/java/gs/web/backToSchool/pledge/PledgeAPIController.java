package gs.web.backToSchool.pledge;

import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.EmailValidator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

import gs.data.pledge.IPledgeDao;
import gs.data.pledge.Pledge;
import gs.data.state.State;
import gs.data.util.NameValuePair;
import gs.data.geo.IGeoDao;
import gs.data.geo.bestplaces.BpZip;
import gs.data.community.*;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.web.util.ReadWriteController;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class PledgeAPIController  implements Controller, ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String GET_NUM_PLEDGES_FUNCTION = "getNumPledges";
    public static final String GET_NUM_PLEDGES_BY_STATE_FUNCTION = "getNumPledgesByState";
    public static final String GET_NUM_PLEDGES_BY_CITY_FUNCTION = "getNumPledgesByCity";
    public static final String SUBMIT_DEFAULT_PLEDGE_FUNCTION = "submitDefaultPledge";
    public static final String SUBMIT_PERSONAL_PLEDGE_FUNCTION = "submitPersonalPledge";
    public static final String STATE_PARAM = "state";
    public static final String ZIP_PARAM = "zip";
    public static final String PLEDGE_ID_PARAM = "pledge_id";
    public static final String PLEDGE_PARAM = "pledge";
    public static final String EMAIL_PARAM = "email";
    private String _function;
    private IPledgeDao _pledgeDao;
    private IGeoDao _geoDao;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json");
        _log.error(request.getMethod() + "; " + _function);
        PrintWriter out = response.getWriter();
        try {
            if (StringUtils.equalsIgnoreCase("get", request.getMethod())) {
                if (StringUtils.equals(GET_NUM_PLEDGES_FUNCTION, _function)) {
                    getNumPledges(out);
                } else if (StringUtils.equals(GET_NUM_PLEDGES_BY_STATE_FUNCTION, _function)) {
                    getNumPledgesByState(out);
                } else if (StringUtils.equals(GET_NUM_PLEDGES_BY_CITY_FUNCTION, _function)) {
                    State state = State.fromString(request.getParameter(STATE_PARAM));
                    getNumPledgesByCity(state, out);
                } else {
                    throw new IllegalArgumentException("Unknown function: " + _function);
                }
            } else if (StringUtils.equalsIgnoreCase("post", request.getMethod())) {
                if (StringUtils.equals(SUBMIT_DEFAULT_PLEDGE_FUNCTION, _function)) {
                    String zip = request.getParameter(ZIP_PARAM);
                    submitDefaultPledge(zip, out);
                } else if (StringUtils.equals(SUBMIT_PERSONAL_PLEDGE_FUNCTION, _function)) {
                    if (StringUtils.isBlank(request.getParameter(PLEDGE_ID_PARAM))
                            || !StringUtils.isNumeric(request.getParameter(PLEDGE_ID_PARAM))) {
                        throw new PledgeAPIException(PledgeAPIException.Code.PARAM_PLEDGE_ID, "Invalid pledge id.");
                    }
                    long pledgeId = new Long(request.getParameter(PLEDGE_ID_PARAM));
                    String pledge = request.getParameter(PLEDGE_PARAM);
                    String email = request.getParameter(EMAIL_PARAM);
                    submitPersonalPledge(pledgeId, pledge, email, out);
                } else {
                    throw new IllegalArgumentException("Unknown function: " + _function);
                }
            } else {
                throw new IllegalArgumentException("Unknown request method: " + request.getMethod());
            }
        } catch (PledgeAPIException e) {
            _log.error(e.toString());
            out.print(e.toJSON());
        } catch (Exception e) {
            _log.error(e, e);
            out.print(new PledgeAPIException().toJSON());
        }
        return null;
    }

    protected void submitPersonalPledge(long pledgeId, String pledgeStr, String email, PrintWriter out) throws PledgeAPIException {
        // validate pledge id
        Pledge submittedPledge = _pledgeDao.getPledgeById(pledgeId);
        if (submittedPledge == null) {
            _log.error("No pledge by this id:" + pledgeId);
            throw new PledgeAPIException(PledgeAPIException.Code.PARAM_PLEDGE_ID, "Unknown pledge id.");
        }

        // validate email
        EmailValidator emv = EmailValidator.getInstance();
        if (!emv.isValid(email)) {
            _log.warn("Invalid email submitted:" + email);
            throw new PledgeAPIException(PledgeAPIException.Code.PARAM_EMAIL, "Please enter a valid email address.");
        }

        // validate pledge
        // TODO: bad word filter

        // check for pre-existing user
        boolean newUser = false;
        User user = _userDao.findUserFromEmailIfExists(email);
        if (user == null) {
            _log.error("New user");
            newUser = true;
            user = new User();
            user.setEmail(email);
            user.setHow("pledge");
            _userDao.saveUser(user);
        }

        // add pledge subscription
        if (newUser || !userHasPledgeSubscription(user)) {
            _log.error("Need to add subscription");
            Subscription pledgeSub = new Subscription();
            pledgeSub.setUser(user);
            pledgeSub.setProduct(SubscriptionProduct.PLEDGE);
            pledgeSub.setState(submittedPledge.getState());

            List<Subscription> newSubs = new ArrayList<Subscription>(1);
            newSubs.add(pledgeSub);
            // ensure list_member record is committed prior to list_active
            ThreadLocalTransactionManager.commitOrRollback();
            _subscriptionDao.addNewsletterSubscriptions(user, newSubs);
        }

        Pledge pledgeToUpdate = submittedPledge;
        // check for an existing pledge by this user
        if (!newUser) {
            _log.error("Checking for existing pledge...");
            Pledge existingPledge = _pledgeDao.getPledgeByUser(user);
            if (existingPledge != null) {
                _log.error("Found it! id:" + existingPledge.getId());
                // we found one ... update this one instead
                pledgeToUpdate = existingPledge;
            }
        }

        pledgeToUpdate.setUser(user);
        pledgeToUpdate.setPledge(pledgeStr);
        _pledgeDao.savePledge(pledgeToUpdate);

        out.print("{}");
    }

    protected boolean userHasPledgeSubscription(User user) {
        _log.error("Checking for pledge sub...");
        for (Subscription sub: user.getSubscriptions()) {
            if (SubscriptionProduct.PLEDGE.equals(sub.getProduct())) {
                _log.error("Found it! " + sub);
                return true;
            }
        }
        return false;
    }

    protected void getNumPledges(PrintWriter out) {
        long total = _pledgeDao.getNumPledges();
        out.print("{total:" + String.valueOf(total) + "}");
    }

    protected void getNumPledgesByState(PrintWriter out) {
        List<NameValuePair<State, Long>> results = _pledgeDao.getSortedNumPledgesByState();
        StringBuilder rval = new StringBuilder();
        rval.append("{pledgesByState:[");
        String[] stateResults = new String[results.size()];
        int counter=0;
        for (NameValuePair<State, Long> stateResultPair: results) {
            String jsonSegment = "{state:'" + stateResultPair.getKey().getLongName() + "',";
            jsonSegment += "pledges:" + stateResultPair.getValue() + "}";
            stateResults[counter++] = jsonSegment;
        }
        rval.append(StringUtils.join(stateResults, ","));
        rval.append("]}");
        out.print(rval.toString());
    }

    protected void getNumPledgesByCity(State state, PrintWriter out) {
        List<NameValuePair<String, Long>> results = _pledgeDao.getTop5CitiesByState(state);
        StringBuilder rval = new StringBuilder();
        rval.append("{pledgesByCity:[");
        String[] stateResults = new String[results.size()];
        int counter=0;
        for (NameValuePair<String, Long> cityResultPair: results) {
            String jsonSegment = "{city:'" + cityResultPair.getKey() + "',";
            jsonSegment += "pledges:" + cityResultPair.getValue() + "}";
            stateResults[counter++] = jsonSegment;
        }
        rval.append(StringUtils.join(stateResults, ","));
        rval.append("]}");
        out.print(rval.toString());
    }

    protected void submitDefaultPledge(String zip, PrintWriter out) throws PledgeAPIException {
        BpZip bpzip = _geoDao.findZip(zip);
        if (bpzip == null) {
            throw new PledgeAPIException(PledgeAPIException.Code.PARAM_ZIP, "Invalid zip code.");
        }

        Pledge pledge = new Pledge();
        pledge.setZip(zip);
        pledge.setCity(bpzip.getName());
        pledge.setState(bpzip.getState());

        _pledgeDao.savePledge(pledge);

        if (pledge.getId() == null) {
            _log.error("No id from saved pledge...");
            throw new PledgeAPIException();
        }
        out.print("{id:" + pledge.getId() + "}");
    }

    public String getFunction() {
        return _function;
    }

    public void setFunction(String function) {
        _function = function;
    }

    public IPledgeDao getPledgeDao() {
        return _pledgeDao;
    }

    public void setPledgeDao(IPledgeDao pledgeDao) {
        _pledgeDao = pledgeDao;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public ISubscriptionDao getSubscriptionDao() {
        return _subscriptionDao;
    }

    public void setSubscriptionDao(ISubscriptionDao subscriptionDao) {
        _subscriptionDao = subscriptionDao;
    }

    protected static class PledgeAPIException extends Exception {
        enum Code {
            UNKNOWN(1),
            PARAM_ZIP(2),
            PARAM_EMAIL(3),
            PARAM_PLEDGE(4),
            PARAM_PLEDGE_ID(5),
            PARAM_STATE(6);
            private final int _code;
            Code(int code) {
                _code = code;
            }
            public String toString() {return String.valueOf(_code);}
        }
        private Code _code;
        private String _message;

        protected PledgeAPIException() {
            _code = Code.UNKNOWN;
            _message = "Service not available.";
        }
        
        protected PledgeAPIException(Code code, String msg) {
            _code = code;
            _message = msg;
        }

        public String toJSON() {
            return "{error:{code:" + _code + ",message:'" + _message + "'}}";
        }

        public String toString() {
            return "PledgeAPIException #" + _code + ": " + _message;
        }
    }
}
