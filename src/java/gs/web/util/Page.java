package gs.web.util;

import java.util.List;
import java.net.URL;

/**
 * @author Chris Kimm <mailto:chriskimm@greatschools.net>
 */
public class Page {

    private String _content;
    private URL _url;

    public Page(URL url, String source) {
        this._url = url;
        this._content = source;
    }

    public String getContent() {
        return _content;
    }

    public void setContent(String content) {
        _content = content;
    }

    public URL getUrl() {
        return _url;
    }

    public void setUrl(URL url) {
        _url = url;
    }

//    public static Page parse(String s) {
//        return new Page(s);
//    }

    /**
     * Returns a List of hrefs on the page that are in the same (sub)domain.
     *
     * @return a <code>List</code> of <code>String</code>s
     */
//    public List getDomainHrefs() {
//        return null;
//    }
}
