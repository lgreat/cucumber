package gs.web.admin;

import gs.data.community.IReportedEntityDao;
import gs.data.community.ReportedEntity;
import gs.data.school.*;
import gs.data.state.State;
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
@RequestMapping("/admin/schoolPhoto/list.page")
public class SchoolPhotoListController implements ReadWriteAnnotationController {
    @Autowired
    private IReportedEntityDao _reportedEntityDao;

    @Autowired
    private ISchoolMediaDao _schoolMediaDao;

    @Autowired
    private ISchoolDao _schoolDao;

    public static final String VIEW = "admin/schoolPhotoList";
    private static final int REPORTED_SCHOOL_MEDIA_PAGE_SIZE = 75;
    private static final String PARAM_PAGE = "p";

    @RequestMapping(method = RequestMethod.GET)
    public String showForm(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) throws Exception {

        int page = 1;
        if (StringUtils.isNotBlank(request.getParameter(PARAM_PAGE))
                && StringUtils.isNumeric(request.getParameter(PARAM_PAGE))) {
            page = Integer.parseInt(request.getParameter(PARAM_PAGE));
        }
        List<SchoolMediaListBean> reportedSchoolPhotos = getReportedSchoolMedia(page);
        modelMap.put("reportedSchoolPhotos", reportedSchoolPhotos);
        return VIEW;
    }

    protected List<SchoolMediaListBean> getReportedSchoolMedia(int page) {
        int offset = 0;
        if (page > 1) {
            offset = (page - 1) * REPORTED_SCHOOL_MEDIA_PAGE_SIZE;
        }
        List<SchoolMediaListBean> returnVal = new ArrayList<SchoolMediaListBean>();

        //Get all the flagged school media in asc order of date creation.
        List<ReportedEntity> reportedEntities = _reportedEntityDao.getActiveReportsByEntityType(ReportedEntity.ReportedEntityType.schoolMedia);

        Map<Integer, ReportedEntity> reportedEntityIdToObj = new HashMap<Integer, ReportedEntity>();
        Map<Integer, Integer> reportedEntityIdToCount = new HashMap<Integer, Integer>();
        Set<Integer> uniqueSchoolMediaIds = new HashSet<Integer>();

        for (ReportedEntity reportedEntity : reportedEntities) {
            //We only want the oldest reported entity.
            //Therefore just store the first one.(List of reportedEntities is sorted in asc order of date creation)
            if (reportedEntityIdToObj.get(reportedEntity.getReportedEntityId()) == null) {
                reportedEntityIdToObj.put((int) reportedEntity.getReportedEntityId(), reportedEntity);
            }

            //Count the number of times an entity is reported.
            if (reportedEntityIdToCount.get(reportedEntity.getReportedEntityId()) == null) {
                reportedEntityIdToCount.put((int) reportedEntity.getReportedEntityId(), 1);
                //Add to a unique set of Ids.This is used to bulk query the schoolMedia table.
                uniqueSchoolMediaIds.add((int) reportedEntity.getReportedEntityId());
            } else {
                int count = reportedEntityIdToCount.get(reportedEntity.getReportedEntityId());
                reportedEntityIdToCount.put((int) reportedEntity.getReportedEntityId(), count++);
            }
        }

        if (uniqueSchoolMediaIds.size() > 0 && reportedEntityIdToCount.size() > 0 && reportedEntityIdToObj.size() > 0) {
            List<SchoolMedia> schoolMediaList = _schoolMediaDao.getByIds(uniqueSchoolMediaIds);
            for (SchoolMedia schoolMedia : schoolMediaList) {
                School school = _schoolDao.getSchoolById(schoolMedia.getSchoolState(), schoolMedia.getSchoolId());
                if (school != null) {
                    SchoolMediaListBean schoolMediaListBean = new SchoolMediaListBean();
                    schoolMediaListBean.setSchool(school);
                    schoolMediaListBean.setSchoolMedia(schoolMedia);
                    if (reportedEntityIdToCount.get(schoolMedia.getId()) != null) {
                        schoolMediaListBean.setNumReports(reportedEntityIdToCount.get(schoolMedia.getId());
                    }
                    if (reportedEntityIdToObj.get(schoolMedia.getId()) != null) {
                        schoolMediaListBean.setReport(reportedEntityIdToObj.get(schoolMedia.getId()));
                    }
                    returnVal.add(schoolMediaListBean);
                }
            }
        }

        return returnVal;
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