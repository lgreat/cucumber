// JavaScript Document

jQuery(document).ready(function() {
    //
    // extend the core jQuery with disableSelection
    jQuery.fn.extend({
        disableSelection : function() {
            this.each(function() {
                this.onselectstart = function() {
                    return false;
                };
                this.unselectable = "on";
                jQuery(this).css('-moz-user-select', 'none');
                jQuery(this).css('-webkit-user-select', 'none');
                jQuery(this).css('-khtml-user-select', 'none');
            });
        }
    });
    //
    // disable selection of button-1 class
    jQuery('.button-1').disableSelection();
    // disable selection of button-1-inactive class
    jQuery('.button-1-inactive').disableSelection();
});