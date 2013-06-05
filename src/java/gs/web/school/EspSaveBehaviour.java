package gs.web.school;

public class EspSaveBehaviour {
    private boolean _isSaveProvisional = false;
    private boolean _isActivateProvisionalData = false;
    private boolean _isApprovedUserFormSave = false;
    private boolean _isIgnoreErrors = false;

    public EspSaveBehaviour(boolean saveProvisional, boolean isIgnoreErrors, boolean isActivateProvisionalData,
                            boolean isApprovedUserFormSave) {
        _isSaveProvisional = saveProvisional;
        _isIgnoreErrors = isIgnoreErrors;
        _isActivateProvisionalData = isActivateProvisionalData;
        _isApprovedUserFormSave = isApprovedUserFormSave;
    }

    public boolean isSaveProvisional() {
        return _isSaveProvisional;
    }

    public boolean isIgnoreErrors() {
        return _isIgnoreErrors;
    }

    public boolean isActivateProvisionalData() {
        return _isActivateProvisionalData;
    }

    public boolean isApprovedUserFormSave() {
        return _isApprovedUserFormSave;
    }

}
