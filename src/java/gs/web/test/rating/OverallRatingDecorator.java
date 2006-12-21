/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: OverallRatingDecorator.java,v 1.11 2006/12/21 19:58:19 thuss Exp $
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

                int sumTrend = 0;
                int subjectsWithTrend = 0;

                List cells = row.getCells();

                for (Iterator cellIter = cells.iterator(); cellIter.hasNext(); ) {
                    IRowGroup.IRow.ICell cell = (IRowGroup.IRow.ICell) cellIter.next();
                    Integer rating = cell.getRating();
                    if (null != rating) {
                        sumRatings += rating.intValue();
                        subjectsWithRating++;
                    }

                    Integer trend = cell.getTrend();
                    if (null != trend) {
                        sumTrend += trend.intValue();
                        subjectsWithTrend++;
                    }
                }

                Integer averageRating = null;
                Integer trend = null;
                if (subjectsWithRating != 0) {
                    averageRating = new Integer(Math.round((float)sumRatings / (float)subjectsWithRating));
                }
                if (subjectsWithTrend != 0) {
                    trend = new Integer(Math.round((float)sumTrend / (float)subjectsWithTrend));
                }

                decoratedRows.add(new Row(row.getLabel(), averageRating, trend));
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

        Row(String label, Integer rating, Integer trend) {
            _label = label;

            if (null != trend) {
                trend = new Integer(trend.intValue());
            }

            _cells.add(new Cell(rating, trend));
        }

        public String getLabel() {
            return _label;
        }

        public List getCells() {
            return _cells;
        }
    }
}
