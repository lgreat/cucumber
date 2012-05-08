/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: AdPosition.java,v 1.108 2012/05/08 22:18:19 yfan Exp $
 */
package gs.web.ads;

import org.apache.commons.lang.enums.Enum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

/**
 * Represent Ad Positions
 *
 * @author David Lee <mailto:dlee@greatschools.org>
 */
public class AdPosition extends Enum {

    /** School profile pages - GreatSchools house ad */
    public static final AdPosition X_11 = new AdPosition("x11", false);

    /** skyscraper 120x600 */
    public static final AdPosition X_20 = new AdPosition("x20", false);

    /** top banner 728x90 */
    public static final AdPosition X_22 = new AdPosition("x22", false);

    /** footer 728x90 */
    public static final AdPosition X_24 = new AdPosition("x24", false);

    /** footer 728x90 on perl article pages */
    public static final AdPosition X_25 = new AdPosition("x25", false);

    /** skyscraper 160x600 */
    public static final AdPosition X_33 = new AdPosition("x33", false);

    /** box ad 300x250 */
    public static final AdPosition X_40 = new AdPosition("x40", false);

    /** expando ad 300x250 or 300x600 */
    public static final AdPosition X_47 = new AdPosition("x47", false);

    /** First box ad on school profile pages 300x250 */
    public static final AdPosition X_48 = new AdPosition("x48", false);

    /** text ad */
    public static final AdPosition X_49 = new AdPosition("x49", false);

    /** text ad */
    public static final AdPosition X_50 = new AdPosition("x50", false);

    /** text ad on editorial pages...contact ad team for latest details */
    public static final AdPosition X_51 = new AdPosition("x51", false);

    /** box ad on city pages 300x250 */
    public static final AdPosition X_66 = new AdPosition("x66", false);

    /** 2nd box ad on school profile pages. See X_48 */
    public static final AdPosition X_67 = new AdPosition("x67", false);

    /** box ad can either be 300x250 or 300x600 */
    public static final AdPosition X_70 = new AdPosition("x70", false);

    private static Set<AdSize> interstitialSizes = new HashSet<AdSize>();
    static {
        interstitialSizes.add(new AdSize(300,137));
        interstitialSizes.add(new AdSize(640,480));
    }

