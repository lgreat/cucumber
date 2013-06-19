package gs.web.school;

public class OspSaveBehaviour extends EspSaveBehaviour {
    //Used for OSP
    private boolean _activateProvisionalData = false;
    private boolean _approvedUserFormSave = false;

    public OspSaveBehaviour(boolean saveProvisional, boolean activateProvisionalData,
                            boolean approvedUserFormSave) {
        setSaveProvisional(saveProvisional);
        _activateProvisionalData = activateProvisionalData;
        _approvedUserFormSave = approvedUserFormSave;
    }

    public boolean isActivateProvisionalData() {
        return _activateProvisionalData;
    }

    public boolean isApprovedUserFormSave() {
        return _approvedUserFormSave;
    }
}