package gs.web.school;

import java.util.List;

public class EspModerationCommand {
    private List<Long> _espMembershipIds;
    private String _moderatorAction;

    public List<Long> getEspMembershipIds() {
        return _espMembershipIds;
    }

    public void setEspMembershipIds(List<Long> espMembershipIds) {
        _espMembershipIds = espMembershipIds;
    }

    public String getModeratorAction() {
        return _moderatorAction;
    }

    public void setModeratorAction(String moderatorAction) {
        _moderatorAction = moderatorAction;
    }
}