var GS = GS || {};
GS.search = GS.search || {};
GS.search.compare = GS.search.compare || (function() {
    var checkedSchools = [];
    var maxCheckedSchools = 8;

    var init = function() {

    };

    var getCheckedSchools = function() {
        return checkedSchools;
    };

    var getMaxCheckedSchools = function() {
        return maxCheckedSchools;
    };

    /**
     * This method is needed because when clicking browse back button, checkboxes stay checked but saved array is gone.
     */
    var findCheckedSchools = function() {
        var checkedSchools = [];
        jQuery('#school-search-results-table-body tbody input:checked').each(function() {
            checkedSchools.push(jQuery(this).attr("id"));
        });
        return checkedSchools;
    };

    /**
     * Get array of state+schoolIds that are listed in query string
     */
    var getQueryStringSchools = function() {
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
    var initializeRowsAndCheckedSchoolsArray = function() {
        var queryStringSchools = getQueryStringSchools();
        var numberOfQueryStringSchools = queryStringSchools.length;

        //start with all the checkboxes checked on this page
        checkedSchools = findCheckedSchools();

        //add to array all of checkboxes which are checked, according to what's in the query string
        for (var i = 0; i < numberOfQueryStringSchools; i++) {
            jQuery('#' +queryStringSchools[i]).prop("checked", true);
            var index = checkedSchools.indexOf(queryStringSchools[i]);
            if (index === -1) {
                checkedSchools.push(queryStringSchools[i]);
            }
        }

        updateAllCheckedRows();
    };

    var onCompareUncheckAllClicked = function() {
        var queryString = window.location.search;
        queryString = removeFromQueryString(queryString, "compareSchools");
        window.location.search = queryString;
    };

    var onCompareCheckboxClicked = function(item) {
        var checkbox = jQuery(item.currentTarget);
        var checked = checkbox.prop("checked");
        var rowId = checkbox.parent().parent().attr("id");
        var row = jQuery('#' + rowId);
        var statePlusSchoolId = row.find('input.compare-school-checkbox').attr('id');

        if (checked) {
            if (checkedSchools.length >= maxCheckedSchools) {
                var encodedCurrentUrl = encodeURIComponent(window.location.pathname + getUpdatedQueryString());
                GSType.hover.compareSchoolsLimitReached.show(checkedSchools.join(','), encodedCurrentUrl, onCompareUncheckAllClicked);
                return false;
            }
            checkedSchools.push(statePlusSchoolId);
            selectRow(checkbox.parent().parent().attr("id"));
        } else {
            var index = checkedSchools.indexOf(statePlusSchoolId);
            if (index !== -1) {
                checkedSchools.splice(index,1);
            }
            deselectRow(checkbox.parent().parent().attr("id"));
        }

        updateAllCheckedRows();
    };

    var updateNumCheckedSchoolsText = function() {
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

    var updateAllCheckedRows = function() {
        var count = checkedSchools.length;

        for (var i = 0; i < count; i++) {
            var id = checkedSchools[i];
            jQuery('#' + id).prop('checked', true);
            selectRow(jQuery('#'+id).parent().parent().attr("id"));
        }
        updateNumCheckedSchoolsText();
    };

    var updateCompareButton = function(rowDomId) {
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

    var selectRow = function(rowDomId) {
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
            updateCompareButton(rowDomId);
        }
    };

    var deselectRow = function(rowDomId) {
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

    return {
        init:init,
        getCheckedSchools:getCheckedSchools,
        getMaxCheckedSchools:getMaxCheckedSchools,
        findCheckedSchools:findCheckedSchools,
        getQueryStringSchools:getQueryStringSchools,
        initializeRowsAndCheckedSchoolsArray:initializeRowsAndCheckedSchoolsArray,
        onCompareUncheckAllClicked:onCompareUncheckAllClicked,
        onCompareCheckboxClicked:onCompareCheckboxClicked,
        updateAllCheckedRows:updateAllCheckedRows,
        updateNumCheckedSchoolsText:updateNumCheckedSchoolsText,
        selectRow:selectRow,
        deselectRow:deselectRow
    }

})();