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
    var getSourceUrlFunc;

    var initializeSchoolsInCompare = function(fromUrl) {
        schoolsInCompare = GS.util.localStorage.getItemFromLocalStorage(compareKeyInLocalStorage);
        compareBtn = $('#js_compareBtn');
        compareModule = $('#js_compareModule');
        getSourceUrlFunc = fromUrl;

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

    var addSchoolToCompare = function(schoolId, state) {
        var dfd = jQuery.Deferred();

        //Validate that the state of the school being added is the same, the school is not already in compare
        //and the max limit of schools in compare is not reached.If these validations do not pass reject the deferred
        //that the calling page can take action..
        //Once all the validations pass add the school to compare
        //and resolve the deferred so that the calling page can take action.
        jQuery.when(
            areSchoolStatesSame(state),
            isSchoolInCompare(schoolId, state),
            isMaxSchoolLimitReached()
        ).done(
            function() {
                addSchool(schoolId, state).done(
                    function() {
                        dfd.resolve();
                    }).fail(function() {
                        dfd.reject();
                    });
            }
        ).fail(
            function() {
                dfd.reject();
            }
        );

        return dfd.promise();
    };

    var addSchool = function(schoolId, state) {
        var dfd = jQuery.Deferred();

        //Get the details of the school by making an ajax call.
        getSchoolsInfo([
            {"schoolId":schoolId,"state":state}
        ]).done(
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

                    //Decide whether to show or hide the compare module.
                    showHideCompareModule();
                    dfd.resolve();
                } else {
                    dfd.reject();
                }
            }
        ).fail(
            function() {
                dfd.reject();
            }
        );
        return dfd.promise();
    }

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
                }
                if (data.schools === undefined || data.schools === null || data.schools.length === 0) {
                    dfd.reject();
                }

                dfd.resolve(data.schools);
            }
        ).fail(function() {
                dfd.reject();
            }
        );
        return dfd.promise();
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

            var encodedCurrentUrl = encodeURIComponent(getSourceUrlFunc());
            window.location = '/school-comparison-tool/results.page?schools=' + schoolsInCompareArr.join(',') +
                '&source=' + encodedCurrentUrl;
        }
        return false;
    };

    var isSchoolInCompare = function(schoolId, state) {
        var dfd = jQuery.Deferred();
        var isSchoolAlreadyPresent = false;
        for (var i = 0; i < schoolsInCompare.length; i++) {
            if (schoolsInCompare[i].schoolId == schoolId && schoolsInCompare[i].state == state) {
                isSchoolAlreadyPresent = true;
                break;
            }
        }
        if (isSchoolAlreadyPresent) {
            dfd.reject();
        } else {
            dfd.resolve();
        }
        return dfd.promise();
    };

    var isMaxSchoolLimitReached = function() {
        var dfd = jQuery.Deferred();
        if (schoolsInCompare.length == maxSchoolsInCompare) {
            var encodedCurrentUrl = encodeURIComponent(getSourceUrlFunc());

            var schoolsArr = [];
            for (var i = 0; i < schoolsInCompare.length; i++) {
                schoolsArr[i] = schoolsInCompare[i].state + schoolsInCompare[i].schoolId;
            }

            GSType.hover.compareSchoolsLimitReached.show(schoolsArr.join(','), encodedCurrentUrl, clearSchoolsInCompare);

            dfd.reject()
        } else {
            dfd.resolve();
        }
        return dfd.promise();
    }

    var areSchoolStatesSame = function(state) {
        var dfd = jQuery.Deferred();
        if (schoolsInCompare.length >= 1 && state != schoolsInCompare[0].state) {
            compareDifferentStatesWarningHover.showHover().done(
                function() {
                    clearSchoolsInCompare();
                    dfd.resolve();
                }).fail(function() {
                    dfd.reject();
                });
        } else {
            dfd.resolve();
        }
        return dfd.promise();
    };

    var clearSchoolsInCompare = function() {
        schoolsInCompare = [];
        GS.util.localStorage.removeItemFromLocalStorage(compareKeyInLocalStorage);
        $('#js_compareSchoolsDiv').children().remove();
        showHideCompareModule();
    };

    //Hover to warn the users when they try to compare schools in 2 states.
    var CompareDifferentStatesWarningHover = function() {
        this.dfd;
        this.loadDialog = function() {
        };

        this.showHover = function() {
            this.dfd = jQuery.Deferred();
            jQuery('#js_compareDifferentStatesWarningHover').on('dialogclose.compare', this.onClose.gs_bind(this));
            compareDifferentStatesWarningHover.show();
            return compareDifferentStatesWarningHover.dfd.promise();
        };

        //When the user picks a school in different state to compare.
        this.onSubmitCompare = function() {
            jQuery('#js_compareDifferentStatesWarningHover').off('dialogclose.compare');
            compareDifferentStatesWarningHover.hide();
            compareDifferentStatesWarningHover.dfd.resolve();
        };

        this.onClose = function() {
            compareDifferentStatesWarningHover.dfd.reject();
        };
    };

    CompareDifferentStatesWarningHover.prototype = new GSType.hover.HoverDialog("js_compareDifferentStatesWarningHover", 640);
    var compareDifferentStatesWarningHover = new CompareDifferentStatesWarningHover();

    //Draws the div for a given school in the compare module.
    var drawSchoolDivInCompareModule = function(schoolId, state, schoolName, schoolType, gradeRange, city, schoolUrl) {
        $('<div id=js_compare_' + schoolId + '_' + state + '><a href="' + schoolUrl + '"> ' + schoolName + '</a><br/> ' + schoolType + ' ' + gradeRange + ' ' + city + ' ' + state +
            ' <a href="#" class="js_removeSchoolFromCompare noInterstitial" id="js_compareRemove_' + schoolId + '_' + state + '">Remove</a></div>').appendTo('#js_compareSchoolsDiv');

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

    var getSchoolsInCompare = function() {
        return schoolsInCompare;
    };

    $(function() {

        compareDifferentStatesWarningHover.loadDialog();

        //Bind the click handler to remove a school from compare.
        $('#js_compareSchoolsDiv').on('click', '.js_removeSchoolFromCompare', function() {
            var schoolSelected = $(this).attr('id');
            var schoolAndState = schoolSelected.substr('js_compareRemove_'.length, schoolSelected.length);
            var schoolId = schoolAndState.substr(0, schoolAndState.indexOf('_'));
            var state = schoolAndState.substr(schoolAndState.indexOf('_') + 1, schoolAndState.length);
            removeSchoolFromCompare(schoolId, state);

            //Trigger a custom event so that the caller can decide what to do once the school is removed.
            $('body').trigger('schoolRemoved', [schoolId,state]);
            return false;
        });

        //Bind the click handler to compare schools.
        $('#js_compareBtn').on('click', compareSchools);

        //Bind the click handler to handle :- user picks a school in a different state to compare.
        jQuery('#js_compareDifferentStatePicked').on('click', compareDifferentStatesWarningHover.onSubmitCompare);

    });

    return {
        addSchoolToCompare:addSchoolToCompare,
        removeSchoolFromCompare:removeSchoolFromCompare,
        compareSchools:compareSchools,
        initializeSchoolsInCompare:initializeSchoolsInCompare,
        getSchoolsInCompare :getSchoolsInCompare
    }
})();

