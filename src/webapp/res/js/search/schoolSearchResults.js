var compareSchoolsArray = [];

    jQuery(function() {

        jQuery('#sort-by').change(function() {
            var i = 0;
            var gradeLevels = [];
            var schoolTypes = [];
            var queryString = window.location.search;

            //to populate an array inside a Spring command, Spring requires data in format gradeLevels[0]=e,gradeLevels[1]=m
            var overwriteGradeLevels = true;
            jQuery('#js-gradeLevels :checked').each(function() {
                queryString = putIntoQueryString(queryString,"gradeLevels", jQuery(this).val(), overwriteGradeLevels);
                overwriteGradeLevels = false;
            });

            var overwriteSchoolTypes = true;
            i = 0;
            jQuery('#js-schoolTypes :checked').each(function() {
                queryString = putIntoQueryString(queryString,"schoolTypes", jQuery(this).val(), overwriteSchoolTypes);
                overwriteSchoolTypes = false;
            });

            if (jQuery('#sort-by').val() !== '') {
                queryString = putIntoQueryString(queryString,"sortBy",jQuery('#sort-by').val(), true);
            } else {
                queryString = removeFromQueryString(queryString, "sortBy");
            }
            
            window.location.search = queryString;
        });

        jQuery('#page-size').change(function() {
            var queryString = window.location.search;
            queryString = putIntoQueryString(queryString,"pageSize",jQuery('#page-size').val(), true);
            window.location.search = queryString;
        });
        
        jQuery('.js-compareButton').click(function() {
            var list = compareSchoolsArray.join(',');
            window.location = '/compareSchools.page?compare.x=true&amp;sc=' + list;
        });

    });

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
        var put = false;
        var vars = [];

        if (queryString.length > 0) {
            vars = queryString.split("&");
        }

        for (var i = 0; i < vars.length; i++) {
            var pair = vars[i].split("=");
            var thisKey = pair[0];

            if (thisKey === key) {
                delete vars[i];
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
            queryString = putIntoQueryString(window.location.search, "compareSchools", compareSchools, true);
        }
        queryString = putIntoQueryString(queryString,"start",start, true);

        //to populate an array inside a Spring command, Spring requires data in format gradeLevels[0]=e,gradeLevels[1]=m
        var overwriteGradeLevels = true;
        jQuery('#js-gradeLevels :checked').each(function() {
            queryString = putIntoQueryString(queryString,"gradeLevels", jQuery(this).val(), overwriteGradeLevels);
            overwriteGradeLevels = false;
        });

        var overwriteSchoolTypes = true;
        i = 0;
        jQuery('#js-schoolTypes :checked').each(function() {
            queryString = putIntoQueryString(queryString,"schoolTypes", jQuery(this).val(), overwriteSchoolTypes);
            overwriteSchoolTypes = false;
        });

        if (jQuery('#sort-by').val() !== '') {
            queryString = putIntoQueryString(queryString,"sortBy",jQuery('#sort-by').val(), true);
        } else {
            queryString = removeFromQueryString(queryString, "sortBy");
        }

        window.location.search = queryString;
    }

