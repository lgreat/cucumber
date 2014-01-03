var GS = GS || {};
GS.genericTabHandler = (function($){
    var allControlsSelector = '[data-gs-tab-control]';
    var allContentsSelector = '[data-gs-tab-content]';
    var tabs = {};
    var isHistoryAPIAvailable = (typeof(window.History) !== 'undefined' && window.History.enabled === true);

    // for all controls on the page, attach the right JS handler
    var init = function(controlContainer, contentContainer) {
        var $controlContainer = $(controlContainer);
        var $contentContainer = $(contentContainer);

        var $allControls = $controlContainer.find(allControlsSelector);
        var $allContents = $contentContainer.find(allContentsSelector);

        $allControls.each(function() {
            var $control = $(this);
            var tabGroup = $control.data('gs-tab-group');
            var tabName = $control.data('gs-tab-control');
            tabs[tabName] = {tabControl: $control, tabGroup: tabGroup};

            // handle nav when tab is clicked
            $control.on('click', function() {
                switchToTab($allControls, $allContents, tabGroup, tabName);
                if(isHistoryAPIAvailable) {
                    return false;
                }
                return true;
            });
        });

        // when browser back button is clicked, the statuschange is used to handle navigation
        if(isHistoryAPIAvailable) {
            window.History.Adapter.bind(window, 'statechange', function() {
                var state = History.getState();
                if (state && state.url) {
//                    var tab = 'Preschools';
                    if (state.url.indexOf('?') > -1) {
                        var queryString = state.url.substr(state.url.indexOf('?') + 1);
                        if (queryString.indexOf('#') > -1) {
                            queryString = queryString.substr(0, queryString.indexOf('#'));
                        }
                        tab = GS.uri.Uri.getFromQueryString('tab', queryString) || tab;
                    }
                    if (tab && tabs[tab]) {
                        switchToTab($allControls, $allContents, tabs[tab].tabGroup, tab);
                    }
                }
            });
        }

        for(var t in tabs) {
            if(tabs.hasOwnProperty(t)) {
                if(tabs[t].tabControl.hasClass('selected')) {
                    updateHistoryEntryWithCurrentTab(t);
                }
            }
        }

        return this;
    };

    var selectorForTabGroup = function(tabGroup) {
        return '[data-gs-tab-group=' +  tabGroup +']';
    };

    var switchToTab = function($controls, $contents, tabGroup, tabName) {
        $controls.filter(selectorForTabGroup(tabGroup)).each(function() {
            var thisTabName = $(this).data('gs-tab-control');
            if (thisTabName !== tabName) {
                $(this).removeClass("selected");
            } else {
                $(this).addClass("selected");
                var url = '?';
//                if(tabName !== 'Preschools')
                url += 'tab=' + tabName;
                window.History.pushState(null, null, url);
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
        window.location.reload();

    };

    var updateHistoryEntryWithCurrentTab = function(currentTabName) {
        if (isHistoryAPIAvailable) {
//            var isPreschoolsTab = (currentTabName === 'Preschools');
            var queryString = window.location.search;
//            if (isPreschoolsTab) {
//                queryString = GS.uri.Uri.removeFromQueryString(queryString, "tab");
//            }
//            else {
                queryString = GS.uri.Uri.putIntoQueryString(queryString, "tab", currentTabName, true);
//            }
            window.History.replaceState(null, null, queryString);
        }
    };


    return {
        init:init
    }

})(jQuery);

jQuery(function() {
    GS.genericTabHandler.init(".tab-control-container", ".tab-content-container");
});




