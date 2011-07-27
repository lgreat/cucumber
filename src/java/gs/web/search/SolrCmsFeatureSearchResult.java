package gs.web.search;

import gs.data.content.cms.ContentKey;
import gs.data.search.indexers.documentBuilders.CmsFeatureDocumentBuilder;
import gs.data.util.CmsUtil;
import org.apache.solr.client.solrj.beans.Field;

import javax.xml.bind.annotation.*;
import java.util.Date;
import java.util.List;

@XmlType(propOrder={"primaryCategoryId","topicId","secondaryCategoryId","gradeId","subjectId",
        "locationId","contentType","contentId","fullUri","promo","title","summary","contentKey"})
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
    private String _promo;
    private String _title;
    private String _summary;
    private Date _dateCreated;
    private String _imageUrl;
    private String _imageAltText;
    private String _length;
    private String _grades;
    private List<String> _subjects;
    private String _deck;
    private String _body;

    private String _previewImageUrl;
    private String _previewImageTitle;
    private String _previewImageAltText;
    private String _smallPreviewImageUrl;
    private String _smallPreviewImageTitle;
    private String _smallPreviewImageAltText;

    private String _pdfUri;

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

    @Field(CmsFeatureDocumentBuilder.FIELD_PROMO)
    public void setPromo(String promo) {
        _promo = promo;
    }

    @XmlElement
    public String getPromo() {
        return _promo;
    }

    public String getPromoOrTitle() {
        String promoOrTitle = getPromo();
        if (promoOrTitle == null) {
            promoOrTitle = getTitle();
        }
        return promoOrTitle;
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
    
    @XmlElement
    public Date getDateCreated() {
        return _dateCreated;
    }

    @Field(CmsFeatureDocumentBuilder.FIELD_CMS_DATE_CREATED)
    public void setDateCreated(Date dateCreated) {
        _dateCreated = dateCreated;
    }

    @XmlTransient
    public ContentKey getContentKey() {
        return new ContentKey(_contentType,_contentId);
    }

    @XmlElement
    /**
     * Gets the relative image URL from Solr and then uses CmsUtil to get an absolute URL
     */
    public String getImageUrl() {
        return CmsUtil.getImageUrl(_imageUrl);
    }
    @Field(CmsFeatureDocumentBuilder.FIELD_IMAGE_URL)
    public void setImageUrl(String imageUrl) {
        _imageUrl = imageUrl;
    }

    @XmlElement
    public String getImageAltText() {
        return _imageAltText;
    }
    @Field(CmsFeatureDocumentBuilder.FIELD_IMAGE_ALT_TEXT)
    public void setImageAltText(String imageAltText) {
        _imageAltText = imageAltText;
    }

    @XmlElement
    public String getLength() {
        return _length;
    }
    @Field(CmsFeatureDocumentBuilder.FIELD_LENGTH)
    public void setLength(String length) {
        _length = length;
    }
    
    @XmlElement
    public String getGrades() {
        return _grades;
    }
    @Field(CmsFeatureDocumentBuilder.FIELD_GRADES)
    public void setGrades(String grades) {
        _grades = grades;
    }
    
    @XmlElement
    public List<String> getSubjects() {
        return _subjects;
    }
    @Field(CmsFeatureDocumentBuilder.FIELD_SUBJECTS)
    public void setSubjects(List<String> subjects) {
        _subjects = subjects;
    }

    @XmlElement
    public String getPreviewImageUrl() {
        return CmsUtil.getImageUrl(_previewImageUrl);
    }
    @Field(CmsFeatureDocumentBuilder.FIELD_PREVIEW_IMAGE_URL)
    public void setPreviewImageUrl(String previewImageUrl) {
        _previewImageUrl = previewImageUrl;
    }

    @XmlElement
    public String getPreviewImageTitle() {
        return _previewImageTitle;
    }
    @Field(CmsFeatureDocumentBuilder.FIELD_PREVIEW_IMAGE_TITLE)
    public void setPreviewImageTitle(String previewImageTitle) {
        _previewImageTitle = previewImageTitle;
    }

    @XmlElement
    public String getPreviewImageAltText() {
        return _previewImageAltText;
    }
    @Field(CmsFeatureDocumentBuilder.FIELD_PREVIEW_IMAGE_ALT_TEXT)
    public void setPreviewImageAltText(String previewImageAltText) {
        _previewImageAltText = previewImageAltText;
    }

    @XmlElement
    public String getSmallPreviewImageUrl() {
        return CmsUtil.getImageUrl(_smallPreviewImageUrl);
    }
    @Field(CmsFeatureDocumentBuilder.FIELD_SMALL_PREVIEW_IMAGE_URL)
    public void setSmallPreviewImageUrl(String smallPreviewImageUrl) {
        _smallPreviewImageUrl = smallPreviewImageUrl;
    }

    @XmlElement
    public String getSmallPreviewImageTitle() {
        return _smallPreviewImageTitle;
    }
    @Field(CmsFeatureDocumentBuilder.FIELD_SMALL_PREVIEW_IMAGE_TITLE)
    public void setSmallPreviewImageTitle(String smallPreviewImageTitle) {
        _smallPreviewImageTitle = smallPreviewImageTitle;
    }

    @XmlElement
    public String getSmallPreviewImageAltText() {
        return _smallPreviewImageAltText;
    }
    @Field(CmsFeatureDocumentBuilder.FIELD_SMALL_PREVIEW_IMAGE_ALT_TEXT)
    public void setSmallPreviewImageAltText(String smallPreviewImageAltText) {
        _smallPreviewImageAltText = smallPreviewImageAltText;
    }

    @XmlElement
    public String getPdfUri() {
        return _pdfUri;
    }
    @Field(CmsFeatureDocumentBuilder.FIELD_PDF_URI)
    public void setPdfUri(String pdfUri) {
        _pdfUri = pdfUri;
    }

    @XmlElement
    public String getDeck() {
        return _deck;
    }

    @Field(CmsFeatureDocumentBuilder.FIELD_DECK)
    public void setDeck(String deck) {
        _deck = deck;
    }

    @XmlElement
    public String getBody() {
        return _body;
    }

    @Field(CmsFeatureDocumentBuilder.FIELD_BODY)
    public void setBody(String body) {
        _body = body;
    }
}
