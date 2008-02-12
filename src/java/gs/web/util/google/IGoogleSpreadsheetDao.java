package gs.web.util.google;

import java.util.Map;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public interface IGoogleSpreadsheetDao {
    public Map<String, String> getDataFromRow(String worksheetUrl,
                                              String primaryKeyColumnName,
                                              String primaryKeyCellValue);

    public Map<String, Map<String, String>> getDataFromWorksheet(String worksheetUrl,
                                                                 String primaryKeyColumnName);
}
