package gs.web.jsp.link;

import gs.data.content.cms.ContentKey;
import gs.web.util.UrlBuilder;

public class CmsContentTagHandler extends LinkTagHandler {

    private ContentKey _contentKey;
    private String _fullUri;
    private Boolean _raiseYourHand;

    protected UrlBuilder createUrlBuilder() {
        return new UrlBuilder(_contentKey, _fullUri, _raiseYourHand);
    }

    public String getContentKey() {
        return _contentKey.toString();
    }

    public void setContentKey(String contentKeyString) {
        _contentKey = new ContentKey(contentKeyString);
    }

    public String getFullUri() {
        return _fullUri;
    }

    public void setFullUri(String fullUri) {
        _fullUri = fullUri;
    }

    public Boolean isRaiseYourHand() {
        return _raiseYourHand;
    }

    public void setRaiseYourHand(Boolean raiseYourHand) {
        _raiseYourHand = raiseYourHand;
    }
}
