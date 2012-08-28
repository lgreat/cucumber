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
    var originalPageTitle;
    var historyAPIAvailable;
    var allTabs;

    var init = function() {
        historyAPIAvailable = (typeof(window.History) !== 'undefined' && window.History.enabled === true);

        $(function() {
            originalPageTitle = document.title;
        });
        return this;
    };

    var tabClickHandler = function($a, tabModule) {
        var allowInterceptHovers = tabModule.allowInterceptHovers;
        if (typeof mssAutoHoverInterceptor === 'undefined' || !allowInterceptHovers || !mssAutoHoverInterceptor.onlyCheckIfShouldIntercept('mssAutoHover')) {
            var tabName = getTabName($a);
            GS.tabManager.showTabWithOptions({
                tab:tabName
            });
            return false;
        }
    };

    /**
     * Get active leaf tab
     *
     * TODO: currently only works for tabs on profile page. Make this more generic. What if there are >1
     * root gsTab suites on the page?
     */
    var getCurrentTab = function(rootGsTabs) {
        var leafTab;
        rootGsTabs = rootGsTabs || tabNamesToTabModules.overview; // assume root tab suite is owner of overview tab

        leafTab = rootGsTabs.getCurrentTab();

        if (leafTab.childGsTabs) {
            return getCurrentTab(leafTab.childGsTabs); // recursion
        }
        return leafTab;
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

        $.extend(allTabs, tabs);
    };

    var getTabByName = function(name) {
        var tabModule = tabNamesToTabModules[name];
        return tabModule.getTabByName(name);
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

    var showTabWithOptions = function(options) {
        var tabObject = options.tab;
        if (typeof tabObject === 'string') {
            tabObject = getTabByName(tabObject);
        }

        if (tabObject === undefined) {
            return false;
        }

        var activeChildTab = getActiveChildTab(tabObject);
        var $a = $(tabObject.selector);

        var tabChanged = tabObject.owner.showTab(tabObject);

        if (!options.skipHistory) {
            GS_changeHistory(getUpdatedTitle(activeChildTab.title), $a.attr('href') );
        }

        if(options && options.hash !== undefined) {
            GS.util.jumpToAnchor(options.hash);
        }

        if (tabChanged && historyAPIAvailable) {
            GS.tracking.sendOmnitureData(activeChildTab.name);
            GS_notifyQuantcastComscore();
        }
    };

    // util method - returns tab name given a jquery object of a link
    var getTabName = function($a) {
        return $a.parent().data('gs-tab');
    };

    var updateHistoryEntryWithCurrentTab = function() {
        if (historyAPIAvailable) {
            var currentTab = getCurrentTab();
            if (currentTab !== getTabByName('overview')) {
                window.History.replaceState(null, getUpdatedTitle(currentTab.title), null);
            }
        }
    };

    return {
        init:init,
        getTabNamesToTabModules:getTabNamesToTabModules,
        registerTabs:registerTabs,
        showTabWithOptions:showTabWithOptions,
        tabClickHandler:tabClickHandler,
        getTabName:getTabName,
        getCurrentTab:getCurrentTab,
        getTabByName:getTabByName,
        getUpdatedTitle:getUpdatedTitle,
        updateHistoryEntryWithCurrentTab:updateHistoryEntryWithCurrentTab
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
                var tabName = GS.tabManager.getTabName($this);
                var title = $this.attr('title');
                tabs[tabName] = {
                    name:tabName,
                    selector:'#' +id,
                    children:undefined,
                    title:title,
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

            // set up click handler for tabs
            $tabs.click(function() {
                GS.tabManager.tabClickHandler($(this), self);
                return false;
            });

            return self;
        };

        var showTabs = function() {
            // TODO: move this
            var allowInterceptHovers = $container.data('gs-allow-intercept-hovers');

            var showHome = $tabs.filter('.selected').length;
            if(!showHome) {
                showFirstTab();
            } else {
                var tabName = GS.tabManager.getTabName($tabs.filter('.selected').first());
                currentTab = getTabByName(tabName);
            }
        };

        var showFirstTab = function() {
            var tabName = GS.tabManager.getTabName($tabs.first());
            showTab(getTabByName(tabName), {
                propagate:false
            });
        };

        var showTab = function(tab, options) {
            var tabChanged = false;
            options = options || {};

            if (parentTab && options.propagate !== false) {
                tabChanged = parentTab.owner.showTab(parentTab) || tabChanged;
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