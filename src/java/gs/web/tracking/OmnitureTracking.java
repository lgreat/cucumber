package gs.web.tracking;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.web.util.context.SubCookie;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class OmnitureTracking {
    protected final static Log _log = LogFactory.getLog(OmnitureTracking.class);

    /**
     * Objects that know how to represent themselves to omniture
     */
    protected static interface OmnitureInformation {
        public String toOmnitureString();
    }

    /**
     * An Omniture eVar, consisting of an EvarNumber and a value. For example, eVar 7 may be set to "foo".
     */
    public static class Evar implements OmnitureInformation {
        private EvarNumber _evarNumber;
        private String _value;

        public Evar(EvarNumber evarNumber, String value) {
            if (evarNumber == null) {
                throw new IllegalArgumentException("EvarNumber must not be null");
            }
            _evarNumber = evarNumber;
            _value = value;
        }

        public String toOmnitureString() {
            return _value;
        }

        public EvarNumber getOmnitureEvar() {
            return _evarNumber;
        }

        public int getNumber() {
            return _evarNumber.getNumber();
        }

        public String toString() {
            return "eVar" + _evarNumber.getNumber() + "=" + _value ;
        }
    }

    /**
     * Typesafe eVar number definitions.
     */
    public enum EvarNumber {
        CrossPromotion(5),
        RegistrationSegment(7);

        private int _number;

        EvarNumber(int num) {
            _number = num;
        }

        public int getNumber() {
            return _number;
        }
    }

    /**
     * Typesafe SuccessEvent definitions.
     */
    public enum SuccessEvent implements OmnitureInformation {
        CommunityRegistration(6),
        ArticleView(7),
        ParentRating(8),
        ParentReview(9),
        ParentSurvey(10),
        NewNewsLetterSubscriber(11),
        ChoicePackRequest(18);

        private int _eventNumber;
        SuccessEvent(int eventNumber){
            _eventNumber = eventNumber;
        }

        public String toOmnitureString() {
            return "event" + _eventNumber + ";";
        }

        protected int getEventNumber(){
            return _eventNumber;
        }
    }

    protected HttpServletRequest _request;
    protected HttpServletResponse _response;
    protected String _events = "";
    protected SubCookie _subCookie;

    public OmnitureTracking(HttpServletRequest request, HttpServletResponse response){
        _request = request;
        _response = response;
        _subCookie = new SubCookie(request, response);
        _events = (String) _subCookie.getProperty("events");
        _events = _events == null ? "" : _events;

        _log.info("events: " + _events);
        // clean up residual???
        _subCookie.setProperty("events", _events);
    }

    /**
     * Add an evar to the cookie. This will overwrite any existing value for that evar.
     */
    public void addEvar(Evar evar) {
        _log.info("addEvar(" + evar + ")");
        if (evar == null) {
            throw new IllegalArgumentException("Evar must not be null");
        }
        if (_subCookie.getProperty("eVar" + evar.getNumber()) != null) {
            _log.warn("Overwriting existing evar value \"" + _subCookie.getProperty("eVar" + evar.getNumber()) +
                    "\" with new value \"" + evar.toOmnitureString() + "\"");
        }
        _subCookie.setProperty("eVar" + evar.getNumber(), evar.toOmnitureString());
    }

    /**
     * Add a success event to the cookie. This will be added in addition to any other existing
     * success events already set on the cookie.
     */
    public void addSuccessEvent(SuccessEvent successEvent){
        _log.info("addSuccessEvent(SuccessEvent." + successEvent + ", '" + _events + "')");
        _events = addOmnitureInformationToString(successEvent, _events);
        _subCookie.setProperty("events", _events);
    }

    /**
     * Null-safe method that appends info.toOmnitureString() to destination.
     */
    protected static String addOmnitureInformationToString(OmnitureInformation info, String destination) {
        if (info == null && destination == null){
            return "";
        } else if (info == null){
            return destination;
        } else if (destination == null){
            return info.toOmnitureString();
        }

        if (destination.contains(info.toOmnitureString())){
            return destination;
        } else {
            return destination + info.toOmnitureString();
        }
    }

    /**
     * For unit tests.
     */
    protected SubCookie getSubCookie() {
        return _subCookie;
    }
}
