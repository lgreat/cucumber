package gs.web.admin;

import gs.data.school.EspMembership;

/**
 * Created by IntelliJ IDEA.
 * User: rramachandran
 * Date: 3/1/12
 * Time: 3:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class EspModerationDetailsCommand {

    private EspMembership _espMembership;

    public EspMembership getEspMembership() {
        return _espMembership;
    }

    public void setEspMembership(EspMembership espMembership) {
        _espMembership = espMembership;
    }
}
