package gs.web.soap;

import static org.apache.axis.Constants.XSD_STRING;
import static org.apache.axis.Constants.SOAP_ELEMENT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis.client.Service;
import org.apache.axis.client.Call;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;

/**
 * Provides Performs a SOAP CreateOrUpdateUserRequest.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CreateOrUpdateUserRequest {
    protected final Log _log = LogFactory.getLog(getClass());

    public static final String DEFAULT_NAMESPACE_URI = "uri://www.greatschools.net/community/createOrUpdateUser/";
    public static final String DEFAULT_TARGET = "http://aroy.dev.greatschools.net/cgi-bin/soap/soapServer.cgi";
    public static final int DEFAULT_TIMEOUT = 10000; // 10s in milliseconds

    private String _namespaceUri = DEFAULT_NAMESPACE_URI;
    private String _target = DEFAULT_TARGET;
    private int _timeout = DEFAULT_TIMEOUT;

    public CreateOrUpdateUserRequest() {}

    public CreateOrUpdateUserRequest(String target) {
        _target = target;
    }

    public void createOrUpdateUserRequest(CreateOrUpdateUserRequestBean bean) throws CreateOrUpdateUserRequestException {
        try {
            Call call = setupCall();
            Object[] params = setupParameters(call, bean);
            Object ret = call.invoke(params);

            if (ret != null) {
                _log.warn("Exception generated on createOrUpdateUserRequest");
                CreateOrUpdateUserRequestException e = (CreateOrUpdateUserRequestException) ret;
                e.fillInStackTrace();
                throw e;
            }
            _log.info("Request successful");
        } catch (CreateOrUpdateUserRequestException e) {
            throw e; // pass this on
        } catch (Exception e) {
            _log.error(e);
            // wrap in a CreateOrUpdateUserRequestException and send on
            CreateOrUpdateUserRequestException ex = new CreateOrUpdateUserRequestException();
            ex.setErrorCode(e.getClass().getName());
            ex.setErrorMessage(e.getMessage());
            throw ex;
        }
    }

    /**
     * Refactor out this method because it is VERY important that the parameters are added to the Call
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
        Object[] params = new Object[3];
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
                "createOrUpdateUserRequest"));

        // set up return parameters
        QName errorQname = new QName(_namespaceUri, "error");
        call.addParameter("error", errorQname, ParameterMode.OUT);
        // this sets it up so that an object of type CreateOrUpdateUserRequestException is
        // automatically created on the return
        call.registerTypeMapping(CreateOrUpdateUserRequestException.class, errorQname,
                new BeanSerializerFactory(CreateOrUpdateUserRequestException.class, errorQname),
                new BeanDeserializerFactory(CreateOrUpdateUserRequestException.class, errorQname));
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
