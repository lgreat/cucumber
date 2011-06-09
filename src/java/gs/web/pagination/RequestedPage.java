package gs.web.pagination;

/**
 * Contains enough information to search for results in a specific page only
 */
public class RequestedPage {
    public final int offset;
    public final int pageNumber;
    public final int pageSize;

    public RequestedPage(int offset, int pageNumber, int pageSize) {
        this.offset = offset;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }
}
