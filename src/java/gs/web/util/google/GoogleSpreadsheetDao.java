package gs.web.util.google;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.util.ServiceException;

import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import gs.data.util.table.HashMapTableRow;
import gs.data.util.table.ITableRow;
import gs.data.util.table.AbstractCachedTableDao;

/**
 * Implementation of ITableDao backed by a google spreadsheet. This extends AbstractCachedTableDao
 * so it is cached.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class GoogleSpreadsheetDao extends AbstractCachedTableDao {
    private static final Logger _log = Logger.getLogger(GoogleSpreadsheetDao.class);

    /** For unit testing */
    private SpreadsheetService _spreadsheetService;
    /** For private/write access */
    private String _username;
    /** For private/write access */
    private String _password;
    /** Url to retrieve spreadsheet from */
    private String _worksheetUrl;

    /**
     * Initialize this class with a URL pointing to the spreadsheet.
     */
    public GoogleSpreadsheetDao(String worksheetUrl) {
        this(worksheetUrl, null, null);
    }

    /**
     * Initialize this class with a URL pointing to the spreadsheet, and a username/password
     * for authentication.
     */
    public GoogleSpreadsheetDao(String worksheetUrl, String username, String password) {
        _worksheetUrl = worksheetUrl;
        _username = username;
        _password = password;
    }

    /**
     * Delegates to getListFeed to contact Google, then iterates over the returned ListFeed
     * to find the rows matching keyName/keyValue. If no results are found, return null.
     *
     * @throws ExternalConnectionException If thrown from getListFeed
     */
    protected List<ITableRow> getRowsByKeyExternal(String keyName, String keyValue)
            throws ExternalConnectionException {
        List<ITableRow> matchingRows = new ArrayList<ITableRow>();
        ListFeed lf = getListFeed();
        if (lf != null) {
            for (ListEntry rowEntry : lf.getEntries()) {
                ITableRow currentRow = new GoogleTableRow(rowEntry);
                if (keyValue == null || keyName == null || keyValue.equals(currentRow.get(keyName))) {
                    matchingRows.add(currentRow);
                }
            }
        }
        if (matchingRows.isEmpty()) {
            return null;
        }
        return matchingRows;
    }

    /**
     * Contacts worksheetUrl and retrieves a ListFeed for the spreadsheet. Should never return null.
     *
     * @throws ExternalConnectionException If there is an error contacting the worksheetUrl, this
     * exception is thrown encapsulating the error.
     */
    protected ListFeed getListFeed() throws ExternalConnectionException {
        ListFeed lf;
        try {
            SpreadsheetService service = getSpreadsheetService();
            if (!StringUtils.isEmpty(_username) && !StringUtils.isEmpty(_password)) {
                service.setUserCredentials(_username, _password);
            }
            _log.info("Contacting Google: " + _worksheetUrl);
            WorksheetEntry dataWorksheet = service.getEntry(new URL(_worksheetUrl), WorksheetEntry.class);
            URL listFeedUrl = dataWorksheet.getListFeedUrl();
            lf = service.getFeed(listFeedUrl, ListFeed.class);
        } catch (MalformedURLException e) {
            _log.error("MalformedURLException: \"" + _worksheetUrl + "\"", e);
            throw new ExternalConnectionException(e);
        } catch (IOException e) {
            _log.error("IOException: \"" + _worksheetUrl + "\"", e);
            throw new ExternalConnectionException(e);
        } catch (ServiceException e) {
            _log.error("ServiceException: \"" + _worksheetUrl + "\"", e);
            throw new ExternalConnectionException(e);
        }
        return lf;
    }

    /**
     * Uses the worksheetUrl as the cache namespace.
     */
    public String getCacheKey() {
        return _worksheetUrl;
    }

    protected void setSpreadsheetService(SpreadsheetService service) {
        _spreadsheetService = service;
    }

    /**
     * Uses "greatschools-GSWeb-9.5"
     */
    protected SpreadsheetService getSpreadsheetService() {
        if (_spreadsheetService != null) {
            return _spreadsheetService;
        }
        return new SpreadsheetService("greatschools-GSWeb-9.5");
    }

    public String getWorksheetUrl() {
        return _worksheetUrl;
    }
    
    public String getUsername() {
        return _username;
    }

    public String getPassword() {
        return _password;
    }
    public void setWorksheetUrl(String worksheetUrl) {
        _worksheetUrl = worksheetUrl;
    }

    /**
     * Helpful subclass of HashMapTableRow that adds a constructor to automatically populate
     * the table row from a google ListEntry object.
     */
    private static class GoogleTableRow extends HashMapTableRow {
        /**
         * Initialize this TableRow with the contents of a ListEntry (a Google spreadsheets row).
         * @param rowEntry row to initialize this object with.
         */
        public GoogleTableRow(ListEntry rowEntry) {
            for (String columnName: rowEntry.getCustomElements().getTags()) {
                addCell(columnName, rowEntry.getCustomElements().getValue(columnName));
            }
        }
    }
}
