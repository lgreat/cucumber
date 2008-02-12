package gs.web.util.google;

import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CachedGoogleSpreadsheetDao extends GoogleSpreadsheetDao implements ICachedGoogleSpreadsheetDao {
    protected static Map<String, GoogleSpreadsheetCachedItem> _rowCache;

    static {
        _rowCache = Collections.synchronizedMap(new HashMap<String, GoogleSpreadsheetCachedItem>());
    }

    /**
     * Retrieves a spreadsheet row. First tries cache, then the Google data source. The cacheDuration
     * parameter is only used if there is a cache miss, and is the duration in milliseconds to cache
     * the result.
     *
     * @param worksheetUrl URL of google worksheet
     * @param primaryKeyColumnName column header for primary key
     * @param primaryKeyCellValue value of primary key for the row you wish to retrieve
     * @param cacheDuration if there is a cache miss, use this value (in milliseconds) as the duration
     * to cache the data from the Google data source.
     * @return map representing the data in the row specified.
     */
    public Map<String, String> getDataFromRow(String worksheetUrl, String primaryKeyColumnName,
                                              String primaryKeyCellValue, long cacheDuration) {
        String cacheKey = worksheetUrl + ":" + primaryKeyColumnName + ":" + primaryKeyCellValue;

        Map<String, String> rowMap = retrieveFromCache(cacheKey);
        if (rowMap == null) {
            // cache miss or expire, retrieve from Google
            rowMap = getDataFromRow(worksheetUrl, primaryKeyColumnName, primaryKeyCellValue);
            if (rowMap != null && !rowMap.isEmpty()) {
                // store in cache if results were found
                GoogleSpreadsheetCachedItem item = new GoogleSpreadsheetCachedItem();
                item.setData(rowMap);
                item.setCacheDuration(cacheDuration);
                item.setCacheTime(System.currentTimeMillis());
                _rowCache.put(cacheKey, item);
            }
        }
        return rowMap;
    }

    public void clearCache() {
        _rowCache.clear();
    }

    /**
     * If the cacheKey has a value in _rowCache and is not expired, then the data map associated
     * with that value is returned. Note if the cacheKey is expired, it will be removed from the
     * cache prior to returning null.
     * @param cacheKey cache key
     * @return Map<String, String> or null if cache miss/expire
     */
    protected Map<String, String> retrieveFromCache(String cacheKey) {
        GoogleSpreadsheetCachedItem item = _rowCache.get(cacheKey);
        if (item == null) {
            return null;
        }
        long cachedTime = item.getCacheTime();
        long cacheDuration = item.getCacheDuration();
        long currentTime = System.currentTimeMillis();
        if (currentTime - cachedTime > cacheDuration) {
            _rowCache.remove(cacheKey);
            return null;
        }
        return item.getData();
    }

    /**
     * Data structure stored in cache map.
     */
    protected static class GoogleSpreadsheetCachedItem {
        private Map<String, String> _data;
        private long _cacheDuration;
        private long _cacheTime;

        public Map<String, String> getData() {
            return _data;
        }

        public void setData(Map<String, String> data) {
            _data = data;
        }

        public long getCacheDuration() {
            return _cacheDuration;
        }

        public void setCacheDuration(long cacheDuration) {
            _cacheDuration = cacheDuration;
        }

        public long getCacheTime() {
            return _cacheTime;
        }

        public void setCacheTime(long cacheTime) {
            _cacheTime = cacheTime;
        }
    }
}
