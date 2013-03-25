/**
 * Created with IntelliJ IDEA.
 * User: mitch
 * Date: 3/6/13
 * Time: 10:07 AM
 * To change this template use File | Settings | File Templates.
 */


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
    var selectShow = $(".js-selectDropDown"); //notify
    selectShow.hide();
    selectShow.click(function(event) {
        // Handle the click on the notify div so the document click doesn't close it
        event.stopPropagation();
    });

    var selectLink = $(".js-selectBox");
    selectLink.click(showSelect);

    function showSelect(event) {
        $(this).unbind('click', showSelect);

        selectShow.show();

        $(document).click(hideSelect);
        $(".js-ddValues").click(showW);

        // So the document doesn't immediately handle this same click event
        event.stopPropagation();
    };

    function hideSelect(event) {
        $(this).unbind('click', hideSelect);

        selectShow.hide();

        selectLink.click(showSelect);
    }

    function showW(event) {
        hideSelect(event);
        var divValue = $(this).html();
        selectLink.html(divValue);
    }
    $(".js-ddValues").mouseover(function () {
        $(this).addClass("ddValuesHighlight");
    });
    $(".js-ddValues").mouseout(function () {
        $(this).removeClass("ddValuesHighlight");
    });
    $(".js-ddValues").click(function () {
        var $this = $(this);
        var selectedValue = $this.html();
        var displayDropdownValue = $(".selectBox").html(selectedValue);

        $(".js-selectDropDown").toggle();
    });

    starRatingInterface("starRatingContainerReview", 16, 5, "overallAsString", "");
    starRatingInterface("starRatingContainerReviewTeacher", 16, 5, "teacherAsString", "");
    starRatingInterface("starRatingContainerReviewPrincipal", 16, 5, "principalAsString", "");
    starRatingInterface("starRatingContainerReviewParent", 16, 5, "parentAsString", "");
});

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