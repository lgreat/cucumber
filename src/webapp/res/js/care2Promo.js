jQuery(function() {

    function setCare2Cookie() {
        subCookie.setObjectProperty('care2', care2CookieKey, 1, 9001);
    }

    jQuery('#care2PromoClose').click(function() {
        setCare2Cookie();
        jQuery('#care2Promo').hide();
        return false;
    });
});
