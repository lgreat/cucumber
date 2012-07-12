package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

public class BoundaryMapTagHandler extends LinkTagHandler {
    @Override
    protected UrlBuilder createUrlBuilder() {
        UrlBuilder builder = new UrlBuilder(UrlBuilder.BOUNDARY_MAP);
        return builder;
    }
}
