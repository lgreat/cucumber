package gs.web.content.cms;

import gs.data.cms.IPublicationDao;
import gs.data.community.IDiscussionDao;
import gs.data.community.IRaiseYourHandDao;
import gs.data.community.RaiseYourHandFeature;
import gs.data.community.User;
import gs.data.content.cms.*;
import gs.data.search.SearchResultsPage;
import gs.data.search.Searcher;
import gs.data.security.IRoleDao;
import gs.data.security.Permission;
import gs.data.security.Role;
import gs.data.security.RoleDaoHibernate;
import gs.web.BaseControllerTestCase;
import gs.web.search.CmsFeatureSearchService;
import gs.web.search.ICmsFeatureSearchResult;
import gs.web.search.SolrCmsFeatureSearchResult;
import org.apache.solr.client.solrj.SolrQuery;

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
    private CmsFeatureSearchService _cmsFeatureSearchService;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new CmsHomepageController();
        CmsHomepageController._gradeByGradeCache.removeAll();

        _cmsCategoryDao = createStrictMock(ICmsCategoryDao.class);
        _searcher = createStrictMock(Searcher.class);
        _discussionDao = createStrictMock(IDiscussionDao.class);
        _publicationDao = createStrictMock(IPublicationDao.class);
        _cmsDiscussionBoarDao = createStrictMock(ICmsDiscussionBoardDao.class);
        _raiseYourHandDao = createStrictMock(IRaiseYourHandDao.class);
        _cmsFeatureSearchService = createStrictMock(CmsFeatureSearchService.class);

        _controller.setCmsCategoryDao(_cmsCategoryDao);
        _controller.setSearcher(_searcher);
        _controller.setDiscussionDao(_discussionDao);
        _controller.setPublicationDao(_publicationDao);
        _controller.setCmsDiscussionBoardDao(_cmsDiscussionBoarDao);
        _controller.setRaiseYourHandDao(_raiseYourHandDao);
        _controller.setSolrCmsFeatureSearchService(_cmsFeatureSearchService);

        _roleDao = new RoleDaoHibernate();
    }

    public void testBasics() {
        assertSame(_cmsCategoryDao, _controller.getCmsCategoryDao());
        assertSame(_searcher, _controller.getSearcher());
        assertSame(_discussionDao, _controller.getDiscussionDao());
        assertSame(_publicationDao, _controller.getPublicationDao());
        assertSame(_cmsDiscussionBoarDao, _controller.getCmsDiscussionBoardDao());
    }

    public void resetAllMocks() {
        super.resetMocks(_cmsCategoryDao, _searcher, _discussionDao, _publicationDao, _cmsDiscussionBoarDao, _raiseYourHandDao, _cmsFeatureSearchService);
    }

    public void replayAllMocks() {
        super.replayMocks(_cmsCategoryDao, _searcher, _discussionDao, _publicationDao, _cmsDiscussionBoarDao,_raiseYourHandDao, _cmsFeatureSearchService);
    }

    public void verifyAllMocks() {
        super.verifyMocks(_cmsCategoryDao, _searcher, _discussionDao, _publicationDao, _cmsDiscussionBoarDao,_raiseYourHandDao, _cmsFeatureSearchService);
    }

    protected CmsCategory getCategory(int id, String name) {
        CmsCategory cat = new CmsCategory();
        cat.setId(id);
        cat.setFullUri("/" + name);
        cat.setName(name);
        cat.setType(CmsCategory.TYPE_GRADE);
        return cat;
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

    public void testPopulateModelWithRecentCMSContentSomeCategories() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        List<CmsCategory> categories = new ArrayList<CmsCategory>(9);
        categories.add(getCategory(198, "p"));
        expect(_cmsCategoryDao.getCmsCategoriesFromIds("198,199,200,201,202,203,204,205,206")).andReturn(categories);
        expectGetCmsContentForCategoryUsingSolr(buildList("198"));
        replayAllMocks();
        _controller.populateModelWithRecentCMSContent(model);
        verifyAllMocks();
        assertNull(model.get(CmsHomepageController.MODEL_RECENT_CMS_CONTENT));
    }

    public void testPopulateModelWithRecentCMSContentNoResults() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        String ids = "198,199,200,201,202,203,204,205,206";
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
        expect(_cmsCategoryDao.getCmsCategoriesFromIds(ids)).andReturn(categories);
        expect(_publicationDao.populateByContentId(eq(1573L), isA(CmsTopicCenter.class))).andReturn(null);
        expect(_publicationDao.populateByContentId(eq(1574L), isA(CmsTopicCenter.class))).andReturn(null).times(6);
        expect(_publicationDao.populateByContentId(eq(1575L), isA(CmsTopicCenter.class))).andReturn(null);
        expect(_publicationDao.populateByContentId(eq(1576L), isA(CmsTopicCenter.class))).andReturn(null);
        expectGetCmsContentForCategoryUsingSolrNoResults(Arrays.asList(ids.split(",")));
        replayAllMocks();
        _controller.populateModelWithRecentCMSContent(model);
        verifyAllMocks();
        assertNull(model.get(CmsHomepageController.MODEL_RECENT_CMS_CONTENT));
    }

    public void testPopulateModelWithRecentCMSContentSomeResultsWithSolr() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        String ids = "198,199,200,201,202,203,204,205,206";
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
        expect(_cmsCategoryDao.getCmsCategoriesFromIds(ids)).andReturn(categories);
        expect(_publicationDao.populateByContentId(eq(1573L), isA(CmsTopicCenter.class))).andReturn(null);
        expect(_publicationDao.populateByContentId(eq(1574L), isA(CmsTopicCenter.class))).andReturn(null).times(6);
        expect(_publicationDao.populateByContentId(eq(1575L), isA(CmsTopicCenter.class))).andReturn(null);
        expect(_publicationDao.populateByContentId(eq(1576L), isA(CmsTopicCenter.class))).andReturn(null);
        expectGetCmsContentForCategoryUsingSolr(Arrays.asList(ids.split(",")));

        replayAllMocks();
        _controller.populateModelWithRecentCMSContent(model);
        verifyAllMocks();
        assertNull(model.get(CmsHomepageController.MODEL_RECENT_CMS_CONTENT));
    }

    private <T> List<T> buildList(T... elements) {
        List<T> list = new ArrayList<T>();
        for (T element : elements) {
            list.add(element);
        }
        return list;
    }

    public void testGetCmsContentForCategoryUsingSolr() throws Exception {
        List<Long> gradeIds = new ArrayList();
        gradeIds.add(1l);
        gradeIds.add(2l);
        gradeIds.add(3l);
        gradeIds.add(4l);

        SolrCmsFeatureSearchResult result1 = new SolrCmsFeatureSearchResult();
        result1.setGradeId(gradeIds);

        SolrCmsFeatureSearchResult result2 = new SolrCmsFeatureSearchResult();
        result2.setGradeId(gradeIds.subList(1,4));

        SolrCmsFeatureSearchResult result3 = new SolrCmsFeatureSearchResult();
        result3.setGradeId(gradeIds.subList(2,4));

        SolrCmsFeatureSearchResult result4 = new SolrCmsFeatureSearchResult();
        result4.setGradeId(gradeIds.subList(3,4));

        List<ICmsFeatureSearchResult> results = new ArrayList<ICmsFeatureSearchResult>();
        results.add(result1);
        results.add(result2);
        results.add(result3);
        results.add(result4);

        SearchResultsPage<ICmsFeatureSearchResult> searchResultsPage = new SearchResultsPage<ICmsFeatureSearchResult>(4, results);

        Map<Long,List<? extends ICmsFeatureSearchResult>> expectedResults = new HashMap<Long,List<? extends ICmsFeatureSearchResult>>();
        expectedResults.put(1l, buildList(result1));
        expectedResults.put(2l, buildList(result1,result2));
        expectedResults.put(3l, buildList(result1,result2,result3));
        expectedResults.put(4l, buildList(result1,result2,result3,result4));

        expect(_cmsFeatureSearchService.search(isA(SolrQuery.class))).andReturn(searchResultsPage);
        replay(_cmsFeatureSearchService);

        assertEquals(expectedResults, _controller.getCmsContentForCategoryUsingSolr(Arrays.asList("1,2,3,4".split(","))));

        verify(_cmsFeatureSearchService);
    }

    public void expectGetCmsContentForCategoryUsingSolr(List<String> gradeIds) throws Exception {

        List < ICmsFeatureSearchResult > results = new ArrayList<ICmsFeatureSearchResult>();
        for (String gradeId : gradeIds) {
            SolrCmsFeatureSearchResult result = new SolrCmsFeatureSearchResult();
            List<Long> resultGradeIds = new ArrayList<Long>();
            resultGradeIds.add(Long.valueOf(gradeId));
            result.setGradeId(resultGradeIds);
            results.add(result);
        }

        SearchResultsPage<ICmsFeatureSearchResult> searchResultsPage = new SearchResultsPage<ICmsFeatureSearchResult>(results.size(), results);

        expect(_cmsFeatureSearchService.search(isA(SolrQuery.class))).andReturn(searchResultsPage);
    }

    public void expectGetCmsContentForCategoryUsingSolrNoResults(List<String> gradeIds) throws Exception {

        List < ICmsFeatureSearchResult > results = new ArrayList<ICmsFeatureSearchResult>();

        SearchResultsPage<ICmsFeatureSearchResult> searchResultsPage = new SearchResultsPage<ICmsFeatureSearchResult>(results.size(), results);

        expect(_cmsFeatureSearchService.search(isA(SolrQuery.class))).andReturn(searchResultsPage);
    }

}
