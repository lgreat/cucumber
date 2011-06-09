package gs.web.pagination;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Immutable class with info on how to control paging for a specific set of pages
 */
public final class Pager {
    private final int _totalItems;
    private final int _pageSize;
    private final int _firstOffset;
    private final int _lastOffset;
    private final int _firstPageNumber;
    private final int _lastPageNumber;
    private final int _totalPages;
    private final PaginationConfig _paginationConfig;

    public Pager(int totalItems, int pageSize, PaginationConfig paginationConfig) {
        _paginationConfig = paginationConfig;

        _totalItems = totalItems;
        _pageSize = pageSize;

        _totalPages = Pagination.getNumberOfPages(_pageSize, totalItems);

        _firstOffset = paginationConfig.isZeroBasedOffset()? 0 : 1;
        _lastOffset = paginationConfig.isZeroBasedOffset()? totalItems-1 : totalItems;
        _firstPageNumber = paginationConfig.isZeroBasedPages()? 0 : 1;
        _lastPageNumber =  paginationConfig.isZeroBasedPages()? _totalPages -1 : _totalPages;
    }

    public Page getFirstPage() {
        return getPage(_firstPageNumber);
    }

    public Page getLastPage() {
        return getPage(_lastPageNumber);
    }

    public Page getPage(int pageNumber) {
        int offset = getOffset(pageNumber);

        if (offset < getFirstOffset() || offset > getLastOffset()) {
            throw new NoSuchElementException("Tried to page beyond start of results");
        }

        Page page = new Page(this, offset);
        return page;
    }

    public List<Page> getPageSequence(int pageNumber) {
        int fifthPageNumber = isZeroBasedPages() ? 4:5;

        Integer firstPageInSeries;
        Integer lastPageInSeries;

        if (getTotalPages() <= fifthPageNumber) {
            firstPageInSeries = getFirstPageNumber();
            lastPageInSeries = getLastPageNumber();
        } else if (pageNumber < fifthPageNumber) {
            firstPageInSeries = getFirstPageNumber();
            lastPageInSeries = fifthPageNumber;
        } else if (pageNumber > (getTotalPages() - 4) && getTotalPages() > fifthPageNumber) {
            firstPageInSeries = getLastPageNumber() - 4;
            lastPageInSeries = getLastPageNumber();
        } else {
            firstPageInSeries = pageNumber - 2;
            lastPageInSeries = pageNumber + 2;
        }

        List<Page> series = new ArrayList<Page>();

        for (int i = firstPageInSeries; i <= lastPageInSeries; i++) {
            Page page = new Page(this, getOffset(i));
            series.add(page);
        }

        return series;
    }

    public int getPageNumber(int resultNumber) {
        return Pagination.getPageNumber(_pageSize, resultNumber, _paginationConfig.isZeroBasedOffset(), _paginationConfig.isZeroBasedPages());
    }

    public int getOffset(int pageNumber) {
        return Pagination.getOffset(_pageSize, pageNumber, _paginationConfig.isZeroBasedOffset(), _paginationConfig.isZeroBasedPages());
    }

    public int getTotalItems() {
        return _totalItems;
    }

    public int getPageSize() {
        return _pageSize;
    }

    public int getFirstOffset() {
        return _firstOffset;
    }

    public int getLastOffset() {
        return _lastOffset;
    }

    public int getFirstPageNumber() {
        return _firstPageNumber;
    }

    public int getLastPageNumber() {
        return _lastPageNumber;
    }

    public int getTotalPages() {
        return _totalPages;
    }

    public boolean isZeroBasedOffset() {
        return _paginationConfig.isZeroBasedOffset();
    }

    public boolean isZeroBasedPages() {
        return _paginationConfig.isZeroBasedPages();
    }
}