package gs.web.util.google;

import org.apache.commons.lang.StringUtils;

/**
 * Encapsulates Google Spreadsheet url information
 * @author Young Fan
 */
public class GoogleSpreadsheetInfo {
    public static final String SPREADSHEET_PREFIX = "http://spreadsheets.google.com/feeds/worksheets/";
    public static final String DEFAULT_VISIBILITY = "public";
    public static final String DEFAULT_PROJECTION = "values";

    /** The key identifying the spreadsheet -- must be supplied! */
    private String _googleKey;
    /** Visiblity, e.g. "public". Defaults to DEFAULT_VISIBILITY. */
    private String _visibility = DEFAULT_VISIBILITY;
    /** Projection, e.g. "values". Defaults to DEFAULT_PROJECTION. */
    private String _projection = DEFAULT_PROJECTION;
    /** Name of worksheet, e.g. "od6". */
    private String _worksheetName;

    public GoogleSpreadsheetInfo(String googleKey, String visibility, String projection, String worksheetName) {
        _googleKey = googleKey;
        _visibility = visibility;
        _projection = projection;
        _worksheetName = worksheetName;
    }

    public String getWorksheetUrl() {
        if (StringUtils.isBlank(_googleKey) || StringUtils.isBlank(_visibility) ||
            StringUtils.isBlank(_projection) || StringUtils.isBlank(_worksheetName)) {
            throw new IllegalStateException("The spreadsheet url is missing one of the four required fields: " +
                "_googleKey = " + _googleKey + ", _visibility = " + _visibility + ", _projection = " + _projection +
                "_worksheetName = " + _worksheetName);
        }

        return SPREADSHEET_PREFIX + _googleKey + "/" + _visibility + "/" + _projection + "/" + _worksheetName;
    }

    public String getGoogleKey() {
        return _googleKey;
    }

    public void setGoogleKey(String googleKey) {
        _googleKey = googleKey;
    }

    public String getVisibility() {
        return _visibility;
    }

    public void setVisibility(String visibility) {
        _visibility = visibility;
    }

    public String getProjection() {
        return _projection;
    }

    public void setProjection(String projection) {
        _projection = projection;
    }

    public String getWorksheetName() {
        return _worksheetName;
    }

    public void setWorksheetName(String worksheetName) {
        _worksheetName = worksheetName;
    }
}
