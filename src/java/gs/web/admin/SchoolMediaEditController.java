package gs.web.admin;


import gs.data.community.IReportedEntityDao;
import gs.data.community.IUserDao;
import gs.data.community.ReportedEntity;
import gs.data.community.User;
import gs.data.school.ISchoolDao;
import gs.data.school.ISchoolMediaDao;
import gs.data.school.SchoolMedia;
import gs.web.util.ReadWriteAnnotationController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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

    @Autowired
    private IReportedEntityDao _reportedEntityDao;

    @Autowired
    private ISchoolMediaDao _schoolMediaDao;

    @Autowired
    private IUserDao _userDao;

    @Autowired
    private ISchoolDao _schoolDao;

    public static final String PARAM_SCHOOL_MEDIA_ID = "id";

    public static final String VIEW = "admin/schoolMediaEdit";

    @RequestMapping(method = RequestMethod.GET)
    public String showReportedMediaEditForm(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response,
                                            @RequestParam(value = PARAM_SCHOOL_MEDIA_ID, required = true) Integer schoolMediaId) throws Exception {
        SchoolMediaEditCommand command = new SchoolMediaEditCommand();
        try {
            SchoolMedia schoolMedia = _schoolMediaDao.getById(schoolMediaId);
            command.setSchoolMedia(schoolMedia);
            command.setReports(_reportedEntityDao.getReports(ReportedEntity.ReportedEntityType.schoolMedia, schoolMedia.getId()));
            command.setSchool(_schoolDao.getSchoolById(schoolMedia.getSchoolState(),schoolMedia.getSchoolId()));
            command.setSender(_userDao.findUserFromId(schoolMedia.getMemberId()));
            if (command.getReports() != null && command.getReports().size() > 0) {
                Map<Integer, User> reportToUserMap = new HashMap<Integer, User>();
                for (ReportedEntity report : command.getReports()) {
                    try {
                        reportToUserMap.put(report.getId(), _userDao.findUserFromId(report.getReporterId()));
                    } catch (Exception e) {
                        // ignore
                    }
                }
                command.setReportToUserMap(reportToUserMap);
            }

        } catch (ObjectRetrievalFailureException orfe) {
            // do nothing
        }
        modelMap.addAttribute("schoolMediaEditCommand", command);
        return VIEW;
    }


}