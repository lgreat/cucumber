package gs.web.admin;

import gs.data.community.ReportedEntity;
import gs.data.community.User;
import gs.data.school.School;
import gs.data.school.SchoolMedia;

import java.util.List;
import java.util.Map;

public class SchoolMediaEditCommand {
    private SchoolMedia _schoolMedia;
    private Integer _id;
    private String _note;
    private String _status;
    private boolean _cancel = false;
    private List<ReportedEntity> _reports;
    private Map<Integer, User> _reportToUserMap;
    private School _school;
    private User _sender;

    public SchoolMedia getSchoolMedia() {
        return _schoolMedia;
    }

    public void setSchoolMedia(SchoolMedia schoolMedia) {
        _schoolMedia = schoolMedia;
    }

    public Integer getId() {
        return _id;
    }

    public void setId(Integer id) {
        _id = id;
    }

    public String getNote() {
        return _note;
    }

    public void setNote(String note) {
        _note = note;
    }

    public String getStatus() {
        return _status;
    }

    public void setStatus(String status) {
        _status = status;
    }

    public boolean isCancel() {
        return _cancel;
    }

    public void setCancel(boolean cancel) {
        _cancel = cancel;
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
}
