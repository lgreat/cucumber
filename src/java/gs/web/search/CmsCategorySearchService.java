package gs.web.search;

import gs.data.content.cms.CmsCategory;

import java.util.List;

public interface CmsCategorySearchService {
    public SearchResultsPage<ICmsCategorySearchResult> search(String queryString) throws SearchException;
    public List<CmsCategory> getCategoriesFromIds(String ids);
}