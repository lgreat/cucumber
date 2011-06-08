package gs.web.promo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: 6/7/11
 * Time: 6:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class K12AdvertiserPageHelper {
    /*
    // TODO-11797 - no need?
    final public static String[] VALID_K12_SCHOOLS = new String[] {
            "AR", "AZ", "CA", "CO", "DC", "FL", "GA", "HI", "ID", "IL", "IN", "INT", "KS", "MN", "MO", "NV", "OH", "OK",
            "OR", "PA", "SC", "TX", "UT", "WA", "WI", "WY"
    };
    */
    final public static Map<String,K12SchoolInfo> K12_SCHOOL_INFO = new HashMap<String,K12SchoolInfo>();
    static {
        K12_SCHOOL_INFO.put("AR", new K12SchoolInfo("Arkansas Virtual Academy", false));
        K12_SCHOOL_INFO.put("AZ", new K12SchoolInfo("Arizona Virtual Academy"));
        K12_SCHOOL_INFO.put("CA", new K12SchoolInfo("California Virtual Academies"));
        K12_SCHOOL_INFO.put("CO", new K12SchoolInfo("Colorado Virtual Academy"));
        K12_SCHOOL_INFO.put("DC", new K12SchoolInfo("DC Community Academy Public Charter School", false));
        K12_SCHOOL_INFO.put("FL", new K12SchoolInfo("Florida Virtual Program", false));
        K12_SCHOOL_INFO.put("GA", new K12SchoolInfo("Georgia Cyber Academy", false));
        K12_SCHOOL_INFO.put("HI", new K12SchoolInfo("Hawaii Technology Academy", false));
        K12_SCHOOL_INFO.put("ID", new K12SchoolInfo("Idaho Virtual Academy"));
        K12_SCHOOL_INFO.put("IL", new K12SchoolInfo("Chicago Virtual Charter School"));
        K12_SCHOOL_INFO.put("IN", new K12SchoolInfo("Indiana Virtual Pilot School", false));
        K12_SCHOOL_INFO.put("INT", new K12SchoolInfo("K12 International Academy", false));
        K12_SCHOOL_INFO.put("KS", new K12SchoolInfo("Lawrence Virtual School"));
        K12_SCHOOL_INFO.put("MN", new K12SchoolInfo("Minnesota Virtual Academy"));
        K12_SCHOOL_INFO.put("MO", new K12SchoolInfo("St. Louis Public Schools Virtual School", false));
        K12_SCHOOL_INFO.put("NV", new K12SchoolInfo("Nevada Virtual School"));
        K12_SCHOOL_INFO.put("OH", new K12SchoolInfo("Ohio Virtual Academy"));
        K12_SCHOOL_INFO.put("OK", new K12SchoolInfo("Oklahoma Virtual Academy", false));
        K12_SCHOOL_INFO.put("OR", new K12SchoolInfo("Oregon Virtual Academy", false));
        K12_SCHOOL_INFO.put("PA", new K12SchoolInfo("Agora Cyber Charter School"));
        K12_SCHOOL_INFO.put("SC", new K12SchoolInfo("South Carolina Virtual Charter School", false));
        K12_SCHOOL_INFO.put("TX", new K12SchoolInfo("Texas Virtual Academy @ Southwest"));
        K12_SCHOOL_INFO.put("UT", new K12SchoolInfo("Utah Virtual Academy", false));
        K12_SCHOOL_INFO.put("WA", new K12SchoolInfo("Washington Virtual Academies"));
        K12_SCHOOL_INFO.put("WI", new K12SchoolInfo("Wisconsin Virtual Academy"));
        K12_SCHOOL_INFO.put("WY", new K12SchoolInfo("Wyoming Virtual Academy", false));
    }

    public static boolean isValidK12School(String school) {
        return K12_SCHOOL_INFO.containsKey(school);
    }

    public static String getK12SchoolName(String school) {
        return K12_SCHOOL_INFO.get(school).getName();
    }

    public static boolean hasBottomCopy(String school) {
        return K12_SCHOOL_INFO.get(school).hasBottomCopy();
    }

    private static class K12SchoolInfo {
        private String _name;
        private boolean _hasBottomCopy = true;

        public K12SchoolInfo(String name, boolean hasBottomCopy) {
            _name = name;
            _hasBottomCopy = hasBottomCopy;
        }

        public K12SchoolInfo(String name) {
            _name = name;
        }

        public String getName() {
            return _name;
        }

        public boolean hasBottomCopy() {
            return _hasBottomCopy;
        }
    }
}
