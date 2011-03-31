package gs.web.search;

import gs.data.content.cms.CmsCategory;
import gs.data.search.SearchException;
import gs.data.search.SearchResultsPage;

import java.util.List;

public interface CmsCategorySearchService {
    public SearchResultsPage<ICmsCategorySearchResult> search(String queryString) throws SearchException;
    public List<CmsCategory> getCategoriesFromIds(String ids);
    public CmsCategory getCategoryFromURI(String requestURI);
}