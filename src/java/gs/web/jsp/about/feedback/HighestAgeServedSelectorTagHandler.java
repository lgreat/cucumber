package gs.web.jsp.about.feedback;

import gs.web.jsp.SelectorTagHandler;

public class HighestAgeServedSelectorTagHandler extends SelectorTagHandler {
    private static String[] OPTION_VALUES = new String[] {"1", "2", "3", "4", "5", "6", "7", "Up"};
    private static String[] OPTION_DISPLAY_NAMES = OPTION_VALUES.clone();

    public String[] getOptionValues() {
        return OPTION_VALUES;
    }

    public String[] getOptionDisplayNames() {
        return OPTION_DISPLAY_NAMES;
    }
}
