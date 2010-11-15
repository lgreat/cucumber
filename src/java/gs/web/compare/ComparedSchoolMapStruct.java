package gs.web.compare;

import gs.data.geo.ILocation;
import gs.data.geo.LatLon;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ComparedSchoolMapStruct extends ComparedSchoolBaseStruct implements ILocation {
    private boolean _selected = false;

    public boolean isSelected() {
        return _selected;
    }

    public void setSelected(boolean selected) {
        _selected = selected;
    }

    /* Convenience methods */

    public String getPhone() {
        return getSchool().getPhone();
    }

    public LatLon getLatLon() {
        return getSchool().getLatLon();
    }


}
