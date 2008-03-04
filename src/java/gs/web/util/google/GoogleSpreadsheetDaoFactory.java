package gs.web.util.google;

import org.apache.commons.lang.StringUtils;
import gs.data.util.table.ITableDao;
import gs.data.util.table.ITableDaoFactory;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class GoogleSpreadsheetDaoFactory implements ITableDaoFactory {
    public static final String BEAN_ID = "googleSpreadsheetFactory";
    public static final String SPREADSHEET_PREFIX = "http://spreadsheets.google.com/feeds/worksheets/";
    public static final String DEFAULT_VISIBILITY = "public";
    public static final String DEFAULT_PROJECTION = "values";

    private String _username;
    private String _password;
    private String _googleKey;
    private String _visibility = DEFAULT_VISIBILITY;
    private String _projection = DEFAULT_PROJECTION;
    private String _worksheetName;

    public ITableDao getTableDao() {
        if (_googleKey == null) {
            throw new IllegalStateException("Cannot instantiate GoogleSpreadsheetDao without googleKey");
        }
        String worksheetUrl = SPREADSHEET_PREFIX +
                _googleKey + "/" +
                _visibility + "/" +
                _projection + "/";
        if (!StringUtils.isEmpty(_worksheetName)) {
                worksheetUrl += _worksheetName;
        }
        GoogleSpreadsheetDao dao;
        if (!StringUtils.isEmpty(_username) && !StringUtils.isEmpty(_password)) {
            dao = new GoogleSpreadsheetDao(worksheetUrl, _username, _password);
        } else {
            dao = new GoogleSpreadsheetDao(worksheetUrl);
        }
        return dao;
    }

    public String getUsername() {
        return _username;
    }

    public void setUsername(String username) {
        _username = username;
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        _password = password;
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
