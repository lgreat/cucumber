package gs.web.school;

public class UspSaveBehaviour extends EspSaveBehaviour {
    //Used for USP
    private boolean _userEmailVerified = false;
    private boolean _ospGatewayFormSave = false;

    public UspSaveBehaviour(boolean isUserEmailVerified, boolean isOspGatewayFormSave,
                            boolean isSaveProvisional) {
        _userEmailVerified = isUserEmailVerified;
        _ospGatewayFormSave = isOspGatewayFormSave;
        setSaveProvisional(isSaveProvisional);
    }

    public boolean isUserEmailVerified() {
        return _userEmailVerified;
    }

    public boolean isOspGatewayFormSave() {
        return _ospGatewayFormSave;
    }

}
