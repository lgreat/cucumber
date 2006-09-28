/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: IRatingsDisplay.java,v 1.3 2006/09/28 21:04:45 dlee Exp $
 */

package gs.web.test.rating;

import java.util.List;

/**
 * Provides clean interface for building the web page. Should support minimal logic within the display code.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 * @author <a href="mailto:dlee@greatschools.net">David Lee</a>
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

                /**
                 * Change from the previous values. Null value means that no trend is available. This is a raw difference
                 * between the rankings, as high as 9 (10-1).
                 */
                Integer getTrend();
            }
        }
    }
}
