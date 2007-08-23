package gs.web.soap;

import gs.data.community.User;
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
     * Test that when the server returns an error, this class behaves appropriately
     */
    public void testError() throws RemoteException {
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
}
