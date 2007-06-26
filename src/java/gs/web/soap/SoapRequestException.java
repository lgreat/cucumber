package gs.web.soap;

/**
 * Provides ...
 *
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SoapRequestException extends Exception {
    private String _errorCode;
    private String _errorMessage;

    public String getErrorCode() {
        return _errorCode;
    }

    public void setErrorCode(String errorCode) {
        this._errorCode = errorCode;
    }

    public String getErrorMessage() {
        return _errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this._errorMessage = errorMessage;
    }
}
