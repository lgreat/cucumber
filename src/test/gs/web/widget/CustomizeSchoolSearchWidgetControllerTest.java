package gs.web.widget;

import gs.web.BaseControllerTestCase;
import gs.data.admin.cobrand.ICobrandDao;
import gs.data.community.IUserDao;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @author npatury@greatschools.org
 */
public class CustomizeSchoolSearchWidgetControllerTest extends BaseControllerTestCase{
    private CustomizeSchoolSearchWidgetController _controller;
    private ICobrandDao _cobrandDao;
    private IUserDao _userDao;
    private SchoolSearchWidgetController _schoolSearchWidgetController;
    @Override
    protected void setUp() throws Exception{
        super.setUp();
        _controller = new CustomizeSchoolSearchWidgetController();
        //_controller.setCommandName("command");
        _controller.setCommandClass(CustomizeSchoolSearchWidgetCommand.class);
        _controller.setFormView("widget/customizeSchoolSearchWidget");
        _controller.setSuccessView("widget/customizeSchoolSearchWidget");
        _cobrandDao = createMock(ICobrandDao.class);
        _controller.setCobrandDao(_cobrandDao);
        _schoolSearchWidgetController = createMock(SchoolSearchWidgetController.class);
        _controller.setSchoolSearchWidgetController(_schoolSearchWidgetController);
        
    }

    public void testBasicGet() throws Exception {
        getRequest().setMethod("GET");
        ModelAndView mv = _controller.handleRequest(getRequest(),getResponse());
        assertEquals("widget/customizeSchoolSearchWidget",mv.getViewName());

    }

    public void testBasicPost() throws Exception {
        getRequest().setMethod("POST");
        _schoolSearchWidgetController.parseSearchQuery(isA(String.class),
                isA(SchoolSearchWidgetCommand.class), isA(HttpServletRequest.class), isA(BindException.class));
        replay(_schoolSearchWidgetController);
        ModelAndView mv =_controller.handleRequest(getRequest(),getResponse());
        assertEquals("widget/customizeSchoolSearchWidget",mv.getViewName());

    }




}
