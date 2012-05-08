define(['uri','ui'],function(uri, ui) {

    var filtersSelector;
    var applyCallback; // gets called when filter Apply button is pressed

    // defines the generic state/behavior for each group of boolean filters
    var BooleanFilter = function(key, defaultFilters) {
        this.key = key;
        this.filters = {};
        this.defaultFilters = defaultFilters;

        this.setFiltersToDefault = function() {
            for (var prop in this.defaultFilters) {
                if (this.defaultFilters.hasOwnProperty(prop)) {
                    this.filters[prop] = this.defaultFilters[prop];
                }
            }
        };

        // set filters to default as part of the constructor function
        this.setFiltersToDefault();

        this.toggle = function(filter) {
            if (this.filters.hasOwnProperty(filter)) {
                this.filters[filter] = !this.filters[filter];
            }
            this.updateDomForOne(filter);
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

        this.updateDom = function() {
            for (var filter in this.filters) {
                if (this.filters.hasOwnProperty(filter)) {
                    this.updateDomForOne(filter);
                }
            }
        };

        this.updateDomForOne = function(filter) {
            var $filter = $(filtersSelector + ' [data-'+dataAttributes.booleanFilter+'=' + filter + ']');
            GS.log('updating dom for ', $filter);
            if (this.filters[filter] === true) {
                $filter.removeClass(ui.buttonClass);
                $filter.addClass(ui.buttonPressedClass);
            } else {
                $filter.addClass(ui.buttonClass);
                $filter.removeClass(ui.buttonPressedClass);
            }
        };

        this.readFromQueryString = function() {
            var queryData = uri.getQueryData();
            var values = queryData[this.key];
            if (values !== undefined) {
                for (var filter in this.filters) {
                    if (this.filters.hasOwnProperty(filter)) {
                        if (values instanceof Array) {
                            this.filters[filter] = (values.indexOf(filter) > -1);
                        } else {
                            this.filters[filter] = (filter === values);
                        }
                    }
                }
            }
        };

        this.readFromDom = function($jq) {
            if ($jq === undefined) {
                $jq = $(filtersSelector + ' [data-'+dataAttributes.booleanFilterGroup + '=' + this.key + ']');
            }
            var $booleanFilters = $jq.find('[data-'+dataAttributes.booleanFilter + ']');

            var filters = this.filter; // allow below anonymous function in each call to access filters without
                                       // prefixing with "this."

            $booleanFilters.each(function() {
                var filterName = $(this).data(dataAttributes.booleanFilter);
                filters[filterName] = $(this).hasClass(ui.buttonPressed);
            });
        };

        this.reset = function() {
            this.setFiltersToDefault();
            this.updateDom();
        };
    };

    /*var RangeFilter = function(minKey, maxKey, defaultMinValue, defaultMaxValue) {
        this.minKey = minKey;
        this.maxKey = maxKey;
        this.minValue = defaultMinValue;
        this.maxValue = defaultMaxValue;
        this.defaultMinValue = defaultMinValue;
        this.defaultMaxValue = defaultMaxValue;
        this.reset = function() {
            this.minValue = this.defaultMinValue;
            this.maxValue = this.defaultMaxValue;
        };
        this.toQueryString = function() {
            var queryString = this.minKey + "=" + this.minValue + "&" + this.maxKey + "=" + this.maxValue;
            return queryString;
        };
        this.readFromQueryString = function() {
            var queryData = uri.getQueryData();
            var maxValue = queryData[this.maxKey];
            if (maxValue !== undefined) {
                this.maxValue = maxValue;
            }
            var minValue = queryData[this.minKey];
            if (minValue !== undefined) {
                this.minValue = minValue;
            }
        };
        this.readFromDom = function($jq) {
            if ($jq === undefined) {
                $jq = $(filtersSelector + ' [data-' + dataAttributes.rangeFilterMin +'] :selected');
            }

            this.minValue = $jq.data(dataAttributes.rangeFilterMin);
            this.maxValue = $jq.data(dataAttributes.rangeFilterMax);
        }
    };*/

    var SelectFilter = function(key, defaultValue) {
        this.key = key;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.init = function(value) {
            this.value = value;
            this.defaultValue = value;
        };
        this.reset = function() {
            this.value = this.defaultValue;
            this.updateDom();
        };
        this.toQueryString = function() {
            var queryString = "";
            if (this.value !== undefined && this.value.length > 0) {
                var queryString = this.key + "=" + this.value;
            }
            return queryString;
        };
        this.readFromQueryString = function() {
            var queryData = uri.getQueryData();
            var value = queryData[this.key];
            if (value !== undefined) {
                this.value = value;
            }
        };
        this.readFromDom = function($jq) {
            if ($jq === undefined) {
                $jq = $(filtersSelector + ' [name=' + this.key + ']');
            }
            this.value = $jq.val();
        };
        this.updateDom = function() {
            var $jq = $(filtersSelector + ' [name=' + this.key + ']');
            $jq.val(this.value);
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

    var distance = new SelectFilter('distance', '25');
    var minGreatSchoolsRating = new SelectFilter('minGreatSchoolsRating', undefined);
    var minCommunityRating = new SelectFilter('minCommunityRating', undefined);
    var schoolSize = new SelectFilter('schoolSize', 'All');


    // put the filters into an array for easy iterating
    var filters = [schoolType, gradeLevel, distance, minGreatSchoolsRating, minCommunityRating, schoolSize];
    // TODO: remove redundancy here and/or at module return value
    var filtersHash = {
        schoolType:schoolType,
        gradeLevel:gradeLevel,
        distance:distance,
        minGreatSchoolsRating:minGreatSchoolsRating,
        minCommunityRating:minCommunityRating,
        schoolSize:schoolSize
    };

    // some methods for the searchResultFilters module
    var toQueryString = function() {
        var queryString = "";
        for (var i = 0; i < filters.length; i++) {
            var filterQs = filters[i].toQueryString();
            if (filterQs.length > 0) {
                if (queryString.length > 0) {
                    queryString += "&";
                }

                queryString += filterQs;
            }
        }
        return queryString;
    };

    var reset = function() {
        for (var i = 0; i < filters.length; i++) {
            filters[i].reset();
        }
    };

    var initFiltersFromQueryString = function() {
        for (var i = 0; i < filters.length; i++) {
            var filter = filters[i];
            filter.readFromQueryString();
        }
    };

    var getUpdatedQueryString = function() {
        var newQueryString = toQueryString();
        var currentQueryString = window.location.search;

        for (var i = 0; i < filters.length; i++) {
            var filter = filters[i];
            if (filter instanceof BooleanFilter || filter instanceof SelectFilter) {
                currentQueryString = uri.removeFromQueryString(currentQueryString, filter.key);
            }
        }

        if (currentQueryString.length > 1) {
            newQueryString = currentQueryString + '&' + newQueryString;
        } else {
            newQueryString = '?' + newQueryString;
        }

        return newQueryString;
    };

    // a hash to look up short keys to GS html5 custom data attributes
    var dataAttributes = {
        booleanFilter: 'school-boolean-filter',
        booleanFilterGroup: 'school-boolean-filter-group',
        rangeFilterGroup: 'school-range-filter-group',
        rangeFilterMin: 'school-range-filter-min',
        rangeFilterMax: 'school-range-filter-max',
        selectFilter: 'school-select-filter',
        filterAction: 'school-filter-action'
    };

    var selectors = {};

    var updateUrl = function() {
        window.location.search = getUpdatedQueryString();
    };

    var setupBooleanFilterHandlers = function() {
        // when a boolean filter button is pressed, toggle associated boolean filter in JS module
        $(selectors.booleanFilterGroups).on('click', '[data-' + dataAttributes.booleanFilter + ']', function() {
            var booleanGroupName = $(this).parent().data(dataAttributes.booleanFilterGroup);
            var booleanFilter = $(this).data(dataAttributes.booleanFilter);
            filtersHash[booleanGroupName].toggle(booleanFilter);

            if(s.tl) {
                if(booleanFilter === 'public') {
                    s.tl(this,'o', 'Mobile_filter_type_public');
                }
                else if(booleanFilter === 'private') {
                    s.tl(this,'o', 'Mobile_filter_type_private');
                }
                else if(booleanFilter === 'charter') {
                    s.tl(this,'o', 'Mobile_filter_type_charter');
                }
                else if(booleanFilter === 'preschool') {
                    s.tl(this,'o', 'Mobile_filter_grade_PK');
                }
                else if(booleanFilter === 'elementary') {
                    s.tl(this,'o', 'Mobile_filter_grade_elem');
                }
                else if(booleanFilter === 'middle') {
                    s.tl(this,'o', 'Mobile_filter_grade_middle');
                }
                else if(booleanFilter === 'high') {
                    s.tl(this,'o', 'Mobile_filter_grade_high');
                }
            }
        });
    };

    /*var setupRangeFilterHandlers = function() {
        // when an option in a select dropdown is chosen, set the associated min/max values into a JS object
        $(selectors.rangeFilterGroups).on('click', '[data-' + dataAttributes.rangeFilterMin +']', function() {
            var $this = $(this);
            var rangeGroupName = $this.parent().data(dataAttributes.rangeFilterGroup);
            var rangeFilter = filtersHash[rangeGroupName];

            rangeFilter.minValue = $this.data(dataAttributes.rangeFilterMin);
            rangeFilter.maxValue = $this.data(dataAttributes.rangeFilterMax);
        });
    };*/

    var setupSelectFilterHandlers = function() {
        // when an option in a select dropdown is chosen, set the associated min/max values into a JS object
        $(filtersSelector + ' .js-selectFilter').on('change', function() {
            var $this = $(this);
            var filterName = $this.attr('name');
            var filter = filtersHash[filterName];

            filter.value = $this.val();

            if (s.tl) {
                if (filterName === 'distance') {
                    s.tl(this,'o', 'Mobile_filter_distance_' + distance.value);
                }
                else if (filterName === 'minGreatSchoolsRating') {
                    if(minGreatSchoolsRating.value === undefined || minGreatSchoolsRating.value === '') {
                        s.tl(this,'o', 'Mobile_filter_GSrating_any');
                    }
                    else {
                        s.tl(this,'o', 'Mobile_filter_GSrating_' + minGreatSchoolsRating.value.toLowerCase());
                    }
                }
                else if (filterName === 'minCommunityRating') {
                    if(minCommunityRating.value === undefined || minCommunityRating.value === '') {
                        s.tl(this,'o', 'Mobile_filter_CommRating_any');
                    }
                    else {
                        s.tl(this,'o', 'Mobile_filter_CommRating_' + minCommunityRating.value.toLowerCase());
                    }
                }
                else if (filterName === 'schoolSize') {
                    if(schoolSize.value === undefined || schoolSize.value === '') {
                        s.tl(this,'o', 'Mobile_filter_size_all');
                    }
                    else {
                        s.tl(this,'o', 'Mobile_filter_size_' + schoolSize.value.toLowerCase());
                    }
                }
            }
        });
    };

    var setupButtonEvents = function() {
        // when a button/link/other is clicked, perform associated action
        $(selectors.filterActions).on('click', function() {
            var action = $(this).data(dataAttributes.filterAction);
            if (action === 'reset') {
                reset();
            } else if (action === 'apply') {
                applyCallback();
            }
        });
    };

    var init = function(selector, callback) {
        filtersSelector = selector;
        applyCallback = callback;

        selectors = {
            booleanFilters: filtersSelector + ' [data-' + dataAttributes.booleanFilter + ']',
            booleanFilterGroups: filtersSelector + ' [data-' + dataAttributes.booleanFilterGroup + ']',
            rangeFilterGroups: filtersSelector + ' [data-' + dataAttributes.rangeFilterGroup + ']',
            rangeFilters: filtersSelector + ' [data-' + dataAttributes.rangeFilterMin + ']',
            selectFilters: filtersSelector + ' [data-' + dataAttributes.selectFilter + ']',
            filterActions: filtersSelector + ' [data-' + dataAttributes.filterAction + ']'
        };

        $(function() {
            initFiltersFromQueryString();

            setupBooleanFilterHandlers();

            setupSelectFilterHandlers();

            setupButtonEvents();
        });
    };

    return {
        init:init,
        toQueryString:toQueryString,
        gradeLevel:gradeLevel,
        distance:distance,
        getUpdatedQueryString:getUpdatedQueryString
    };


});