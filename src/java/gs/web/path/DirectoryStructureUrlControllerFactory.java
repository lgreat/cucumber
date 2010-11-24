package gs.web.path;

import gs.data.geo.City;
import gs.data.geo.IGeoDao;
import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @author Young Fan
 */
public class DirectoryStructureUrlControllerFactory implements IDirectoryStructureUrlControllerFactory {
    private static Logger _log = Logger.getLogger(DirectoryStructureUrlControllerFactory.class);

    private HttpServletRequest _request;

    private List<IDirectoryStructureUrlController> _controllers;

    private IDistrictDao _districtDao;

    private IGeoDao _geoDao;

    public IDirectoryStructureUrlController getController() {
        if (_request == null) {
            throw new IllegalStateException("Request was null.");
        }

        // extract request information from the request uri
        DirectoryStructureUrlFields fields = new DirectoryStructureUrlFields(_request);

        State state = fields.getState();
        String cityName = fields.getCityName();
        String districtName = fields.getDistrictName();

        // district object for convenience
        if (!StringUtils.isBlank(districtName) && state != null && !StringUtils.isBlank(cityName)) {
            District district = getDistrictDao().findDistrictByNameAndCity(state, districtName, cityName);
            // might be null
            fields.setDistrict(district);
        }

        // city object for convenience
        if (StringUtils.isNotBlank(fields.getCityName()) && fields.getState() != null) {
            City city = getGeoDao().findCity(fields.getState(), fields.getCityName());
            // might be null
            fields.setCity(city);
        }
        // set the fields in the request instead of in the controller itself or else different requests would
        // be using each others' fields!
        _request.setAttribute(IDirectoryStructureUrlController.FIELDS, fields);

        // pick the appropriate controller to handle the request
        for (IDirectoryStructureUrlController controller : _controllers) {
            if (controller.shouldHandleRequest(fields)) {
                return controller;
            }
        }

        return null;
    }

    // auto-wired
    public void setRequest(HttpServletRequest request) {
        _request = request;
    }

    // explicitly set in spring config file
    public void setControllers(List<IDirectoryStructureUrlController> controllers) {
        _controllers = controllers;
    }

    public IDistrictDao getDistrictDao() {
        return _districtDao;
    }

    public void setDistrictDao(IDistrictDao districtDao) {
        _districtDao = districtDao;
    }

    public IGeoDao getGeoDao() {
        return _geoDao;
    }

    public void setGeoDao(IGeoDao geoDao) {
        _geoDao = geoDao;
    }
}
