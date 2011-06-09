package gs.web.pagination;

public class DefaultPaginationConfig extends PaginationConfig {
    public static final String DEFAULT_OFFSET_PARAM = "start";
    public static final String DEFAULT_PAGE_NUMBER_PARAM = "p";
    public static final String DEFAULT_PAGE_SIZE_PARAM = "pageSize";
    public static final int DEFAULT_PAGE_SIZE = 25;
    public static final int DEFAULT_MAX_PAGE_SIZE = 100;

    public static final boolean ZERO_BASED_OFFSET = true;
    public static final boolean ZERO_BASED_PAGES = false;

    public DefaultPaginationConfig() {
        super(DEFAULT_PAGE_SIZE_PARAM, DEFAULT_PAGE_NUMBER_PARAM, DEFAULT_OFFSET_PARAM, DEFAULT_PAGE_SIZE, DEFAULT_MAX_PAGE_SIZE, ZERO_BASED_OFFSET, ZERO_BASED_PAGES);
    }
}
