package gs.web.widget;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;
import java.util.Comparator;

import gs.web.util.validator.EmailValidator;
import gs.web.util.UrlBuilder;
import gs.web.util.PageHelper;
import gs.web.util.UrlUtil;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.data.geo.City;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class CustomizeSchoolSearchWidgetCommand implements EmailValidator.IEmail {
    private static final Logger _log = Logger.getLogger(CustomizeSchoolSearchWidgetCommand.class);
    private String _searchQuery = "94536";
    private String _cobrand = CustomizeSchoolSearchWidgetController.DEFAULT_COBRAND;
    private String _email;
    private String _widgetCodeCheck="no";
    private int _height = CustomizeSchoolSearchWidgetController.MINIMUM_HEIGHT;
    private int _width = CustomizeSchoolSearchWidgetController.MINIMUM_WIDTH;
    private int _zoom = 13;
    private String _dimensions = _width + "x" + _height;
    private String _backgroundColor = "FFCC66";
    private String _textColor = "0066B8";
    private String _bordersColor = "FFCC66";
    private boolean _terms;
    private String _widgetCode;
    private String _uniqueId;
    private City _city;
    private String _cityName = "Fremont";
    private String _state = "CA";
    private String _normalizedAddress = "Fremont, CA 94536";
    private float _lat = 37.57101f;
    private float _lon = -121.98144f;
    private static Map<String,String> _defaultColorMap;

    static{
        
         _defaultColorMap = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String o1, String o2) {
                return colorStringToInt(o2).compareTo(colorStringToInt(o1));
            }
        });

        _defaultColorMap.put("FFFFFF", "");
        _defaultColorMap.put("BDCFEF", "");
        _defaultColorMap.put("000000", "");
        _defaultColorMap.put("82CAFA", "");
        _defaultColorMap.put("999999", "");
        _defaultColorMap.put("BBDD66", "");
        _defaultColorMap.put("EE8888", "");
        _defaultColorMap.put("0066B8", "");
        _defaultColorMap.put("66CCFF", "");
        _defaultColorMap.put("CCBBCC", "");
        _defaultColorMap.put("FFCC99", "");
        _defaultColorMap.put("FF9977", "");
        _defaultColorMap.put("595959", "");
        _defaultColorMap.put("99BBCC", "");
        _defaultColorMap.put("993333", "");
        _defaultColorMap.put("004488", "");
        _defaultColorMap.put("998899", "");
        _defaultColorMap.put("FFDD00", "");
    }

    public String getSearchQuery() {
        return _searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        _searchQuery = searchQuery;
    }

    public String getCobrandSite() {
        return _cobrand;
    }

    public void setCobrandSite(String cobrand) {
        _cobrand = cobrand;
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public int getZoom() {
        return _zoom;
    }

    public void setZoom(int zoom) {
        _zoom = zoom;
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
        _backgroundColor = backgroundColor.trim();
    }

    public String getTextColor() {
        return _textColor;
    }

    public String getTextColorSelect() {
        return _textColor;
    }

    public void setTextColor(String textColor) {
        _textColor = textColor.trim();
    }

    public String getBordersColor() {
        return _bordersColor;
    }

    public String getBordersColorSelect() {
        return _bordersColor;
    }

    public void setBordersColor(String bordersColor) {
        _bordersColor = bordersColor.trim();
    }

    public boolean isTerms() {
        return _terms;
    }

    public void setTerms(boolean terms) {
        _terms = terms;
    }

    public Map<String, String> getBackgroundColorOptions() {
        return getDefaultColorMap("FFCC66",_backgroundColor.trim());
    }

    public Map<String, String> getTextColorOptions() {
        return getDefaultColorMap("0066B8",_textColor.trim());

    }

    public Map<String, String> getBordersColorOptions() {
        return getDefaultColorMap("FFCC66",_bordersColor.trim());
    }

    //This method is used to compare the colors so that the colors are ordered from white to black
    public static Integer colorStringToInt(String colorString) {
        if (StringUtils.length(colorString) != 6 && StringUtils.length(colorString) != 3) {
             return 0;
        }
        if(StringUtils.length(colorString) == 6)
        {
            return    Integer.parseInt(colorString.substring(0,2), 16)*10000
                + Integer.parseInt(colorString.substring(2,4), 16)*100
                + Integer.parseInt(colorString.substring(4,6), 16);
        }
        if(StringUtils.length(colorString) == 3)
        {
            return    Integer.parseInt(colorString.substring(0,2), 16)
                + Integer.parseInt(colorString.substring(2,3), 16)*10;
        }
          return 0;
    }

    protected Map<String,String> getDefaultColorMap(){
        return getDefaultColorMap(null,null);
    }

    protected Map<String,String> getDefaultColorMap(String firstOption){
        return getDefaultColorMap(firstOption,null);
    }
    
    protected Map<String, String> getDefaultColorMap(String firstOption,String additionalColor) {
        // sort by approximate order white-to-black
        Map<String, String> rval = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String o1, String o2) {
                return colorStringToInt(o2).compareTo(colorStringToInt(o1));
            }
        });

        if (StringUtils.isNotBlank(firstOption)) {
            rval.put(firstOption, "");
        }
        rval.putAll(_defaultColorMap);
        if(additionalColor != null && StringUtils.isBlank(rval.get(additionalColor))){
            rval.put(additionalColor,"");
        }        
        return rval;
    }

    public String getIframeUrl() {
        return getIframeUrl(null);
    }

    public String getIframeUrl(HttpServletRequest request) {
        String rval = "";
        if (request != null) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOL_FINDER_WIDGET);

            // allways have iframe pull from www.greatschools.org except for internal servers
            SessionContext context = SessionContextUtil.getSessionContext(request);
            PageHelper pageHelper = new PageHelper(context, request);
            // non-developer workstation
            if (!UrlUtil.isDeveloperWorkstation(request.getServerName())) {
                if (pageHelper.isCloneServer()) {
                    // purposefully include clone to allow testing production behavior without code being on production yet
                    rval += urlBuilder.asFullUrl("clone.greatschools.org",80);
                } else if (pageHelper.isStagingServer()) {
                    rval += urlBuilder.asFullUrl("staging.greatschools.org",80);
                } else if (pageHelper.isDevEnvironment()) {
                    rval += urlBuilder.asFullUrl("dev.greatschools.org",80);
                } else {
                    // not dev, staging, or developer workstation -- so use www.greatschools.org
                    rval += urlBuilder.asFullUrl("www.greatschools.org",80);
                }
            } else {
                // developer workstation, so use own url
                rval += urlBuilder.asFullUrl(request);                
            }
        } else {
            rval += SchoolSearchWidgetController.BEAN_ID;
        }
        String separator = "?";
        try {
            if (StringUtils.isNotBlank(_searchQuery)) {
                rval += separator  + "searchQuery=" + URLEncoder.encode(_searchQuery, "UTF-8");
                separator = "&amp;";
            }
            if (StringUtils.isNotBlank(_cobrand) && !StringUtils.equals(CustomizeSchoolSearchWidgetController.DEFAULT_COBRAND, _cobrand)) {
                rval += separator  + "cobrandHostname=" + URLEncoder.encode(_cobrand, "UTF-8");
                separator = "&amp;";
            }
            rval += separator  + "textColor=" + URLEncoder.encode(_textColor, "UTF-8");
            separator = "&amp;";
            rval += separator  + "bordersColor=" + URLEncoder.encode(_bordersColor, "UTF-8");
            rval += separator  + "lat=" + URLEncoder.encode(String.valueOf(_lat), "UTF-8");
            rval += separator  + "lon=" + URLEncoder.encode(String.valueOf(_lon), "UTF-8");
            rval += separator  + "cityName=" + URLEncoder.encode(_cityName, "UTF-8");
            rval += separator  + "state=" + URLEncoder.encode(_state, "UTF-8");
            rval += separator  + "normalizedAddress=" + URLEncoder.encode(_normalizedAddress, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            _log.error(uee);
        }
        rval += separator  + "width=" + getIframeWidth();
        separator = "&amp;";
        rval += separator  + "height=" + getIframeHeight();
        rval += separator  + "zoom=" + getZoom();


        return rval;
    }

    public void setWidgetCode(String widgetCode) {
        _widgetCode = widgetCode;
    }

    public String getWidgetCode() {
        return _widgetCode;
    }

    public String getUniqueId() {
        return _uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        _uniqueId = uniqueId;
    }

    public City getCity() {
        return _city;
    }

    public void setCity(City city) {
        _city = city;
    }


    public String getWidgetCodeCheck() {
        return _widgetCodeCheck;
    }

    public void setWidgetCodeCheck(String widgetCodeCheck) {
        _widgetCodeCheck = widgetCodeCheck;
    }

    public String getCityName() {
        return _cityName;
    }

    public void setCityName(String cityName) {
        _cityName = cityName;
    }

    public String getState() {
        return _state;
    }

    public void setState(String state) {
        _state = state;
    }

    public String getNormalizedAddress() {
        return _normalizedAddress;
    }

    public void setNormalizedAddress(String normalizedAddress) {
        _normalizedAddress = normalizedAddress;
    }

    public float getLat() {
        return _lat;
    }

    public void setLat(float lat) {
        _lat = lat;
    }

    public float getLon() {
        return _lon;
    }

    public void setLon(float lon) {
        _lon = lon;
    }
}
