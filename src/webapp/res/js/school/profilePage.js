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

    var isHistoryAPIAvailable = (typeof(window.History) !== 'undefined' && window.History.enabled === true);
    var originalPageTitle;
    var originalQueryData;

    //TODO: find out ad slot names for profile
    var adSlotPrefix = "School_Profile_Page";
    var adSlotKeys = ['Footer_728x90', 'Header_728x90', 'Community_Ad_300x50',
        'BelowFold_300x250', 'BelowFold_Top_300x125', 'AboveFold_300x600', 'Sponsor_630x40'];


    var init = function() {
        originalPageTitle = document.title;
        originalQueryData = GS.uri.Uri.getQueryData();

        $('.gsTabs').each(function() {
            var $this = $(this);
            var key = $this.data('gs-tabs') || $this;
            var tabsModule = new GS.Tabs($this, key);
            tabsModule.showTabs();
        });

        GS.tabManager.setOnTabChanged(onTabChanged);
        GS.tabManager.setBeforeTabChange(beforeTabChange);

        updateHistoryEntryWithCurrentTab();
        handleHashBang();

        var i = adSlotKeys.length;
        while(i--) {
            adSlotKeys[i] = adSlotPrefix + '_' + adSlotKeys[i];
        }

        if (typeof(window.History) !== 'undefined' && window.History.enabled === true) {
            window.History.Adapter.bind(window, 'statechange', function() {
                var state = History.getState();
                if (state && state.url) {
                    var tab = 'overview';
                    if (state.url.indexOf('?') > -1) {
                        var queryString = state.url.substr(state.url.indexOf('?')+1);
                        if (queryString.indexOf('#') > -1) {
                            queryString = queryString.substr(0, queryString.indexOf('#'));
                        }
                        tab = GS.uri.Uri.getFromQueryString('tab', queryString) || tab;
                    }
                    if (tab) {
                        GS.tabManager.showTabWithOptions({
                            tab:tab,
                            skipHistory:true
                        });
                    }
                }
            });
        }
        return this;
    };

    var beforeTabChange = function(newTab) {
        if (newTab.name === 'reviews' && originalQueryData.hasOwnProperty('page') && originalQueryData.page !== '1') {
            return false; // do not allow JS tab change
        }
        return true;
    };

    var onTabChanged = function(currentTab, options) {
        options = options || {};
        var $a = $(currentTab.selector);
        var jumpedToAnchor = false;

        if(isHistoryAPIAvailable && options && options.hash !== undefined) {
            GS.util.jumpToAnchor(options.hash);
            jumpedToAnchor = true;
        }

        if (!options.skipHistory) {
            if (isHistoryAPIAvailable) {
                if (jumpedToAnchor) {
                    GS_updateHistory(getUpdatedTitle(currentTab.title), $a.attr('href'));
                } else {
                    GS_changeHistory(getUpdatedTitle(currentTab.title), $a.attr('href'));
                }
            } else {
                var anchorVal = "/" + currentTab.name;
                if(options && options.hash !== undefined) {
                    GS.util.jumpToAnchor(options.hash);
                    anchorVal += "/" + options.hash;
                }
                GS.util.jumpToAnchor("!" + anchorVal);
            }
        }


        // special-case for JS that needs to execute when culture tab is shown:
        // TODO: more elegant solution. Maybe tab-changing code should fire event that's namespaced using the tab name
        if (currentTab.name === 'culture') {
            $('.infiniteCarousel11:visible').trigger('shown');
        }


        GS.tracking.sendOmnitureData(currentTab.name);
        GS_notifyQuantcastComscore();
        refreshAds();
    };

    var getUpdatedTitle = function(tabTitle) {
        var oldPageTitle = originalPageTitle;
        // check whether to augment or replace title
        var replaceTheTitle = tabTitle.indexOf('-') !== -1;

        if (replaceTheTitle) {
            return tabTitle;
        }

        // might need to preserve titles of overview pages. Special-case overview title TODO: need special case?
        var overviewSuffix = " - School overview";
        if (oldPageTitle.indexOf(overviewSuffix, oldPageTitle.length - overviewSuffix.length) !== -1) {
            oldPageTitle = "for " + oldPageTitle.replace(overviewSuffix, "");
        }

        // do regex replace of original title with tab link's title attribute
        return oldPageTitle.replace(/((?!for).)*/, tabTitle + " ");
    };

    var updateHistoryEntryWithCurrentTab = function() {
        if (isHistoryAPIAvailable) {
            var currentTab = GS.tabManager.getCurrentTab();
            if (currentTab !== GS.tabManager.getTabByName('overview')) {
                window.History.replaceState(null, getUpdatedTitle(currentTab.title), null);
            }
        }
    };

    var handleHashBang = function() {
        var hash = window.location.hash;
        var options = {};
        if (hash.substring(1,2) === '!') {
            var tokens = hash.substring(2).split('/');
            if (tokens.length > 1 && GS.tabManager.getTabByName(tokens[1]) !== undefined) {
                options.tab = tokens[1];
                if (tokens.length > 2) {
                    options.hash = tokens[2];
                }
                GS.tabManager.showTabWithOptions(options);
            }
        }
    };

    var refreshAds = function() {
        GS.ad.refreshAds(adSlotKeys);
    };

    return {
        init:init,
        refreshAds:refreshAds
    };
}());


GS.util = GS.util || {};
GS.util.jumpToAnchor = function(hash) {
    window.location.hash=hash;
    return false;
};

var GS_changeHistory = function(title, url) {
    if (typeof(window.History) !== 'undefined' && window.History.enabled === true) {
        window.History.pushState(null, title, url);
    }
};

var GS_updateHistory = function(title, url) {
    if (typeof(window.History) !== 'undefined' && window.History.enabled === true) {
        window.History.replaceState(null, title, url);
    }
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
function drawPieChart(dataIn, divNameId, dimensions, catchClick) {

    // Create and populate the data table.
    var data = google.visualization.arrayToDataTable(dataIn, true);

    var options = {
        width: dimensions,
        height: dimensions,
        legend: 'none',
        tooltip: {showColorCode: true,text:'value',textStyle:{color: '#2b2b2b', fontName: 'Arial', fontSize: '10'}},
        colors:['#327FA0','#E2B66C','#DB7258','#A4B41E','#38A37A','#B66483','#7B498F','#414F7B'],
        pieSliceText: 'none',
        chartArea:{left:15,top:15,bottom:10,right:10,width:"80%",height:"80%"},
        pieSliceBorderColor:'white'

    }

    // Create and draw the visualization.
    var pieChart = new google.visualization.PieChart(document.getElementById(divNameId));
        pieChart.draw(data, options);

    if(catchClick){
        google.visualization.events.addListener(pieChart, 'select', selectHandler);
    }
    function selectHandler() {
        GS.tracking.sendOmnitureData('demographics');
        GS.tabManager.showTabWithOptions({tab:'demographics', hash:'header'});
    }
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