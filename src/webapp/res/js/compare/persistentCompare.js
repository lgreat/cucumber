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
        var item = '';
        if (enabled) {
            item = localStorage.getItem(namespacePrefix + key);
            if (item != undefined && item != null && item.length > 0 && (item.charAt(0) === '{' || item.charAt(0) === '[')) {
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
        var item = '';
        if (enabled) {
            item = localStorage.getItem(namespacePrefix + key);
        }
        return item != '';
    };

    return {
        putItemInLocalStorage:putItemInLocalStorage,
        getItemFromLocalStorage:getItemFromLocalStorage,
        removeItemFromLocalStorage:removeItemFromLocalStorage,
        hasItemInLocalStorage:hasItemInLocalStorage
    }
})();


GS.util.storeSchools = (function() {
    var subCookiePropertyName = 'compareSchools';
    var cookieExpiryDays = 30;

    //First tries to store schools in local storage.If that fails, then uses cookies as a fallback.
    //The cookies are expired after 30 days.
    var putSchools = function(key, value) {
        if (!GS.util.localStorage.putItemInLocalStorage(key, value)) {
            if (typeof value === "object") {
                value = JSON.stringify(value);
            }
            subCookie.setObjectProperty(key, subCookiePropertyName, value, cookieExpiryDays);
        }
    };

    //First tries to get the schools from local storage.If that fails, then reads from cookies as a fallback.
    var getSchools = function(key) {
        var item = GS.util.localStorage.getItemFromLocalStorage(key);
        if (item === '') {
            item = subCookie.getObjectProperty(key, subCookiePropertyName);
            if (item != undefined && item != null && item.length > 0 && (item.charAt(0) === '{' || item.charAt(0) === '[')) {
                item = JSON.parse(item);
            } else if (item === undefined || item === null) {
                item = '';
            }
        }
        return item;
    };

    //First tries to remove the schools from local storage.If that fails, expires the cookie.
    var removeSchools = function(key) {
        if (!GS.util.localStorage.removeItemFromLocalStorage(key)) {
            subCookie.setObject(key);
        }
    };

    return {
        putSchools:putSchools,
        getSchools:getSchools,
        removeSchools:removeSchools
    }
})();

// Invokes arguments synchronously, in order, with short-circuit behavior
// Returns a promise that resolves iff all arguments resolve, and fails as soon as the first argument
// to fail fails, without proceeding to invoke any following arguments
GS.util.synchronousWhen = (function() {
    // return a function that, when invoked, executes actionFunc and chains the resulting promise to a deferred
    var getFunctionLinkingActionToDeferred = function (actionFunc, deferred) {
        return function () {
            actionFunc()
                .done(deferred.resolve.gs_bind(deferred))
                .fail(deferred.reject.gs_bind(deferred));
        };
    };

    // Chains previousAction to nextAction and returns a deferred that is linked to nextAction
    // If previousActionDeferred is rejected, then reject the masterDeferred -- interrupting the chain
    // If previousActionDeferred is resolved, invoke nextAction
    var failFastChain = function(masterDeferred, previousActionDeferred, nextAction) {
        var nextActionDeferred = new jQuery.Deferred()
            .fail(masterDeferred.reject.gs_bind(masterDeferred));
        previousActionDeferred.done(getFunctionLinkingActionToDeferred(nextAction, nextActionDeferred));
        return nextActionDeferred;
    };

    return function() {
        var masterDeferred = new jQuery.Deferred();
        var deferredArray = new Array();

        var actionInQueue = new jQuery.Deferred();
        actionInQueue.resolve(); // start the chain
        for (var x = 0; x < arguments.length; x++) {
            actionInQueue = failFastChain(masterDeferred, actionInQueue, arguments[x]);
            deferredArray.push(actionInQueue);
        }
        jQuery.when.apply(this, deferredArray).done(function() {
            masterDeferred.resolve();
        });
        return masterDeferred.promise();
    };
})();

