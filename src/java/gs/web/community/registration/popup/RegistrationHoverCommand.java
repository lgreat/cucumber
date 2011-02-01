package gs.web.community.registration.popup;

import gs.web.community.registration.UserCommand;
import gs.web.util.validator.EmailValidator;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class RegistrationHoverCommand extends UserCommand implements EmailValidator.IEmail{
    public static enum JoinHoverType {
        Auto,
        ChooserTipSheet,
        LearningDifficultiesNewsletter,
        PostComment,
        TrackGrade,
        GlobalHeader,
        FooterNewsletter,
        SchoolReview,
        BTSTip,
        MSL,
    }
    private boolean _mslOnly;
    private String _how;
    private JoinHoverType _joinHoverType;

    public RegistrationHoverCommand() {
        super();
    }

    public boolean isMslOnly() {
        return _mslOnly;
    }

    public void setMslOnly(boolean mslOnly) {
        _mslOnly = mslOnly;
    }

    public String getHow() {
        return _how;
    }

    public void setHow(String how) {
        _how = how;
    }

    public JoinHoverType getJoinHoverType() {
        return _joinHoverType;
    }

    public void setJoinHoverType(JoinHoverType joinHoverType) {
        _joinHoverType = joinHoverType;
    }
}