$(function() {
    //TODO move to a new file
    //Initialize the schools to compare.
    GS.school.compare.initializeSchoolsInCompare(function() {
        return window.location.pathname + GS.search.filters.getUpdatedQueryString();
    });

    //Bind the click handler to checkbox
    $('.compare-school-checkbox').on('click', function() {
        var schoolCheckbox = $(this);
        var schoolSelected = schoolCheckbox.attr('id');
        var schoolId = schoolSelected.substr(2, schoolSelected.length);
        var state = schoolSelected.substr(0, 2);
        var checked = schoolCheckbox.is(':checked');
        if (checked === true) {
            //If the checkbox was checked then try adding a school to the compare module.
            //If adding a school fails then un check the checkbox.
            GS.school.compare.addSchoolToCompare(schoolId, state).done(
                function() {
                    //If there are more than 2 schools in compare then the 'compare' label
                    //should be switched to 'compare now' link.
                    var schoolsInCompare = GS.school.compare.getSchoolsInCompare();
                    if (schoolsInCompare.length >= 2) {
                        for (var i = 0; i < schoolsInCompare.length; i++) {
                            var schoolId = schoolsInCompare[i].schoolId;
                            var state = schoolsInCompare[i].state;
                            writeCompareNowLink(schoolId, state);
                        }
                    }
                }).fail(
                function() {
                    schoolCheckbox.removeAttr('checked');
                }
            );
        } else {
            GS.school.compare.removeSchoolFromCompare(schoolId, state);
            //After the school is removed, the 'compare now' link should be switched to 'compare' label.
            //Also if there are less than 2 schools remaining in the compare module after this school has been deleted
            //then switch all the the 'compare now' links to 'compare' label.
            removeCompareNowLinks(schoolId, state);
        }
    });

    //Bind the custom event that gets triggered after the schools are initialized in the compare module.
    //This is used to check the check boxes for the schools that are in the compare module.
    $('body').on('schoolsInitialized', function(event, schoolsInCompare) {
        for (var i = 0; i < schoolsInCompare.length; i++) {
            var schoolId = schoolsInCompare[i].schoolId;
            var state = schoolsInCompare[i].state;

            var schoolCheckBox = $('#' + state + schoolId);
            schoolCheckBox.prop("checked", true);

            //If there are more than 2 schools in compare then the 'compare' label
            //should be switched to 'compare now' link.
            if (schoolsInCompare.length >= 2) {
                writeCompareNowLink(schoolId, state);
            }
        }
    });

    //Bind the custom event that gets triggered after a school is removed from the compare module.
    //This is used to un check the check boxes for the school that is removed from the compare module.
    $('body').on('schoolRemoved', function(event, schoolId, state) {
        var checkBox = $('#' + state + schoolId);
        checkBox.removeAttr('checked');
        //After the school is removed, the 'compare now' link should be switched to 'compare' label.
        //Also if there are less than 2 schools remaining in the compare module after this school has been deleted
        //then switch all the the 'compare now' links to 'compare' label.
        removeCompareNowLinks(schoolId, state);
    });

    $('body').on('click', '.js_compare_link', function() {
        GS.school.compare.compareSchools();
    });

    var writeCompareNowLink = function(schoolId, state) {
        //The 'compare' label should be switched to 'compare now' link.
        var compareLabel = $('#js_' + state + schoolId + '_compare_label');
        compareLabel.html('<a href="#" class="js_compare_link noInterstitial">Compare Now</a>');
    };

    var removeCompareNowLinks = function(schoolId, state) {
        //The 'compare now' link should be switched to 'compare' label for the school that has been deleted.
        removeCompareNowLink(schoolId, state);

        //If there are less than 2 schools remaining in the compare module after the school has been deleted
        //then switch all the the 'compare now' links to 'compare' label.
        var schoolsInCompare = GS.school.compare.getSchoolsInCompare();
        if (schoolsInCompare.length < 2) {
            for (var i = 0; i < schoolsInCompare.length; i++) {
                var schoolIdentifier = schoolsInCompare[i].schoolId;
                var stateStr = schoolsInCompare[i].state;
                removeCompareNowLink(schoolIdentifier, stateStr);
            }
        }
    };

    var removeCompareNowLink = function(schoolId, state) {
        //The 'compare now' link should be switched to 'compare' label .
        var compareLabel = $('#js_' + state + schoolId + '_compare_label');
        compareLabel.html('Compare');
    };

});
