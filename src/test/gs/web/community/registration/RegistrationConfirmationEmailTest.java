/**
 * Copyright (c) 2006 GreatSchools.org. All Rights Reserved.
 */
package gs.web.community.registration;

import gs.web.BaseControllerTestCase;
import gs.data.util.email.MockJavaMailSender;
import gs.data.util.email.EmailHelperFactory;
import gs.data.community.User;
import gs.data.community.UserProfile;
import gs.data.state.State;
import gs.data.geo.IGeoDao;
import gs.data.geo.City;

import javax.mail.MessagingException;
import javax.mail.Message;
import java.io.IOException;
import java.util.List;

import static org.easymock.EasyMock.*;

/**
 * Provides testing for the RegistrationConfirmationEmail bean.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RegistrationConfirmationEmailTest extends BaseControllerTestCase {
    private static final String FROM_EMAIL = "aroy@greatschools.net";
    private static final String FROM_NAME = "Anthony";
    private static final String SUBJECT = "Testing";
    private RegistrationConfirmationEmail _email;
    private MockJavaMailSender _mailSender;
    private IGeoDao _geoDao;

    public void setUp() throws Exception {
        super.setUp();
        _email = new RegistrationConfirmationEmail();
        _email.setFromEmail(FROM_EMAIL);
        _email.setFromName(FROM_NAME);
        _email.setSubject(SUBJECT);
        _mailSender = new MockJavaMailSender();
        _mailSender.setHost("greatschools.net");
        EmailHelperFactory _factory = new EmailHelperFactory();
        _factory.setMailSender(_mailSender);
        _email.setEmailHelperFactory(_factory);
        _geoDao = createStrictMock(IGeoDao.class);
        _email.setGeoDao(_geoDao);
    }

    public void testSend() throws MessagingException, IOException {
        // verify init
        assertEquals(FROM_EMAIL, _email.getFromEmail());
        assertEquals(SUBJECT, _email.getSubject());
        assertEquals(FROM_NAME, _email.getFromName());
        // setup
        User user = new User();
        user.setEmail("aroy+1@greatschools.net");
        user.setUserProfile(new UserProfile());
        user.getUserProfile().setScreenName("Anthony");
        user.getUserProfile().setState(State.CA);
        user.getUserProfile().setCity("Alameda");
        City city = new City();
        city.setId(123);
        city.setName("Alameda");
        expect(_geoDao.findCity(State.CA, "Alameda")).andReturn(city);
        // call
        replay(_geoDao);
        _email.sendToUser(user, "initpass", getRequest());
        verify(_geoDao);
        // verify
        List msgs = _mailSender.getSentMessages();
        assertNotNull(msgs);
        assertEquals(1, msgs.size());
        Message msg = (Message) msgs.get(0);
        assertEquals(SUBJECT, msg.getSubject());
        assertTrue(msg.getFrom()[0].toString().indexOf(FROM_NAME) > -1);
        assertTrue(msg.getFrom()[0].toString().indexOf(FROM_EMAIL) > -1);
        assertNotNull(msg.getContent());
    }

    public void xtestRealSend() throws Exception {
        RegistrationConfirmationEmail email = (RegistrationConfirmationEmail)
                getApplicationContext().getBean(RegistrationConfirmationEmail.BEAN_ID);
        User user = new User();
        user.setUserProfile(new UserProfile());
        user.getUserProfile().setScreenName("Test GS-7601");
        user.getUserProfile().setState(State.CA);
        user.getUserProfile().setCity("Alameda");
        user.setEmail("aroy@greatschools.net");

        email.sendToUser(user, "password", getRequest());
    }
}
