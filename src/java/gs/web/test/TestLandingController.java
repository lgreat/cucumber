package gs.web.test;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.ServiceException;
import gs.web.util.list.Anchor;
import gs.web.util.UrlBuilder;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.content.IArticleDao;
import gs.data.content.Article;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.validation.Errors;
import org.springframework.validation.BindException;

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
 * This is the controller for the Test Landing Page.  It gets the data for the page
 * model from a google spreadsheet by using the gdata api.  
 *
 * @author chriskimm@greatschools.net
 */
public class TestLandingController extends SimpleFormController {

    public static final String BEAN_ID = "/test/landing.page";
    Map<String, Map> _cache;
    private URL _worksheetUrl;

    /** Used to populate city lists */
    private IGeoDao _geoDao;

    /** Used to build article links */
    private IArticleDao _articleDao;

    private StateManager _stateManager;

    private static final Logger _log = Logger.getLogger(TestLandingController.class);

//    protected Object formBackingObject(HttpServletRequest request) throws Exception {
//
//        TestLandingCommand command = new TestLandingCommand();
//        return command;
//    }
    
    protected Map referenceData(HttpServletRequest request, Object cmd, Errors errors) throws Exception {
        Map<String, Object> refData = new HashMap<String, Object>();

        if (StringUtils.isNotBlank(request.getParameter("clear"))) {
            _cache = null;
        }

        String stateParam = request.getParameter("state");
        if (StringUtils.isNotBlank(stateParam)) {
            State state = getStateManager().getState(stateParam);
            refData.put("cities", getCityList(state));
            String testIdParam = request.getParameter("tid");
            if (StringUtils.isNotBlank(testIdParam)) {
                String key = stateParam + testIdParam;
                Map<String, String> testData = getTestData(key);
                if (testData == null) {
                    errors.reject("Could not find test info for: " + key);
                } else {
                    refData.putAll(testData);
                }
            } else {
                errors.reject("test id (tid) parameter is missing");
            }
        } else {
            errors.reject("state parameter is missing");            
        }
        return refData;
    }
    
    protected List<City> getCityList(State state) {
        List<City> cities = _geoDao.findCitiesByState(state);
        City city = new City();
        city.setName("My city is not listed");
        cities.add(0, city);
        return cities;
    }

    protected Map<String, String> getTestData(String key) {
        if (_cache == null) {
             _cache = new HashMap<String, Map>();
            loadCache(_cache);
        }
        return _cache.get(key);
    }

    protected ModelAndView processFormSubmission(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 Object cmdObject,
                                                 BindException errors) {
        System.out.println ("command: " + cmdObject.getClass());
        String type = request.getParameter("type");
        System.out.println ("type: " + type);
        TestLandingCommand command = (TestLandingCommand)cmdObject;

        View view = null;
        if ("achievement".equals(type)) {
            UrlBuilder builder = new UrlBuilder(command.getSchool(), UrlBuilder.SCHOOL_PROFILE_TEST_SCORE);
            view = new RedirectView(builder.asSiteRelative(request));
        } else if ("compare".equals(type)) {
            UrlBuilder builder = new UrlBuilder(command.getSchool(), UrlBuilder.COMPARE_SCHOOL);
            view = new RedirectView(builder.asSiteRelative(request));
        } 
        ModelAndView mAndV = new ModelAndView(view);
        return mAndV;
    }
    /*
    public ModelAndView onSubmit(Object command, BindException errors) {
        System.out.println ("command: " + command.getClass());
        ModelAndView mAndV = new ModelAndView(new RedirectView("www.google.com"));
        return mAndV;
    }
    */

    private void loadCache(Map cache) {
        cache.clear();
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
                List<WorksheetEntry> worksheets = entry.getWorksheets();
                for (WorksheetEntry we : worksheets) {
                    System.out.println ("\t\t ws name: " + we.getTitle());
                    System.out.println ("\t\t ws url: " + we.getListFeedUrl());
                }
            }
            */

            WorksheetEntry dataWorksheet = service.getEntry(getWorksheetUrl(), WorksheetEntry.class);
            URL listFeedUrl = dataWorksheet.getListFeedUrl();
            ListFeed lf = service.getFeed(listFeedUrl, ListFeed.class);
            for (ListEntry entry : lf.getEntries()) {
                Map<String, Object> values = new HashMap<String, Object>();
                for (String tag : entry.getCustomElements().getTags()) {
                    if ("links".equals(tag)) {
                        values.put(tag, parseAnchorList(entry.getCustomElements().getValue(tag)));
                    } else {
                        values.put(tag, entry.getCustomElements().getValue(tag));                        
                    }
                }
                String state = entry.getCustomElements().getValue("state");
                String tid = entry.getCustomElements().getValue("tid");
                cache.put(state+tid, values);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }

    List<Anchor> parseAnchorList(String text) {
        List<Anchor> list = new ArrayList<Anchor>();
        if (StringUtils.isNotBlank(text)) {
            String[] links = text.split("\\n");
            for (String s : links) {
                String[] s2 = s.split(",");
                if (s2.length > 0) {
                    if (s2[0] != null) {
                        if (s2[0].startsWith("aid:")) {
                            try {
                                int aid = Integer.parseInt(s2[0].substring(4));
                                Article article = getArticleDao().getArticleFromId(aid);
                                if (article != null) {
                                    UrlBuilder builder = new UrlBuilder(article, null, false);
                                    list.add(new Anchor(builder.toString(), article.getTitle()));
                                } else {
                                    _log.warn("Could not find article: " + aid);
                                }
                            } catch (NumberFormatException e) {
                                _log.warn(e);
                            }
                        } else if (s2.length == 2) {
                            list.add(new Anchor(s2[1], s2[0]));
                        }
                    }
                }
            }
        }
        return list;
    }

    public URL getWorksheetUrl() {
        return _worksheetUrl;
    }

    public void setWorksheetUrl(URL worksheetUrl) {
        _worksheetUrl = worksheetUrl;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public IArticleDao getArticleDao() {
        return _articleDao;
    }

    public void setArticleDao(IArticleDao articleDao) {
        _articleDao = articleDao;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }
}
