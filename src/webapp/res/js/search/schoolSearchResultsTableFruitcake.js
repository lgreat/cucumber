GS = GS || {};
GS.search = GS.search || {};

GS.search.SchoolSearchResult = function() {
    var element = jQuery('#' + 'js-school-search-result-template');//TODO: pass into constructor
    var nameObject = element.find('.js-school-search-result-name');
    var streetObject = element.find('.js-school-search-result-street');
    var cityStateZipObject = element.find('.js-school-search-result-citystatezip');

    this.setName = function(name) {
        nameObject.html(name);
    };

    this.setStreet = function(street) {
        streetObject.html(street);
    };

    this.setCityStateZip = function(cityStateZip) {
        cityStateZipObject.html(cityStateZip);
    };

    this.getElement = function() {
        return element;
    }
};

GS.search.SchoolSearcher = function() {
    this.url = function() {
        var value = window.location.href;
        value = window.location.protocol + "//" + window.location.host + window.location.pathname;
        return value;
    };

    this.valsToArray = function(selector) {
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

    this.checkboxesToArray = function(name, parentSelector) {
        var selector = 'input:checkbox[name=' + name + ']:checked';
        if (parentSelector !== undefined) {
            selector = parentSelector + ' ' +selector;
        }
        return this.valsToArray(selector);
    };

    this.search = function(callback, errorCallback) {
        var i = 0;
        var data = {};
        var gradeLevels = [];
        var schoolTypes = [];
        var affiliations = [];
        var ospFilters = ['beforeAfterCare','transportation','ell','studentsVouchers','schoolFocus', 'specialEdPrograms','sports','artsAndMusic','studentClubs']; // strings must match checkbox group field names

        data.requestType = "ajax";
        data.decorator="emptyDecorator";
        data.confirm="true";

        //to populate an array inside a Spring command, Spring requires data in format gradeLevels[0]=e,gradeLevels[1]=m
        jQuery('#js-gradeLevels :checked').each(function() {
            if (jQuery(this).val() !== '') {
                gradeLevels[i++] = jQuery(this).val();
            }
        });

        i = 0;
        jQuery('#js-schoolTypes :checked').each(function() {
            if (jQuery(this).val() !== '') {
                schoolTypes[i++] = jQuery(this).val();
            }
        });

        i=0;
        jQuery('#js-affiliations :checked').each(function() {
            if (jQuery(this).val() !== '') {
                affiliations[i++] = jQuery(this).val();
            }
        });

        data["gradeLevels"] = gradeLevels;
        data["st"] = schoolTypes;
        data["affiliations"] = affiliations;

        var addOspFiltersToRequestData = function() {
            for (var j = 0; j < ospFilters.length; j++) {
                var ospFilter = ospFilters[j];
                var checkedFilters = this.checkboxesToArray(ospFilter);
                data[ospFilter] = checkedFilters;
            }
        };
        addOspFiltersToRequestData.call(this);

        // GS-11789
        //data["studentTeacherRatio"] = jQuery('#studentTeacherRatioSelect').val();
        var schoolSize = jQuery('#schoolSizeSelect').val();
        if (schoolSize !== 'All') {
            data["schoolSize"] = jQuery('#schoolSizeSelect').val();
        }

        var distanceSelect = jQuery('#findASchoolTabs input[name=distance]');
        if (distanceSelect.size() == 1) {
            data["distance"] = distanceSelect.val();
        }

        var queryString = window.location.search;
        queryString = removeFromQueryString(queryString, "gradeLevels");
        queryString = removeFromQueryString(queryString, "st");
        queryString = removeFromQueryString(queryString, "affiliations");
        queryString = removeFromQueryString(queryString, "studentTeacherRatio");
        queryString = removeFromQueryString(queryString, "schoolSize");
        queryString = removeFromQueryString(queryString, "distance");
        queryString = removeFromQueryString(queryString, "start");

        // remove OSP filter keys
        for (var k = 0; k < ospFilters.length; k++) {
            queryString = removeFromQueryString(queryString, ospFilters[k]);
        }

        if (typeof(window.History) !== 'undefined' && window.History.enabled === true) {
            // use HTML 5 history API to rewrite the current URL to represent the new state.
            window.History.replaceState(null, document.title, buildQueryString(window.location.search));
        }

        console.log('searching...:', this.url() + queryString, data);
        jQuery.ajax({type: "get", url: this.url() + queryString, data:data, success: callback, error: errorCallback, traditional: true});
    }
};

GS.search.SchoolSearchResultsTable = function() {
    var thisDomElement = jQuery('#school-search-results-table-body tbody'); //TODO: pass this into constructor
    var checkedSchools = [];
    var searcher = new GS.search.SchoolSearcher();
    var maxCheckedSchools = 8;

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

    /**
     * This method is needed because when clicking browse back button, checkboxes stay checked but saved array is gone.
     */
    this.findCheckedSchools = function() {
        var checkedSchools = [];
        jQuery('#school-search-results-table-body tbody input:checked').each(function() {
            checkedSchools.push(jQuery(this).attr("id"));
        });
        return checkedSchools;
    };

    /**
     * Get array of state+schoolIds that are listed in query string
     */
    this.getQueryStringSchools = function() {
        var checkedSchoolsList = getFromQueryString("compareSchools");
        var queryStringSchools = [];
        if (checkedSchoolsList !== undefined && checkedSchoolsList.length > 0) {
            queryStringSchools =  checkedSchoolsList.split(',');
        }
        return queryStringSchools;
    };

    /**
     * When table is set up and page is loaded (or browse back button is clicked), reset saved checked schools
     * and make sure all rows are highlighted correctly, etc
     */
    this.initializeRowsAndCheckedSchoolsArray = function() {
        var queryStringSchools = this.getQueryStringSchools();
        var numberOfQueryStringSchools = queryStringSchools.length;

        //start with all the checkboxes checked on this page
        checkedSchools = this.findCheckedSchools();

        //add to array all of checkboxes which are checked, according to what's in the query string
        for (var i = 0; i < numberOfQueryStringSchools; i++) {
            jQuery('#' +queryStringSchools[i]).prop("checked", true);
            var index = checkedSchools.indexOf(queryStringSchools[i]);
            if (index === -1) {
                checkedSchools.push(queryStringSchools[i]);
            }
        }

        this.updateAllCheckedRows();
    };

    this.onCompareUncheckAllClicked = function() {
        var queryString = window.location.search;
        queryString = removeFromQueryString(queryString, "compareSchools");

        queryString = buildQueryString(queryString);

        window.location.search = queryString;
    }.gs_bind(this);

    this.onCompareCheckboxClicked = function(item) {
        var checkbox = jQuery(item.currentTarget);
        var checked = checkbox.prop("checked");
        var rowId = checkbox.parent().parent().attr("id");
        var row = jQuery('#' + rowId);
        var statePlusSchoolId = row.find('input.compare-school-checkbox').attr('id');

        if (checked) {
            if (checkedSchools.length >= maxCheckedSchools) {
                var encodedCurrentUrl = encodeURIComponent(window.location.pathname + buildQueryString(window.location.search));
                GSType.hover.compareSchoolsLimitReached.show(checkedSchools.join(','), encodedCurrentUrl, this.onCompareUncheckAllClicked);
                return false;
            }
            checkedSchools.push(statePlusSchoolId);
            this.selectRow(checkbox.parent().parent().attr("id"));
        } else {
            var index = checkedSchools.indexOf(statePlusSchoolId);
            if (index !== -1) {
                checkedSchools.splice(index,1);
            }
            this.deselectRow(checkbox.parent().parent().attr("id"));
        }

        this.updateAllCheckedRows();
    }.gs_bind(this);

    this.updateAllCheckedRows = function() {
        var count = checkedSchools.length;

        for (var i = 0; i < count; i++) {
            var id = checkedSchools[i];
            jQuery('#' + id).prop('checked', true);
            this.selectRow(jQuery('#'+id).parent().parent().attr("id"));
        }
        this.updateNumCheckedSchoolsText();
    };

    this.updateCompareButton = function(rowDomId) {
        var row = jQuery('#' + rowDomId);
        var compareLabel = row.find('td.js-checkbox-column > .js-compareLabel');
        var compareHelperMessage = row.find('td.js-checkbox-column > .js-compareHelperMessage');
        var compareButton = row.find('td.js-checkbox-column > .js-compareButton');

        compareLabel.hide();
        compareHelperMessage.hide();
        compareButton.hide();

        if (checkedSchools.length === 0) {
            compareLabel.show();
        } else if (checkedSchools.length === 1) {
            compareHelperMessage.show();
        } else if (checkedSchools.length > 1) {
            compareButton.show();
        }
    };

    this.selectRow = function(rowDomId) {
        var row = jQuery('#' + rowDomId);
        if (row.size() > 0) {
            var isWhite = null;
            var pattern2 = /_y|_b/gi;
            var stars = row.find('td.stars-column > a > span');
            var starsClass = stars.attr('class');
            var badge = row.find('td.badge-column > a > span');
            (starsClass.match(pattern2) === null) ? isWhite = true : isWhite = false;

            jQuery(row).find('td').removeClass("bg-color-fff");
            jQuery(row).find('td').addClass("bg-color-f4fafd");

            if (badge.length !== 0 && isWhite) {
                var badgeClass = badge.attr('class');
                var blueBadgeClass = badgeClass + '_b';
                badge.removeClass(badgeClass).addClass(blueBadgeClass);
            }
            if (isWhite) {
                var blueStarsClass = starsClass + '_b';
                stars.removeClass(starsClass).addClass(blueStarsClass);
            }
            this.updateCompareButton(rowDomId);
        }
    };

    this.deselectRow = function(rowDomId) {
        var row = jQuery('#' + rowDomId);
        if (row.size() > 0) {
            var isBlue = null;
            var pattern1 = /_b/gi;
            var pattern3 = /sprite badge_sm_(\d{1,2}|[a-z]{2})/gi;
            var pattern4 = /sprite stars_sm_(\d|[a-z_]{7})/gi;
            var stars = row.find('td.stars-column > a > span');
            var starsClass = stars.attr('class');
            (starsClass.match(pattern1) === null) ? isBlue = false : isBlue = true;
            var whiteStarsClass = starsClass.match(pattern4)[0];
            var badge = row.find('td.badge-column > a > span');
            if (badge.length != 0) {
                var badgeClass = badge.attr('class');
                var whiteBadgeClass = badgeClass.match(pattern3)[0];
                badge.removeClass(badgeClass).addClass(whiteBadgeClass);
            }
            stars.removeClass(starsClass).addClass(whiteStarsClass);

            jQuery(row).find('td').removeClass("bg-color-f4fafd");
            jQuery(row).find('td').addClass("bg-color-fff");

            var compareLabel = row.find('td.js-checkbox-column > .js-compareLabel');
            var compareHelperMessage = row.find('td.js-checkbox-column > .js-compareHelperMessage');
            var compareButton = row.find('td.js-checkbox-column > .js-compareButton');

            compareLabel.show();
            compareHelperMessage.hide();
            compareButton.hide();
        }
    };

    this.clear = function() {
        thisDomElement.find('.school-search-result-row').remove();
    };

    this.update = function() {

        var onSearchSuccess = function(data) {
            var afterFadeIn = function() {
                jQuery("#spinner").hide();
                //reattach callbacks to dom element events
                this.attachEventHandlers();
                this.updateAllCheckedRows();
            }.gs_bind(this);

            jQuery('#js-school-search-results-table').html(data);
            jQuery('#school-search-results-table-body').css("opacity",.2);
            jQuery('#school-search-results-table-body').animate(
                {opacity: 1},
                250,
                'linear',
                afterFadeIn
            );
        }.gs_bind(this);

        var onSearchError = function() {
            this.clear();
            jQuery("#spinner").hide();
        }.gs_bind(this);

        jQuery('#spinner').show();

        jQuery('#school-search-results-table-body').animate(
            { opacity: .2 },
            250,
            'linear',
            function() {
                searcher.search(onSearchSuccess, onSearchError);
            }
        );
    };

    this.onPageSizeChanged = function() {
        var queryString = window.location.search;
        queryString = putIntoQueryString(queryString,"pageSize",jQuery('#page-size').val(), true);
        queryString = buildQueryString(queryString);
        queryString = removeFromQueryString(queryString, "start");
        window.location.search = queryString;
    }.gs_bind(this);

    this.onSortChanged = function() {
        var i = 0;
        var gradeLevels = [];
        var schoolTypes = [];
        var queryString = window.location.search;

        queryString = buildQueryString(queryString);
        queryString = this.persistCompareCheckboxesToQueryString(queryString);
        queryString = putIntoQueryString(queryString,"sortChanged",true, true);
        queryString = removeFromQueryString(queryString, "start");

        window.location.search = queryString;
    }.gs_bind(this);

    this.page = function(pageNumber, pageSize) {
        var start = (pageNumber-1) * pageSize;

        var queryString = window.location.search;
        queryString = this.persistCompareCheckboxesToQueryString(queryString);
        queryString = putIntoQueryString(queryString,"start",start, true);

        queryString = buildQueryString(queryString);

        window.location.search = queryString;
    }.gs_bind(this);

    this.persistCompareCheckboxesToQueryString = function(queryString) {
        var compareSchoolsList = this.getCheckedSchools().join(',');
        if (compareSchoolsList !== undefined && compareSchoolsList.length > 0) {
            queryString = putIntoQueryString(queryString, "compareSchools", compareSchoolsList, true);
        } else {
            queryString = removeFromQueryString(queryString, "compareSchools");
        }
        return queryString;
    }.gs_bind(this);

    this.getCheckedSchools = function() {
        return checkedSchools;
    };

    this.sendToCompare = function() {
        if (checkedSchools !== undefined && checkedSchools.length > 0) {
            var encodedCurrentUrl = encodeURIComponent(window.location.pathname + buildQueryString(window.location.search));
            window.location ='/school-comparison-tool/results.page?schools=' + checkedSchools.join(',') +
                    '&source=' + encodedCurrentUrl;
        }
    };

    this.updateNumCheckedSchoolsText = function() {
        if (checkedSchools !== undefined && checkedSchools.length >= 2) {
            jQuery('tr.uncheck').show();
            jQuery('.js-how-many-compare-checked-unlinked').hide();
            jQuery('.js-num-checked-send-to-compare').show();
            jQuery('.js-how-many-compare-checked-linked').html(checkedSchools.length);
        } else if (checkedSchools !== undefined && checkedSchools.length == 1) {
            jQuery('tr.uncheck').show();
            jQuery('.js-how-many-compare-checked-unlinked').show();
            jQuery('.js-num-checked-send-to-compare').hide();
        } else {
            jQuery('tr.uncheck').hide();
            jQuery('.js-how-many-compare-checked-unlinked').show();
            jQuery('.js-num-checked-send-to-compare').hide();
        }
    };

    this.attachEventHandlers = function() {
        jQuery('.compare-school-checkbox').click(this.onCompareCheckboxClicked);
        jQuery('#page-size').change(this.onPageSizeChanged);
        jQuery('#sort-by').change(this.onSortChanged);
        jQuery('.js-compareButton').click(this.sendToCompare.gs_bind(this));
        jQuery('.js-num-checked-send-to-compare').click(this.sendToCompare.gs_bind(this));
        jQuery('.js-compare-uncheck-all').click(this.onCompareUncheckAllClicked);
    };

    this.attachEventHandlers();

    this.initializeRowsAndCheckedSchoolsArray();

};

GS.search.FilterTracking = function() {
    var gradeLevel = new Object();
    gradeLevel['p'] = 'PK';
    gradeLevel['e'] = 'elem';
    gradeLevel['m'] = 'middle';
    gradeLevel['h'] = 'high';
    gradeLevel['all'] = 'all';

    this.track = function(cssId) {
        var lastHyphenIndex = cssId.lastIndexOf('-');
        var customLinkName;
        if (lastHyphenIndex > 0) {
            var cssIdPrefix = cssId.substr(0,lastHyphenIndex);
            var filter = cssId.substr(lastHyphenIndex + 1);
            if (cssIdPrefix == 'school-type') {
                customLinkName = 'Search_filter_type_' + filter;
            } else if (cssIdPrefix == 'grade-level') {
                customLinkName = 'Search_filter_grade_' + gradeLevel[filter];
            }

            //TODO: track affiliation filter

            if (customLinkName != undefined) {
                if (s.tl) {
                    s.tl(true, 'o', customLinkName);
                }
            }
        }
    };

    this.trackSelectBox = function(cssId) {
        var $selectBox = jQuery('#' + cssId);
        var value = $selectBox.val();
        var customLinkName;
        if (cssId === 'schoolSizeSelect') {
            customLinkName = 'Search_filter_size_' + value.toLowerCase();
        } else if (cssId === 'studentTeacherRatioSelect') {
            customLinkName = 'Search_filter_stratio_' + value.toLowerCase();
        } else if (cssId === 'distanceSelect') {
            customLinkName = 'Search_filter_distance_' + value.toLowerCase();
        }
        
        if (customLinkName != undefined) {
            if (s.tl) {
                s.tl(true, 'o', customLinkName);
            }
        }
    }
};

jQuery(function() {
    GS.search.schoolSearchResultsTable = new GS.search.SchoolSearchResultsTable();
    GS.search.filterTracking = new GS.search.FilterTracking();

    jQuery('#js-searchFilterBox input').click(function() {
        var cssId = jQuery(this).attr('id');

        // may need to change checkbox checking in jQuery 1.6+
        // http://stackoverflow.com/questions/426258/how-do-i-check-a-checkbox-with-jquery-or-javascript
        if (cssId === 'grade-level-all') {
            if (jQuery('#grade-level-all').is(':checked')) {
                jQuery('#js-searchFilterBox .jq-grade-level').prop('checked',true);
            } else {
                jQuery('#js-searchFilterBox .jq-grade-level').prop('checked', false);
            }
        } else if (cssId === 'school-type-all') {
            if (jQuery('#school-type-all').is(':checked')) {
                jQuery('#js-searchFilterBox .jq-school-type').prop('checked',true);
            } else {
                jQuery('#js-searchFilterBox .jq-school-type').prop('checked', false);
            }
        }
        var numGradeLevels = jQuery('#js-searchFilterBox .jq-grade-level').size();
        var numGradeLevelsChecked = jQuery('#js-searchFilterBox .jq-grade-level:checked').size();
        if (numGradeLevels == numGradeLevelsChecked) {
            jQuery('#grade-level-all').prop('checked',true);
        } else {
            jQuery('#grade-level-all').prop('checked', false);
        }

        var numSchoolTypes = jQuery('#js-searchFilterBox .jq-school-type').size();
        var numSchoolTypesChecked = jQuery('#js-searchFilterBox .jq-school-type:checked').size();
        if (numSchoolTypes == numSchoolTypesChecked) {
            jQuery('#school-type-all').prop('checked',true);
        } else {
            jQuery('#school-type-all').prop('checked', false);
        }

        GS.search.filterTracking.track(cssId);
        GS.search.schoolSearchResultsTable.update();
    });

    jQuery('#js-searchFilterBox select').change(function() {
        var cssId = jQuery(this).attr('id');
        GS.search.filterTracking.trackSelectBox(cssId);
        GS.search.schoolSearchResultsTable.update();
    });


});