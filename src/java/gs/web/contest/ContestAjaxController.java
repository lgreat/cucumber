package gs.web.contest;

import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.validator.EmailValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import gs.data.contest.IContestDao;
import gs.data.contest.Contest;
import gs.data.contest.ContestEntry;
import gs.web.util.ReadWriteController;
import gs.web.tracking.OmnitureTracking;
import gs.web.tracking.CookieBasedOmnitureTracking;

/**
 * GS-8998
 */
public class ContestAjaxController implements ReadWriteController {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_OPT_IN_PARTNER = "optInPartner";
    public static final String PARAM_OPT_IN_GS = "optInGS";

    private Integer _contestId;
    private IContestDao _contestDao;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        PrintWriter out = response.getWriter();
        String result;

        String email = request.getParameter(PARAM_EMAIL);
        String optInPartner = request.getParameter(PARAM_OPT_IN_PARTNER);
        String optInGS = request.getParameter(PARAM_OPT_IN_GS);

        EmailValidator emv = EmailValidator.getInstance();
        if (emv.isValid(email)) {
            // normalize the email address to reduce the chance of duplicates
            email = email.toLowerCase().trim();

            Contest contest = _contestDao.findContestById(_contestId);
            if (contest != null) {
                ContestEntry entry = _contestDao.findEntry(contest.getId(), email);
                if (entry == null) {
                    entry = new ContestEntry();
                    entry.setContest(contest);
                    entry.setEmail(email);
                    entry.setOptedInPartner("true".equals(optInPartner));
                    entry.setOptedInGS("true".equals(optInGS));

                    _contestDao.save(entry);

                    OmnitureTracking ot = new CookieBasedOmnitureTracking(request, response);
                    ot.addSuccessEvent(OmnitureTracking.SuccessEvent.SweepstakesEntered);
                }
                result = "ok";
            } else {
                result = "invalidContestId";
            }

        } else {
            result = "invalidEmail";
        }

        out.print(result);

        return null;
    }

    public Integer getContestId() {
        return _contestId;
    }

    public void setContestId(Integer contestId) {
        _contestId = contestId;
    }

    public IContestDao getContestDao() {
        return _contestDao;
    }

    public void setContestDao(IContestDao contestDao) {
        _contestDao = contestDao;
    }
}
