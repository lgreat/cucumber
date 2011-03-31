package gs.web.search;

import gs.data.search.ISearchResult;

import java.util.List;

public interface ICmsFeatureSearchResult extends ISearchResult {
    public List<Long> getPrimaryCategoryId();
    public List<Long> getTopicId();
    public List<Long> getSecondaryCategoryId();
    public List<Long> getGradeId();
    public List<Long> getSubjectId();
    public List<Long> getLocationId();
    public String getContentType();
    public String getContentKey();
    public String getFullUri();
    public String getTitle();
    public String getSummary();
}