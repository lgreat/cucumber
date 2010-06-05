package gs.web.backToSchool;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/backToSchool/backToSchoolChecklist.page")
public class BackToSchoolChecklistController {
    protected final Log _log = LogFactory.getLog(getClass());

    BackToSchoolChecklist _backToSchoolChecklist;

    @RequestMapping(method=RequestMethod.GET)
    public String handleRequestInternal(HttpServletRequest request, Model model) throws Exception {
        return "backToSchool/backToSchoolChecklist";
    }

    @RequestMapping(method=RequestMethod.POST)
    public void addChecklistItem(HttpServletRequest request, HttpServletResponse response, @RequestParam("checklistItem") String checklistItem) {

        SessionContext sessionContext = SessionContextUtil.getSessionContext(request);

        if (sessionContext == null) {
            _log.warn("No SessionContext found in request.");
            return;
        }

        User user = sessionContext.getUser();
        _backToSchoolChecklist.addChecklistItem(checklistItem, user);
      
        try {
            response.setContentType("application/json");
            response.getWriter().print(1);
            response.getWriter().flush();
        } catch (IOException e) {
            _log.info("Failed to get response writer");
            //give up
        }
    }

    public BackToSchoolChecklist getBackToSchoolChecklist() {
        return _backToSchoolChecklist;
    }

    public void setBackToSchoolChecklist(BackToSchoolChecklist backToSchoolChecklist) {
        _backToSchoolChecklist = backToSchoolChecklist;
    }
}
