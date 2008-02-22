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
import gs.web.util.CachedItem;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class GoogleSpreadsheetDao implements IGoogleSpreadsheetDao {
    private static final Logger _log = Logger.getLogger(GoogleSpreadsheetDao.class);

    protected static Map<String, CachedItem<List<SpreadsheetRow>>> _cache;

    /** For unit testing */
    private SpreadsheetService _service;
    private String _username;
    private String _password;
    private String _worksheetUrl;

    static {
        _cache = Collections.synchronizedMap(new HashMap<String, CachedItem<List<SpreadsheetRow>>>());
    }

    public GoogleSpreadsheetDao(String worksheetUrl) {
        this(worksheetUrl, null, null);
    }

    public GoogleSpreadsheetDao(String worksheetUrl, String username, String password) {
        _worksheetUrl = worksheetUrl;
        _username = username;
        _password = password;
    }

    public SpreadsheetRow getFirstRowByKey(String keyName, String keyValue) {
        List<SpreadsheetRow> rows = getRowsByKey(keyName, keyValue);
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        return rows.get(0);
    }

    public List<SpreadsheetRow> getRowsByKey(String keyName, String keyValue) {
        String cacheKey = _worksheetUrl + ":" + keyName + ":" + keyValue;

        List<SpreadsheetRow> rows = getFromCache(cacheKey);
        if (rows == null || rows.isEmpty()) {
            // cache miss or expire, retrieve from Google
            rows = getRowsByKeyExternal(keyName, keyValue);
            if (rows != null && !rows.isEmpty()) {
                // store in cache if results were found
                putIntoCache(cacheKey, rows, HOUR);
            }
        }
        return rows;
    }

    public List<SpreadsheetRow> getAllRows() {
        List<SpreadsheetRow> rows = getFromCache(_worksheetUrl);
        if (rows == null || rows.isEmpty()) {
            // cache miss or expire, retrieve from Google
            rows = getAllRowsExternal();
            if (rows != null && !rows.isEmpty()) {
                // store in cache if results were found
                putIntoCache(_worksheetUrl, rows, HOUR);
            }
        }
        return rows;
    }

    protected List<SpreadsheetRow> getRowsByKeyExternal(String keyName, String keyValue) {
        List<SpreadsheetRow> matchingRows = new ArrayList<SpreadsheetRow>();
        ListFeed lf = getListFeed();
        if (lf != null) {
            for (ListEntry rowEntry : lf.getEntries()) {
                SpreadsheetRow currentRow = new SpreadsheetRow(rowEntry);
                if (keyValue.equals(currentRow.getCell(keyName))) {
                    matchingRows.add(currentRow);
                }
            }
        }
        if (matchingRows.isEmpty()) {
            return null;
        }
        return matchingRows;
    }

    protected List<SpreadsheetRow> getAllRowsExternal() {
        List<SpreadsheetRow> matchingRows = new ArrayList<SpreadsheetRow>();
        ListFeed lf = getListFeed();
        if (lf != null) {
            for (ListEntry rowEntry : lf.getEntries()) {
                matchingRows.add(new SpreadsheetRow(rowEntry));
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
            _log.error("MalformedURLException \"" + _worksheetUrl + "\"");
            _log.error(e);
        } catch (IOException e) {
            _log.error(e);
            e.printStackTrace();
        } catch (ServiceException e) {
            _log.error(e);
            e.printStackTrace();
        }
        return lf;
    }

    public List<SpreadsheetRow> getFromCache(String s) {
        List<SpreadsheetRow> rval = null;
        CachedItem<List<SpreadsheetRow>> item = _cache.get(s);
        if (item != null) {
            rval = item.getDataIfNotExpired();
            if (rval == null) {
                clearCacheKey(s);
            }
        }
        return rval;
    }

    public void putIntoCache(String s, List<SpreadsheetRow> rows, long timeToExpiration) {
        CachedItem<List<SpreadsheetRow>> item = new CachedItem<List<SpreadsheetRow>>();
        item.setData(rows);
        item.setCachedAt(System.currentTimeMillis());
        item.setCacheDuration(timeToExpiration);
        _cache.put(s, item);
    }

    public void clearCacheKey(String s) {
        _cache.remove(s);
    }

    public void clearCache() {
        _cache.clear();
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
}
