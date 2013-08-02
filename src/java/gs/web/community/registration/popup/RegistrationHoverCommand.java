package gs.web.community.registration.popup;

import gs.data.community.SubscriptionProduct;
import gs.data.state.State;
import gs.web.community.registration.UserCommand;
import gs.web.community.registration.UserRegistrationCommand;
import gs.web.util.validator.EmailValidator;

import java.util.List;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class RegistrationHoverCommand extends UserRegistrationCommand implements EmailValidator.IEmail{
    public static enum JoinHoverType {
        Auto,
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
    private JoinHoverType _joinHoverType;
    private String _redirectUrl;

    public String joinTypeToHow() {
        switch (_joinHoverType) {
            case Auto:
                return "hover_mss";
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

    public List<NthGraderSubscription> getGradeNewsletters() {
        return _gradeNewsletters;
    }

    //////////////// Copied from UserCommand.java ////////////////////////////////////////
    public void setGradeNewsletters(List<NthGraderSubscription> gradeNewsletters) {
        _gradeNewsletters = gradeNewsletters;
    }//following list is used by NthGraderHover for the grade by grade newsletters.
    private List<NthGraderSubscription> _gradeNewsletters;

    public static class NthGraderSubscription{

        private boolean _checked;
        private SubscriptionProduct _subProduct;

        public NthGraderSubscription(boolean chk, SubscriptionProduct sub) {
            _checked = chk;
            _subProduct = sub;
        }

        public boolean getChecked() {
            return _checked;
        }

        public void setChecked(boolean checked) {
            _checked = checked;
        }

        public SubscriptionProduct getSubProduct() {
            return _subProduct;
        }
    }

    private boolean _newsletter = false;
    private boolean _partnerNewsletter = false;
    public boolean getNewsletter() {
        return _newsletter;
    }

    public void setNewsletter(boolean newsletter) {
        _newsletter = newsletter;
    }

    public boolean getPartnerNewsletter() {
        return _partnerNewsletter;
    }

    public void setPartnerNewsletter(boolean partnerNewsletter) {
        _partnerNewsletter = partnerNewsletter;
    }

    private String _mystatSchoolName;
    private int _mystatSchoolId;
    private State _mystatSchoolState;
    private boolean _mystat;

    public boolean isMystat() {
        return _mystat;
    }

    public void setMystat(boolean mystat) {
        _mystat = mystat;
    }

    public String getMystatSchoolName() {
        return _mystatSchoolName;
    }

    public void setMystatSchoolName(String mystatSchoolName) {
        _mystatSchoolName = mystatSchoolName;
    }

    public int getMystatSchoolId() {
        return _mystatSchoolId;
    }

    public void setMystatSchoolId(int mystatSchoolId) {
        _mystatSchoolId = mystatSchoolId;
    }

    public State getMystatSchoolState() {
        return _mystatSchoolState;
    }

    public void setMystatSchoolState(State mystatSchoolState) {
        _mystatSchoolState = mystatSchoolState;
    }


    //////////////// End Copied from UserCommand.java /////////////////////////////////////

    public RegistrationHoverCommand() {
        super();
    }

    public boolean isMslOnly() {
        return _mslOnly;
    }

    public void setMslOnly(boolean mslOnly) {
        _mslOnly = mslOnly;
    }

    public JoinHoverType getJoinHoverType() {
        return _joinHoverType;
    }

    public void setJoinHoverType(JoinHoverType joinHoverType) {
        _joinHoverType = joinHoverType;
    }

    public boolean isMssJoin() {
        return RegistrationHoverCommand.JoinHoverType.Auto == getJoinHoverType();
    }

    public String getRedirectUrl() {
        return _redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        _redirectUrl = redirectUrl;
    }
}