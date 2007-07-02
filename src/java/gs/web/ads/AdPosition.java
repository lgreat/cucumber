/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: AdPosition.java,v 1.13 2007/07/02 17:25:51 cpickslay Exp $
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

    public static final AdPosition Footer_728x90 = new AdPosition("Footer_728x90", true);

    public static final AdPosition AboveFold_300x250 = new AdPosition("AboveFold_300x250", true);

    public static final AdPosition AboveFold_600x250 = new AdPosition("AboveFold_300x600", true);

    public static final AdPosition BelowFold_300x250 = new AdPosition("BelowFold_300x250", true);

    public static final AdPosition BelowFold_Top_300x125 = new AdPosition("BelowFold_Top_300x125", true);

    public static final AdPosition BelowFold_Bottom_300x125 = new AdPosition("BelowFold_Bottom_300x125", true);

    public static final AdPosition AboveFold_Right_120x600 = new AdPosition("AboveFold_Right_120x600", true);

    public static final AdPosition Interstitial = new AdPosition("Interstitial", true);

    private boolean _isGAMPosition;

    private AdPosition(String s, boolean isGamControlled) {
        super(s);
        _isGAMPosition = isGamControlled;
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
}
