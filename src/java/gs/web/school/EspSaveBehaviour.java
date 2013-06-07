package gs.web.school;

import gs.data.school.EspResponseSource;

public class EspSaveBehaviour {
    //Used for OSP
    private boolean _isActivateProvisionalData = false;
    private boolean _isApprovedUserFormSave = false;
    //Used for USP
    private boolean _isUserEmailVerified = false;
    private EspResponseSource _espResponseSource;
    //Used for both USP and OSP
    private boolean _isSaveProvisional = false;

    public EspSaveBehaviour(boolean saveProvisional, boolean isActivateProvisionalData,
                            boolean isApprovedUserFormSave) {
        _isSaveProvisional = saveProvisional;
        _isActivateProvisionalData = isActivateProvisionalData;
        _isApprovedUserFormSave = isApprovedUserFormSave;
    }

    public EspSaveBehaviour(boolean isUserEmailVerified, EspResponseSource espResponseSource,
                            boolean isSaveProvisional) {

        _isUserEmailVerified = isUserEmailVerified;
        _isSaveProvisional = isSaveProvisional;
        _espResponseSource = espResponseSource;
    }

    public boolean isSaveProvisional() {
        return _isSaveProvisional;
    }

    public boolean isActivateProvisionalData() {
        return _isActivateProvisionalData;
    }

    public boolean isApprovedUserFormSave() {
        return _isApprovedUserFormSave;
    }

    public boolean isUserEmailVerified() {
        return _isUserEmailVerified;
    }

    public void setIsUserEmailVerified(boolean isUserEmailVerified) {
        _isUserEmailVerified = isUserEmailVerified;
    }

    public EspResponseSource getEspResponseSource() {
        return _espResponseSource;
    }

    public void setEspResponseSource(EspResponseSource espResponseSource) {
        _espResponseSource = espResponseSource;
    }
}
