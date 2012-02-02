package gs.web.school;

import java.util.HashMap;
import java.util.Map;

/**
 * Maintains the state of the user.
 */
public class EspUserStateStruct {
    private boolean isNewUser = true;
    private boolean isEmailValid = true;
    private boolean isUserEmailValidated = false;
    private boolean isUserRequestedESP = false;
    private boolean isUserAwaitingESPMembership = false;
    private boolean isUserApprovedESPMember = false;
    private boolean isUserCookieSet = false;
    private boolean isCookieMatched = true;

    public boolean isNewUser() {
        return isNewUser;
    }

    public void setNewUser(boolean newUser) {
        isNewUser = newUser;
    }

    public boolean isEmailValid() {
        return isEmailValid;
    }

    public void setEmailValid(boolean emailValid) {
        isEmailValid = emailValid;
    }

    public boolean isUserEmailValidated() {
        return isUserEmailValidated;
    }

    public void setUserEmailValidated(boolean userEmailValidated) {
        isUserEmailValidated = userEmailValidated;
    }

    public boolean isUserRequestedESP() {
        return isUserRequestedESP;
    }

    public void setUserRequestedESP(boolean userRequestedESP) {
        isUserRequestedESP = userRequestedESP;
    }

    public boolean isUserAwaitingESPMembership() {
        return isUserAwaitingESPMembership;
    }

    public void setUserAwaitingESPMembership(boolean userAwaitingESPMembership) {
        isUserAwaitingESPMembership = userAwaitingESPMembership;
    }

    public boolean isUserApprovedESPMember() {
        return isUserApprovedESPMember;
    }

    public void setUserApprovedESPMember(boolean userApprovedESPMember) {
        isUserApprovedESPMember = userApprovedESPMember;
    }

    public boolean isUserCookieSet() {
        return isUserCookieSet;
    }

    public void setUserCookieSet(boolean userCookieSet) {
        isUserCookieSet = userCookieSet;
    }

    public boolean isCookieMatched() {
        return isCookieMatched;
    }

    public void setCookieMatched(boolean cookieMatched) {
        isCookieMatched = cookieMatched;
    }

    public Map getUserState() {
        Map data = new HashMap();

        data.put("isEmailValid", isEmailValid());
        data.put("isUserApprovedESPMember", isUserApprovedESPMember());
        data.put("isUserAwaitingESPMembership", isUserAwaitingESPMembership());
        data.put("isUserEmailValidated", isUserEmailValidated());
        data.put("isUserCookieSet", isUserCookieSet());
        data.put("isCookieMatched", isCookieMatched());
        return data;
    }

}