if (GS === undefined) {
    var GS = {};
}
if (GS.module === undefined) {
    GS.module = {};
}
if (GS.form === undefined) {
    GS.form = {};
}
GS.form.selectionMadeAutoComplete = false;

GS.parentReviewLandingPage = {} || GS.parentReviewLandingPage;
/*GS.parentReviewLandingPage.attachAutocomplete = function () {
    var searchBox = $('.js-parentReviewLandingPageSearchBox');
    var url = "/search/schoolAutocomplete.page";

    var formatter = function (row) {
        if (row != null && row.length > 0) {
            //var suggestion = row[0];
            // capitalize first letter of all words but the last
            // capitalize the entire last word (state)
            //return suggestion.substr(0, suggestion.length-2).replace(/\w+/g, function(word) { return word.charAt(0).toUpperCase() + word.substr(1); }) + suggestion.substr(suggestion.length-2).toUpperCase();
        }
        return row;
    };

    searchBox.autocomplete2(url, {
        extraParams: {
            state: function () {
                //var rval = searchStateSelect.val();
                // TODO: add state
                var rval = "CA";
                if (rval === '') {
                    return null;
                }
                return rval;
            },
            schoolCity: true
        },
        extraParamsRequired: true,
        minChars: 3,
        selectFirst: false,
        cacheLength: 150,
        matchSubset: true,
        max: 6,
        autoFill: false,
        dataType: "text",
        formatItem: formatter,
        formatResult: formatter
    });
};*/
GS.parentReviewLandingPage.attachAutocomplete = function () {
    var searchBox = $('.js-parentReviewLandingPageSearchBox').find("input");
    var url = "/search/schoolAutocomplete.page";
    var cache = {};
    var terms = [];

    var formatter = function (row) {
        if (row != null && row.length > 0) {
            //var suggestion = row[0];
            // capitalize first letter of all words but the last
            // capitalize the entire last word (state)
            //return suggestion.substr(0, suggestion.length-2).replace(/\w+/g, function(word) { return word.charAt(0).toUpperCase() + word.substr(1); }) + suggestion.substr(suggestion.length-2).toUpperCase();
        }
        return row;
    };

    var cacheNewTerm = function(newTerm, results) {
        // maintain a 15-term cache
        if (terms.push(newTerm) > 15) {
            delete cache[terms.shift()];
        }
        cache[newTerm] = results;
    };

    var updateUIWithSchool = function(school) {
        $("#schoolId").val(school.id);
        $("#schoolState").val(school.state);
        $("#js-bannerSchoolName").html(school.name);
        if(school.address != null && school.address != ""){
            var iconAddressBanner = '<span class="iconx16 i-16-locationOrange mrs"><!-- do not collapse --></span>'+school.address;
            $("#js-bannerSchoolInfo .js-bannerSchoolAddress").html(iconAddressBanner).show();
        }
        var iconPageBanner = false;
        var contentExistsBefore = false;

        if(school.type != null && school.type != ""){
            iconPageBanner = true;
            contentExistsBefore = true;
            $("#js-bannerSchoolInfo .js-bannerSchoolType").html(school.type).show();
        }
        if(school.gradeRange != null && school.gradeRange != ""){
            iconPageBanner = true;
            var gradeContent = school.gradeRange;
            if(contentExistsBefore){
                gradeContent = " / "+school.gradeRange;
            }
            $("#js-bannerSchoolInfo .js-bannerSchoolGradeRange").html(gradeContent).show();
            contentExistsBefore = true;
        }
        if(ui.item.enrollment != null && ui.item.enrollment != "" && ui.item.enrollment != "0"){
            iconPageBanner = true;
            var enrollmentContent = ui.item.enrollment;
            if(contentExistsBefore){
                enrollmentContent = " / "+ui.item.enrollment + " Students";
            }
            $("#js-bannerSchoolInfo .js-bannerSchoolEnrollment").html(enrollmentContent).show();
            contentExistsBefore = true;
        }
        console.log("iconPageBanner:"+iconPageBanner);
        if(iconPageBanner){
            console.log($("#js-bannerSchoolInfo .js-bannerPageIcon"));
            $("#js-bannerSchoolInfo .js-bannerPageIcon").show();
        }
        if(ui.item.levelCode == "h"){
            $("#js-showStudentForHighSchoolOnly").show();
        }
    };

    var mapSchool = function(school) {
        return {
            label: school.name + " - " + school.city,
            id: school.id,
            value: school.name + " - " + school.city,
            address: school.street + " " + school.cityStateZip,
            enrollment: school.enrollment,
            type:  school.type,
            name: school.name,
            levelCode: school.levelCode,
            gradeRange: school.gradeRange,
            state: school.state
        }
    };

    // Caching strategy from http://stackoverflow.com/a/14144009
    searchBox.autocomplete({
        minLength: 3,
        source: function (request, response) {
            var state = function () {
                var rval =  $("#js-reviewLandingState").find(".js-selectBoxText").html();
                if (rval === '') {
                    return null;
                }
                return rval;
            };

            var term = request.term.toLowerCase();
            if (term in cache) {
                if (cache.hasOwnProperty(term)) {
                    response($.map(cache[term], mapSchool));
                }
            } else if (terms.length > 0) {
                var lastTerm = terms[terms.length - 1];
                if (term.substring(0, lastTerm.length) === lastTerm) {
                    var results = [];
                    var cachedResultsForLastTerm = cache[lastTerm];
                    if (cachedResultsForLastTerm !== undefined && cachedResultsForLastTerm.length) {
                        for (var i = 0; i < cachedResultsForLastTerm.length; i++) {
                            var resultItem = cachedResultsForLastTerm[i];
                            if (resultItem.name.toLowerCase().indexOf(term) !== -1) {
                                results.push(resultItem);
                            }
                        }
                    }
                    response($.map(results, mapSchool));
                    return true;
                }
            }
            $.ajax({
                type: 'GET',
                dataType: 'json',
                url: url,
                data: {
                    q: term,
                    state: state,
                    schoolCity: true
                },
                success: function (data) {
                    cacheNewTerm(term, data.schools);
                    response($.map(data.schools, mapSchool));
                }
            });
        },
        position: { my : "left top", at: "left top+40" },
        select: function( event, ui ) {
            $( this ).val( ui.item.label );
            updateUIWithSchool(ui.item);
            GS.form.selectionMadeAutoComplete = true;
        },
        focus: function( event, ui ) {
            if (ui.item !== undefined && ui.item.hasOwnProperty('label')) {
                $( this ).val( ui.item.label );
                updateUIWithSchool(ui.item);
                GS.form.selectionMadeAutoComplete = true;
            }
        }
    });
};