    /**
     * Google Ad Manager Position
     */
    public static final AdPosition Top_300x137 = new AdPosition("Top_300x137", true);
    public static final AdPosition Bottom_300x137 = new AdPosition("Bottom_300x137", true);
    public static final AdPosition House_Ad_300x250 = new AdPosition("House_Ad_300x250", true);
    public static final AdPosition House_Ad_300x137 = new AdPosition("House_Ad_300x137", true);
    public static final AdPosition House_Ad_300x137_Special = new AdPosition("House_ad_300x137", true, House_Ad_300x137);
    public static final AdPosition House_Ad_300x137_B2S = new AdPosition("House_Ad_300x137_B2S", true);
    public static final AdPosition House_Ad_370x158 = new AdPosition("House_Ad_370x158", true);
    public static final AdPosition Badge_100x69 = new AdPosition("Badge_100x69", true);
    public static final AdPosition Header_728x90 = new AdPosition("Header_728x90", true);
    public static final AdPosition Header_728x90_A_Test = new AdPosition("Header_728x90_A_Test", true, Header_728x90);
    public static final AdPosition Header_728x90_B_Test = new AdPosition("Header_728x90_B_Test", true, Header_728x90);
    public static final AdPosition Header_728x90_C_Test = new AdPosition("Header_728x90_C_Test", true, Header_728x90);
    public static final AdPosition Footer_728x90 = new AdPosition("Footer_728x90", true);
    public static final AdPosition AboveFold_300x125 = new AdPosition("AboveFold_300x125", true);
    public static final AdPosition AboveFold_300x250 = new AdPosition("AboveFold_300x250", true);
    public static final AdPosition AboveFold2_300x250 = new AdPosition("AboveFold2_300x250", true);
    public static final AdPosition AboveFold_300x250_A_Test = new AdPosition("AboveFold_300x250_A_Test", true, AboveFold_300x250);
    public static final AdPosition AboveFold_300x250_C_Test = new AdPosition("AboveFold_300x250_C_Test", true, AboveFold_300x250);
    public static final AdPosition BelowFold_311x250 = new AdPosition("BelowFold_311x250", true);
    public static final AdPosition Homepage_300x250 = new AdPosition("Homepage_300x250", true);
    public static final AdPosition Homepage_Left_300x250 = new AdPosition("Homepage_Left_300x250", true);
    public static final AdPosition Homepage_185x28 = new AdPosition("Homepage_185x28", true); // GS-6479
    public static final AdPosition Homepage_Footer_728x90 = new AdPosition("Homepage_Footer_728x90", true); // GS-6583
    public static final AdPosition Homepage_House_Ad_598x102 = new AdPosition("Homepage_House_Ad_598x102", true); // GS-9920, GS-11663
    public static final AdPosition Homepage_House_Ad_328x150 = new AdPosition("Homepage_House_Ad_328x150", true);
    public static final AdPosition Homepage_House_Ad_300x137 = new AdPosition("Homepage_House_Ad_300x137", true);
    public static final AdPosition AboveFold_300x600 = new AdPosition("AboveFold_300x600", true);
    public static final AdPosition BelowFold_300x250 = new AdPosition("BelowFold_300x250", true);
    public static final AdPosition BelowFold2_300x250 = new AdPosition("BelowFold2_300x250", true);
    public static final AdPosition BelowFold_Top_300x125 = new AdPosition("BelowFold_Top_300x125", true);
    public static final AdPosition BelowFold_Bottom_300x125 = new AdPosition("BelowFold_Bottom_300x125", true);
    public static final AdPosition AboveFold_Right_120x600 = new AdPosition("AboveFold_Right_120x600", true, new AdSize(160,600));
    public static final AdPosition AboveFold_Left_160x600 = new AdPosition("AboveFold_Left_160x600", true);
    public static final AdPosition AboveFold_Right_160x600 = new AdPosition("AboveFold_Right_160x600", true);
    public static final AdPosition AboveFold_Left_160x600_A_Test = new AdPosition("AboveFold_Left_160x600_A_Test", true, AboveFold_Left_160x600);
    public static final AdPosition AboveFold_Left_160x600_B_Test = new AdPosition("AboveFold_Left_160x600_B_Test", true, AboveFold_Left_160x600);
    public static final AdPosition AboveFold_Left_160x600_C_Test = new AdPosition("AboveFold_Left_160x600_C_Test", true, AboveFold_Left_160x600);
    public static final AdPosition Interstitial = new AdPosition("Interstitial", true, interstitialSizes);
    public static final AdPosition Interstitial_RC = new AdPosition("Interstitial_RC", true, interstitialSizes); // GS-6114
    public static final AdPosition Interstitial_City = new AdPosition("Interstitial_City", true, interstitialSizes); // GS-6114
    public static final AdPosition Interstitial_Search = new AdPosition("Interstitial_Search", true, interstitialSizes); // GS-6114
    public static final AdPosition Interstitial_School = new AdPosition("Interstitial_School", true, interstitialSizes); // GS-7589
    public static final AdPosition Interstitial_ContentSearch = new AdPosition("Interstitial_ContentSearch", true, interstitialSizes); // GS-10345
    public static final AdPosition PageSponsor_99x40 = new AdPosition("PageSponsor_99x40", true);
    public static final AdPosition ModuleBoxLeft_166x45 = new AdPosition("ModuleBoxLeft_166x45", true);
    public static final AdPosition ModuleBoxMiddle_166x45 = new AdPosition("ModuleBoxMiddle_166x45", true);
    public static final AdPosition ModuleBoxRight_166x45 = new AdPosition("ModuleBoxRight_166x45", true);
    public static final AdPosition BoxLeft_166x45 = new AdPosition("BoxLeft_166x45", true);
    public static final AdPosition BoxRight_166x45 = new AdPosition("BoxRight_166x45", true);
    public static final AdPosition YouTube_381x311 = new AdPosition("YouTube_381x311", true); // GS-6523
    public static final AdPosition YouTube_304x211 = new AdPosition("YouTube_304x211", true); // GS-6597
    public static final AdPosition Inline_460x94 = new AdPosition("Inline_460x94", true);
    public static final AdPosition PageSponsor_122x45 = new AdPosition("PageSponsor_122x45", true);
    public static final AdPosition BTS_Quiz_Promo_300x316 = new AdPosition("BTS_Quiz_Promo_300x316", true);
    public static final AdPosition Survey_179x151 = new AdPosition("Survey_179x151", true);
    public static final AdPosition Promo_170x92 = new AdPosition("Promo_170x92", true); // GS-7166
    public static final AdPosition Generic_640 = new AdPosition("Generic_640", true); // GS-8363
    public static final AdPosition Generic_640x480 = new AdPosition("Generic_640x480", true); // GS-8429
    public static final AdPosition sponsor_210x37 = new AdPosition("sponsor_210x37", true);
    public static final AdPosition sponsor_90x45 = new AdPosition("sponsor_90x45", true);
    public static final AdPosition sponsor_120x80 = new AdPosition("sponsor_120x80", true);
    public static final AdPosition countdown_b2s_311x250 = new AdPosition("countdown_b2s_311x250", true);
    public static final AdPosition Facebook_Promo_300x250 = new AdPosition("Facebook_Promo_300x250", true); // GS-9137
    public static final AdPosition Promo_300x250 = new AdPosition("Promo_300x250", true); // GS-10582
    public static final AdPosition HeaderLogo_88x33 = new AdPosition("HeaderLogo_88x33", true);
    public static final AdPosition Inline_423x60 = new AdPosition("Inline_423x60", true);
    public static final AdPosition SponsoredSearch_Top_542x60 = new AdPosition("SponsoredSearch_Top_542x60", true); // GS-9967
    public static final AdPosition SponsoredSearch_Bottom_542x60 = new AdPosition("SponsoredSearch_Bottom_542x60", true); // GS-9967
    public static final AdPosition TopRatedSponsor_310x40 = new AdPosition("TopRatedSponsor_310x40", true); // GS-9999
    public static final AdPosition Global_NavPromo_970x30 = new AdPosition("Global_NavPromo_970x30", true); // GS-9975, GS-12744
    public static final AdPosition Global_HeaderPromo_88x31 = new AdPosition("Global_HeaderPromo_88x31", true); // GS-11618
    public static final AdPosition House_Ad_423x230 = new AdPosition("House_Ad_423x230", true); // GS-9920, GS-11663
    public static final AdPosition Inline_598x50 = new AdPosition("Inline_598x50", true); // GS-10041
    public static final AdPosition RYH_Sponsor_90x32 = new AdPosition("RYH_Sponsor_90x32", true); // GS-10166
    public static final AdPosition SponsorPromo_300x600 = new AdPosition("SponsorPromo_300x600", true); // GS-10060
    public static final AdPosition CustomSponsor_407x65 = new AdPosition("CustomSponsor_407x65", true); // GS-10414
    public static final AdPosition Community_Ad_300x50 = new AdPosition("Community_Ad_300x50", true); // GS-10357
    public static final AdPosition Sponsor_610x16 = new AdPosition("Sponsor_610x16", true); // GS-10540
    public static final AdPosition Sponsor_610x30 = new AdPosition("Sponsor_610x30", true); //GS-11847
    public static final AdPosition Sponsor_BelowFold_610x38 = new AdPosition("Sponsor_BelowFold_610x38", true); // GS-10708
    public static final AdPosition SponsoredSearch_Top_423x68 = new AdPosition("SponsoredSearch_Top_423x68", true); // GS-10772
    public static final AdPosition SponsoredSearch_Top_423x120 = new AdPosition("SponsoredSearch_Top_423x120", true); // GS-10772
    public static final AdPosition SponsoredSearch_Bottom_423x68 = new AdPosition("SponsoredSearch_Bottom_423x68", true); // GS-10772
    public static final AdPosition Sponsor_610x225 = new AdPosition("Sponsor_610x225", true); // GS-11664
    public static final AdPosition Sponsor_88x31 = new AdPosition("Sponsor_88x31", true); // GS-11708
    public static final AdPosition Homepage_FindASchool_Sponsor_88x31 = new AdPosition("Homepage_FindASchool_Sponsor_88x31", true); // GS-11619
    public static final AdPosition FindASchool_Sponsor_88x31 = new AdPosition("FindASchool_Sponsor_88x31", true); // GS-11619
    public static final AdPosition SchoolReviews_Integrated_586x30 = new AdPosition("SchoolReviews_Integrated_586x30", true); // GS-11969
    public static final AdPosition K12_Module_Logo_88x31 = new AdPosition("K12_Module_Logo_88x31", true); // GS-11927
    public static final AdPosition Custom_Peelback_Ad = new AdPosition("Custom_Peelback_Ad", true); // GS-11966
    public static final AdPosition Custom_Welcome_Ad = new AdPosition("Custom_Welcome_Ad", true); // GS-11966
    public static final AdPosition House_Ad_300x210 = new AdPosition("House_Ad_300x210", true); // GS-12049
    public static final AdPosition Sponsor_320x50 = new AdPosition("Sponsor_320x50", true); //GS-12366

