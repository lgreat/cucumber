package gs.web.soap;

import gs.data.community.User;
import gs.data.soap.SoapRequestException;
import gs.web.BaseTestCase;
import org.apache.axis.client.Call;
import static org.easymock.classextension.EasyMock.*;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import java.rmi.RemoteException;

/**
 * Provides testing for the ChangePasswordRequest class.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ChangePasswordRequestTest extends BaseTestCase {
    private ChangePasswordRequest _request;
    private User _user;
    private Call _call;

    public void setUp() throws Exception {
        super.setUp();
        _request = new ChangePasswordRequest();
        _user = new User();
        _user.setId(123);
        _call = createMock(Call.class);
        _call.addParameter((String) anyObject(), (QName) anyObject(), (ParameterMode) anyObject());
        expectLastCall().times(2);
        _request.setMockCall(_call);
    }

    /**
     * Test that normal success conditions result in success
     */
    public void testSuccess() throws RemoteException {
        expect(_call.invoke((Object[])anyObject())).andReturn(null);
        replay(_call);

        _request.setMockCall(_call);
        try {
            _request.changePasswordRequest(_user);
        } catch (SoapRequestException e) {
            fail(e.getErrorMessage());
        }

        verify(_call);
    }

    /**
     * Test that returning id as string results in success
     */
    public void testSuccessString() throws RemoteException {
        expect(_call.invoke((Object[])anyObject())).andReturn("123");
        replay(_call);

        _request.setMockCall(_call);
        try {
            _request.changePasswordRequest(_user);
        } catch (SoapRequestException e) {
            fail(e.getErrorMessage());
        }

        verify(_call);
    }

    /**
     * Test that returning id as integer results in success
     */
    public void testSuccessInteger() throws RemoteException {
        expect(_call.invoke((Object[])anyObject())).andReturn(123);
        replay(_call);

        _request.setMockCall(_call);
        try {
            _request.changePasswordRequest(_user);
        } catch (SoapRequestException e) {
            fail(e.getErrorMessage());
        }

        verify(_call);
    }

    public void testIsDisabled() {
        _request.setTarget(null);
        _request.setMockCall(null);

        try {
            _request.changePasswordRequest(_user);
            // success -- null target, but no exceptions, means no call was made
        } catch (SoapRequestException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    /**
     * Test that a failed response results in an exception
     */
    public void testInvalidResponse() throws RemoteException {
        expect(_call.invoke((Object[])anyObject())).andReturn(new SoapRequestException());
        replay(_call);

        try {
            _request.changePasswordRequest(_user);
            fail("Didn't receive expected exception");
        } catch (SoapRequestException e) {
            // success
        }

        verify(_call);
    }

    /**
     * Test that a failed response results in an exception
     */
    public void testInvalidResponseWrongId() throws RemoteException {
        expect(_call.invoke((Object[])anyObject())).andReturn("321");
        replay(_call);

        try {
            _request.changePasswordRequest(_user);
            fail("Didn't receive expected exception");
        } catch (SoapRequestException e) {
            // success
        }

        verify(_call);
    }

    /**
     * Test that a failed response results in an exception
     */
    public void testUnexpectedException() throws RemoteException {
        expect(_call.invoke((Object[])anyObject())).andThrow(new RemoteException());
        replay(_call);

        try {
            _request.changePasswordRequest(_user);
            fail("Didn't receive expected exception");
        } catch (SoapRequestException e) {
            // success
        }

        verify(_call);
    }

//    public void testRealRequest() throws RemoteException {
//        _request = new ChangePasswordRequest();
//        _request.setTarget("http://community.staging.greatschools.net/soap/user");
//        //_request.setTarget("http://aroy.dev.greatschools.net/cgi-bin/soap/soapServer.cgi");
//        User user = new User();
//        user.setId(10);
//        try {
//            _request.changePasswordRequest(user, "foobar");
//        } catch (SoapRequestException e) {
//            fail("Received unexpected exception " + e.getErrorCode() + ": " + e.getErrorMessage());
//        }
//    }
}
