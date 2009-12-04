/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: Cell.java,v 1.3 2009/12/04 20:54:12 npatury Exp $
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
