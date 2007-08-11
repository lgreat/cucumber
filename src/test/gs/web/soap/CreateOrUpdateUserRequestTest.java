package gs.web.soap;

import gs.web.BaseTestCase;
import org.apache.axis.client.Call;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import java.rmi.RemoteException;

/**
 * Provides testing for the CreateOrUpdateUserRequest.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CreateOrUpdateUserRequestTest extends BaseTestCase {
    private CreateOrUpdateUserRequest _request;
    private CreateOrUpdateUserRequestBean _bean;
    private Call _call;

    public void setUp() throws Exception {
        super.setUp();
        _request = new CreateOrUpdateUserRequest();
        _bean = new CreateOrUpdateUserRequestBean("1", "Anthony", "aroy@greatschools.net");
        _call = createMock(Call.class);
        _call.addParameter((String) anyObject(), (QName) anyObject(), (ParameterMode) anyObject());
        expectLastCall().anyTimes();
    }

    /**
     * Test that normal success conditions result in success
     */
    public void testSuccess() throws RemoteException {
        expect(_call.invoke((Object[]) anyObject())).andReturn(null);
        replay(_call);

        _request.setMockCall(_call);
        try {
            _request.createOrUpdateUserRequest(_bean);
        } catch (SoapRequestException e) {
            fail("Received unexpected exception " + e.getErrorCode() + ": " + e.getErrorMessage());
        }
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
            _request.createOrUpdateUserRequest(_bean);
            fail("Did not receive expected error");
        } catch (SoapRequestException e) {
            assertEquals("Unexpected exception", error, e);
            // success
        }
    }
}
