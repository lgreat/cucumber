package gs.web.community.registration.popup;

import gs.data.community.SubscriptionProduct;
import gs.data.state.State;
import gs.web.community.registration.UserRegistrationCommand;
import gs.web.util.validator.EmailValidator;

import javax.validation.constraints.AssertTrue;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class MssRegistrationHoverCommand extends RegistrationHoverCommand implements EmailValidator.IEmail{

    public boolean isPasswordOrFacebookId() {
        return true; // don't require password or facebook ID
    }

}