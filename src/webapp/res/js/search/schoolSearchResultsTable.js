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

    this.search = function(callback, errorCallback) {
        var i = 0;
        var data = {};
        var gradeLevels = [];
        var schoolTypes = [];
        var affiliations = [];

        data.requestType = "ajax";
        data.decorator="emptyDecorator";
        data.confirm="true";
        
        //to populate an array inside a Spring command, Spring requires data in format gradeLevels[0]=e,gradeLevels[1]=m
        jQuery('#js-gradeLevels :checked').each(function() {
            gradeLevels[i++] = jQuery(this).val();
        });

        i = 0;
        jQuery('#js-schoolTypes :checked').each(function() {
            schoolTypes[i++] = jQuery(this).val();
        });

        i=0;
        jQuery('#js-affiliations :checked').each(function() {
           affiliations[i++] = jQuery(this).val();
        });
        data["gradeLevels"] = gradeLevels;
        data["st"] = schoolTypes;
        data["affiliations"] = affiliations;

        var queryString = window.location.search;
        queryString = removeFromQueryString(queryString, "gradeLevels");
        queryString = removeFromQueryString(queryString, "st");
        queryString = removeFromQueryString(queryString, "affiliations");

        jQuery.ajax({type: "post", url: this.url() + queryString, data:data, success: callback, error: errorCallback});
    };
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
            jQuery('#' +queryStringSchools[i]).attr("checked", true);
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
        var checked = checkbox.attr("checked");
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
            jQuery('#' + id).attr('checked', true);
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
};

jQuery(function() {
    GS.search.schoolSearchResultsTable = new GS.search.SchoolSearchResultsTable();
    GS.search.filterTracking = new GS.search.FilterTracking();

    jQuery('#topicbarGS input').click(function() {
        var cssId = jQuery(this).attr('id');
        GS.search.filterTracking.track(cssId);
        GS.search.schoolSearchResultsTable.update();
    });


});