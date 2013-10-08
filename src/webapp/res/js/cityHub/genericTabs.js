var GS = GS || {};
GS.genericTabHandler = (function($){
    var allControlsSelector = '[data-gs-tab-control]';
    var allContentsSelector = '[data-gs-tab-content]';
    var $tabs = {};

    var activeTabDataAttribute = 'gs-tab-active';
    var inactiveTabDataAttribute = 'gs-tab-inactive';
    var isHistoryAPIAvailable = (typeof(window.History) !== 'undefined' && window.History.enabled === true);

    // for all controls on the page, attach the right JS handler
    var init = function(controlContainer, contentContainer) {
        var $controlContainer = $(controlContainer);
        var $contentContainer = $(contentContainer);

        var $allControls = $controlContainer.find(allControlsSelector);
        var $allContents = $contentContainer.find(allContentsSelector);

        $allControls.each(function() {
            var $control = $(this);
            $tabs[$control.data('gs-tab-control')] = {name: $control};
            $control.on('click', function() {
                var tabGroup = $control.data('gs-tab-group');
                var tabName = $(this).data('gs-tab-control');
                switchToTab($allControls, $allContents, tabGroup, tabName);
                return false;
            });
        });
    };

    var selectorForTabGroup = function(tabGroup) {
        return '[data-gs-tab-group=' +  tabGroup +']';
    };

    var switchToTab = function($controls, $contents, tabGroup, tabName) {
        $controls.filter(selectorForTabGroup(tabGroup)).each(function() {
            var thisTabName = $(this).data('gs-tab-control');
            if (thisTabName !== tabName) {
                $(this).removeClass("selected")
            } else {
                $(this).addClass("selected")

                updateHistoryEntryWithCurrentTab($(this));
            }
        });

        $contents.filter(selectorForTabGroup(tabGroup)).each(function() {
            var thisTabName = $(this).data('gs-tab-content');
            if (thisTabName !== tabName) {
                $(this).addClass("dn");
                $(this).removeClass("db");
            } else {
                $(this).addClass("db");
                $(this).removeClass("dn");
            }
        });
    };
   var updateHistoryEntryWithCurrentTab = function($currentTab) {
        var isPreschoolsTab = ($currentTab.data('gs-tab-control') === 'Preschools');
        if (isHistoryAPIAvailable) {
            var queryString = window.location.search;
            if (isPreschoolsTab) {
                queryString = GS.uri.Uri.putIntoQueryString(queryString, "tab", $currentTab.data('gs-tab-control'), true);
            }
            else {
                queryString = GS.uri.Uri.removeFromQueryString(queryString, "tab");
            }
            var state = {tabName : $tabs[$currentTab.data('gs-tab-control')]};
            window.History.pushState(state, null, queryString);
        }
        else {
            var anchorVal = '';
            if(!isPreschoolsTab) {
                anchorVal = "/" + $currentTab.data('gs-tab-control');
            }
            window.location.hash = "!" + anchorVal;
        }
    };


    return {
        init:init
    }

})(jQuery);

jQuery(function() {
    GS.genericTabHandler.init(".tab-control-container", ".tab-content-container");
});




