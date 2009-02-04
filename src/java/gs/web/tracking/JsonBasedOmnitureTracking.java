package gs.web.tracking;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.NullArgumentException;

import java.util.Set;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: jnorton
 * Date: Feb 3, 2009
 * Time: 11:45:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class JsonBasedOmnitureTracking extends OmnitureTracking{
    protected final static Log _log = LogFactory.getLog(JsonBasedOmnitureTracking.class);

    private Set<SuccessEvent> _successEvents;
    private Set<Evar> _eVars;

    public JsonBasedOmnitureTracking(){
        this._successEvents = new HashSet<SuccessEvent>();
        this._eVars = new HashSet<Evar>();
    }

    /**
     * Add an evar to be tracked. This will overwrite any existing value for that evar.
     */
    public void addEvar(Evar evar){
        addObjectToSet(evar, this._eVars);
    }

    /**
     * Add a success event . This will be added in addition to any other existing
     * success events.
     */
    public void addSuccessEvent(SuccessEvent successEvent){
        addObjectToSet(successEvent, this._successEvents);
    }

    public String toJsonObject(){
        StringBuilder sb = new StringBuilder();

        sb.append("{");
        // events
        sb.append("\"successEvents\":");
        sb.append(eventsToJson());
        sb.append(",");
        // eVars
        sb.append("\"eVars\":");
        sb.append(eVarsToJson());

        sb.append("}");
        return sb.toString();
    }

    protected String eVarsToJson(){
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        boolean first = true;
        for (Evar evar : _eVars){
            if (!first){
                sb.append(",");
            }
            first = false;

            sb.append("\"");
            sb.append("eVar");
            sb.append(evar.getNumber()) ;
            sb.append("\"");
            sb.append(":");
            sb.append("\"");
            sb.append(evar.toOmnitureString());
            sb.append("\"");
        }
        sb.append("}");

        return sb.toString();
    }
    protected String eventsToJson(){
        StringBuilder sb = new StringBuilder();
        sb.append("\"");

        for (SuccessEvent se : _successEvents){
            sb.append(se.toOmnitureString());
        }
        sb.append("\"");

        return sb.toString();
    }

    protected static void addObjectToSet(Object o, Set s){
        if (o == null){
            _log.warn("Trying to add a null object to the set");
            throw new NullArgumentException ("Argument object cannot be null");
        }
        s.add(o);
    }
}
