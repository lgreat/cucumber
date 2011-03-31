package gs.web.search;

import gs.data.search.ISearchResult;

public interface ICmsCategorySearchResult extends ISearchResult {
   public Long getCategoryId();
    public String getCategoryName();
    public String getCategoryType();
    public String getCategoryFullUri();
}