/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: Anchor.java,v 1.3 2006/04/07 02:47:55 apeterson Exp $
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
    private final String _image;

    /**
     * Constructor.
     *
     * @param href     site-relative href
     * @param contents contents of the anchor tag
     */
    public Anchor(String href, String contents) {
        _href = href;
        _contents = contents;
        _styleClass = null;
        _image = null;
    }

    /**
     * Constructor.
     *
     * @param href     site-relative href
     * @param contents contents of the anchor tag
     */
    public Anchor(String href, String contents, String styleClass) {
        _href = href;
        _contents = contents;
        _styleClass = styleClass;
        _image = null;
    }

    /**
     * Constructor.
     *
     * @param href     site-relative href
     * @param contents contents of the anchor tag
     */
    public Anchor(String href, String contents, String styleClass, String image) {
        _href = href;
        _contents = contents;
        _styleClass = styleClass;
        _image = image;
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

    public String getImage() {
        return _image;
    }
}
