var GS = GS || {};
GS.tracking = GS.tracking || {};
GS.tracking.customLinks = GS.tracking.customLinks || (function() {

    var init = function() {
        registerDataAttributeHandlers();
    };

    var registerDataAttributeHandlers = function(containerSelector) {
        containerSelector = containerSelector || 'body';

        var customLinkDataAttribute = 'gs-custom-link';
        var customLinkSelector = '[data-' + customLinkDataAttribute + ']';

        var $container = $(containerSelector);

        $container.on('click', customLinkSelector, function() {
            var $this = $(this);
            var track = false;

            if ($this.is(':checkbox')) {
                if ($this.prop('checked') === true) {
                    track = true;
                }
            } else {
                track = true;
            }

            if (track === true) {
                if (s.tl) {
                    s.tl(true, 'o', $this.data(customLinkDataAttribute));
                }
            }
        });
    };

    return {
        init:init,
        registerDataAttributeHandlers:registerDataAttributeHandlers
    }

})();

$(function() {
    GS.tracking.customLinks.init();
});
