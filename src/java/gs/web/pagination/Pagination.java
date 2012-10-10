package gs.web.pagination;

import gs.data.pagination.PaginationConfig;
import gs.web.util.UrlUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * Static methods for doing pagination-related stuff
 */
public class Pagination {

    public static final String MODE_PAGE_NUMBER = "pageNumber";
    public static final String MODE_OFFSET = "offset";

    public static RequestedPage getRequestedPage(Integer pageSize, Integer offset, Integer pageNumber) {
        return getRequestedPage(pageSize, offset, pageNumber, gs.data.pagination.Pagination.DEFAULT_PAGINATION_CONFIG);
    }

    public static RequestedPage getPageFromRequest(HttpServletRequest request) {
        return getPageFromRequest(request, gs.data.pagination.Pagination.DEFAULT_PAGINATION_CONFIG);
    }

    public static RequestedPage getRequestedPage(Integer pageSize, Integer offset, Integer pageNumber, PaginationConfig config) {
        
        if (pageSize == null || pageSize < 1 || pageSize > config.getMaxPageSize()) {
            pageSize = config.getDefaultPageSize();
        }

        if (offset != null && offset < 0) {
            //throw it away if it's not valid
            offset = null;
        }

        if (pageNumber != null && pageNumber < 0) {
            pageNumber = null;
        }

        //if we have pageNumber but no offset, get offset from page number, and vice versa
        if (offset == null && pageNumber != null) {
            offset = gs.data.pagination.Pagination.getOffset(pageSize, pageNumber, config.isZeroBasedOffset(), config.isZeroBasedPages());
        } else if (offset != null && pageNumber == null) {
            pageNumber = gs.data.pagination.Pagination.getPageNumber(pageSize, offset, config.isZeroBasedOffset(), config.isZeroBasedPages());
        } else if (offset == null && pageNumber == null) {
            offset = config.getFirstOffset();
            pageNumber = gs.data.pagination.Pagination.getPageNumber(pageSize, offset, config.isZeroBasedOffset(), config.isZeroBasedPages());
        }
        
        RequestedPage params = new RequestedPage(offset, pageNumber, pageSize);

        return params;
    }


    /**
     * Attempts to retrieve values necessary for paging from an HttpServletRequest.
     *
     * @param request
     * @param config
     * @return
     */
    public static RequestedPage getPageFromRequest(HttpServletRequest request, PaginationConfig config) {

        Integer offset;
        Integer pageNumber;
        Integer pageSize;

        try {
            pageSize = Integer.valueOf(request.getParameter(config.getPageSizeParam()));
        } catch (NumberFormatException e) {
            pageSize = null;
        }

        try {
            offset = Integer.valueOf(request.getParameter(config.getOffsetParam()));
        } catch (NumberFormatException e) {
            offset = null;
        }

        try {
            pageNumber = Integer.valueOf(request.getParameter(config.getPageNumberParam()));
        } catch (NumberFormatException e) {
            pageNumber = null;
        }
        
        RequestedPage params = getRequestedPage(pageSize, offset, pageNumber, config);

        return params;
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
        String newUrl;
        if (offset != 0) {
            newUrl = UrlUtil.putQueryParamIntoUrl(url, gs.data.pagination.Pagination.DEFAULT_PAGINATION_CONFIG.getOffsetParam(), String.valueOf(offset));
        } else {
            newUrl = UrlUtil.removeQueryParamsFromUrl(url, gs.data.pagination.Pagination.DEFAULT_PAGINATION_CONFIG.getOffsetParam());
        }
        return newUrl;
    }

    public static String recordPageNumberInUrl(int pageNumber, String url) {
        String newUrl = UrlUtil.putQueryParamIntoUrl(url, gs.data.pagination.Pagination.DEFAULT_PAGINATION_CONFIG.getPageNumberParam(), String.valueOf(pageNumber));
        return newUrl;
    }

}
