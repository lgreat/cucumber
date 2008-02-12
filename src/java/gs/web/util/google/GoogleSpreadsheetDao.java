package gs.web.util.google;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.util.ServiceException;

import java.util.Map;
import java.util.HashMap;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class GoogleSpreadsheetDao implements IGoogleSpreadsheetDao {

    private static final Logger _log = Logger.getLogger(GoogleSpreadsheetDao.class);

    public Map<String, String> getDataFromRow(String worksheetUrl, String primaryKeyColumnName, String primaryKeyCellValue) {
        Map<String, String> model = new HashMap<String, String>();
        try {
            SpreadsheetService service = new SpreadsheetService("greatschools-GSWeb-9.5");
            WorksheetEntry dataWorksheet = service.getEntry(new URL(worksheetUrl), WorksheetEntry.class);
            URL listFeedUrl = dataWorksheet.getListFeedUrl();
            ListFeed lf = service.getFeed(listFeedUrl, ListFeed.class);
            for (ListEntry entry : lf.getEntries()) {
                String entryId = entry.getCustomElements().getValue(primaryKeyColumnName);
                if (primaryKeyCellValue.equals(entryId)) {
                    for (String tag: entry.getCustomElements().getTags()) {
                        model.put(tag, entry.getCustomElements().getValue(tag));
                    }
                    break;
                }
            }
        } catch (MalformedURLException e) {
            _log.error("MalformedURLException \"" + worksheetUrl + "\"");
            _log.error(e);
        } catch (IOException e) {
            _log.error(e);
            e.printStackTrace();
        } catch (ServiceException e) {
            _log.error(e);
            e.printStackTrace();
        }
        return model;
    }

    public Map<String, Map<String, String>> getDataFromWorksheet(String worksheetUrl, String primaryKeyColumnName) {
        Map<String, Map<String, String>> worksheetMap = new HashMap<String, Map<String, String>>();

        try {
            SpreadsheetService service = new SpreadsheetService("greatschools-GSWeb-9.5");
            WorksheetEntry dataWorksheet = service.getEntry(new URL(worksheetUrl), WorksheetEntry.class);
            URL listFeedUrl = dataWorksheet.getListFeedUrl();
            ListFeed lf = service.getFeed(listFeedUrl, ListFeed.class);
            // for each row
            for (ListEntry entry : lf.getEntries()) {
                String entryId = entry.getCustomElements().getValue(primaryKeyColumnName);
                // if the primary key is not blank
                if (!StringUtils.isBlank(entryId)) {
                    Map<String, String> rowMap = new HashMap<String, String>();
                    // for each cell in the row
                    for (String tag: entry.getCustomElements().getTags()) {
                        // store into map
                        rowMap.put(tag, entry.getCustomElements().getValue(tag));
                    }
                    // store rowMap in worksheetMap
                    worksheetMap.put(entryId, rowMap);
                }
            }
        } catch (MalformedURLException e) {
            _log.error(e);
            e.printStackTrace();
        } catch (IOException e) {
            _log.error(e);
            e.printStackTrace();
        } catch (ServiceException e) {
            _log.error(e);
            e.printStackTrace();
        }

        return worksheetMap;
    }
    
}
