package gs.web.community.registration.popup;

import gs.web.community.registration.RegistrationController;
import gs.web.community.registration.UserCommand;
import gs.web.util.ReadWriteController;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Anthony Roy <mailto:droy@greatschools.org>
 */
public class LDRegistrationHoverController extends RegistrationController implements ReadWriteController {

    @Override
    protected boolean hasChildRows() {
        return false;
    }

    @Override
    protected void onBindOnNewForm(HttpServletRequest request,
                                   Object command,
                                   BindException errors) {
        super.onBind(request, command);
        ((UserCommand)command).setLdNewsletter(true);
    }
}