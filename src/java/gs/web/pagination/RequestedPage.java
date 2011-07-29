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

    /**
     * Compares requested offset with how many results were returned. Returns the first offset of the last page if
     * requested offset does not exist
     * @param config
     * @param numberOfActualResults one-based count of results
     * @return the correct one-based offset to use
     */
    public int getValidatedOffset(PaginationConfig config, int numberOfActualResults) {

        int oneBasedOffset = config.getOneBasedPosition(offset);
        int firstOffsetOfLastPage = offset;

        if (oneBasedOffset > numberOfActualResults) {
            int lastOffset;
            if (config.isZeroBasedOffset()) {
                lastOffset = numberOfActualResults-1;
            } else {
                lastOffset = numberOfActualResults;
            }

            int lastPage = Pagination.getPageNumber(pageSize, lastOffset, config);

            firstOffsetOfLastPage = Pagination.getOffset(pageSize, lastPage, config);
        }
        
        return firstOffsetOfLastPage;
    }
}
