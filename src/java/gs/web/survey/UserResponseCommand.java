package gs.web.survey;

import gs.data.community.Subscription;
import gs.data.community.SubscriptionProduct;
import gs.data.community.User;
import gs.data.school.LevelCode;
import gs.data.school.School;
import gs.data.school.SchoolType;
import gs.data.school.review.Poster;
import gs.data.state.State;
import gs.data.survey.Survey;
import gs.data.survey.SurveyPage;
import gs.data.survey.UserResponse;
import gs.web.util.validator.EmailValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserResponseCommand implements EmailValidator.IEmail {

    private Map<String, UserResponse> _responseMap;
    private User _user;
    private Survey _survey;
    private School _school;
    private String _email;
    private boolean _terms = true; // default
    private int _year;
    private SurveyPage _page;
    private boolean _NLSignUpChecked = false;
    private Poster _who = Poster.PARENT; // default

    private State _prevState;
    private String _prevCity;
    private int _prevSchoolId;

    private State _nextState;
    private String _nextCity;
    private int _nextSchoolId;

    private LevelCode.Level _level;

    public UserResponseCommand () {
        _responseMap = new HashMap<String, UserResponse>();
    }

    public List<UserResponse> getResponses() {
        List<UserResponse> responses = new ArrayList<UserResponse>(_responseMap.values());
        for (UserResponse response : responses) {
            response.setSchoolId(getSchool().getId());
            response.setState(getSchool().getDatabaseState());
            response.setSurveyId(getSurvey().getId());
            response.setSurveyPageId(getPage().getId());
            response.setUserId(getUser().getId());
            response.setYear(getYear());
            response.setWho(getWho());
        }
        return responses;
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

    public boolean getTerms() {
        return _terms;
    }

    public void setTerms(boolean _terms) {
        this._terms = _terms;
    }

    public int getYear() {
        return _year;
    }

    public void setYear(int _year) {
        this._year = _year;
    }

    public SurveyPage getPage() {
        return _page;
    }

    public void setPage(SurveyPage page) {
        _page = page;
    }

        public Poster getWho() {
        return _who;
    }

    public void setWho(Poster who) {
        _who = who;
    }

    /**
     *
     * @return true if user should get a newsletter promo
     */
    public boolean isNLPromoShown() {
        if (null == _user) {
            return true;
        } else {
            if (SchoolType.PRIVATE.equals(_school.getType())) {
                Subscription sub = _user.findSubscription(SubscriptionProduct.PARENT_ADVISOR);
                return sub == null;
            } else {
                return !_user.hasMssSubscription(_school);
            }
        }
    }

    public boolean isNLSignUpChecked() {
        return _NLSignUpChecked;
    }

    public void setNLSignUpChecked(boolean NLSignUpChecked) {
        _NLSignUpChecked = NLSignUpChecked;
    }

    public State getPrevState() {
        return _prevState;
    }

    public void setPrevState(State prevState) {
        _prevState = prevState;
    }

    public String getPrevCity() {
        return _prevCity;
    }

    public void setPrevCity(String prevCity) {
        _prevCity = prevCity;
    }

    public int getPrevSchoolId() {
        return _prevSchoolId;
    }

    public void setPrevSchoolId(int prevSchoolId) {
        _prevSchoolId = prevSchoolId;
    }

    public State getNextState() {
        return _nextState;
    }

    public void setNextState(State nextState) {
        _nextState = nextState;
    }

    public String getNextCity() {
        return _nextCity;
    }

    public void setNextCity(String nextCity) {
        _nextCity = nextCity;
    }

    public int getNextSchoolId() {
        return _nextSchoolId;
    }

    public void setNextSchoolId(int nextSchoolId) {
        _nextSchoolId = nextSchoolId;
    }

    public LevelCode.Level getLevel() {
        return _level;
    }

    public void setLevel(LevelCode.Level level) {
        _level = level;
    }
}

