package gs.web.content.cms;

import gs.web.BaseControllerTestCase;
import gs.data.content.cms.ICmsArticleDao;
import gs.data.content.cms.CmsArticle;

import static org.easymock.EasyMock.*;

public class ArticleControllerTest extends BaseControllerTestCase {
    private CmsArticleController _controller;
    private ICmsArticleDao _cmsArticleDao;

    public void setUp() throws Exception {
        super.setUp();
        _controller = new CmsArticleController();

        _cmsArticleDao = createStrictMock(ICmsArticleDao.class);

        _controller.setCmsArticleDao(_cmsArticleDao);
    }

    public void testReplaceGreatSchoolsUrls() {
        CmsArticle article = new CmsArticle();

        article.setBody("Hello! Visit us " +
                "<a href=\"gs://home?foo=bar&taz=whatcomesnext\">here</a>." +
                " Also, you may want to go <a href=\"gs://home?foo=bar&taz=whatcomesnext\">here</a>.");

        article.setSummary("Hello again! Visit us " +
                "<a href=\"gs://home?foo=bar&taz=whatcomesnext\">here</a>." +
                " Also, you may want to go <a href=\"gs://home?foo=bar&taz=whatcomesnext\">here</a>.");
        replay(_cmsArticleDao);
        _controller.replaceGreatSchoolsUrlsInArticle(article, getRequest());
        verify(_cmsArticleDao);

        assertEquals("Hello! Visit us <a href=\"/\">here</a>." +
                " Also, you may want to go <a href=\"/\">here</a>.", article.getBody());

        assertEquals("Hello again! Visit us <a href=\"/\">here</a>." +
                " Also, you may want to go <a href=\"/\">here</a>.", article.getSummary());
    }
}
