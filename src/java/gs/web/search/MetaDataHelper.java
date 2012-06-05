package gs.web.search;

import java.util.HashMap;
import java.util.Map;

/**
* Created with IntelliJ IDEA.
* User: samson
* Date: 6/4/12
* Time: 5:02 PM
* To change this template use File | Settings | File Templates.
*/
class MetaDataHelper {
    public Map<String,Object> getMetaData(SchoolSearchCommandWithFields commandWithFields) {
        String searchString = commandWithFields.getSearchString();
        Map<String,Object> model = new HashMap<String,Object>();
        model.put(SchoolSearchController.MODEL_TITLE, SchoolSearchController.getTitle(searchString));
        return model;
    }
}
