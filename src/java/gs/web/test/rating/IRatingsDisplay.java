/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: IRatingsDisplay.java,v 1.6 2009/12/04 22:27:13 chriskimm Exp $
 */

package gs.web.test.rating;

import java.util.List;

/**
 * Provides clean interface for building the web page. Should support minimal logic within the display code.
 *
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
 * @author <a href="mailto:dlee@greatschools.org">David Lee</a>
 */
public interface IRatingsDisplay {

    /**
     * Currently the number of subjects, but in the future subject may be grouped together in one column, so we call
     * them subjects groups here.
     */
    List getSubjectGroupLabels(); // List of String objects

    List getRowGroups(); // list of IRowGroup objects

    interface IRowGroup {
        /**
         * Returns "By Grade", "By Category", "By Ethnicity", "By Gender"
         */
        String getLabel();

        int getNumRows();

        /**
         * Returns list of rows. This should be non-null and non-empty.
         */
        List getRows();

        interface IRow {
            String getLabel();

            /**
             * Return a list of cells.  This should be non-null and non-empty.
             * @return
             */
            List getCells();


            interface ICell {
                /**
                 * Returns null if there is no rating, otherwise the rating 1 - 10.
                 */
                Integer getRating();
            }
        }
    }
}
