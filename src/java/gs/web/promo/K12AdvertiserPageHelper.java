package gs.web.promo;

import gs.data.state.State;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class K12AdvertiserPageHelper {

    final public static Map<String,K12SchoolInfo> K12_SCHOOL_INFO = new HashMap<String,K12SchoolInfo>();
    static {
        K12_SCHOOL_INFO.put("AK", new K12SchoolInfo("akva", "Alaska Virtual Academy", false, false));
        K12_SCHOOL_INFO.put("AR", new K12SchoolInfo("arva", "Arkansas Virtual Academy", false));
        K12_SCHOOL_INFO.put("AZ", new K12SchoolInfo("azva", "Arizona Virtual Academy"));
        K12_SCHOOL_INFO.put("CA", new K12SchoolInfo("cava", "California Virtual Academies"));
        K12_SCHOOL_INFO.put("CO", new K12SchoolInfo("cova", "Colorado Virtual Academy"));
        K12_SCHOOL_INFO.put("DC", new K12SchoolInfo("capcs", "Community Academy Public Charter School Online", false));
        K12_SCHOOL_INFO.put("FL", new K12SchoolInfo("flva", "Florida Virtual Academies", false));
        K12_SCHOOL_INFO.put("GA", new K12SchoolInfo("gca", "Georgia Cyber Academy", false));
        K12_SCHOOL_INFO.put("HI", new K12SchoolInfo("hta", "Hawaii Technology Academy", false));
        K12_SCHOOL_INFO.put("IA", new K12SchoolInfo("iava", "Iowa Virtual Academy", false, false));
        K12_SCHOOL_INFO.put("ID", new K12SchoolInfo("idva", "Idaho Virtual Academy"));
        K12_SCHOOL_INFO.put("IL", new K12SchoolInfo("cvcs", "Chicago Virtual Charter School"));
        K12_SCHOOL_INFO.put("IN", new K12SchoolInfo("ha", "Hoosier Academies", false));
        K12_SCHOOL_INFO.put("INT", new K12SchoolInfo("int", "K12 International Academy", false));
        K12_SCHOOL_INFO.put("KS", new K12SchoolInfo("lvs", "Lawrence Virtual School"));
        K12_SCHOOL_INFO.put("LA", new K12SchoolInfo("lavca", "Louisiana Virtual Charter Academy", false, false));
        K12_SCHOOL_INFO.put("MA", new K12SchoolInfo("mava", "Massachussetts Virtual Academy", false, false));
        K12_SCHOOL_INFO.put("MI", new K12SchoolInfo("mvca", "Michigan Virtual Charter Academy", false, false));
        K12_SCHOOL_INFO.put("MN", new K12SchoolInfo("mnva", "Minnesota Virtual Academy"));
        K12_SCHOOL_INFO.put("NJ", new K12SchoolInfo("njvacs", "New Jersey Virtual Academy Charter School", false, false));
        K12_SCHOOL_INFO.put("NM", new K12SchoolInfo("nmva", "New Mexico Virtual Academy", false, false));
        K12_SCHOOL_INFO.put("NV", new K12SchoolInfo("nvva", "Nevada Virtual Academy"));
        K12_SCHOOL_INFO.put("OH", new K12SchoolInfo("ohva", "Ohio Virtual Academy"));
        K12_SCHOOL_INFO.put("OK", new K12SchoolInfo("ovca", "Oklahoma Virtual Charter Academy", false));
        K12_SCHOOL_INFO.put("OR", new K12SchoolInfo("orva", "Oregon Virtual Academy", false));
        K12_SCHOOL_INFO.put("PA", new K12SchoolInfo("agora", "Agora Cyber Charter School"));
        K12_SCHOOL_INFO.put("SC", new K12SchoolInfo("scvcs", "South Carolina Virtual Charter School", false));
        K12_SCHOOL_INFO.put("TN", new K12SchoolInfo("tnva", "Tennessee Virtual Academy", false, false));
        K12_SCHOOL_INFO.put("TX", new K12SchoolInfo("txva", "Texas Virtual Academy"));
        K12_SCHOOL_INFO.put("UT", new K12SchoolInfo("utva", "Utah Virtual Academy", false));
        K12_SCHOOL_INFO.put("VA", new K12SchoolInfo("vava", "Virginia Virtual Academy", false, false));
        K12_SCHOOL_INFO.put("WA", new K12SchoolInfo("wava", "Washington Virtual Academies"));
        K12_SCHOOL_INFO.put("WI", new K12SchoolInfo("wiva", "Wisconsin Virtual Academy"));
        K12_SCHOOL_INFO.put("WY", new K12SchoolInfo("wyva", "Wyoming Virtual Academy", false));
    }

    /**
     * Per PT-128, send requests to state-specific state landing pages instead to the profile page for that
     * state's k12 school. This map contains the school id for each state's k12 school.
     *
     * California is not included because California has more than one k12 school. The controller should instead
     * redirect to a search results page.
     */
    public static final Map<State, Integer> K12_STATE_TO_SCHOOL_ID = new HashMap<State, Integer>(50) {
        {
            put(State.AK, 941);
            put(State.AL, 3688);
            put(State.AR, 3207);
            put(State.AZ, 2805);
            put(State.CO, 2328);
            put(State.CT, 3181);
            put(State.DC, 562);
            put(State.DE, 984);
            put(State.FL, 13481);
            put(State.GA, 6653);
            put(State.HI, 673);
            put(State.IA, 3369);
            put(State.ID, 914);
            put(State.IL, 7579);
            put(State.IN, 4716);
            put(State.KS, 1798);
            put(State.KY, 4118);
            put(State.LA, 4099);
            put(State.MA, 5290);
            put(State.MD, 4156);
            put(State.ME, 2185);
            put(State.MI, 9055);
            put(State.MN, 3354);
            put(State.MO, 5582);
            put(State.MS, 2758);
            put(State.MT, 1232);
            put(State.NC, 7992);
            put(State.ND, 1120);
            put(State.NE, 4301);
            put(State.NH, 1698);
            put(State.NJ, 7039);
            put(State.NM, 1999);
            put(State.NV, 859);
            put(State.NY, 13545);
            put(State.OH, 5039);
            put(State.OK, 3492);
            put(State.OR, 3093);
            put(State.PA, 6375);
            put(State.RI, 894);
            put(State.SC, 3488);
            put(State.SD, 1807);
            put(State.TN, 5210);
            put(State.TX, 20807);
            put(State.UT, 1964);
            put(State.VA, 5222);
            put(State.VT, 1000);
            put(State.WA, 3740);
            put(State.WI, 5989);
            put(State.WV, 1620);
            put(State.WY, 1093);
        }
    };

    public static String getClickthruSchoolParam(String state) {
        K12SchoolInfo schoolInfo;
        if (StringUtils.isNotBlank(state) && isValidK12School(state)) {
            schoolInfo = K12_SCHOOL_INFO.get(state);
        } else {
            schoolInfo = K12_SCHOOL_INFO.get("INT");
        }
        if (schoolInfo != null) {
            return schoolInfo.getClickthruSchoolParam();
        }
        throw new IllegalStateException("K12 school info should not be null");
    }

    public static boolean isValidK12School(String state) {
        return K12_SCHOOL_INFO.containsKey(state);
    }

    public static String getK12SchoolName(String state) {
        return K12_SCHOOL_INFO.get(state).getName();
    }

    public static boolean hasBottomCopy(String state) {
        return K12_SCHOOL_INFO.get(state).hasBottomCopy();
    }

    public static boolean hasSummary(String state) {
        return K12_SCHOOL_INFO.get(state).hasSummary();
    }

    private static class K12SchoolInfo {
        private String _clickthruSchoolParam;
        private String _name;
        private boolean _hasBottomCopy;
        private boolean _hasSummary;

        public K12SchoolInfo(String clickthruSchoolParam, String name) {
            this(clickthruSchoolParam, name, true);
        }

        public K12SchoolInfo(String clickthruSchoolParam, String name, boolean hasBottomCopy) {
            this(clickthruSchoolParam, name, hasBottomCopy, true);
        }

        public K12SchoolInfo(String clickthruSchoolParam, String name, boolean hasBottomCopy, boolean hasSummary) {
            _clickthruSchoolParam = clickthruSchoolParam;
            _name = name;
            _hasBottomCopy = hasBottomCopy;
            _hasSummary = hasSummary;
        }

        public String getClickthruSchoolParam() {
            return _clickthruSchoolParam;
        }

        public String getName() {
            return _name;
        }

        public boolean hasBottomCopy() {
            return _hasBottomCopy;
        }

        public boolean hasSummary() {
            return _hasSummary;
        }
    }
}
