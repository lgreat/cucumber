package gs.web.jsp.link;

import gs.web.util.UrlBuilder;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class ArticleSearchTagHandler extends LinkTagHandler {
    private String _query;
    private Integer _page;

    protected UrlBuilder createUrlBuilder() {
        UrlBuilder urlBuilder = new UrlBuilder(UrlBuilder.ARTICLE_SEARCH, getState(), getQuery());
        if (null != _page) {
            urlBuilder.setParameter("p", String.valueOf(_page));
        }
        return urlBuilder;
    }

    public String getQuery() {
        return _query;
    }

    public void setQuery(String query) {
        _query = query;
    }

    public Integer getPage() {
        return _page;
    }

    public void setPage(Integer page) {
        _page = page;
    }
}
