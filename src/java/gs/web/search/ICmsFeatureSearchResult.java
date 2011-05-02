package gs.web.search;

import gs.data.search.ISearchResult;
import gs.data.content.cms.ContentKey;
import java.util.List;
import java.util.Date;

public interface ICmsFeatureSearchResult extends ISearchResult {
    public List<Long> getPrimaryCategoryId();
    public List<Long> getTopicId();
    public List<Long> getSecondaryCategoryId();
    public List<Long> getGradeId();
    public List<Long> getSubjectId();
    public List<Long> getLocationId();
    public String getContentType();
    public Long getContentId();
    public ContentKey getContentKey();
    public String getFullUri();
    public String getPromo();
    public String getTitle();
    public String getSummary();
    public Date getDateCreated();
    public void setContentType(String contentType);
    public void setContentId(Long contentId);
    public void setFullUri(String fullUri);
    public void setTitle(String title);

}