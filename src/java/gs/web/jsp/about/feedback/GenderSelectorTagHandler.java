package gs.web.jsp.about.feedback;

import gs.web.jsp.SelectorTagHandler;

public class GenderSelectorTagHandler extends SelectorTagHandler {
    private static String[] OPTION_VALUES = new String[] {"coed", "all female", "all male"};
    private static String[] OPTION_DISPLAY_NAMES = OPTION_VALUES.clone();

    public String[] getOptionValues() {
        return OPTION_VALUES;
    }

    public String[] getOptionDisplayNames() {
        return OPTION_DISPLAY_NAMES;
    }
}
