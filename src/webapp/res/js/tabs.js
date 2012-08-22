var GS = GS || {};

var GS_changeHistory = function(title, url) {
    if (typeof(window.History) !== 'undefined' && window.History.enabled === true) {
        window.History.pushState(null, title, url);
    }
};
GS.util = GS.util || {};
GS.util.jumpToAnchor = function(hash) {
    window.location.hash=hash;
    return false;
};


GS.tabManager = (function() {
    "use strict";

    var tabNamesToTabModules = {};

    var init = function() {
        return this;
    };

    var tabClickHandler = function($a, tabModule) {
        var allowInterceptHovers = tabModule.allowInterceptHovers;
        if (!allowInterceptHovers || !mssAutoHoverInterceptor.onlyCheckIfShouldIntercept('mssAutoHover')) {
            var tabName = $a.parent().data('gs-tab');
            GS.tabManager.showTabWithOptions({
                tab:tabName
            });
            return false;
        }
    };

    var getActiveChildTab = function(tab) {
        if (tab.childGsTabs) {
            return tab.childGsTabs.getCurrentTab();
        }
        return tab;
    };

    var getTabNamesToTabModules = function() {
        return tabNamesToTabModules;
    };

    // allows each suite of tabs to place themselves into manager's map
    var registerTabs = function(tabSuiteName, tabModule) {
        var t;
        var tab;
        var parentTab;
        var parentGsTabs;
        var tabs = tabModule.getTabs();

        if (tabNamesToTabModules.hasOwnProperty(tabSuiteName)) {
            parentGsTabs = tabNamesToTabModules[tabSuiteName];
            if (parentGsTabs !== undefined) {
                tabModule.setParentGsTabs(parentGsTabs);
            }
            parentTab = parentGsTabs.getTabByName(tabSuiteName);
            if (parentTab !== undefined) {
                parentTab.children = tabs;
                parentTab.childGsTabs = tabModule;
                tabModule.setParentTab(parentTab);
            }
        }

        for (t in tabs) {
            if (tabs.hasOwnProperty(t)) {
                tab = tabs[t];
                tabNamesToTabModules[tab.name] = tabModule;
                if (parentTab !== undefined) {
                    tab.parent = parentTab;
                }
            }
        }
    };

    var getTabByName = function(name) {
        var tabModule = tabNamesToTabModules[name];
        return tabModule.getTabByName(name);
    };

    var showTabWithOptions = function(options) {
        var tabObject = options.tab;
        if (typeof tabObject === 'string') {
            tabObject = getTabByName(tabObject);
        }

        if (tabObject === undefined) {
            return false;
        }

        var $a = $(tabObject.selector);

        var tabChanged = tabObject.owner.showTab(tabObject);

        if(options && options.hash !== undefined) {
            GS.util.jumpToAnchor(options.hash);
        }

        if (!options.skipHistory) {
            GS_changeHistory($a.attr('title'), $a.attr('href') );
        }

        if (tabChanged && typeof(window.History) !== 'undefined' && window.History.enabled === true) {
            GS.tracking.sendOmnitureData((getActiveChildTab(tabObject)).name);
        }
    };

    return {
        init:init,
        getTabNamesToTabModules:getTabNamesToTabModules,
        registerTabs:registerTabs,
        showTabWithOptions:showTabWithOptions,
        tabClickHandler:tabClickHandler
    };
}()).init();

GS.Tabs = function(selectorOrContainer, tabSuiteName, options) {
    "use strict";

    var $container;
    if (selectorOrContainer instanceof $) {
        $container = selectorOrContainer;
    } else {
        $container = $(selectorOrContainer);
    }
    var gsTabsSelf = this;

    return (function() {
        var self;
        var $tabNav; // the ul that contains the tabs
        var $tabs; // collection of actual jquery tab objects (in this case 'a' tags)
        var tabs = {}; // tab structure
        var currentTab;
        var parentGsTabs; // this tab suite's parent tab suite, if one exists
        var parentTab; // the parent tab of this entire suite, if one exists
        var allowInterceptHovers = true; // TODO: need this?

        var buildTabStructure = function() {
            $tabs.each(function() {
                var $this = $(this);
                var id = $this.attr('id');
                var tabName = $this.parent().data('gs-tab');
                tabs[tabName] = {
                    name:tabName,
                    selector:'#' +id,
                    children:undefined,
                    owner:self
                };
            });
        };

        // automatically executed
        var init = function() {
            self = this;
            $tabNav = $container.find('ul:first'); // get only the first ul not all of the descendents
            $tabs = $tabNav.find('li>a'); // TODO: update this selector; it matches too many items
            buildTabStructure();
            GS.tabManager.registerTabs(tabSuiteName, self);
            return self;
        };

        var showTabs = function() {
            // TODO: move this
            var allowInterceptHovers = $container.data('gs-allow-intercept-hovers');

            var showHome = $tabNav.find('.selected').length;
            if(!showHome) {
                showFirstTab();
            }

            $tabs.click(function() {
                GS.tabManager.tabClickHandler($(this), self);
                return false;
            });
        };

        var showFirstTab = function() {
            var tabName = $tabs.first().parent().data('gs-tab');
            showTab(getTabByName(tabName));
        };

        var showTab = function(tab) {
            var tabChanged = false;

            if (parentTab) {
                tabChanged = tabChanged || parentTab.owner.showTab(parentTab);
            }

            if (tab !== currentTab) {
                var $a = $(tab.selector);
                var $layers = $tabNav.siblings('div');
                $layers.hide(); // hide all layers
                var tabNum = $tabNav.find('li').index($a.parent());// find reference to the content
                $layers.eq(tabNum).show();// show the content
                $tabNav.find('li a').removeClass('selected');// turn all of them off
                $tabNav.find('li #arrowdiv').removeClass('selected');// turn all of them off
                $a.addClass('selected');// turn selected on
                $a.siblings().addClass('selected');// turn selected on
                tabChanged = true;
            }

            currentTab = tab;

            return tabChanged;
        };

        var getCurrentTab = function() {
            return currentTab;
        };
        var getTabs = function() {
            return tabs;
        };

        var getTabByName = function(str) {
            return tabs[str];
        };
        var getTabBySelector = function(selector) {
            var prop;
            var tab;
            for (prop in tabs) {
                if (tabs.hasOwnProperty(prop)) {
                    tab = tabs[prop];
                    if (tab.selector === selector) {
                        return tab;
                    }
                }
            }
        };

        var setParentGsTabs = function(gsTabs) {
            parentGsTabs = gsTabs;
        };
        var setParentTab = function(tab) {
            parentTab = tab;
        };

        return {
            init:init,
            tabSuiteName:tabSuiteName,
            showTabs:showTabs,
            showTab:showTab,
            getTabs:getTabs,
            getCurrentTab:getCurrentTab,
            getTabByName:getTabByName,
            getTabBySelector:getTabBySelector,
            setParentGsTabs:setParentGsTabs,
            setParentTab:setParentTab,
            allowInterceptHovers:allowInterceptHovers,
            blah:gsTabsSelf.blah
        };
    }()).init();
};

GS.Tabs.prototype.blah = function() {
  alert('yay');
};