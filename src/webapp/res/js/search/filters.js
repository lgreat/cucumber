var GS = GS || {};
GS.search = GS.search || {};
GS.search.filters = GS.search.filters || (function() {
    var savedFilters;

    var init = function() {
        $(function() {
            $('.js-applyFilters').on('click', function(){
                /*if (window.location.pathname.indexOf('search.page') > -1) {*/
                jQuery("#js_totalResultsCountReturn").hide();
                jQuery("#js-spinny-search").show();
                GS.search.results.update();
                GS.search.filters.save();
                var dropdownId = $(this).data('gs-dropdown-hider');
                $('html').unbind('click.gs.visibility.content.' + dropdownId);
                /*}*/
                var queryString = window.location.search;
                var queryStringData = GS.uri.Uri.getQueryData(queryString);
                if(queryStringData['view'] === 'map') {
                    var districtBoundaryLink = $('#js-showDistrictBoundaryLink').find('a');
                    var districtBoundaryUri = "/school-district-boundaries-map/?";
                    // if we can pull lat/lon from query, do so
                    if (queryStringData['lat'] !== undefined && queryStringData['lon'] !== undefined) {
                        districtBoundaryUri += "lat=" + queryStringData['lat']  + "&lon=" +
                            queryStringData['lon'] + "&";
                    } else {
                        // otherwise parse them out of the pre-written link
                        var existingHref = districtBoundaryLink.attr('href');
                        var queryStart = existingHref.indexOf("?");
                        if (queryStart > -1) {
                            var existingQuery = existingHref.substring(queryStart);
                            var existingQueryData = GS.uri.Uri.getQueryData(existingQuery);
                            if (existingQueryData.passThroughURI) {
                                existingHref = decodeURIComponent(existingQueryData.passThroughURI);
                                queryStart = existingHref.indexOf("?");
                                existingQuery = existingHref.substring(queryStart);
                                existingQueryData = GS.uri.Uri.getQueryData(existingQuery);
                            }
                            if (existingQueryData.lat !== undefined && existingQueryData.lon !== undefined) {
                                districtBoundaryUri += "lat=" + existingQueryData['lat']  + "&lon=" +
                                    existingQueryData['lon'] + "&";
                            }
                        }
                    }
                    if(queryStringData['gradeLevels'] === undefined || queryStringData['gradeLevels'].length > 1){
                        districtBoundaryUri += 'level=e';
                    } else {
                        districtBoundaryUri += 'level=' + queryStringData['gradeLevels'];
                    }
                    if (districtBoundaryUri.indexOf('lat') > -1 &&
                        districtBoundaryUri.indexOf('lon') > -1 &&
                        districtBoundaryUri.indexOf('level') > -1) {
                        districtBoundaryLink.attr('href', districtBoundaryUri);
                    }
                }
            });
        });
    };

    var globalCheckboxAndRadioFilters = [
        'distance',
        'gradeLevels',
        'st',
        'religious'
    ];

    var advancedCheckboxAndRadioFilters = [
        'beforeAfterCare',
        'transportation',
        'ell',
        'studentsVouchers',
        'schoolFocus',
        'specialEdPrograms',
        'sports',
        'artsAndMusic',
        'studentClubs',
        'ratingCategories',
        'staffResources'
    ];

    // strings must match checkbox group field names
    var checkboxAndRadioFilters = globalCheckboxAndRadioFilters.concat(advancedCheckboxAndRadioFilters);

    var save = function() {
        savedFilters = getFilterData();
    };

    var getSavedFilters = function() {
        return savedFilters;
    };

    var reset = function() {
        for (var index in advancedCheckboxAndRadioFilters) {
            $('input:checkbox[name=' + advancedCheckboxAndRadioFilters[index] + ']:checked').prop('checked',false).trigger('change');
        }
    };

    var valsToArray = function(selector) {
        var result = [];
        var $elements = $(selector);
        $elements.each(function() {
            var $this = $(this);
            var val = $this.val();
            if (val !== undefined && val !== '') {
                result.push(val);
            }
        });
        return result;
    };

    var checkboxesAndRadiosToArray = function(name, parentSelector) {
        //TODO: limit jquery searches to children of specific ID
        var selector = 'input[name=' + name + ']:checked';
        if (parentSelector !== undefined) {
            selector = parentSelector + ' ' +selector;
        }

        return valsToArray(selector);
    };

    var getUpdatedQueryStringData = function() {
        var queryStringData = getQueryStringDataWithoutFilters();
        var filterData = getFilterData();

        $.extend(queryStringData, filterData);
        return queryStringData;
    };

    var getQueryStringDataWithoutFilters = function() {
        var queryStringData = GS.uri.Uri.getQueryData();

        for (var i in checkboxAndRadioFilters) {
            if (queryStringData.hasOwnProperty(checkboxAndRadioFilters[i])) {
                delete queryStringData[checkboxAndRadioFilters[i]];
            }
        }

        return queryStringData;
    };

    var getUpdatedQueryString = function() {
        var queryStringData = getUpdatedQueryStringData();

        var queryString = GS.uri.Uri.getQueryStringFromObject(queryStringData);

        return queryString;
    };

    var getFilterData = function() {
        var data = {};
        var i = 0;

        //TODO: limit jquery searches to children of specific ID

        for (var j = 0; j < checkboxAndRadioFilters.length; j++) {
            var ospFilter = checkboxAndRadioFilters[j];
            var checkedFilters = checkboxesAndRadiosToArray(ospFilter);
            data[ospFilter] = checkedFilters;
        }

        var distanceSelect = jQuery('#findASchoolTabs input[name=distance]');
        if (distanceSelect.size() == 1) {
            data["distance"] = distanceSelect.val();
        }

        return data;
    };


    return {
        init:init,
        getFilterData:getFilterData,
        getUpdatedQueryStringData:getUpdatedQueryStringData,
        getUpdatedQueryString:getUpdatedQueryString,
        save:save,
        getSavedFilters:getSavedFilters,
        reset:reset,
        getQueryStringDataWithoutFilters:getQueryStringDataWithoutFilters
    }

})();
