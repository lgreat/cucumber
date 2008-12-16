package gs.web.widget;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CustomizeSchoolSearchWidgetCommand {
    private static final Logger _log = Logger.getLogger(CustomizeSchoolSearchWidgetCommand.class);
    private String _searchQuery;
    private String _cobrand = "www";
    private String _email;
    private int _height = CustomizeSchoolSearchWidgetController.MINIMUM_HEIGHT;
    private int _width = CustomizeSchoolSearchWidgetController.MINIMUM_WIDTH;
    private String _dimensions = _width + "x" + _height;
    private String _backgroundColor = "BFE9F1";
    private String _textColor = "228899";
    private String _bordersColor = "9CD4DB";
    private boolean _terms;

    public String getSearchQuery() {
        return _searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        _searchQuery = searchQuery;
    }

    public String getCobrand() {
        return _cobrand;
    }

    public void setCobrand(String cobrand) {
        _cobrand = cobrand;
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public int getHeight() {
        return _height;
    }

    public void setHeight(int height) {
        _height = height;
        if (_height < CustomizeSchoolSearchWidgetController.MINIMUM_HEIGHT) {
            _height = CustomizeSchoolSearchWidgetController.MINIMUM_HEIGHT;
        }
    }

    public int getWidth() {
        return _width;
    }

    public void setWidth(int width) {
        _width = width;
        if (_width < CustomizeSchoolSearchWidgetController.MINIMUM_WIDTH) {
            _width = CustomizeSchoolSearchWidgetController.MINIMUM_WIDTH;
        }
    }

    public String getDimensions() {
        return _dimensions;
    }

    public void setDimensions(String dimensions) {
        _dimensions = dimensions;
    }

    public int getIframeWidth() {
        // 4+1 left border, 4+1 right border
        return _width - 10;
    }

    public int getIframeHeight() {
        // 4+1 top border, 20 + 1 bottom border, 40 bottom area
        return _height - 66;
    }

    public String getBackgroundColor() {
        return _backgroundColor;
    }

    public String getBackgroundColorSelect() {
        return _backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        _backgroundColor = backgroundColor;
    }

    public String getTextColor() {
        return _textColor;
    }

    public String getTextColorSelect() {
        return _textColor;
    }

    public void setTextColor(String textColor) {
        _textColor = textColor;
    }

    public String getBordersColor() {
        return _bordersColor;
    }

    public String getBordersColorSelect() {
        return _bordersColor;
    }

    public void setBordersColor(String bordersColor) {
        _bordersColor = bordersColor;
    }

    public boolean isTerms() {
        return _terms;
    }

    public void setTerms(boolean terms) {
        _terms = terms;
    }

    public Map<String, String> getBackgroundColorOptions() {
        Map<String, String> rval = new HashMap<String, String>();
        rval.put("FFFFFF", "");
        rval.put("BFE9F1", "");
        return rval;
    }

    public Map<String, String> getTextColorOptions() {
        Map<String, String> rval = new HashMap<String, String>();
        rval.put("FFFFFF", "");
        rval.put("228899", "");
        return rval;
    }

    public Map<String, String> getBordersColorOptions() {
        Map<String, String> rval = new HashMap<String, String>();
        rval.put("FFFFFF", "");
        rval.put("9CD4DB", "");
        return rval;
    }

    public String getIframeUrl() {
        String rval = SchoolSearchWidgetController.BEAN_ID;
        String separator = "?";
        try {
            if (StringUtils.isNotBlank(_searchQuery)) {
                rval += separator  + "searchQuery=" + URLEncoder.encode(_searchQuery, "UTF-8");
                separator = "&amp;";
            }
            if (StringUtils.isNotBlank(_cobrand) && !StringUtils.equals("www", _cobrand)) {
                rval += separator  + "cobrand=" + URLEncoder.encode(_cobrand, "UTF-8");
                separator = "&amp;";
            }
            rval += separator  + "textColor=" + URLEncoder.encode(_textColor, "UTF-8");
            separator = "&amp;";
        } catch (UnsupportedEncodingException uee) {
            _log.error(uee);
        }
        rval += separator  + "width=" + getIframeWidth();
        separator = "&amp;";
        rval += separator  + "height=" + getIframeHeight();
        return rval;
    }
}
