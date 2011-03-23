package gs.web.search;

public interface ICmsCategorySearchResult extends ISearchResult {
   public Long getCategoryId();
    public String getCategoryName();
    public String getCategoryType();
    public String getCategoryFullUri();
}