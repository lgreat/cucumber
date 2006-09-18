package gs.web.community.registration;

import gs.data.community.User;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class NewsletterCommand {
    private User _user;
    private int _availableMssSubs;
    private boolean _allMss = false; // default
    private List _studentSchools;
    private boolean _hasK = false;
    private boolean _hasFirst = false;
    private boolean _hasSecond = false;
    private boolean _hasThird = false;
    private boolean _hasFourth = false;
    private boolean _hasFifth = false;
    private boolean _hasMiddle = false;
    private boolean _hasHigh = false;
    private List _subscriptions;

    public NewsletterCommand() {
        _user = new User();
        _studentSchools = new ArrayList();
    }

    public User getUser() {
        return _user;
    }

    public void setUser(User user) {
        this._user = user;
    }

    public void setAvailableMssSubs(int availableSubs) {
        _availableMssSubs = availableSubs;
    }

    public int getAvailableMssSubs() {
        return _availableMssSubs;
    }

    public void setAllMss(boolean b) {
        _allMss = b;
    }

    /**
     * Indicates to the page that only a single checkbox should be presented for all the
     * schools. If false, indicates that one checkbox per school should be provided.
     */
    public boolean getAllMss() {
        return _allMss;
    }

    public List getStudentSchools() {
        return _studentSchools;
    }

    public void setStudentSchools(List studentSchools) {
        _studentSchools = studentSchools;
    }

    public int getNumStudentSchools() {
        return _studentSchools.size();
    }

    public int getNumStudents() {
        return (_user.getStudents() != null?_user.getStudents().size():0);
    }

    public boolean getHasK() {
        return _hasK;
    }

    public void setHasK(boolean hasK) {
        _hasK = hasK;
    }

    public boolean getHasFirst() {
        return _hasFirst;
    }

    public void setHasFirst(boolean hasFirst) {
        _hasFirst = hasFirst;
    }

    public boolean getHasSecond() {
        return _hasSecond;
    }

    public void setHasSecond(boolean hasSecond) {
        _hasSecond = hasSecond;
    }

    public boolean getHasThird() {
        return _hasThird;
    }

    public void setHasThird(boolean hasThird) {
        _hasThird = hasThird;
    }

    public boolean getHasFourth() {
        return _hasFourth;
    }

    public void setHasFourth(boolean hasFourth) {
        _hasFourth = hasFourth;
    }

    public boolean getHasFifth() {
        return _hasFifth;
    }

    public void setHasFifth(boolean hasFifth) {
        _hasFifth = hasFifth;
    }

    public boolean getHasMiddle() {
        return _hasMiddle;
    }

    public void setHasMiddle(boolean hasMiddle) {
        _hasMiddle = hasMiddle;
    }

    public boolean getHasHigh() {
        return _hasHigh;
    }

    public void setHasHigh(boolean hasHigh) {
        _hasHigh = hasHigh;
    }

    /**
     * This variable and the user are the only ones consulted by the controller during the
     * onSubmit phase. Thus, this needs to get populated prior to that (generally, onBindAndValidate) 
     */
    public List getSubscriptions() {
        return _subscriptions;
    }

    public void setSubscriptions(List subs) {
        _subscriptions = subs;
    }
}
