package gs.web.pagination;

import gs.web.util.UrlUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * Static methods for doing pagination-related stuff
 */
public class Pagination {

    public static final String MODE_PAGE_NUMBER = "pageNumber";
    public static final String MODE_OFFSET = "offset";

    private static final DefaultPaginationConfig DEFAULT_PAGINATION_CONFIG = new DefaultPaginationConfig();

    public static RequestedPage getPageFromRequest(HttpServletRequest request) {
        return getPageFromRequest(request, DEFAULT_PAGINATION_CONFIG);
    }

    /**
     * Attempts to retrieve values necessary for paging from an HttpServletRequest.
     *
     * @param request
     * @param config
     * @return
     */
    public static RequestedPage getPageFromRequest(HttpServletRequest request, PaginationConfig config) {

        Integer offset = null;
        Integer pageNumber;
        Integer pageSize;

        try {
            pageSize = Integer.valueOf(request.getParameter(config.getPageSizeParam()));
        } catch (NumberFormatException e) {
            pageSize = config.getDefaultPageSize();
        }
        if (pageSize < 1 || pageSize > config.getMaxPageSize()) {
            pageSize = config.getDefaultPageSize();
        }

        try {
            //try to get offset from request
            offset = Integer.valueOf(request.getParameter(config.getOffsetParam()));
            if (offset < 0) {
                //throw away offset if it's not valid. It will be set to default value later.
                offset = null;
            }
        } catch (NumberFormatException e) {
            //handle this below
        }

        try {
            //now try to get page number from request
            pageNumber = Integer.valueOf(request.getParameter(config.getPageNumberParam()));
            //if we succeeded but offset is null, set offset based on page number
            if (offset == null) {
                Pagination.getOffset(pageSize, pageNumber, config.isZeroBasedOffset(), config.isZeroBasedPages());
            }
        } catch (NumberFormatException e) {
            //if we couldnt get a page number then set it based on offset. if offset is null, set it to first offset
            if (offset == null) {
                offset = config.getFirstOffset();
            }
            pageNumber = Pagination.getPageNumber(pageSize, offset, config.isZeroBasedOffset(), config.isZeroBasedPages());
        }
        
        RequestedPage params = new RequestedPage(offset, pageNumber, pageSize);

        return params;
    }

    /**
     * Calculate the number of pages for page size and result count
     * @param pageSize
     * @param numberOfResults
     * @return
     */
    public static int getNumberOfPages(int pageSize, int numberOfResults) {
        if (pageSize == 0) {
            throw new IllegalArgumentException("pageSize must be greater than zero");
        }
        
        int numberOfPages = (int) Math.ceil(numberOfResults / ((float)pageSize));
        return numberOfPages;
    }

    /**
     * Calculates the page number that the given result exists on
     *
     * @param pageSize
     * @param resultNumber
     * @param zeroBasedOffset whether or not you're using zero or one-based offsets
     * @param zeroBasedPages whether or not you're using zero or one-based page numbers
     * @return page number, which depends on zeroBasedPages argument
     */
    public static int getPageNumber(int pageSize, int resultNumber, boolean zeroBasedOffset, boolean zeroBasedPages) {
        int firstOffset = zeroBasedOffset? 0 : 1;
        int firstPage = zeroBasedPages? 0 : 1;

        int pageNumber = (int) (Math.ceil((resultNumber+1-firstOffset) / ((float)pageSize)));
        return firstPage + (pageNumber-1);
    }

    /**
     * Calculates the page number that the given result exists on. Integer used as starting page depends on provided
     * PaginationConfig
     * 
     * @param pageSize
     * @param resultNumber
     * @param config
     * @return
     */
    public static int getPageNumber(int pageSize, int resultNumber, PaginationConfig config) {
        return getPageNumber(pageSize, resultNumber, config.isZeroBasedOffset(), config.isZeroBasedPages());
    }

    public static int getPageNumber(int pageSize, int resultNumber) {
        return getPageNumber(pageSize, resultNumber, DEFAULT_PAGINATION_CONFIG);
    }

    /**
     * Gets the starting offset for the given page
     *
     * @param pageSize
     * @param pageNumber
     * @param zeroBasedOffset whether or not you're using zero or one-based offsets
     * @param zeroBasedPages whether or not you're using zero or one-based page numbers
     * @return
     */
    public static int getOffset(int pageSize, int pageNumber, boolean zeroBasedOffset, boolean zeroBasedPages) {
        int firstOffset = zeroBasedOffset? 0 : 1;
        int firstPage = zeroBasedPages? 0 : 1;

        int offset = pageSize * (pageNumber-firstPage);
        return offset + firstOffset;
    }

    /**
     * Gets the starting offset for the given page
     * @param pageSize
     * @param pageNumber
     * @param config
     * @return
     */
    public static int getOffset(int pageSize, int pageNumber, PaginationConfig config) {
        return getOffset(pageSize, pageNumber, config.isZeroBasedOffset(), config.isZeroBasedPages());
    }

    public static int getOffset(int pageSize, int pageNumber) {
        return getOffset(pageSize, pageNumber, DEFAULT_PAGINATION_CONFIG);
    }

    public static String recordPageInUrl(Page page, String url, String mode) {
        boolean usingOffset = !MODE_PAGE_NUMBER.equalsIgnoreCase(mode);

        if (usingOffset) {
            int offset = page.getOffset();
            return recordOffsetInUrl(offset, url);
        } else {
            int pageNumber = page.getPageNumber();
            return recordPageNumberInUrl(pageNumber, url);
        }
    }

    public static String recordOffsetInUrl(int offset, String url) {
        String newUrl = UrlUtil.putQueryParamIntoUrl(url, DEFAULT_PAGINATION_CONFIG.getOffsetParam(), String.valueOf(offset));
        return newUrl;
    }

    public static String recordPageNumberInUrl(int pageNumber, String url) {
        String newUrl = UrlUtil.putQueryParamIntoUrl(url, DEFAULT_PAGINATION_CONFIG.getPageNumberParam(), String.valueOf(pageNumber));
        return newUrl;
    }

}
