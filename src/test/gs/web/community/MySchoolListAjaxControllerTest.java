package gs.web.community;

import gs.data.school.ISchoolDao;
import gs.web.BaseControllerTestCase;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.easymock.EasyMock.createStrictMock;

public class MySchoolListAjaxControllerTest extends BaseControllerTestCase {
    MySchoolListAjaxController _controller;

    ISchoolDao _schoolDao;


    public void setUp() {

        _controller = new MySchoolListAjaxController();

        _schoolDao = createStrictMock(ISchoolDao.class);

        _controller.setSchoolDao(_schoolDao);

    }

    public void testHandleAdd() throws Exception {
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
/*
        request.setAttribute();


        _controller.handleAdd()*/

    }

    public void testHandleDelete() throws Exception {

    }
}
