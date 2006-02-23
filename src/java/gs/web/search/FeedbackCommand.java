package gs.web.search;

/**
 * This class represents a the data in a search feedback form as used:
 * here:  http://www.greatschools.net/search/feedback.page
 * It a simple javabean used to construct the feedback email.
 *
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class FeedbackCommand {

    public boolean test = false;
    
    private String _query;
    private String _expected;
    private String _comment;
    private String _description;
    private String _email;

    public String getState() {
        return _state;
    }

    public void setState(String state) {
        _state = state;
    }

    private String _state;

    public String getQuery() {
        return _query;
    }

    public void setQuery(String query) {
        _query = query;
    }

    public String getExpected() {
        return _expected;
    }

    public void setExpected(String expected) {
        _expected = expected;
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public void setComment(String s) {
        _comment = s;
    }

    public String getComment() {
        return _comment;
    }

    public void setDescription(String s) {
        _description = s;
    }

    public String getDescription() {
        return _description;
    }
}
