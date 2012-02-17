package gs.web.jsp.about.feedback;

import gs.web.jsp.SelectorTagHandler;

/**
 * Created by IntelliJ IDEA.
 * User: eddie
 * Date: 12/23/11
 * Time: 2:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class GenericSelectorTagHandler extends SelectorTagHandler {

    private String[] _optionValues;
    private String[] _optionDisplayNames;

    public GenericSelectorTagHandler(String[] optionValues,String[] optionDisplayNames) {
        _optionValues = optionValues;
        _optionDisplayNames = optionDisplayNames == null ? _optionValues.clone() : optionDisplayNames;
    }

    public String[] getOptionValues() {
        return _optionValues;
    }

    public String[] getOptionDisplayNames() {
        return _optionDisplayNames;
    }
}