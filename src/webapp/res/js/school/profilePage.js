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

GS.ad = GS.ad || {};
GS.ad.profile = GS.ad.profile || {};

// should be initialized in xGAMSetup.tagx but also initialize here to guarantee code doesn't fail
GS.ad.targeting = GS.ad.targeting || {};
GS.ad.targeting.pageLevel = GS.ad.targeting.pageLevel || {};
GS.ad.targeting.pageLevel['template'] = GS.ad.targeting.pageLevel['template'] || [];

GS.ad.profile.tabNameForAdTargeting = {
    ''                   : 'overview',
    'overview'           : 'overview',
    'reviews'            : 'reviews',
    'test-scores'        : 'testscores',
    'ratings'            : 'testscores',
    'college-readiness'  : 'testscores',
    'demographics'       : 'studteach',
    'teachers'           : 'studteach',
    'programs-culture'   : 'progcult',
    'culture'            : 'progcult',
    'programs-resources' : 'progcult',
    'extracurriculars'   : 'progcult',
    'enrollment'         : 'enrollment'
};

GS.profile = GS.profile || (function() {
    "use strict";


    var isHistoryAPIAvailable = (typeof(window.History) !== 'undefined' && window.History.enabled === true);
    var originalPageTitle;
    var originalQueryData;
    var initialTab;

    var refreshableProfileAllAdSlotKeys = [
        'School_Profile_Page_Footer_728x90',
        'School_Profile_Page_Header_728x90',
        'School_Profile_Page_Footer_Branded_Tandem_728x90',
        'School_Profile_Page_Header_Branded_Tandem_728x90',
        'School_Profile_Page_Community_Ad_300x50',
        'School_Profile_Page_BelowFold_300x250',
        'School_Profile_Page_BelowFold_Top_300x125',
        'School_Profile_Page_AboveFold_300x600',
        'School_Profile_Page_AboveFold_300x600',
        'School_Profile_Page_Branded_Tandem_Tile_150x30',
        'Global_NavPromo_970x30',
        'Custom_Welcome_Ad',
        'School_Profile_Page_Footer_Branded_Tandem_728x90',
        'School_Profile_Page_Header_Branded_Tandem_728x90',
        'School_Profile_Page_AboveFold_Branded_Tandem_300x600',
        'School_Profile_Page_BelowFold_Branded_Tandem_300x250'
    ];

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

    var refreshableCultureAdSlotKeys = [
        'School_Profile_Page_Community_Ad_300x50',
        'School_Profile_Page_BelowFold_Top_300x125'
    ];

    var refreshableCultureBranding = [
        'School_Profile_Page_Footer_Branded_Tandem_728x90',
        'School_Profile_Page_Header_Branded_Tandem_728x90',
        'School_Profile_Page_AboveFold_Branded_Tandem_300x600',
        'School_Profile_Page_BelowFold_Branded_Tandem_300x250'
    ];
    var refreshableCultureNoBranding = [
        'School_Profile_Page_Footer_728x90',
        'School_Profile_Page_Header_728x90',
        'School_Profile_Page_AboveFold_300x600',
        'School_Profile_Page_BelowFold_300x250'
    ];
    var refreshableTandemTileBranding = [
        'School_Profile_Page_Branded_Tandem_Tile_150x30'
    ];

    var otherAdSlotKeys = [
        'Global_NavPromo_970x30',
        'Custom_Welcome_Ad'
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

        initialTab = currentTab;
        if (initialTab.name === 'overview') {
            refreshableOverviewAdSlotKeys.push('School_Profile_Page_Sponsor_630x40');
        } else if (initialTab.name === 'reviews') {
            refreshableReviewsAdSlotKeys.push('School_Profile_Page_Reviews_CustomSponsor_630x40');
        } else if (initialTab.name === 'culture') {
//            refreshableCultureAdSlotKeys.push('School_Profile_Page_Culture_CustomSponsor_630x40');
        }

        // We can't just call refreshAdsForTab, since then non-delayed ads would get hit with two impressions
        // on initial page load, any ads which have not been delayed have been displayed()ed and shown.
        // but, there's logic that's needed to initialize the existing "handleTandem" logic, so that when Tandem
        // call resolves/rejects, the handleTandem code knows which ads to refresh
        initializeAdsForTab(currentTab.name);
        //refreshAdsForTab(currentTab);

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

    var hideAllAds = function(){
        var len = refreshableProfileAllAdSlotKeys.length;
        var needle = "School_Profile_Page_";
        var needleLength = needle.length;
        var title = "";
        for( var i=0; i < len; i++){
            var title = refreshableProfileAllAdSlotKeys[i];
            if(title.indexOf(needle) >= 0){
                $("#"+title.substring(needleLength)).hide();
            }
            else{
                $("#"+title).hide();
            }

        }
    }

    var showRelevantAdDivs = function(adslots){
        var len = adslots.length;
        var needle = "School_Profile_Page_";
        var needleLength = needle.length;
        var title = "";
        for( var i=0; i < len; i++){
            var title = adslots[i];
            if(title.indexOf(needle) >= 0){
                $("#"+title.substring(needleLength)).show();
            }
            else{
                $("#"+title).show();
            }

        }
    }

    var refreshAdsForTab = function (tabName) {
        /*  For the culture page with tandem
        *   Need to hide both ads as we don't know which will be filled yet.
        * */

        hideAllAds();

        switch (tabName) {
            case "overview":{
                var layerArr = ['Branded_Tandem_Tile_150x30'];
                handleTandemBranding(refreshableTandemTileBranding, layerArr, '', '', tabName);
                refreshOverviewAds(GS.ad.profile.tabNameForAdTargeting[tabName]);
                break;
            }
            case "reviews":{
                refreshReviewsAds(GS.ad.profile.tabNameForAdTargeting[tabName]);
                break;
            }
            case "culture":{
                var layerArrCultureBranded = [
                    'Footer_Branded_Tandem_728x90',
                    'Header_Branded_Tandem_728x90',
                    'AboveFold_Branded_Tandem_300x600',
                    'BelowFold_Branded_Tandem_300x250'
                ];
                var layerArrCulture = [
                    'Footer_728x90',
                    'Header_728x90',
                    'AboveFold_300x600',
                    'BelowFold_300x250'
                ];
                handleTandemBranding(refreshableCultureBranding, layerArrCultureBranded, refreshableCultureNoBranding, layerArrCulture, tabName);
                refreshCultureAds(GS.ad.profile.tabNameForAdTargeting[tabName]);
                break;
            }
            default:{
                refreshNonOverviewAds(GS.ad.profile.tabNameForAdTargeting[tabName]);
            }
        }
    };

    var initializeAdsForTab = function (tabName) {
        // on initial page load, any ads which have not been delayed have been displayed()ed and shown.
        // but, there's logic that's needed to initialize the existing "handleTandemAd" logic in calendar.js, so that
        // when Tandem call resolves/rejects, the handleTandem code knows which ads to refresh

        switch (tabName) {
            case "overview":{
                var layerArr = ['Branded_Tandem_Tile_150x30'];
                handleTandemBranding(refreshableTandemTileBranding, layerArr, '', '', tabName);
                break;
            }
            case "culture":{
                var layerArrCultureBranded = [
                    'Footer_Branded_Tandem_728x90',
                    'Header_Branded_Tandem_728x90',
                    'AboveFold_Branded_Tandem_300x600',
                    'BelowFold_Branded_Tandem_300x250'
                ];
                var layerArrCulture = [
                    'Footer_728x90',
                    'Header_728x90',
                    'AboveFold_300x600',
                    'BelowFold_300x250'
                ];
                handleTandemBranding(refreshableCultureBranding, layerArrCultureBranded, refreshableCultureNoBranding, layerArrCulture, tabName);
                break;
            }
            default:{
            }
        }
    };

    var refreshTandemAds = function(originalArr, slots, layers){
        for(var i=0; i < layers.length; i++){
            $("#"+layers[i]).show();
        }
        return originalArr.concat(slots);
    }

    var handleTandemBranding = function(refreshableBranding, brandingLayerId, refreshableNoBranding, noBrandingLayerId, tabName) {
        // this is not set on load if I check in tandem object
        if(hasTandemBranding == 'true'){
            if(GS.school.tandem.isTandemReturned()){
                // show branded ad by pushing on the
                if(GS.school.tandem.isTandemActive()){
                    if(tabName == "overview"){
                        refreshableOverviewAdSlotKeys = refreshTandemAds(refreshableOverviewAdSlotKeys, refreshableBranding, brandingLayerId);
                    }
                    else{
                        refreshableCultureAdSlotKeys = refreshTandemAds(refreshableCultureAdSlotKeys,refreshableBranding, brandingLayerId);
                    }
                }
                else{
                    if(noBrandingLayerId != "" && $.isArray(noBrandingLayerId) && noBrandingLayerId.length > 0){
                        refreshableCultureAdSlotKeys = refreshTandemAds(refreshableCultureAdSlotKeys, refreshableNoBranding, noBrandingLayerId);
                    }
                }
            }
            else{
                GS.school.tandem.setTandemShowAd('true');
                GS.school.tandem.setTandemTabName(tabName);
                GS.school.tandem.setTandemWhichAds(refreshableBranding, brandingLayerId, refreshableNoBranding, noBrandingLayerId);
            }
        }
        else{
            if(noBrandingLayerId != "" && $.isArray(noBrandingLayerId) && noBrandingLayerId.length > 0){
                refreshableCultureAdSlotKeys = refreshTandemAds(refreshableCultureAdSlotKeys, refreshableNoBranding, noBrandingLayerId);
            }
        }
    }

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
                document.title = getUpdatedTitle(currentTab.title);
            }
        }


        // special-case for JS that needs to execute when culture tab is shown:
        // TODO: more elegant solution. Maybe tab-changing code should fire event that's namespaced using the tab name
        if (currentTab.name === 'culture') {
            $('.infiniteCarousel11:visible').trigger('shown');
        }

        if(GS.tracking.profile.getOmnitureProfileNavElementName() !== undefined) {
            GS.tracking.data.updateProps.setProps(currentTab.name, {'prop9' : GS.tabManager.getOmniturePageNameForTab(currentTab.name) + ' ' + GS.tracking.profile.getOmnitureProfileNavElementName()});
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

    // need to replace all these calls to directly call refreshAdsOnTabGeneric

    var refreshOverviewAds = function(tabName) {
        refreshAdsOnTabGeneric(refreshableOverviewAdSlotKeys, tabName, true);
    };
    var refreshReviewsAds = function(tabName) {
        refreshAdsOnTabGeneric(refreshableReviewsAdSlotKeys, tabName, true);
    };
    var refreshCultureAds = function(tabName) {
        refreshAdsOnTabGeneric(refreshableCultureAdSlotKeys, tabName, true);
    };
    var refreshNonOverviewAds = function(tabName) {
        refreshAdsOnTabGeneric(refreshableNonOverviewAdSlotKeys, tabName, true);
    };
    var refreshNonOverviewAdsWithoutTargetingChange = function() {
        refreshAdsOnTabGeneric(refreshableNonOverviewAdSlotKeys, '', false);
    };

    /*
        adslots - array of ad slots to show and refresh
        tabName - tab name for targeting
        targeting - boolean to either target tab(true) or no tab target (false)
     */
    var refreshAdsOnTabGeneric = function(adslots, tabName, targeting) {
        // patch for ads issue GS-14387. Should be combined with existing GS.ad.refreshAds call in xGAMSetup
        var numberAdSlots = adslots.length;
        var i, slot, slotName;
        var slotsLeftToRefresh = [];

        if(GS.ad.slots === undefined) return;

        for (i = 0; i < numberAdSlots; i++) {
            slotName = adslots[i];
            slot = GS.ad.slots[slotName];
            if(slot.GS_displayedYet === false) {
                // need to display instead of refresh
                GS.ad.displayAd(slotName, slot.GS_domId);
            } else {
                // track that we still need to refresh this slot
                slotsLeftToRefresh.push(slotName);
            }
        }

        // show all slots that were passed into this function, regardless of how we tell google to refresh/show them
        showRelevantAdDivs(adslots);
        if (GS.ad.unhideGhostTextForAdSlots) {
            GS.ad.unhideGhostTextForAdSlots(adslots);
        }

        // if no ad slots are left to be refreshed, exit early
        if (!slotsLeftToRefresh.length > 0) {
            return;
        }

        if(targeting){
            GS.ad.setTargetingAndRefresh(slotsLeftToRefresh, 'template', GS.ad.targeting.pageLevel['template'].concat(tabName));
        }
        else{
            GS.ad.refreshAds(slotsLeftToRefresh);
        }
    };

    var getAlternateSitePath = function() {
        var $currentTabLayer = GS.tabManager.getActiveLayer();
        var href = $currentTabLayer.find('a.js-alternateSitePath').attr('href');
        return href;
    };


    var getDelayedAds= function() {
        var slots = [];
        var tab = GS.uri.Uri.getQueryData().tab || "overview";
        slots = slots.concat(refreshableTandemTileBranding);
        slots = slots.concat(refreshableCultureBranding);

        if (tab === "culture") {
            slots = slots.concat(refreshableCultureNoBranding);
        }

        return slots;
    };

    var shouldDelayAd = function(slotName) {
        var delayedAds = GS.profile.getDelayedAds();
        var shouldDelay = ($.inArray(slotName, delayedAds) > -1);
        return shouldDelay;
    };

    return {
        init:init,
        refreshAdsOnTabGeneric:refreshAdsOnTabGeneric,
        refreshAdsForTab:refreshAdsForTab,
        getAlternateSitePath:getAlternateSitePath,
        refreshNonOverviewAdsWithoutTargetingChange:refreshNonOverviewAdsWithoutTargetingChange,
        getDelayedAds:getDelayedAds,
        shouldDelayAd:shouldDelayAd
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
var hashValue = window.location.hash;
jQuery(document).ready(function() {
    GS.profile.init();
    if(hashValue != ""){
        GS.util.jumpToAnchor(hashValue);
    }
    if ( GS.util.isBrowserIELessThan8() ){ jQuery(".arrowdiv").remove() }

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

    // ratings subgroup interactions with menu
    var ratingsSubgroupsMenu = $('#js_ratings_cat_menu');
    var ratingsSubgroupsContentWrapper = $('#js_ratings_cat_content_wrapper');
    if (ratingsSubgroupsMenu.length === 1 && ratingsSubgroupsContentWrapper.length === 1) {
        var ratingsSubgroupLabels = ratingsSubgroupsMenu.find('.js_ratings_cat_label');
        ratingsSubgroupLabels.on('click', function() {
            var catSelected = $(this).attr('id');
            $(this).parent().css("background-color", "#C9E4F1");
            $(this).parent().addClass("selected");
            $(this).parent().siblings().removeClass("selected");
            $(this).parent().siblings().css("background-color", "#FFFFFF");

            //Hide all the data
            ratingsSubgroupsContentWrapper.find('.js_ratings_cat_content').hide();

            //Show the data for the grade selected.
            $('#' + catSelected + '_content').show();

            // this click event handler is getting triggered even on inital load, when
            // the tab isn't visible. In this case we can't perform an ad refresh
            if (ratingsSubgroupsMenu.is(':visible')) {
                GS.profile.refreshNonOverviewAdsWithoutTargetingChange();
            }
        });

        ratingsSubgroupLabels.hover(
            function () {
                if (!$(this).parent().hasClass("selected")) {
                    $(this).parent().css("background-color", "#F1F1F1");
                }
            },
            function () {
                if (!$(this).parent().hasClass("selected")) {
                    $(this).parent().css("background-color", "#FFFFFF");
                }
            }
        );

        //Select the first category by default and trigger its click event, so that the data is displayed.
        var firstCategoryToSelect = ratingsSubgroupsMenu.children(":first").find("a");
        firstCategoryToSelect.trigger('click');
    }

    // test scores interactions with grade or test menus
    var testsMenu = $('#js_testSelect');
    var testScoresGrades = $('#js_testScoresGrades');
    var gradeLabel = testScoresGrades.find('.js_grade');
    var testScoresValues = $('#js_testScoresValues');
    var subjectContent = testScoresValues.find('.js_subjects');
    var specialTestsContent = testScoresValues.find('.js_specialTests');
    if (testsMenu.length === 1 && testScoresGrades.length === 1 && testScoresValues.length === 1) {
        testsMenu.on('change', function(e) {
            var select = e.target;
            var option = select.options[select.selectedIndex];
            var testSelected = $(option).val();
            var hideGrades = $(option).hasClass('js_hideGrades');

            $("#js_testLabelHeader").html($(option).text() + ' Results');

            //Hide all the grades and the subject data
            testScoresGrades.find('.js_grades').hide();
            subjectContent.hide();

            //Hide special tests
            specialTestsContent.hide();

            //Show the grades for the test.
            $('#js_' + testSelected + '_grades').show();

            // Show subgroup test
            if( testSelected.match(/_subgroup$/) ) {
                $('#js_subgroup').show();
                $('#js_testLabelHeader').addClass('bottom');
            } else {
                $('#js_subgroup').hide();
                $('#js_testLabelHeader').removeClass('bottom');
            }

            //Select the first grade by default for the test and trigger its click event, so that the data is displayed.
            var firstGradeToSelect = $('#js_' + testSelected + '_grades').children(":first").find("a");
            firstGradeToSelect.trigger('click');

            //Show special test if it's selected
            $('#js_' + testSelected).show();

            if (hideGrades) {
                $('#js_testScoresGrades').removeClass('grid_4').addClass('hide');
                $('#js_testScoresValues').removeClass('grid_11').addClass('grid_15');
            } else {
                $('#js_testScoresGrades').removeClass('hide').addClass('grid_4');
                $('#js_testScoresValues').removeClass('grid_15').addClass('grid_11');
            }

            // this click event handler is getting triggered even on inital load, when
            // the tab isn't visible. In this case we can't perform an ad refresh
            if (testsMenu.is(':visible')) {
                GS.profile.refreshNonOverviewAdsWithoutTargetingChange();
            }
        });

        //Add the handler for clicking a specific grade.
        gradeLabel.on('click', function () {
            var gradeSelected = $(this).attr('id');
            $(this).parent().css("background-color", "#C9E4F1");
            $(this).parent().addClass("selected");
            $(this).parent().siblings().removeClass("selected");
            $(this).parent().siblings().css("background-color", "#FFFFFF");

            //Hide all the data
            subjectContent.hide();

            //Show the data for the grade selected.
            $('#' + gradeSelected + '_subjects').show();

            // this click event handler is getting triggered even on inital load, when
            // the tab isn't visible. In this case we can't perform an ad refresh
            if (gradeLabel.is(':visible')) {
                GS.profile.refreshNonOverviewAdsWithoutTargetingChange();
            }
        });

        gradeLabel.hover(
            function () {
                if (!$(this).parent().hasClass("selected")) {
                    $(this).parent().css("background-color", "#F1F1F1");
                }
            },
            function () {
                if (!$(this).parent().hasClass("selected")) {
                    $(this).parent().css("background-color", "#FFFFFF");
                }
            }
        );

        // Trigger the test change event.
        testsMenu.change();
    }
});

var gs_eventclick = (GS.util.isBrowserTouch()) ? "touchstart" : "click";
var gs_eventmove = (GS.util.isBrowserTouch()) ? "touchmove" : "mousemove";
var gs_eventend = (GS.util.isBrowserTouch()) ? "touchend" : "mouseleave";

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
    $('#'+containerS).on(gs_eventmove, function(e){
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
    $('#'+containerS).on(gs_eventclick, function(e){
        var offset = $(this).offset();
        var x = e.pageX - offset.left;
        // special ipad case
        if(gs_eventclick == "touchstart"){x = event.touches[0].pageX - offset.left;}
        var currentStar = Math.floor(x/iconWidth) +1;
        if(currentStar > totalStars) currentStar = totalStars;
        overallStarRating.val(currentStar);
        overallStarRating.blur();
        starsOn.removeClass(removeClassStr).addClass(iconStr + currentStar);
        starsOff.removeClass(removeClassStr).addClass(iconStr+ (totalStars - currentStar));

    });
    $('#'+containerS).on(gs_eventend, function(e){
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
        colors:['#4393B5','#38A37A','#84D07C','#E2B66C','#E2937D','#DA5F6E','#B66483','#7B498F','#414F7B','#A7A7A7','#7CC7CE','#489A9D','#A4CEBB','#649644','#E0D152','#F1A628','#A3383A','#8C734D','#EA6394','#CE92C0','#5A78B1'],
//        colors:['#327FA0','#E2B66C','#DB7258','#A4B41E','#38A37A','#B66483','#7B498F','#414F7B'],
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

var GS = GS || {};
GS.photoGallery = GS.photoGallery || {};
Function.prototype.gs_bind = function(obj) {
    var method = this;
    return function() {
        return method.apply(obj, arguments);
    };
};
/**
 * Constructor
 */

GS.photoGallery.PhotoGallery = function(prefix, multiSizeImageArray, debug, triggerLayer) {
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
        attachShowEvent(triggerLayer, null);
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
        }
        //show desired image  - align vertically
        var image = multiSizeImageArray[index].getFull();
        var h = image.getImageHeight();
        var paddingToAdd = Math.round((500-h)/2);
        id = fullSizeImageIdPrefix + '-' + index;
        jQuery('.' + id).css('padding-top', paddingToAdd);
        jQuery('.' + id).show();

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
        loadFullSizeImages();
    };


    /*****
     * the last class of the containing parent div needs to end with -number
     * ex. imageIdent-4
     * @param obj    - this is the img obj
     * @return {*}   - returns its id
     */
    function parseImageId(obj){
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
        var overlayClass = 'js-overlay_black';
        var $me = jQuery('#' + id);
        ModalManager.showModal({
            'layerId' :  id,
            'bgcolorOverlay' : overlayClass
        });
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
            var index = parseImageId($(event.target));
            loadFullSizeImages();
            showFullSizeImage(index); //load the first full size image into gallery
            showMod();
            return false;
        }.gs_bind(this));
    };

};
/**
 * Constructor
 */
GS.photoGallery.MultiSizeImage = function(thumbnailImage, fullSizeImage) {
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
GS.photoGallery.Image = function(src, h, alt, id, cssClass, title, width) {
    var src = src;
    this.id = id;
    this.cssClass = cssClass;
    this.alt = alt;
    this.title = title;
    var height = h;
    this.width = width;
    var loaded = false;
    var getSrc = function(){return src;}
    var getImageHeight = function(){return height;}
    var isLoaded = function(){return loaded;}
    var setLoaded = function(b){loaded = b;}
    return{
        isLoaded: isLoaded,
        getSrc: getSrc,
        setLoaded: function( b ){setLoaded(b);},
        getImageHeight: getImageHeight
    }
};

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/////////     CA API Charts - line graph and bar graph

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


GS.color_lines = { colors:['#4393B5','#38A37A','#84D07C','#E2B66C','#E2937D','#DA5F6E','#B66483','#7B498F','#414F7B','#A7A7A7','#7CC7CE','#489A9D','#A4CEBB','#649644','#E0D152','#F1A628','#A3383A','#8C734D','#EA6394','#CE92C0','#5A78B1'] };

// specific to the ca api to generate graph with 200 as min or 400 as min
GS.getMinYLines = function() {
    var min = 400;
    for(var i = 0; i < data[0].values.length; i ++) {
        if(data[0].values[i].Y < min) {
            min = 200;
        }
    }
    return min;
};

GS.getMinYBar = function() {
    var min = 400;
    for(var i = 0; i < data_bar.values.length; i ++) {
        if(data_bar.values[i].Y < min) {
            min = 200;
        }
    }
    return min;
};

GS.drawGraphContainer = function (options) {
    var settings = $.extend( {
        'layerId' : 'not set',
        'width' : '600',
        'height' : '200',
        'x_axis_increments' : 1,
        'y_axis_increments' : 200,
        'y_max' : 1050,
        'y_min' : 400,
        'colors' : '',
        'data' : '',
        'graph_type' : 'line_graph',
        'legend_width' : 120
    }, options);

    var w = settings['width'] - settings['legend_width'];
    var data = settings['data'];
    var col = settings['colors'];

    var drawLegend = function(c, d){
        for(i=0; i < d.length; i++){
            drawSquareAndText(c, col.colors[i], d[i].title, i, w);
        }
        drawDashedLineAndText(c, "#CCC", "Statewide goal", d.length, w);

    }
    var image_placeHolder = "/res/images/pixel.png";

    var drawSquareAndText = function(c, color, text, i , w){
        var offsetx = w+20;
        var offsety = 20;
        var linespacer = 17;
        c.strokeStyle = "#000";
        c.fillStyle = color;
        c.font = '8pt sans-serif';
        c.textAlign = "left";
        c.beginPath();
        c.fillRect(offsetx,(offsety - 5 + linespacer*i),10,10);
        //c.strokeRect(offsetx,(offsety - 5 + linespacer*i),10,10);
        c.fillStyle = "#777";
        c.fillText(text,(offsetx + 20), (offsety + linespacer*i));
    }

    var drawDashedLineAndText = function(c, color, text, i , w){
        var offsetx = w+20;
        var offsety = 20;
        var linespacer = 17;
        c.strokeStyle = "#CCC";
        c.fillStyle = color;
        c.font = '8pt sans-serif';
        c.textAlign = "left";
        c.beginPath();
        drawHorizontalDashedLine(c, (offsety + linespacer*i), offsetx, (offsetx+10), color);
        c.fillStyle = "#777";
        c.fillText(text,(offsetx + 20), (offsety + linespacer*i));
    }

    var yearToInt = {};
    var yearXCounter = 0;
    var createYearRange = function(){
        //CREATE year range
        var yearMin = 3000;
        var yearMax = 2000;
        for(var j=0; j < data.length; j++){
            for(var i = 0; i < data[j].values.length; i++) {
                if(data[j].values[i].X < yearMin) yearMin = data[j].values[i].X;
                if(data[j].values[i].X > yearMax) yearMax = data[j].values[i].X;
            }
        }
        //build year array
        for(var i = yearMin; i <= yearMax; i++) {
            yearToInt[i.toString()] = yearXCounter;
            yearXCounter++;
        }
    }

    var drawFrameLines = function(c){

        c.lineWidth = 1;
        c.strokeStyle = '#777';
        c.font = '8pt sans-serif';
        c.textAlign = "center";
        c.beginPath();
        //c.moveTo(xPadding, 0);
        c.moveTo(xPadding, settings['height'] - yPadding);
        c.lineTo(w, settings['height'] - yPadding);
        c.stroke();
        c.fillStyle = "#777";

        // Draw the X value texts
        if(settings['graph_type'] == "bar_graph"){
            c.fillText(data.x_axis_title, ((w + xPadding)/2), settings['height'] - yPadding + 20);
        }
        else{
            createYearRange();
            $.each(yearToInt, function(key, value) {
                c.fillText(key, getXPixel(value), settings['height'] - yPadding + 20);
            });
        }

        // Draw the Y value texts
        c.textAlign = "right"
        c.textBaseline = "middle";

        for(var i = settings['y_min']; i <= settings['y_max']; i += settings['y_axis_increments']) {

            if(i == 800){
                c.fillStyle = "#000";
                c.font = '8pt sans-serif';
                c.fillText(i, xPadding - 20, getYPixel(i));
                drawHorizontalDashedLine(c, getYPixel(i), (xPadding+1), w, "#CCC");
            }
            else{
                c.fillStyle = "#777";
                c.font = '8pt sans-serif';
                c.fillText(i, xPadding - 20, getYPixel(i));
                drawHorizontalLine(c, i, (xPadding+1), w, "#ccc");
            }
        }
    }

    var drawBarGraph = function(c){
        var bottom = settings['height'] - yPadding -1;
        var top = 0;
        var left = 0;
        for(i=0; i < data.values.length; i++){
            var offsetx = xPadding*1.5;
            var offsety = yPadding;
            var barspacer = 30;
            c.fillStyle = col.colors[i];
            top = getYPixel(data.values[i].Y);
            left = offsetx+(i*barspacer);
            c.beginPath();
            c.fillRect(left,top,10,bottom-top);
            var h = bottom-top;
            var classname = "graphBar"+i;
            var areaTag = '<div class="'+classname+'" style="background-image:url('+image_placeHolder+');cursor:pointer; position:absolute; top:'+ top + 'px; left:' + left + 'px; width:10px; height:' + h + 'px;"></div>';
            graph.after(areaTag);
            var content_popup = "<span class='small bottom'><span class='bold'>" + data.values[i].title + "</span><br />API: " + data.values[i].Y + "</span>";
            if(data.values[i].N != 0){
                content_popup += "<br /><span class='small bottom'>No. tested: "+ data.values[i].N + "</span>";
            }
            $("."+classname).popover({content: content_popup, placement:'top', delay:{ show: 100, hide: 100 }});
        }
    }

    var drawFullLineGraph = function(c){
        c.lineWidth = 2;
        for(var i=0; i < data.length; i++){
            c.strokeStyle = col.colors[i];
            c.fillStyle = col.colors[i];

            // Draw the line graph
            drawLineGraph(c, data[i].values);

            // Draw the dots
            drawLineDots(c, data[i].values, i);
        }
    }

    var drawLineGraph = function(c, v) {
        c.beginPath();
        c.moveTo(getXPixel(yearToInt[v[0].X]), getYPixel(v[0].Y));
        for(var i = 0; i < v.length; i ++) {
            c.lineTo(getXPixel(yearToInt[v[i].X]), getYPixel(v[i].Y));
        }
        c.stroke();
    }

    var drawLineDots = function(c, v, linecount) {
        for(var i = 0; i < v.length; i ++) {
            c.beginPath();
            c.arc(getXPixel(yearToInt[v[i].X]), getYPixel(v[i].Y), 3, 0, Math.PI * 2, true);
            c.fill();
            var hit_area = 10;
            var classname = "graphPoint"+i+linecount;
            var areaTag = '<div class="'+classname+
                '" style="background-image:url('+image_placeHolder+');cursor:pointer; position:absolute; top:'+ (getYPixel(v[i].Y) - hit_area/2) + "px; left:" + (getXPixel(yearToInt[v[i].X]) - hit_area/2) +
                "px; width:"+hit_area+"px; height:"+hit_area+'px;"><!--Do not collapse--></div>';
            graph.after(areaTag);
            var content_popup =  "<span class='small bottom'><span class='bold'>" + v[i].X + "</span><br />API: " + v[i].Y + "</span>";
            if(v[i].N != 0){
                content_popup += "<br /><span class='small bottom'>No. tested: "+ v[i].N + "</span>";
            }
            $("."+classname).popover({content: content_popup, placement:'top', delay:{ show: 100, hide: 100 }});
        }
    }
    var drawHorizontalLine = function(c, y, xStart, xEnd, color) {
        c.lineWidth = 1;
        c.strokeStyle = color;
        c.beginPath();
        c.moveTo(xStart, getYPixel(y));
        c.lineTo(xEnd, getYPixel(y));
        c.stroke();
    }

    var drawHorizontalDashedLine = function(c, y, xStart, xEnd, color) {
        c.lineWidth = 1;
        c.beginPath();
        var dashLength = 3;
        var dashSpace = 3;
        var x = xStart;
        var y = y;
        var drawline = true;
        c.moveTo(x, y);

        while(xEnd > x){
            if(drawline){
                x += dashLength;
                c.strokeStyle = color;
                c.lineTo(x, y);
            }
            else{
                x += dashSpace;
                c.moveTo(x, y);
            }
            c.stroke();
            drawline = !drawline;
        }
    }


    var getXPixel = function(val) {
        return ((w - xPadding*3) / (yearXCounter-1)) * val + (xPadding * 2);
    }

    // Return the y pixel for a graph point
    var getYPixel = function(val) {
        return settings['height'] - (((settings['height'] - yPadding) / (settings['y_max']- settings['y_min'])) * (val - settings['y_min'])) - yPadding;
    }

    var xPadding = 50;
    var yPadding = 30;
    var graph = $('#'+settings['layerId']);
    var graphcanvas = document.getElementById(settings['layerId']);
    graph[0].width = settings['width'];
    graph[0].height = settings['height'];
    var canvas_obj = graphcanvas.getContext('2d');

    if(settings['graph_type'] == "line_graph"){
        drawFrameLines(canvas_obj);
        drawFullLineGraph(canvas_obj);
        drawLegend(canvas_obj, data);
    }
    if(settings['graph_type'] == "bar_graph"){
        drawFrameLines(canvas_obj);
        drawBarGraph(canvas_obj);
        drawLegend(canvas_obj, data.values);
    }
};

// School review moderation
function disableReview(reviewId, isTopic) {
    var contentType = isTopic?'topicalSchoolReview':'schoolReview';
    jQuery.post(GS.uri.Uri.getBaseHostname() + '/community/deactivateContent.page', {
        contentId:reviewId,
        contentType:contentType}).done(function() {
            window.location.reload();
        });
}

function enableReview(reviewId, isTopic) {
    var contentType = isTopic?'topicalSchoolReview':'schoolReview';
    jQuery.post(GS.uri.Uri.getBaseHostname () + '/community/deactivateContent.page', {
        contentId:reviewId,
        contentType:contentType,
        reactivate:true}).done(function() {
            window.location.reload();
        });
}


///////////////////////////////////////////////////////////////////////////////////////
// Code for GS-18347

GS.ad = GS.ad || {};

GS.ad.startConsoleLogging = function() {
    var pos = 0;

    (function log(){
        var logItems = googletag.getEventLog().H;
        for (; pos < logItems.length; pos++) {
            console.log(logItems[pos].Xa);
        }
        setTimeout(log, 100);
    })();
};

GS.ad.displayAd = function(slotName) {
    if (slotName === undefined || slotName === null || slotName === '') {
        return;
    }

    var googleSlot = GS.ad.slots[slotName];
    var domId = googleSlot.GS_domId;

    googletag.cmd.push(function() {
        googletag.display(domId);
        googleSlot.GS_displayedYet = true;
    });
};

// called from within AdTagHandler
// Profile page implementation of an ad's GPT display() step. Delay displaying ad if its visibility is conditional
GS.ad.display = function(slotName, domId) {
    if(GS.profile.shouldDelayAd(slotName)) {
        // When existing code goes to refresh the ad, we must first check if it has already been displayed
        GS.ad.slots[slotName].GS_displayedYet = false;
        GS.ad.slots[slotName].GS_domId = domId;

        // the "ad" class attribute is specified in AdTagHandler.java
        $("#"+domId).closest('.ad').parent().hide();
    } else {
        googletag.cmd.push(function() {
            googletag.display(domId);
        });
    }
};
