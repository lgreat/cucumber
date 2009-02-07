package gs.web.jsp.link.microsite;

import gs.web.jsp.link.LinkTagHandler;
import gs.web.util.UrlBuilder;

public class SchoolChoiceCenterTagHandler extends LinkTagHandler {

    private Boolean _showConfirmation;

    protected UrlBuilder createUrlBuilder() {
        if (_showConfirmation != null) {
            return new UrlBuilder(UrlBuilder.SCHOOL_CHOICE_CENTER, _showConfirmation);
        } else {
            return new UrlBuilder(UrlBuilder.SCHOOL_CHOICE_CENTER);
        }
    }

    public Boolean isShowConfirmation() {
        return _showConfirmation;
    }

    public void setShowConfirmation(Boolean showConfirmation) {
        _showConfirmation = showConfirmation;
    }
}
