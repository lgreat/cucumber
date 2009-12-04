/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: CityTagHandler.java,v 1.3 2009/12/04 20:54:11 npatury Exp $
 */

package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import gs.data.state.State;

/**
 * Provides a link to a city page.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class CityTagHandler extends LinkTagHandler {

    private State _state;
    private String _city;

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.CITY_PAGE, _state, _city);
    }

    public void setState(State state) {
        _state = state;
    }
    
    public void setCity(String city) {
        _city = city;
    }

}
