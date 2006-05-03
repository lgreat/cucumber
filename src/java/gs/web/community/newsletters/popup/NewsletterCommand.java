/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: NewsletterCommand.java,v 1.1 2006/05/03 01:16:16 dlee Exp $
 */
package gs.web.community.newsletters.popup;

import gs.data.state.State;
import gs.web.util.IEmail;

/**
 * The purpose is ...
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class NewsletterCommand implements IEmail {
    private String _email;
    private boolean _mystat;
    private int _schoolId;
    private String _schoolName;
    private boolean _greatnews;
    private boolean _my1;
    private boolean _my2;
    private boolean _my3;
    private boolean _my4;
    private boolean _my5;
    private boolean _myMs;
    private boolean _myHs;
    private State _state;
    private String _errors;


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

    public boolean isGn() {
        return _greatnews;
    }

    public void setGreatnews(boolean greatnews) {
        _greatnews = greatnews;
    }

    public boolean isMy1() {
        return _my1;
    }

    public void setMy1(boolean my1) {
        _my1 = my1;
    }

    public boolean isMy2() {
        return _my2;
    }

    public void setMy2(boolean my2) {
        _my2 = my2;
    }

    public boolean isMy3() {
        return _my3;
    }

    public void setMy3(boolean my3) {
        _my3 = my3;
    }

    public boolean isMy4() {
        return _my4;
    }

    public void setMy4(boolean my4) {
        _my4 = my4;
    }

    public boolean isMy5() {
        return _my5;
    }

    public void setMy5(boolean my5) {
        _my5 = my5;
    }

    public boolean isMyMs() {
        return _myMs;
    }

    public void setMyMs(boolean myMs) {
        _myMs = myMs;
    }

    public boolean isMyHs() {
        return _myHs;
    }

    public void setMyHs(boolean myHs) {
        _myHs = myHs;
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    public String getErrors() {
        return _errors;
    }

    public void setErrors(String errors) {
        errors = _errors;
    }
}
