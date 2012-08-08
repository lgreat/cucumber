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

var GS = GS || {};
GS.util = GS.util || {};
GS.profile = GS.profile || {};

GS.util.jumpToAnchor = function(hash) {
    window.location.hash=hash;
    return false;
};

GS.profile.linkToTabs = function(destination){
    $("#js_"+destination).triggerHandler('click');
    return false;
};

GS.profile.linkToTabAndAnchor = function(destinationTab, hash) {
    try {
        GS.profile.linkToTabs(destinationTab);
        GS.util.jumpToAnchor(hash);
    } catch (e) {
        // on error, fall back on default click handling
        return true;
    }
    return false;
};

jQuery(document).ready(function() {

    /* Set up event handlers */
    jQuery('#js_profileHeadWriteReviewLink').on('click', function() {
        return GS.profile.linkToTabAndAnchor('reviews', 'schoolReviewSubmitForm');
    });

    /* End set up event handlers */

    if ( jQuery.browser.msie ) {   if(jQuery.browser.version <= 7){ jQuery(".arrowdiv").remove() } }

    /* this initializes all of the star rating options on the reviews page */
    starRatingInterface("starRatingContainer1", 16, 5, "overallAsString", true);
    starRatingInterface("starRatingContainer2", 16, 5, "teacherAsString", false);
    starRatingInterface("starRatingContainer3", 16, 5, "principalAsString", false);
    starRatingInterface("starRatingContainer4", 16, 5, "parentAsString", false);

    var $parents = $('#showParents')
        , $students = $('#showStudents')
        , $teachers = $('#showTeachers')
        , $sort = $('#sltRatingSort');

    var ratings = function () {
        var csv = new Array();
        if ($parents && $parents.prop('checked')) csv.push('p');
        if ($students && $students.prop('checked')) csv.push('s');
        if ($teachers && $teachers.prop('checked')) csv.push('t');
        if ($sort.val() && $sort.attr('action')){
            var address = $sort.attr('action').split('?'), params = (address.length>1) ? address[1]:'';
            params = GS.uri.Uri.removeFromQueryString(params, 'page');
            params = GS.uri.Uri.putIntoQueryString(params, 'sortBy', $sort.val());
            if (csv.length>0){
                params = GS.uri.Uri.putIntoQueryString(params, 'reviewsBy', csv.join(','));
            } else {
                params = GS.uri.Uri.removeFromQueryString(params, 'reviewsBy');
            }
            location.href = address[0] + params + '#revPagination';
        }
    }

    // sorting for reviews page
    if ($sort) $sort.on('change', ratings);
    if ($parents) $parents.on('click', ratings);
    if ($students) $students.on('click', ratings);
    if ($teachers) $teachers.on('click', ratings);
//

    $('[data-gs-tab] a').on('click', function() {
        var $this = $(this);
        var $tabName = $this.parent().data('gs-tab');
        GS.tracking.sendOmnitureData($tabName);
    });
});

/********************************************************************************************************
 *
 *  currently created to work with the review page form!!!!
 *
 * @param containerS    the id of the layer that contains the star rating.
 * @param iconW         icon width needs to have a css component that it is compatible with.
 * @param starsT        total stars currently set to 5 -- also needs to be changed as default value in the jspx or tagx
 * @param overallSR     sets the hidden value of a form field
 */

function starRatingInterface(containerS, iconW, starsT, overallSR, topStarSelector){
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
    $('#'+containerS).mousemove(function(e){
        var offset = $(this).offset();
        var x = e.pageX - offset.left;
        var currentStar = Math.floor(x/iconWidth) +1;
        if(currentStar > totalStars) currentStar = totalStars;
        starsOn.removeClass(removeClassStr).addClass(iconStr + currentStar);
        starsOff.removeClass(removeClassStr).addClass(iconStr+ (totalStars - currentStar));
        if(topStarSelector){
            $(reviewTopStarDescriptor).html(arrStarValuesText[currentStar]);
        }
    });
    $('#'+containerS).click(function(e){
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
        if(topStarSelector){
            $(reviewTopStarDescriptor).html(arrStarValuesText[currentRating]);
        }
    });
}
function drawPieChart(dataIn, divNameId, dimensions) {

    // Create and populate the data table.
    var data = google.visualization.arrayToDataTable(dataIn, true);

    var options = {
        width: dimensions,
        height: dimensions,
        legend: 'none',
        tooltip: {showColorCode: true,text:'value',textStyle:{color: '#2b2b2b', fontName: 'Arial', fontSize: '10'}},
        colors:['#327FA0','#E2B66C','#DB7258','#A4B41E','#38A37A','#B66483','#7B498F','#414F7B'],
        pieSliceText: 'none',
        chartArea:{left:10,top:10,bottom:10,right:10,width:"88%",height:"88%"},
        pieSliceBorderColor:'white'

    }

    // Create and draw the visualization.
    new google.visualization.PieChart(document.getElementById(divNameId)).draw(data, options);
}

function drawBarChart(dataIn, divNameId, c, w, h) {
    var data = google.visualization.arrayToDataTable(dataIn, true);

    var options = {
        colors: c,
        hAxis: {minValue: 0, maxValue: 10, gridlines:{count:11}},
        legend: 'none',
        width:w,
        height:h
    };

    var chart = new google.visualization.BarChart(document.getElementById(divNameId));
    chart.draw(data, options);
}