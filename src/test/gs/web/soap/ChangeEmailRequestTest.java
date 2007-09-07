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
 * Provides testing for the ChangeEmailRequest class.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ChangeEmailRequestTest extends BaseTestCase {
    private ChangeEmailRequest _request;
    private User _user;
    private Call _call;

    public void setUp() throws Exception {
        super.setUp();
        _request = new ChangeEmailRequest();
        _user = new User();
        _user.setId(123);
        _user.setEmail("aroy@greatschools.net");
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

        try {
            _request.changeEmailRequest(_user);
        } catch (SoapRequestException e) {
            fail(e.getErrorMessage());
        }

        verify(_call);
    }

    /**
     * Test that normal success conditions result in success
     */
    public void testSuccessWithString() throws RemoteException {
        expect(_call.invoke((Object[])anyObject())).andReturn("123");
        replay(_call);

        try {
            _request.changeEmailRequest(_user);
        } catch (SoapRequestException e) {
            fail(e.getErrorMessage());
        }

        verify(_call);
    }

    public void testIsDisabled() throws RemoteException, SoapRequestException {
        _request.setTarget(null);
        _request.setMockCall(null);

        _request.changeEmailRequest(_user);
        // success -- null target, but no exceptions, means no call was made
    }

    /**
     * Test that a failed response results in an exception
     */
    public void testInvalidResponse() throws RemoteException {
        expect(_call.invoke((Object[])anyObject())).andReturn(new SoapRequestException());
        replay(_call);

        try {
            _request.changeEmailRequest(_user);
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
            _request.changeEmailRequest(_user);
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
            _request.changeEmailRequest(_user);
            fail("Didn't receive expected exception");
        } catch (SoapRequestException e) {
            // success
        }

        verify(_call);
    }
}
