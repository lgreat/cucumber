package gs.web.mobile;

import net.sourceforge.wurfl.core.CapabilityNotDefinedException;
import net.sourceforge.wurfl.core.MarkUp;

import java.util.Map;

public class Device {

    private Boolean _mobileDevice;

    private net.sourceforge.wurfl.core.Device _device;

    public Device(net.sourceforge.wurfl.core.Device device) {
        if (device == null) {
            throw new IllegalArgumentException("WURFL device cannot be null");
        }
        _device = device;
    }

    public boolean isIphone() {
        return _device.getUserAgent().contains("iPhone");
    }

    public boolean isMobileDevice() {
        if (_device == null) {
            return false;
        }

        if (_mobileDevice == null) {
            String capability = _device.getCapability("is_wireless_device");
            _mobileDevice = (capability != null && capability.length() > 0 && Boolean.valueOf(capability));
        }

        return _mobileDevice;
    }

    public Integer getResolutionHeight() {
        if (_device == null) {
            return null;
        }

        Integer height = Integer.valueOf(_device.getCapability("resolution_height"));
        return height;
    }

    public Integer getResolutionWidth() {
        if (_device == null) {
            return null;
        }

        Integer width = Integer.valueOf(_device.getCapability("resolution_width"));
        return width;
    }

    public String getId() {
        return _device.getId();
    }

    public String getUserAgent() {
        return _device.getUserAgent();
    }

    public String getCapability(String name) throws CapabilityNotDefinedException {
        return _device.getCapability(name);
    }

    public Map getCapabilities() {
        return _device.getCapabilities();
    }

    public MarkUp getMarkUp() {
        return _device.getMarkUp();
    }
}