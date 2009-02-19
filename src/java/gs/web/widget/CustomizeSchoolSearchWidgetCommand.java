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
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CustomizeSchoolSearchWidgetCommand implements EmailValidator.IEmail {
    private static final Logger _log = Logger.getLogger(CustomizeSchoolSearchWidgetCommand.class);
    private String _searchQuery = "94536";
    private String _cobrand = CustomizeSchoolSearchWidgetController.DEFAULT_COBRAND;
    private String _email;
    private int _height = CustomizeSchoolSearchWidgetController.MINIMUM_HEIGHT;
    private int _width = CustomizeSchoolSearchWidgetController.MINIMUM_WIDTH;
    private int _zoom = 13;
    private String _dimensions = _width + "x" + _height;
    private String _backgroundColor = "BFE9F1";
    private String _textColor = "228899";
    private String _bordersColor = "9CD4DB";
    private boolean _terms;
    private String _widgetCode;
    private String _uniqueId;
    private City _city;

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
        return getDefaultColorMap("BFE9F1");
    }

    public Map<String, String> getTextColorOptions() {
        return getDefaultColorMap("228899");
    }

    public Map<String, String> getBordersColorOptions() {
        return getDefaultColorMap("9CD4DB");
    }

    public Integer colorStringToInt(String colorString) {
        if (StringUtils.length(colorString) != 6) {
            return 0;
        }
        return    Integer.parseInt(colorString.substring(0,2), 16)
                + Integer.parseInt(colorString.substring(2,4), 16)
                + Integer.parseInt(colorString.substring(4,6), 16);
    }

    protected Map<String, String> getDefaultColorMap(String firstOption) {
        // sort by approximate order white-to-black
        Map<String, String> rval = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String o1, String o2) {
                return colorStringToInt(o2).compareTo(colorStringToInt(o1));
            }
        });
        if (StringUtils.isNotBlank(firstOption)) {
            rval.put(firstOption, "");
        }
        rval.put("FFFFFF", "");
        rval.put("BDCFEF", "");
        rval.put("000000", "");
        rval.put("82CAFA", "");
        rval.put("898989", "");
        rval.put("BBDD66", "");
        rval.put("EE8888", "");
        rval.put("0088CC", "");
        rval.put("66CCFF", "");
        rval.put("CCBBCC", "");
        rval.put("FFEE99", "");
        rval.put("FF9977", "");
        rval.put("595959", "");
        rval.put("99BBCC", "");
        rval.put("993333", "");
        rval.put("004488", "");
        rval.put("998899", "");
        rval.put("FFDD00", "");
        return rval;
    }

    public String getIframeUrl() {
        return getIframeUrl(null);
    }

    public String getIframeUrl(HttpServletRequest request) {
        String rval = "";
        if (request != null) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOL_FINDER_WIDGET);

            // allways have iframe pull from www.greatschools.net except for internal servers
            SessionContext context = SessionContextUtil.getSessionContext(request);
            PageHelper pageHelper = new PageHelper(context, request);
            // non-developer workstation
            if (!UrlUtil.isDeveloperWorkstation(request.getServerName())) {
                if (pageHelper.isCloneServer()) {
                    // purposefully include clone to allow testing production behavior without code being on production yet
                    rval += urlBuilder.asFullUrl("clone.greatschools.net",80);
                } else if (pageHelper.isStagingServer()) {
                    rval += urlBuilder.asFullUrl("staging.greatschools.net",80);
                } else if (pageHelper.isDevEnvironment()) {
                    rval += urlBuilder.asFullUrl("dev.greatschools.net",80);
                } else {
                    // not dev, staging, or developer workstation -- so use www.greatschools.net
                    rval += urlBuilder.asFullUrl("www.greatschools.net",80);
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
            rval += separator  + "bordersColor=" + URLEncoder.encode(_bordersColor, "UTF-8");
            separator = "&amp;";
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


     public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
         System.out.println("HERERERE");
         PrintWriter out = response.getWriter();
        String rval = "";
        if (request != null) {
            UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.SCHOOL_FINDER_WIDGET);

            // allways have iframe pull from www.greatschools.net except for internal servers
            SessionContext context = SessionContextUtil.getSessionContext(request);
            PageHelper pageHelper = new PageHelper(context, request);
            // non-developer workstation
            if (!UrlUtil.isDeveloperWorkstation(request.getServerName())) {
                if (pageHelper.isCloneServer()) {
                    // purposefully include clone to allow testing production behavior without code being on production yet
                    rval += urlBuilder.asFullUrl("clone.greatschools.net",80);
                } else if (pageHelper.isStagingServer()) {
                    rval += urlBuilder.asFullUrl("staging.greatschools.net",80);
                } else if (pageHelper.isDevEnvironment()) {
                    rval += urlBuilder.asFullUrl("dev.greatschools.net",80);
                } else {
                    // not dev, staging, or developer workstation -- so use www.greatschools.net
                    rval += urlBuilder.asFullUrl("www.greatschools.net",80);
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
            rval += separator  + "bordersColor=" + URLEncoder.encode(_bordersColor, "UTF-8");
            separator = "&amp;";
        } catch (UnsupportedEncodingException uee) {
            _log.error(uee);
        }
        rval += separator  + "width=" + getIframeWidth();
        separator = "&amp;";
        rval += separator  + "height=" + getIframeHeight();
        rval += separator  + "zoom=" + getZoom();
        out.print(rval);
        
        return null;
    }

}
