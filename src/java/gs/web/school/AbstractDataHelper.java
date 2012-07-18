package gs.web.school;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/** Abstract Data Helper for use by all data helpers.  The main purpose of this helper is to create a map on the
 *  request object to keep references to data that will be reused across a request.  The map attached to the request
 *  attribute needs to be created early in the processing cycle otherwise the attribute is lost.
 *
 * @author Bob Raker  <mailto:rraker@greatschools.org>
 */
public abstract class AbstractDataHelper {

    /** Used for access to a shared map stored in the request.  Contents of that map are pointers to shared data */
    public static final String SHARED_STATE_KEY = "SharedState";

    protected static void initialize( HttpServletRequest request ) {

        // Set up the shared state map in the request if not present.  Entries in this shared state map are data that is to be reused during a request
        Map<String, Map<String, Object>> sharedState = (Map<String, Map<String, Object>>) request.getAttribute(SHARED_STATE_KEY);
        if( sharedState == null ) {
            sharedState =  new HashMap<String, Map<String, Object>>();
            request.setAttribute( SHARED_STATE_KEY, sharedState );
        }

    }

    /**
     * Helper to access data stored in a common object in the request.  That shared object needs to get created early
     * because request attributes created later are lost.
     * @param request
     * @param key The key to the object in the shared data map
     * @return the requested object or null if it does not exist
     */
    protected Object getSharedData( HttpServletRequest request, String key ) {
        Object data = null;
        Map<String, Map<String, Object>> sharedDataMap = (Map<String, Map<String, Object>>) request.getAttribute( SHARED_STATE_KEY );
        if( sharedDataMap != null ) {
//        if( sharedDataMap == null ) {
//            throw new IllegalArgumentException( "The request must already contain the shared data map named " + SHARED_STATE_KEY  +
//                    " Make sure AbstractDataHelper.initialize() was called early in the request cycle.");
//        }
            // The shared state map consists of an entry for each data helper.  Get that map
            Map<String, Object> dataHelperMap = sharedDataMap.get(this.getClass().getName());

            if( dataHelperMap != null ) {
                data = dataHelperMap.get(key);
            }
        }
        return data;
    }

    protected void setSharedData( HttpServletRequest request, String key, Object data ) {
        Map<String, Map<String, Object>> sharedDataMap = (Map<String, Map<String, Object>>) request.getAttribute( SHARED_STATE_KEY );
        if( sharedDataMap == null ) {
//            throw new IllegalArgumentException( "The request must already contain the shared data map named " + SHARED_STATE_KEY  +
//                " Make sure AbstractDataHelper.initialize() was called early in the request cycle.");
            sharedDataMap = new HashMap<String, Map<String, Object>>();
            request.setAttribute( SHARED_STATE_KEY, sharedDataMap );
        }
        // The shared state map consists of an entry for each data helper.  Get that map
        Map<String, Object> dataHelperMap = sharedDataMap.get(this.getClass().getName());
        if( dataHelperMap == null ) {
            dataHelperMap = new HashMap<String, Object>();
            sharedDataMap.put(this.getClass().getName(), dataHelperMap);
        }

        dataHelperMap.put(key, data);
    }

}
