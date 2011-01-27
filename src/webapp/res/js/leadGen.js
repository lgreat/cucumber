jQuery(function() {
    jQuery('#leadGenFirstNameError').hide();
    jQuery('#leadGenLastNameError').hide();
    jQuery('#leadGenEmailError').hide();
    jQuery('#leadGenThankyou').hide();

    var primroseFormSubmitted = false;

    function primroseLeadGenSubmitHandler(data) {
        jQuery('#leadGenSpinny').hide();
        jQuery('#leadGenForm').hide();
        jQuery('#leadGenThankYou').show();
    }

    jQuery('#leadGenSubmit').click(function() {
        if (validateLeadGenForm()) {
            jQuery('#leadGenSubmit').hide();
            jQuery('#leadGenSpinny').show();
            var params = {
                firstName: jQuery('#leadGenFirstName').val(),
                lastName: jQuery('#leadGenLastName').val(),
                email: jQuery('#leadGenEmail').val()
            };

            jQuery.post('/promo/primroseLeadGenAjax.page', params, primroseLeadGenSubmitHandler);
            primroseFormSubmitted = true;
        }

        return false;
    });

    function validateLeadGenForm() {
        jQuery('#leadGenFirstNameError').hide();
        jQuery('#leadGenLastNameError').hide();
        jQuery('#leadGenEmailError').hide();

        var passed = true;

        if (jQuery('#leadGenFirstName').val() == '') {
            jQuery('#leadGenFirstNameError').show();
            passed = false;
        }
        if (jQuery('#leadGenFirstName').val() == '') {
            jQuery('#leadGenLastNameError').show();
            passed = false;
        }
        if (jQuery('#leadGenEmail').val() == '') {
            jQuery('#leadGenEmailError').show();
            passed = false;
        }

        if (!passed) {
            jQuery('#leadGenIntroText').hide();
        } else {
            jQuery('#leadGenIntroText').show();
        }

        return passed;
    }
});
