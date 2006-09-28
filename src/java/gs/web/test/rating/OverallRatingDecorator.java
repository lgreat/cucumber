/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: OverallRatingDecorator.java,v 1.1 2006/09/28 01:04:16 dlee Exp $
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

                for (int i = 0; i < subjects.size(); i++) {
                    Integer rating = row.getRating(i);

                    if (null != rating) {
                        sumRatings += rating.intValue();
                        subjectsWithRating++;
                    }
                }

                Integer averageRating = null;
                Integer trend = null;
                if (subjectsWithRating != 0) {
                    averageRating = new Integer(sumRatings / subjectsWithRating);
                    //todo trend
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
        private Integer _rating;
        private Integer _trend;

        Row(String label, Integer rating, Integer trend) {
            _label = label;
            _rating = rating;
            _trend = trend;
        }

        public String getLabel() {
            return _label;
        }

        public Integer getRating(int subjectGroupIndex) {
            return _rating;
        }

        public Integer getTrend(int subjectGroupIndex) {
            return _trend;
        }
    }
}
