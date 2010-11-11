package gs.web.compare;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ComparedSchoolMapStruct extends ComparedSchoolBaseStruct {
    private boolean _selected = false;

    public boolean isSelected() {
        return _selected;
    }

    public void setSelected(boolean selected) {
        _selected = selected;
    }
}
