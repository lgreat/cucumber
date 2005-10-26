/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: Anchor.java,v 1.1 2005/10/26 20:51:33 apeterson Exp $
 */

package gs.web.util;

/**
 * Bean to hold attributes for an anchor tag.
 *
 * @author <a href="mailto:apeterson@greatschools.net">Andrew J. Peterson</a>
 */
public class Anchor {
    private final String _href;
    private final String _contents;
    private final String _styleClass; // CSS class, or null
    // TODO add more attributes as needed

    public Anchor(String href, String contents) {
        _href = href;
        _contents = contents;
        _styleClass = null;
    }

    public Anchor(String href, String contents, String styleClass) {
        _href = href;
        _contents = contents;
        _styleClass = styleClass;
    }

    public String getHref() {
        return _href;
    }

    public String getContents() {
        return _contents;
    }

    public String getStyleClass() {
        return _styleClass;
    }
}
