package gs.web.content.cms;

import gs.data.cms.IPublicationDao;
import gs.data.community.IDiscussionDao;
import gs.data.community.IRaiseYourHandDao;
import gs.data.community.RaiseYourHandFeature;
import gs.data.community.User;
import gs.data.content.cms.*;
import gs.data.search.IndexDir;
import gs.data.search.Indexer;
import gs.data.search.Searcher;
import gs.data.security.IRoleDao;
import gs.data.security.Permission;
import gs.data.security.Role;
import gs.data.security.RoleDaoHibernate;
import gs.web.BaseControllerTestCase;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;

import java.util.*;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class CmsHomepageControllerTest extends BaseControllerTestCase {
    private CmsHomepageController _controller;
    private ICmsCategoryDao _cmsCategoryDao;
    private IDiscussionDao _discussionDao;
    private IPublicationDao _publicationDao;
    private IRaiseYourHandDao _raiseYourHandDao;
    private ICmsDiscussionBoardDao _cmsDiscussionBoarDao;
    private IRoleDao _roleDao;
    private Searcher _searcher;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new CmsHomepageController();

        _cmsCategoryDao = createStrictMock(ICmsCategoryDao.class);
        _searcher = createStrictMock(Searcher.class);
        _discussionDao = createStrictMock(IDiscussionDao.class);
        _publicationDao = createStrictMock(IPublicationDao.class);
        _cmsDiscussionBoarDao = createStrictMock(ICmsDiscussionBoardDao.class);
        _raiseYourHandDao = createStrictMock(IRaiseYourHandDao.class);

        _controller.setCmsCategoryDao(_cmsCategoryDao);
        _controller.setSearcher(_searcher);
        _controller.setDiscussionDao(_discussionDao);
        _controller.setPublicationDao(_publicationDao);
        _controller.setCmsDiscussionBoardDao(_cmsDiscussionBoarDao);
        _controller.setRaiseYourHandDao(_raiseYourHandDao);

        _roleDao = new RoleDaoHibernate();
    }

    public void testBasics() {
        assertSame(_cmsCategoryDao, _controller.getCmsCategoryDao());
        assertSame(_searcher, _controller.getSearcher());
        assertSame(_discussionDao, _controller.getDiscussionDao());
        assertSame(_publicationDao, _controller.getPublicationDao());
        assertSame(_cmsDiscussionBoarDao, _controller.getCmsDiscussionBoardDao());
    }

    public void replayAllMocks() {
        super.replayMocks(_cmsCategoryDao, _searcher, _discussionDao, _publicationDao, _cmsDiscussionBoarDao,_raiseYourHandDao);
    }

    public void verifyAllMocks() {
        super.verifyMocks(_cmsCategoryDao, _searcher, _discussionDao, _publicationDao, _cmsDiscussionBoarDao,_raiseYourHandDao);
    }

    protected CmsCategory getCategory(int id, String name) {
        CmsCategory cat = new CmsCategory();
        cat.setId(id);
        cat.setFullUri("/" + name);
        cat.setName(name);
        cat.setType(CmsCategory.TYPE_GRADE);
        return cat;
    }

    protected CmsFeature getFeature(long index) {
        CmsFeature feature = new CmsFeature();
        feature.setContentKey(new ContentKey("Article", index));
        feature.setTitle("title" + index);
        feature.setBody("body" + index);
        feature.setSummary("summary" + index);
        feature.setFullUri("fullUri" + index);
        CmsCategory cat = new CmsCategory();
        cat.setName("category" + index);
        feature.setPrimaryKategory(cat);
        List<CmsCategory> breadcrumbs = Arrays.asList(cat);
        feature.setPrimaryKategoryBreadcrumbs(breadcrumbs);
        feature.setDateCreated(new Date());
        return feature;
    }

    private Searcher setupNoResultsSearcher() throws Exception {
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, new StandardAnalyzer(), true);

        List<CmsFeature> features = new ArrayList<CmsFeature>();
        Indexer indexer = new Indexer();
        indexer.indexCategories(indexer.indexCmsFeatures(features, writer), writer);
        writer.close();

        IndexDir indexDir = new IndexDir(dir, null);
        return new Searcher(indexDir);
    }

    private Searcher setupSomeResultsSearcher() throws Exception {
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, new StandardAnalyzer(), true);

        List<CmsFeature> features = new ArrayList<CmsFeature>();
        CmsFeature feature = getFeature(1);
        CmsCategory cat = getCategory(198, "p");
        feature.setPrimaryKategory(cat);
        feature.setPrimaryKategoryBreadcrumbs(Arrays.asList(cat));
        feature.setSecondaryKategories(Arrays.asList(cat));
        feature.setSecondaryKategoryBreadcrumbs(Arrays.asList(Arrays.asList(cat)));
        features.add(feature);
        Indexer indexer = new Indexer();
        indexer.indexCategories(indexer.indexCmsFeatures(features, writer), writer);
        writer.close();

        IndexDir indexDir = new IndexDir(dir, null);

        return new Searcher(indexDir);
    }

    public void testPopulateModelWithRecentCMSContentNoCategories() {
        Map<String, Object> model = new HashMap<String, Object>();
        expect(_cmsCategoryDao.getCmsCategoriesFromIds("198,199,200,201,202,203,204,205,206")).andReturn(null);
        replayAllMocks();
        _controller.populateModelWithRecentCMSContent(model);
        verifyAllMocks();
        assertNull(model.get(CmsHomepageController.MODEL_RECENT_CMS_CONTENT));
    }

    public void testInsertRaiseYourHandDiscussionsIntoModel() {
        Map<String, Object> model = new HashMap<String, Object>();

        User user = new User();
        Role cmsAdminRole = new Role();
        cmsAdminRole.setKey(Role.CMS_ADMIN);
        Set<Permission> permissions = new HashSet<Permission>();
        Permission raiseYourHand = new Permission();
        raiseYourHand.setKey(Permission.COMMUNITY_MANAGE_RAISE_YOUR_HAND);
        permissions.add(raiseYourHand);
        cmsAdminRole.setPermissions(permissions);
        user.addRole(cmsAdminRole);

        getSessionContext().setUser(user);

        List<RaiseYourHandFeature> result = new ArrayList<RaiseYourHandFeature>();

        expect(_raiseYourHandDao.getFeatures(new ContentKey("TopicCenter",2077l),CmsHomepageController.MAX_RAISE_YOUR_HAND_DISCUSSIONS_FOR_CMSADMIN)).andReturn(result);

        replayAllMocks();
        _controller.insertRaiseYourHandDiscussionsIntoModel(getRequest(),model);
        verifyAllMocks();
        assertNotNull("MODEL_ALL_RAISE_YOUR_HAND_FOR_TOPIC should not be null!", model.get(CmsHomepageController.MODEL_ALL_RAISE_YOUR_HAND_FOR_TOPIC));
    }

    public void testPopulateModelWithRecentCMSContentSomeCategories() {
        Map<String, Object> model = new HashMap<String, Object>();
        List<CmsCategory> categories = new ArrayList<CmsCategory>(9);
        categories.add(getCategory(198, "p"));
        expect(_cmsCategoryDao.getCmsCategoriesFromIds("198,199,200,201,202,203,204,205,206")).andReturn(categories);
        replayAllMocks();
        _controller.populateModelWithRecentCMSContent(model);
        verifyAllMocks();
        assertNull(model.get(CmsHomepageController.MODEL_RECENT_CMS_CONTENT));
    }

    public void testPopulateModelWithRecentCMSContentNoResults() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        List<CmsCategory> categories = new ArrayList<CmsCategory>(9);
        categories.add(getCategory(198, "p"));
        categories.add(getCategory(199, "k"));
        categories.add(getCategory(200, "1"));
        categories.add(getCategory(201, "2"));
        categories.add(getCategory(202, "3"));
        categories.add(getCategory(203, "4"));
        categories.add(getCategory(204, "5"));
        categories.add(getCategory(205, "m"));
        categories.add(getCategory(206, "h"));
        expect(_cmsCategoryDao.getCmsCategoriesFromIds("198,199,200,201,202,203,204,205,206")).andReturn(categories);
        _controller.setSearcher(setupNoResultsSearcher());
        expect(_publicationDao.populateByContentId(eq(1573L), isA(CmsTopicCenter.class))).andReturn(null);
        expect(_publicationDao.populateByContentId(eq(1574L), isA(CmsTopicCenter.class))).andReturn(null).times(6);
        expect(_publicationDao.populateByContentId(eq(1575L), isA(CmsTopicCenter.class))).andReturn(null);
        expect(_publicationDao.populateByContentId(eq(1576L), isA(CmsTopicCenter.class))).andReturn(null);
        replayAllMocks();
        _controller.populateModelWithRecentCMSContent(model);
        verifyAllMocks();
        assertNull(model.get(CmsHomepageController.MODEL_RECENT_CMS_CONTENT));
    }

    public void testPopulateModelWithRecentCMSContentSomeResults() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        List<CmsCategory> categories = new ArrayList<CmsCategory>(9);
        categories.add(getCategory(198, "p"));
        categories.add(getCategory(199, "k"));
        categories.add(getCategory(200, "1"));
        categories.add(getCategory(201, "2"));
        categories.add(getCategory(202, "3"));
        categories.add(getCategory(203, "4"));
        categories.add(getCategory(204, "5"));
        categories.add(getCategory(205, "m"));
        categories.add(getCategory(206, "h"));
        expect(_cmsCategoryDao.getCmsCategoriesFromIds("198,199,200,201,202,203,204,205,206")).andReturn(categories);
        _controller.setSearcher(setupSomeResultsSearcher());
        expect(_publicationDao.populateByContentId(eq(1573L), isA(CmsTopicCenter.class))).andReturn(null);
        expect(_publicationDao.populateByContentId(eq(1574L), isA(CmsTopicCenter.class))).andReturn(null).times(6);
        expect(_publicationDao.populateByContentId(eq(1575L), isA(CmsTopicCenter.class))).andReturn(null);
        expect(_publicationDao.populateByContentId(eq(1576L), isA(CmsTopicCenter.class))).andReturn(null);
        replayAllMocks();
        _controller.populateModelWithRecentCMSContent(model);
        verifyAllMocks();
        assertNull(model.get(CmsHomepageController.MODEL_RECENT_CMS_CONTENT));
    }


}
