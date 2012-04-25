define(['uri','ui'],function(uri, ui) {

    var filtersSelector;

    // defines the generic state/behavior for each group of boolean filters
    var BooleanFilter = function(key, filters) {
        this.key = key;
        this.filters = filters;
        this.defaultFilters = filters;
        this.toggle = function(filter) {
            if (filters.hasOwnProperty(filter)) {
                filters[filter] = !filters[filter];
            }
        };
        // does not prepend question mark
        this.toQueryString = function() {
            var queryString = "";
            for (var filter in this.filters) {
                if (this.filters.hasOwnProperty(filter) && this.filters[filter] === true) {
                    if (queryString.length > 0) {
                        queryString += "&";
                    }
                    queryString += this.key + "=" + filter;
                }
            }
            return queryString;
        };

        this.reset = function() {
            filters = this.defaultFilters;
        }
    };

    var RangeFilter = function(lowKey, highKey) {
        this.lowKey = lowKey;
        this.highKey = highKey;
        this.lowValue = undefined;
        this.highValue = undefined;
        this.defaultLowValue = undefined;
        this.defaultHighValue = undefined;
        this.init = function(lowValue, highValue) {
            this.lowValue = lowValue;
            this.defaultLowValue = lowValue;
            this.highValue = highValue;
            this.defaultHighValue = highValue;
        };
        this.reset = function() {
            this.lowValue = this.defaultLowValue;
            this.highValue = this.defaultHighValue;
        };
        this.toQueryString = function() {
            var queryString = this.lowKey + "=" + this.lowValue + "&" + this.highKey + "=" + this.highValue;
            return queryString;
        };
    };

    // create the filters
    var schoolType = new BooleanFilter('st', {
        public:true,
        charter:true,
        private:true
    });

    var gradeLevel = new BooleanFilter('gradeLevels', {
        preschool:true,
        elementary:true,
        middle:true,
        high:true
    });

    var enrollment = new RangeFilter('minEnrollment','maxEnrollment');

    // put the filters into an array for easy iterating
    var filters = [schoolType, gradeLevel, enrollment];
    // TODO: remove redundancy here and/or at module return value
    var filtersHash = {
        schoolType:schoolType,
        gradeLevel:gradeLevel,
        enrollment:enrollment
    };

    // some methods for the searchResultFilters module
    var toQueryString = function() {
        var queryString = "";
        for (var i = 0; i < filters.length; i++) {
            if (queryString.length > 0) {
                queryString += "&";
            }
            queryString += filters[i].toQueryString();
        }
        return queryString;
    };

    var reset = function() {
        for (var i = 0; i < filters.length; i++) {
            filters[i].reset();
        }
    };

    // make the state of the dom reflect the state of filters when arriving on a page with filters pre-chosen
    var updateDomClasses = function() {
        // for each filter set up in this JS file, get the filter object
        // if the filter is a boolean filter, get the boolean filters (key values) from query string, and update the state of the filter obj
        // if the filter is a range filter, get the high/low values from the query string and update the state of the filter obj
        // other filter types
        /*for (var i = 0; i < filters.length; i++) {
            $('[data-school-filter-group]').each(function() {

            });
        }*/
    };

    var getUpdatedQueryString = function() {
        var newQueryString = toQueryString();
        var currentQueryString = window.location.search;

        for (var i = 0; i < filters.length; i++) {
            var filter = filters[i];
            if (filter instanceof BooleanFilter) {
                currentQueryString = uri.removeFromQueryString(currentQueryString, filter.key);
            } else if (filter instanceof RangeFilter) {
                currentQueryString = uri.removeFromQueryString(currentQueryString, filter.lowKey);
                currentQueryString = uri.removeFromQueryString(currentQueryString, filter.highKey);
            }
        }

        if (currentQueryString.length > 1) {
            newQueryString = currentQueryString + '&' + newQueryString;
        } else {
            newQueryString = '?' + newQueryString;
        }

        return newQueryString;
    };

    var dataAttributes = {
        booleanFilter: 'school-boolean-filter',
        booleanFilterGroup: 'school-boolean-filter-group',
        rangeFilterGroup: 'school-range-filter-group',
        rangeFilterLow: 'school-range-filter-low',
        rangeFilterHigh: 'school-range-filter-high',
        rangeFilterDefault: 'school-range-filter-default',
        filterAction: 'school-filter-action'
    };

    var updateUrl = function() {
        window.location.search = getUpdatedQueryString();
    };

    var setupBooleanFilterHandlers = function() {
        // when a boolean filter button is pressed, toggle associated boolean filter in JS module
        $(filtersSelector + ' [data-'+dataAttributes.booleanFilterGroup+']').on('click', '[data-' + dataAttributes.booleanFilter + ']', function() {
            var booleanGroupName = $(this).parent().data(dataAttributes.booleanFilterGroup);
            var booleanFilter = $(this).data(dataAttributes.booleanFilter);
            GS.log(booleanGroupName, booleanFilter, filtersHash);
            filtersHash[booleanGroupName].toggle(booleanFilter);
        });
    };

    var setupRangeFilterHandlers = function() {
        // when an option in a select dropdown is chosen, set the associated low/high values into a JS object
        $(filtersSelector + ' [data-'+dataAttributes.rangeFilterGroup+']').on('click', '[data-' + dataAttributes.rangeFilterLow +']', function() {
            var $this = $(this);
            var rangeGroupName = $this.parent().data(dataAttributes.rangeFilterGroup);
            var rangeFilter = filtersHash[rangeGroupName];

            rangeFilter.lowValue = $this.data(dataAttributes.rangeFilterLow);
            rangeFilter.highValue = $this.data(dataAttributes.rangeFilterHigh);
        });
    };

    var setupButtonEvents = function() {
        // when a button/link/other is clicked, perform associated action
        $(filtersSelector + ' [data-'+dataAttributes.filterAction+']').on('click', function() {
            var action = $(this).data(dataAttributes.filterAction);
            if (action === 'reset') {
                reset();
            } else if (action === 'apply') {
                updateUrl();
            }
        });
    };

    var init = function(selector) {
        filtersSelector = selector;

        $(function() {
            // when the page is loaded, read the default values for range filter from the dom and set them onto JS object
            $(filtersSelector + ' [data-'+dataAttributes.rangeFilterDefault+']').each(function() {
                var $this = $(this);
                var rangeGroupName = $this.parent().data('school-range-filter-group');
                filtersHash[rangeGroupName].init(
                        $this.data(dataAttributes.rangeFilterLow),
                        $this.data(dataAttributes.rangeFilterHigh)
                );
            });

            setupBooleanFilterHandlers();

            setupRangeFilterHandlers();

            setupButtonEvents();

            // make the state of the dom reflect the state of filters when arriving on a page with filters pre-chosen
            // TODO: updateDomClasses();
        });
    };

    return {
        init:init,
        toQueryString:toQueryString,
        schoolType:schoolType,
        gradeLevel:gradeLevel,
        enrollment:enrollment,
        getUpdatedQueryString:getUpdatedQueryString
    };


});