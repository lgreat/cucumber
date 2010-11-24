var compareSchoolsArray = [];

    jQuery(function() {
        jQuery('#nearby-cities-link').mouseover(function() {
           jQuery('#nearby-cities').show();
        });
        jQuery('#nearby-cities-link').mouseout(function() {
           jQuery('#nearby-cities').hide();
        });

        jQuery('#sort-by').change(function() {
            var i = 0;
            var gradeLevels = [];
            var schoolTypes = [];
            var queryString = window.location.search;

            //to populate an array inside a Spring command, Spring requires data in format gradeLevels[0]=e,gradeLevels[1]=m
            jQuery('#js-gradeLevels :checked').each(function() {
                queryString = putIntoQueryString(queryString,"gradeLevels", jQuery(this).val(), false);
            });

            i = 0;
            jQuery('#js-schoolTypes :checked').each(function() {
                queryString = putIntoQueryString(queryString,"schoolTypes", jQuery(this).val(), false);
            });

            queryString = putIntoQueryString(queryString,"sortBy",jQuery('#sort-by').val(), true);
            //queryString = putIntoQueryString(queryString,"gradeLevels", gradeLevels);
            //queryString = putIntoQueryString(queryString,"schoolTypes", schoolTypes);
            window.location.search = queryString;
        });

        jQuery('#page-size').change(function() {
            var queryString = window.location.search;
            queryString = putIntoQueryString(queryString,"pageSize",jQuery('#page-size').val(), true);
            window.location.search = queryString;
        });

        jQuery('.compare-school-checkbox').click(function() {
            // to highlight table row
            var theTD = jQuery(this).parent();
            var theTR = theTD.parent();

            // change GS Rating badge and parent rating stars backgrounds from white to blue
            jQuery('#school-search-results-table-body').find(':checked').each(function () {
                var isWhite = null;
                var pattern2 = /_y|_b/gi;
                var stars = jQuery(this).parent().parent().find('td.stars-column > span');
                var starsClass = stars.attr('class');
                (starsClass.match(pattern2) === null) ? isWhite = true : isWhite = false;
                var badge = jQuery(this).parent().parent().find('td.badge-column > span');
                if (badge.length != 0) {
                    var badgeClass = badge.attr('class');
                    if (isWhite) {
                        var blueBadgeClass = badgeClass + '_b';
                        badge.removeClass(badgeClass).addClass(blueBadgeClass);
                    }
                }

                if (isWhite) {
                    var blueStarsClass = starsClass + '_b';
                    stars.removeClass(starsClass).addClass(blueStarsClass);
                }
            });
            // change GS Rating badge and parent rating stars backgrounds from blue to white
            jQuery('#school-search-results-table-body').find(':checkbox').not(':checked').each(function () {
                var isBlue = null;
                var pattern1 = /_b/gi;
                var pattern3 = /sprite badge_sm_(\d{1,2}|[a-z]{2})/gi;
                var pattern4 = /sprite stars_sm_(\d|[a-z_]{7})/gi;
                var stars = jQuery(this).parent().parent().find('td.stars-column > span');
                var starsClass = stars.attr('class');
                (starsClass.match(pattern1) === null) ? isBlue = false : isBlue = true;
                var whiteStarsClass = starsClass.match(pattern4)[0];
                var badge = jQuery(this).parent().parent().find('td.badge-column > span');
                if (badge.length != 0) {
                    var badgeClass = badge.attr('class');
                    var whiteBadgeClass = badgeClass.match(pattern3)[0];
                    if (isBlue) {
                        badge.removeClass(badgeClass).addClass(whiteBadgeClass);
                    }
                }
                if (isBlue) {
                    stars.removeClass(starsClass).addClass(whiteStarsClass);
                }
            });
            // change TD background color
            if (jQuery(this).attr('checked')) {
                theTR.children().addClass('bg-color-f4fafd');
                compareSchoolsArray.push(jQuery(this).attr('id'));
            } else {
                theTR.children().removeClass('bg-color-f4fafd');
                compareSchoolsArray.pop(jQuery(this).attr('id'));
            }
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
        var compareSchools = compareSchoolsArray.join(',');
        var queryString = window.location.search;
        if (compareSchools !== undefined && compareSchools.length > 0) {
            queryString = putIntoQueryString(window.location.search, "compareSchools", compareSchools, true);
        }
        queryString = putIntoQueryString(queryString,"start",start, true);

        window.location.search = queryString;
    }