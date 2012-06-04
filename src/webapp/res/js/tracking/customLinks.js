var GS = GS || {};
GS.tracking = GS.tracking || {};
GS.tracking.customLinks = GS.tracking.customLinks || (function() {

    var init = function() {
        registerDataAttributeHandlers();
    };

    var registerDataAttributeHandlers = function(containerSelector) {
        containerSelector = containerSelector || '';

        var customLinkDataAttribute = 'gs-custom-link';
        var customLinkSelector = containerSelector + ' [data-' + customLinkDataAttribute + ']';

        var $customLinkElements = $(customLinkSelector);

        $customLinkElements.on('click', function() {
            var $this = $(this);
            var track = false;

            if ($this.is(':checkbox')) {
                if ($this.attr('checked') === true) {
                    track = true;
                }
            } else {
                if (s.tl) {
                    track = true;
                }
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

GS.tracking.customLinks.init();