package gs.web.admin.gwt.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ServiceException extends Exception implements IsSerializable  {
    private String _message;


    public ServiceException() {
        super();
    }

    public ServiceException(final String message) {
        super(message);
        this._message = message;
    }


    public String getMessage() {
        return this._message;
    }
}
