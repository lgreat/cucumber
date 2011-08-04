GS = GS || {};
GS.search = GS.search || {};

GS.search.onMapMarkerClick = function(state, id) {
    jQuery('.bg-color-f4fafd input:not(:checked)').each(function(item) {
        jQuery(this).parent().parent().removeClass('bg-color-f4fafd');
    });
    jQuery('#nearby-schools-' + state + id).addClass('bg-color-f4fafd');

    if (s.tl) {
        s.tl(true,'o', 'Map_pin_click');
    }
};

    /**
     * Takes a string that resembles a URL querystring in the format ?key=value&amp;key=value&amp;key=value
     * Deprecated. Use GS.Uri
     * @param queryString
     * @param key
     * @param value
     */
    function putIntoQueryString(queryString, key, value, overwrite) {
        queryString = queryString.substring(1);
        var put = false;
        var vars = [];

        if (overwrite === undefined) {
            overwrite = true;
        }

        if (queryString.length > 0) {
            vars = queryString.split("&");
        }

        for (var i = 0; i < vars.length; i++) {
            var pair = vars[i].split("=");
            var thisKey = pair[0];

            if (overwrite === true && thisKey === key) {
                vars[i] = key + "=" + value;
                put = true;
            }
        }

        if (put !== true) {
            vars.push(key + "=" + value);
        }


        queryString = "?" + vars.join("&");
        return queryString;
    }

    /**
     * Returns the value associated with a key in the current url's query string
     * Deprecated. Use GS.Uri
     * @param key
     */
    function getFromQueryString(key) {
        queryString = window.location.search.substring(1);
        var vars = [];
        var result;

        if (queryString.length > 0) {
            vars = queryString.split("&");
        }

        for (var i = 0; i < vars.length; i++) {
            var pair = vars[i].split("=");
            var thisKey = pair[0];

            if (thisKey === key) {
                result = pair[1];
                break;
            }
        }

        return result;
    }

    // Deprecated. Use GS.Uri
    function removeFromQueryString(queryString, key) {
        if (queryString.substring(0,1) === '?') {
            queryString = queryString.substring(1);
        }

        var vars = [];
        if (queryString.length > 0) {
            vars = queryString.split("&");
        }

        for (var i = 0; i < vars.length; i++) {
            var pair = vars[i].split("=");
            var thisKey = pair[0];

            if (thisKey == key) {
                // http://wolfram.kriesing.de/blog/index.php/2008/javascript-remove-element-from-array
                vars.splice(i,1);
                i--;
            }
        }

        queryString = "?" + vars.join("&");
        return queryString;
    }

    function buildQueryString(queryString) {
        //to populate an array inside a Spring command, Spring requires data in format gradeLevels[0]=e,gradeLevels[1]=m
        queryString = removeFromQueryString(queryString, "gradeLevels");
        var checkedGradeLevels = jQuery('#js-gradeLevels :checked');
        var overwriteGradeLevels = true;
        checkedGradeLevels.each(function() {
            if (jQuery(this).val() !== '') {
                queryString = putIntoQueryString(queryString, "gradeLevels", jQuery(this).val(), overwriteGradeLevels);
                overwriteGradeLevels = false;
            }
        });

        queryString = removeFromQueryString(queryString, "st");
        var checkedSchoolTypes = jQuery('#js-schoolTypes :checked');
        var overwriteSchoolTypes = true;
        checkedSchoolTypes.each(function() {
            if (jQuery(this).val() !== '') {
                queryString = putIntoQueryString(queryString, "st", jQuery(this).val(), overwriteSchoolTypes);
                overwriteSchoolTypes = false;
            }
        });

        queryString = removeFromQueryString(queryString, "affiliations");
        var checkedAffiliations = jQuery('#js-affiliations :checked');
        var overwriteAffiliations = true;
        checkedAffiliations.each(function() {
            if (jQuery(this).val() !== '') {
                queryString = putIntoQueryString(queryString, "affiliations", jQuery(this).val(), overwriteAffiliations);
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
            queryString = putIntoQueryString(queryString, "schoolSize", schoolSize, true);
        } else {
            queryString = removeFromQueryString(queryString, "schoolSize");
        }

        var distanceSelect = jQuery('#distanceSelect');
        if (distanceSelect.size() == 1) {
            queryString = putIntoQueryString(queryString, "distance", distanceSelect.val(), true);
        }

        if (jQuery('#sort-by').val() !== '' && typeof(jQuery('#sort-by').val()) !== 'undefined') {
            queryString = putIntoQueryString(queryString,"sortBy",jQuery('#sort-by').val(), true);
        } else {
            queryString = removeFromQueryString(queryString, "sortBy");
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



