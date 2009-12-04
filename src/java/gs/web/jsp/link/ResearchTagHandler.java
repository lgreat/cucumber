/*
 * Copyright (c) 2005-2006 GreatSchools.org. All Rights Reserved.
 * $Id: ResearchTagHandler.java,v 1.3 2009/12/04 22:27:02 chriskimm Exp $
 */

package gs.web.jsp.link;

import gs.data.state.State;
import gs.web.util.UrlBuilder;

/**
 * Provides link to the "Research Home" page. This was previously known
 * as the "state home page", "choosing pathway" and "research & compare".
 *
 * @author <a href="mailto:apeterson@greatschools.org">Andrew J. Peterson</a>
 */
public class ResearchTagHandler extends LinkTagHandler {

    private State _state;

    protected UrlBuilder createUrlBuilder() {

        UrlBuilder builder = new UrlBuilder(UrlBuilder.RESEARCH, getState());
        return builder;
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }
}
