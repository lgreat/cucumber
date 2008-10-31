package gs.web.content;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

import gs.data.json.JSONObject;
import gs.data.json.JSONArray;
import gs.data.json.JSONException;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.content.DonorsChooseProposal;
import gs.data.content.IDonorsChooseDao;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;
import gs.data.geo.ICounty;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.UrlUtil;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class DonorsChooseController extends AbstractController {
    private static final Log _log = LogFactory.getLog(DonorsChooseController.class);
    private String _viewName;
    private ISchoolDao _schoolDao;
    private IGeoDao _geoDao;
    private StateManager _stateManager;

    private IDonorsChooseDao _donorsChooseDao;

    public static final String SCHOOL_ID_PARAM = "schoolID";
    public static final String CITY_ID_PARAM = "cityID";
    public static final String MAX_PROPOSALS_PARAM = "maxProposals";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        List<DonorsChooseProposal> props = new ArrayList<DonorsChooseProposal>();

        SessionContext context = SessionContextUtil.getSessionContext(request);
        State state = context.getState();

        String schoolID = request.getParameter(SCHOOL_ID_PARAM);
        String cityID = request.getParameter(CITY_ID_PARAM);
        Integer maxProposals = Integer.valueOf(request.getParameter(MAX_PROPOSALS_PARAM));

        try {

            if (state != null && StringUtils.isNumeric(schoolID)) {
                School school = _schoolDao.getSchoolById(state, Integer.valueOf(schoolID));
                ICounty county = _geoDao.findCountyByFipsCode(school.getFipsCountyCode());
                props = _donorsChooseDao.getProposalsForSchool(school, county, maxProposals);
            } else if (StringUtils.isNumeric(cityID)) {
                City city = _geoDao.findCityById(Integer.valueOf(cityID));
                ICounty county = _geoDao.findCountyByFipsCode(city.getCountyFips());
                props = _donorsChooseDao.getProposalsForCity(city, county, maxProposals);
            }

        } catch (Exception e) {
            _log.error("Error getting proposals", e);
        }
        
        model.put("proposals", props);

        if (props != null && !props.isEmpty()) {
            model.put("zoneId", props.toArray(new DonorsChooseProposal[]{})[0].getZoneId());

            Map<String,String> longStateNames = new HashMap<String,String>();
            for (DonorsChooseProposal prop : props) {
                longStateNames.put(prop.getId(), _stateManager.getState(prop.getState()).getLongName());
            }
            model.put("longStateNames", longStateNames);
        }

        boolean isInternalServer = false;
        if (UrlUtil.isDevEnvironment(request.getServerName()) || UrlUtil.isStagingServer(request.getServerName())) {
            isInternalServer = true;
        }
        model.put("isInternalServer", isInternalServer);

        return new ModelAndView(_viewName, model);
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public ISchoolDao getSchoolDao() {
        return _schoolDao;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public StateManager getStateManager() {
        return _stateManager;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }

    public void setDonorsChooseDao(IDonorsChooseDao donorsChooseDao) {
        _donorsChooseDao = donorsChooseDao;
    }
}
