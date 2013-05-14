package gs.web.school;


import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
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

    @RequestMapping(method= RequestMethod.GET)
    public void handleGet(
        @RequestParam(value="ncesCode", required = true) String ncesCode,
        HttpServletResponse response
    ) {

        //Execute and get the response.
        try {
            HttpResponse apiResponse = getSchoolData(ncesCode);
            TandemApiResponse tandemApiResponse = TandemApiResponse.create(apiResponse);

            // get the URL of the xcal file
            String yearlyEventsXCal = tandemApiResponse.getYearlyEventsXCal();

            HttpResponse xcalResponse = getXCalResponse(yearlyEventsXCal);

            // send the xcal back to the client
            ByteStreams.copy(xcalResponse.getEntity().getContent(), response.getOutputStream());

            response.getOutputStream().flush();
        } catch (Exception e) {
            _log.debug("Api call to tandem failed:", e);
        }

        return;
    }

    @RequestMapping(value="/ical", method= RequestMethod.GET)
    public void handleGetICal(
            @RequestParam(value="ncesCode", required = true) String ncesCode,
            HttpServletResponse response
    ) {

        //Execute and get the response.
        try {
            HttpResponse apiResponse = getSchoolData(ncesCode);
            TandemApiResponse tandemApiResponse = TandemApiResponse.create(apiResponse);

            // get the URL of the xcal file
            String yearlyEventsICal = tandemApiResponse.getYearlyEventsICal();

            HttpResponse icalResponse = getXCalResponse(yearlyEventsICal);

            // send the ical back to the client


            response.setContentType("text/Calendar");
            String outFileName = "ical.ics";
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

}

class TandemApiResponse {
    JSONObject _jsonObject;

    public static TandemApiResponse create(HttpResponse httpResponse) throws IOException, JSONException {

        InputStream inputStream = httpResponse.getEntity().getContent();

        String string = CharStreams.toString(new InputStreamReader(inputStream, "UTF-8"));

        return create(string);
    }

    public static TandemApiResponse create(String data) throws JSONException {
        JSONObject jsonObj = XML.toJSONObject(data);
        TandemApiResponse response = new TandemApiResponse(jsonObj);
        return response;
    }

    TandemApiResponse(JSONObject jsonObject) throws JSONException {
        _jsonObject = jsonObject;
    }

    public String getYearlyEventsXCal() {
        try {
            return (String) _jsonObject.getJSONObject("data").getJSONObject("school").get("yearly_events_xcal");
        } catch (Exception e) {
            return null;
        }
    }

    public String getYearlyEventsICal() {
        try {
            return (String) _jsonObject.getJSONObject("data").getJSONObject("school").get("yearly_events_ical");
        } catch (Exception e) {
            return null;
        }
    }
}
