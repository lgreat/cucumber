package gs.web.about;

import gs.web.BaseTestCase;

/**
 * This class tests the setters and getters of PressRelease
 * @author <a href="yfan@greatschools.net">Young Fan</a>
 */
public class PressReleaseTest extends BaseTestCase {
    private PressRelease _item;
    final private static String _text = "text";
    final private static String _url = "url";
    final private static String _date = "date";

    public void setUp() {
        _item = new PressRelease();
        _item.setText(_text);
        _item.setUrl(_url);
        _item.setDate(_date);
    }
    public void testText() {
        assertSame("Expected text: " + _text, _text, _item.getText());
    }

    public void testUrl() {
        assertSame("Expected url: " + _url, _url, _item.getUrl());
    }

    public void testDate() {
        assertSame("Expected date: " + _date, _date, _item.getDate());
    }
}
