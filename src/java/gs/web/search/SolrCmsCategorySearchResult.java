package gs.web.search;

import gs.data.search.indexers.documentBuilders.CmsCategoryDocumentBuilder;
import org.apache.solr.client.solrj.beans.Field;

import javax.xml.bind.annotation.*;
@XmlType(propOrder={"categoryId","categoryName","categoryType","categoryFullUri"})      
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class SolrCmsCategorySearchResult implements ICmsCategorySearchResult {
    private Long _categoryId;
    private String _categoryName;
    private String _categoryType;
    private String _categoryFullUri;

    // empty constructor required by JAXB
    public SolrCmsCategorySearchResult(){
    }

    @XmlElement
    public Long getCategoryId() {
        return _categoryId;
    }

    @Field(CmsCategoryDocumentBuilder.FIELD_CONTENT_ID)
    public void setCategoryId(Long categoryId) {
        _categoryId = categoryId;
    }

    @XmlElement
    public String getCategoryName() {
        return _categoryName;
    }

    @Field(CmsCategoryDocumentBuilder.FIELD_CMS_CATEGORY_NAME)
    public void setCategoryName(String categoryName) {
        _categoryName = categoryName;
    }

     @XmlElement
    public String getCategoryType() {
        return _categoryType;
    }

    @Field(CmsCategoryDocumentBuilder.FIELD_CMS_CATEGORY_TYPE)
    public void setCategoryType(String categoryType) {
        _categoryType = categoryType;
    }

    @XmlElement
    public String getCategoryFullUri() {
        return _categoryFullUri;
    }

    @Field(CmsCategoryDocumentBuilder.FIELD_FULL_URI)
    public void setCategoryFullUri(String categoryFullUri) {
        _categoryFullUri = categoryFullUri;
    }
}