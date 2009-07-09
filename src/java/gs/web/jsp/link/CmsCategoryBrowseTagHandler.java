package gs.web.jsp.link;

import gs.web.util.UrlBuilder;
import gs.data.content.cms.ICmsCategoryDao;
import gs.data.content.cms.CmsCategory;
import gs.data.util.SpringUtil;
import org.apache.commons.lang.StringEscapeUtils;

public class CmsCategoryBrowseTagHandler extends LinkTagHandler {
    private CmsCategory _category;
    private int _categoryId;
    private String _language;
    private static ICmsCategoryDao _cmsCategoryDao;

    static {
        _cmsCategoryDao = (ICmsCategoryDao) SpringUtil.getApplicationContext().getBean(ICmsCategoryDao.BEAN_ID);
    }

    public CmsCategoryBrowseTagHandler() {
        _category = _cmsCategoryDao.getCmsCategoryFromId(_categoryId);
    }

    /**
     Not currently supported: default link text must be provided
     */
    @Override
    protected String getDefaultLinkText() {
        return StringEscapeUtils.escapeHtml(_category.getName());
    }

    protected UrlBuilder createUrlBuilder() {
        _category = _cmsCategoryDao.getCmsCategoryFromId(_categoryId);
        return new UrlBuilder(_category, _language, UrlBuilder.CMS_CATEGORY_BROWSE);
    }

    public int getCategoryId() {
        return _categoryId;
    }

    public void setCategoryId(int categoryId) {
        _categoryId = categoryId;
    }

    public String getLanguage() {
        return _language;
    }

    public void setLanguage(String language) {
        _language = language;
    }

    public ICmsCategoryDao getCmsCategoryDao() {
        return _cmsCategoryDao;
    }

    public void setCmsCategoryDao(ICmsCategoryDao cmsCategoryDao) {
        _cmsCategoryDao = cmsCategoryDao;
    }
}
