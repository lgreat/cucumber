package gs.web.community;

import gs.web.BaseControllerTestCase;
import gs.data.content.IArticleCategoryDao;
import gs.data.content.ArticleCategory;
import gs.data.util.feed.IFeedDao;

import static org.easymock.EasyMock.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.ArrayList;

import com.sun.syndication.feed.synd.SyndEntry;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class RelatedCategoryPostsControllerTest extends BaseControllerTestCase {
    private RelatedCategoryPostsController _controller;
    private IArticleCategoryDao _articleCategoryDao;
    private IFeedDao _feedDao;
    private RelatedCategoryPostsController.RelatedCommunityPost _relatedPost;

    public void setUp() throws Exception {
        super.setUp();

        _controller = new RelatedCategoryPostsController();

        _articleCategoryDao = createStrictMock(IArticleCategoryDao.class);
        _feedDao = createStrictMock(IFeedDao.class);

        _controller.setArticleCategoryDao(_articleCategoryDao);
        _controller.setFeedDao(_feedDao);
        _controller.setNumberOfEntriesToShow(2);

        _relatedPost = new RelatedCategoryPostsController.RelatedCommunityPost();

        getRequest().setServerName("dev.greatschools.net");
    }

    public void testBasics() {
        _controller.setNumberOfEntriesToShow(5);
        _controller.setViewName("view");

        assertSame(_feedDao, _controller.getFeedDao());
        assertSame(_articleCategoryDao, _controller.getArticleCategoryDao());
        assertEquals(5, _controller.getNumberOfEntriesToShow());
        assertEquals("view", _controller.getViewName());
    }

    public void testRelatedCommunityPost() {
        _relatedPost.setDescription("description");
        _relatedPost.setLink("link");
        _relatedPost.setTitle("title");
        assertEquals("description", _relatedPost.getDescription());
        assertEquals("link", _relatedPost.getLink());
        assertEquals("title", _relatedPost.getTitle());

        _relatedPost.setType("http://community.greatschools.net/q-and-a/123456/What-is-the-meaning");
        assertEquals("question", _relatedPost.getType());

        _relatedPost.setType("http://community.greatschools.net/advice/123456/This-is-the-meaning");
        assertEquals("advice", _relatedPost.getType());

        _relatedPost.setType("http://community.greatschools.net/groups/123456/Searching-for-the-meaning");
        assertEquals("group", _relatedPost.getType());

        _relatedPost.setType("http://community.greatschools.net/groups/123456/Searching-for-the-meaning/discussion/123456");
        assertEquals("discussion", _relatedPost.getType());

        // fallback
        _relatedPost.setType("http://community.greatschools.net/");
        assertEquals("Expect default type to be question", "question", _relatedPost.getType());
    }

    public void testGetSearchTermFromArticleCategories() {
        List<ArticleCategory> cats = new ArrayList<ArticleCategory>();

        assertNull(_controller.getSearchTermFromArticleCategories(null));
        assertNull(_controller.getSearchTermFromArticleCategories(cats));

        ArticleCategory cat = new ArticleCategory();
        cat.setId(32); // First Grade
        cats.add(cat);
        assertEquals("Expect search term for category 32 to be \"First Grade\" (required by later tests)",
                "First Grade", _controller.getSearchTermFromArticleCategories(cats));
    }

    public void testGetArticleCategoriesFromIds() {
        ArticleCategory cat32 = new ArticleCategory();
        ArticleCategory cat64 = new ArticleCategory();

        String[] ids = new String[]{};
        List<ArticleCategory> cats;
        replay(_articleCategoryDao);
        cats = _controller.getArticleCategoriesFromIds(ids);
        verify(_articleCategoryDao);
        reset(_articleCategoryDao);
        assertNotNull(cats);
        assertEquals(0, cats.size());

        ids = new String[] {"32"};
        expect(_articleCategoryDao.getArticleCategory(32)).andReturn(cat32);
        replay(_articleCategoryDao);
        cats = _controller.getArticleCategoriesFromIds(ids);
        verify(_articleCategoryDao);
        reset(_articleCategoryDao);
        assertNotNull(cats);
        assertEquals(1, cats.size());

        ids = new String[] {"32", "64"};
        expect(_articleCategoryDao.getArticleCategory(32)).andReturn(cat32);
        expect(_articleCategoryDao.getArticleCategory(64)).andReturn(cat64);
        replay(_articleCategoryDao);
        cats = _controller.getArticleCategoriesFromIds(ids);
        verify(_articleCategoryDao);
        reset(_articleCategoryDao);
        assertNotNull(cats);
        assertEquals(2, cats.size());
    }

    public void testGetFeedURL() {
        String searchTerm = "foo";
        _controller.setNumberOfEntriesToShow(3);

        String url = _controller.getFeedURL(getRequest(), searchTerm);

        assertEquals("http://community.dev.greatschools.net/search/rss/" +
                "?q=foo&search_type=0&sort=relevance&limit=3", url);
        
        searchTerm = "foo bar";

        url = _controller.getFeedURL(getRequest(), searchTerm);

        assertEquals("Expect URL encoded search term",
                "http://community.dev.greatschools.net/search/rss/" +
                "?q=foo+bar&search_type=0&sort=relevance&limit=3", url);
    }

    public void testGetRelatedPostsFromFeedEntries() {
        List<SyndEntry> feedEntries = new ArrayList<SyndEntry>();
        SyndEntry entry = createMock(SyndEntry.class);
        feedEntries.add(entry);
        SyndEntry entry2 = createMock(SyndEntry.class);
        feedEntries.add(entry2);

        expect(entry.getTitle()).andReturn("title");
        expect(entry.getLink()).andReturn("link").times(2);
        expect(entry.getDescription()).andReturn(null);
        replay(entry);
        expect(entry2.getTitle()).andReturn("title2");
        expect(entry2.getLink()).andReturn("link2").times(2);
        expect(entry2.getDescription()).andReturn(null);
        replay(entry2);
        List<RelatedCategoryPostsController.RelatedCommunityPost> relatedPosts =
                _controller.getRelatedPostsFromFeedEntries(feedEntries);
        verify(entry);
        verify(entry2);
        assertNotNull(relatedPosts);
        assertEquals(2, relatedPosts.size());
        _relatedPost = relatedPosts.get(0);
        assertEquals("title", _relatedPost.getTitle());
        assertEquals("link", _relatedPost.getLink());
        assertEquals("", _relatedPost.getDescription());
        assertEquals("question", _relatedPost.getType());
        _relatedPost = relatedPosts.get(1);
        assertEquals("title2", _relatedPost.getTitle());
        assertEquals("link2", _relatedPost.getLink());
        assertEquals("", _relatedPost.getDescription());
        assertEquals("question", _relatedPost.getType());
    }

    public void testGetRelatedPostsFromFeedEntriesLimit() {
        List<SyndEntry> feedEntries = new ArrayList<SyndEntry>();
        SyndEntry entry = createMock(SyndEntry.class);
        feedEntries.add(entry);
        SyndEntry entry2 = createMock(SyndEntry.class);
        feedEntries.add(entry2);

        _controller.setNumberOfEntriesToShow(1);
        expect(entry.getTitle()).andReturn("title");
        expect(entry.getLink()).andReturn("link").times(2);
        expect(entry.getDescription()).andReturn(null);
        replay(entry);
        replay(entry2); // no calls expected
        List<RelatedCategoryPostsController.RelatedCommunityPost> relatedPosts =
                _controller.getRelatedPostsFromFeedEntries(feedEntries);
        verify(entry);
        verify(entry2);
        assertNotNull(relatedPosts);
        assertEquals(1, relatedPosts.size());
        _relatedPost = relatedPosts.get(0);
        assertEquals("title", _relatedPost.getTitle());
        assertEquals("link", _relatedPost.getLink());
        assertEquals("", _relatedPost.getDescription());
        assertEquals("question", _relatedPost.getType());
    }

    public void testHandleRequestInternal() throws Exception {
        getRequest().setParameter("category", "32");
        _controller.setViewName("/view");
        _controller.setNumberOfEntriesToShow(2);

        ArticleCategory cat32 = new ArticleCategory();
        cat32.setId(32);
        expect(_articleCategoryDao.getArticleCategory(32)).andReturn(cat32);
        replay(_articleCategoryDao);

        List<SyndEntry> feedEntries = new ArrayList<SyndEntry>();
        SyndEntry entry = createMock(SyndEntry.class);
        feedEntries.add(entry);

        String url = "http://community.dev.greatschools.net/search/rss/" +
                "?q=First+Grade&search_type=0&sort=relevance&limit=2";
        expect(_feedDao.getFeedEntries(url, 2)).andReturn(feedEntries);
        replay(_feedDao);

        expect(entry.getTitle()).andReturn("title");
        expect(entry.getLink()).andReturn("link").times(2);
        expect(entry.getDescription()).andReturn(null);
        replay(entry);

        ModelAndView mAndV = _controller.handleRequestInternal(getRequest(), getResponse());
        verify(_articleCategoryDao);
        verify(_feedDao);
        verify(entry);

        assertEquals("/view", mAndV.getViewName());
        assertNotNull(mAndV.getModel().get("relatedPosts"));

        List relatedPosts = (List) mAndV.getModel().get("relatedPosts");
        assertEquals(1, relatedPosts.size());
        _relatedPost = (RelatedCategoryPostsController.RelatedCommunityPost) relatedPosts.get(0);
        assertEquals("title", _relatedPost.getTitle());
        assertEquals("link", _relatedPost.getLink());
        assertEquals("", _relatedPost.getDescription());
        assertEquals("question", _relatedPost.getType());
    }
}
