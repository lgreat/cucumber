package gs.web.school;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.school.*;
import gs.data.security.IRoleDao;
import gs.data.security.Role;
import gs.data.util.DigestUtil;
import gs.web.util.UrlBuilder;
import org.apache.commons.lang.StringUtils;
import gs.web.util.ReadWriteAnnotationController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/school/esp/moderation/form.page")
public class EspModerationController implements ReadWriteAnnotationController {
    public static final String FORM_VIEW = "school/espModerationForm";
    protected final Log _log = LogFactory.getLog(getClass());

    @Autowired
    private IEspMembershipDao _espMembershipDao;

    @Autowired
    private IUserDao _userDao;

    @Autowired
    private ISchoolDao _schoolDao;

    @Autowired
    private IRoleDao _roleDao;

    @RequestMapping(method = RequestMethod.GET)
    public String showForm(ModelMap modelMap) {
        EspModerationCommand command = new EspModerationCommand();
        modelMap.addAttribute("espModerationCommand", command);
        populateModelWithMemberships(modelMap);
        return FORM_VIEW;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String updateEspMembership(@ModelAttribute("espModerationCommand") EspModerationCommand command,
                                      BindingResult result,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {

        for (String idAndIndex : command.getEspMembershipIds()) {
            Long id = new Long(idAndIndex.substring(0, idAndIndex.indexOf("-")));
            int index = new Integer(idAndIndex.substring(idAndIndex.indexOf("-") + 1, idAndIndex.length())) - 1;
            EspMembership membership = getEspMembershipDao().findEspMembershipById(id, false);
            if ("approve".equals(command.getModeratorAction())) {
                User user = membership.getUser();
                if (user != null) {
                    membership.setStatus(EspMembershipStatus.APPROVED);
                    membership.setActive(true);
                    Role role = _roleDao.findRoleByKey(Role.ESP_MEMBER);
                    user.addRole(role);
                    getUserDao().updateUser(user);
                    try {
                        sendESPVerificationEmail(request, user, membership);

                    } catch (Exception e) {
                        _log.error("Error sending email message: " + e, e);
                    }
                }
            } else if ("disapprove".equals(command.getModeratorAction())) {
                membership.setStatus(EspMembershipStatus.REJECTED);
            }

            if (StringUtils.isNotBlank(command.getNote().get(index))) {
                membership.setNote(command.getNote().get(index));
            }
            //TODO : do a bulk update.Also modify the pojo is ok?
            getEspMembershipDao().updateEspMembership(membership);
        }

        String redirect = "/school/esp/moderation/form.page";
        return "redirect:" + redirect;
    }

    private void populateModelWithMemberships(ModelMap modelMap) {
        List<EspMembership> memberships = getEspMembershipDao().findAllEspMemberships(false);
        List<EspMembership> membershipsToProcess = new ArrayList<EspMembership>();
        List<EspMembership> approvedMemberships = new ArrayList<EspMembership>();
        List<EspMembership> disapprovedMemberships = new ArrayList<EspMembership>();

        for (EspMembership membership : memberships) {
            long schoolId = membership.getSchoolId();
            School school = getSchoolDao().getSchoolById(membership.getState(), (int) schoolId);
            membership.setSchool(school);

            if (membership.getStatus().equals(EspMembershipStatus.PROCESSING)) {
                membershipsToProcess.add(membership);
            }
        }

        modelMap.put("membershipsToProcess", membershipsToProcess);
        modelMap.put("approvedMemberships", approvedMemberships);
        modelMap.put("disapprovedMemberships", disapprovedMemberships);
    }

    private void sendESPVerificationEmail(HttpServletRequest request, User user,EspMembership membership) throws NoSuchAlgorithmException {
        String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        Date now = new Date();
        String nowAsString = String.valueOf(now.getTime());
        hash = DigestUtil.hashString(hash + nowAsString);

        UrlBuilder builder = new UrlBuilder(UrlBuilder.REGISTRATION_VALIDATION,
                null,
                hash + user.getId());
        builder.addParameter("date", nowAsString);
        //TODO change this to ESP form.
        String redirect = "school/esp/form.page";
        builder.addParameter("redirect", redirect);
//        builder.addParameter("schoolId", membership.getSchoolId().toString());
//        builder.addParameter("state", membership.getState().toString());

        String verificationLink = builder.asAbsoluteAnchor(request, builder.asFullUrl(request)).asATag();
        //TODO send ET email with verificationLink as param.
        System.out.println("--verificationLink--------------" + verificationLink);
    }

    public IEspMembershipDao getEspMembershipDao() {
        return _espMembershipDao;
    }

    public void setEspMembershipDao(IEspMembershipDao espMembershipDao) {
        _espMembershipDao = espMembershipDao;
    }

    public IUserDao getUserDao() {
        return _userDao;
    }

    public void setUserDao(IUserDao userDao) {
        _userDao = userDao;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public IRoleDao getRoleDao() {
        return _roleDao;
    }

    public void setRoleDao(IRoleDao roleDao) {
        _roleDao = roleDao;
    }

}