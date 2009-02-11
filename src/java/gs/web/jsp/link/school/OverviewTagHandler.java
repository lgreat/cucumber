/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: OverviewTagHandler.java,v 1.2 2009/02/11 17:45:20 npatury Exp $
 */
package gs.web.jsp.link.school;

import gs.web.util.UrlBuilder;

/**
 * School Overview Page
 *
 * @author David Lee <mailto:dlee@greatschools.net>
 */
public class OverviewTagHandler extends BaseSchoolTagHandler {
    private Boolean _showConfirmation;
    
    public Boolean getShowConfirmation() {
        return _showConfirmation;
    }

    public void setShowConfirmation(Boolean showConfirmation) {
        _showConfirmation = showConfirmation;
    }


    
    protected UrlBuilder createUrlBuilder() {
        if (_showConfirmation != null) {
            return new UrlBuilder( UrlBuilder.SCHOOL_PROFILE, _showConfirmation,getSchool());
        } else {
        UrlBuilder builder = new UrlBuilder(getSchool(), UrlBuilder.SCHOOL_PROFILE);
        return builder;
    }
    }
}
