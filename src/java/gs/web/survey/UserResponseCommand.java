package gs.web.survey;

import gs.data.survey.UserResponse;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class UserResponseCommand {

    private Map<String, UserResponse> _responseMap;
    private List<UserResponse> _responses = new ArrayList<UserResponse>();

    public UserResponseCommand () {
        _responseMap = new HashMap<String, UserResponse>();
        _responseMap.put("default", new UserResponse());
    }
    public List<UserResponse> getResponses() {
        return _responses;
    }

    public void setResponses(List<UserResponse> responses) {
        _responses = responses;
    }

    public Map<String, UserResponse> getResponseMap() {
        return _responseMap;
    }

    public void addToResponseMap(String id, UserResponse ur) {
        _responseMap.put(id, ur);
    }
}

