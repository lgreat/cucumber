package gs.web.community.registration.popup;

import gs.web.community.registration.RegistrationController;
import gs.web.community.registration.UserCommand;
import gs.web.tracking.OmnitureTracking;
import gs.web.tracking.CookieBasedOmnitureTracking;
import gs.web.util.PageHelper;
import gs.web.util.UrlUtil;
import gs.web.util.ReadWriteController;
import gs.web.util.context.SessionContextUtil;
import gs.data.dao.hibernate.ThreadLocalTransactionManager;
import gs.data.community.User;
import gs.data.community.Subscription;
import gs.data.community.SubscriptionProduct;
import gs.data.school.School;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

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