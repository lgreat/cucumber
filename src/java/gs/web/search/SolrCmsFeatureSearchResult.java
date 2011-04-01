package gs.web.search;

import gs.data.content.cms.ContentKey;
import gs.data.search.indexers.documentBuilders.CmsFeatureDocumentBuilder;
import org.apache.solr.client.solrj.beans.Field;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlType(propOrder={"primaryCategoryId","topicId","secondaryCategoryId","gradeId","subjectId",
        "locationId","contentType","contentId","fullUri","title","summary","contentKey"})
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class SolrCmsFeatureSearchResult implements ICmsFeatureSearchResult {

    private List<Long> _primaryCategoryId;
    private List<Long> _topicId;
    private List<Long> _secondaryCategoryId;
    private List<Long> _gradeId;
    private List<Long> _subjectId;
    private List<Long> _locationId;
    private String _contentType;
    private Long _contentId;
    private String _fullUri;
    private String _title;
    private String _summary;


    // empty constructor required by JAXB
    public SolrCmsFeatureSearchResult(){
    }

    @XmlElement
    public String getContentType() {
        return _contentType;
    }
    @Field(CmsFeatureDocumentBuilder.FIELD_CONTENT_TYPE)
    public void setContentType(String contentType) {
        _contentType = contentType;
    }

    @XmlElement
    public Long getContentId() {
        return _contentId;
    }
    @Field(CmsFeatureDocumentBuilder.FIELD_CONTENT_ID)
    public void setContentId(Long contentId) {
        _contentId = contentId;
    }

   @XmlElement
   public List<Long> getPrimaryCategoryId() {
        return _primaryCategoryId;
    }
    @Field(CmsFeatureDocumentBuilder.FIELD_CMS_PRIMARY_CATEGORY_ID)
    public void setPrimaryCategoryId(List<Long> primaryCategoryId) {
        _primaryCategoryId = primaryCategoryId;
    }
    @XmlElement
    public List<Long> getTopicId() {
        return _topicId;
    }
   @Field(CmsFeatureDocumentBuilder.FIELD_CMS_TOPIC_ID)
    public void setTopicId(List<Long> topicId) {
        _topicId = topicId;
    }
    @XmlElement
    public List<Long> getSecondaryCategoryId() {
        return _secondaryCategoryId;
    }

    @Field(CmsFeatureDocumentBuilder.FIELD_CMS_SECONDARY_CATEGORY_ID)
    public void setSecondaryCategoryId(List<Long> secondaryCategoryId) {
        _secondaryCategoryId = secondaryCategoryId;
    }
    @XmlElement
    public List<Long> getGradeId() {
        return _gradeId;
    }
   @Field(CmsFeatureDocumentBuilder.FIELD_CMS_GRADE_ID)
    public void setGradeId(List<Long> gradeId) {
        _gradeId = gradeId;
    }
    @XmlElement
    public List<Long> getSubjectId() {
        return _subjectId;
    }
    @Field(CmsFeatureDocumentBuilder.FIELD_CMS_SUBJECT_ID)
    public void setSubjectId(List<Long> subjectId) {
        _subjectId = subjectId;
    }
    @XmlElement
    public List<Long> getLocationId() {
        return _locationId;
    }
   @Field(CmsFeatureDocumentBuilder.FIELD_CMS_LOCATION_ID)
    public void setLocationId(List<Long> locationId) {
        _locationId = locationId;
    }

    @XmlElement
    public String getFullUri() {
        return _fullUri;
    }
    @Field(CmsFeatureDocumentBuilder.FIELD_FULL_URI)
    public void setFullUri(String fullUri) {
        _fullUri = fullUri;
    }

    @XmlElement
    public String getTitle() {
        return _title;
    }

    @Field(CmsFeatureDocumentBuilder.FIELD_TITLE)
    public void setTitle(String title) {
        _title = title;
    }

    @XmlElement
    public String getSummary() {
        return _summary;
    }

    @Field(CmsFeatureDocumentBuilder.FIELD_RESULT_SUMMARY)
    public void setSummary(String summary) {
        _summary = summary;
    }

    @XmlTransient
    public ContentKey getContentKey() {
        return new ContentKey(_contentType,_contentId);
    }


}
