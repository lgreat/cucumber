package gs.web.tracking;

import gs.web.util.context.SubCookie;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA.
 * User: jnorton
 * Date: Feb 3, 2009
 * Time: 11:44:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class CookieBasedOmnitureTracking extends OmnitureTracking{
    protected final static Log _log = LogFactory.getLog(OmnitureTracking.class);


    protected HttpServletRequest _request;
    protected HttpServletResponse _response;

    protected SubCookie _subCookie;

    public CookieBasedOmnitureTracking(HttpServletRequest request, HttpServletResponse response){
        _request = request;
        _response = response;
        _subCookie = new SubCookie(request, response);
        _events = _subCookie.getProperty("events");
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

    @Override
    public void addProp(Prop prop) {
        _subCookie.setProperty("prop" + prop.getNumber(), prop.toOmnitureString());
    }

    /**
     * For unit tests.
     */
    protected SubCookie getSubCookie() {
        return _subCookie;
    }

}
