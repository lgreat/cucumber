package gs.web.soap;

import gs.data.community.User;
import gs.data.soap.SoapRequest;
import gs.data.soap.SoapRequestException;
import static org.apache.axis.Constants.XSD_STRING;
import org.apache.axis.client.Call;

import javax.xml.rpc.ParameterMode;

/**
 * Provides Performs a SOAP changeEmailRequest.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.org>
 */
public class ChangePasswordRequest extends SoapRequest {
    public static final String DEFAULT_NAMESPACE_URI = "uri://www.greatschools.org/community/changePassword/";
    public static final int DEFAULT_TIMEOUT = 15000; // 15s in milliseconds

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
        String conTime = System.getProperty(CONNECT_TIMEOUT_PROP);
        String readTime = System.getProperty(READ_TIMEOUT_PROP);
        try {
            Call call = setupCall("changePasswordRequest");
            Object[] params = setupParameters(call, user);

            System.setProperty(CONNECT_TIMEOUT_PROP, CONNECT_TIMEOUT);
            System.setProperty(READ_TIMEOUT_PROP, READ_TIMEOUT);
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
        } finally {
            System.setProperty(CONNECT_TIMEOUT_PROP, (conTime != null)?conTime:"-1");
            System.setProperty(READ_TIMEOUT_PROP, (readTime != null)?readTime:"-1");
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
