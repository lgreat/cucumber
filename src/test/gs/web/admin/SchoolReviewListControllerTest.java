package gs.web.admin;

import gs.data.school.review.IReviewDao;
import gs.web.BaseControllerTestCase;

import static org.easymock.EasyMock.*;

/**
 * @author Anthony Roy <mailto:aroy@greatschools.net>
 */
public class SchoolReviewListControllerTest extends BaseControllerTestCase {
    private SchoolReviewListController _controller;
    private IReviewDao _reviewDao;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        _controller = new SchoolReviewListController();

        _reviewDao = createStrictMock(IReviewDao.class);

        _controller.setReviewDao(_reviewDao);

        _controller.setViewName("view");
    }

    public void testBasics() {
        assertSame(_reviewDao, _controller.getReviewDao());
        assertEquals("view", _controller.getViewName());
    }
}
