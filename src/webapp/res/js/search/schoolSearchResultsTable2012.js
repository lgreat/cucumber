GS = GS || {};
GS.search = GS.search || {};

Array.prototype.contains = function(obj) {
    var i = this.length;
    while (i--) {
        if (this[i] === obj) {
            return true;
        }
    }
    return false;
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
    GS.search.results.init(GS.search.filters, GS.search.compare, GS.tracking.customLinks);

    GS.search.filterTracking = new GS.search.FilterTracking();

    jQuery('#js-searchFilterBox input').click(function() {
        var cssId = jQuery(this).attr('id');
        GS.search.filterTracking.track(cssId);
        GS.search.results.update();
    });

    jQuery('#js-searchFilterBox select').change(function() {
        var cssId = jQuery(this).attr('id');
        GS.search.filterTracking.trackSelectBox(cssId);
        GS.search.results.update();
    });


});