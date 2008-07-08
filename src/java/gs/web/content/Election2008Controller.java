package gs.web.content;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class Election2008Controller extends SimpleFormController {
    public static final String BEAN_ID = "/content/election2008.page";
    protected final Log _log = LogFactory.getLog(getClass());

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object objCommand, BindException errors) {
        Election2008Command command = (Election2008Command) objCommand;

        // since I'm forwarding to another FormController, I need to pass it info
        // in the language it understands ... namely it's command
        Election2008EmailCommand emailCommand = new Election2008EmailCommand();

        // TODO: utilize SiteVisitor API to sync email
        emailCommand.setUserEmail(command.getEmail());
        emailCommand.setSuccess(true);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("edin08Cmd", emailCommand);
        return new ModelAndView(getSuccessView(), model);
    }
}
