package gs.web.util.google;

import gs.web.util.ICacheable;

import java.util.List;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public interface IGoogleSpreadsheetDao extends ICacheable<String, List<SpreadsheetRow>> {
    public SpreadsheetRow getFirstRowByKey(String keyName,
                                           String keyValue);

    public List<SpreadsheetRow> getRowsByKey(String keyName,
                                             String keyValue);

    public List<SpreadsheetRow> getAllRows();
}
