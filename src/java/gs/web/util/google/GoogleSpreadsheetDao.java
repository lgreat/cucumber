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
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class GoogleSpreadsheetDao extends AbstractCachedTableDao {
    private static final Logger _log = Logger.getLogger(GoogleSpreadsheetDao.class);

    /** For unit testing */
    private SpreadsheetService _service;
    /** For private/write access */
    private String _username;
    /** For private/write access */
    private String _password;
    private String _worksheetUrl;

    public GoogleSpreadsheetDao(String worksheetUrl) {
        this(worksheetUrl, null, null);
    }

    public GoogleSpreadsheetDao(String worksheetUrl, String username, String password) {
        _worksheetUrl = worksheetUrl;
        _username = username;
        _password = password;
    }

    protected List<ITableRow> getRowsByKeyExternal(String keyName, String keyValue) {
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

    protected ListFeed getListFeed() {
        ListFeed lf = null;
        try {
            SpreadsheetService service = getSpreadsheetService();
            if (!StringUtils.isEmpty(_username) && !StringUtils.isEmpty(_password)) {
                service.setUserCredentials(_username, _password);
            }
            WorksheetEntry dataWorksheet = service.getEntry(new URL(_worksheetUrl), WorksheetEntry.class);
            URL listFeedUrl = dataWorksheet.getListFeedUrl();
            lf = service.getFeed(listFeedUrl, ListFeed.class);
        } catch (MalformedURLException e) {
            _log.error("MalformedURLException: \"" + _worksheetUrl + "\"");
            _log.error(e);
        } catch (IOException e) {
            _log.error("IOException: \"" + _worksheetUrl + "\"");
            _log.error(e);
        } catch (ServiceException e) {
            _log.error("ServiceException: \"" + _worksheetUrl + "\"");
            _log.error(e);
        }
        return lf;
    }

    public String getCacheKey() {
        return _worksheetUrl;
    }

    protected void setSpreadsheetService(SpreadsheetService service) {
        _service = service;
    }

    protected SpreadsheetService getSpreadsheetService() {
        if (_service != null) {
            return _service;
        }
        return new SpreadsheetService("greatschools-GSWeb-9.5");
    }

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
