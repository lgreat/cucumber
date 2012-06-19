/* =1 modal loading - can extend to more generic uses - Mitchell Seltzer

 // calling public methods is then as easy as:
showModal();
hideModal();

 -------------------------------------------------------------------------------------------*/

(function( $ ){
    var instantiated;
    var $modalOverlay = $('.js-overlay');
    var $modal = $('.js-modal');

    function showModalLayer( options ){
        var settings = $.extend( {
            'layerId' : '',
            'containerId' : 'document',
            'overlay' : 'true',
            'content' : '',
            'templateId' : '0',
            'closeButton' : 'false'
        }, options);
        alert(settings.layerId);
//        var modalHeight = $modal.outerHeight();
//        var horizonOffset = (modalHeight/2)*(-1);
//        $modal.css("top",horizonOffset);
//        $modalOverlay.show();
//        $modal.show();
    }

    function hideModalLayer( options ){
        var settings = $.extend( {
            'layerId' : '',
            'containerId' : 'document'
        }, options);
//        $modalOverlay.hide();
//        $modal.hide();
    }

    function init() {
        // singleton
        return {
            showModal: showModalLayer( options ),
            hideModal: hideModalLayer( options )
        };
    }

    function getInstance() {
        if ( !instantiated ) {
            instantiated = init();
        }
        return instantiated;
    }

    return {
        showModal: function( options ) {
            getInstance().showModal( options );
        },
        hideModal: function( options ) {
            getInstance().hideModal( options );
        }
    };
})( jQuery );
