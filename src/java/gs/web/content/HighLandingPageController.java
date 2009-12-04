package gs.web.content;

import java.util.Map;

/**
 * TODO: evaluate necessity of this class
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 *
 * Everything should be made as simple as possible, but not simpler. Did I go too far?
 */
public class HighLandingPageController extends BaseGradeLevelLandingPageController {

    @Override
    protected void populateModel(Map<String, Object> model) {
        super.populateModel(model);
        loadTableRowIntoModel(model, "hs_actionTipMonth");
        loadTableRowIntoModel(model, "hs_actionTipFreshman");
        loadTableRowIntoModel(model, "hs_actionTipSophomore");
        loadTableRowIntoModel(model, "hs_actionTipJunior");
        loadTableRowIntoModel(model, "hs_actionTipSenior");
    }
}