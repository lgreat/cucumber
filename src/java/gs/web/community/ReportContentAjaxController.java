package gs.web.community;

import gs.web.util.ReadWriteController;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ReportContentAjaxController extends SimpleFormController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object commandObj, BindException errors) throws Exception {
        ReportContentCommand command = (ReportContentCommand) commandObj;

        _log.info("User " + command.getReporterId() + " reported " + command.getType() +
                " with id " + command.getContentId() + ", with reason \"" + command.getReason() + "\".");

        // TODO: actually report the thing!
        
        return null;
    }
}
