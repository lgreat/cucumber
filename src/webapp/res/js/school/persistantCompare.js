var GS = GS || {};
GS.util = GS.util || {};
GS.util.localStorage = (function() {

    var enabled = !!window.localStorage;
    var namespaceKey = 'GS';

    var putItemInLocalStorage = function(key, value) {
        var rval = false;
        if (enabled) {
            try {
                if (typeof value === "object") {
                    value = JSON.stringify(value);
                }
                localStorage.setItem(namespaceKey + key, value);

                rval = true;
            }
            catch(e) {
            }
        }
        return rval;
    };

    var getItemFromLocalStorage = function(key) {
        var item = "";
        if (enabled) {
            item = localStorage.getItem(namespaceKey + key);
            if (item != null && item.length > 0 && (item[0] === '{' || item[0] === '[')) {
                item = JSON.parse(item);
            }
        }
        return item;
    };

    var removeItemFromLocalStorage = function(key) {
        var rval = false;
        if (enabled) {
            localStorage.removeItem(namespaceKey + key);
            rval = true;
        }
        return rval;
    };

    var hasItemInLocalStorage = function(key) {
        var item = "";
        if (enabled) {
            item = localStorage.getItem(namespaceKey + key);
        }
        return item != "";
    };

    return {
        putItemInLocalStorage:putItemInLocalStorage,
        getItemFromLocalStorage:getItemFromLocalStorage,
        removeItemFromLocalStorage:removeItemFromLocalStorage,
        hasItemInLocalStorage:hasItemInLocalStorage
    }
})();

