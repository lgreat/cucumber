package gs.web.school;


import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

@Controller
@RequestMapping("/school/calendar")
public class SchoolCalendarAjaxController {

    public static final String API_BASE_URL = "https://api.intand.com/rest/index.php?token=greatschoolsftw";

    protected final Logger _log = Logger.getLogger(getClass());

    // tracks NCES code to boolean. Cache value equals false if we know the school has no calendar data
    protected static Cache _calendarDataCache;

    public static final String CACHE_KEY = "gs.web.school.SchoolCalendarData";

    static {
        CacheManager cacheManager = CacheManager.getInstance();
        _calendarDataCache = cacheManager.getCache(CACHE_KEY);
    }

    @RequestMapping(method = RequestMethod.GET)
    public void handleGet(@RequestParam(value = "ncesCode", required = true) String ncesCode,
                          HttpServletResponse response) {

        // if we know the school has no data, abort
        if (cachedLackOfData(ncesCode)) {
            return;
        }

        //Execute and get the response.
        try {
            HttpResponse apiResponse = getSchoolData(ncesCode);
            TandemApiResponse tandemApiResponse = TandemApiResponse.create(apiResponse);

            if (tandemApiResponse.hasData()) {
                // get the URL of the xcal file
                String yearlyEventsXCal = tandemApiResponse.getYearlyEventsXCal();

                HttpResponse xcalResponse = getXCalResponse(yearlyEventsXCal);

                // send the xcal back to the client
                ByteStreams.copy(xcalResponse.getEntity().getContent(), response.getOutputStream());

                response.getOutputStream().flush();
            } else {
                cacheLackOfData(ncesCode);
            }

        } catch (Exception e) {
            _log.debug("Api call to tandem failed:", e);
        }

        return;
    }

    @RequestMapping(value = "/ical", method = RequestMethod.GET)
    public void handleGetICal(@RequestParam(value = "ncesCode", required = true) String ncesCode,
                              @RequestParam(value = "schoolName", required = true) String schoolName,
                              @RequestParam(value = "format", defaultValue = "iCal") String format,
                              HttpServletResponse response) {

        //Execute and get the response.
        try {
            HttpResponse apiResponse = getSchoolData(ncesCode);
            TandemApiResponse tandemApiResponse = TandemApiResponse.create(apiResponse);

            // get the URL of the xcal file
            String yearlyEventsICal = tandemApiResponse.getYearlyEventsICal();

            HttpResponse icalResponse = getXCalResponse(yearlyEventsICal);

            // send the ical back to the client


            response.setContentType("text/Calendar");
            String outFileName = schoolName + "-" + format + "-calendar.ics";
            response.setHeader("Cache-control", "no-store");
            response.setHeader("Content-disposition", "inline; filename=\"" + outFileName + "\"");
            response.setHeader("Cache-Control", "private");
            response.setHeader("Vary", "Accept-Encoding");

            ByteStreams.copy(icalResponse.getEntity().getContent(), response.getOutputStream());

            response.getOutputStream().flush();
        } catch (Exception e) {
            _log.debug("Api call to tandem failed:", e);
        }

        return;
    }

    @RequestMapping(value = "/printTandemCalendar", method = RequestMethod.POST)
    public String handlePost(ModelMap modelMap,
                             @RequestParam(value = "data", required = true) String data,
                             @RequestParam(value = "schoolName", required = true) String schoolName) {
        modelMap.put("data", data);
        modelMap.put("schoolName", schoolName);
        return "school/printTandemCalendar";
    }

    public HttpResponse getSchoolData(String ncesCode) throws IOException {
        final String apiCallType = "school";
        final String apiUrl = API_BASE_URL + "&type=" + apiCallType + "&nces_id=" + ncesCode;
        final HttpGet httpGet = new HttpGet(apiUrl);
        final HttpClient httpClient = createHttpClient();

        HttpResponse apiResponse = httpClient.execute(httpGet);

        return apiResponse;
    }

    public HttpResponse getXCalResponse(String url) throws IOException {
        final HttpClient httpClient = createHttpClient();
        HttpGet xcalClient = new HttpGet(url);
        HttpResponse xcalResponse = httpClient.execute(xcalClient);
        return xcalResponse;
    }

    public HttpClient createHttpClient() {
        return new DefaultHttpClient();
    }

    public boolean cachedLackOfData(String ncesCode) {
        Element element = _calendarDataCache.get(getCacheKey(ncesCode));
        if (element != null && (Boolean) element.getObjectValue() == false) {
            return true; // there's no calendar data for this school
        }
        return false;
    }

    public void cacheLackOfData(String ncesCode) {
        Element element = new Element(getCacheKey(ncesCode), false);
        _calendarDataCache.put(element);
    }

    public String getCacheKey(String ncesCode) {
        return ncesCode;
    }

}

class TandemApiResponse {
    // A URL
    private String _yearlyEventsXCal;

    // A URL
    private String _yearlyEventsICal;

    public static TandemApiResponse create(HttpResponse httpResponse) {
        TandemApiResponse response;
        InputStream inputStream;

        try {
            inputStream = httpResponse.getEntity().getContent();
            String string = CharStreams.toString(new InputStreamReader(inputStream, "UTF-8"));
            response = create(string);
        } catch (IOException e) {
            response = new TandemApiResponse();
        } catch (JSONException e) {
            response = new TandemApiResponse();
        }

        return response;
    }

    public static TandemApiResponse create(String data) throws JSONException {
        JSONObject jsonObj = XML.toJSONObject(data);
        TandemApiResponse response = new TandemApiResponse();

        response.setYearlyEventsXCal(
            (String) jsonObj.getJSONObject("data").getJSONObject("school").get("yearly_events_xcal")
        );
        response.setYearlyEventsICal(
            (String) jsonObj.getJSONObject("data").getJSONObject("school").get("yearly_events_ical")
        );
        return response;
    }

    private TandemApiResponse() {

    }

    public boolean hasData() {
        return _yearlyEventsICal != null || _yearlyEventsXCal != null;
    }

    String getYearlyEventsXCal() {
        return _yearlyEventsXCal;
    }

    String getYearlyEventsICal() {
        return _yearlyEventsICal;
    }

    void setYearlyEventsXCal(String yearlyEventsXCal) {
        _yearlyEventsXCal = yearlyEventsXCal;
    }

    void setYearlyEventsICal(String yearlyEventsICal) {
        _yearlyEventsICal = yearlyEventsICal;
    }
}
