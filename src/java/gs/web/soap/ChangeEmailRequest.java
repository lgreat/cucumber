package gs.web.soap;

import static org.apache.axis.Constants.XSD_STRING;
import static org.apache.axis.Constants.SOAP_ELEMENT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis.client.Service;
import org.apache.axis.client.Call;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;

import gs.data.community.User;

/**
 * Provides Performs a SOAP CreateOrUpdateUserRequest.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ChangeEmailRequest {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String DEFAULT_NAMESPACE_URI = "uri://www.greatschools.net/community/changeEmail/";
    public static final String DEFAULT_TARGET = "http://aroy.dev.greatschools.net/cgi-bin/soap/soapServer.cgi";
    public static final int DEFAULT_TIMEOUT = 10000; // 10s in milliseconds
    public static final boolean DISABLE_REQUEST = true;

    private String _namespaceUri = DEFAULT_NAMESPACE_URI;
    private String _target = DEFAULT_TARGET;
    private int _timeout = DEFAULT_TIMEOUT;

    public ChangeEmailRequest() {}

    public ChangeEmailRequest(String target) {
        _target = target;
    }

    public void changeEmailRequest(User user) throws SoapRequestException {
        // quick hack to disable this class but still allow test cases to work
        if (DISABLE_REQUEST && !_target.contains("response")) { return; }
        try {
            Call call = setupCall();
            Object[] params = setupParameters(call, user);
            Object ret = call.invoke(params);

            if (ret != null) {
                _log.warn("Exception generated on changeEmailRequest");
                SoapRequestException e = (SoapRequestException) ret;
                e.fillInStackTrace();
                throw e;
            }
            _log.info("SOAP request to " + _target + " successful");
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

    private Call setupCall() throws ServiceException, MalformedURLException {
        Service service = new Service();
        Call call = (Call) service.createCall();
        // don't bother explicitly specifying types of each element in the xml request
        call.setProperty(Call.SEND_TYPE_ATTR, Boolean.FALSE);
        // set connection timeout
        call.setProperty(Call.CONNECTION_TIMEOUT_PROPERTY, _timeout);
        // location SOAP call is being made to
        call.setTargetEndpointAddress(new java.net.URL(_target));
        // name of operation
        call.setOperationName(new QName(_namespaceUri,
                "changeEmailRequest"));

        // set up return parameters
        QName errorQname = new QName(_namespaceUri, "error");
        call.addParameter("error", errorQname, ParameterMode.OUT);
        // this sets it up so that an object of type SoapRequestException is
        // automatically created on the return
        call.registerTypeMapping(SoapRequestException.class, errorQname,
                new BeanSerializerFactory(SoapRequestException.class, errorQname),
                new BeanDeserializerFactory(SoapRequestException.class, errorQname));
        // default return type must be specified
        // this is for any unexpected parameters not listed above
        call.setReturnType(SOAP_ELEMENT);
        return call;
    }

    public String getNamespaceUri() {
        return _namespaceUri;
    }

    public void setNamespaceUri(String namespaceUri) {
        _namespaceUri = namespaceUri;
    }

    public String getTarget() {
        return _target;
    }

    public void setTarget(String target) {
        _target = target;
    }

    public int getTimeout() {
        return _timeout;
    }

    public void setTimeout(int timeout) {
        _timeout = timeout;
    }
}
