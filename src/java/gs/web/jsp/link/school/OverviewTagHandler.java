/**
 * Copyright (c) 2005 GreatSchools.org. All Rights Reserved.
 * $Id: OverviewTagHandler.java,v 1.5 2011/09/15 00:36:23 ssprouse Exp $
 */
package gs.web.jsp.link.school;

import gs.web.util.UrlBuilder;

/**
 * School Overview Page
 *
 * @author David Lee <mailto:dlee@greatschools.org>
 */
public class OverviewTagHandler extends BaseSchoolTagHandler {
    private Boolean _showConfirmation;

    public OverviewTagHandler() {
        setAbsolute(true);
    }
    
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
