/* =1 modal loading - can extend to more generic uses - Mitchell Seltzer

 // calling public methods is then as easy as:
 modalLoading.getInstance().showModal();
 modalLoading.getInstance().hideModal();

 -------------------------------------------------------------------------------------------*/

var modalLoading = (function () {
    var instantiated;

    function init() {
        // singleton
        return {
            showModal: function () {
                $modal = $('.js-modal');
                var modalHeight = $modal.outerHeight();
                var horizonOffset = (modalHeight/2)*(-1);
                $modal.css("top",horizonOffset);
                $('.js-overlay').show();
                $('.js-modal').show();
            },
            hideModal: function () {
                $('.js-overlay').hide();
                $('.js-modal').hide();
            }
        };
    }
    return {
        getInstance: function () {
            if ( !instantiated ) {
                instantiated = init();
            }
            return instantiated;
        }
    };
})();