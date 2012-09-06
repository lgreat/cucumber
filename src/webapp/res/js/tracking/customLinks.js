var GS = GS || {};
GS.tracking = GS.tracking || {};
GS.tracking.customLinks = GS.tracking.customLinks || (function() {

    var init = function() {
        registerDataAttributeHandlers();
//        registerDataAttributeHandlersWithDelay();
    };

    var registerDataAttributeHandlers = function(containerSelector) {
        containerSelector = containerSelector || 'body';

        var customLinkDataAttribute = 'gs-custom-link';
        var customLinkSelector = '[data-' + customLinkDataAttribute + ']';

        var $container = $(containerSelector);

        $container.on('click', customLinkSelector, function() {
            var $this = $(this);

            if (s.tl) {
                if($this.hasClass('jq-school-type') || $this.hasClass('jq-grade-level') || $this.hasClass('js-advFilters')) {
                    if($this.is(':checked')) {
                        s.tl(true,'o', $this.data(customLinkDataAttribute) + '_check');
                    }
                    else {
                        s.tl(true,'o', $this.data(customLinkDataAttribute) + '_uncheck');
                    }
                }
                else {
                    s.tl(true, 'o', $this.data(customLinkDataAttribute));
                }
            }
        });
    };

    /*var registerDataAttributeHandlersWithDelay = function(containerSelector) {
        containerSelector = containerSelector || 'body';

        var customLinkDataAttribute = 'gs-custom-link';
        var customLinkSelector = '[data-' + customLinkDataAttribute + ']';

        var $container = $(containerSelector);

        $container.on('click', customLinkSelector, function() {
            var $this = $(this);
            var track = false;

            if ($this.is(':checkbox') || $this.is(':radio')) {
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
    };*/

    var registerCustomLink = function(containerSelector, elementSelector, linkNamePrefix) {
        containerSelector = containerSelector || 'body';

        var $container = $(containerSelector);

        $container.on('click', elementSelector, function() {
            var $this = $(this);
            var track = false;

            if ($this.is(':checkbox') || $this.is(':radio')) {
                if ($this.prop('checked') === true) {
                    track = true;
                }
            } else {
                track = true;
            }

            if (track === true) {
                if (s.tl) {
                    var prefix = linkNamePrefix || $this.attr('name');
                    var value = $this.val();
                    s.tl(true, 'o', prefix + '_' + value);
                }
            }
        });
    };

    return {
        init:init,
        registerDataAttributeHandlers:registerDataAttributeHandlers,
        registerCustomLink:registerCustomLink
    }

})();

$(function() {
    GS.tracking.customLinks.init();
});
