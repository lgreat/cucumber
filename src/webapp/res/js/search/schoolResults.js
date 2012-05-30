var GS = GS || {};
GS.search = GS.search || {};
GS.search.results = GS.search.results || (function() {
    var thisDomElement = jQuery('#school-search-results-table-body tbody'); //TODO: pass this into constructor
    var filtersModule;
    var compareModule;

    // http://stackoverflow.com/questions/1744310/how-to-fix-array-indexof-in-javascript-for-ie-browsers
    // we use indexOf on some arrays in this .js file, but IE doesn't support it natively, so we have to implement it here
    if (!Array.prototype.indexOf) {
        Array.prototype.indexOf = function(obj, start) {
            for (var i = (start || 0), j = this.length; i < j; i++) {
                if (this[i] == obj) { return i; }
            }
            return -1;
        }
    }

    var init = function(_filtersModule, _compareModule) {
        filtersModule = _filtersModule;
        compareModule = _compareModule;

        attachEventHandlers();

        compareModule.initializeRowsAndCheckedSchoolsArray();
    };

    var url = function() {
        var value = window.location.protocol + "//" + window.location.host + window.location.pathname;
        return value;
    };


    var search = function(callback, errorCallback, queryStringData) {
        var queryString = GS.uri.Uri.getQueryStringFromObject(queryStringData);

        var data = {};
        data.requestType = "ajax";
        data.decorator="emptyDecorator";
        data.confirm="true";

        if (typeof(window.History) !== 'undefined' && window.History.enabled === true) {
            // use HTML 5 history API to rewrite the current URL to represent the new state.
            window.History.replaceState(null, document.title, queryString);
        }

        jQuery.ajax({
            type: "get",
            url: url() + queryString,
            data:data,
            success: callback,
            error: errorCallback,
            traditional: true
        });
    };


    var clear = function() {
        thisDomElement.find('.school-search-result-row').remove();
    };

    var update = function(queryStringData) {
        if (queryStringData === undefined) {
            queryStringData = filtersModule.getUpdatedQueryStringData();
        }

        var onSearchSuccess = function(data) {
            var afterFadeIn = function() {
                jQuery("#spinner").hide();
                //reattach callbacks to dom element events
                attachEventHandlers();
                compareModule.updateAllCheckedRows();
            };

            jQuery('#js-school-search-results-table').html(data);
            jQuery('#school-search-results-table-body').css("opacity",.2);
            jQuery('#school-search-results-table-body').animate(
                    {opacity: 1},
                    250,
                    'linear',
                    afterFadeIn
            );
            GS.util.htmlMirrors.updateAll();
        };

        var onSearchError = function() {
            clear();
            jQuery("#spinner").hide();
        };

        jQuery('#spinner').show();

        jQuery('#school-search-results-table-body').animate(
                { opacity: .2 },
                250,
                'linear',
                function() {
                    search(onSearchSuccess, onSearchError, queryStringData);
                }
        );
    };

    var onPageSizeChanged = function() {
        var queryString = window.location.search;
        queryString = GS.uri.Uri.putIntoQueryString(queryString,"pageSize",jQuery('#page-size').val(), true);
        queryString = GS.uri.Uri.removeFromQueryString(queryString, "start");
        window.location.search = queryString;
    };

    var onSortChanged = function() {
        var i = 0;
        var gradeLevels = [];
        var schoolTypes = [];
        var queryString = window.location.search;

        queryString = persistCompareCheckboxesToQueryString(queryString);
        queryString = GS.uri.Uri.putIntoQueryString(queryString,"sortChanged",true, true);
        queryString = GS.uri.Uri.removeFromQueryString(queryString, "start");

        window.location.search = queryString;
    };

    var page = function(pageNumber, pageSize) {
        var start = (pageNumber-1) * pageSize;

        var queryString = window.location.search;
        queryString = persistCompareCheckboxesToQueryString(queryString);
        queryString = GS.uri.Uri.putIntoQueryString(queryString,"start",start, true);

        window.location.search = queryString;
    };

    var persistCompareCheckboxesToQueryString = function(queryString) {
        var compareSchoolsList = compareModule.getCheckedSchools().join(',');
        if (compareSchoolsList !== undefined && compareSchoolsList.length > 0) {
            queryString = GS.uri.Uri.putIntoQueryString(queryString, "compareSchools", compareSchoolsList, true);
        } else {
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "compareSchools");
        }
        return queryString;
    };


    var sendToCompare = function() {
        var checkedSchools = compareModule.getCheckedSchools();
        if (checkedSchools !== undefined && checkedSchools.length > 0) {
            var encodedCurrentUrl = encodeURIComponent(window.location.pathname + filtersModule.getUpdatedQueryString());
            window.location ='/school-comparison-tool/results.page?schools=' + checkedSchools.join(',') +
                    '&source=' + encodedCurrentUrl;
        }
    };

    var attachEventHandlers = function() {
        jQuery('.compare-school-checkbox').click(compareModule.onCompareCheckboxClicked);
        jQuery('#page-size').change(onPageSizeChanged);
        jQuery('#sort-by').change(onSortChanged);
        jQuery('.js-compareButton').click(sendToCompare());
        jQuery('.js-num-checked-send-to-compare').click(sendToCompare());
        jQuery('.js-compare-uncheck-all').click(compareModule.onCompareUncheckAllClicked);
    };

    return {
        init:init,
        update:update,
        page:page
    };

})();