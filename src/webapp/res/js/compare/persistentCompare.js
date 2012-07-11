var GS = GS || {};
GS.util = GS.util || {};
GS.util.localStorage = (function() {

    //enabled var checks if local storage is supported by the browser.
    var enabled = !!window.localStorage;
    var namespacePrefix = 'GS';

    var putItemInLocalStorage = function(key, value) {
        var rval = false;
        if (enabled) {
            try {
                if (typeof value === "object") {
                    value = JSON.stringify(value);
                }
                localStorage.setItem(namespacePrefix + key, value);
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
            item = localStorage.getItem(namespacePrefix + key);
            if (item != undefined && item != null && item.length > 0 && (item[0] === '{' || item[0] === '[')) {
                item = JSON.parse(item);
            }
        }
        return item;
    };

    var removeItemFromLocalStorage = function(key) {
        var rval = false;
        if (enabled) {
            localStorage.removeItem(namespacePrefix + key);
            rval = true;
        }
        return rval;
    };

    var hasItemInLocalStorage = function(key) {
        var item = "";
        if (enabled) {
            item = localStorage.getItem(namespacePrefix + key);
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
    var compareBtn;
    var compareModule;

    var addSchoolToCompare = function(schoolId, state) {
        var rval = true;
        var schoolAlreadyPresent = isSchoolInCompare(schoolId, state);

        if (!schoolAlreadyPresent) {
            if (schoolsInCompare.length === maxSchoolsInCompare) {
                rval = false;
            } else {
                //Get the details of the school by making an ajax call.
                jQuery.when(
                    getSchoolsInfo([
                        {"schoolId":schoolId,"state":state}
                    ])).done(
                    function(schools) {

                        //Once the ajax call completes successfully, check that only one school is being added
                        //and its not a preschool.
                        if (schools.length === 1 && schools[0].gradeRange !== 'PK') {
                            var newSchool = {};
                            newSchool['schoolId'] = schools[0].schoolId;
                            newSchool['state'] = schools[0].state;
                            schoolsInCompare.push(newSchool);

                            //Add the school to local storage.
                            GS.util.localStorage.putItemInLocalStorage(compareKeyInLocalStorage, schoolsInCompare);

                            //Draw the div in the compare module.
                            drawSchoolDivInCompareModule(schools[0].schoolId, schools[0].state, schools[0].name, schools[0].type,
                                schools[0].gradeRange, schools[0].city, schools[0].schoolUrl);
                        }

                        //Decide whether to show or hide the compare module.
                        showHideCompareModule();
                    }
                ).fail(
                    function() {
                       rval = false;
                    }
                );
            }
        }
        return rval;
    };

    var removeSchoolFromCompare = function(schoolId, state) {
        //Remove the school from the local storage array.
        for (var i = 0; i < schoolsInCompare.length; i++) {
            if (schoolsInCompare[i].schoolId == schoolId && schoolsInCompare[i].state == state) {
                schoolsInCompare.splice(i, 1);
                break;
            }
        }

        //If there are no more schools present remove the entire compare key from local storage.
        //Else write the new array of schools into the local storage.
        if (schoolsInCompare.length == 0) {
            GS.util.localStorage.removeItemFromLocalStorage(compareKeyInLocalStorage);
        } else {
            GS.util.localStorage.putItemInLocalStorage(compareKeyInLocalStorage, schoolsInCompare);
        }

        //Remove the div from the compare module.
        var schoolDiv = $('#js_compareSchoolsDiv').children('#js_compare_' + schoolId + '_' + state);
        schoolDiv.remove();

        //Decide whether to show or hide the compare module.
        showHideCompareModule();
    };

    var compareSchools = function() {
        //If there are at least 2 schools in compare , then take the user to the compare tool.
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

    var initializeSchoolsInCompare = function() {
        schoolsInCompare = GS.util.localStorage.getItemFromLocalStorage(compareKeyInLocalStorage);
        compareBtn = $('#js_compareBtn');
        compareModule = $('#js_compareModule');

        //If there are schools in local storage then get the details of the schools by making an ajax call.
        if (schoolsInCompare != null && schoolsInCompare != undefined) {
            jQuery.when(
                getSchoolsInfo(schoolsInCompare)).done(
                function(schools) {

                    //Once the ajax call completes successfully, if a school is not a preschool then draw the div in compare module.
                    for (var i = 0; i < schools.length; i++) {
                        if (schools[i].gradeRange !== 'PK') {
                            drawSchoolDivInCompareModule(schools[i].schoolId, schools[i].state, schools[i].name, schools[i].type,
                                schools[i].gradeRange, schools[i].city, schools[i].schoolUrl);
                        }
                    }

                    //Trigger a custom event so that the caller can decide what to do once the schools have been initialized.
                    $('body').trigger('schoolsInitialized', [schools]);

                    //Decide whether to show or hide the compare module.
                    showHideCompareModule();
                }
            ).fail(
                function() {
                    //TODO what?
                }
            )
        } else {
            schoolsInCompare = [];
        }
    };

    var drawSchoolDivInCompareModule = function(schoolId, state, schoolName, schoolType, gradeRange, city, schoolUrl) {
        $('<div id=js_compare_' + schoolId + '_' + state + '><a href="'+schoolUrl+'"> ' + schoolName + '</a><br/> ' + schoolType + ' ' + gradeRange + ' ' + city + ' ' + state +
            ' <a href="#" class="js_removeSchoolFromCompare noInterstitial" id="js_compareRemove_' + schoolId + '_' + state + '">Remove</a></div>').appendTo('#js_compareSchoolsDiv');

    };

    //Makes an ajax call to get the details of the schools.
    var getSchoolsInfo = function(schoolsIdsAndStates) {
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
                if (data.schools === undefined || data.schools === null || data.schools.length === 0) {
                    dfd.reject();
                    return;
                }

                dfd.resolve(data.schools);
            }
        ).fail(function() {
                dfd.reject();
            }
        );
        return dfd.promise();
    };

    var showHideCompareModule = function() {
        //If there are at least 1 school in the compare array then display the compare module.
        if (schoolsInCompare.length >= 1) {
            compareModule.show();
            //Decide whether to show or hide the compare button.
            showHideCompareButton();
        } else {
            compareModule.hide();
        }
    };

    var showHideCompareButton = function() {
        //If there are at least 2 schools in the compare array then display the compare button.
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
        initializeSchoolsInCompare:initializeSchoolsInCompare
    }
})();

$(function() {
    //Initialize the schools to compare.
    GS.school.compare.initializeSchoolsInCompare();

    //Bind the click handler to remove a school from compare.
    $('#js_compareSchoolsDiv').on('click', '.js_removeSchoolFromCompare', function() {
        var schoolSelected = $(this).attr('id');
        var schoolAndState = schoolSelected.substr('js_compareRemove_'.length, schoolSelected.length);
        var schoolId = schoolAndState.substr(0, schoolAndState.indexOf('_'));
        var state = schoolAndState.substr(schoolAndState.indexOf('_') + 1, schoolAndState.length);
        GS.school.compare.removeSchoolFromCompare(schoolId, state);

        //Trigger a custom event so that the caller can decide what to do once the school is removed.
        $('body').trigger('schoolRemoved', [schoolId,state]);
        return false;
    });

    //Bind the click handler to compare schools.
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

    $('body').on('schoolsInitialized', function(event, schoolsInCompare) {
        for (var i = 0; i < schoolsInCompare.length; i++) {
            var schoolId = schoolsInCompare[i].schoolId;
            var state = schoolsInCompare[i].state;

            var schoolCheckBox = $('#' + state + schoolId);
            schoolCheckBox.prop("checked", true);
        }
    });

    $('body').on('schoolRemoved', function(event, schoolId, state) {
        var checkBox = $('#' + state + schoolId);
        checkBox.removeAttr('checked');
    });

});