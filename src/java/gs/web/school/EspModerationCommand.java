package gs.web.school;

import java.util.List;

public class EspModerationCommand {
    private List<String> _espMembershipIds;
    private String _moderatorAction;
    private List<String> _note;

    public List<String> getEspMembershipIds() {
        return _espMembershipIds;
    }

    public void setEspMembershipIds(List<String> espMembershipIds) {
        _espMembershipIds = espMembershipIds;
    }

    public String getModeratorAction() {
        return _moderatorAction;
    }

    public void setModeratorAction(String moderatorAction) {
        _moderatorAction = moderatorAction;
    }

    public List<String> getNote() {
        return _note;
    }

    public void setNote(List<String> note) {
        _note = note;
    }
}