/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: NewsletterCommand.java,v 1.9 2006/08/09 00:33:02 dlee Exp $
 */
package gs.web.community.newsletters.popup;

import gs.data.state.State;
import gs.web.util.validator.EmailValidator.IEmail;
import gs.web.util.validator.SchoolIdValidator.ISchoolId;
import gs.web.util.validator.StateValidator.IState;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class NewsletterCommand implements IEmail, ISchoolId, IState {
    private String _email;
    private boolean _mystat;
    private int _schoolId;
    private String _schoolName;
    private boolean _greatnews;
    private boolean _myk;
    private boolean _my1;
    private boolean _my2;
    private boolean _my3;
    private boolean _my4;
    private boolean _my5;
    private boolean _myMs;
    private boolean _myHs;
    private State _state;
    private boolean _checked;
    private String _referrer;


    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public boolean isMystat() {
        return _mystat;
    }

    public void setMystat(boolean mystat) {
        _mystat = mystat;
        if (mystat) {
            _checked = true;
        }
    }

    public int getSchoolId() {
        return _schoolId;
    }

    public void setSchoolId(int schoolId) {
        _schoolId = schoolId;
    }

    public String getSchoolName() {
        return _schoolName;
    }

    public void setSchoolName(String schoolName) {
        _schoolName = schoolName;
    }

    public boolean isMyk() {
        return _myk;
    }

    public void setMyk(boolean myk) {
        _myk = myk;
        if (myk) {
            _checked = true;
        }
    }

    public boolean isGn() {
        return _greatnews;
    }

    public void setGn(boolean greatnews) {
        _greatnews = greatnews;
        if (greatnews) {
            _checked = true;
        }
    }

    public boolean isMy1() {
        return _my1;
    }

    public void setMy1(boolean my1) {
        _my1 = my1;
        if (my1) {
            _checked = true;
        }
    }

    public boolean isMy2() {
        return _my2;
    }

    public void setMy2(boolean my2) {
        _my2 = my2;
        if (my2) {
            _checked = true;
        }
    }

    public boolean isMy3() {
        return _my3;
    }

    public void setMy3(boolean my3) {
        _my3 = my3;
        if (my3) {
            _checked = true;
        }
    }

    public boolean isMy4() {
        return _my4;
    }

    public void setMy4(boolean my4) {
        _my4 = my4;
        if (my4) {
            _checked = true;
        }
    }

    public boolean isMy5() {
        return _my5;
    }

    public void setMy5(boolean my5) {
        _my5 = my5;
        if (my5) {
            _checked = true;
        }
    }

    public boolean isMyMs() {
        return _myMs;
    }

    public void setMyMs(boolean myMs) {
        _myMs = myMs;
        if (myMs) {
            _checked = true;
        }
    }

    public boolean isMyHs() {
        return _myHs;
    }

    public void setMyHs(boolean myHs) {
        _myHs = myHs;
        if (myHs) {
            _checked = true;
        }
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    public boolean isChecked() {
        return _checked;
    }

    public void setChecked(boolean checked) {
        _checked = checked;
    }

    public String getReferrer() {
        if (_referrer == null) {
            return "";
        }
        return _referrer;
    }

    public void setReferrer(String referrer) {
        _referrer = referrer;
    }
}
