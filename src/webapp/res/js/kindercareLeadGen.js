jQuery(function() {
    jQuery('#kindercareFirstNameError').hide();
    jQuery('#kindercareLastNameError').hide();
    jQuery('#kindercareEmailError').hide();
    jQuery('#kindercareThankyou').hide();

    var kindercareFormSubmitted = false;

    function kindercareLeadGenSubmitHandler(data) {
        jQuery('#kindercareSpinny').hide();
        jQuery('#kindercareLeadGenForm').hide();
        jQuery('#kindercareThankYou').show();
        subCookie.setObjectProperty('kindercare', kindercareCookieKey, 1);
    }

    jQuery('#kindercareLeadGenSubmit').click(function() {
        if (validateLeadGenForm()) {
            jQuery('#kindercareLeadGenSubmit').hide();
            jQuery('#kindercareSpinny').show();
            var params = {
                firstName: jQuery('#kinder_firstname').val(),
                lastName: jQuery('#kinder_lastname').val(),
                email: jQuery('#kinder_email').val(),
                informed: jQuery('#kinder_informed').is(':checked'),
                offers: jQuery('#kinder_offers').is(':checked'),
                schoolId: jQuery('#kinder_schoolId').val(),
                state: jQuery('#kinder_state').val()
            };

            jQuery.post('/school/kindercareLeadGenAjax.page', params, kindercareLeadGenSubmitHandler);
            kindercareFormSubmitted = true;
        }

        return false;
    });

    jQuery('#kindercareLeadGenClose').click(function() {
        if (!kindercareFormSubmitted) {
            kindercareFormSubmitted = true;
            var params = {
                schoolId: jQuery('#kinder_schoolId').val(),
                state: jQuery('#kinder_state').val()
            };
            jQuery.post('/school/kindercareLeadGenAjax.page', params, kindercareLeadGenSubmitHandler);
        }

        jQuery('#kindercareLeadGen').hide();

        return false;
    });

    function validateLeadGenForm() {
        jQuery('#kindercareFirstNameError').hide();
        jQuery('#kindercareLastNameError').hide();
        jQuery('#kindercareEmailError').hide();

        var passed = true;

        if (jQuery('#kinder_firstname').val() == '') {
            jQuery('#kindercareFirstNameError').show();
            passed = false;
        }
        if (jQuery('#kinder_lastname').val() == '') {
            jQuery('#kindercareLastNameError').show();
            passed = false;
        }
        if (jQuery('#kinder_email').val() == '') {
            jQuery('#kindercareEmailError').show();
            passed = false;
        }

        if (!passed) {
            jQuery('#kindercareIntroText').hide();
        } else {
            jQuery('#kindercareIntroText').show();
        }

        return passed;
    }
});
