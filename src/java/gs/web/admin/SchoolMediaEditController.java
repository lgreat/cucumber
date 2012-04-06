package gs.web.admin;

import gs.data.community.IReportedEntityDao;
import gs.data.community.IUserDao;
import gs.data.community.ReportedEntity;
import gs.data.community.User;
import gs.data.school.ISchoolDao;
import gs.data.school.ISchoolMediaDao;
import gs.data.school.SchoolMedia;
import gs.web.util.ReadWriteAnnotationController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/schoolMedia/edit.page")
public class SchoolMediaEditController implements ReadWriteAnnotationController {

    protected final Log _log = LogFactory.getLog(SchoolMediaEditController.class);

    public static final String PARAM_SCHOOL_MEDIA_ID = "id";

    public static final String VIEW = "admin/schoolMediaEdit";

    public static final String EDIT_URL = "admin/schoolMedia/edit.page";

    public static final String LIST_URL = "admin/schoolMedia/list.page";

    @Autowired
    private IReportedEntityDao _reportedEntityDao;

    @Autowired
    private ISchoolMediaDao _schoolMediaDao;

    @Autowired
    private IUserDao _userDao;

    @Autowired
    private ISchoolDao _schoolDao;

    @RequestMapping(method = RequestMethod.GET)
    public String showReportedMediaEditForm(ModelMap modelMap,
                                            @RequestParam(value = PARAM_SCHOOL_MEDIA_ID, required = true) Integer schoolMediaId) throws Exception {
        SchoolMediaEditCommand command = new SchoolMediaEditCommand();
        try {
            SchoolMedia schoolMedia = _schoolMediaDao.getById(schoolMediaId);
            command.setSchoolMedia(schoolMedia);
            command.setReports(_reportedEntityDao.getReports(ReportedEntity.ReportedEntityType.schoolMedia, schoolMedia.getId()));
            command.setSchool(_schoolDao.getSchoolById(schoolMedia.getSchoolState(), schoolMedia.getSchoolId()));
            command.setSender(_userDao.findUserFromId(schoolMedia.getMemberId()));
            command.setSchoolMediaId(schoolMedia.getId());
            if (command.getReports() != null && command.getReports().size() > 0) {
                Map<Integer, User> reportToUserMap = new HashMap<Integer, User>();
                for (ReportedEntity report : command.getReports()) {
                    try {
                        reportToUserMap.put(report.getId(), _userDao.findUserFromId(report.getReporterId()));
                    } catch (Exception e) {
                        _log.error("ERROR while retrieving user for report id=" + report.getId());
                    }
                }
                command.setReportToUserMap(reportToUserMap);
            }

        } catch (ObjectRetrievalFailureException orfe) {
            _log.error(orfe);
        }
        modelMap.addAttribute("schoolMediaEditCommand", command);
        return VIEW;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String editSchoolMedia(@ModelAttribute("schoolMediaEditCommand") SchoolMediaEditCommand command) throws Exception {

        String listPage = LIST_URL;
        if (command.getSchoolMediaId() == null) {
            return "redirect:/" + listPage;
        }

        SchoolMedia schoolMedia = _schoolMediaDao.getById(command.getSchoolMediaId());
        if (schoolMedia == null || command.getModeratorAction() == null) {
            return "redirect:/" + listPage;
        }

        String editPage = EDIT_URL + '?' + PARAM_SCHOOL_MEDIA_ID + '=' + schoolMedia.getId();
        String successView = "";
        if (command.getModeratorAction().equalsIgnoreCase("Resolve Report")
                || command.getModeratorAction().equalsIgnoreCase("Resolve Reports")) {
            _reportedEntityDao.resolveReportsFor(ReportedEntity.ReportedEntityType.schoolMedia, schoolMedia.getId());
            successView = listPage;
        } else if (command.getModeratorAction().equalsIgnoreCase("Disable Photo")) {
            _schoolMediaDao.disableById(schoolMedia.getId());
            successView = editPage;
        } else if (command.getModeratorAction().equalsIgnoreCase("Save Comments")) {
            schoolMedia.setNote(command.getNote());
            _schoolMediaDao.save(schoolMedia);
            successView = editPage;
        } else if (command.getModeratorAction().equalsIgnoreCase("Publish Photo")) {
            _schoolMediaDao.enableById(schoolMedia.getId());
            successView = editPage;
        }
        return "redirect:/" + successView;
    }

}