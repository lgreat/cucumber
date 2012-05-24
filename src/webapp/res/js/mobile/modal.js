/* =1 modal loading - can extend to more generic uses - Mitchell Seltzer

 // calling public methods is then as easy as:
 modal.showModal();
 modal.hideModal();

 -------------------------------------------------------------------------------------------*/

define(function() {
    var instantiated;

    function init() {
        // singleton
        var $modalOverlay = $('.js-overlay');
        var $modal = $('.js-modal');
        return {
            showModal: function () {
                var modalHeight = $modal.outerHeight();
                var horizonOffset = (modalHeight/2)*(-1);
                $modal.css("top",horizonOffset);
                $modalOverlay.show();
                $modal.show();
            },
            hideModal: function () {
                $modalOverlay.hide();
                $modal.hide();
            }
        };
    }
    function getInstance() {
        if ( !instantiated ) {
            instantiated = init();
        }
        return instantiated;
    }
    return {
        showModal: function() {
            getInstance().showModal();
        },
        hideModal: function() {
            getInstance().hideModal();
        }
    };
});
