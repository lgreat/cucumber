package gs.web.search;

import gs.data.geo.IGeoDao;
import gs.data.geo.bestplaces.BpZip;
import gs.data.school.LevelCode;
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
public class NearbySchoolSearchController extends AbstractCommandController {
    private static final Logger _log = Logger.getLogger(NearbySchoolSearchController.class);

    private SchoolSearchController _schoolSearchController;
    private IGeoDao _geoDao;

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
                    if (levelCode != null) {
                        selectedGradeLevel = levelCode.getLowestLevel().getName();
                    }
                }
            }
            return new ModelAndView(new RedirectView("/?invalidZipCode=true" +
                    (nearbyCommand.getDistance() != null ? "&distance=" + nearbyCommand.getDistance() : "") +
                    (selectedGradeLevel != null ? "&gradeLevels=" + selectedGradeLevel : "")));
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

        return _schoolSearchController.handle(request, response, searchCommand, e);
    }

    public SchoolSearchController getSchoolSearchController() {
        return _schoolSearchController;
    }

    public void setSchoolSearchController(SchoolSearchController schoolSearchController) {
        _schoolSearchController = schoolSearchController;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }
}
