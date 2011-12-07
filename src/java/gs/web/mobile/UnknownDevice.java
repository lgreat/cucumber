package gs.web.mobile;

import net.sourceforge.wurfl.core.*;

import java.util.HashMap;
import java.util.Map;

public class UnknownDevice implements net.sourceforge.wurfl.core.Device {
    public String getId() {
        return "UNKNOWN";
    }

    public String getUserAgent() {
        return "UNKNOWN";
    }

    public String getCapability(String name) throws CapabilityNotDefinedException {
        return null;
    }

    public Map getCapabilities() {
        return new HashMap<String,String>();
    }

    public MarkUp getMarkUp() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getDeviceRootId() {
        return null;
    }

    public boolean isActualDeviceRoot() {
        return false;
    }
}
