

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

    function page(pageNumber, pageSize) {
        var start = (pageNumber-1) * pageSize;
        var compareSchools = GS.search.schoolSearchResultsTable.getCheckedSchools().join(',');
        var queryString = window.location.search;
        if (compareSchools !== undefined && compareSchools.length > 0) {
            queryString = putIntoQueryString(queryString, "compareSchools", compareSchools, true);
        }
        queryString = putIntoQueryString(queryString,"start",start, true);

        //to populate an array inside a Spring command, Spring requires data in format gradeLevels[0]=e,gradeLevels[1]=m
        queryString = removeFromQueryString(queryString, "gradeLevels");
        var checkedGradeLevels = jQuery('#js-gradeLevels :checked');
        var numGradeLevels = jQuery('#js-gradeLevels input[type=checkbox]').size();
        if (checkedGradeLevels.size() < numGradeLevels) {
            var overwriteGradeLevels = true;
            checkedGradeLevels.each(function() {
                queryString = putIntoQueryString(queryString, "gradeLevels", jQuery(this).val(), overwriteGradeLevels);
                overwriteGradeLevels = false;
            });
        }

        queryString = removeFromQueryString(queryString, "schoolTypes");
        var checkedSchoolTypes = jQuery('#js-schoolTypes :checked');
        var numSchoolTypes = jQuery('#js-schoolTypes input[type=checkbox]').size();
        if (checkedSchoolTypes.size() < numSchoolTypes) {
            var overwriteSchoolTypes = true;
            checkedSchoolTypes.each(function() {
                queryString = putIntoQueryString(queryString, "schoolTypes", jQuery(this).val(), overwriteSchoolTypes);
                overwriteSchoolTypes = false;
            });
        }

        if (jQuery('#sort-by').val() !== '') {
            queryString = putIntoQueryString(queryString,"sortBy",jQuery('#sort-by').val(), true);
        } else {
            queryString = removeFromQueryString(queryString, "sortBy");
        }

        window.location.search = queryString;
    }

