package gs.web.util;

import gs.data.school.School;
import gs.web.ads.AdPosition;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdUtil {
    final private static String K12_CLICK_THROUGH_URL_PREFIX = "http://ww2.k12.com/cm/?affl=gr8t";

    /**
     * Return the click-through url to use on ads to send users to specific pages of the k12.com site
     * @param school e.g. cava, txva
     * @param page e.g. ot, sr, rc, cp, so, ar
     * @return url
     */
    public static String getK12ClickThroughUrl(String school, String page) {
        if (StringUtils.length(school) > 4 && school.matches(".*\\.(com|net|org|edu|gov|biz)$")) {
            return K12_CLICK_THROUGH_URL_PREFIX + "&page=" + page + "&url=" + school;
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

    // GS-13689 this map contains an entry for each K12 school that should have an affiliate link in place of the regular school website link
    private static Map<String,String> k12SchoolCodeMap = new HashMap<String,String>();
    static {
        k12SchoolCodeMap.put("AK-941", "AKVA"); // Alaska Virtual Academy
        k12SchoolCodeMap.put("AK-942", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("AK-943", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("AK-944", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("AL-3687", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("AL-3688", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("AL-3689", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("AR-3207", "ARVA"); // Arkansas Virtual Academy
        k12SchoolCodeMap.put("AR-3260", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("AR-3261", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("AR-3262", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("AZ-2805", "AZVA"); // Arizona Virtual Academy
        k12SchoolCodeMap.put("AZ-5565", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("AZ-5566", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("AZ-5567", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("CA-12222", "CAVA"); // California Virtual Academy @ San Diego School
        k12SchoolCodeMap.put("CA-12556", "CAVA"); // California Virtual Academy @ Jamestown School
        k12SchoolCodeMap.put("CA-14827", "CAVA"); // California Virtual Academy @ Sonoma School
        k12SchoolCodeMap.put("CA-17024", "CAVA"); // California Virtual Academy @ Kings School
        k12SchoolCodeMap.put("CA-17025", "CAVA"); // California Virtual Academy @ Los Angeles School
        k12SchoolCodeMap.put("CA-17026", "CAVA"); // California Virtual Academy @ San Mateo School
        k12SchoolCodeMap.put("CA-17027", "CAVA"); // California Virtual Academy @ Sutter School
        k12SchoolCodeMap.put("CA-24782", "canb.insightschools.net"); // Insight School of California - North Bay
        k12SchoolCodeMap.put("CA-25269", "SFFLEX"); // San Francisco Flex Academy
        k12SchoolCodeMap.put("CA-25449", "losangeles.iqacademyca.com"); // iQ Academy California Los Angeles
        k12SchoolCodeMap.put("CA-25515", "cala.insightschools.net"); // Insight School of California - Los Angeles
        k12SchoolCodeMap.put("CA-25516", "SVFLEX"); // Silicon Valley Flex Academy
        k12SchoolCodeMap.put("CA-25542", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("CA-25544", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("CA-25546", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("CO-2328", "COVA"); // Colorado Virtual Academy
        k12SchoolCodeMap.put("CO-4245", "co.insightschools.net"); // Insight School of Colorado
        k12SchoolCodeMap.put("CO-4250", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("CO-4251", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("CO-4252", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("CT-3180", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("CT-3181", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("CT-3182", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("DC-562", "CAPCS"); // Community Academy PCS - Online
        k12SchoolCodeMap.put("DC-857", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("DC-858", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("DC-859", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("DE-983", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("DE-984", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("DE-985", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("FL-13481", "FLVP"); // Florida Virtual Academy
        k12SchoolCodeMap.put("FL-13497", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("FL-13498", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("FL-13499", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("GA-6653", "GCA"); // Georgia Cyber Academy
        k12SchoolCodeMap.put("GA-6659", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("GA-6660", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("GA-6661", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("HI-673", "HTA"); // Hawaii Technology Academy
        k12SchoolCodeMap.put("HI-677", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("HI-678", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("HI-679", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("IA-3369", "IAVA"); // Iowa Virtual Academy
        k12SchoolCodeMap.put("IA-3370", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("IA-3371", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("IA-3372", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("ID-1360", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("ID-1361", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("ID-1362", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("ID-914", "IDVA"); // Idaho Virtual Academy
        k12SchoolCodeMap.put("IL-10239", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("IL-10240", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("IL-10241", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("IL-7579", "CVCS"); // Chicago Virtual Charter School
        k12SchoolCodeMap.put("IN-4716", "HA"); // Hoosier Academy - Indianapolis
        k12SchoolCodeMap.put("IN-4717", "HA"); // Hoosier Academy - Muncie
        k12SchoolCodeMap.put("IN-4741", "HA"); // Hoosier Academy Virtual Charter School
        k12SchoolCodeMap.put("IN-4805", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("IN-4806", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("IN-4807", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("KS-1798", "LVS"); // Lawrence Virtual School
        k12SchoolCodeMap.put("KS-2306", "ks.insightschools.net"); // Insight School of KS at Hilltop Education Center
        k12SchoolCodeMap.put("KS-2330", "LVS"); // Lawrence Virtual High School
        k12SchoolCodeMap.put("KS-2331", "iqacademyks.com"); // iQ Academy Kansas
        k12SchoolCodeMap.put("KS-2335", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("KS-2336", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("KS-2337", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("KY-4117", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("KY-4118", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("KY-4119", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("LA-4099", "LAVCA"); // Louisiana Virtual Charter Academy
        k12SchoolCodeMap.put("LA-4100", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("LA-4101", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("LA-4102", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("MA-5290", "MAVA"); // Massachusetts Virtual Academy
        k12SchoolCodeMap.put("MA-5293", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("MA-5294", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("MA-5295", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("MD-4155", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("MD-4156", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("MD-4157", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("ME-2184", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("ME-2185", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("ME-2186", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("MI-9055", "MVCA"); // Michigan Virtual Charter Academy
        k12SchoolCodeMap.put("MI-9073", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("MI-9074", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("MI-9075", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("MN-3354", "MNVA"); // Minnesota Virtual Academy
        k12SchoolCodeMap.put("MN-4253", "iqacademymn.org"); // iQ Academy Minnesota
        k12SchoolCodeMap.put("MN-5639", "mn.insightschools.net"); // Insight School of Minnesota
        k12SchoolCodeMap.put("MN-5684", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("MN-5685", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("MN-5686", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("MO-5581", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("MO-5582", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("MO-5583", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("MS-2757", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("MS-2758", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("MS-2759", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("MT-1231", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("MT-1232", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("MT-1233", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("NC-7990", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("NC-7992", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("NC-7993", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("ND-1119", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("ND-1120", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("ND-1121", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("NE-4300", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("NE-4301", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("NE-4302", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("NH-1697", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("NH-1698", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("NH-1699", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("NJ-7038", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("NJ-7039", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("NJ-7040", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("NM-1999", "NMVA"); // New Mexico Virtual Academy
        k12SchoolCodeMap.put("NM-2000", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("NM-2001", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("NM-2002", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("NV-1190", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("NV-1191", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("NV-1192", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("NV-859", "NVVA"); // Nevada Virtual Academy
        k12SchoolCodeMap.put("NY-13544", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("NY-13545", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("NY-13546", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("OH-5039", "OHVA"); // Ohio Virtual Academy
        k12SchoolCodeMap.put("OH-9905", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("OH-9906", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("OH-9907", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("OK-3492", "OKVA"); // Oklahoma Virtual Charter Academy
        k12SchoolCodeMap.put("OK-3493", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("OK-3494", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("OK-3495", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("OR-3093", "ORVA"); // Oregon Virtual Academy
        k12SchoolCodeMap.put("OR-3114", "or.insightschools.net"); // Insight School of Oregon
        k12SchoolCodeMap.put("OR-3117", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("OR-3118", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("OR-3119", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("PA-11380", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("PA-11381", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("PA-11382", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("PA-6375", "Agora"); // Agora Cyber Charter School
        k12SchoolCodeMap.put("RI-893", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("RI-894", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("RI-895", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("SC-3488", "SCVCS"); // South Carolina Virtual Charter School
        k12SchoolCodeMap.put("SC-3489", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("SC-3490", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("SC-3491", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("SD-1806", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("SD-1807", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("SD-1808", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("TN-5210", "TNVA"); // Tennessee Virtual Academy
        k12SchoolCodeMap.put("TN-5212", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("TN-5213", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("TN-5214", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("TX-20117", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("TX-20118", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("TX-20119", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("UT-1964", "UTVA"); // Utah Virtual Academy
        k12SchoolCodeMap.put("UT-2010", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("UT-2011", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("UT-2012", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("VA-5222", "VAVA"); // Virginia Virtual Academy
        k12SchoolCodeMap.put("VA-5225", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("VA-5226", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("VA-5227", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("VT-1000", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("VT-1001", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("VT-999", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("WA-3040", "iqacademywa.com"); // iQ Academy Washington
        k12SchoolCodeMap.put("WA-3740", "WAVA"); // Washington Virtual Academies
        k12SchoolCodeMap.put("WA-3787", "wa.insightschools.net"); // Insight School of Washington
        k12SchoolCodeMap.put("WA-5734", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("WA-5735", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("WA-5736", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("WI-5989", "WIVA"); // Wisconsin Virtual Academy
        k12SchoolCodeMap.put("WI-6021", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("WI-6022", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("WI-6023", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("WV-1619", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("WV-1620", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("WV-1621", "keystoneschoolonline.com"); // The Keystone School
        k12SchoolCodeMap.put("WY-1093", "WYVA"); // Wyoming Virtual Academy
        k12SchoolCodeMap.put("WY-1094", "gwuohs.com"); // George Washington University Online High School
        k12SchoolCodeMap.put("WY-1095", "INT"); // K12 International Academy
        k12SchoolCodeMap.put("WY-1096", "keystoneschoolonline.com"); // The Keystone School
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
        if (s == null || s.getId() == null || s.getDatabaseState() == null || s.getPhysicalAddress() == null
                || s.getPhysicalAddress().getState() == null || StringUtils.isBlank(trafficDriverCode)) {
            // School must have ID and state set, and traffic driver code must be specified
            return null;
        }

        String k12SchoolCode = k12SchoolCodeMap.get(s.getStateAbbreviation() + "-" + s.getId());
        if (k12SchoolCode != null) {
            return getK12ClickThroughUrl(k12SchoolCode, trafficDriverCode);
        }
        return null;
    }
}
