package gs.web.soap;

import gs.data.community.User;
import gs.data.soap.SoapRequest;
import gs.data.soap.SoapRequestException;
import static org.apache.axis.Constants.XSD_STRING;
import org.apache.axis.client.Call;

import javax.xml.rpc.ParameterMode;
import java.util.Map;

/**
 * Provides Performs a SOAP changeEmailRequest.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ChangePasswordRequest extends SoapRequest {
    public static final String DEFAULT_NAMESPACE_URI = "uri://www.greatschools.net/community/changePassword/";
    public static final int DEFAULT_TIMEOUT = 10000; // 10s in milliseconds

    public ChangePasswordRequest() {
        this(null);
    }

    public ChangePasswordRequest(String target) {
        setTarget(target);
        setNamespaceUri(DEFAULT_NAMESPACE_URI);
        setTimeout(DEFAULT_TIMEOUT);
    }

    public void changePasswordRequest(User user) throws SoapRequestException {
        // quick hack to disable this class but still allow test cases to work
        if (isDisabled()) { return; }
        try {
            Call call = setupCall("changePasswordRequest");
            Object[] params = setupParameters(call, user);
            Object ret = call.invoke(params);

            validateResponse(ret, user.getId().toString());

            _log.info("SOAP request to " + getTarget() + " successful on user with id=" + user.getId());
        } catch (SoapRequestException e) {
            e.printStackTrace();
            throw e; // pass this on
        } catch (Exception e) {
            e.printStackTrace();
            _log.error(e);
            // wrap in a SoapRequestException and send on
            SoapRequestException ex = new SoapRequestException();
            ex.setErrorCode(e.getClass().getName());
            ex.setErrorMessage(e.getMessage());
            throw ex;
        }
    }

    /**
     * Setup the parameters for the soap call.
     *
     * Refactored out this method because it is VERY important that the parameters are added to the Call
     * object in the same order they are passed to the invoke method. By putting both operations here
     * it is easier to visually verify that this is the case;
     *
     * To add a parameter, increment the object array size and add a new pair of lines:
     * call.addParameter ...
     * params[++index] = ...
     *
     * @param call to set the parameter types on
     * @param user to get values for the parameters
     * @return parameters for the invoke method
     */
    private Object[] setupParameters(Call call, User user) {
        Object[] params = new Object[2];
        int index = -1;
        // set up outbound parameters
        // (note the ParameterMode is IN because these are the parameters passed IN to the SOAP call, but
        //  I prefer to think of them as outbound parameters)
        call.addParameter("id", XSD_STRING, ParameterMode.IN);
        params[++index] = String.valueOf(user.getId());
        call.addParameter("password", XSD_STRING, ParameterMode.IN);
        params[++index] = user.getPasswordMd5();

        return params;
    }
}
