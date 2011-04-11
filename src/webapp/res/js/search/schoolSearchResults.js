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

    function removeFromQueryString(queryString, key) {
        queryString = queryString.substring(1);
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
        var numGradeLevels = jQuery('#js-gradeLevels input[type=checkbox]').size();
        var overwriteGradeLevels = true;
        checkedGradeLevels.each(function() {
            queryString = putIntoQueryString(queryString, "gradeLevels", jQuery(this).val(), overwriteGradeLevels);
            overwriteGradeLevels = false;
        });


        queryString = removeFromQueryString(queryString, "st");
        var checkedSchoolTypes = jQuery('#js-schoolTypes :checked');
        var numSchoolTypes = jQuery('#js-schoolTypes input[type=checkbox]').size();
        var overwriteSchoolTypes = true;
        checkedSchoolTypes.each(function() {
            queryString = putIntoQueryString(queryString, "st", jQuery(this).val(), overwriteSchoolTypes);
            overwriteSchoolTypes = false;
        });

        var checkedAffiliations = jQuery('#js-affiliations :checked');
        var overwriteAffiliations = true;
        checkedAffiliations.each(function() {
            queryString = putIntoQueryString(queryString, "affiliations", jQuery(this).val(), overwriteAffiliations);
            overwriteAffiliations = false;
        });

        var studentTeacherRatio = jQuery('#studentTeacherRatioSelect').val();
        queryString = putIntoQueryString(queryString, "studentTeacherRatio", studentTeacherRatio, true);

        var schoolSize = jQuery("#schoolSizeSelect").val();
        queryString = putIntoQueryString(queryString, "schoolSize", schoolSize, true);

        var distance = jQuery("#distanceSelect").val();
        queryString = putIntoQueryString(queryString, "distance", distance, true);

        if (jQuery('#sort-by').val() !== '') {
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



