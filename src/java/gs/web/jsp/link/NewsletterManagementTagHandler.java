/**
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: NewsletterManagementTagHandler.java,v 1.3 2009/06/11 17:32:23 aroy Exp $
 */
package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import gs.data.state.State;

/**
 * Generate a tag to the page where the user manages their newsletters.
 */
public class NewsletterManagementTagHandler extends LinkTagHandler {
    private String _email;
    private Integer _schoolId;
    private State _schoolState;

    protected UrlBuilder createUrlBuilder() {

        UrlBuilder builder = new UrlBuilder(UrlBuilder.NEWSLETTER_MANAGEMENT, getState(), _email);

        if (_schoolState != null && _schoolId != null) {
            builder.setParameter("schoolState", _schoolState.getAbbreviation());
            builder.setParameter("schoolId", String.valueOf(_schoolId));
        }

        return builder;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public Integer getSchoolId() {
        return _schoolId;
    }

    public void setSchoolId(Integer schoolId) {
        _schoolId = schoolId;
    }

    public State getSchoolState() {
        return _schoolState;
    }

    public void setSchoolState(State schoolState) {
        _schoolState = schoolState;
    }
}
