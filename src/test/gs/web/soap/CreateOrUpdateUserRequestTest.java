package gs.web.soap;

import gs.web.BaseTestCase;

/**
 * Provides testing for the CreateOrUpdateUserRequest.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CreateOrUpdateUserRequestTest extends BaseTestCase {
    private CreateOrUpdateUserRequest _request;
    private CreateOrUpdateUserRequestBean _bean;

    public void setUp() throws Exception {
        super.setUp();
        _request = new CreateOrUpdateUserRequest();
        _bean = new CreateOrUpdateUserRequestBean("1", "Anthony", "aroy@greatschools.net");
    }

    /**
     * Test that normal success conditions result in success
     */
    public void testSuccess() {
        _request.setTarget(CreateOrUpdateUserRequest.DEFAULT_TARGET + "?response=success");
        try {
            _request.createOrUpdateUserRequest(_bean);
        } catch (CreateOrUpdateUserRequestException e) {
            fail("Received unexpected exception " + e.getErrorCode() + ": " + e.getErrorMessage());
        }
    }

    /**
     * Test that when the server returns an error, this class behaves appropriately
     */
    public void testError() {
        // make the perl script generate an error
        _request.setTarget(CreateOrUpdateUserRequest.DEFAULT_TARGET + "?response=error");
        try {
            _request.createOrUpdateUserRequest(_bean);
            fail("Did not receive expected error");
        } catch (CreateOrUpdateUserRequestException e) {
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
        _request.setTarget(CreateOrUpdateUserRequest.DEFAULT_TARGET + "?response=timeout");
        // set timeout to 1 second (in ms)
        _request.setTimeout(1000);
        try {
            _request.createOrUpdateUserRequest(_bean);
            fail("Did not receive expected error");
        } catch (CreateOrUpdateUserRequestException e) {
            assertNotNull(e.getErrorCode());
            assertNotNull(e.getErrorMessage());
            // success
        }
    }
}
