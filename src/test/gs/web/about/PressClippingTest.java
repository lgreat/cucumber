package gs.web.about;

import gs.web.BaseTestCase;

/**
 * This class tests the setters and getters of PressClipping
 * @author <a href="yfan@greatschools.org">Young Fan</a>
 */
public class PressClippingTest extends BaseTestCase {
    private PressClipping _item;
    final private static String _text = "text";
    final private static String _url = "url";
    final private static String _source = "source";
    final private static String _date = "date";

    public void setUp() {
        _item = new PressClipping();
        _item.setText(_text);
        _item.setUrl(_url);
        _item.setSource(_source);
        _item.setDate(_date);
    }
    public void testText() {
        assertSame("Expected text: " + _text, _text, _item.getText());
    }

    public void testUrl() {
        assertSame("Expected url: " + _url, _url, _item.getUrl());
    }

    public void testSource() {
        assertSame("Expected source: " + _source, _source, _item.getSource());
    }

    public void testDate() {
        assertSame("Expected date: " + _date, _date, _item.getDate());
    }
}
