package gs.web.api;

import gs.data.util.string.StringUtils;

/**
 * @author chriskimm@greatschools.net
 */
public class RegistrationCommand {

    private String _name;
    private String _organization;
    private String _email;
    private String _industry;

    public String getindustry() {
        return _industry;
    }

    public void setindustry(String industry) {
        _industry = industry;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getOrganization() {
        return _organization;
    }

    public void setOrganization(String organization) {
        _organization = organization;
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }

    @Override
    public String toString() {
        return StringUtils.buildString("RegistrationCommand - name:", getName(),
                " org: ", getOrganization(), " email: ", getEmail());
    }
}
