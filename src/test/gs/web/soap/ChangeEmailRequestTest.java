package gs.web.soap;

import gs.web.BaseTestCase;
import gs.data.community.User;

/**
 * Provides testing for the ChangeEmailRequest class.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ChangeEmailRequestTest extends BaseTestCase {
    private ChangeEmailRequest _request;
    private User _user;

    public void setUp() throws Exception {
        super.setUp();
        _request = new ChangeEmailRequest();
        _user = new User();
        _user.setId(123);
        _user.setEmail("aroy@greatschools.net");
    }

    /**
     * Test that normal success conditions result in success
     */
    public void testSuccess() {
        _request.setTarget(ChangeEmailRequest.DEFAULT_TARGET + "?response=success");
        try {
            _request.changeEmailRequest(_user);
        } catch (SoapRequestException e) {
            fail(e.getErrorMessage());
        }
    }

    /**
     * Test that when the server returns an error, this class behaves appropriately
     */
    public void testError() {
        // make the perl script generate an error
        _request.setTarget(ChangeEmailRequest.DEFAULT_TARGET + "?response=error");
        try {
            _request.changeEmailRequest(_user);
            fail("Did not receive expected error");
        } catch (SoapRequestException e) {
            assertEquals("UNKNOWN", e.getErrorCode());
            assertEquals("Could not connect to database", e.getErrorMessage());
            // success
        }
    }

    /**
     * Test timeout condition
     */
    public void testTimeout() {
        // make the perl script sleep for 10 seconds
        _request.setTarget(ChangeEmailRequest.DEFAULT_TARGET + "?response=timeout");
        // set timeout to 1 second (in ms)
        _request.setTimeout(1000);
        try {
            _request.changeEmailRequest(_user);
            fail("Did not receive expected error");
        } catch (SoapRequestException e) {
            assertNotNull(e.getErrorCode());
            assertNotNull(e.getErrorMessage());
            // success
        }
    }
}
