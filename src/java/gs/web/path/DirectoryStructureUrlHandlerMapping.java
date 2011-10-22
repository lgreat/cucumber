package gs.web.path;

import org.springframework.beans.BeansException;

import java.util.Map;
import java.util.HashMap;

import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.url.DirectoryStructureUrlFactory;
import gs.web.jsp.Util;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

/**
 * @author <a href="mailto:yfan@greatschools.org">Young Fan</a>
 */
public class DirectoryStructureUrlHandlerMapping extends SimpleUrlHandlerMapping {

    private static Map<String,Object> _map = new HashMap<String,Object>();

    static {
        for (State state : StateManager.getList()) {
            String stateNameForUrl = DirectoryStructureUrlFactory.getStateNameForUrl(state);
            String pattern = "/" + stateNameForUrl + "/**";
            _map.put(pattern, "/directoryStructureUrlRequest.page");
            pattern = "/" + Util.capitalize(stateNameForUrl) + "/**";
            _map.put(pattern, "/directoryStructureUrlRequest.page");
        }
        _map.put("/district-of-columbia/**", "/directoryStructureUrlRequest.page");
        _map.put("/District-of-columbia/**", "/directoryStructureUrlRequest.page");
    }

    public void setUrlMap(Map urlMap) {
        super.setUrlMap(_map);
    }

    public Map getUrlMap() {
        return _map;
    }

    protected void registerHandlers(Map<String,Object> urlMap)
                         throws BeansException {
        super.registerHandlers(_map);
    }
}
