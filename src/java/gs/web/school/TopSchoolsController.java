package gs.web.school;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import gs.web.util.context.SessionContext;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.RedirectView301;
import gs.data.state.State;
import gs.data.state.StateManager;
import gs.data.school.ISchoolDao;
import gs.data.school.School;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * @author thuss
 */
public class TopSchoolsController extends AbstractController {

    public static final String BEAN_ID = "/schools/topSchools.page";

    public static final String MODEL_NATIONAL = "national";
    public static final String MODEL_STATE_NAME = "stateName";
    public static final String MODEL_STATE_ABBREVIATION = "stateAbbreviation";
    public static final String MODEL_TOP_SCHOOLS = "topSchools";
    public static final String MODEL_ALL_STATES = "allStates";

    protected String _viewName;
    protected ISchoolDao _schoolDao;
    protected StateManager _stateManager;
    protected static Cache _cache;

    static {
        // Cache active top schools for 6 hrs for performance
        CacheManager manager = CacheManager.create();
        _cache = new Cache(TopSchoolsController.class.getName(), 51, false, false, 3600, 600);
        manager.addCache(_cache); // You have to add a cache to a manager for it to work
    }

    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SessionContext context = SessionContextUtil.getSessionContext(request);
        State state = context.getState();

        // If someone accidentally enters .../California instead of ../california, 301 them 
        if (!request.getRequestURI().equals(request.getRequestURI().toLowerCase()))
            return new ModelAndView(new RedirectView301(request.getRequestURI().toLowerCase()));

        boolean national = false;
        if (request.getRequestURI().endsWith("schools/")) {
            national = true;
            state = State.CA;
        }

        Map<String, Object> model = new HashMap<String, Object>();
        model.put(MODEL_STATE_NAME, state.getLongName());
        model.put(MODEL_STATE_ABBREVIATION, state.getAbbreviation());
        model.put(MODEL_NATIONAL, national);
        model.put(MODEL_TOP_SCHOOLS, getTopSchools(state));
        model.put(MODEL_ALL_STATES, _stateManager.getList());
        return new ModelAndView(_viewName, model);
    }

    protected List<School> getTopSchools(State state) {
//        String key = state.getAbbreviation() + "_top_schools";
//        Element cacheElement = _cache.get(key);
//        List<School> schools;
//        if (cacheElement == null) {
//            schools = _schoolDao.getTopSchools(state);
//            _cache.put(new Element(key, schools));
//        } else {
//            schools = (List<School>) cacheElement.getObjectValue();
//        }
//        return schools;
        return _schoolDao.getTopSchools(state);         
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public void setSchoolDao(ISchoolDao schoolDao) {
        _schoolDao = schoolDao;
    }

    public void setStateManager(StateManager stateManager) {
        _stateManager = stateManager;
    }
}
