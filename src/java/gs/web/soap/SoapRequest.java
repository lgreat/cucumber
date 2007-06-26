package gs.web.soap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import static org.apache.axis.Constants.SOAP_ELEMENT;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.ParameterMode;
import javax.xml.namespace.QName;
import java.net.MalformedURLException;

/**
 * Provides base class for simple soap requests.
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SoapRequest {
    protected final Log _log = LogFactory.getLog(getClass());

    private String _namespaceUri;
    private String _target;
    private int _timeout;

    protected Call setupCall(String operationName) throws ServiceException, MalformedURLException {
        return setupCall(operationName, SoapRequestException.class);
    }

    protected Call setupCall(String operationName, Class exceptionClass) throws ServiceException, MalformedURLException {
        Service service = new Service();
        Call call = (Call) service.createCall();
        // don't bother explicitly specifying types of each element in the xml request
        call.setProperty(Call.SEND_TYPE_ATTR, Boolean.FALSE);
        // set connection timeout
        call.setProperty(Call.CONNECTION_TIMEOUT_PROPERTY, _timeout);
        // location SOAP call is being made to
        call.setTargetEndpointAddress(new java.net.URL(_target));
        // name of operation
        call.setOperationName(new QName(_namespaceUri, operationName));

        // set up return parameters
        QName errorQname = new QName(_namespaceUri, "error");
        call.addParameter("error", errorQname, ParameterMode.OUT);
        // this sets it up so that an object of type SoapRequestException is
        // automatically created on the return
        call.registerTypeMapping(exceptionClass, errorQname,
                new BeanSerializerFactory(exceptionClass, errorQname),
                new BeanDeserializerFactory(exceptionClass, errorQname));
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
