package gs.web.pagination;

public class PaginationConfig {
    /**
     * The string literal that will be used if the page size is stored in a map
     */
    private final String _pageSizeParam;

    /**
     * The string literal that will be used if the page number is stored in a map
     */
    private final String _pageNumberParam;

    /**
     * The string literal that will beu sed if the offset is stored in a map
     */
    private final String _offsetParam;

    /**
     * The default page size to be used if one is not present
     */
    private final int _defaultPageSize;

    /**
     * Offsets starts with value 0 if true
     */
    private boolean _zeroBasedOffset;

    /**
     * Page numbers start with value 0 if true
     */
    private boolean _zeroBasedPages;

    private int _maxPageSize;

    public PaginationConfig(String pageSizeParam, String pageNumberParam, String offsetParam, int defaultPageSize, int maxPageSize, boolean zeroBasedOffset, boolean zeroBasedPages) {
        _pageSizeParam = pageSizeParam;
        _pageNumberParam = pageNumberParam;
        _offsetParam = offsetParam;
        _defaultPageSize = defaultPageSize;
        _maxPageSize = maxPageSize;
        _zeroBasedOffset = zeroBasedOffset;
        _zeroBasedPages = zeroBasedPages;
    }

    public String getPageSizeParam() {
        return _pageSizeParam;
    }

    public String getPageNumberParam() {
        return _pageNumberParam;
    }

    public String getOffsetParam() {
        return _offsetParam;
    }

    public int getDefaultPageSize() {
        return _defaultPageSize;
    }

    public boolean isZeroBasedOffset() {
        return _zeroBasedOffset;
    }

    public boolean isZeroBasedPages() {
        return _zeroBasedPages;
    }

    public int getFirstOffset() {
        return _zeroBasedOffset? 0:1;
    }

    public int getMaxPageSize() {
        return _maxPageSize;
    }

    /**
     * Get the zero-based position for the given offset
     * @param offset
     * @return
     */
    public int getZeroBasedPosition(int offset) {
        if (isZeroBasedOffset()) {
            return offset;
        } else {
            return offset + 1;
        }
    }

    /**
     * Get the one-based position for the given offset
     * @param offset
     * @return
     */
    public int getOneBasedPosition(int offset) {
        if (isZeroBasedOffset()) {
            return offset + 1;
        } else {
            return offset;
        }
    }
}
