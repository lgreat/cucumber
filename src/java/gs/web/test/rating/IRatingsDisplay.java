/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: IRatingsDisplay.java,v 1.1 2006/09/26 23:22:17 apeterson Exp $
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
         * @deprecated use #getRows()
         */
        IRow getRow(int i);

        /**
         * Returns list of rows. This should be non-null and non-empty.
         */
        List getRows();

        interface IRow {
            String getLabel();

            /**
             * Returns null if there is no rating, otherwise the rating 1 - 10.
             */
            Integer getRating(int subjectGroupIndex);

            /**
             * Change from the previous values. Null value means that no trend is available. This is a raw difference
             * between the rankings, as high as 9 (10-1).
             */
            Integer getTrend(int subjectGroupIndex);
        }
    }
}
