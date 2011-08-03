package gs.web.pagination;

import gs.data.pagination.DefaultPaginationConfig;
import gs.data.pagination.PaginationConfig;

import java.util.List;
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
        
        if (offset > _pager.getLastOffset()) {
            throw new NoSuchElementException("Tried to page beyond end of results");
        }

        Page page = new Page(_pager, offset);
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
}
