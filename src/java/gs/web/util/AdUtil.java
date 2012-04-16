package gs.web.util;

import gs.data.state.StateManager;
import gs.web.ads.AdPosition;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

public class AdUtil {
    final private static StateManager STATE_MANAGER = new StateManager();

    final private static String K12_CLICK_THROUGH_URL_PREFIX = "http://ww2.k12.com/cm/?affl=gr8t";
    final private static String OTHER_TRAFFIC_DRIVER = "ot";

    /**
     * Return the click-through url to use on ads to send users to specific pages of the k12.com site
     * @param referrer e.g. http://www.greatschools.org/search/search.page?search_type=0&q=san+francisco&state=CA&c=school
     * @param k12School e.g. CA, INT
     * @param trafficDriverParam e.g. ot, sr, rc, cp, so, ar
     * @return url
     */
    public static String getK12ClickThroughUrl(String referrer, String k12School, String trafficDriverParam) {
        boolean hasReferrer = StringUtils.isNotBlank(referrer);

        String page;
        if (hasReferrer && trafficDriverParam != null && trafficDriverParam.matches("^\\w{2}$")) {
            page = trafficDriverParam;
        } else {
            page = OTHER_TRAFFIC_DRIVER;
        }

        String school = (StringUtils.isNotBlank(k12School) ? k12School : "INT");
        if (!"INT".equals(school) && STATE_MANAGER.getState(school) == null) {
            school = "INT";
        }

        return K12_CLICK_THROUGH_URL_PREFIX + "&page=" + page + "&school=" + school;
    }

    public static void adPositionAdNameHelper(Map<String,AdPosition> adNameAdPositionMap, List<String> adNames,
                                              AdPosition adPosition, String slotPrefix, boolean includeSlotPrefix) {
        if (adNameAdPositionMap == null || adNames == null || adPosition == null) {
            throw new IllegalArgumentException("Required argument is null; the only argument that can be null is slotPrefix");
        }
        String adName = (includeSlotPrefix && slotPrefix != null ? slotPrefix : "") + adPosition.getName();
        adNameAdPositionMap.put(adName, adPosition);
        adNames.add(adName);
    }
}