$(document).ready(function() {
//    GS.module.schoolSelect = new GS.module.SchoolSelect();
//
//    GS.module.schoolSelect.registerValidCallback(function() {
//       jQuery('#addParentReviewForm').show();
//    });
//
//    GS.module.schoolSelect.registerInvalidCallback(function() {
//       jQuery('#addParentReviewForm').hide();
//    });

//    jQuery('#addParentReviewForm').hide();
    GS.parentReviewLandingPage.attachAutocomplete();
//    GS.parentReviewLandingPage.attachAutocomplete();

});


/**
 * Created with IntelliJ IDEA.
 * User: mitch
 * Date: 3/6/13
 * Time: 10:07 AM
 * To change this template use File | Settings | File Templates.
 */
GS = GS || {};

$(document).ready(function() {
    $('#js-reviewContent').characterCounter({charLimit:1200});
    $('#js_submitSelectSchool').on("click",function() {

        var trackSchoolChosen = function() {
            // should track when a school is chosen by user (go button is clicked)
            //requires /res/js/omnitureEventNotifier.js
            omnitureEventNotifier.clear();
            omnitureEventNotifier.successEvents = "event77;";
            omnitureEventNotifier.send();
        };
        trackSchoolChosen();
        $('.js-pageOneReviewLandingPage').fadeOut('slow', function() {
            $('.js-pageTwoReviewLandingPage').fadeIn('fast', function() {
                $('.headerBar').show().animate({
                    top: '+=200'
                }, 2000);
            });
        });

    });


    GS_schoolReviewFormLandingPage("parentReviewFormLandingPage");

    GS_spriteCheckBoxes("js-reviewLandingCheckboxTerms", "parentReviewTerms", 1, 0);
    GS_spriteCheckBoxes("js-reviewLandingCheckboxEmail", "sendMeEmailUpdates", 1, 0);

    GS_initializeCustomSelect("js-reviewLandingIAm", GS_selectCallbackReviewsIAm);
    GS_initializeCustomSelect("js-reviewLandingState");

    starRatingInterface("starRatingContainerReview", 16, 5, "overallAsString", "");
    starRatingInterface("starRatingContainerReviewTeacher", 16, 5, "teacherAsString", "");
    starRatingInterface("starRatingContainerReviewPrincipal", 16, 5, "principalAsString", "");
    starRatingInterface("starRatingContainerReviewParent", 16, 5, "parentAsString", "");
    starRatingInterface("starRatingContainerReviewTeacherForStudent", 16, 5, "teacherStudentAsString", "");
});

