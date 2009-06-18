package gs.web.jsp.link;

import gs.web.jsp.BaseTagHandler;
import gs.data.content.cms.ICmsCategoryDao;
import gs.data.content.cms.CmsCategory;
import gs.data.util.SpringUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;

public class CmsCategoryBrowseTagHandler extends BaseTagHandler {
    private int _categoryId;
    private String _language;
    private static ICmsCategoryDao _cmsCategoryDao;

    static {
        _cmsCategoryDao = (ICmsCategoryDao) SpringUtil.getApplicationContext().getBean(ICmsCategoryDao.BEAN_ID);
    }

    public void doTag() throws JspException, IOException {
        CmsCategory category = _cmsCategoryDao.getCmsCategoryFromId(_categoryId);

        if (category != null) {
            getJspContext().getOut().print("<a href=\"/articles/" + category.getFullUri() +
                (StringUtils.isNotBlank(_language) ? "?language=" + _language : "") + "\">");

            // Output the body, if any. Otherwise, output the category name.
            JspFragment jspBody = getJspBody();
            if (jspBody != null && StringUtils.isNotEmpty(jspBody.toString())) {
                try {
                    jspBody.invoke(getJspContext().getOut());
                } catch (JspException e) {
                    getJspContext().getOut().print(StringEscapeUtils.escapeHtml(category.getName()));
                }
            } else {
                getJspContext().getOut().print(StringEscapeUtils.escapeHtml(category.getName()));
            }

            getJspContext().getOut().print("</a>");
        }
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
