define (['uri','searchResultFilters'], function(uri,searchResultFilters) {

    // set value each time module is initialized
    var $sortSelect = undefined;
    var lastSort = undefined;

    var loadMoreSelector = '#loadMore';

    var templateSelector = '#schoolSearchResultMobileTemplate';
    var template = null;

    // paging related items
    var firstPage = {}; // a hash with various paging information. See Page.java's getMap();
    var offsetKey = 'start';

    var currentOffset = 0;

    // filters info
    var filtersSelector = '.js-searchResultFilters';

    var init = function(page) {
        searchResultFilters.init(filtersSelector);
        firstPage = page;
        currentOffset = firstPage.offset;

        $(function() {
            $sortSelect = $('.js-sort-select'); // module might need to be reinitialized after an ajax page load

            var $loadMore = $(loadMoreSelector);
            $loadMore.on('click', function(){
                loadMore();
            });
            attachEventHandlers();
        });
    };

    var attachEventHandlers = function() {
        $sortSelect.on('change', function() {
            var value = $(this).val();
            if (value.length > 0) {
                var newQueryString = uri.putIntoQueryString(document.location.search, 'sortBy', value, true);
            } else {
                var newQueryString = uri.removeFromQueryString(document.location.search, 'sortBy');
            }
            var newUrl = window.location.pathname + newQueryString;
            lastSort = value;
            window.location.href = newUrl;
        });
    };

    var loadMore = function() {
        var queryString = window.location.search;
        GS.log('current offset: ', currentOffset);

        // get offset of next page

        var nextOffset = currentOffset + firstPage.pageSize;
        if (nextOffset > firstPage.lastOffset) {
            $('#loadMore').hide();
            return false;
        }

        // change offset in url
        queryString = uri.putIntoQueryString(queryString, offsetKey, nextOffset, true);
        queryString = uri.putIntoQueryString(queryString, 'ajax', 'true', true);

        var url = window.location.protocol + '//' + window.location.host + window.location.pathname + queryString;

        GS.log('searching using url:', url);

        $.ajax({
            url:url,
            type:'get'
        }).done(function(data) {
            GS.log('search got data back from ajax call: ', data);

            if (data) {
                var $list = $("#js-schoolSearchResultsList");
                $list.append(data);
            }

            if (nextOffset + firstPage.pageSize > firstPage.lastOffset) {
                $('#loadMore').hide();
            }

            currentOffset = nextOffset;
            GS.log('new current offset: ', currentOffset);

        }).fail(function(data) {
            GS.log('paging failed', data);
        });
    };

    return {
        init:init
    }
});