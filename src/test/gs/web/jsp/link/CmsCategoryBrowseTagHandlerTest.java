package gs.web.jsp.link;

import gs.web.jsp.MockPageContext;
import gs.web.jsp.MockJspWriter;
import gs.data.content.cms.*;
import gs.data.search.Searcher;
import gs.data.search.Indexer;
import gs.data.search.IndexDir;
import gs.data.util.CmsUtil;
import junit.framework.TestCase;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspContext;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.Writer;
import java.io.IOException;

public class CmsCategoryBrowseTagHandlerTest extends TestCase {
    private ICmsCategoryDao _cmsCategoryDao;
    private CmsCategoryBrowseTagHandler _handler; 

    public void setUp() throws Exception {
        super.setUp();
        CmsUtil.enableCms();
        _cmsCategoryDao = new CmsCategoryDao();
        _cmsCategoryDao.setSearcher(setupSearcher());
        _handler = new CmsCategoryBrowseTagHandler();
        _handler.setCmsCategoryDao(_cmsCategoryDao);
        CmsUtil.disableCms();
    }

    public void testDoTagWithLanguage() throws Exception {
        MockPageContext pc = new MockPageContext();
        _handler.setPageContext(pc);
        _handler.setCategoryId(1);
        _handler.setLanguage("ES");

        _handler.doStartTag();
        _handler.doAfterBody();
        _handler.doEndTag();
        MockJspWriter out = (MockJspWriter) pc.getOut();
        assertEquals("<a href=\"/articles/improve-your-school?language=ES\"></a>",
                out.getOutputBuffer().toString());
    }

    public void testDoTagWithoutBody() throws Exception {
        MockPageContext pc = new MockPageContext();
        _handler.setPageContext(pc);
        _handler.setCategoryId(1);

        _handler.doStartTag();
        _handler.doAfterBody();
        _handler.doEndTag();
        MockJspWriter out = (MockJspWriter) pc.getOut();
        assertEquals("<a href=\"/articles/improve-your-school\"></a>",
                out.getOutputBuffer().toString());
    }

    /*
    // currently don't know how to test tag with body now that we've switched this to LinkTagHandler
    public void testDoTagWithBody() throws Exception {
        final MockPageContext pc = new MockPageContext();
        _handler.setPageContext(pc);
        _handler.setCategoryId(1);
        _handler.set JspBody( new JspFragment() {
            public void invoke(Writer writer) throws JspException, IOException {
                writer.write("the tag body");
            }

            public JspContext getJspContext() {
                return pc;
            }
        });

        _handler.doStartTag();
        _handler.doAfterBody();
        _handler.doEndTag();
        MockJspWriter out = (MockJspWriter) pc.getOut();
        assertEquals("<a href=\"/articles/improve-your-school\">the tag body</a>",
                out.getOutputBuffer().toString());
    }
    */

    protected CmsFeature getFeature(long index) {
        CmsFeature feature = new CmsFeature();
        feature.setContentKey(new ContentKey("Article", index));
        feature.setTitle("title" + index);
        feature.setBody("body" + index);
        feature.setSummary("summary" + index);
        feature.setFullUri("fullUri" + index);
        CmsCategory cat = new CmsCategory();
        cat.setName("kategory" + index);
        feature.setPrimaryKategory(cat);
        List<CmsCategory> breadcrumbs = Arrays.asList(cat);
        feature.setPrimaryKategoryBreadcrumbs(breadcrumbs);
        return feature;
    }

    protected CmsCategory getCmsCategory(String name, String fullUri, int id) {
        CmsCategory cat = new CmsCategory();
        cat.setName(name);
        cat.setFullUri(fullUri);
        cat.setId(id);
        return cat;
    }

    private Searcher setupSearcher() throws Exception {
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, new StandardAnalyzer(), true);

        List<CmsFeature> features = new ArrayList<CmsFeature>();

        // feature id: primary cat id
        // 1:1
        // 2:2
        // feature id: secondary cat ids
        // 1:2,3
        // 2:3,4
        CmsFeature feature1 = getFeature(1);
        CmsCategory cat = getCmsCategory("Improve your school",
                "improve-your-school", 1);
        feature1.setPrimaryKategory(cat);
        feature1.setPrimaryKategoryBreadcrumbs(Arrays.asList(cat));
        List<CmsCategory> secondaryCats = new ArrayList<CmsCategory>(2);
        secondaryCats.add(getCmsCategory("Secondary Information",
                "improve-your-school/secondary-information", 3));
        secondaryCats.add(getCmsCategory("Building community",
                "improve-your-school/building-community", 2));
        feature1.setSecondaryKategories(secondaryCats);
        features.add(feature1);
        CmsFeature feature2 = getFeature(2);
        CmsCategory cat2 = getCmsCategory("Building community",
                "improve-your-school/building-community", 2);
        feature2.setPrimaryKategory(cat2);
        feature2.setPrimaryKategoryBreadcrumbs(Arrays.asList(cat2));
        secondaryCats = new ArrayList<CmsCategory>(2);
        secondaryCats.add(getCmsCategory("Secondary Information",
                "improve-your-school/secondary-information", 3));
        secondaryCats.add(getCmsCategory("Elementary school",
                "elementary-school", 4));
        feature2.setSecondaryKategories(secondaryCats);
        features.add(feature2);

        Indexer indexer = new Indexer();
        indexer.indexCategories(indexer.indexCmsFeatures(features, writer), writer);
        writer.close();

        IndexDir indexDir = new IndexDir(dir, null);
        return new Searcher(indexDir);
    }
}
