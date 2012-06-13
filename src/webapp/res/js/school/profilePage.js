GS = GS || {};
GS.school = GS.school || {};
GS.school.profile = GS.school.profile || (function() {

    var init = function() {


        registerEventHandlers();
        setupTabs();

    };

    var registerEventHandlers = function() {

    };

    var setupTabs = function() {
        var $tabGroup = $('[data-gs-tab-group=profileTabs]');
        var $tabBodyGroup = $('[data-gs-tab-body-group=profileTabs]');
        var $allTabBodies = $tabBodyGroup.find('[data-gs-tab-body]');

        $tabGroup.on('click', '[data-gs-tab]', function() {
            var $this = $(this);
            var tab = $this.data('gs-tab');

            var $tabBody = $tabBodyGroup.find('[data-gs-tab-body=' + tab + ']');
            $allTabBodies.hide();
            $tabBody.show();
            $tabGroup.find('li').removeClass('selected');
            $this.addClass('selected');
        });
        // select default tab. This may change depending on URL parameter, or possibly model variable
        $('[data-gs-tab=overview]').addClass('selected');
    };


    return {
        init:init

    }

})();