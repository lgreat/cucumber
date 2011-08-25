package gs.web.pagination;

import gs.data.pagination.PaginationConfig;
import org.easymock.EasyMock;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class PaginationTest {

    @Test
    public void testGetRequestedPage() throws Exception {

        PaginationConfig config = new PaginationConfig("pageSize","pageNumber","start",20,100,true,false);

        RequestedPage requestedPage = Pagination.getRequestedPage(20, null, 2, config);

        assertEquals(20, requestedPage.offset);

        requestedPage = Pagination.getRequestedPage(20, 40, null, config);
        assertEquals(3, requestedPage.pageNumber);
        assertEquals(20, requestedPage.pageSize);
    }


    @Test
    public void testGetRequestedPageUsesDefaultPageSize() throws Exception {

        PaginationConfig config = new PaginationConfig("pageSize","pageNumber","start",15,100,true,false);

        RequestedPage requestedPage = Pagination.getRequestedPage(null, null, 2, config);

        assertEquals(15, requestedPage.pageSize);
    }

    @Test
    public void getPageFromRequest() throws Exception {
        PaginationConfig config = new PaginationConfig("pageSize","pageNumber","start",20,100,true,false);

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);

        expect(request.getParameter(config.getPageSizeParam())).andReturn("20");

        expect(request.getParameter(config.getOffsetParam())).andReturn("40");

        expect(request.getParameter(config.getPageNumberParam())).andReturn(null);

        replay(request);

        Pagination.getPageFromRequest(request, config);

        verify(request);
    }
}


