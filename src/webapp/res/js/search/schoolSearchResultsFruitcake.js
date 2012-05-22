GS = GS || {};
GS.search = GS.search || {};

    function buildQueryString(queryString) {
        //to populate an array inside a Spring command, Spring requires data in format gradeLevels[0]=e,gradeLevels[1]=m
        queryString = removeFromQueryString(queryString, "gradeLevels");
        var checkedGradeLevels = jQuery('#js-gradeLevels :checked');
        var overwriteGradeLevels = true;
        checkedGradeLevels.each(function() {
            if (jQuery(this).val() !== '') {
                queryString = GS.uri.Uri.putIntoQueryString(queryString, "gradeLevels", jQuery(this).val(), overwriteGradeLevels);
                overwriteGradeLevels = false;
            }
        });

        queryString = removeFromQueryString(queryString, "st");
        var checkedSchoolTypes = jQuery('#js-schoolTypes :checked');
        var overwriteSchoolTypes = true;
        checkedSchoolTypes.each(function() {
            if (jQuery(this).val() !== '') {
                queryString = GS.uri.Uri.putIntoQueryString(queryString, "st", jQuery(this).val(), overwriteSchoolTypes);
                overwriteSchoolTypes = false;
            }
        });

        queryString = removeFromQueryString(queryString, "affiliations");
        var checkedAffiliations = jQuery('#js-affiliations :checked');
        var overwriteAffiliations = true;
        checkedAffiliations.each(function() {
            if (jQuery(this).val() !== '') {
                queryString = GS.uri.Uri.putIntoQueryString(queryString, "affiliations", jQuery(this).val(), overwriteAffiliations);
                overwriteAffiliations = false;
            }
        });

        // GS-11789
        /*
        var studentTeacherRatio = jQuery('#studentTeacherRatioSelect');
        if (studentTeacherRatio.size() == 1) {
            queryString = putIntoQueryString(queryString, "studentTeacherRatio", studentTeacherRatio.val(), true);
        }
        */

        var schoolSize = jQuery("#schoolSizeSelect").val();
        if (schoolSize !== 'All') { // GS-11789
            queryString = GS.uri.Uri.putIntoQueryString(queryString, "schoolSize", schoolSize, true);
        } else {
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "schoolSize");
        }

        var distanceSelect = jQuery('#distanceSelect');
        if (distanceSelect.size() == 1) {
            queryString = GS.uri.Uri.putIntoQueryString(queryString, "distance", distanceSelect.val(), true);
        }

        if (jQuery('#sort-by').val() !== '' && typeof(jQuery('#sort-by').val()) !== 'undefined') {
            queryString = GS.uri.Uri.putIntoQueryString(queryString,"sortBy",jQuery('#sort-by').val(), true);
        } else {
            queryString = GS.uri.Uri.removeFromQueryString(queryString, "sortBy");
        }
        return queryString;
    }




jQuery(function () {
if (jQuery.browser.msie && jQuery.browser.version.substr(0,1)<7) {
jQuery('.js-trigger,.js-popup').mouseover(function(){
    // do something
    jQuery('#sort-by').hide();
    jQuery('#page-size').hide();
}).mouseout(function(){
    // do something else
    jQuery('#sort-by').show();
    jQuery('#page-size').show();
})
}
});



