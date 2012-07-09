var GS = GS || {};
GS.search = GS.search || {};
GS.search.filters = GS.search.filters || (function() {
    var savedFilters;

    var init = function() {
        $(function() {
            $('.js-applyFilters').on('click', function(){
                if($('#jq-filterBar').find($(this)).length > 0) {
                    GS.ad.refreshAds();
                }

                /*if (window.location.pathname.indexOf('search.page') > -1) {*/
                GS.search.results.update();
                GS.search.filters.save();
                var dropdownId = $(this).data('gs-dropdown-hider');
                $('html').unbind('click.gs.visibility.content.' + dropdownId);
                /*}*/
                var queryString = window.location.search;
                var queryStringData = GS.uri.Uri.getQueryData(queryString);
                if(queryStringData['view'] === 'map') {
                    var districtBoundaryLink = $('#js-showDistrictBoundaryLink').find('a');
                    var districtBoundaryUri = "/school-district-boundaries-map/?lat=" + queryStringData['lat']  + "&lon=" +
                                                queryStringData['lon'];
                    if(queryStringData['gradeLevels'] === undefined || queryStringData['gradeLevels'].length > 1){
                        districtBoundaryUri += '&level=e';
                    }
                    else {
                        districtBoundaryUri += '&level=' + queryStringData['gradeLevels'];
                    }
                    districtBoundaryLink.attr('href', districtBoundaryUri);
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
        'ratingCategories'
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
