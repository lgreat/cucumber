package gs.web.promo;

import org.apache.commons.lang.StringUtils;

public class EmailSignUpCommand {
    private String _email;

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        if (StringUtils.isNotBlank(email)) {
            _email = email.trim();
        } else {
            _email = null;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("EmailSignUpCommand");
        sb.append("{");
        sb.append("_email='").append(_email).append('\'');
        sb.append("}");
        return sb.toString();
    }
}
