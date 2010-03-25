package gs.web.email;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import gs.data.state.StateManager;
import gs.data.state.State;
import gs.data.community.Student;
import gs.data.community.SubscriptionProduct;
import gs.data.school.School;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: eddie
 * Date: Mar 24, 2010
 * Time: 2:21:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class UnsubscribeCommand {
    protected final Log _log = LogFactory.getLog(getClass());

    private int userId;
    private String email;
    private String verified;

    public String getVerified() {
        return verified;
    }

    public void setVerified(String verified) {
        this.verified = verified;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

}
