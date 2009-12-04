package gs.web.jsp;

import gs.data.content.ArticleCategory;
import gs.data.content.IArticleCategoryDao;
import gs.data.util.SpringUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;

/**
 * @author greatschools.org>
 */
public class ArticleCategoryBrowseTagHandler extends BaseTagHandler {
    private int _categoryId;
    private static IArticleCategoryDao _articleCategoryDao;

    static {
        _articleCategoryDao = (IArticleCategoryDao) SpringUtil.getApplicationContext().getBean(IArticleCategoryDao.BEAN_ID);
    }

    public void doTag() throws JspException, IOException {
        ArticleCategory articleCategory = _articleCategoryDao.getArticleCategory(_categoryId);

        if (articleCategory == null) {
            return; // NOTE: Early exit!
        }

        StringBuffer b = new StringBuffer();

        b.append("<a href=\"");

        String link = getHref(articleCategory);
        b.append(link);

        b.append("\">");


        getJspContext().getOut().print(b);

        // Output the body, if any. Otherwise, output the article title.
        JspFragment jspBody = getJspBody();
        if (jspBody != null && StringUtils.isNotEmpty(jspBody.toString())) {
            try {
                jspBody.invoke(getJspContext().getOut());
            } catch (JspException e) {
                getJspContext().getOut().print(articleCategory.getTypeDisplay());
            }
        } else {
            getJspContext().getOut().print(articleCategory.getTypeDisplay());
        }

        getJspContext().getOut().print("</a>");

    }

    protected String getHref(ArticleCategory articleCategory) {
        String url = "/articles/" + articleCategory.getId();

        List<ArticleCategory> cats = new ArrayList<ArticleCategory>();
        Set<String> types = new HashSet<String>();
        ArticleCategory parentCat;
        String parentType;
        int infiniteLoopCounter = 0;

        cats.add(0, articleCategory);
        types.add(articleCategory.getType());

        parentType = articleCategory.getParentType();
        while (StringUtils.isNotBlank(parentType) &&
                !types.contains(parentType) &&
                infiniteLoopCounter < 10 && !StringUtils.equals("NEW CATEGORY", parentType)) {
            infiniteLoopCounter++;
            parentCat = _articleCategoryDao.getArticleCategoryByType(parentType);
            if (parentCat != null && types.add(parentCat.getType())) {
                cats.add(0, parentCat);
                parentType = parentCat.getParentType();
            } else {
                break;
            }
        }

        for (ArticleCategory cat: cats) {
            String type = cat.getTypeDisplay().trim();
            type = StringUtils.replaceChars(type, " /?&", "----");
            url += "/" + type;
        }

        return url;
    }

    public int getCategoryId() {
        return _categoryId;
    }

    public void setCategoryId(int categoryId) {
        _categoryId = categoryId;
    }
}
