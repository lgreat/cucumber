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
GS.profile = GS.profile || (function() {
    "use strict";

    var tabs = {};
    tabs.overview = {
        name: "overview",
        selector: "#js_overview"
    };
    tabs.reviews = {
        name: "reviews",
        selector: "#js_reviews"
    };
    tabs.testsAndRatings = {
        name: "testsAndRatings",
        selector: "#js_test-scores"
    };
    tabs['test-scores'] = {
        name: "test-scores",
        selector: "#js_tests",
        parent: tabs.testsAndRatings
    };
    tabs.ratings = {
        name: "ratings",
        selector: "#js_ratings",
        parent: tabs.testsAndRatings
    };
    tabs.demographics = {
        name: "demographics",
        selector: "#js_demographics"
    };
    tabs.students = {
        name: "students",
        selector: "#js_students",
        parent: tabs.demographics
    };
    tabs.teachers = {
        name: "teachers",
        selector: "#js_teachers",
        parent: tabs.demographics
    };
    tabs['programs-culture-master'] = {
        name: "programs-culture-master",
        selector: "#js_programs-culture"
    };
    tabs.enrollment = {
        name: "enrollment",
        selector: "#js_enrollment",
        parent: tabs['programs-culture-master']
    };
    tabs['programs-culture'] = {
        name: "programs-culture",
        selector: "#js_highlights",
        parent: tabs['programs-culture-master']
    };
    tabs['programs-resources'] = {
        name: "programs-resources",
        selector: "#js_programsresources",
        parent:tabs['programs-culture-master']
    };
    tabs.extracurriculars = {
        name: "extracurriculars",
        selector: "#js_extracurriculars",
        parent:tabs['programs-culture-master']
    };
    tabs.culture = {
        name: "culture",
        selector: "#js_culture",
        parent:tabs['programs-culture-master']
    };

    // for each tab, create an array that contains all of its children
    var tab;
    for (tab in tabs) {
        if (tabs.hasOwnProperty(tab)) {
            var parent = tabs[tab].parent;
            if (parent !== undefined) {
                parent.children = parent.children || [];
                parent.children.push(tabs[tab]);
            }
        }
    }

    var init = function() {
        setupTabClickHandlers();
        return this;
    };

    // given a tab, determines which child subtab is active
    var getActiveChildTab = function(parentTab) {
        if (typeof parentTab === 'string') {
            parentTab = tabs[parentTab];
        }
        if (parentTab === undefined || parentTab.children === undefined) {
            return undefined;
        }
        var i = parentTab.children.length;
        while (i--) {
            if ($(parentTab.children[i].selector).hasClass('selected')) { // check if the subtab is selected
                return parentTab.children[i];
            }
        }
    };

    var linkToTabAndAnchor = function(destinationTab, hash) {
        if (typeof destinationTab === 'string') {
            destinationTab = tabs[destinationTab];
        }

        try {
            if (destinationTab.parent !== undefined) {
                linkToTabAndAnchor(destinationTab.parent); // recursion
            }
            var selector = destinationTab.selector;
            linkToTabs(selector);

            if(hash !== undefined) {
                GS.util.jumpToAnchor(hash);
            }
        } catch (e) {
            // on error, fall back on default click handling
            return true;
        }
        return false;
    };

    var setupTabClickHandlers = function() {
        // register some custom data attributes that will allow easily linking to a profile tab and anchor
        $('body').on('click', '[data-gs-show-tab]', function(event) {
            var $this = $(this);
            var tabName = $this.data('gs-show-tab');
            var tabOptions = $this.data('gs-tab-options');
            linkToTabAndAnchor(tabName, tabOptions);
            event.preventDefault();
            event.stopPropagation();
        });

        // send omniture data when tabs are clicked
        $('[data-gs-tab] a').on('click', function() {
            var $this = $(this);
            var tabName = $this.parent().data('gs-tab');
            var childTab = getActiveChildTab(tabName);
            if (childTab !== undefined) {
                GS.tracking.sendOmnitureData(childTab.name);
            } else {
                GS.tracking.sendOmnitureData(tabName);
            }
        });
    };

    var linkToTabs = function(selector){
        $(selector).showTab();
        return false;
    };

    return {
        getActiveChildTab: getActiveChildTab,
        linkToTabs : linkToTabs,
        linkToTabAndAnchor : linkToTabAndAnchor,
        setupTabClickHandlers : setupTabClickHandlers,
        init:init
    };
}());


GS.util.jumpToAnchor = function(hash) {
    window.location.hash=hash;
    return false;
};

jQuery(document).ready(function() {
    GS.profile.init();

    if ( jQuery.browser.msie ) {   if(jQuery.browser.version <= 7){ jQuery(".arrowdiv").remove() } }

    /* this initializes all of the star rating options on the reviews page */
    starRatingInterface("starRatingContainer1", 16, 5, "overallAsString", "js_reviewTopStarDescriptor");
    starRatingInterface("starRatingContainer2", 16, 5, "teacherAsString", "");
    starRatingInterface("starRatingContainer3", 16, 5, "principalAsString", "");
    starRatingInterface("starRatingContainer4", 16, 5, "parentAsString", "");

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