    private boolean _isGAMPosition;
    private String _baseName = null;
    private Set<AdSize> _sizes = new HashSet<AdSize>();
    private Set<AdSize> _companionSizes = new HashSet<AdSize>();

    private boolean _isActive;

    private AdPosition(String s, boolean isGamControlled) {
        this(s, isGamControlled, (Set<AdSize>)null);
    }

    public AdPosition(String s, boolean isGamControlled, AdSize size) {
        super(s);
        _isGAMPosition = isGamControlled;
        Set<AdSize> sizes = new HashSet<AdSize>();
        sizes.add(size);
        _sizes = sizes;
        addCompanionSizes();
    }

    public AdPosition(String s, boolean isGamControlled, Set<AdSize> sizes) {
        super(s);
        _isGAMPosition = isGamControlled;

        if (sizes == null) {
            sizes = new HashSet<AdSize>();
        }
        if (sizes.isEmpty()) {
            sizes.add(new AdSize(getBaseName()));
        }
        _sizes = sizes;
        addCompanionSizes();
    }

    public AdPosition(String s, boolean isGamControlled, AdPosition baseAdPosition) {
        this(s, isGamControlled, baseAdPosition, null);
    }

    public AdPosition(String s, boolean isGamControlled, AdPosition baseAdPosition, Set<AdSize> sizes) {
        this(s,isGamControlled, sizes);
        _baseName = baseAdPosition.getName();
    }

