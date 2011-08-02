package gs.web.geo;

import gs.data.state.State;
import gs.web.util.context.SessionContextUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class PopularCitiesController extends AbstractController {
    StateSpecificFooterHelper _stateSpecificFooterHelper;
    private String _viewName;
    SessionContextUtil _sessionContextUtil;

    public static final String PARAM_STATE = "state";
    public static final String PARAM_HIDDEN = "hidden";

    public static final String VIEW_STATE = "state";
    public static final String VIEW_HIDE_MODULE = "hidePopularCitiesModule";

    private static final Logger _log = Logger.getLogger(PopularCitiesController.class);

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String,Object> model = new HashMap<String,Object>();

        String stateString = request.getParameter(PARAM_STATE);
        if (StringUtils.isNotBlank(stateString)) {
            try {
                 State state = State.fromString(stateString);
                _stateSpecificFooterHelper.placePopularCitiesInModel(state, model);
                model.put(VIEW_STATE,state);
            } catch (Exception e) {
                _log.debug("Cannot get state for string:" + stateString);
            }
        }

        String hiddenString = request.getParameter(PARAM_HIDDEN);
        if (StringUtils.isNotBlank(hiddenString) && Boolean.valueOf(hiddenString) == true) {
            model.put(VIEW_HIDE_MODULE,true);
        }

        return new ModelAndView(getViewName(),model);
    }

    public StateSpecificFooterHelper getStateSpecificFooterHelper() {
        return _stateSpecificFooterHelper;
    }

    public void setStateSpecificFooterHelper(StateSpecificFooterHelper stateSpecificFooterHelper) {
        _stateSpecificFooterHelper = stateSpecificFooterHelper;
    }

    public String getViewName() {
        return _viewName;
    }

    public void setViewName(String viewName) {
        _viewName = viewName;
    }

    public SessionContextUtil getSessionContextUtil() {
        return _sessionContextUtil;
    }

    public void setSessionContextUtil(SessionContextUtil sessionContextUtil) {
        _sessionContextUtil = sessionContextUtil;
    }
}