/********************************************************************************************************
 *
 *  currently created to work with the review page form!!!!
 *
 * @param containerLayer            the id of the layer that contains the check box.
 * @param fieldToSet                hidden form fields id will be submitted
 * @param checkedValue              value to set hidden field to when checked
 * @param uncheckedValue            value to set hidden field to when unchecked
 */
function GS_spriteCheckBoxes(containerLayer, fieldToSet, checkedValue, uncheckedValue){
    var container = $("#"+containerLayer);
    var checkOn  = container.find(".js-checkBoxSpriteOn");
    var checkOff = container.find(".js-checkBoxSpriteOff");
    var checkBoxField =  $("#"+fieldToSet);
    checkOff.on("click", function(){
        checkOff.hide();
        checkOn.show();
        checkBoxField.val(checkedValue);
    });
    checkOn.on("click", function(){
        checkOn.hide();
        checkOff.show();
        checkBoxField.val(uncheckedValue);
    });
}

/********************************************************************************************************
 *
 *  currently created to work with the review page form!!!!
 *
 * @param containerS            the id of the layer that contains the star rating.
 * @param iconW                 icon width needs to have a css component that it is compatible with.
 * @param starsT                total stars currently set to 5 -- also needs to be changed as default value in the jspx or tagx
 * @param overallSR             sets the hidden value of a form field
 * @param divWriteTextValues    show the text value in this div -- the display values are defined in arrStarValuesText
 */

