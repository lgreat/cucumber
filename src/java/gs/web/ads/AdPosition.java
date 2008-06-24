/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: AdPosition.java,v 1.25 2008/06/24 17:50:12 yfan Exp $
 */
package gs.web.ads;

import org.apache.commons.lang.enums.Enum;

import java.util.List;

/**
 * Represent Ad Positions
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class AdPosition extends Enum {

    /**
     * School profile pages - GreatSchools house ad
     */
    public static final AdPosition X_11 = new AdPosition("x11", false);

    /**
     * skyscraper 120x600
     */
    public static final AdPosition X_20 = new AdPosition("x20", false);

    /**
     * top banner 728x90
     */
    public static final AdPosition X_22 = new AdPosition("x22", false);

    /**
     * footer 728x90
     */
    public static final AdPosition X_24 = new AdPosition("x24", false);


    /**
     * footer 728x90 on perl article pages
     */
    public static final AdPosition X_25 = new AdPosition("x25", false);


    /**
     * skyscraper 160x600
     */
    public static final AdPosition X_33 = new AdPosition("x33", false);

    /**
     * box ad 300x250
     */
    public static final AdPosition X_40 = new AdPosition("x40", false);


    /**
     * expando ad 300x250 or 300x600
     */
    public static final AdPosition X_47 = new AdPosition("x47", false);

    /**
     * First box ad on school profile pages 300x250
     */
    public static final AdPosition X_48 = new AdPosition("x48", false);

    /**
     * text ad
     */
    public static final AdPosition X_49 = new AdPosition("x49", false);

/**
     * text ad
     */
    public static final AdPosition X_50 = new AdPosition("x50", false);

    /**
     * text ad on editorial pages...contact ad team for latest details
     */
    public static final AdPosition X_51 = new AdPosition("x51", false);

    /**
     * box ad on city pages 300x250
     */
    public static final AdPosition X_66 = new AdPosition("x66", false);


    /**
     * 2nd box ad on school profile pages. See X_48
     */
    public static final AdPosition X_67 = new AdPosition("x67", false);

    /**
     * box ad can either be 300x250 or 300x600
     */
    public static final AdPosition X_70 = new AdPosition("x70", false);

    /**
     * Google Ad Manager Position
     */
    public static final AdPosition Top_300x137 = new AdPosition("Top_300x137", true);

    public static final AdPosition House_Ad_300x137 = new AdPosition("House_Ad_300x137", true);

    public static final AdPosition Header_728x90 = new AdPosition("Header_728x90", true);
    public static final AdPosition Header_728x90_A_Test = new AdPosition("Header_728x90_A_Test", true, Header_728x90);
    public static final AdPosition Header_728x90_B_Test = new AdPosition("Header_728x90_B_Test", true, Header_728x90);
    public static final AdPosition Header_728x90_C_Test = new AdPosition("Header_728x90_C_Test", true, Header_728x90);

    public static final AdPosition Footer_728x90 = new AdPosition("Footer_728x90", true);

    public static final AdPosition AboveFold_300x125 = new AdPosition("AboveFold_300x125", true);

    public static final AdPosition AboveFold_300x250 = new AdPosition("AboveFold_300x250", true);
    public static final AdPosition AboveFold_300x250_A_Test = new AdPosition("AboveFold_300x250_A_Test", true, AboveFold_300x250);
    public static final AdPosition AboveFold_300x250_B_Test = new AdPosition("AboveFold_300x250_B_Test", true, AboveFold_300x250);
    public static final AdPosition AboveFold_300x250_C_Test = new AdPosition("AboveFold_300x250_C_Test", true, AboveFold_300x250);

    public static final AdPosition Homepage_300x250 = new AdPosition("Homepage_300x250", true);
    // created for GS-6479
    public static final AdPosition Homepage_185x28 = new AdPosition("Homepage_185x28", true);

    public static final AdPosition AboveFold_300x600 = new AdPosition("AboveFold_300x600", true);

    public static final AdPosition BelowFold_300x250 = new AdPosition("BelowFold_300x250", true);

    public static final AdPosition BelowFold_Top_300x125 = new AdPosition("BelowFold_Top_300x125", true);

    public static final AdPosition BelowFold_Bottom_300x125 = new AdPosition("BelowFold_Bottom_300x125", true);

    public static final AdPosition AboveFold_Right_120x600 = new AdPosition("AboveFold_Right_120x600", true);

    public static final AdPosition AboveFold_Left_160x600 = new AdPosition("AboveFold_Left_160x600", true);
    public static final AdPosition AboveFold_Left_160x600_A_Test = new AdPosition("AboveFold_Left_160x600_A_Test", true, AboveFold_Left_160x600);
    public static final AdPosition AboveFold_Left_160x600_B_Test = new AdPosition("AboveFold_Left_160x600_B_Test", true, AboveFold_Left_160x600);
    public static final AdPosition AboveFold_Left_160x600_C_Test = new AdPosition("AboveFold_Left_160x600_C_Test", true, AboveFold_Left_160x600);
    public static final AdPosition Interstitial = new AdPosition("Interstitial", true);

    // The following 3 were created for GS-6114
    public static final AdPosition Interstitial_RC = new AdPosition("Interstitial_RC", true);
    public static final AdPosition Interstitial_City = new AdPosition("Interstitial_City", true);
    public static final AdPosition Interstitial_Search = new AdPosition("Interstitial_Search", true);

    public static final AdPosition PageSponsor_99x40 = new AdPosition("PageSponsor_99x40", true);
    public static final AdPosition ModuleBoxLeft_166x45 = new AdPosition("ModuleBoxLeft_166x45", true);
    public static final AdPosition ModuleBoxMiddle_166x45 = new AdPosition("ModuleBoxMiddle_166x45", true);
    public static final AdPosition ModuleBoxRight_166x45 = new AdPosition("ModuleBoxRight_166x45", true);
    public static final AdPosition BoxLeft_166x45 = new AdPosition("BoxLeft_166x45", true);
    public static final AdPosition BoxRight_166x45 = new AdPosition("BoxRight_166x45", true);

    private boolean _isGAMPosition;
    private String _baseName = null;

    private boolean _isActive;

    private AdPosition(String s, boolean isGamControlled) {
        super(s);
        _isGAMPosition = isGamControlled;
    }

    public AdPosition(String s, boolean isGamControlled, AdPosition baseAdPosition) {
        this(s,isGamControlled);
        _baseName = baseAdPosition.getName();
    }

    /**
     *
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
     *
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


