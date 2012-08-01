package gs.web.search;

import gs.data.geo.IGeoDao;
import gs.data.geo.bestplaces.BpZip;
import gs.data.school.LevelCode;
import gs.web.ControllerFamily;
import gs.web.IControllerFamilySpecifier;
import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: 3/2/11
 * Time: 1:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class NearbySchoolSearchController2012 extends AbstractCommandController implements IControllerFamilySpecifier {
    private static final Logger _log = Logger.getLogger(NearbySchoolSearchController2012.class);

    private SchoolSearchController2012 _schoolSearchController2012;
    private IGeoDao _geoDao;
    private ControllerFamily _controllerFamily;

    @Override
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException e) throws Exception {
        NearbySchoolSearchCommand nearbyCommand = (NearbySchoolSearchCommand)command;
        BpZip zip;

        boolean invalidZipCode = false;
        if (e.hasErrors()) {
            if (e.hasFieldErrors("distance")) {
                nearbyCommand.setDistance("5");
            }

            if (e.hasFieldErrors("zipCode")) {
                invalidZipCode = true;
            }
        }

        zip = _geoDao.findZip(nearbyCommand.getZipCode());
        if (zip == null) {
            invalidZipCode = true;
        }

        if (invalidZipCode) {
            String selectedGradeLevel = null;

            if (nearbyCommand.hasGradeLevels()) {
                String[] gradeLevels = nearbyCommand.getGradeLevels();
                if (gradeLevels.length == 1) {
                    LevelCode levelCode = LevelCode.createLevelCode(gradeLevels[0]);
                    if (levelCode != null && !levelCode.hasNoLevelCodes()) {
                        selectedGradeLevel = levelCode.getLowestLevel().getName();
                    }
                }
            }
            return new ModelAndView(new RedirectView(buildRedirectUrl(nearbyCommand.getRedirectUrl(),
                    nearbyCommand.getDistance(), selectedGradeLevel)));
        }

        // GS-11511 - nearby search by zip code
        Map<String,Object> nearbySearchInfo = new HashMap<String,Object>();
        nearbySearchInfo.put("zipCode", zip.getZip());
        nearbySearchInfo.put("state", zip.getState());
        nearbySearchInfo.put("city", zip.getName());
        request.setAttribute("nearbySearchInfo", nearbySearchInfo);

        SchoolSearchCommand searchCommand = nearbyCommand.getSchoolSearchCommand();
        searchCommand.setLat((double)zip.getLat());
        searchCommand.setLon((double)zip.getLon());
        searchCommand.setState(zip.getState().getAbbreviationLowerCase());
        searchCommand.setCity(zip.getName());
        searchCommand.setZipCode(zip.getZip());

        return _schoolSearchController2012.handle(request, response, searchCommand, e);
    }

    private static String buildRedirectUrl(String redirectUrl, String distance, String selectedGradeLevel) {
        if (redirectUrl == null || !redirectUrl.startsWith("/")) {
            // default to home page
            redirectUrl = "/";
        }
        return redirectUrl +
                (redirectUrl.contains("?") ? "&" : "?") +
                "invalidZipCode=true" +
                (distance != null ? "&distance=" + distance : "") +
                (selectedGradeLevel != null ? "&gradeLevels=" + selectedGradeLevel : "");
    }

    public SchoolSearchController2012 getSchoolSearchController2012() {
        return _schoolSearchController2012;
    }

    public void setSchoolSearchController2012(SchoolSearchController2012 schoolSearchController2012) {
        _schoolSearchController2012 = schoolSearchController2012;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }

    public ControllerFamily getControllerFamily() {
        return _controllerFamily;
    }

    public void setControllerFamily(ControllerFamily controllerFamily) {
        _controllerFamily = controllerFamily;
    }
}
