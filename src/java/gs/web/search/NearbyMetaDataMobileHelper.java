package gs.web.search;

import java.util.HashMap;
import java.util.Map;

/**
* Created with IntelliJ IDEA.
* User: samson
* Date: 6/4/12
* Time: 5:04 PM
* To change this template use File | Settings | File Templates.
*/
class NearbyMetaDataMobileHelper {
    public Map<String,Object> getMetaData(SchoolSearchCommandWithFields commandWithFields) {
        String searchString = commandWithFields.getSearchString();
        Map<String,Object> model = new HashMap<String,Object>();
        model.put(SchoolSearchController.MODEL_TITLE, SchoolSearchController.getTitleMobile(searchString));
        return model;
    }

    public Map<String,Object> getMetaData(Map nearbySearchInfo) {
        Map<String,Object> model = new HashMap<String,Object>();

        if (nearbySearchInfo != null) {
            Object zipCodeObj = nearbySearchInfo.get("zipCode");
            if (zipCodeObj != null && zipCodeObj instanceof String) {
                model.put(SchoolSearchController.MODEL_TITLE, SchoolSearchController.getTitleMobile((String) zipCodeObj));
            }
        }

        return model;
    }
}