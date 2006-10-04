/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: Cell.java,v 1.1 2006/10/04 01:05:35 dlee Exp $
 */
package gs.web.test.rating;

/**
 * Bean
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class Cell implements IRatingsDisplay.IRowGroup.IRow.ICell {
    Integer _rating;
    Integer _trend;

    public Cell(Integer rating, Integer trend) {
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