function starRatingInterface(containerS, iconW, starsT, overallSR, divWriteTextValues){
    /* star rating */
    var iconWidth = iconW;
    var totalStars = starsT;
    var iconStr =  "i-"+iconWidth+"-star-";
    var removeClassStr = "";
    var starsOn = $('#'+containerS+' .starsOn');
    var starsOff = $('#'+containerS+' .starsOff');
    var overallStarRating = $("#"+overallSR);
    var arrStarValuesText = ['Click on stars to rate','Unsatisfactory', 'Below average','Average','Above average','Excellent'];
    var arrStarValueDefault = 'Click on stars to rate';

    for(var i=1; i<=totalStars; i++){
        removeClassStr += iconStr+i;
        if(i != totalStars){
            removeClassStr += " ";
        }
    }
    $('#'+containerS).mousemove (function(e){
        var offset = $(this).offset();
        var x = e.pageX - offset.left;
        var currentStar = Math.floor(x/iconWidth) +1;
        if(currentStar > totalStars) currentStar = totalStars;
        starsOn.removeClass(removeClassStr).addClass(iconStr + currentStar);
        starsOff.removeClass(removeClassStr).addClass(iconStr+ (totalStars - currentStar));
        if(divWriteTextValues != ""){
            $("#"+divWriteTextValues).html(arrStarValuesText[currentStar]);
        }
    });
    $('#'+containerS).click (function(e){
        var offset = $(this).offset();
        var x = e.pageX - offset.left;
        var currentStar = Math.floor(x/iconWidth) +1;
        if(currentStar > totalStars) currentStar = totalStars;
        overallStarRating.val(currentStar);
        overallStarRating.blur();
        starsOn.removeClass(removeClassStr).addClass(iconStr + currentStar);
        starsOff.removeClass(removeClassStr).addClass(iconStr+ (totalStars - currentStar));

    });
    $('#'+containerS).mouseleave (function(e){
        var currentRating = overallStarRating.val();
        starsOn.removeClass(removeClassStr).addClass(iconStr + currentRating);
        starsOff.removeClass(removeClassStr).addClass(iconStr+ (totalStars - currentRating));
        if(divWriteTextValues != ""){
            $("#"+divWriteTextValues).html(arrStarValuesText[currentRating]);
        }
    });
}


/**********************************************************************************************
 *
 * @param layerContainer  --- this is the surrounding layer that contains
 .js-selectBox - this is the clickable element to open the drop down
 .js-selectDropDown - this is the dropdown select list container
 .js-ddValues - each element in the select list
 .js-selectBoxText - the text that gets set.  This is the part that should be scrapped for option choice
 * @param callbackFunction - optional function callback when selection is made.
 * @constructor
 */
function GS_initializeCustomSelect(layerContainer, callbackFunction){
    var selectContainer = $("#"+layerContainer); //notify
    var selectBox = selectContainer.find(".js-selectBox");
    var selectDropDownBox = selectContainer.find(".js-selectDropDown");
    var selectDropDownItem = selectContainer.find(".js-ddValues");
    var selectBoxText = selectContainer.find(".js-selectBoxText");

    selectBox.on("click", showSelect);

    selectDropDownBox.on("click", function(event) {
        // Handle the click on the notify div so the document click doesn't close it
        event.stopPropagation();
    });

    function showSelect(event) {
        $(this).off('click');
        selectDropDownBox.show();
        $(document).on("click", hideSelect);
        selectDropDownItem.on("click", showW);
        // So the document doesn't immediately handle this same click event
        event.stopPropagation();
    };

    function hideSelect(event) {
        $(this).off('click');
        selectDropDownItem.off('click');
        selectDropDownBox.hide();
        selectBox.on("click", showSelect);
    }

    function showW(event) {
        hideSelect(event);
        selectBoxText.html($(this).html());
        if(callbackFunction) callbackFunction($(this).html());
    }

    selectDropDownItem.mouseover(function () {
        $(this).addClass("ddValuesHighlight");
    });

    selectDropDownItem.mouseout(function () {
        $(this).removeClass("ddValuesHighlight");
    });
}

/*
 Callback from Custom Select - occurs when choice is made on the Parent / Teacher / Student / Other drop down
 */

function GS_selectCallbackReviewsIAm(selectValue){
    hideAllLayers();
    if(selectValue == "Parent"){
        $('#js-reviewsLandingStarBox-Parent').show();
    }
    if(selectValue == "Student"){
        $('#js-reviewsLandingStarBox-Student').show();
    }
    if(selectValue == "Teacher/Staff member"){
        $('#js-reviewsLandingStarBox-NoAdd').show();
    }
    if(selectValue == "Other"){
        $('#js-reviewsLandingStarBox-NoAdd').show();
    }
    function hideAllLayers(){
        $('#js-reviewsLandingStarBox-Parent').hide();
        $('#js-reviewsLandingStarBox-Start').hide();
        $('#js-reviewsLandingStarBox-NoAdd').hide();
        $('#js-reviewsLandingStarBox-Student').hide();
    }
    $('#selectValueIAm').val(selectValue);
    $('#posterAsString').val(selectValue);
}