GS.school = GS.school || {};
GS.school.compare = (function() {

    var maxSchoolsInCompare = 8;
    var compareKeyInLocalStorage = "schoolsToCompare";
    var schoolsInCompare;

    var addSchoolToCompare = function(schoolId, state) {
        var schoolAlreadyPresent = isSchoolInCompare(schoolId, state);
        if (!schoolAlreadyPresent) {
            if (schoolsInCompare.length === maxSchoolsInCompare) {
                return false;
            } else {

                jQuery.when(
                    getSchoolDetailsInCompare([
                        {"schoolId":schoolId,"state":state}
                    ])).done(
                    function() {

                        var newSchool = {};
                        newSchool['schoolId'] = schoolId;
                        newSchool['state'] = state;
                        schoolsInCompare.push(newSchool);

                        GS.util.localStorage.putItemInLocalStorage(compareKeyInLocalStorage, schoolsInCompare);
                        showHideCompareButton();
                    }
                ).fail(
                    function() {
                        //TODO what?
                    }
                )
            }
        }
        return true;
    };

    var removeSchoolFromCompare = function(schoolId, state) {
        var schoolDiv = $('#js_compareDiv').children('#js_compare_' + schoolId + '_' + state);
        schoolDiv.remove();

        for (var i = 0; i < schoolsInCompare.length; i++) {
            if (schoolsInCompare[i].schoolId == schoolId && schoolsInCompare[i].state == state) {
                schoolsInCompare.splice(i, 1);
                break;
            }
        }

        if (schoolsInCompare.length == 0) {
            GS.util.localStorage.removeItemFromLocalStorage(compareKeyInLocalStorage);
        } else {
            GS.util.localStorage.putItemInLocalStorage(compareKeyInLocalStorage, schoolsInCompare);
        }
        showHideCompareButton();
    };

    var compareSchools = function() {
        if (schoolsInCompare.length >= 2) {
            var schoolsInCompareArr = [];
            for (var i = 0; i < schoolsInCompare.length; i++) {
                schoolsInCompareArr[i] = schoolsInCompare[i].state + schoolsInCompare[i].schoolId;
            }
            // TODO source is needed
//            var encodedCurrentUrl = encodeURIComponent(window.location.pathname + filtersModule.getUpdatedQueryString());
//            window.location ='/school-comparison-tool/results.page?schools=' + checkedSchools.join(',') +
//                    '&source=' + encodedCurrentUrl;
//
            var encodedCurrentUrl = encodeURIComponent(window.location.pathname);
            window.location = '/school-comparison-tool/results.page?schools=' + schoolsInCompareArr.join(',');
        }
        return false;
    };

    var isSchoolInCompare = function(schoolId, state) {
        var schoolAlreadyPresent = false;
        for (var i = 0; i < schoolsInCompare.length; i++) {
            if (schoolsInCompare[i].schoolId == schoolId && schoolsInCompare[i].state == state) {
                schoolAlreadyPresent = true;
                break;
            }
        }
        return schoolAlreadyPresent;
    };

    var initializeAllSchoolsInCompare = function() {
        schoolsInCompare = GS.util.localStorage.getItemFromLocalStorage(compareKeyInLocalStorage);
        if (schoolsInCompare != null) {
            getSchoolDetailsInCompare(schoolsInCompare);
            for (var i = 0; i < schoolsInCompare.length; i++) {
                var schoolId = schoolsInCompare[i].schoolId;
                var state = schoolsInCompare[i].state;

                var schoolCheckBox = $('#' + state + schoolId);
                schoolCheckBox.prop("checked", true);
            }
        } else {
            schoolsInCompare = [];
        }
        showHideCompareButton();
    };

    var drawSchoolDivInCompareModule = function(schoolId, state, schoolName, schoolType, gradeRange, city, schoolUrl) {
        $('<div id=js_compare_' + schoolId + '_' + state + '>' + schoolName + ' ' + schoolType + ' ' + gradeRange + ' ' + city + ' ' + state +
            ' <a href="#" class="js_removeSchoolFromCompare noInterstitial" id="js_compareRemove_' + schoolId + '_' + state + '">Remove</a><\/div>').appendTo('#js_compareDiv');

    };

    var getSchoolDetailsInCompare = function(schoolsIdsAndStates) {
        var dfd = jQuery.Deferred();
        $.ajax({
            type: 'POST',
            url: '/school/schoolDetails.page',
            dataType: 'json',
            data: {schoolIdsAndStates: JSON.stringify({ schools : schoolsIdsAndStates}),
                responseFormat:'json'}
        }).done(
            function(data) {

                if (data == null || data.JsonError === true) {
                    dfd.reject();
                    return;
                }
                if (data.schools == null || data.schools.length == 0) {
                    dfd.reject();
                    return;
                }

                var schools = data.schools;

                for (var i = 0; i < schools.length; i++) {
                    if (schools[i].gradeRange !== 'PK') {
                        drawSchoolDivInCompareModule(schools[i].schoolId, schools[i].state, schools[i].name, schools[i].type,
                            schools[i].gradeRange, schools[i].city, schools[i].schoolUrl);
                    }
                }
                dfd.resolve();
            }
        ).fail(function() {
                dfd.reject();
            }
        );
        return dfd.promise();
    };

    var showHideCompareButton = function() {
        var compareBtn = $('#js_compareBtn');
        if (schoolsInCompare.length >= 2) {
            compareBtn.show();
        } else {
            compareBtn.hide();
        }
    };

    return {
        addSchoolToCompare:addSchoolToCompare,
        removeSchoolFromCompare:removeSchoolFromCompare,
        compareSchools:compareSchools,
        initializeAllSchoolsInCompare:initializeAllSchoolsInCompare
    }
})();

$(function() {

    GS.school.compare.initializeAllSchoolsInCompare();
    $('#js_compareDiv').on('click', '.js_removeSchoolFromCompare', function() {
        var schoolSelected = $(this).attr('id');
        var schoolAndState = schoolSelected.substr('js_compareRemove_'.length, schoolSelected.length);
        var schoolId = schoolAndState.substr(0, schoolAndState.indexOf('_'));
        var state = schoolAndState.substr(schoolAndState.indexOf('_') + 1, schoolAndState.length);
        var checkBox = $('#' + state + schoolId);
        checkBox.removeAttr('checked');
        GS.school.compare.removeSchoolFromCompare(schoolId, state);
        return false;
    });

    $('#js_compareBtn').on('click', function() {
        GS.school.compare.compareSchools();
    });

    //TODO move to a new file
    $('.compare-school-checkbox').click(function() {
        var schoolSelected = $(this).attr('id');
        var schoolId = schoolSelected.substr(2, schoolSelected.length);
        var state = schoolSelected.substr(0, 2);
        var checked = $(this).is(':checked');
        if (checked === true) {
            GS.school.compare.addSchoolToCompare(schoolId, state);
        } else {
            GS.school.compare.removeSchoolFromCompare(schoolId, state);
        }
    });


});