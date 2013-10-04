var GS = GS || {};
GS.genericTabHandler = (function($){
    var allControlsSelector = '[data-gs-tab-control]';
    var allContentsSelector = '[data-gs-tab-content]';

    var activeTabDataAttribute = 'gs-tab-active';
    var inactiveTabDataAttribute = 'gs-tab-inactive';

    // for all controls on the page, attach the right JS handler
    var init = function(controlContainer, contentContainer) {
        $controlContainer = $(controlContainer);
        $contentContainer = $(contentContainer);

        var $allControls = $controlContainer.find(allControlsSelector);
        var $allContents = $contentContainer.find(allContentsSelector);

        $allControls.each(function() {
            var $control = $(this);
            $control.on('click', function() {
                var tabGroup = $control.data('gs-tab-group');
                var tabName = $(this).data('gs-tab-control');
                switchToTab($allControls, $allContents, tabGroup, tabName);
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
                styleTabInactive($(this));
            } else {
                styleTabActive($(this));
            }
        });

        $contents.filter(selectorForTabGroup(tabGroup)).each(function() {
            var thisTabName = $(this).data('gs-tab-content');
            if (thisTabName !== tabName) {
                styleTabInactive($(this));
            } else {
                styleTabActive($(this));
            }
        });
    };

    var styleTabActive = function($tab) {
        activeClass = $tab.data(activeTabDataAttribute);
        inactiveClass = $tab.data(inactiveTabDataAttribute);
        $tab.addClass(activeClass);
        $tab.removeClass(inactiveClass);
    };

    var styleTabInactive = function($tab) {
        activeClass = $tab.data(activeTabDataAttribute);
        inactiveClass = $tab.data(inactiveTabDataAttribute);
        $tab.addClass(inactiveClass);
        $tab.removeClass(activeClass);
    };


    return {
        init:init
    }

})(jQuery);

jQuery(function() {
    GS.genericTabHandler.init(".tab-control-container", ".tab-content-container");
});




