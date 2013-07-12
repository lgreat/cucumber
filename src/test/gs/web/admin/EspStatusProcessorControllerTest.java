package gs.web.admin;

import gs.data.community.User;
import gs.data.school.*;
import gs.data.state.INoEditDao;
import gs.data.state.State;
import gs.web.BaseControllerTestCase;
import gs.web.GsMockHttpServletRequest;
import gs.web.school.EspFormExternalDataHelper;
import gs.web.school.EspFormValidationHelper;
import gs.web.school.EspSaveHelper;
import gs.web.school.usp.EspStatus;
import gs.web.school.usp.EspStatusManager;
import org.easymock.classextension.EasyMock;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static org.easymock.EasyMock.*;


public class EspStatusProcessorControllerTest extends BaseControllerTestCase {
    private IEspResponseDao _espResponseDao;
    private ISchoolDao _schoolDao;
    private BeanFactory _beanFactory;
    EspStatusManager _espStatusManager;
    EspStatusProcessorController _controller;
    private JavaMailSender _mailSender;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _espResponseDao = createMock(IEspResponseDao.class);
        _schoolDao = createMock(ISchoolDao.class);
        _beanFactory = EasyMock.createStrictMock(BeanFactory.class);
        _espStatusManager = EasyMock.createStrictMock(EspStatusManager.class);
        _mailSender = EasyMock.createStrictMock(JavaMailSender.class);

        _controller = new EspStatusProcessorController();
        _controller.setSchoolDao(_schoolDao);
        _controller.setEspResponseDao(_espResponseDao);
        _controller.setBeanFactory(_beanFactory);
        _controller.setMailSender(_mailSender);
    }

    private void resetAllMocks() {
        resetMocks(_schoolDao, _espResponseDao, _beanFactory, _espStatusManager, _mailSender);
    }

    private void replayAllMocks() {
        replayMocks(_schoolDao, _espResponseDao, _beanFactory, _espStatusManager, _mailSender);
    }

    private void verifyAllMocks() {
        verifyMocks(_schoolDao, _espResponseDao, _beanFactory, _espStatusManager, _mailSender);
    }

    public void testProcessEspSchoolStatus_nullParams() {
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();

        replayAllMocks();
        _controller.processEspSchoolStatus(request, response);
        verifyAllMocks();
    }

    public void testProcessEspSchoolStatus_invalidStateParam() {
        GsMockHttpServletRequest request = getRequest();
        request.setParameter(_controller.PARAM_STATE, "asd");
        request.setParameter(_controller.PARAM_SCHOOL_IDS, "1,2");
        HttpServletResponse response = getResponse();

        replayAllMocks();
        _controller.processEspSchoolStatus(request, response);
        verifyAllMocks();
    }

    public void testProcessEspSchoolStatus() {
        GsMockHttpServletRequest request = getRequest();
        request.setParameter(_controller.PARAM_STATE, "mo");
        // 2nd school Id is invalid. It should be ignored and continue processing the rest of the items in the list.
        request.setParameter(_controller.PARAM_SCHOOL_IDS, "1,asd,2");
        HttpServletResponse response = getResponse();

        expect(_schoolDao.getSchoolById(State.MO, 1)).andReturn(new School());
        expect(_beanFactory.getBean(eq(EspStatusManager.BEAN_NAME), isA(School.class))).andReturn(_espStatusManager);
        expect(_espStatusManager.getEspStatus()).andReturn(EspStatus.OSP_PREFERRED);
        _espResponseDao.deactivateResponses(isA(School.class),isA(Set.class));

        expect(_schoolDao.getSchoolById(State.MO, 2)).andReturn(new School());
        expect(_beanFactory.getBean(eq(EspStatusManager.BEAN_NAME), isA(School.class))).andReturn(_espStatusManager);
        expect(_espStatusManager.getEspStatus()).andReturn(EspStatus.OSP_OUTDATED);

        _mailSender.send(isA(SimpleMailMessage.class));

        replayAllMocks();
        _controller.processEspSchoolStatus(request, response);
        verifyAllMocks();
    }
}