var GS = GS || {};
GS.tabModules = GS.tabModules || {};

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


GS.tabModules = GS.tabModules || {};
GS.tabManager = (function() {
    "use strict";

    var tabNamesToTabModules = {};

    var init = function() {
        $(function() {
            $('[data-gs-tabs]').gsTabs();
        });

        return this;
    };

    var getTabNamesToTabModules = function() {
        return tabNamesToTabModules;
    };

    // allows each suite of tabs to place themselves into manager's map
    var registerTabs = function(tabSuiteName, tabModule) {
        var t;
        var tab;
        var parentTab;
        var tabs = tabModule.getTabs();

        if (tabNamesToTabModules.hasOwnProperty(tabSuiteName)) {
            parentTab = tabNamesToTabModules[tabSuiteName].getTabByName(tabSuiteName);
            if (parentTab !== undefined) {
                parentTab.children = tabs;
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

    var showTabWithOptions = function(options) {
        var tabObject = options.tab;
        var tabModule;
        if (typeof tabObject === 'string') {
            var tabName = tabObject;
            tabModule = tabNamesToTabModules[tabName];
            if (tabModule === undefined) {
                return false;
            }
            tabObject = tabModule.getTabByName(tabName);
        } else {
            tabModule = tabNamesToTabModules[tabObject.name];
        }

        if (tabObject === undefined) {
            return false;
        }

        try {
            if (tabObject.parent !== undefined) {
                showTabWithOptions({tab:tabObject.parent, skipHistory:true}); // recursion
            }
            tabModule.showTab(tabObject, options.skipHistory);

            if(options.hash !== undefined) {
                GS.util.jumpToAnchor(options.hash);
            }
        } catch (e) {
            // on error, fall back on default click handling
            return true;
        }
        return false;
    };


    var showTab = function(selector) {
        var name = selector.substring(4);
        showTabByName(name);
    };

    var showTabByName = function(tabName) {
        var tabModule = tabNamesToTabModules[tabName];
        tabModule.showTab(tabModule.getTabByName(tabName));
    };

    return {
        init:init,
        getTabNamesToTabModules:getTabNamesToTabModules,
        showTabByName:showTabByName,
        showTab:showTab,
        registerTabs:registerTabs,
        showTabWithOptions:showTabWithOptions
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

    return (function() {
        var self;
        var $tabNav; // the ul that contains the tabs
        var $tabs; // collection of actual jquery tab objects (in this case 'a' tags)
        var tabs = {}; // tab structure
        var currentTab;

        var buildTabStructure = function() {
            $tabs.each(function() {
                var $this = $(this);
                var id = $this.attr('id');
                var tabName = $this.parent().data('gs-tab');
                tabs[tabName] = {
                    name:tabName,
                    selector:'#' +id,
                    children:undefined
                };
            });
        };

        // automatically executed
        var init = function() {
            self = this;
            GS.tabModules[tabSuiteName] = this;
            $tabNav = $container.find('ul:first'); // get only the first ul not all of the descendents
            $tabs = $tabNav.find('li>a'); // TODO: update this selector; it matches too many items
            buildTabStructure();
            GS.tabManager.registerTabs(tabSuiteName, self);
            console.log(tabs);
            return self;
        };

        var showTabs = function() {

            // TODO: move this
            var allowInterceptHovers = $container.data('gs-allow-intercept-hovers');

            var showHome = $tabNav.find('.selected').length;
            if(!showHome) {
                $tabNav.find('li:first a').addClass('selected').siblings().addClass('selected');
                $container.children('div:first').show();
            }
            $tabNav.find('li').each(function(){
                $(this).find('a').click(function(){ //When any link is clicked
                    if (!allowInterceptHovers || !mssAutoHoverInterceptor.onlyCheckIfShouldIntercept('mssAutoHover')) {
                        showTab('#' + $(this).attr('id'));
                        return false;
                    }
                });
            });
        };


        var showTab = function(selectorOrTab, skipHistory, options) {
            var tab;
            if (selectorOrTab.hasOwnProperty('selector')) {
                tab = selectorOrTab;
            } else {
                tab = getTabBySelector(selectorOrTab);
            }

            var $a = $(tab.selector);

            styleTabAsShown($a);

            if(options && options.hash !== undefined) {
                GS.util.jumpToAnchor(options.hash);
            }

            currentTab = tab;


            if (!skipHistory) {
                GS_changeHistory($a.attr('title'), $a.attr('href') );
            }
        };

        var styleTabAsShown = function($a) {
            var $layers = $tabNav.siblings('div');
            $layers.hide(); // hide all layers
            var tabNum = $tabNav.find('li').index($a.parent());// find reference to the content
            $layers.eq(tabNum).show();// show the content
            $tabNav.find('li a').removeClass('selected');// turn all of them off
            $tabNav.find('li #arrowdiv').removeClass('selected');// turn all of them off
            $a.addClass('selected');// turn selected on
            $a.siblings().addClass('selected');// turn selected on
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

        return {
            init:init,
            showTabs:showTabs,
            showTab:showTab,
            getTabs:getTabs,
            getCurrentTab:getCurrentTab,
            getTabByName:getTabByName,
            getTabBySelector:getTabBySelector
        };
    }()).init();
};
