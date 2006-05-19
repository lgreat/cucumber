/*
 * Copyright (c) 2005-2006 GreatSchools.net. All Rights Reserved.
 * $Id: CityTagHandler.java,v 1.1 2006/05/19 17:56:14 apeterson Exp $
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


    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    public String getCity() {
        return _city;
    }

    public void setCity(String city) {
        _city = city;
    }

}
