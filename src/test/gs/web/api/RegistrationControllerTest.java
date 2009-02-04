package gs.web.api;

//import gs.web.BaseControllerTestCase;
import java.util.List;
import gs.web.BaseControllerTestCase;
import gs.data.api.IApiAccountDao;
import gs.data.api.ApiAccount;
import static org.easymock.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author chriskimm
 */
public class RegistrationControllerTest extends BaseControllerTestCase {

    private RegistrationController _controller;
    private IApiAccountDao _apiAccountDao;

    private String SUCCESS_VIEW = "api/success";
    @Override
    public void setUp() throws Exception {
        _controller = new RegistrationController();
        _controller.setSuccessView(SUCCESS_VIEW);
        _apiAccountDao = createMock(IApiAccountDao.class);
        _controller.setApiAccountDao(_apiAccountDao);
    }

    public void testFormSubmit() throws Exception {
        ApiAccount command = new ApiAccount();
        command.setName("tommy");
        command.setOrganization("GreatSchools");
        command.setEmail("tommy@gs.net");
        _apiAccountDao.save(command);
        replay(_apiAccountDao);
        ModelAndView mAndV = _controller.onSubmit(command);
        assertEquals(SUCCESS_VIEW, mAndV.getViewName());
        verify(_apiAccountDao);
    }
}
