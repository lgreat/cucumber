package gs.web.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EspModerationCommand {
    private List<Integer> _espMembershipIds = new ArrayList<Integer>();
    private String _moderatorAction;
    private Map<Integer,String> _notes = new HashMap<Integer,String>();

    public List<Integer> getEspMembershipIds() {
        return _espMembershipIds;
    }

    public void setEspMembershipIds(List<Integer> espMembershipIds) {
        _espMembershipIds = espMembershipIds;
    }

    public String getModeratorAction() {
        return _moderatorAction;
    }

    public void setModeratorAction(String moderatorAction) {
        _moderatorAction = moderatorAction;
    }

    public Map<Integer, String> getNotes() {
        return _notes;
    }

    public void setNotes(Map<Integer, String> notes) {
        _notes = notes;
    }
}