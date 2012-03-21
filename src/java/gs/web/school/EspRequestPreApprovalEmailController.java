package gs.web.school;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.util.DigestUtil;
import gs.web.util.ReadWriteAnnotationController;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping("/official-school-profile/espPreApprovalEmail.page")
public class EspRequestPreApprovalEmailController implements ReadWriteAnnotationController {
    private static final Log _log = LogFactory.getLog(EspRequestPreApprovalEmailController.class);

    protected ExactTargetAPI _exactTargetAPI;

    @Autowired
    private IUserDao _userDao;

    @RequestMapping(method = RequestMethod.GET)
    public void display(ModelMap modelMap, HttpServletRequest request) {
        String email = request.getParameter("email");
        String schoolName = request.getParameter("schoolName");
        if (StringUtils.isNotBlank(email) && StringUtils.isNotBlank(schoolName)) {
            User user = _userDao.findUserFromEmailIfExists(email);
            if (user != null) {
                if (!sendESPVerificationEmail(request, user, schoolName)) {
                    _log.error("ERROR sending pre-approval email.");
                }
            }
        }
    }

    public boolean sendESPVerificationEmail(HttpServletRequest request, User user, String schoolName) {
        boolean emailSendSuccess = false;
        if (user != null && StringUtils.isNotBlank(user.getEmail())) {
            try {
                String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());

                UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ESP_PRE_REGISTRATION, null, hash + user.getId());

                StringBuffer espVerificationUrl = new StringBuffer("<a href=\"");
                espVerificationUrl.append(urlBuilder.asFullUrl(request));
                espVerificationUrl.append("\">" + urlBuilder.asFullUrl(request) + "</a>");

                Map<String, String> emailAttributes = new HashMap<String, String>();
                emailAttributes.put("HTML__espVerificationUrl", espVerificationUrl.toString());
                emailAttributes.put("first_name", user.getFirstName());
                emailAttributes.put("ESP_schoolname", schoolName);
                _exactTargetAPI.sendTriggeredEmail("ESP-preapproval", user, emailAttributes);
                emailSendSuccess = true;
            } catch (Exception e) {
                //Do nothing.
            }
        }
        return emailSendSuccess;
    }

    public ExactTargetAPI getExactTargetAPI() {
        return _exactTargetAPI;
    }

    public void setExactTargetAPI(ExactTargetAPI exactTargetAPI) {
        _exactTargetAPI = exactTargetAPI;
    }

}