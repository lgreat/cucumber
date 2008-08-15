package gs.web.jsp;

public class YesNoSelectorTagHandler extends SelectorTagHandler {
    private static String[] OPTION_VALUES = new String[] {"Yes", "No"};
    private static String[] OPTION_DISPLAY_NAMES = OPTION_VALUES.clone();

    public String[] getOptionValues() {
        return OPTION_VALUES;
    }

    public String[] getOptionDisplayNames() {
        return OPTION_DISPLAY_NAMES;
    }
}
