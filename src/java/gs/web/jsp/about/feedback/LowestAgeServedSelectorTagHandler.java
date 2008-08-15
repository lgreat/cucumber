package gs.web.jsp.about.feedback;

import gs.web.jsp.SelectorTagHandler;

public class LowestAgeServedSelectorTagHandler extends SelectorTagHandler {
    private static String[] OPTION_VALUES = new String[] {"0", "1", "2", "3", "4", "5"};
    private static String[] OPTION_DISPLAY_NAMES = OPTION_VALUES.clone();

    public String[] getOptionValues() {
        return OPTION_VALUES;
    }

    public String[] getOptionDisplayNames() {
        return OPTION_DISPLAY_NAMES;
    }
}
