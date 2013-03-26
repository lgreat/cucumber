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

        $('.js-pageOneReviewLandingPage').fadeOut('slow', function() {
            $('.js-pageTwoReviewLandingPage').fadeIn('fast', function() {
                $('.headerBar').show().animate({
                    top: '+=200'
                }, 2000);
            });
        });

    });
//    parentReviewTerms
    GS_spriteCheckBoxes("js-reviewLandingCheckboxTerms", "parentReviewTerms", 1, 0);
    GS_spriteCheckBoxes("js-reviewLandingCheckboxEmail", "sendMeEmailUpdates", 1, 0);

    GS_initializeCustomSelect("js-reviewLandingIAm", GS_selectCallbackReviewsIAm);
    GS_initializeCustomSelect("js-reviewLandingState");

    starRatingInterface("starRatingContainerReview", 16, 5, "overallAsString", "");
    starRatingInterface("starRatingContainerReviewTeacher", 16, 5, "teacherAsString", "");
    starRatingInterface("starRatingContainerReviewPrincipal", 16, 5, "principalAsString", "");
    starRatingInterface("starRatingContainerReviewParent", 16, 5, "parentAsString", "");
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

    selectBox.click(showSelect);

    selectDropDownBox.click(function(event) {
        // Handle the click on the notify div so the document click doesn't close it
        event.stopPropagation();
    });

    function showSelect(event) {
        $(this).unbind('click', showSelect);
        selectDropDownBox.show();
        $(document).click(hideSelect);
        selectDropDownItem.click(showW);
        // So the document doesn't immediately handle this same click event
        event.stopPropagation();
    };

    function hideSelect(event) {
        $(this).unbind('click', hideSelect);
        selectDropDownItem.unbind('click', showW);
        selectDropDownBox.hide();
        selectBox.click(showSelect);
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
        $('#js-reviewsLandingStarBox-NoAdd').show();
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
    }
}