package gs.web.district;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gs.data.school.district.District;
import gs.data.school.district.NearbyDistrict;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import java.util.List;

/**
 * @author npatury
 */

public class NearbyDistrictController extends AbstractController {

    private IDistrictDao _districtDao;
    public static final String PARAM_DISTRICT_ID = "district_id";
    public static final String PARAM_STATE = "state";
    public static final String PARAM_LIMIT = "limit";
    public static final String MODEL_NEARBYDISTRICT_LIST ="nearbyDistricts";

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String stateParam = request.getParameter(PARAM_STATE);
        State state = State.fromString(stateParam);
        String districtIdParam = request.getParameter(PARAM_DISTRICT_ID);
        District district = _districtDao.findDistrictById(state, Integer.parseInt(districtIdParam));
        ModelAndView modelAndView = new ModelAndView("/district/nearbyDistricts");
        List<NearbyDistrict> nearbyDistricts =  _districtDao.findNearbyDistricts(district,Integer.parseInt(request.getParameter(PARAM_LIMIT)));
        if(nearbyDistricts.size() > 0){
            modelAndView.addObject(MODEL_NEARBYDISTRICT_LIST, nearbyDistricts);
        }
        return modelAndView;
    }

    public IDistrictDao getDistrictDao() {
        return _districtDao;
    }

    public void setDistrictDao(IDistrictDao districtDao) {
        _districtDao = districtDao;
    }

}
