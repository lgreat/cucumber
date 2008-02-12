package gs.web.util.google;

import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public interface ICachedGoogleSpreadsheetDao {
    public static final long HOUR = 3600000;
    public static final long DAY = 86400000;

    public Map<String, String> getDataFromRow(String worksheetUrl,
                                              String primaryKeyColumnName,
                                              String primaryKeyCellValue,
                                              long cacheDuration);

    public void clearCache();
}
