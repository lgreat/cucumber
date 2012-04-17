define (['uri','hogan'], function(uri,hogan) {

    // set value each time module is initialized
    var $sortSelect = undefined;
    var lastSort = undefined;

    var loadMoreSelector = '#loadMore';

    var templateSelector = '#schoolSearchResultMobileTemplate';
    var template = null;

    // paging related items
    var currentPage = {}; // a hash with various paging information. See Page.java's getMap();
    var offsetKey = 'start';

    var init = function(page) {
        currentPage = page;

        $sortSelect = $('.js-sort-select'); // module might need to be reinitialized after an ajax page load

        var $loadMore = $(loadMoreSelector);
        $loadMore.on('click', function(){
            loadMore();
        });
        attachEventHandlers();

        $(function() {
            template = hogan.compile($(templateSelector).html());
        });
    };

    var attachEventHandlers = function() {
        $sortSelect.on('change', function() {
            var value = $(this).val();
            var newQueryString = uri.putIntoQueryString(document.location.search, 'sortBy', value, true);
            var newUrl = window.location.pathname + newQueryString;
            lastSort = value;
            window.location.href = newUrl;
        });
    };

    var loadMore = function() {
        var queryString = window.location.search;

        // get offset of next page
        var nextOffset = currentPage.offset;
        if (!currentPage.isLastPage) {
            nextOffset = currentPage.lastOffsetOnPage + 1;
        } else {
            return false;
        }

        // change offset in url
        queryString = uri.putIntoQueryString(queryString, offsetKey, nextOffset, true);
        queryString = uri.putIntoQueryString(queryString, 'format', 'json', true);

        var url = window.location.protocol + '//' + window.location.host + window.location.pathname + queryString;
        url = url.replace('.page','.json');

        GS.log('searching using url:', url);

        $.ajax({
            url:url,
            type:'get',
            dataType:'json'
        }).done(function(data) {
            GS.log('search got data back from ajax call: ', data);

            if (data.totalResults > 0) {
                var $list = $("#js-schoolSearchResultsList");
                for (var i = 0; i < data.schoolSearchResults.length; i++) {
                    var result = data.schoolSearchResults[i];
                    var html = template.render({
                        schoolId:result.id,
                        schoolDatabaseState:result.databaseState,
                        schoolName:result.name,
                        address:result.address,
                        city:result.city,
                        zipCode:result.zip,
                        schoolType:result.schoolType,
                        levelCode:result.levelCode,
                        gradeRangeString:result.grades.rangeString,
                        greatSchoolsRating:result.greatSchoolsRating,
                        parentRating:result.parentRating,
                        distance:result.distance
                    });

                    $list.append(html);
                }
            }

            currentPage = data.page;
            if (currentPage.isLastPage === true) {
                $('#loadMore').hide();
            }

        }).fail(function(data) {
            GS.log('paging failed', data);
        });

    };

    return {
        init:init
    }
});