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
    var initialTab;

    var refreshableNonOverviewAdSlotKeys = [
        'School_Profile_Page_Footer_728x90',
        'School_Profile_Page_Header_728x90',
        'School_Profile_Page_Community_Ad_300x50',
        'School_Profile_Page_BelowFold_300x250',
        'School_Profile_Page_BelowFold_Top_300x125',
        'School_Profile_Page_AboveFold_300x600'
    ];
    var refreshableOverviewAdSlotKeys = refreshableNonOverviewAdSlotKeys.slice(0);
    var refreshableReviewsAdSlotKeys = refreshableNonOverviewAdSlotKeys.slice(0);

    var otherAdSlotKeys = [
        'Global_NavPromo_970x30',
        'Custom_Welcome_Ad',
        'Custom_Peelback_Ad'
    ];

    var init = function() {
        //window.History.debug.enable = true;
        // Bind to StateChange Event
        /*window.History.Adapter.bind(window,'statechange',function(){ // Note: We are using statechange instead of popstate
            var State = window.History.getState(); // Note: We are using History.getState() instead of event.state
            console.log('statechange', State.data, State.title, State.url);
            window.History.log(State.data, State.title, State.url);
        });

        window.History.Adapter.bind(window,'hashchange',function(){ // Note: We are using statechange instead of popstate
            var State = window.History.getState(); // Note: We are using History.getState() instead of event.state
            console.log('hashchange', State.data, State.title, State.url);
            window.History.log('hash change', State.data, State.title, State.url);
        });

        window.History.Adapter.bind(window,'anchorchange',function(){ // Note: We are using statechange instead of popstate
            var State = window.History.getState(); // Note: We are using History.getState() instead of event.state
            console.log('anchorchange', State.data, State.title, State.url);
            window.History.log('anchor change', State.data, State.title, State.url);
        });*/


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

        var currentTab = GS.tabManager.getCurrentTab();
        //refreshAdsForTab(currentTab.name);
        initialTab = currentTab;
        if (initialTab.name === 'overview') {
            refreshableOverviewAdSlotKeys.push('School_Profile_Page_Sponsor_630x40');
        } else if (initialTab.name === 'reviews') {
            refreshableReviewsAdSlotKeys.push('School_Profile_Page_Reviews_CustomSponsor_630x40');
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

    var refreshAdsForTab = function(tabName) {
        if (tabName === "overview") {
            refreshOverviewAds();
        } else if (tabName === 'reviews') {
            refreshReviewsAds();
        } else {
            refreshNonOverviewAds();
        }
    };

    var beforeTabChange = function(newTab) {
        if (newTab.name === 'reviews' && originalQueryData.hasOwnProperty('page') && originalQueryData.page !== '1') {
            return false; // do not allow JS tab change
        }
        return true;
    };

    var onTabChanged = function(currentTab, options) {
        //console.log('on tab changed', currentTab);
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
        refreshAdsForTab(currentTab.name);
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

                // if href contains a hash, History.js replaceState will not function properly
                var href = window.location.href;
                    if (window.location.hash) {
                    href = href.replace(window.location.hash, '');
                }
                window.History.replaceState(null, getUpdatedTitle(currentTab.title), href);
                //window.History.replaceState(null, getUpdatedTitle(currentTab.title), null);
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

    var refreshOverviewAds = function() {
        //console.log('refreshing overview ads', refreshableOverviewAdSlotKeys);
        GS.ad.refreshAds(refreshableOverviewAdSlotKeys);
    };
    var refreshReviewsAds = function() {
        //console.log('refreshing reviews ads', refreshableReviewsAdSlotKeys);
        GS.ad.refreshAds(refreshableReviewsAdSlotKeys);
    };
    var refreshNonOverviewAds = function() {
        //console.log('refresh non overview ads', refreshableNonOverviewAdSlotKeys);
        GS.ad.refreshAds(refreshableNonOverviewAdSlotKeys);
    };
    var initializeOverviewAds = function() {
        //console.log('init overview ads', refreshableOverviewAdSlotKeys.concat(otherAdSlotKeys));
        //GS.ad.refreshAds(refreshableOverviewAdSlotKeys.concat(otherAdSlotKeys));
        GS.ad.refreshAds(['School_Profile_Page_Header_728x90']);
    };
    var initializeNonOverviewAds = function() {
        //console.log('init non overview ads');
        GS.ad.refreshAds(refreshableNonOverviewAdSlotKeys.concat(otherAdSlotKeys));
    };

    return {
        init:init,
        refreshAdsForTab:refreshAdsForTab,
        initializeOverviewAds:initializeOverviewAds,
        initializeNonOverviewAds:initializeNonOverviewAds
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


/**
 * GS-12260 PHOTO GALLERY JAVASCRIPT
 */

var GSM = GSM || {};
GSM.photoGallery = GSM.photoGallery || {};
Function.prototype.gs_bind = function(obj) {
    var method = this;
    return function() {
        return method.apply(obj, arguments);
    };
};
/**
 * Constructor
 */

GSM.photoGallery.PhotoGallery = function(prefix, multiSizeImageArray, debug, triggerLayer) {
    var closeButtonDomId = prefix + "-photo-gallery-close"; //close button
    var backButtonId = prefix + "-photo-gallery-back";
    var nextButtonId = prefix + "-photo-gallery-next";
    var thumbnailIdPrefix = prefix + "-gallery-thumbnail";
    var thumbnailSelectedCssClass = "gallery-thumbnail-selected";
    var fullSizeImageIdPrefix = prefix + "-gallery-fullsize";
    var triggerLayer = triggerLayer;
    var id = prefix + "-photo-gallery";
    var currentFullSizeImage = 0;
    var numberOfImages = multiSizeImageArray.length;
    var multiSizeImageArray = multiSizeImageArray;

    var thumbnailLoaderPosition = 0;
    var fullSizeImageLoaderPosition = 0;
    var chosenTimeout = 50; //ms
    var debug = debug;
    var photoMargins = [];
    var shownOnce = false;

    var init = function(){
        attachShowEvent(triggerLayer, null);// function() {jQuery('.infiniteCarousel8').infiniteCarousel();});
        loadThumbnails();
        applyThumbnailClickHandlers();
        applyButtonClickHandlers();
    };
    return{
        init:init
    };

    function showFullSizeImage(index) {
        var id;
        //hide all other images
        for (var i = 0; i < multiSizeImageArray.length; i++) {
            if (i === index) {
                continue;
            }
            id = fullSizeImageIdPrefix + '-' + i;

            jQuery('.' + id).hide();
            jQuery('.' + thumbnailIdPrefix + '-' + i).removeClass(thumbnailSelectedCssClass);
        }
        //show desired image
        id = fullSizeImageIdPrefix + '-' + index;

        jQuery('.' + id).show();

        jQuery('.' + thumbnailIdPrefix + '-' + index).addClass(thumbnailSelectedCssClass);
//        console.log("index - before trigger:"+index);
        jQuery('.' + thumbnailIdPrefix + '-' + index).trigger('itemSelected'); // custom infiniteCarousel event

        //track change
        currentFullSizeImage = index;
    };

    function showNextImage() {
        var targetIndex = parseInt(currentFullSizeImage) + 1;

        if (targetIndex >= numberOfImages) {
            targetIndex = 0;
        }
        showFullSizeImage(targetIndex);
        sendOmnitureTrackingInfo();
    };

    function showPreviousImage () {
        var targetIndex = parseInt(currentFullSizeImage) - 1;
        if (targetIndex < 0) {
            targetIndex = numberOfImages - 1;
        }
        showFullSizeImage(targetIndex);
        sendOmnitureTrackingInfo();
    };

    function loadThumbnail (index) {
        var image = multiSizeImageArray[index].getThumb();
        if (!image.isLoaded()) {
            var container = jQuery('.' + thumbnailIdPrefix + '-' + index);
            container.find('img').attr('src', image.getSrc());
            image.setLoaded(true);
            return true;
        } else {
            return false;
        }
    };

    function loadThumbnails() {
        if (thumbnailLoaderPosition >= numberOfImages) {
            return;
        }
        var success = loadThumbnail(thumbnailLoaderPosition);
        if (success) {
            thumbnailLoaderPosition++;
            if (thumbnailLoaderPosition < multiSizeImageArray.length) {
                setTimeout(loadThumbnails.gs_bind(this), chosenTimeout);
            }
        } else {
            thumbnailLoaderPosition++;
            if (thumbnailLoaderPosition < multiSizeImageArray.length) {
                loadThumbnails();
            }
        }
    };

    function loadFullSizeImage(index) {
        var image = multiSizeImageArray[index].getFull();
        if (!image.isLoaded()) {
            var container = jQuery('.' + fullSizeImageIdPrefix + '-' + index);
            container.find('img').attr('src', image.getSrc());
            image.setLoaded(true);
            return true;
        } else {
            return false;
        }
    };

    function loadFullSizeImages() {
        if (fullSizeImageLoaderPosition >= numberOfImages) {
            return;
        }
        var success = loadFullSizeImage(fullSizeImageLoaderPosition);
        if (success) {
            fullSizeImageLoaderPosition++;
            if (fullSizeImageLoaderPosition < multiSizeImageArray.length) {
                setTimeout(loadFullSizeImages.gs_bind(this), chosenTimeout * 2);
            }
        } else {
            fullSizeImageLoaderPosition++;
            if (fullSizeImageLoaderPosition < multiSizeImageArray.length) {
                loadFullSizeImages();
            }
        }
    };

    function loadImages() {
        loadThumbnails();
        loadFullSizeImages();
    };

    function applyThumbnailClickHandlers() {
        var myContainer = jQuery('.' + id + " .unordered-carousel");
        myContainer.on('click',   function(event){
            var index = parseThumbnailId($(event.target));
            showFullSizeImage(index);
            sendOmnitureTrackingInfo();
        });
    };

    /*****
     * the last class of the containing parent div needs to end with -number
     * ex. imageIdent-4
     * @param obj    - this is the img obj
     * @return {*}   - returns its id
     */
    function parseThumbnailId(obj){
        return obj.parent().attr('class').split("-").reverse()[0];
    }

    function sendOmnitureTrackingInfo() {
        //requires /res/js/omnitureEventNotifier.js
        omnitureEventNotifier.clear();
        omnitureEventNotifier.successEvents = "event58;";
        omnitureEventNotifier.send();
    };

    function applyButtonClickHandlers() {
        jQuery('#' + backButtonId).click(function() {
            showPreviousImage();
        }.gs_bind(this));
        jQuery('#' + nextButtonId).click(function() {
            showNextImage();
        }.gs_bind(this));
        jQuery('#' + closeButtonDomId).click(function() {
            hideMod();
        }.gs_bind(this));
    };

    function showMod(){
        sendOmnitureTrackingInfo();
        var $me = jQuery('#' + id);
        ModalManager.showModal({
            'layerId' :  id
        });
//        if(!shownOnce){
        $me.find('.js_infiniteCarousel').trigger('shown'); // custom infiniteCarousel event
//        }
    };

    function hideMod() {
        ModalManager.hideModal({
            'layerId' : id
        });
        return false;
    };

    /**
     * Make the gallery open when provided dom node is clicked
     * @param id
     */
    function attachShowEvent(cssClass, initialCallback) {
        jQuery("." + cssClass).click(function(event) {
            var index = parseThumbnailId($(event.target));
            loadFullSizeImages();
            if (initialCallback && typeof(initialCallback) === 'function' && !shownOnce) {
                shownOnce = true;
                initialCallback();
            }
//            var photoNumVar = jQuery('input.js_photoNum').val();
//            var photoNumToShow = (photoNumVar !== undefined && photoNumVar !== null) ? (isNaN(photoNumVar - 1) ? 0 : (photoNumVar - 1)) : 0;
            showFullSizeImage(index); //load the first full size image into gallery
            showMod();
//            if (initialCallback && typeof(initialCallback) === 'function' && !shownOnce) {
//                shownOnce = true;
//                initialCallback();
//            }
            return false;
        }.gs_bind(this));
    };

};
/**
 * Constructor
 */
GSM.photoGallery.MultiSizeImage = function(thumbnailImage, fullSizeImage) {
    var thumbnailImage = thumbnailImage;
    var fullSizeImage = fullSizeImage;
    var getThumb = function(){return thumbnailImage}
    var getFull = function(){return fullSizeImage}
    return {
        getThumb: getThumb,
        getFull: getFull
    }
};


/**
 * Constructor
 */
GSM.photoGallery.Image = function(src, alt, id, cssClass, title, height, width) {
    var src = src;
    this.id = id;
    this.cssClass = cssClass;
    this.alt = alt;
    this.title = title;
    this.height = height;
    this.width = width;
    var loaded = false;
    var getSrc = function(){return src;}
    var isLoaded = function(){return loaded;}
    var setLoaded = function(b){loaded = b;}
    return{
        isLoaded: isLoaded,
        getSrc: getSrc,
        setLoaded: function( b ){setLoaded(b);}
    }
};