function GS_isValidEmailAddress(emailAddress) {
    var pattern = new RegExp(/^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i);
    return pattern.test(emailAddress);
};

function GS_countWords(text) {
    var count = 0;
    var a = text.replace(/\n/g, ' ').replace(/\t/g, ' ');
    var z = 0;
    for (; z < a.length; z++) {
        if (a.charAt(z) == ' ' && a.charAt(z - 1) != ' ') { count++; }
    }
    return count + 1; // # of words is # of spaces + 1
}

function GS_validateReviewText(reviewText) {
    var returnValue = "";
    if (reviewText.length > 1200) {
        var returnValue = 'Please keep your comments under 1200 characters.';
    }
    if (this.countWords(reviewText) < 15) {
        var returnValue = 'Please use at least 15 words in your comment.';
    }
    return returnValue;
}

/**
 * Do not use new operator on this object until after form is rendered
 * @constructor
 * @param {string} id the dom id of the form but should not contain the pound sign.
 */
function GS_schoolReviewFormLandingPage(id) {
    var form = jQuery('#' + id);

    var submitButtonClass = "js-submitParentReview";
    var emailFormClass = "js-email";
    var overallStarRatingsClass = "js-overallAsString";
    var reviewFormClass = "js-reviewContent";
    var termsOfUseClass = "js-parentReviewTerms";
    var posterSelectClass = "js-posterAsString";

    var submitButton = form.find('.' + submitButtonClass);
    var review = form.find('.' + reviewFormClass);
    var overallRating = form.find('.' + overallStarRatingsClass);
    var termsOfUse = form.find('.' + termsOfUseClass);
    var email = form.find('.' + emailFormClass);
    var posterString = form.find('.' + posterSelectClass);

    submitButton.on("click", function(event){
        if(validateForm()){
            postReview(form);
        }
    });

    var trackReviewSubmitted = function() {
        // should track when a school is chosen by user (go button is clicked)
        //requires /res/js/omnitureEventNotifier.js
        omnitureEventNotifier.clear();
        omnitureEventNotifier.successEvents = "event78;";
        omnitureEventNotifier.send();
    };

    function clearErrors(){
        form.find('.' + emailFormClass + '-error').hide();
        form.find('.' + overallStarRatingsClass + '-error').hide();
        form.find('.' + termsOfUseClass + '-error').hide();
        form.find('.' + posterSelectClass + '-error').hide();
        form.find('.' + reviewFormClass + '-error').hide();
    }

    function validateForm(){
        clearErrors();
        var allowPost = true;
        if(!GS_isValidEmailAddress(email)){
            allowPost = false;
            form.find('.' + emailFormClass + '-error').show();
        }
        if(overallRating.val() == "0"){
            allowPost = false;
            form.find('.' + overallStarRatingsClass + '-error').show();
        }
        if(termsOfUse.val() == "0"){
            allowPost = false;
            form.find('.' + termsOfUseClass + '-error').show();
        }
        if(posterString.val() == ""){
            allowPost = false;
            form.find('.' + posterSelectClass + '-error').show();
        }
        var errorReview = GS_validateReviewText(review.val());
        if(errorReview != ""){
            allowPost = false;
            form.find('.' + reviewFormClass + '-error').show();
        }
        return allowPost;
    }

    function postReview(form){
        var url = '/school/review/postReview.page';
        var formData = form.serialize();

        jQuery.ajax({
            type: 'POST',
            url: url,
            data: formData,
            dataType: 'json'
        }).done(function(data) {
            trackReviewSubmitted();
            $('.js-pageTwoReviewLandingPage').fadeOut('slow', function() {
                $('.js-pageThreeReviewLandingPage').fadeIn('fast');
            });
        }).error(function() {
            console.log("error occurred with form post");
        });
    }
}