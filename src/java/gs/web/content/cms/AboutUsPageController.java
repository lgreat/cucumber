package gs.web.content.cms;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

public class AboutUsPageController extends CmsPageController {

    private String _leftNavSelectedItem = null;

    @Override
    public void populateModel(Map<String, Object> model) {
        if (StringUtils.isNotBlank(_leftNavSelectedItem)) {
            model.put("leftNavSelectedItem", _leftNavSelectedItem);
        }
    }

    public void setLeftNavSelectedItem(String leftNavSelectedItem) {
        _leftNavSelectedItem = leftNavSelectedItem;
    }
}