package gs.web.school;


import gs.data.community.IReportedEntityDao;
import gs.data.community.ReportedEntity;
import gs.data.community.User;
import gs.data.school.ISchoolMediaDao;
import gs.data.school.School;
import gs.data.school.SchoolMedia;
import gs.data.school.SchoolMediaDaoHibernate;
import gs.data.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(value="schoolMediaHelper")
public class SchoolMediaHelper {

    @Autowired
    ISchoolMediaDao _schoolMediaDao;
    @Autowired
    IReportedEntityDao _reportedEntityDao;


    public static final int DEFAULT_MAX_PHOTOS_IN_GALLERY = 10;

    public void addSchoolPhotosToModel(School school, Map<String, Object> model) {

        List<SchoolMedia> schoolMedias =  _schoolMediaDao.getActiveBySchool(school,DEFAULT_MAX_PHOTOS_IN_GALLERY);

        model.put("basePhotoPath", CommunityUtil.getMediaPrefix());
        model.put("photoGalleryImages",schoolMedias);

        if (model.get("validUser") != null){
            User user = (User) model.get("validUser"); // TODO: go directly to sessionContext rather than model,
            // but change sessionContext so it will only try to get the user once, so that user is cached
            model.put("photoReports", getReportsForSchoolMedia(user, schoolMedias));
        }
    }

    private Map<Integer, Boolean> getReportsForSchoolMedia(User user, List<SchoolMedia> schoolMediaList) {
        if (schoolMediaList == null || user == null) {
            return null;
        }
        Map<Integer, Boolean> reports = new HashMap<Integer, Boolean>(schoolMediaList.size());
        for (SchoolMedia schoolMedia: schoolMediaList) {
            reports.put(schoolMedia.getId(),
                    _reportedEntityDao.hasUserReportedEntity
                            (user, ReportedEntity.ReportedEntityType.schoolMedia, schoolMedia.getId()));
        }
        return reports;
    }

}
