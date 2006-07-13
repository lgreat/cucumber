/*
 * Copyright (c) 2005 GreatSchools.net. All Rights Reserved.
 * $Id: Anchor.java,v 1.1 2006/07/13 07:52:30 apeterson Exp $
 */

package gs.web.util.list;

import gs.web.util.UrlBuilder;

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
    private String _before; // text that is drawn before the link
    private String _after; // text that is drawn after the link

    /**
     * Constructor.
     *
     * @param href     site-relative href, not Xml encoded
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
     * @param href     site-relative href, not Xml encoded
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
     * @param href     site-relative href, not Xml encoded
     * @param contents contents of the anchor tag
     */
    public Anchor(String href, String contents, String styleClass, String image) {
        _href = href;
        _contents = contents;
        _styleClass = styleClass;
        _image = image;
    }

    /**
     * The Href, not Xml encoded
     */
    public String getHref() {
        return _href;
    }

    /**
     * Encodes the Href suitable for direct writing to an XHTML web page.
     */
    public String getHrefXml() {
        return UrlBuilder.encodeForXml(_href);
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

    public String getBefore() {
        return _before;
    }

    public void setBefore(String before) {
        _before = before;
    }

    public String getAfter() {
        return _after;
    }

    public void setAfter(String after) {
        _after = after;
    }

    public String asATag() {
        return "<a" +
                " href=\"" +
                _href +
                "\">" +
                _contents +
                "</a>";
    }
}
