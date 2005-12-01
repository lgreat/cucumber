/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: UnorderedListModel.java,v 1.1 2005/12/01 20:32:52 apeterson Exp $
 */

package gs.web.util;

import java.util.Collection;

/**
 * Provides the model of what the unorderedList.jspx page wants.
 * This will become an actual class that fleshes out behavior, but right
 * now it is just the constants that are used.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class UnorderedListModel {
    String _header;

    /**
     * Collection of Anchor objects.
     */
    Collection _results;

    public static final String HEAD = "header";

    /**
     * Collection of Anchor objects.
     */
    public static final String RESULTS = "results";

}
