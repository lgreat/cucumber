package gs.web.admin;

import gs.data.community.IReportedEntityDao;
import gs.data.community.ReportedEntity;
import gs.data.school.*;
import gs.web.util.ReadWriteAnnotationController;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Controller
@RequestMapping("/admin/schoolMedia/list.page")
public class SchoolMediaListController implements ReadWriteAnnotationController {
    @Autowired
    private IReportedEntityDao _reportedEntityDao;

    @Autowired
    private ISchoolMediaDao _schoolMediaDao;

    @Autowired
    private ISchoolDao _schoolDao;

    public static final String VIEW = "admin/schoolMediaList";
    public static final String MODEL_TOTAL_REPORTED = "totalReportedMedia";
    public static final String MODEL_REPORTED_MEDIA = "reportedSchoolMedia";
    private static final int REPORTED_SCHOOL_MEDIA_PAGE_SIZE = 1;
    public static final String MODEL_PAGE_SIZE = "pageSize";
    private static final String PARAM_PAGE = "p";

    @RequestMapping(method = RequestMethod.GET)
    public String showReportedMediaList(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) throws Exception {

        int page = 1;
        if (StringUtils.isNotBlank(request.getParameter(PARAM_PAGE))
                && StringUtils.isNumeric(request.getParameter(PARAM_PAGE))) {
            page = Integer.parseInt(request.getParameter(PARAM_PAGE));
        }
        List<SchoolMediaListBean> reportedSchoolMedia = getReportedSchoolMedia(page);
        modelMap.put(MODEL_REPORTED_MEDIA, reportedSchoolMedia);
        modelMap.put(MODEL_TOTAL_REPORTED, _reportedEntityDao.countActiveReportsByEntityType(ReportedEntity.ReportedEntityType.schoolMedia));
        modelMap.put(MODEL_PAGE_SIZE, REPORTED_SCHOOL_MEDIA_PAGE_SIZE);
        return VIEW;
    }

    protected List<SchoolMediaListBean> getReportedSchoolMedia(int page) {
        int offset = 0;
        if (page > 1) {
            offset = (page - 1) * REPORTED_SCHOOL_MEDIA_PAGE_SIZE;
        }

        List<SchoolMediaListBean> returnVal = new ArrayList<SchoolMediaListBean>();

        //Get all the flagged school media Ids in asc order of date creation.
        List<Long> schoolMediaIdsLong = _reportedEntityDao.getDistinctReportedEntityIds(ReportedEntity.ReportedEntityType.schoolMedia,
                REPORTED_SCHOOL_MEDIA_PAGE_SIZE, offset);
        List<SchoolMedia> schoolMediaList = _schoolMediaDao.getByIds(getSchoolMediaIds(schoolMediaIdsLong));

        for (SchoolMedia schoolMedia : schoolMediaList) {
            School school = _schoolDao.getSchoolById(schoolMedia.getSchoolState(), schoolMedia.getSchoolId());
            if (school != null) {
                SchoolMediaListBean bean = new SchoolMediaListBean();
                bean.setSchool(school);
                bean.setSchoolMedia(schoolMedia);
                bean.setNumReports((_reportedEntityDao.getNumberTimesReported
                        (ReportedEntity.ReportedEntityType.schoolMedia, schoolMedia.getId())));
                bean.setReport(_reportedEntityDao.getOldestReport(ReportedEntity.ReportedEntityType.schoolMedia, schoolMedia.getId()));
                returnVal.add(bean);
            }
        }

        return returnVal;
    }

    protected List<Integer> getSchoolMediaIds(List<Long> schoolMediaIdsLong) {
        List<Integer> schoolMediaIds = new ArrayList<Integer>();
        for (Long schoolMediaId : schoolMediaIdsLong) {
            int sd = new Integer(schoolMediaId.toString());
            schoolMediaIds.add(sd);
        }
        return schoolMediaIds;
    }

    public static final class SchoolMediaListBean {
        private SchoolMedia _schoolMedia;
        private int _numReports;
        private ReportedEntity _report;
        private School _school;

        public SchoolMedia getSchoolMedia() {
            return _schoolMedia;
        }

        public void setSchoolMedia(SchoolMedia schoolMedia) {
            _schoolMedia = schoolMedia;
        }

        public int getNumReports() {
            return _numReports;
        }

        public void setNumReports(int numReports) {
            _numReports = numReports;
        }

        public ReportedEntity getReport() {
            return _report;
        }

        public void setReport(ReportedEntity report) {
            _report = report;
        }

        public School getSchool() {
            return _school;
        }

        public void setSchool(School school) {
            _school = school;
        }
    }


}