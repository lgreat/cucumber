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
import java.util.regex.Matcher;

import gs.data.pledge.IPledgeDao;
import gs.data.pledge.Pledge;
import gs.data.state.State;
import gs.data.util.NameValuePair;
import gs.data.geo.IGeoDao;
import gs.data.geo.bestplaces.BpZip;
import gs.data.community.*;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.web.util.ReadWriteController;
import gs.web.school.review.AddParentReviewsController;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class PledgeAPIController  implements Controller, ReadWriteController {
    public static enum ContentType {
        XML("text/xml"),
        JSON("application/json");
        private final String _ctype;
        ContentType(String ctype) {
            _ctype = ctype;
        }
        public String getContentType() {
            return _ctype;
        }
    }

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
    public static final String SIGNUP_PARAM = "signup";
    public static final String PLEDGE_EMAIL_TRIGGER_KEY = "Great_Parents_Pledge_Welcome";
    public static final int MAX_PLEDGE_CHARS = 110;
    private ContentType _contentType = ContentType.XML;
    private String _function;
    private IPledgeDao _pledgeDao;
    private IGeoDao _geoDao;
    private IUserDao _userDao;
    private ISubscriptionDao _subscriptionDao;
    private ExactTargetAPI _exactTargetAPI;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(_contentType.getContentType());
        PrintWriter out = response.getWriter();
        try {
            IPledgeResponse pledgeResponse;
            // Call the right method and get the response object
            pledgeResponse = callFunction(request);
            // convert response object to the right content type
            out.print(pledgeResponse.toContentType(_contentType));
        } catch (PledgeAPIException e) {
            _log.error(e.toString());
            out.print(e.toContentType(_contentType));
        } catch (Exception e) {
            _log.error(e, e);
            out.print(new PledgeAPIException().toContentType(_contentType));
        }
        return null;
    }

    protected IPledgeResponse callFunction(HttpServletRequest request) throws PledgeAPIException {
        IPledgeResponse pledgeResponse;
        if (StringUtils.equalsIgnoreCase("get", request.getMethod())) {
            if (StringUtils.equals(GET_NUM_PLEDGES_FUNCTION, _function)) {
                pledgeResponse = getNumPledges();
            } else if (StringUtils.equals(GET_NUM_PLEDGES_BY_STATE_FUNCTION, _function)) {
                pledgeResponse = getNumPledgesByState();
            } else if (StringUtils.equals(GET_NUM_PLEDGES_BY_CITY_FUNCTION, _function)) {
                State state = State.fromString(request.getParameter(STATE_PARAM));
                pledgeResponse = getNumPledgesByCity(state);
            } else {
                throw new IllegalArgumentException("Unknown GET function: " + _function);
            }
        } else if (StringUtils.equalsIgnoreCase("post", request.getMethod())) {
            if (StringUtils.equals(SUBMIT_DEFAULT_PLEDGE_FUNCTION, _function)) {
                String zip = request.getParameter(ZIP_PARAM);
                pledgeResponse = submitDefaultPledge(zip);
            } else if (StringUtils.equals(SUBMIT_PERSONAL_PLEDGE_FUNCTION, _function)) {
                if (StringUtils.isBlank(request.getParameter(PLEDGE_ID_PARAM))
                        || !StringUtils.isNumeric(request.getParameter(PLEDGE_ID_PARAM))) {
                    throw new PledgeAPIException(PledgeAPIException.Code.PARAM_PLEDGE_ID, "Invalid pledge id.");
                }
                long pledgeId = new Long(request.getParameter(PLEDGE_ID_PARAM));
                String pledge = request.getParameter(PLEDGE_PARAM);
                String email = request.getParameter(EMAIL_PARAM);
                boolean signup = StringUtils.equals("true", request.getParameter(SIGNUP_PARAM));
                pledgeResponse = submitPersonalPledge(pledgeId, pledge, email, signup);
            } else {
                throw new IllegalArgumentException("Unknown POST function: " + _function);
            }
        } else {
            throw new IllegalArgumentException("Unknown request method: " + request.getMethod());
        }
        return pledgeResponse;
    }

    protected IPledgeResponse getNumPledges() {
        return new KeyValueResponse("total", String.valueOf(_pledgeDao.getNumPledges()));
    }

    protected IPledgeResponse getNumPledgesByState() {
        List<NameValuePair<State, Long>> results = _pledgeDao.getSortedNumPledgesByState();
        return new StatePledgesResponse(results);
    }

    protected IPledgeResponse getNumPledgesByCity(State state) {
        List<NameValuePair<String, Long>> results = _pledgeDao.getTop5CitiesByState(state);
        return new CityPledgesResponse(results);
    }

    protected IPledgeResponse submitDefaultPledge(String zip) throws PledgeAPIException {
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
        return new KeyValueResponse("id", String.valueOf(pledge.getId()));
    }

    protected IPledgeResponse submitPersonalPledge(long pledgeId, String pledgeStr, String email, boolean signup) throws PledgeAPIException {
        // validate email
        EmailValidator emv = EmailValidator.getInstance();
        if (!emv.isValid(email)) {
            _log.warn("Invalid email submitted:" + email);
            throw new PledgeAPIException(PledgeAPIException.Code.PARAM_EMAIL, "Please enter a valid email address.");
        }

        // validate pledge
        if (StringUtils.isNotBlank(pledgeStr) && hasBadWord(pledgeStr)) {
            throw new PledgeAPIException(PledgeAPIException.Code.PARAM_PLEDGE,
                    "Oops! No bad words allowed -- please try again.");
        }
        StringUtils.abbreviate(pledgeStr, MAX_PLEDGE_CHARS);

        // only store the user, subscription, and pledge if the user wants to sign up for updates
        if (signup) {
            // validate pledge id
            Pledge submittedPledge = _pledgeDao.getPledgeById(pledgeId);
            if (submittedPledge == null) {
                _log.error("No pledge by this id:" + pledgeId);
                throw new PledgeAPIException(PledgeAPIException.Code.PARAM_PLEDGE_ID, "Unknown pledge id.");
            }

            // check for pre-existing user
            boolean newUser = false;
            User user = _userDao.findUserFromEmailIfExists(email);
            if (user == null) {
                newUser = true;
                user = new User();
                user.setEmail(email);
                user.setHow("pledged");
                _userDao.saveUser(user);
            }

            // add pledge subscription
            if (newUser || !userHasPledgeSubscription(user)) {
                Subscription pledgeSub = new Subscription();
                pledgeSub.setUser(user);
                pledgeSub.setProduct(SubscriptionProduct.PLEDGE);
                pledgeSub.setState(submittedPledge.getState());

                List<Subscription> newSubs = new ArrayList<Subscription>(1);
                newSubs.add(pledgeSub);
                if (newUser) {
                    user.getSubscriptions(); // force lazy-load
                    // ensure list_member record is committed prior to list_active
                    ThreadLocalTransactionManager.commitOrRollback();
                }
                _subscriptionDao.addNewsletterSubscriptions(user, newSubs);

                // send triggered email
                _exactTargetAPI.sendTriggeredEmail(PLEDGE_EMAIL_TRIGGER_KEY, user);
            }

            Pledge pledgeToUpdate = submittedPledge;
            // check for an existing pledge by this user
            if (!newUser) {
                Pledge existingPledge = _pledgeDao.getPledgeByUser(user);
                if (existingPledge != null) {
                    // we found one ... update this one instead
                    pledgeToUpdate = existingPledge;
                }
            }

            pledgeToUpdate.setUser(user);
            pledgeToUpdate.setPledge(pledgeStr);
            _pledgeDao.savePledge(pledgeToUpdate);
        }
        return new EmptyPledgeResponse();
    }

    protected boolean hasBadWord(final String text) {
        Matcher m = AddParentReviewsController.BAD_WORDS.matcher(text);
        return m.matches();
    }

    protected boolean userHasPledgeSubscription(User user) {
        if (user.getSubscriptions() != null) {
            for (Subscription sub: user.getSubscriptions()) {
                if (SubscriptionProduct.PLEDGE.equals(sub.getProduct())) {
                    return true;
                }
            }
        }
        return false;
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

    public ExactTargetAPI getExactTargetAPI() {
        return _exactTargetAPI;
    }

    public void setExactTargetAPI(ExactTargetAPI exactTargetAPI) {
        _exactTargetAPI = exactTargetAPI;
    }

    public ContentType getContentType() {
        return _contentType;
    }

    public void setContentType(ContentType contentType) {
        _contentType = contentType;
    }

    protected static interface IPledgeResponse {
        public String toContentType(ContentType ct);
        public boolean equals(Object o); // for unit tests
    }

    protected static abstract class BasePledgeResponse implements IPledgeResponse {
        public String toContentType(ContentType ct) {
            if (ct == ContentType.XML) {
                return "<response>\n<error code=\"0\" message=\"No errors.\"/>\n" + toXML() + "\n</response>";
            } else {
                return toJSON();
            }
        }
        public abstract String toJSON();
        public abstract String toXML();
    }

    protected static class EmptyPledgeResponse extends BasePledgeResponse {
        public String toJSON() {
            return "{}";
        }
        public String toXML() {
            return "<success/>";
        }
        public boolean equals(Object o) {
            return o instanceof EmptyPledgeResponse;
        }
    }

    protected static class KeyValueResponse extends BasePledgeResponse {
        private String _key;
        private String _value;
        protected KeyValueResponse(String key, String value) {
            _key = key;
            _value = value;
        }
        public String toJSON() {
            return "{" + _key + ":" + _value + "}";
        }
        public String toXML() {
            return "<" + _key + ">" + _value + "</" + _key + ">";
        }
        public boolean equals(Object o) {
            if (!(o instanceof KeyValueResponse)) {
                return false;
            }
            KeyValueResponse other = (KeyValueResponse) o;
            return other._key.equals(_key) && other._value.equals(_value);
        }
    }

    protected static class StatePledgesResponse extends BasePledgeResponse {
        List<NameValuePair<State, Long>> _pledgesPerState;
        protected StatePledgesResponse(List<NameValuePair<State, Long>> pledgesPerState) {
            _pledgesPerState = pledgesPerState;
        }
        public String toJSON() {
            StringBuilder rval = new StringBuilder();
            rval.append("{pledgesByState:[");
            String[] stateResults = new String[_pledgesPerState.size()];
            int counter=0;
            for (NameValuePair<State, Long> stateResultPair: _pledgesPerState) {
                String jsonSegment = "{state:'" + stateResultPair.getKey().getLongName() + "',";
                jsonSegment += "pledges:" + stateResultPair.getValue() + "}";
                stateResults[counter++] = jsonSegment;
            }
            rval.append(StringUtils.join(stateResults, ","));
            rval.append("]}");
            return rval.toString();
        }
        public String toXML() {
            StringBuilder rval = new StringBuilder();
            rval.append("<pledgesByState>");
            for (NameValuePair<State, Long> stateResultPair: _pledgesPerState) {
                rval.append("\n  <stateValue ");
                rval.append("state=\"").append(stateResultPair.getKey().getLongName()).append("\" ");
                rval.append("pledges=\"").append(stateResultPair.getValue()).append("\"/>");
            }
            rval.append("\n</pledgesByState>");
            return rval.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StatePledgesResponse other = (StatePledgesResponse) o;

            if (_pledgesPerState != null) {
                if (other._pledgesPerState == null) {
                    return false;
                }
                if (_pledgesPerState.size() != other._pledgesPerState.size()) {
                    return false;
                }
                for (int i=0; i < _pledgesPerState.size(); i++) {
                    if (!_pledgesPerState.get(i).getKey().equals(other._pledgesPerState.get(i).getKey())) {
                        return false;
                    }
                    if (!_pledgesPerState.get(i).getValue().equals(other._pledgesPerState.get(i).getValue())) {
                        return false;
                    }
                }
                if (!_pledgesPerState.equals(other._pledgesPerState))
                    return false;
            } else if (other._pledgesPerState != null) {
                return false;
            }
            return true;
        }
    }

    protected static class CityPledgesResponse extends BasePledgeResponse {
        List<NameValuePair<String, Long>> _pledgesPerCity;
        protected CityPledgesResponse(List<NameValuePair<String, Long>> pledgesPerCity) {
            _pledgesPerCity = pledgesPerCity;
        }
        public String toJSON() {
            StringBuilder rval = new StringBuilder();
            rval.append("{pledgesByCity:[");
            String[] cityResults = new String[_pledgesPerCity.size()];
            int counter=0;
            for (NameValuePair<String, Long> stateResultPair: _pledgesPerCity) {
                String jsonSegment = "{city:'" + stateResultPair.getKey() + "',";
                jsonSegment += "pledges:" + stateResultPair.getValue() + "}";
                cityResults[counter++] = jsonSegment;
            }
            rval.append(StringUtils.join(cityResults, ","));
            rval.append("]}");
            return rval.toString();
        }
        public String toXML() {
            StringBuilder rval = new StringBuilder();
            rval.append("<pledgesByCity>");
            for (NameValuePair<String, Long> cityResultPair: _pledgesPerCity) {
                rval.append("\n  <cityValue ");
                rval.append("city=\"").append(cityResultPair.getKey()).append("\" ");
                rval.append("pledges=\"").append(cityResultPair.getValue()).append("\"/>");
            }
            rval.append("\n</pledgesByCity>");
            return rval.toString();
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CityPledgesResponse other = (CityPledgesResponse) o;

            if (_pledgesPerCity != null) {
                if (other._pledgesPerCity == null) {
                    return false;
                }
                if (_pledgesPerCity.size() != other._pledgesPerCity.size()) {
                    return false;
                }
                for (int i=0; i < _pledgesPerCity.size(); i++) {
                    if (!_pledgesPerCity.get(i).getKey().equals(other._pledgesPerCity.get(i).getKey())) {
                        return false;
                    }
                    if (!_pledgesPerCity.get(i).getValue().equals(other._pledgesPerCity.get(i).getValue())) {
                        return false;
                    }
                }
                if (!_pledgesPerCity.equals(other._pledgesPerCity))
                    return false;
            } else if (other._pledgesPerCity != null) {
                return false;
            }
            return true;
        }
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

        public String toContentType(ContentType ct) {
            if (ct == ContentType.XML) {
                return "<response>\n<error code=\"" + _code + "\" message=\"" + _message + "\"/>\n</response>";
            } else {
                return "{error:{code:" + _code + ",message:'" + _message + "'}}";
            }
        }
        public String toString() {
            return "PledgeAPIException #" + _code + ": " + _message;
        }
        public Code getCode() {
            return _code;
        }
    }
}