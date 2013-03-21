package gs.web.realEstateAgent;

import gs.data.search.*;
import gs.data.search.beans.SolrSchoolSearchResult;
import gs.data.search.fields.DocumentType;
import gs.data.search.fields.SchoolFields;
import gs.data.util.CommunityUtil;
import gs.web.PdfView;
import gs.web.util.UrlBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: rramachandran
 * Date: 1/4/13
 * Time: 8:39 AM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/real-estate/guides/neighborhood-guide")
@Component("realEstateSchoolsReportController")
public class RealEstateSchoolsReportController {

    @Autowired
    private GsSolrSearcher _gsSolrSearcher;

    @Autowired
    private InternalResourceViewResolver _viewResolver;

    @Autowired
    private RealEstateAgentHelper _realEstateAgentHelper;

    public static final int MAX_ALLOWED_SCHOOLS = 6;

    private static final String VIEW_NAME = "/realEstateAgent/realEstateSchoolReport";
    public static final String MODEL_SCHOOL_SEARCH_RESULTS = "schoolSearchResults";

    private static Logger _logger = Logger.getLogger(RealEstateSchoolsReportController.class);

    @RequestMapping(method= RequestMethod.POST)
    public View post(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response,
                     @RequestParam(value="lat", required = false) String lat,
                     @RequestParam(value="lon", required = false) String lon,
                     @RequestParam(value="state", required = false) String state) {
        return get(modelMap, request, response, lat, lon, state);
    }

    @RequestMapping(method= RequestMethod.GET)
    public View get(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response,
                    @RequestParam(value="lat", required = false) String lat,
                    @RequestParam(value="lon", required = false) String lon,
                    @RequestParam(value="state", required = false) String state) {

        SearchResultsPage<SolrSchoolSearchResult> searchResultsPage;

        Integer userId = _realEstateAgentHelper.getUserId(request);

        if(userId == null && !_realEstateAgentHelper.skipUserValidation(request)) {
            return new RedirectView(_realEstateAgentHelper.getRealEstateSchoolGuidesUrl(request));
        }

        try {
            searchResultsPage = searchForSchools(lat, lon, state.toLowerCase());

            if (searchResultsPage.getTotalResults() == 0) {
                _logger.debug("No schools found. Redirecting to 404 page");
                return new RedirectView("/status/error404.page");
            }
        } catch (Exception e) {
            if (lat != null && lon != null && state != null) {
                _logger.debug("Exception while getting School objects with given params. State: " + state + ", lat: " + lat + ", lon: " + lon, e);
            } else {
                _logger.debug("Exception while getting School objects with given params.", e);
            }
            return new RedirectView("/status/error404.page");
        }

        modelMap.put("streetNumber", request.getParameter("streetNumber"));
        modelMap.put("streetName", request.getParameter("streetName"));
        modelMap.put("city", request.getParameter("city"));
        modelMap.put("state", state);
        modelMap.put("zipcode", request.getParameter("zipcode"));
        modelMap.put("bath", request.getParameter("bath"));
        modelMap.put("bed", request.getParameter("bed"));
        modelMap.put("sqFeet", request.getParameter("sqFeet"));

        modelMap.put(MODEL_SCHOOL_SEARCH_RESULTS, searchResultsPage.getSearchResults());

        modelMap.put("basePhotoPath", CommunityUtil.getMediaPrefix());

        String locationSearchUrl = new UrlBuilder(state, lat, lon, UrlBuilder.BY_LOCATION_SEARCH).asFullUrl(request);

        String qrCodeImgSrc = new UrlBuilder(UrlBuilder.QR_CODE_GENERATOR).asFullUrl(request);

        modelMap.put("imgSrc", qrCodeImgSrc);
        try {
            modelMap.put("locationSearchUrl", URLEncoder.encode(locationSearchUrl, "UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            _logger.debug("Exception while trying to encode the url - " + locationSearchUrl, e);
        }

        try {
            View viewToWrap = _viewResolver.resolveViewName(VIEW_NAME, Locale.getDefault());

            if("true".equals(request.getParameter("pageView"))) {
                return viewToWrap;
            }
            return new PdfView(viewToWrap);
        } catch (Exception e) {
            return new RedirectView("/status/error404.page");
        }
    }

    protected SearchResultsPage<SolrSchoolSearchResult> searchForSchools(String lat, String lon, String state) {
        SearchResultsPage<SolrSchoolSearchResult> searchResultsPage = new SearchResultsPage(0, new ArrayList<SolrSchoolSearchResult>());

        GsSolrQuery q = createGsSolrQuery();

        q.filter(DocumentType.SCHOOL).page(0, MAX_ALLOWED_SCHOOLS);

        String[] gradeLevels = {"e","m","h"};
        q.filter(SchoolFields.GRADE_LEVEL, gradeLevels);

        q.filter(SchoolFields.SCHOOL_DATABASE_STATE, state);

        q.restrictToRadius(Float.parseFloat(lat), Float.parseFloat(lon), (float)60.0);

        q.requireNonOptionalWords();

        q.setSpellCheckEnabled(false);
        q.sort(FieldSort.DISTANCE);

        try {
            searchResultsPage = _gsSolrSearcher.search(q, SolrSchoolSearchResult.class, true);

            if (searchResultsPage.isDidYouMeanResults()) {
                // adapting old existing logic to new code: If the search results we got back are the result
                // of an automatic second search using a Solr spelling suggestion, then we want it to appear
                // that we never received any results so the site will display the No Results page with the
                // "did you mean" suggestion
                searchResultsPage.setTotalResults(0);
                searchResultsPage.setSearchResults(new ArrayList<SolrSchoolSearchResult>());
            }

        } catch (SearchException e) {
            _logger.error("Problem occured while getting schools : ", e);
        }

        return searchResultsPage;
    }

    protected GsSolrQuery createGsSolrQuery() {
        return new GsSolrQuery(QueryType.SCHOOL_SEARCH);
    }

    public GsSolrSearcher getGsSolrSearcher() {
        return _gsSolrSearcher;
    }

    public void setGsSolrSearcher(GsSolrSearcher _gsSolrSearcher) {
        this._gsSolrSearcher = _gsSolrSearcher;
    }

    public InternalResourceViewResolver getViewResolver() {
        return _viewResolver;
    }

    public void setViewResolver(InternalResourceViewResolver _viewResolver) {
        this._viewResolver = _viewResolver;
    }
}
