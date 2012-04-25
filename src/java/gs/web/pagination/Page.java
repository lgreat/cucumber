package gs.web.pagination;

import gs.data.pagination.DefaultPaginationConfig;
import gs.data.pagination.PaginationConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Provides an easy way to group page info (for example when providing it to a jsp view).
 * Represents a page within a group of pages. Knows how to get previous or next pages
 */
public class Page {

    /**
     * Starting offset of the current page
     */
    private final int _offset;

    private final int _pageNumber;

    private final Pager _pager;

    public Page(int offset, int pageSize, int totalItems, PaginationConfig paginationConfig) {
        _pager = new Pager(totalItems, pageSize, paginationConfig);

        _offset = offset;
        _pageNumber = _pager.getPageNumber(_offset);
    }

    public Page(int offset, int pageSize, int totalItems) {
        this(offset, pageSize, totalItems, new DefaultPaginationConfig());
    }

    /**
     * @param pager
     * @param offset the starting offset of this page
     */
    Page(Pager pager, int offset) {
        _pager = pager;
        _offset = offset;
        _pageNumber = _pager.getPageNumber(_offset);
    }

    public int getLastOffsetOnPage() {
        int lastOffsetOnPage = _offset + (_pager.getPageSize() - 1);

        if (lastOffsetOnPage > _pager.getLastOffset()) {
            lastOffsetOnPage = _pager.getLastOffset();
        }

        return lastOffsetOnPage;
    }

    public boolean isFirstPage() {
        return _pager.getFirstPageNumber() == _pageNumber;
    }

    public boolean isLastPage() {
        return _pager.getLastPageNumber() == _pageNumber;
    }

    public Page getPreviousPage() {
        return pageBack(1);
    }

    public Page getNextPage() {
        return pageAhead(1);
    }

    public Page pageAhead(int numberOfPages) {
        int offset = _offset + (numberOfPages * _pager.getPageSize());
        Page page;
        
        if (offset > _pager.getLastOffset()) {
            //throw new NoSuchElementException("Tried to page beyond end of results");
            //if there are 6 results, and page size is 4, there are 2 pages. If this page starts at result 4, then this
            //page would include all results for the next page (page 2). If this happens, just return the last page
            //when asking for the next page. not ideal, but I don't know of better solution without more effort/risk
            page = _pager.getLastPage();
        } else {
            page = new Page(_pager, offset);
        }

        return page;
    }

    public Page pageBack(int numberOfPages) {
        int offset = _offset - (numberOfPages * _pager.getPageSize());

        if (offset < _pager.getFirstOffset()) {
            throw new NoSuchElementException("Tried to page beyond start of results");
        }

        Page page = new Page(_pager, offset);
        return page;
    }

    public List<Page> getPageSequence() {
        return _pager.getPageSequence(_pageNumber);
    }

    public int getOffset() {
        return _offset;
    }

    public int getPageNumber() {
        return _pageNumber;
    }

    public Pager getPager() {
        return _pager;
    }

    public Map<String,Object> getMap() {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("isFirstPage", isFirstPage());
        map.put("isLastPage", isLastPage());
        map.put("offset", _offset);
        map.put("pageNumber", _pageNumber);
        map.put("lastOffsetOnPage", getLastOffsetOnPage());
        map.put("firstOffset", _pager.getFirstOffset());
        map.put("lastOffset", _pager.getLastOffset());
        map.put("firstPageNumber", _pager.getFirstPageNumber());
        map.put("lastPageNumber", _pager.getLastPageNumber());
        map.put("pageSize", _pager.getPageSize());
        return map;
    }
}
