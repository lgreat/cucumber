package gs.web.promo;

import gs.data.promo.ILeadGenDao;
import gs.data.promo.LeadGen;
import gs.data.school.School;
import gs.web.school.KindercareLeadGenCommand;
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
import java.util.Date;

/**
 * @author Young Fan <mailto:yfan@greatschools.org>
 */
@org.springframework.stereotype.Controller
public class LeadGenAjaxController implements ReadWriteAnnotationController {
    protected final Log _log = LogFactory.getLog(getClass());
    public static final String SUCCESS = "1";
    public static final String FAILURE = "0";

    public static final String CAMPAIGN_PRIMROSE = "primrose";

    private ILeadGenDao _leadGenDao;

    @RequestMapping(value = "/promo/primroseLeadGenAjax.page", method = RequestMethod.POST)
    public void generatePrimroseLead(@ModelAttribute("command") LeadGenCommand command,
                               HttpServletRequest request, HttpServletResponse response) throws Exception {
        _log.info(command.toString());

        if (validatePrimrose(command)) {
            // log data
            logData(CAMPAIGN_PRIMROSE, command);

            response.getWriter().print(SUCCESS);
            return;
        }

        _log.warn("Failure generating lead for " + CAMPAIGN_PRIMROSE + ": " + command.getEmail());
        response.getWriter().print(FAILURE);
    }

    /**
     * Returns true if the command seems valid
     */
    protected boolean validatePrimrose(LeadGenCommand command) {

        // validate not null firstname, lastname, email
        if (StringUtils.isBlank(command.getFirstName())
                || StringUtils.isBlank(command.getLastName())
                || StringUtils.isBlank(command.getEmail())) {
            return false;
        }
        // validate format email
        EmailValidator emailValidator = EmailValidator.getInstance();
        if (!emailValidator.isValid(command.getEmail())) {
            _log.warn("Lead gen submitted with invalid email: " + command.getEmail());
            return false;
        }

        return true;
    }

    private void logData(String campaign, LeadGenCommand command) {
        LeadGen leadGen =
                new LeadGen(campaign, new Date(), command.getFirstName(),
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
