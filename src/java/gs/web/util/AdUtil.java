package gs.web.util;

import gs.data.school.School;
import gs.data.state.StateManager;
import gs.web.ads.AdPosition;
import gs.web.promo.K12AdvertiserPageHelper;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdUtil {
    final private static StateManager STATE_MANAGER = new StateManager();

    final private static String K12_CLICK_THROUGH_URL_PREFIX = "http://ww2.k12.com/cm/?affl=gr8t";

    /**
     * Return the click-through url to use on ads to send users to specific pages of the k12.com site
     * @param school e.g. cava, txva
     * @param page e.g. ot, sr, rc, cp, so, ar
     * @return url
     */
    public static String getK12ClickThroughUrl(String school, String page) {
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

    // GS-13689 this map contains an entry for each K12 school that should have an affiliate link in place of the regular school website link
    private static Map<String,String> k12SchoolCodeMap = new HashMap<String,String>();
    static {
        // TODO-13689 need to add complete list of K12 schools that should be in this map; commented-out Il-7579 and CA-1 are examples only
        //k12SchoolCodeMap.put("IL-7579","chi");
        //k12SchoolCodeMap.put("CA-1","sfflex");
    }

    /**
     * Return the K12 school code for the specified school, if it exists. A K12 school code will only be
     * returned if the school profile page for that school should have an affiliate link displayed in place
     * of the regular school website link.
     * @param s the school for which to attempt to return a K12 school code. The school object must have ID and database state populated.
     * @param trafficDriverCode a two-letter string indicating the traffic driver
     * @return a K12 school code if the specified school should have an affiliate link displayed on the school profile page;
     *         otherwise, null is returned
     */
    public static String getK12AffiliateLinkForSchool(School s, String trafficDriverCode) {
        if (s == null || s.getId() == null || s.getDatabaseState() == null || StringUtils.isBlank(trafficDriverCode)) {
            throw new IllegalArgumentException("School must have ID and state set, and traffic driver code must be specified");
        }

        String k12SchoolCode = k12SchoolCodeMap.get(s.getStateAbbreviation() + "-" + s.getId());
        if (k12SchoolCode != null) {
            return getK12ClickThroughUrl(k12SchoolCode, trafficDriverCode);
        }
        return null;
    }
}
