package gs.web.admin;

import gs.data.school.EspMembership;
import org.springframework.validation.ObjectError;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: rramachandran
 * Date: 3/19/12
 * Time: 12:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddMembershipCommand {
    private String _memberId;
    private String _schoolIds;
    private String _state;
    private Set<ObjectError> _errors;

    public String getMemberId() {
        return _memberId;
    }

    public void setMemberId(String memberId) {
        _memberId = memberId;
    }

    public String getSchoolIds() {
        return _schoolIds;
    }

    public void setSchoolIds(String schoolIds) {
        _schoolIds = schoolIds;
    }

    public String getState() {
        return _state;
    }

    public void setState(String state) {
        _state = state;
    }

    public Set<ObjectError> getErrors() {
        return _errors;
    }

    public void setErrors(Set<ObjectError> errors) {
        _errors = errors;
    }
}
