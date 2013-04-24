package gs.web.community.registration;

import gs.data.community.Subscription;
import gs.data.community.SubscriptionProduct;
import gs.data.state.State;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

public class UserSubscriptionCommand {
    protected final Log _log = LogFactory.getLog(getClass());

    private List<Subscription> _subscriptions;
    private boolean _newsletter = false;
    private boolean _partnerNewsletter = false;
    private boolean _ldNewsletter = false;
    private boolean _brainDrainNewsletter = false;
    private String _startweek;
    private boolean _chooserRegistration = false;

    private boolean _mystat = true;
    private String _mystatSchoolName;
    private int _mystatSchoolId;
    private State _mystatSchoolState;

    // back to school tips
    private boolean _btsTip = false;
    private String btsTipVersion = null;


    public boolean isBtsTip() {
        return _btsTip;
    }

    public void setBtsTip(boolean btsTip) {
        _btsTip = btsTip;
    }

    public String getBtsTipVersion() {
        return btsTipVersion;
    }

    public void setBtsTipVersion(String btsTipVersion) {
        this.btsTipVersion = btsTipVersion;
    }

    public List<NthGraderSubscription> getGradeNewsletters() {
        return _gradeNewsletters;
    }

    public void setGradeNewsletters(List<NthGraderSubscription> gradeNewsletters) {
        _gradeNewsletters = gradeNewsletters;
    }//following list is used by NthGraderHover for the grade by grade newsletters.
    private List<NthGraderSubscription> _gradeNewsletters;

    public UserSubscriptionCommand() {
        _subscriptions = new ArrayList<Subscription>();
        _gradeNewsletters = new ArrayList<NthGraderSubscription>();
    }

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

    public boolean getLdNewsletter() {
        return _ldNewsletter;
    }

    public void setLdNewsletter(boolean ldNewsletter) {
        _ldNewsletter = ldNewsletter;
    }

     public boolean getBrainDrainNewsletter() {
        return _brainDrainNewsletter;
    }

    public void setBrainDrainNewsletter(boolean brainDrainNewsletter) {
        _brainDrainNewsletter = brainDrainNewsletter;
    }

     public String getStartweek() {
        return _startweek;
    }

    public void setStartweek(String startweek) {
        this._startweek = startweek;
    }

    public boolean isChooserRegistration() {
        return _chooserRegistration;
    }

    public void setChooserRegistration(boolean chooserRegistration) {
        _chooserRegistration = chooserRegistration;
    }

    public void setNumStudents(int num) {
        // ignore -- this is so JSTL treats this as a bean property
    }

    public List<Subscription> getSubscriptions() {
        return _subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        _subscriptions = subscriptions;
    }

    public void addSubscription(Subscription sub) {
        getSubscriptions().add(sub);
    }

    public int getNumSubscriptions() {
        return getSubscriptions().size();
    }

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

}