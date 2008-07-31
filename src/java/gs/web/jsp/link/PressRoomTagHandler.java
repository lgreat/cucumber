package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class PressRoomTagHandler extends LinkTagHandler {

    public PressRoomTagHandler() {
        super();
        setRel("nofollow");
    }

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(UrlBuilder.PRESS_ROOM, getState());
    }
}
