package gs.web.util.google;

import com.google.gdata.data.spreadsheet.ListEntry;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;

/**
 * Simple wrapper over a Map of String to String, representing each cell in a row indexed by column name.
 * Created the wrapper because it makes method signatures a little easier to deal with. In addition, code
 * that reads
 * <code>row.getCell("id");</code>
 * is a lot easier to read than the equivalent pulling out of a map. This class could eventually contain
 * other helpful methods for interacting with spreadsheet rows, if such seem necessary.
 *
 * Also, currently it's backed by a map, but that loses the order of the columns. If that becomes
 * important, the implementation can be changed without affecting the clients of this class.
 *
 * Also(2), this implementation expects column names to be unique. I'm not sure that's enforced by
 * Google. To make a row that didn't expect that would probably require an ISpreadsheetRow, and I
 * have an IHeadache right now so I'm leaving that for another month.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SpreadsheetRow {
    private Map<String, String> _rowMap = new HashMap<String, String>();

    public SpreadsheetRow() {
    }

    /**
     * Initialize this SpreadsheetRow with the contents of a ListEntry (a Google spreadsheets row).
     * @param rowEntry row to initialize this object with.
     */
    public SpreadsheetRow(ListEntry rowEntry) {
        for (String columnName: rowEntry.getCustomElements().getTags()) {
            addCell(columnName, rowEntry.getCustomElements().getValue(columnName));
        }
    }

    public void addCell(String columnName, String cellValue) {
        _rowMap.put(columnName, cellValue);
    }

    public Set<String> getColumnNames() {
        return _rowMap.keySet();
    }

    /**
     * Gets the value of the cell in the column named columnName.
     *
     * @param columnName name of column containing the cell you want
     * @return value of cell
     */
    public String getCell(String columnName) {
        return _rowMap.get(columnName);
    }

}
