/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: AdPosition.java,v 1.1 2006/09/19 23:31:10 dlee Exp $
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

    public static final AdPosition X_20 = new AdPosition("x20");
    public static final AdPosition X_22 = new AdPosition("x22");

    public static final AdPosition X_24 = new AdPosition("x24");
    public static final AdPosition X_33 = new AdPosition("x33");

    public static final AdPosition X_40 = new AdPosition("x40");
    public static final AdPosition X_48 = new AdPosition("x48");

    public static final AdPosition X_49 = new AdPosition("x49");
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
