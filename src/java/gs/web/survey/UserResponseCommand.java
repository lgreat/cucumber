package gs.web.survey;

import gs.data.community.User;
import gs.data.school.School;
import gs.data.survey.Survey;
import gs.data.survey.UserResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserResponseCommand {

    private Map<String, UserResponse> _responseMap;
    private User _user;
    private Survey _survey;
    private School _school;
    private String _email;
    private int _year;

    public UserResponseCommand () {
        _responseMap = new HashMap<String, UserResponse>();
    }

    public List<UserResponse> getResponses() {
        return new ArrayList<UserResponse>(_responseMap.values());
    }

    public Map<String, UserResponse> getResponseMap() {
        return _responseMap;
    }

    public void addToResponseMap(final UserResponse ur) {
        String key = "q" + ur.getQuestionId() + "a" + ur.getAnswerId();
        _responseMap.put(key,ur);
    }

    public User getUser() {
        return _user;
    }

    public void setUser(User user) {
        _user = user;
    }

    public Survey getSurvey() {
        return _survey;
    }

    public void setSurvey(Survey survey) {
        _survey = survey;
    }

    public School getSchool() {
        return _school;
    }

    public void setSchool(School school) {
        _school = school;
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public int getYear() {
        return _year;
    }

    public void setYear(int year) {
        _year = year;
    }
}

