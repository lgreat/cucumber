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
            // number of checked checkboxes
            var n = jQuery('#school-search-results-table-body input:checked').length;
            // to highlight table row
            var theTD = jQuery(this).parent();
            var theTR = theTD.parent();

            // change GS Rating badge and parent rating stars backgrounds from white to blue
            jQuery('#school-search-results-table-body').find(':checked').each(function () {
                var compareLabel = jQuery(this).parent().parent().find('td.js-checkbox-column > .js-compareLabel');
                var compareHelperMessage = jQuery(this).parent().parent().find('td.js-checkbox-column > .js-compareHelperMessage');
                var compareButton = jQuery(this).parent().parent().find('td.js-checkbox-column > .js-compareButton');
                compareLabel.hide();
                compareHelperMessage.hide();
                compareButton.hide();
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

                if (n == 0) {
                    compareLabel.show();
                    compareHelperMessage.hide();
                    compareButton.hide();
                }

                if (n == 1) {
                    compareLabel.hide();
                    compareHelperMessage.show();
                    compareButton.hide();
                }

                if (n > 1) {
                    compareLabel.hide();
                    compareHelperMessage.hide();
                    compareButton.show();
                }

            });
            // change GS Rating badge and parent rating stars backgrounds from blue to white
            jQuery('#school-search-results-table-body').find(':checkbox').not(':checked').each(function () {
                var compareLabel = jQuery(this).parent().parent().find('td.js-checkbox-column > .js-compareLabel');
                var compareHelperMessage = jQuery(this).parent().parent().find('td.js-checkbox-column > .js-compareHelperMessage');
                var compareButton = jQuery(this).parent().parent().find('td.js-checkbox-column > .js-compareButton');
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

                compareLabel.show();
                compareHelperMessage.hide();
                compareButton.hide();

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

/*
    function countChecked() {
        var n = jQuery('#school-search-results-table-body input:checked').length;
        if (n >= 2) {
            */
/* show "Compare Now" button on checked schools *//*

            jQuery('#compareBtn').attr('disabled', '');
            jQuery('#compareBtn').removeClass('button-1-inactive');
            jQuery('#compareBtn').addClass('button-1');
        } else {
            jQuery('#compareBtn').attr('disabled', 'disabled');
            jQuery('#compareBtn').removeClass('button-1');
            jQuery('#compareBtn').addClass('button-1-inactive');
        }
        if (n == 1) {
            */
/* show "Check 2 or more..." message on checked school *//*

            jQuery('.compareHelperMessage').show();
        } else {
            jQuery('.compareHelperMessage').hide();
        }
        if (n = 0) {
            */
/* show "COMPARE" label on all schools *//*

        }
    }

    countChecked();
    jQuery(":checkbox").click(countChecked);*/
