/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: OverallRatingDecorator.java,v 1.12 2007/02/06 19:07:00 dlee Exp $
 */
package gs.web.test.rating;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Decorate SchoolRatingsDisplay
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class OverallRatingDecorator implements IRatingsDisplay {
    protected final Log _log = LogFactory.getLog(getClass());

    private List _decoratedRowGroups;

    public OverallRatingDecorator(final SchoolRatingsDisplay schoolRatingsDisplay) {
        List rowGroups = schoolRatingsDisplay.getRowGroups();
        _decoratedRowGroups = new ArrayList();

        for (Iterator rowGroupIter = rowGroups.iterator(); rowGroupIter.hasNext();) {
            IRowGroup rowGroup = (IRowGroup) rowGroupIter.next();
            List rows = rowGroup.getRows();
            List decoratedRows = new ArrayList();

            for (Iterator rowIter = rows.iterator(); rowIter.hasNext();) {
                IRowGroup.IRow row = (IRowGroup.IRow) rowIter.next();
                int sumRatings = 0;
                int subjectsWithRating = 0;

                List cells = row.getCells();

                for (Iterator cellIter = cells.iterator(); cellIter.hasNext(); ) {
                    IRowGroup.IRow.ICell cell = (IRowGroup.IRow.ICell) cellIter.next();
                    Integer rating = cell.getRating();
                    if (null != rating) {
                        sumRatings += rating.intValue();
                        subjectsWithRating++;
                    }
                }

                Integer averageRating = null;
                if (subjectsWithRating != 0) {
                    averageRating = new Integer(Math.round((float)sumRatings / (float)subjectsWithRating));
                }
                decoratedRows.add(new Row(row.getLabel(), averageRating));
            }
            _decoratedRowGroups.add(new RowGroup(rowGroup.getLabel(), decoratedRows));
        }
    }

    public List getSubjectGroupLabels() {
        List subjects = new ArrayList();
        subjects.add("GREATSCHOOLS<br/>RATING");
        return subjects;
    }

    public List getRowGroups() {
        return _decoratedRowGroups;
    }

    protected class Row implements IRowGroup.IRow {
        private String _label;
        private List _cells = new ArrayList();

        Row(String label, Integer rating) {
            _label = label;

            _cells.add(new Cell(rating));
        }

        public String getLabel() {
            return _label;
        }

        public List getCells() {
            return _cells;
        }
    }
}
