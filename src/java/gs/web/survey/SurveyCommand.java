package gs.web.survey;

import gs.data.survey.UserResponse;

import java.util.List;

public class SurveyCommand {

    private List<UserResponse> _responses;

    public List<UserResponse> getResponses() {
        return _responses;
    }

    public void setResponses(List<UserResponse> responses) {
        _responses = responses;
    }
}

