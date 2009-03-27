package gs.web.district;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

import gs.data.school.district.District;
import gs.data.school.district.IDistrictDao;
import gs.data.state.State;
import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;

/**
 * @author droy@greatschools.net
 * @author npatury@greatschools.net
 */
public class DistrictHomeController extends AbstractController {
    public static final String PARAM_DISTRICT_ID = "district_id";
    String _viewName;

    private IDistrictDao _districtDao;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        final SessionContext sessionContext = SessionContextUtil.getSessionContext(request);
        State state = sessionContext.getStateOrDefault();
        String districtIdStr = request.getParameter(PARAM_DISTRICT_ID);
        if (!StringUtils.isBlank(districtIdStr) && StringUtils.isNumeric(districtIdStr)) {
            int districtId = Integer.parseInt(districtIdStr);
            District district = _districtDao.findDistrictById(state, districtId);
            model.put("district", district);
        }
        
        return new ModelAndView(getViewName(), model);
    }

    public IDistrictDao getDistrictDao() {
        return _districtDao;
    }

    public void setDistrictDao(IDistrictDao districtDao) {
        _districtDao = districtDao;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }
}
