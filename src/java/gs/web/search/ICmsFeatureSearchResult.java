package gs.web.search;

import gs.data.search.ISearchResult;
import gs.data.content.cms.ContentKey;
import gs.data.search.indexers.documentBuilders.CmsFeatureDocumentBuilder;
import org.apache.solr.client.solrj.beans.Field;

import javax.xml.bind.annotation.XmlElement;
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
    public String getPromoOrTitle();
    public String getTitle();
    public String getSummary();
    public Date getDateCreated();
    public String getImageUrl();
    public String getImageAltText();
    public String getLength();
    public void setGrades(List<String> grades);
    public String getGrades();
    public String getGradesCsv();
    public List<String> getSubjects();
    public void setSubjects(List<String> subjects);
    public void setContentType(String contentType);
    public void setContentId(Long contentId);
    public void setFullUri(String fullUri);
    public void setTitle(String title);

    public String getPreviewImageUrl();
    public void setPreviewImageUrl(String previewImageUrl);
    public String getPreviewImageTitle();
    public void setPreviewImageTitle(String previewImageTitle);
    public String getPreviewImageAltText();
    public void setPreviewImageAltText(String previewImageAltText);
    public String getSmallPreviewImageUrl();
    public void setSmallPreviewImageUrl(String smallPreviewImageUrl);
    public String getSmallPreviewImageTitle();
    public void setSmallPreviewImageTitle(String smallPreviewImageTitle);
    public String getSmallPreviewImageAltText();
    public void setSmallPreviewImageAltText(String smallPreviewImageAltText);
    public String getPdfUri();
    public void setPdfUri(String pdfUri);
    
    public String getDeck();
    public void setDeck(String deck);
    public String getBody();
    public void setBody(String body);

    public String getSubjectsString();
}