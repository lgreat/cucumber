package gs.web.jsp.link;

import gs.data.state.State;
import gs.web.util.UrlBuilder;
import gs.web.util.context.SessionContext;

public class GlossaryTermLinkTagHandler extends LinkTagHandler {
    private String _termId;

    protected UrlBuilder createUrlBuilder() {
        SessionContext sessionContext = getSessionContext();
        State state = sessionContext.getStateOrDefault();

        return new UrlBuilder(UrlBuilder.GLOSSARY_TERM, state, _termId);
    }

    public void setTermId(String termId) {
        _termId = termId;
    }
}
