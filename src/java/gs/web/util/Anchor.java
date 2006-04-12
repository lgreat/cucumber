/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: Anchor.java,v 1.4 2006/04/12 17:37:34 apeterson Exp $
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
    private final String _image;
    private String _styleClass; // CSS class, or null

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

    public void setStyleClass(String styleClass) {
        _styleClass = styleClass;
    }

    public String getImage() {
        return _image;
    }
}
