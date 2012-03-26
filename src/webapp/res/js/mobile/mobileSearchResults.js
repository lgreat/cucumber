define (['jquery','jquery.mobile', 'uri'], function($, jQueryMobile, uri) {

    // set value each time module is initialized
    var $sortSelect = undefined;
    var lastSort = undefined;

    var init = function() {
        $sortSelect = $('.js-sort-select'); // module might need to be reinitialized after an ajax page load
        attachEventHandlers();
    };

    var attachEventHandlers = function() {
        $sortSelect.on('change', function() {
            var value = $(this).val();
            var newQueryString = uri.putIntoQueryString(document.location.search, 'sortBy', value, true);
            var newUrl = window.location.pathname + newQueryString;
            lastSort = value;
            $.mobile.changePage(newUrl);
        });
    };

    return {
        init:init
    }
});