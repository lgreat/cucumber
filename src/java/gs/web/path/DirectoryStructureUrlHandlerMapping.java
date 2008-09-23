package gs.web.path;

import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.beans.BeansException;

import java.util.Map;
import java.util.HashMap;

import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.util.SpringUtil;
import gs.web.jsp.Util;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: Jul 24, 2008
 * Time: 1:08:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryStructureUrlHandlerMapping extends SimpleUrlHandlerMapping {

    private static Map<String,String> _map = new HashMap<String,String>();

    static {
        for (State state : StateManager.getList()) {
            String stateNameForUrl = state.getLongName().toLowerCase().replaceAll(" ", "-");
            String pattern = "/" + stateNameForUrl + "/**";
            _map.put(pattern, "/directoryStructureUrlRequest.page");
            pattern = "/" + Util.capitalize(stateNameForUrl) + "/**";
            _map.put(pattern, "/directoryStructureUrlRequest.page");
        }
    }

    public void setUrlMap(Map urlMap) {
        super.setUrlMap(_map);
    }

    public Map getUrlMap() {
        return _map;
    }

    protected void registerHandlers(Map urlMap)
                         throws BeansException {
        super.registerHandlers(_map);
    }
}