    public AdSize getSize() {
        if (_sizes.size() == 1) {
            return _sizes.iterator().next();
        } else {
            throw new UnsupportedOperationException("AdPosition getSize() can only be called if there is only one size");
        }
    }

    public Set<AdSize> getSizes() {
        return _sizes;
    }

    public Set<AdSize> getCompanionSizes() {
        return _companionSizes;
    }

    public List<AdSize> getAllSizes() {
        List<AdSize> allSizes = new ArrayList<AdSize>();
        allSizes.addAll(_sizes);
        allSizes.addAll(_companionSizes);
        return allSizes;
    }

    private void addCompanionSizes() {
        Set<AdSize> companionSizes = new HashSet<AdSize>();
        for (AdSize size : _sizes) {
            AdSize companionSize = size.getCompanionSize();
            if (companionSize != null) {
                companionSizes.add(companionSize);
            }
        }
        companionSizes.removeAll(_sizes);
        _companionSizes = companionSizes;
    }

    /**
     * @param adName
     * @return AdPosition if one exists
     * @throws IllegalArgumentException
     */
    public static AdPosition getAdPosition(String adName) throws IllegalArgumentException {
        AdPosition adPosition =  (AdPosition) Enum.getEnum(AdPosition.class, adName);

        if (null == adPosition) {
            throw new IllegalArgumentException("Ad name does not exist: " + adName);
        } else {
            return adPosition;
        }
    }

    public static List getAllAdPositions() {
        return Enum.getEnumList(AdPosition.class);
    }

    /**
     * @return true if this position is served through google ad manager, false otherwise
     */
    public boolean isGAMPosition() {
        return _isGAMPosition;
    }

    /**
     * does this ad position have any campaigns running on the live site?
     * @return true if position has ads running on it.
     */
    public boolean isActive() {
        return _isActive;
    }

    /**
     * used in cobrands
     * @return
     */
    public String getBaseName(){
        if (_baseName == null ) {
            return this.getName();
        } else {
            return _baseName;
        }
    }
}