package gs.web.widget;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

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
        return _width - 8;
    }

    public int getIframeHeight() {
        return _height - 64;
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
        } catch (UnsupportedEncodingException uee) {
            _log.error(uee);
        }
        rval += separator  + "width=" + getIframeWidth();
        separator = "&amp;";
        rval += separator  + "height=" + getIframeHeight();
        return rval;
    }
}
