package gs.web.school;

import gs.data.community.IUserDao;
import gs.data.school.*;
import gs.web.util.ReadWriteAnnotationController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/school/esp/moderation/**")
public class EspModerationController implements ReadWriteAnnotationController {
    public static final String FORM_VIEW = "school/espModerationForm";

    @Autowired
    private IEspMembershipDao _espMembershipDao;

    @Autowired
    private IUserDao _userDao;

    @Autowired
    private ISchoolDao _schoolDao;

    @RequestMapping(value = "/school/esp/moderation/form.page", method = RequestMethod.GET)
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

        for (Long id : command.getEspMembershipIds()) {
            EspMembership membership = getEspMembershipDao().findEspMembershipById(id);
            if ("approve".equals(command.getModeratorAction())) {
                membership.setStatus(EspMembershipStatus.APPROVED);
            } else if ("disapprove".equals(command.getModeratorAction())) {
                membership.setStatus(EspMembershipStatus.DISAPPROVED);
            }
            //TODO : do a bulk update.Also modify the pojo is ok?
            getEspMembershipDao().updateEspMembership(membership);
        }

        String redirect = "/school/esp/moderation/form.page";
        return "redirect:" + redirect;
    }

    private void populateModelWithMemberships(ModelMap modelMap) {
        List<EspMembership> memberships = getEspMembershipDao().findAllEspMemberships();
        List<EspMembership> membershipsToProcess = new ArrayList<EspMembership>();
        List<EspMembership> approvedMemberships = new ArrayList<EspMembership>();
        List<EspMembership> disapprovedMemberships = new ArrayList<EspMembership>();

        for (EspMembership membership : memberships) {
            long schoolId = membership.getSchoolId();
            School school = getSchoolDao().getSchoolById(membership.getState(), (int) schoolId);
            membership.setSchoolName(school.getName());

            if (membership.getStatus().equals(EspMembershipStatus.PROCESSING)) {
                membershipsToProcess.add(membership);
            }
        }

        modelMap.put("membershipsToProcess", membershipsToProcess);
        modelMap.put("approvedMemberships", approvedMemberships);
        modelMap.put("disapprovedMemberships", disapprovedMemberships);
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

}