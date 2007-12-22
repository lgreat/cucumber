package gs.web.test;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;
import gs.web.util.list.Anchor;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chriskimm@greatschools.net
 */
public class TestLandingController extends AbstractController {

    public static final String BEAN_ID = "/test/landing.page";
    Map<String, Map> _cache = new HashMap<String, Map>();

    public TestLandingController() {
        super();
        loadCache();
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String stateParam = request.getParameter("state");
        String testIdParam = request.getParameter("tid");
        String key = stateParam + testIdParam;
        Map<String, String> data = _cache.get(key);

        ModelAndView mAndV = new ModelAndView("/test/landing");
        mAndV.getModel().putAll(data);
        return mAndV; 
    }

    private void loadCache() {
        _cache.clear();
        SpreadsheetService service = new SpreadsheetService("greatschools-tests-landing");
        try {
            service.setUserCredentials("chriskimm@greatschools.net", "greattests");
            /*
            URL metafeedUrl = new URL("http://spreadsheets.google.com/feeds/spreadsheets/private/full");
            SpreadsheetFeed feed = service.getFeed(metafeedUrl, SpreadsheetFeed.class);
            List spreadsheets = feed.getEntries();
            for (int i = 0; i < spreadsheets.size(); i++) {
                SpreadsheetEntry entry = (SpreadsheetEntry)spreadsheets.get(i);
                System.out.println ("entry url: " + entry.getWorksheetFeedUrl());
                System.out.println("\t" + entry.getTitle().getPlainText());
            }
            */

            URL wsUrl = new URL("http://spreadsheets.google.com/feeds/worksheets/o02465749437938730339.5684652189673031494/private/full/od6");
            WorksheetEntry dataWorksheet = service.getEntry(wsUrl, WorksheetEntry.class);
            URL listFeedUrl = dataWorksheet.getListFeedUrl();
            ListFeed lf = service.getFeed(listFeedUrl, ListFeed.class);
            for (ListEntry entry : lf.getEntries()) {
                Map<String, String> values = new HashMap<String, String>();
                for (String tag : entry.getCustomElements().getTags()) {
                    if ("links".equals(tag)) {
                        values.put(tag, entry.getCustomElements().getValue(tag));
                    } else {
                        values.put(tag, entry.getCustomElements().getValue(tag));                        
                    }
                }
                String state = entry.getCustomElements().getValue("state");
                String tid = entry.getCustomElements().getValue("tid");
                _cache.put(state+tid, values);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }

    /*
    List<Anchor> parseAnchorList(String text) {
        List<Anchor> list = new ArrayList<Anchor>();
        if (StringUtils.isNotBlank(text)) {
            list.add(new Anchor("http://www.greatschools.net", "greatschools home"));
            list.add(new Anchor("http://www.google.net", "google website"));
        }
        return list;
    }
    */
}
