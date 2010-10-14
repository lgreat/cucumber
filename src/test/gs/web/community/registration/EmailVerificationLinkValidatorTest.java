package gs.web.community.registration;

import gs.data.community.IUserDao;
import gs.data.community.User;
import gs.data.util.DigestUtil;
import gs.web.BaseTestCase;
import org.springframework.validation.MapBindingResult;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;

public class EmailVerificationLinkValidatorTest extends BaseTestCase {

    EmailVerificationLinkValidator _emailVerificationLinkValidator;
    IUserDao _userDao;

    public void setUp() {
        _emailVerificationLinkValidator = new EmailVerificationLinkValidator();
        _userDao = createStrictMock(IUserDao.class);
        _emailVerificationLinkValidator.setUserDao(_userDao);
    }

    public void testValidate() throws Exception {

        Map<String,String> map = new HashMap<String,String>();
        MapBindingResult mapBindingResult = new MapBindingResult(map, "emailVerificationLink");
        EmailVerificationLinkCommand emailVerificationLinkCommand = new EmailVerificationLinkCommand();

        User user = new User();
        user.setId(1234);
        user.setEmail("test@greatschools.org");

        Date now = new Date();

        String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        hash = DigestUtil.hashString(hash + now.getTime());

        String badId = "alalj";
        String goodId = hash + user.getId();
        String emptyId = "";

        emailVerificationLinkCommand.setHashPlusUserId(badId);
        emailVerificationLinkCommand.setDate(String.valueOf(now.getTime()));
        _emailVerificationLinkValidator.validate(emailVerificationLinkCommand, mapBindingResult);
        assertTrue("Command should have error", mapBindingResult.hasFieldErrors("hashPlusUserId"));

        expect(_userDao.findUserFromId(user.getId())).andReturn(user);
        replay(_userDao);
        mapBindingResult = new MapBindingResult(map, "emailVerificationLink");
        emailVerificationLinkCommand.setHashPlusUserId(goodId);
        emailVerificationLinkCommand.setDate(String.valueOf(now.getTime()));
        _emailVerificationLinkValidator.validate(emailVerificationLinkCommand, mapBindingResult);
        assertFalse("Request should have validated", mapBindingResult.hasErrors());
        verify(_userDao);
        reset(_userDao);

        mapBindingResult = new MapBindingResult(map, "emailVerificationLink");
        emailVerificationLinkCommand.setHashPlusUserId(emptyId);
        emailVerificationLinkCommand.setDate(String.valueOf(now.getTime()));
        _emailVerificationLinkValidator.validate(emailVerificationLinkCommand, mapBindingResult);
        assertTrue("Command should have error", mapBindingResult.hasFieldErrors("hashPlusUserId"));
    }

    public void testValidateExpiredLink() throws Exception {
        Map<String,String> map = new HashMap<String,String>();
        MapBindingResult mapBindingResult = new MapBindingResult(map, "emailVerificationLink");
        EmailVerificationLinkCommand emailVerificationLinkCommand = new EmailVerificationLinkCommand();

        Date now = new Date();

        emailVerificationLinkCommand.setDate(String.valueOf((now.getTime() - Long.valueOf(EmailVerificationLinkValidator.EXPIRE_DURATION_IN_MILLIS)) - 1l));
        _emailVerificationLinkValidator.validate(emailVerificationLinkCommand, mapBindingResult);
        assertTrue("Command should have error", mapBindingResult.hasFieldErrors("dateSentAsString"));
    }

    public void testVerificationHashMatchesUser() throws Exception {
        EmailVerificationLinkCommand emailVerificationLinkCommand = new EmailVerificationLinkCommand();

        Date now = new Date();

        User user = new User();
        user.setId(1);
        user.setEmail("test@greatschools.org");

        String hash = DigestUtil.hashStringInt(user.getEmail(), user.getId());
        hash = DigestUtil.hashString(hash + now.getTime());

        emailVerificationLinkCommand.setUser(user);
        emailVerificationLinkCommand.setHashPlusUserId(hash + user.getId());
        emailVerificationLinkCommand.setDate(String.valueOf(now.getTime()));

        assertTrue("user's hash should match input hash",  _emailVerificationLinkValidator.verificationHashMatchesUser(emailVerificationLinkCommand));
    }

    public void testVerificationHashInvalid() throws Exception {
        EmailVerificationLinkCommand emailVerificationLinkCommand = new EmailVerificationLinkCommand();

        Date now = new Date();

        User user = new User();
        user.setId(1);
        user.setEmail("test@greatschools.org");

        String hash = "invalid";

        emailVerificationLinkCommand.setUser(user);
        emailVerificationLinkCommand.setHashPlusUserId(hash + user.getId());
        emailVerificationLinkCommand.setDate(String.valueOf(now.getTime()));

        assertFalse("user's hash should not match input hash",  _emailVerificationLinkValidator.verificationHashMatchesUser(emailVerificationLinkCommand));
    }

}
