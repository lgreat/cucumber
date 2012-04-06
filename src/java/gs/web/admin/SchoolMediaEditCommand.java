package gs.web.admin;

import gs.data.community.ReportedEntity;
import gs.data.community.User;
import gs.data.school.School;
import gs.data.school.SchoolMedia;

import java.util.List;
import java.util.Map;

public class SchoolMediaEditCommand {
    private SchoolMedia _schoolMedia;
    private Integer _schoolMediaId;
    private String _note;
    private List<ReportedEntity> _reports;
    private Map<Integer, User> _reportToUserMap;
    private School _school;
    private User _sender;
    private String _moderatorAction;

    public SchoolMedia getSchoolMedia() {
        return _schoolMedia;
    }

    public void setSchoolMedia(SchoolMedia schoolMedia) {
        _schoolMedia = schoolMedia;
    }

    public Integer getSchoolMediaId() {
        return _schoolMediaId;
    }

    public void setSchoolMediaId(Integer schoolMediaId) {
        _schoolMediaId = schoolMediaId;
    }

    public String getNote() {
        return _note;
    }

    public void setNote(String note) {
        _note = note;
    }

    public List<ReportedEntity> getReports() {
        return _reports;
    }

    public void setReports(List<ReportedEntity> reports) {
        _reports = reports;
    }

    public Map<Integer, User> getReportToUserMap() {
        return _reportToUserMap;
    }

    public void setReportToUserMap(Map<Integer, User> reportToUserMap) {
        _reportToUserMap = reportToUserMap;
    }

    public School getSchool() {
        return _school;
    }

    public void setSchool(School school) {
        _school = school;
    }

    public User getSender() {
        return _sender;
    }

    public void setSender(User sender) {
        _sender = sender;
    }

    public String getModeratorAction() {
        return _moderatorAction;
    }

    public void setModeratorAction(String moderatorAction) {
        _moderatorAction = moderatorAction;
    }
}