GS.school = GS.school || {};
GS.school.compare = (function() {
    var MODULE_ID = 'js_compareModule';
    var maxSchoolsInCompare = 8;
    var compareKey = "schoolsToCompare";
    var schoolsInCompare;
    var compareBtn;
    var compareModule;
    var getSourceUrlFunc;
    var compareAddBtn;
    var contextSchoolId;
    var contextSchoolState;

    var initializeSchoolsInCompare = function(fromUrl) {
        schoolsInCompare = GS.util.storeSchools.getSchools(compareKey);
        compareBtn = $('#js_compareBtn');
        compareModule = $('#' + MODULE_ID);
        compareAddBtn = compareModule.has('#js_compareAddBtn').length != 0 ? $(compareModule.find('#js_compareAddBtn')[0]) : undefined;
        getSourceUrlFunc = fromUrl;

        // if the js_compareaddbtn is available, then get the context information
        // about the school from that button
        if (compareAddBtn!=undefined && compareAddBtn.data('id') !== undefined && compareAddBtn.data('state') !== undefined){
            contextSchoolId = compareAddBtn.data('id').toString();
            contextSchoolState = compareAddBtn.data('state');
        }

        //If there are schools in local storage/cookies then get the details of the schools by making an ajax call.
        if (schoolsInCompare != null && schoolsInCompare != undefined && schoolsInCompare.length > 0) {
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

                    triggerUpdatePageWithSchoolsEvent();
                }
            ).fail(
                function() {
                    //TODO what?
                }
            )
        } else {
            // triggering event so we can clear the checkboxes if there is no schools in compare
            schoolsInCompare = [];
            triggerUpdatePageWithSchoolsEvent();
        }

        //Decide whether to show or hide the compare module.
        showHideCompareModule();
    };

    var addSchoolToCompare = function(schoolId, state) {
        var dfd = jQuery.Deferred();

        //Validate that the state of the school being added is the same, the school is not already in compare
        //and the max limit of schools in compare is not reached.If these validations do not pass reject the deferred
        //that the calling page can take action..
        //Once all the validations pass add the school to compare
        //and resolve the deferred so that the calling page can take action.
        var f_areSchoolStatesSame = function() {return areSchoolStatesSame(state);};
        var f_isSchoolInCompare = function() {return isSchoolInCompare(schoolId, state);};
        // if any validation fails, don't bother with the rest
        GS.util.synchronousWhen(
            f_areSchoolStatesSame,
            f_isSchoolInCompare,
            isMaxSchoolLimitReached
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

                    //Add the school to local storage/cookies.
                    GS.util.storeSchools.putSchools(compareKey, schoolsInCompare);
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
        //Remove the school from the local storage/cookies array.
        for (var i = 0; i < schoolsInCompare.length; i++) {
            if (schoolsInCompare[i].schoolId == schoolId && schoolsInCompare[i].state == state) {
                schoolsInCompare.splice(i, 1);

                // add back the "Add this school to compare" button if removing the context school
                if (contextSchoolId!=null && contextSchoolId!=undefined && contextSchoolState!=null && contextSchoolState!=undefined){
                    if (schoolId==contextSchoolId && state == contextSchoolState){
                        $('#js_compareAddBtn').removeClass('dn').show();
                    }
                }
                break;
            }
        }

        //If there are no more schools present remove the entire compare key from local storage/cookies.
        //Else write the new array of schools into the local storage/cookies.
        if (schoolsInCompare.length == 0) {
            GS.util.storeSchools.removeSchools(compareKey);
        } else {
            GS.util.storeSchools.putSchools(compareKey, schoolsInCompare);
        }

        //Remove the div from the compare module.
        var schoolDiv = $('#js_compareSchoolsDiv').children('#js_compare_' + schoolId + '_' + state);
        schoolDiv.remove();

        //Add omniture tracking when user clicks the remove button in compare module.
        if (s.tl) {
            s.tl(true, 'o', 'Compare_Module_Remove_Item');
        }

        //Decide whether to show or hide the compare module.
        showHideCompareModule();
    };

    var compareSchools = function() {
        //If there are at least 2 schools in compare , then take the user to the compare tool.
        if (schoolsInCompare.length >= 2) {

            //Add omniture tracking to the compare feature.
            if (s.tl) {
                s.tl(this, 'o', 'Compare_Module_CompareNow');
            }
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

    var validateSchoolInCompare = function (schoolId, state) {
        var isSchoolAlreadyPresent = false;
        for (var i = 0; i < schoolsInCompare.length; i++) {
            if (schoolsInCompare[i].schoolId == schoolId && schoolsInCompare[i].state == state) {
                isSchoolAlreadyPresent = true;
                break;
            }
        }
        return isSchoolAlreadyPresent;
    };

    var isSchoolInCompare = function(schoolId, state) {
        var dfd = jQuery.Deferred();

        if (validateSchoolInCompare(schoolId, state)) {
            dfd.reject();
        } else {
            dfd.resolve();
        }
        return dfd.promise();
    };

    var validateMaxSchoolLimitReached = function () {
        return (schoolsInCompare.length == maxSchoolsInCompare);
    }

    var isMaxSchoolLimitReached = function() {
        var dfd = jQuery.Deferred();
        if (validateMaxSchoolLimitReached()) {
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
    };

    var validateSchoolStatesSame = function(state) {
        return (schoolsInCompare.length == 0 || (schoolsInCompare.length >= 1 && state == schoolsInCompare[0].state));
    };

    var areSchoolStatesSame = function(state) {
        if (!validateSchoolStatesSame(state)) {
            compareDifferentStatesWarningHover.show();
        }
//        var dfd = jQuery.Deferred();
//        if (!validateSchoolStatesSame(state)) {
//            compareDifferentStatesWarningHover.showHover().done(
//                function() {
//                    clearSchoolsInCompare();
//                    dfd.resolve();
//                }).fail(function() {
//                    dfd.reject();
//                });
//        } else {
//            dfd.resolve();
//        }
//        return dfd.promise();
    };

    var clearSchoolsInCompare = function() {
        for (var i = 0; i < schoolsInCompare.length; i++) {
            // make sure to let the page know the schools are being removed
            triggerSchoolRemovedEvent(schoolsInCompare[i].schoolId, schoolsInCompare[i].state);
        }
        schoolsInCompare = [];
        GS.util.storeSchools.removeSchools(compareKey);
        $('#js_compareSchoolsDiv').children().remove();
        showHideCompareModule();
    };

    //Hover to warn the users when they try to compare schools in 2 states.
//    var CompareDifferentStatesWarningHover = function() {
//        this.dfd = null;
//        this.loadDialog = function() {
//        };
//
//        this.showHover = function() {
//            this.dfd = jQuery.Deferred();
//            jQuery('#js_compareDifferentStatesWarningHover').on('dialogclose.compare', this.onClose.gs_bind(this));
//            compareDifferentStatesWarningHover.show();
//            return compareDifferentStatesWarningHover.dfd.promise();
//        };
//
//        //When the user picks a school in different state to compare.
//        this.onSubmitCompare = function() {
//            jQuery('#js_compareDifferentStatesWarningHover').off('dialogclose.compare');
//            compareDifferentStatesWarningHover.hide();
//            compareDifferentStatesWarningHover.dfd.resolve();
//        };
//
//        this.onClose = function() {
//            compareDifferentStatesWarningHover.dfd.reject();
//        };
//    };



    CompareDifferentStatesWarningHover.prototype = new GSType.hover.HoverDialog("js_compareDifferentStatesWarningHover");
    var compareDifferentStatesWarningHover = new CompareDifferentStatesWarningHover();

    //Draws the div for a given school in the compare module.
    var drawSchoolDivInCompareModule = function(schoolId, state, schoolName, schoolType, gradeRange, city, schoolUrl) {
        $('<div id=js_compare_' + schoolId + '_' + state + '><div class="pam"><div class="fl" style="width: 90%"><a href="' + schoolUrl + '"> ' + schoolName
            + '</a></div>' +
            '<div class="fr"><a href="#" class="js_removeSchoolFromCompare noInterstitial iconx16 i-16-close" title="Remove" id="js_compareRemove_' + schoolId + '_' + state + '"></a></div>' +
            '<div class="clearfloat"></div>' +
            '<div class="small bottom">' + city + ' ' + state +
            '</div>' +
            '</div>' +
            '<hr class="keyline2"/>').appendTo('#js_compareSchoolsDiv');
    };

    var showHideCompareModule = function() {
        //Display text in the compare module, based on how many schools have been added to the compare module.
        if (schoolsInCompare.length === 1) {
            $('#js_wantToCompareSchools').hide();
            $('#js_selectOneMoreSchool').show();
        } else if (schoolsInCompare.length < 1) {
            $('#js_selectOneMoreSchool').hide();
            $('#js_wantToCompareSchools').show();
        } else if (schoolsInCompare.length > 1) {
            $('#js_selectOneMoreSchool').hide();
            $('#js_wantToCompareSchools').hide();
        }
        //Decide whether to show or hide the compare button.
        showHideCompareButton();
        showHideCompareWithContextSchool();
    };

    var showHideCompareButton = function() {
        //If there are at least 2 schools in the compare array then display the compare button.
        if (schoolsInCompare.length >= 2) {
            compareBtn.show();
        } else {
            compareBtn.hide();
        }
    };

    var showHideCompareWithContextSchool = function () {
        if (contextSchoolId && contextSchoolState) {
            if (schoolsInCompare.length > 0){
                compareModule.removeClass('dn');
                $('.js_compareNoSchools').addClass('dn');
            } else {
                compareModule.addClass('dn');
                $('.js_compareNoSchools').removeClass('dn');
            }
            showHideAddButton();
        }
    }

    var showHideAddButton = function () {
        if (contextSchoolId && contextSchoolState){
            if (validateSchoolStatesSame(contextSchoolState) &&
                !validateMaxSchoolLimitReached() &&
                !validateSchoolInCompare(contextSchoolId, contextSchoolState)){
                compareAddBtn.removeClass('dn');
            } else {
                compareAddBtn.addClass('dn');
            }
        }
    };

    var getSchoolsInCompare = function() {
        return schoolsInCompare;
    };

    var triggerSchoolRemovedEvent = function(schoolId, state) {
        //Trigger a custom event so that the caller can decide what to do once the school is removed.
        compareModule.trigger('schoolRemoved', [schoolId,state]);
    };

    //Trigger a custom event so that the caller can decide what to do once the schools have been initialized.
    var triggerUpdatePageWithSchoolsEvent = function() {
        compareModule.trigger('schoolsInitialized', [schoolsInCompare]);

    };

    $(function() {

        //compareDifferentStatesWarningHover.loadDialog();

        //Bind the click handler to remove a school from compare.
        $('#js_compareSchoolsDiv').on('click', '.js_removeSchoolFromCompare', function() {
            var schoolSelected = $(this).attr('id');
            var schoolAndState = schoolSelected.substr('js_compareRemove_'.length, schoolSelected.length);
            var schoolId = schoolAndState.substr(0, schoolAndState.indexOf('_'));
            var state = schoolAndState.substr(schoolAndState.indexOf('_') + 1, schoolAndState.length);
            removeSchoolFromCompare(schoolId, state);
            triggerSchoolRemovedEvent(schoolId, state);
            return false;
        });

        //Bind the click handler when this is for specific school
        $('#js_compareAddBtn').on('click', function(){
            if (contextSchoolId != null && contextSchoolId != undefined ){
                if (contextSchoolState!=null && contextSchoolState!=undefined){
                    var compareAddBtn = $(this);
                    addSchoolToCompare(contextSchoolId, contextSchoolState).done(
                        function() {
                            compareAddBtn.addClass('dn').hide();
                        }
                    );
                }
            }
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
        getSchoolsInCompare :getSchoolsInCompare,
        triggerUpdatePageWithSchoolsEvent:triggerUpdatePageWithSchoolsEvent
    }
})();
