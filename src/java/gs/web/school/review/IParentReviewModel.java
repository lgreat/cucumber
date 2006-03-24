/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: IParentReviewModel.java,v 1.2 2006/03/24 20:51:26 apeterson Exp $
 */

package gs.web.school.review;

/**
 * The purpose of this interface is...
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public interface IParentReviewModel {
    String getSchoolName();

    int  getStars();

    String getDate();

    String getQuip();
}
