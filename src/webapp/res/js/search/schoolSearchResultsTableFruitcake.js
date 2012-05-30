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
    GS.search.filters.init();
    GS.search.schoolSearchForm.init(GS.search.filters);
    GS.search.results.init(GS.search.filters, GS.search.compare);

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
        GS.search.results.update();
    });

    jQuery('#js-searchFilterBox select').change(function() {
        var cssId = jQuery(this).attr('id');
        GS.search.filterTracking.trackSelectBox(cssId);
        GS.search.results.update();
    });


});