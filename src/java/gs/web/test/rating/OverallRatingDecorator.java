/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: OverallRatingDecorator.java,v 1.2 2006/09/28 21:04:45 dlee Exp $
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
        List subjects = schoolRatingsDisplay.getSubjectGroupLabels();
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
                Integer trend = null;
                if (subjectsWithRating != 0) {
                    averageRating = new Integer(sumRatings / subjectsWithRating);
                }
                decoratedRows.add(new Row(row.getLabel(), averageRating, trend));
            }
            _decoratedRowGroups.add(new RowGroup(rowGroup.getLabel(), rowGroup.getNumRows(), decoratedRows));
        }
    }


    public List getSubjectGroupLabels() {
        List subjects = new ArrayList();
        subjects.add("GreatSchools Rating");
        return subjects;
    }

    public List getRowGroups() {
        return _decoratedRowGroups;
    }

    private class RowGroup implements IRowGroup {
        private String _label;
        private int _numRows;
        private List _rows;

        RowGroup(String label, int numRows, List rows) {
            _label = label;
            _numRows = numRows;
            _rows = rows;
        }

        public String getLabel() {
            return _label;
        }

        public int getNumRows() {
            return _numRows;
        }

        public List getRows() {
            return _rows;
        }
    }

    private class Row implements IRowGroup.IRow {
        private String _label;
        private List _cells;

        Row(String label, Integer rating, Integer trend) {
            _label = label;
            _cells = new ArrayList();
            _cells.add(new Cell(rating, trend));
        }

        public String getLabel() {
            return _label;
        }

        public List getCells() {
            return _cells;
        }
    }

    private class Cell implements IRowGroup.IRow.ICell {
        Integer _rating;
        Integer _trend;

        Cell(Integer rating, Integer trend) {
            _rating = rating;
            _trend = trend;
        }

        public Integer getRating() {
            return _rating;
        }

        public Integer getTrend() {
            return _trend;
        }
    }
}
