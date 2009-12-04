package gs.web.community.registration.popup;

import gs.web.community.registration.LoginCommand;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class LoginHoverCommand extends LoginCommand {
    private boolean _joinError = false;
    private String _how;

    public boolean isJoinError() {
        return _joinError;
    }

    public void setJoinError(boolean joinError) {
        _joinError = joinError;
    }

    public String getHow() {
        return _how;
    }

    public void setHow(String how) {
        _how = how;
    }
}