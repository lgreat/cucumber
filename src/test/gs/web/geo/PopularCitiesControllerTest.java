package gs.web.geo;

import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import org.easymock.classextension.EasyMock;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createStrictMock;

public class PopularCitiesControllerTest extends BaseControllerTestCase {

    PopularCitiesController _controller;
    StateSpecificFooterHelper _stateSpecificFooterHelper;
    GsMockHttpServletRequest _request;

    public void setUp() throws Exception{
        super.setUp();
        _stateSpecificFooterHelper = createStrictMock(StateSpecificFooterHelper.class);
        _controller = new PopularCitiesController();
        _request = getRequest();
        _request.setMethod("GET");

        _controller.setStateSpecificFooterHelper(_stateSpecificFooterHelper);
    }

    private void resetAllMocks() {
        EasyMock.reset(_stateSpecificFooterHelper);
    }

    private void replayAllMocks() {
        EasyMock.replay(_stateSpecificFooterHelper);
    }

    private void verifyAllMocks() {
        EasyMock.verify(_stateSpecificFooterHelper);
    }

    public void testBasic() throws Exception {
        resetAllMocks();
        State state = State.CA;

        _request.setParameter(PopularCitiesController.PARAM_STATE, state.getAbbreviationLowerCase());

        _stateSpecificFooterHelper.placePopularCitiesInModel(eq(state), isA(Map.class));

        replayAllMocks();

        ModelAndView modelAndView = _controller.handleRequest(_request, getResponse());

        verifyAllMocks();
        assertEquals(state, modelAndView.getModel().get(PopularCitiesController.VIEW_STATE));
        assertNull(modelAndView.getModel().get(PopularCitiesController.PARAM_HIDDEN));
    }

    public void testInvalidState() throws Exception {
        resetAllMocks();

        _request.setParameter(PopularCitiesController.PARAM_STATE, "zz");

        replayAllMocks();

        ModelAndView modelAndView = _controller.handleRequest(_request, getResponse());

        verifyAllMocks();
        assertNull(modelAndView.getModel().get(PopularCitiesController.VIEW_STATE));
    }

    public void testStateNull() throws Exception {
        resetAllMocks();

        replayAllMocks();

        ModelAndView modelAndView = _controller.handleRequest(_request, getResponse());

        verifyAllMocks();
        assertNull(modelAndView.getModel().get(PopularCitiesController.VIEW_STATE));
    }


    public void testHidden() throws Exception {
        resetAllMocks();
        State state = State.CA;
        _request.setParameter(PopularCitiesController.PARAM_STATE, state.getAbbreviationLowerCase());
        _request.setParameter(PopularCitiesController.PARAM_HIDDEN, "true");

        _stateSpecificFooterHelper.placePopularCitiesInModel(eq(state), isA(Map.class));

        replayAllMocks();

        ModelAndView modelAndView = _controller.handleRequest(_request, getResponse());

        verifyAllMocks();
        assertEquals(state, modelAndView.getModel().get(PopularCitiesController.VIEW_STATE));
        assertEquals(true, modelAndView.getModel().get(PopularCitiesController.VIEW_HIDE_MODULE));
    }


}
