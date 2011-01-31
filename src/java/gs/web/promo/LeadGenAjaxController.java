package gs.web.promo;

import gs.data.promo.ILeadGenDao;
import gs.data.promo.LeadGen;
import gs.web.util.ReadWriteAnnotationController;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.EmailValidator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Young Fan <mailto:yfan@greatschools.org>
 */
@org.springframework.stereotype.Controller
public class LeadGenAjaxController implements ReadWriteAnnotationController {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String SUCCESS = "OK";
    public static final String FAILURE = "0";

    private ILeadGenDao _leadGenDao;

    @RequestMapping(value = "/promo/leadGenAjax.page", method = RequestMethod.POST)
    public void generateLead(@ModelAttribute("command") LeadGenCommand command,
                               HttpServletRequest request, HttpServletResponse response) throws Exception {
        _log.info(command.toString());

        String errors = validate(command);
        if (StringUtils.isBlank(errors)) {
            // log data
            logData(command);

            response.getWriter().print(SUCCESS);
            return;
        }

        _log.warn("Failure generating lead for " + command.getCampaign() + ": " + command.getEmail());
        response.getWriter().print(errors);
    }

    /**
     * Returns empty string if the command seems valid; otherwise, comma-separated list of fields with errors
     */
    protected String validate(LeadGenCommand command) {
        List<String> errorList = new ArrayList<String>();

        // validate not null firstname, lastname, email
        if (StringUtils.isBlank(command.getFirstName())) {
            errorList.add("firstName");
        }
        if (StringUtils.isBlank(command.getLastName())) {
            errorList.add("lastName");
        }
        if (StringUtils.isBlank(command.getEmail())) {
            errorList.add("email");
        } else {
            // validate format email
            EmailValidator emailValidator = EmailValidator.getInstance();
            if (!emailValidator.isValid(command.getEmail())) {
                _log.warn("Lead gen submitted with invalid email: " + command.getEmail());
                errorList.add("email");
            }
        }

        return StringUtils.join(errorList, ',');
    }

    private void logData(LeadGenCommand command) {
        LeadGen leadGen =
                new LeadGen(command.getCampaign(), new Date(), command.getFirstName(),
                                      command.getLastName(), command.getEmail());
        _leadGenDao.save(leadGen);
    }

    public ILeadGenDao getLeadGenDao() {
        return _leadGenDao;
    }

    public void setLeadGenDao(ILeadGenDao leadGenDao) {
        _leadGenDao = leadGenDao;
    }
}
