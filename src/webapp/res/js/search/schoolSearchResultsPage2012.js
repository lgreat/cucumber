GS = GS || {};

GS.schoolSearchResultsPage = GS.schoolSearchResultsPage || (function() {
    var listResultsLinkSelector = '.js-listResultsLink';
    var mapResultsLinkSelector = '.js-mapResultsLink';
    var body = '#contentGS';

    var init = function() {
        registerEventHandlers();
        GS.search.schoolSearchForm.init(GS.search.filters, GS.ui.contentDropdowns);
    };

    var registerEventHandlers = function() {
        $(body).on('click', listResultsLinkSelector, function() {
            if(GS.uri.Uri.getFromQueryString('view') === undefined) {
                return;
            }
            else {
                var uri = window.location.search;
                uri = GS.uri.Uri.removeFromQueryString(uri, 'view');
                window.location.search = uri;
            }
        });
        $(body).on('click', mapResultsLinkSelector, function() {
            if(GS.uri.Uri.getFromQueryString('view') === 'map') {
                return;
            }
            else {
                var uri = window.location.search;
                uri = GS.uri.Uri.putIntoQueryString(uri, 'view', 'map', true);
                window.location.search = uri;
            }
        });
    };

    return {
        init:init

    }

})();

jQuery(function () {
    GS.schoolSearchResultsPage.init();
});