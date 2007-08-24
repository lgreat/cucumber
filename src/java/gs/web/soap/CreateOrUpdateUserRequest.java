package gs.web.soap;

import static org.apache.axis.Constants.XSD_STRING;
import org.apache.axis.client.Call;

import javax.xml.rpc.ParameterMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides Performs a SOAP CreateOrUpdateUserRequest.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CreateOrUpdateUserRequest extends SoapRequest {
    public static final String DEFAULT_NAMESPACE_URI = "uri://www.greatschools.net/community/createOrUpdateUser/";
    public static final String DEFAULT_TARGET = "http://localhost:12000/soap/user/";
    public static final int DEFAULT_TIMEOUT = 10000; // 10s in milliseconds
    public static final boolean DISABLE_REQUEST = true;

    public CreateOrUpdateUserRequest() {
        this(DEFAULT_TARGET);
    }

    public CreateOrUpdateUserRequest(String target) {
        setTarget(target);
        setNamespaceUri(DEFAULT_NAMESPACE_URI);
        setTimeout(DEFAULT_TIMEOUT);
    }

    public void createOrUpdateUserRequest(CreateOrUpdateUserRequestBean bean) throws SoapRequestException {
        // quick hack to disable this class but still allow test cases to work
        if (DISABLE_REQUEST && _mockCall == null) { return; }
        try {
            Call call = setupCall("createOrUpdateUserRequest");
            Object[] params = setupParameters(call, bean);

            Object ret = call.invoke(params);

            if (ret != null && ret instanceof Map) {
                _log.warn("Exception generated on createOrUpdateUserRequest of class Map");
                Map returnMap = (Map) ret;
                SoapRequestException e = new SoapRequestException();
                e.setErrorCode(String.valueOf(returnMap.get("errorCode")));
                e.setErrorMessage(String.valueOf(returnMap.get("errorMessage")));
                e.fillInStackTrace();
                throw e;
            } else if (ret != null && ret instanceof SoapRequestException) {
                _log.warn("Exception generated on changePasswordRequest");
                throw (SoapRequestException) ret;
            } else if (ret != null && ret.toString() != null) {
                if (ret.toString().equals(bean.getId())) {
                    _log.info("createOrUpdateUserRequest successful on id " + ret);
                } else {
                    _log.warn("Exception generated on createOrUpdateUserRequest of class String");
                    SoapRequestException e = new SoapRequestException();
                    e.setErrorMessage(ret.toString());
                    e.fillInStackTrace();
                    throw e;
                }
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
     * @param bean to get values for the parameters
     * @return parameters for the invoke method
     */
    private Object[] setupParameters(Call call, CreateOrUpdateUserRequestBean bean) {
        Object[] params = new Object[4];
        int index = -1;
        // set up outbound parameters
        // (note the ParameterMode is IN because these are the parameters passed IN to the SOAP call, but
        //  I prefer to think of them as outbound parameters)
        call.addParameter("id", XSD_STRING, ParameterMode.IN);
        params[++index] = bean.getId();
        call.addParameter("screenName", XSD_STRING, ParameterMode.IN);
        params[++index] = bean.getScreenName();
        call.addParameter("email", XSD_STRING, ParameterMode.IN);
        params[++index] = bean.getEmail();
        call.addParameter("password", XSD_STRING, ParameterMode.IN);
        params[++index] = bean.getPassword();

        return params;
    }
}