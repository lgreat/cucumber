/* =1 disableSelection (disable text selection)
 -------------------------------------------------------------------------------------------*/

(function($) {
    // extend the core jQuery with disableSelection
    $.fn.extend({
        modalBox : function() {
            return this.each(function() {
                // PUBLIC functions
                this.showLayer = function() {
                    consol.log("ShowLayer");
                    $modal = this.('.js-modal');
                    var modalHeight = $modal.outerHeight();
                    var horizonOffset = (modalHeight/2)*(-1);
                    $modal.css("top",horizonOffset);
                    $('.js-overlay').show();
                    $('.js-modal').show();
                };

                this.hideLayer = function() {
                    consol.log("HideLayer");
                    this.('.js-overlay').hide();
                    this.('.js-modal').hide();
                };
            });
        }
    });
})(jQuery);