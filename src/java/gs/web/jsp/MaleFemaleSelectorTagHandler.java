package gs.web.jsp;

public class MaleFemaleSelectorTagHandler extends SelectorTagHandler {
    private static String[] OPTION_VALUES = new String[] {"m", "f"};
    private static String[] OPTION_DISPLAY_NAMES = new String[] {"Male", "Female"};

    public String[] getOptionValues() {
        return OPTION_VALUES;
    }

    public String[] getOptionDisplayNames() {
        return OPTION_DISPLAY_NAMES;
    }
}
