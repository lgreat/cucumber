/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: AdPosition.java,v 1.4 2006/10/06 23:43:37 chriskimm Exp $
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
     * skyscraper 120x600
     */
    public static final AdPosition X_20 = new AdPosition("x20");

    /**
     * top banner 728x90
     */
    public static final AdPosition X_22 = new AdPosition("x22");

    /**
     * footer 728x90
     */
    public static final AdPosition X_24 = new AdPosition("x24");

    /**
     * skyscraper 160x600
     */
    public static final AdPosition X_33 = new AdPosition("x33");

    /**
     * box ad 300x250
     */
    public static final AdPosition X_40 = new AdPosition("x40");

    /**
     * box ad on school profile pages 300x250
     */
    public static final AdPosition X_48 = new AdPosition("x48");

    /**
     * text ad
     */
    public static final AdPosition X_49 = new AdPosition("x49");

/**
     * text ad
     */
    public static final AdPosition X_50 = new AdPosition("x50");

    /**
     * text ad on editorial pages...contact ad team for latest details
     */
    public static final AdPosition X_51 = new AdPosition("x51");

    /**
     * box ad on city pages 300x250
     */
    public static final AdPosition X_66 = new AdPosition("x66");


    private AdPosition(String s) {
        super(s);
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
}
