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
        var cookieName = 'kindercare';
        var dataObject = subCookie.getObject(cookieName);
        // if no cookie, set value to 1 and show hover
        if (dataObject == undefined || dataObject[kindercareCookieKey] == undefined){
            subCookie.setObjectProperty(cookieName, kindercareCookieKey, 1, 9001);
        }
        kindercareFormSubmitted = true;
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
        } else {
            jQuery('#kindercareIntroText').hide();
        }

        return false;
    });

    jQuery('#kindercareLeadGenClose').click(function() {
        if (kindercareFormSubmitted) {
            jQuery('#kindercareLeadGen').hide();
            return false;
        }
        
        var params = {
            schoolId: jQuery('#kinder_schoolId').val(),
            state: jQuery('#kinder_state').val()
        };
        jQuery.post('/school/kindercareLeadGenAjax.page', params, kindercareLeadGenSubmitHandler);
        jQuery('#kindercareLeadGen').hide();

        return false;
    });

    function validateLeadGenForm() {
        jQuery('#kindercareFirstNameError').hide();
        jQuery('#kindercareLastNameError').hide();
        jQuery('#kindercareEmailError').hide();

        passed = true;

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

        return passed;
    }
});
