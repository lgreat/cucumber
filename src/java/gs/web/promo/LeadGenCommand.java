package gs.web.promo;

import org.apache.commons.lang.StringUtils;

public class LeadGenCommand {
    private String _campaign;
    private String _firstName;
    private String _lastName;
    private String _email;

    public String getCampaign() {
        return _campaign;
    }

    public void setCampaign(String campaign) {
        if (StringUtils.isNotBlank(campaign)) {
            _campaign = campaign.trim();
        }
    }

    public String getFirstName() {
        return _firstName;
    }

    public void setFirstName(String firstName) {
        if (StringUtils.isNotBlank(firstName)) {
            _firstName = firstName.trim();
        } else {
            _firstName = null;
        }
    }

    public String getLastName() {
        return _lastName;
    }

    public void setLastName(String lastName) {
        if (StringUtils.isNotBlank(lastName)) {
            _lastName = lastName.trim();
        } else {
            _lastName = null;
        }
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        if (StringUtils.isNotBlank(email)) {
            _email = email;
        } else {
            _email = null;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("PrimroseGenCommand");
        sb.append("{_campaign='").append(_campaign).append('\'');
        sb.append(", _firstName='").append(_firstName).append('\'');
        sb.append(", _lastName='").append(_lastName).append('\'');
        sb.append(", _email='").append(_email).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
