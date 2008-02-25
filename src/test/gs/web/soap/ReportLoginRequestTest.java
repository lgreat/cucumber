package gs.web.soap;

import gs.web.BaseTestCase;
import gs.data.community.User;
import gs.data.soap.SoapRequestException;
import org.apache.axis.client.Call;
import static org.easymock.classextension.EasyMock.*;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import java.rmi.RemoteException;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ReportLoginRequestTest extends BaseTestCase {
    private ReportLoginRequest _request;
    private User _user;
    private Call _call;

    public void setUp() throws Exception {
        super.setUp();
        _request = new ReportLoginRequest();
        _user = new User();
        _user.setId(123);
        _call = createMock(Call.class);
        _call.addParameter(eq("id"), (QName) anyObject(), (ParameterMode) anyObject());
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
            _request.reportLoginRequest(_user);
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
            _request.reportLoginRequest(_user);
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
            _request.reportLoginRequest(_user);
        } catch (SoapRequestException e) {
            fail(e.getErrorMessage());
        }

        verify(_call);
    }

    public void testIsDisabled() {
        _request.setTarget(null);
        _request.setMockCall(null);

        try {
            _request.reportLoginRequest(_user);
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
            _request.reportLoginRequest(_user);
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
            _request.reportLoginRequest(_user);
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
            _request.reportLoginRequest(_user);
            fail("Didn't receive expected exception");
        } catch (SoapRequestException e) {
            // success
        }

        verify(_call);
    }

//    public void testRealRequest() throws RemoteException {
//        _request = new ReportLoginRequest();
//        _request.setTarget("http://community.dev.greatschools.net/soap/user");
//        User user = new User();
//        // comdev aroy=2068065
//        user.setId(2068065);
//        try {
//            _request.reportLoginRequest(user);
//        } catch (SoapRequestException e) {
//            fail("Received unexpected exception " + e.getErrorCode() + ": " + e.getErrorMessage());
//        }
//    }
}
