package gs.web.soap;

import gs.data.community.User;
import gs.web.BaseTestCase;
import org.apache.axis.client.Call;
import static org.easymock.classextension.EasyMock.*;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.HashMap;

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
        expectLastCall().anyTimes();
    }

    /**
     * Test that normal success conditions result in success
     */
    public void testSuccess() throws RemoteException {
        expect(_call.invoke((Object[])anyObject())).andReturn(null);
        replay(_call);

        _request.setMockCall(_call);
        try {
            _request.changePasswordRequest(_user, "123456");
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
            _request.changePasswordRequest(_user, "123456");
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
            _request.changePasswordRequest(_user, "123456");
        } catch (SoapRequestException e) {
            fail(e.getErrorMessage());
        }

        verify(_call);
    }

    /**
     * Test that when the server returns an error, this class behaves appropriately
     */
    public void testErrorSoapRequestException() throws RemoteException {
        SoapRequestException error = new SoapRequestException();
        expect(_call.invoke((Object[]) anyObject())).andReturn(error);
        replay(_call);

        _request.setMockCall(_call);
        try {
            _request.changePasswordRequest(_user, "123456");
            fail("Did not receive expected error");
        } catch (SoapRequestException e) {
            assertEquals("Unexpected exception", error, e);
            // success
        }

        verify(_call);
    }

    /**
     * Test that when the server returns an error map, this class behaves appropriately
     */
    public void testErrorMap() throws RemoteException {
        Map<String, String> map = new HashMap<String, String>();
        map.put("errorCode", "code");
        map.put("errorMessage", "message");
        expect(_call.invoke((Object[]) anyObject())).andReturn(map);
        replay(_call);

        _request.setMockCall(_call);
        try {
            _request.changePasswordRequest(_user, "123456");
            fail("Did not receive expected error");
        } catch (SoapRequestException e) {
            assertEquals("Unexpected exception", "code", e.getErrorCode());
            assertEquals("Unexpected exception", "message", e.getErrorMessage());
            // success
        }

        verify(_call);
    }

    /**
     * Test that when the server returns an error string, this class behaves appropriately
     */
    public void testErrorString() throws RemoteException {
        String error = "error!";
        expect(_call.invoke((Object[]) anyObject())).andReturn(error);
        replay(_call);

        _request.setMockCall(_call);
        try {
            _request.changePasswordRequest(_user, "123456");
            fail("Did not receive expected error");
        } catch (SoapRequestException e) {
            assertEquals("Unexpected exception", "error!", e.getErrorMessage());
            // success
        }

        verify(_call);
    }

    /**
     * Test that when the server returns an error id, this class behaves appropriately
     */
    public void testErrorInteger() throws RemoteException {
        Integer error = 15;
        expect(_call.invoke((Object[]) anyObject())).andReturn(error);
        replay(_call);

        _request.setMockCall(_call);
        try {
            _request.changePasswordRequest(_user, "123456");
            fail("Did not receive expected error");
        } catch (SoapRequestException e) {
            assertEquals("Unexpected exception", "15", e.getErrorMessage());
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
