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
        TrackGradeAuto,
        GlobalHeader,
        FooterNewsletter,
        SchoolReview,
        BTSTip,
        MSL,
        Facebook
    }
    private boolean _mslOnly;
    private String _how;
    private JoinHoverType _joinHoverType;

    public String joinTypeToHow() {
        switch (_joinHoverType) {
            case Auto:
                return "hover_mss";
            case ChooserTipSheet:
                return "acq_chooserpack";
            case LearningDifficultiesNewsletter:
                return "hover_ld";
            case PostComment:
                return "hover_community";
            case TrackGrade:
                return "hover_greatnews";
            case TrackGradeAuto:
                return "hover_greatnews_auto";
            case GlobalHeader:
                return "hover_headerjoin";
            case FooterNewsletter:
                return "hover_footernewsletter";
            case SchoolReview:
                return "hover_review";
            case BTSTip:
                return "hover_btstip";
            case MSL:
                return "hover_msl";
            case Facebook:
                return "facebook";
        }
        return null;
    }

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