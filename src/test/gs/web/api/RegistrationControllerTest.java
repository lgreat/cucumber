package gs.web.api;

//import gs.web.BaseControllerTestCase;
import java.util.List;
import java.util.Map;

import gs.web.BaseControllerTestCase;
import gs.data.api.IApiAccountDao;
import gs.data.api.ApiAccount;
import gs.data.util.email.MockJavaMailSender;
import gs.data.util.email.EmailHelperFactory;
import gs.data.integration.exacttarget.ExactTargetAPI;
import gs.data.community.User;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author chriskimm
 */
public class RegistrationControllerTest extends BaseControllerTestCase {

    private RegistrationController _controller;
    private IApiAccountDao _apiAccountDao;
    private MockJavaMailSender _javaMailSender;
    private ExactTargetAPI _exactTargetAPI;

    private String SUCCESS_VIEW = "api/success";

    @Override
    public void setUp() throws Exception {
        _controller = new RegistrationController();
        _controller.setSuccessView(SUCCESS_VIEW);
        _apiAccountDao = createMock(IApiAccountDao.class);
        _controller.setApiAccountDao(_apiAccountDao);
        EmailHelperFactory _factory = new EmailHelperFactory();
        _javaMailSender = new MockJavaMailSender();
        _javaMailSender.setHost("greatschools.net");
        _factory.setMailSender(_javaMailSender);
        _controller.setEmailHelperFactory(_factory);
        _exactTargetAPI = createStrictMock(ExactTargetAPI.class);
        _controller.setExactTargetAPI(_exactTargetAPI);
    }

    public void testFormSubmit() throws Exception {
        ApiAccount command = new ApiAccount();
        command.setName("tommy");
        command.setOrganization("GreatSchools");
        command.setEmail("tommy@gs.net");
        command.setIndustry("education");
        _apiAccountDao.save(command);
        User user = new User();
        user.setEmail(command.getEmail());
        _exactTargetAPI.sendTriggeredEmail(eq("apiWelcomeMessage"),isA(User.class));
        replay(_apiAccountDao);
        replay(_exactTargetAPI);

        ModelAndView mAndV = _controller.onSubmit(command);
       
        verify(_apiAccountDao);
        verify(_exactTargetAPI);

        List messages = _javaMailSender.getSentMessages();
        assertEquals(1, messages.size());
        
        assertEquals(SUCCESS_VIEW, mAndV.getViewName());
    }


}
