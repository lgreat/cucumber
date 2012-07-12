//GS = GS || {};
//GS.school = GS.school || {};
//GS.school.profile = GS.school.profile || (function() {
//
//    var $tabGroup = null;
//    var $tabBodyGroup = null;
//    var $allTabBodies = null;
//
//    var init = function(tabToSelect) {
//        $tabGroup = $('[data-gs-tab-group=profileTabs]');
//        $tabBodyGroup = $('[data-gs-tab-body-group=profileTabs]');
//        $allTabBodies = $tabBodyGroup.find('[data-gs-tab-body]').hide();
//
//        if (!tabToSelect || tabToSelect == '' || $tabGroup.find('[data-gs-tab=' + tabToSelect + ']').length == 0) {
//            tabToSelect = 'overview';
//        }
//
//        registerEventHandlers();
//        setupTabs(tabToSelect);
//    };
//
//    var registerEventHandlers = function() {
//
//    };
//
//    var setupTabs = function(tabToSelect) {
//        $tabGroup.on('click', '[data-gs-tab]', function() {
//            var $this = $(this);
//            var tab = $this.data('gs-tab');
//
//            var $tabBody = $tabBodyGroup.find('[data-gs-tab-body=' + tab + ']');
//            $allTabBodies.hide();
//            $tabBody.show();
//            $tabGroup.find('li').removeClass('selected');
//            $this.addClass('selected');
//        });
//        // select default tab. This may change depending on URL parameter (or possibly model variable?)
//        $('[data-gs-tab=' + tabToSelect + ']').addClass('selected');
//        $tabBodyGroup.find('[data-gs-tab-body=' + tabToSelect + ']').show();
//    };
//
//
//    return {
//        init:init
//
//    }
//
//})();
//<a href="/school/profile.page?tab=programs_extracurriculars&amp;state=ca&amp;id=1" onClick="linkToTabs('extracurriculars');">Link to Programs</a>

jQuery(document).ready(function() {
    if ( jQuery.browser.msie ) {   if(jQuery.browser.version <= 7){ jQuery(".arrowdiv").remove() } }

    var linkToTabs = function(destination){
        $("#js_"+destination).triggerHandler('click').stopPropagation();
        return false;
    }
    starRating(16, 5, "overallStarRating");


});

/********************************************************************************************************
 *
 *  currently created to work with the review page form!!!!
 *
 * @param iconW         icon width needs to have a css component that it is compatible with.
 * @param starsT        total stars currently set to 5 -- also needs to be changed as default value in the jspx or tagx
 * @param overallSR     sets the hidden value of a form field
 */

function starRating(iconW, starsT, overallSR){
    /* star rating */
    var iconWidth = iconW;
    var totalStars = starsT;
    var iconStr =  "i-"+iconWidth+"-star-";
    var removeClassStr = "";
    var starsOn = $('#starRatingContainer .starsOn');
    var starsOff = $('#starRatingContainer .starsOff');
    var overallStarRating = $("#"+overallSR);

    for(var i=1; i<=totalStars; i++){
        removeClassStr += iconStr+i;
        if(i != totalStars){
            removeClassStr += " ";
        }
    }
    $('#starRatingContainer').mousemove(function(e){
        var offset = $(this).offset();
        var x = e.pageX - offset.left;
        var currentStar = Math.floor(x/iconWidth) +1;
        if(currentStar > totalStars) currentStar = totalStars;
        starsOn.removeClass(removeClassStr).addClass(iconStr + currentStar);
        starsOff.removeClass(removeClassStr).addClass(iconStr+ (totalStars - currentStar));
    });
    $('#starRatingContainer').click(function(e){
        var offset = $(this).offset();
        var x = e.pageX - offset.left;
        var currentStar = Math.floor(x/iconWidth) +1;
        if(currentStar > totalStars) currentStar = totalStars;
        overallStarRating.val(currentStar);
        starsOn.removeClass(removeClassStr).addClass(iconStr + currentStar);
        starsOff.removeClass(removeClassStr).addClass(iconStr+ (totalStars - currentStar));

    });
    $('#starRatingContainer').mouseout(function(e){
        var currentRating = overallStarRating.val();
        starsOn.removeClass(removeClassStr).addClass(iconStr + currentRating);
        starsOff.removeClass(removeClassStr).addClass(iconStr+ (totalStars - currentRating));
    });
}