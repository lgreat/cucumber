package gs.web.path;

import gs.web.GsMockHttpServletRequest;
import gs.web.BaseControllerTestCase;
import gs.web.util.context.SessionContextUtil;
import gs.web.util.RedirectView301;
import gs.web.school.SchoolsController;
import gs.web.school.SchoolOverviewController;
import gs.data.school.district.IDistrictDao;
import gs.data.school.ISchoolDao;
import gs.data.search.Searcher;
import gs.data.geo.IGeoDao;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: youngfan
 * Date: Sep 24, 2008
 * Time: 12:28:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryStructureUrlRequestControllerTest extends BaseControllerTestCase {
    private DirectoryStructureUrlRequestController _controller;
    private SessionContextUtil _sessionContextUtil;

    private DirectoryStructureUrlControllerFactory _controllerFactory;

    private SchoolsController _schoolsController =
        (SchoolsController) getApplicationContext().getBean(SchoolsController.BEAN_ID);
    private SchoolOverviewController _schoolOverviewController =
        (SchoolOverviewController) getApplicationContext().getBean(SchoolOverviewController.BEAN_ID);
    private List<IDirectoryStructureUrlController> _controllers = new ArrayList<IDirectoryStructureUrlController>();

    protected void setUp() throws Exception {
        super.setUp();

        _controller = new DirectoryStructureUrlRequestController();
        _controller.setApplicationContext(getApplicationContext());
        _sessionContextUtil = (SessionContextUtil) getApplicationContext().getBean(SessionContextUtil.BEAN_ID);

        _controllers.clear();
        _controllers.add(_schoolsController);
        _controllers.add(_schoolOverviewController);

        _controllerFactory = new DirectoryStructureUrlControllerFactory();
        _controllerFactory.setControllers(_controllers);
    }

    public void testHandleRequestInternalRedirects() throws Exception {
        GsMockHttpServletRequest request = getRequest();
        request.setMethod("GET");

        // adding trailing slash
        request.removeAllParameters();
        request.setQueryString(null);
        request.setRequestURI("/alaska/anchorage");
        _controllerFactory.setRequest(request);
        _controller.setController(_controllerFactory.getController());
        ModelAndView mAndV = _controller.handleRequestInternal(request, getResponse());
        assertTrue(mAndV.getView() instanceof RedirectView);
        assertEquals("/alaska/anchorage/", ((RedirectView) mAndV.getView()).getUrl());

        // redirect request url with capitalized state name to same url with lowercased state name
        request.removeAllParameters();
        request.setQueryString(null);
        request.setRequestURI("/California/sonoma/private-charter/elementary-schools/");
        _controllerFactory.setRequest(request);
        _controller.setController(_controllerFactory.getController());
        mAndV = _controller.handleRequestInternal(request, getResponse());
        assertTrue(mAndV.getView() instanceof RedirectView301);
        assertEquals("/california/sonoma/private-charter/elementary-schools/", ((RedirectView301) mAndV.getView()).getUrl());

        // valid new-style city browse request
        request.removeAllParameters();
        request.setQueryString(null);
        request.setRequestURI("/california/alameda/elementary-schools/");
        _controllerFactory.setRequest(request);
        _controller.setController(_controllerFactory.getController());
        mAndV = _controller.handleRequestInternal(request, getResponse());
        assertEquals(_schoolsController.getViewName(), mAndV.getViewName());

        // invalid new-style city browse request
        request.removeAllParameters();
        request.setQueryString(null);
        request.setRequestURI("/california/alameda/elementary/private-charter/schools/");
        _controllerFactory.setRequest(request);
        _controller.setController(_controllerFactory.getController());
        mAndV = _controller.handleRequestInternal(request, getResponse());
        assertEquals("status/error", mAndV.getViewName());
    }


    public void testIsRequestURIWithTrailingSlash() throws Exception {
        GsMockHttpServletRequest request = getRequest();

        request.setRequestURI(null);
        boolean foundException = false;
        try {
            DirectoryStructureUrlRequestController.isRequestURIWithTrailingSlash(request);
        } catch (IllegalArgumentException e) {
            foundException = true;
        }
        assertTrue("Expected IllegalArgumentException when request URI null", foundException);

        request.setRequestURI("/california/san-francisco");
        assertFalse("Expected false return value", DirectoryStructureUrlRequestController.isRequestURIWithTrailingSlash(request));

        request.setRequestURI("/california/san-francisco/");
        assertTrue("Expected true return value", DirectoryStructureUrlRequestController.isRequestURIWithTrailingSlash(request));
    }

    public void testCreateURIWithTrailingSlash() throws Exception {
        GsMockHttpServletRequest request = getRequest();

        request.setRequestURI(null);
        boolean foundException = false;
        try {
            DirectoryStructureUrlRequestController.createURIWithTrailingSlash(request);
        } catch (IllegalArgumentException e) {
            foundException = true;
        }
        assertTrue("Expected IllegalArgumentException when request URI null", foundException);

        request.setRequestURI("/california/san-francisco");
        assertEquals("Expected appended trailing slash", "/california/san-francisco/", DirectoryStructureUrlRequestController.createURIWithTrailingSlash(request));

        request.setRequestURI("/california/san-francisco/");
        assertEquals("Expected unmodified request url", "/california/san-francisco/", DirectoryStructureUrlRequestController.createURIWithTrailingSlash(request));

        request.setRequestURI("/california/san-francisco");
        request.setQueryString(SchoolsController.PARAM_RESULTS_PER_PAGE + "=30");
        request.setParameter(SchoolsController.PARAM_RESULTS_PER_PAGE, String.valueOf(30));
        assertEquals("Expected appended trailing slash on request uri", "/california/san-francisco/", DirectoryStructureUrlRequestController.createURIWithTrailingSlash(request));

        request.setRequestURI("/california/san-francisco/");
        request.setQueryString(SchoolsController.PARAM_RESULTS_PER_PAGE + "=30");
        request.setParameter(SchoolsController.PARAM_RESULTS_PER_PAGE, String.valueOf(30));
        assertEquals("Expected unmodified request uri", "/california/san-francisco/", DirectoryStructureUrlRequestController.createURIWithTrailingSlash(request));
    }
}
