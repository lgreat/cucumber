var GS = GS || {};
GS.form = GS.form || {};
GS.form.RequestOtherEditors = function() {
    var preApproveNewEditor = function() {
        hideAllErrors();
        var email = jQuery('#js_email').val();
        var state = jQuery('#js_schoolState').val();
        var schoolId = jQuery('#js_schoolId').val();
        var firstName = jQuery('#js_firstName').val();
        var lastName = jQuery('#js_lastName').val();
        var jobTitle = jQuery('#js_jobTitle').val();

        var isFirstNameValid = validateFirstName(firstName);
        var isLastNameValid = validateLastName(lastName);
        var isEmailValid = validateEmail(email);
        if (isEmailValid && isFirstNameValid && isLastNameValid) {
            jQuery.ajax({
                type: 'POST',
                url: "/official-school-profile/addPreApprovedMemberships.page",
                data: {email:email, state:state, schoolId:schoolId, firstName:firstName, lastName:lastName, jobTitle:jobTitle},
                dataType: 'json',
                async: true
            }).done(
                function(data) {
                    if (data == null) {
                        alert("null response");
                        jQuery('#js_requestOtherEditorsFormInputs').hide(); // added here for testing
                        jQuery('#js_requestOtherEditorsThankYou').show();
                    } else {
                        if (checkEmailForErrors(data)) {
                            jQuery('#js_requestOtherEditorsFormInputs').hide();
                            jQuery('#js_requestOtherEditorsThankYou').show();
                        }

                    }
                }).fail(function() {
                    alert("An error occurred, please try again.");
                }
            );
        }


    };

    var checkEmailForErrors = function(data) {
        var isValid = false;
        if (data.errorCode === 'noEmail' || data.errorCode === 'invalidEmail') {
            jQuery('#js_emailError').html("Please enter a valid email address.");
            jQuery('#js_emailErrorDiv').show();
        } else if (data.errorCode === 'userAlreadyApproved') {
            jQuery('#js_emailError').html("This user is already pre-approved.");
            jQuery('#js_emailErrorDiv').show();
        } else if (data.errorCode === 'userAlreadyPreApproved') {
            jQuery('#js_emailError').html("This user is already approved.");
            jQuery('#js_emailErrorDiv').show();
        } else {
            isValid = true;
        }
        return isValid;
    };

    var validateEmail = function(email) {
        var emailRegex = /^[_a-zA-Z0-9+-]+(\.[_a-zA-Z0-9+-]+)*@[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)*(\.[a-zA-Z]{2,6})$/;
        var errMsg = '';
        if (email.length === 0 || !emailRegex.test(email)) {
            errMsg = 'Please enter a valid email address.';
        }
        if (errMsg !== '') {
            jQuery('#js_emailError').html(errMsg);
            jQuery('#js_emailErrorDiv').show();
            return false;
        }
        return true;
    };

    var validateFirstName = function(firstName) {
        var nameRegex = /[0-9&<>\\]+/g;
        var errMsg = '';
        if (firstName.length === 0 || firstName.length > 24 || firstName.length < 2) {
            errMsg = 'First name must be 2-24 characters long.';
        } else if (firstName.match(nameRegex) != null) {
            errMsg = 'Please remove the numbers or symbols.';
        }
        if (errMsg !== '') {
            jQuery('#js_firstNameError').html(errMsg);
            jQuery('#js_firstNameErrorDiv').show();
            return false;
        }
        return true;
    };

    var validateLastName = function(lastName) {
        var nameRegex = /^[0-9a-zA-Z-_.,&\s]+$/;
        var errMsg = '';
        if (lastName.length === 0 || lastName.length > 24 || lastName.length < 0) {
            errMsg = 'Last name must be 1-24 characters long.';
        } else if (lastName.match(nameRegex) == null) {
            errMsg = 'Last name may contain only letters, numbers, spaces, and the following punctuation:, . - _ &';
        }
        if (errMsg !== '') {
            jQuery('#js_lastNameError').html(errMsg);
            jQuery('#js_lastNameErrorDiv').show();
            return false;
        }
        return true;
    };

    var hideAllErrors = function() {
        jQuery('#js_emailErrorDiv').hide();
        jQuery('#js_firstNameErrorDiv').hide();
        jQuery('#js_lastNameErrorDiv').hide();
    }

    return {
        preApproveNewEditor: preApproveNewEditor,
        hideAllErrors: hideAllErrors
    };
};

GS.form.requestOtherEditors = new GS.form.RequestOtherEditors();

GSType.hover.EspOtherEditors = function() {
    this.loadDialog = function() {
        this.pageName = 'OSP Add/See Accounts';
        this.hier1 = 'ESP';
    };
    this.showHover = function() {
        GSType.hover.espOtherEditors.show();
    };
    this.onClose = function() {
    };
};

GSType.hover.EspOtherEditors.prototype = new GSType.hover.HoverDialog("espOtherEditors", 640);
GSType.hover.espOtherEditors = new GSType.hover.EspOtherEditors();

jQuery(function() {
    GSType.hover.espOtherEditors.loadDialog();

    jQuery('#js_otherEspEditors').click(function() {
            GS.form.requestOtherEditors.hideAllErrors();
            GSType.hover.espOtherEditors.showHover();
        }
    );

    jQuery('#js_requestOtherPreApproval').click(
        GS.form.requestOtherEditors.preApproveNewEditor
    );

    jQuery('#js_requestAnotherEspEditor').click(function() {
            GS.form.requestOtherEditors.hideAllErrors();
            jQuery('#js_requestOtherEditorsFormInputs').show();
            jQuery('#js_requestOtherEditorsThankYou').hide();
        }
    );

});
