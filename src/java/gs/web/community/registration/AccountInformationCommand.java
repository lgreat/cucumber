package gs.web.community.registration;

import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.geo.City;
import gs.data.school.School;
import gs.data.school.Grade;
import gs.data.school.Grades;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class AccountInformationCommand {
    private StateManager _stateManager;

    private int _memberId;
    private String _gender;
    private State _state;
    private String _city;
    private String _parentAmbassador = "yes";

    private List<StudentCommand> _students;
    private List<List<City>> _cityList;
    private List<List<School>> _schools;
    private List<City> _profileCityList;

    public AccountInformationCommand() {
        _students = new ArrayList<StudentCommand>();
        _schools = new ArrayList<List<School>>();
        _cityList = new ArrayList<List<City>>();

        _stateManager = new StateManager();
    }

    public int getMemberId() {
        return _memberId;
    }

    public void setMemberId(int memberId) {
        _memberId = memberId;
    }

    public String getGender() {
        return _gender;
    }

    public void setGender(String gender) {
        _gender = gender;
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    public String getCity() {
        return _city;
    }

    public void setCity(String city) {
        _city = city;
    }

    public List<List<City>> getCityList() {
        return _cityList;
    }

    public void setCityList(List<List<City>> cityList) {
        _cityList = cityList;
    }

    public void addCityList(List<City> list) {
        getCityList().add(list);
    }

    public String getParentAmbassador() {
        return _parentAmbassador;
    }

    public void setParentAmbassador(String parentAmbassador) {
        _parentAmbassador = parentAmbassador;
    }

    public List<StudentCommand> getStudents() {
        return _students;
    }

    public void setStudents(List<StudentCommand> students) {
        _students = students;
    }

    public void addStudentCommand(StudentCommand student) {
        getStudents().add(student);
    }
    
    public int getNumStudents() {
        return getStudents().size();
    }

    public List<List<School>> getSchools() {
        return _schools;
    }

    public void setSchools(List<List<School>> schools) {
        _schools = schools;
    }

    public void addSchools(List<School> schools) {
        _schools.add(schools);
    }

    public List<City> getProfileCityList() {
        return _profileCityList;
    }

    public void setProfileCityList(List<City> profileCityList) {
        _profileCityList = profileCityList;
    }

    public List<State> getStateList() {
        return _stateManager.getListByAbbreviations();
    }

    public List<Grade> getGradeList() {
        return Grades.createGrades(Grade.PRESCHOOL, Grade.G_12).asList();
    }

    protected static class StudentCommand {
        private int _schoolId;
        private Grade _grade;
        private State _state;
        private String _city;

        public int getSchoolId() {
            return _schoolId;
        }

        public void setSchoolId(int schoolId) {
            _schoolId = schoolId;
        }

        public Grade getGrade() {
            return _grade;
        }

        public void setGrade(Grade grade) {
            _grade = grade;
        }

        public State getState() {
            return _state;
        }

        public void setState(State state) {
            _state = state;
        }

        public String getCity() {
            return _city;
        }

        public void setCity(String city) {
            _city = city;
        }
    }
}
