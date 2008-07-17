package gs.web.about;

/**
 * @author <a href="yfan@greatschools.net">Young Fan</a>
 */
public class PressClipping {
    private String _text;
    private String _url;
    private String _source;
    private String _date;

    public String getText() {
        return _text;
    }

    public void setText(String text) {
        _text = text;
    }

    public String getUrl() {
        return _url;
    }

    public void setUrl(String url) {
        _url = url;
    }

    public String getSource() {
        return _source;
    }

    public void setSource(String source) {
        _source = source;
    }

    public String getDate() {
        return _date;
    }

    public void setDate(String date) {
        _date = date;
    }
}
