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
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ChangeEmailRequest extends SoapRequest {
    public static final String DEFAULT_NAMESPACE_URI = "uri://www.greatschools.net/community/changeEmail/";
    public static final String DEFAULT_TARGET = "http://aroy.dev.greatschools.net/cgi-bin/soap/soapServer.cgi";
    public static final int DEFAULT_TIMEOUT = 10000; // 10s in milliseconds
    public static final boolean DISABLE_REQUEST = true;

    public ChangeEmailRequest() {
        this(DEFAULT_TARGET);
    }

    public ChangeEmailRequest(String target) {
        setTarget(target);
        setNamespaceUri(DEFAULT_NAMESPACE_URI);
        setTimeout(DEFAULT_TIMEOUT);
    }

    public void changeEmailRequest(User user) throws SoapRequestException {
        // quick hack to disable this class but still allow test cases to work
        if (DISABLE_REQUEST && _mockCall == null) { return; }
        try {
            Call call = setupCall("changeEmailRequest");
            Object[] params = setupParameters(call, user);
            Object ret = call.invoke(params);

            if (ret != null) {
                _log.warn("Exception generated on changeEmailRequest");
                SoapRequestException e = (SoapRequestException) ret;
                e.fillInStackTrace();
                throw e;
            }
            _log.info("SOAP request to " + getTarget() + " successful");
        } catch (SoapRequestException e) {
            throw e; // pass this on
        } catch (Exception e) {
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
        call.addParameter("email", XSD_STRING, ParameterMode.IN);
        params[++index] = user.getEmail();

        return params;
    }
}
