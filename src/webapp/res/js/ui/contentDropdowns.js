var GS = GS || {};
GS.ui = GS.ui || {};

// content dropdown = a block of markup that is wrapped in a container, and can be shown/hidden by clicking
// an opener (such as a down arrow) or closed (such as with an A tag )
GS.ui.contentDropdowns = GS.ui.contentDropdowns || (function() {

    var containerSelector = 'body';

    var init = function() {
        registerContentOpeners(containerSelector);
        registerContentHiders(containerSelector);
    };

    var registerContentOpeners = function(containerSelector) {
        $(containerSelector).on('click', '[data-gs-dropdown-opener]', function() {
            var $this = $(this);
            var dropdownId = $this.data('gs-dropdown-opener');
            var dropdownSelector = '#' + dropdownId;

            var $content = $('[data-gs-content=' + dropdownId + ']');
            $content.toggle();
            $this.closest(' .dropDown-default').toggle();
            $this.closest(' .dropDown-hover').toggle();


            // save any checkboxes within. This is filter-specific code
            var $checkedCheckboxes = $content.find('input:checked');
            var $uncheckedCheckboxes = $content.find('input:not(:checked)');

            if ($content.is(':visible')) {
                $('html').on('click.gs.visibility.content.' + dropdownId, function(event) {
                    if ($(event.target).closest(dropdownSelector).length == 0) {
                        $content.hide();
                            $(dropdownSelector +' .dropDown-default').hide();
                            $(dropdownSelector + ' .dropDown-hover').show();
                            if ($content.find('button.js-applyFilters').size() > 0) { // condition is hack so only checkboxes that require apply button can get undone
                                $checkedCheckboxes.prop('checked',true).trigger('change');
                                $uncheckedCheckboxes.prop('checked',false).trigger('change');
                            }
                        $('html').unbind('click.gs.visibility.content.' + dropdownId);
                    }
                });
            }

            // reset checkboxes with close button is clicked
            $(containerSelector + ' [data-gs-undo-filters=' + dropdownId + ']').on('click', function() {
                $checkedCheckboxes.prop('checked',true).trigger('change');
                $uncheckedCheckboxes.prop('checked',false).trigger('change');
            });
        });
    };

    var registerContentHiders = function(containerSelector) {
        $(containerSelector).on('click', '[data-gs-dropdown-hider]', function() {
            var $this = $(this);
            var targetId = $this.data('gs-dropdown-hider');
            $(containerSelector + ' [data-gs-content=' + targetId + ']').hide();
        });
    };

    //filter specific code is here:

    // dynamically update a label that summarizes how many filters within a group are checked
    // (currently All or Some)
    var setupFilterStatusWatchers = function(containerSelector) {
        var $container = $(containerSelector);
        var prefix = '';
        $container.find('[data-gs-filter-status]').each(function() {
            // get the element marked with gs-filter-status
            // then get the matching gs-content element
            // count all checkboxes within the content element to figure out the prefix to use
            // update the value of the filter status element
            var $filterStatus = $(this);
            var contentId = $filterStatus.data('gs-filter-status');
            var $checkboxes = $container.find('[data-gs-content=' + contentId + '] input:checkbox');
            var $radios = $container.find('[data-gs-content=' + contentId + '] input:radio');
            $checkboxes.on('change', function() {
                prefix = 'Some';
                if($checkboxes.filter(':checked').length === 0 || $checkboxes.not(':checked').length === 0)   {
                    prefix = 'All';
                }
                var newValue = $filterStatus.html();
                newValue = newValue.replace(/(^)(All|Some|No)($|\W)(.*)/,'$1' +  prefix + '$3$4');
                $filterStatus.html(newValue);
            });
            $checkboxes.trigger('change');

            $radios.on('change', function() {
                $filterStatus.html($radios.filter(':checked').parent().find('label').html());
            });
            if ($radios.length > 0) {
                $radios.filter(':checked').trigger('change');
            }
        });
    };

    return {
        init:init,
        registerContentOpeners:registerContentOpeners,
        registerContentHiders:registerContentHiders,
        setupFilterStatusWatchers:setupFilterStatusWatchers
    }
})();
