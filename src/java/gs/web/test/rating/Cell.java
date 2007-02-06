/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: Cell.java,v 1.2 2007/02/06 19:07:00 dlee Exp $
 */
package gs.web.test.rating;

/**
 * Bean
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class Cell implements IRatingsDisplay.IRowGroup.IRow.ICell {
    Integer _rating;

    public Cell(Integer rating) {
        _rating = rating;
    }

    public Integer getRating() {
        return _rating;
    }

}
