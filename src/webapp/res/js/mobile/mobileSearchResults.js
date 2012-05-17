define (['uri','searchResultFilters', 'history'], function(uri,searchResultFilters) {

    // set value each time module is initialized
    var $sortSelect = undefined;
    var lastSort = undefined;

    var searchResultsSelector = '#js-schoolSearchResultsList';
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
        searchResultFilters.init(filtersSelector, applyFilters);
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

    // when we load in results via ajax after filters are applied, we lose track of how many pages there are, etc
    // there only way to know (since we're returning html from the ajax call) is to embed the values into the dom
    // and extract them, or include a script block within the html that will get executed and set the page data
    // on this module
    var reset = function(page) {
        firstPage = page;
        currentOffset = 0;
        $('.js_searchResultsCount').html(page.totalResults);
        if (page.isLastPage != true) {
            $(loadMoreSelector).show();
        } else {
            $(loadMoreSelector).hide();
        }
    };

    var attachEventHandlers = function() {
        $sortSelect.on('change', function() {
            var value = $(this).val();

            var queryString = searchResultFilters.getUpdatedQueryString();
            if (value.length > 0) {
                var newQueryString = uri.putIntoQueryString(queryString, 'sortBy', value, true);
            } else {
                var newQueryString = uri.removeFromQueryString(queryString, 'sortBy');
            }
            var newUrl = window.location.pathname + newQueryString;
            lastSort = value;
            window.location.href = newUrl;
        });
        $('#searchFilter').on('click', function(){
            listFilterToggle();
        });
        $('.js-searchCancel').on('click',function(){
            listFilterToggle();
        });
        $('.js-mapResultsLink').on('click', function() {
            var newQueryString = searchResultFilters.getUpdatedQueryString();
            newQueryString = uri.removeFromQueryString(newQueryString, 'view');
            newQueryString = uri.putIntoQueryString(newQueryString, 'view', 'map');
            window.location.search = newQueryString;
            return false;
        });
    };

    var applyFilters = function() {
        var queryString = searchResultFilters.getUpdatedQueryString();

        // rewrite URL using history API
        if (typeof(window.History) !== 'undefined' && window.History.enabled === true) {
            // use HTML 5 history API to rewrite the current URL to represent the new state.
            window.History.replaceState(null, document.title, queryString);
        }

        queryString = uri.putIntoQueryString(queryString, 'ajax', true);
        var url = window.location.protocol + '//' + window.location.host + window.location.pathname + queryString;

        $.ajax({
            url:url,
            type:'get'
        }).done(function(data) {
            GS.log('search got data back from filtering ajax call: ', data);
            if (data) {
                var $list = $(searchResultsSelector);
                $list.html(data);
                listFilterToggle();

            }
        }).fail(function(data) {
            GS.log('filtering failed', data);
        });
    };

    var listFilterToggle = function(){
        $('#js-school-search-results-table').toggle();
        $('.js-nearbyMoreResults').toggle();
        $('.js-searchResultFilters').toggle();
    }

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
                var $list = $(searchResultsSelector);
                $list.append(data);
            }

            if (nextOffset + firstPage.pageSize > firstPage.lastOffset) {
                $(loadMoreSelector).hide();
            }

            currentOffset = nextOffset;
            GS.log('new current offset: ', currentOffset);

        }).fail(function(data) {
            GS.log('paging failed', data);
        });
    };

    return {
        reset:reset,
        init:init
    }
});