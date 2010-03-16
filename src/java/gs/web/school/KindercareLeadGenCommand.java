package gs.web.school;

import gs.data.state.State;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class KindercareLeadGenCommand {
    private int _schoolId;
    private State _state;
    private String _firstName;
    private String _lastName;
    private String _email;
    private boolean _informed;
    private boolean _offers;

    public int getSchoolId() {
        return _schoolId;
    }

    public void setSchoolId(int schoolId) {
        _schoolId = schoolId;
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    public String getFirstName() {
        return _firstName;
    }

    public void setFirstName(String firstName) {
        _firstName = firstName;
    }

    public String getLastName() {
        return _lastName;
    }

    public void setLastName(String lastName) {
        _lastName = lastName;
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public boolean isInformed() {
        return _informed;
    }

    public void setInformed(boolean informed) {
        _informed = informed;
    }

    public boolean isOffers() {
        return _offers;
    }

    public void setOffers(boolean offers) {
        _offers = offers;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("KindercareLeadGenCommand");
        sb.append("{_schoolId=").append(_schoolId);
        sb.append(", _state=").append(_state);
        sb.append(", _firstName='").append(_firstName).append('\'');
        sb.append(", _lastName='").append(_lastName).append('\'');
        sb.append(", _email='").append(_email).append('\'');
        sb.append(", _informed=").append(_informed);
        sb.append(", _offers=").append(_offers);
        sb.append('}');
        return sb.toString();
    }
}
