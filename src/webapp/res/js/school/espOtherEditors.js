var GS = GS || {};
GS.form = GS.form || {};
GS.form.RequestOtherEditors = function() {
    var MAX_ACCOUNTS = 4;
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
                        alert("An error occurred, please try again.");
                    } else {
                        if (handleErrors(data)) {
                            updateEditorsList(firstName, lastName);
                            jQuery('#js_requestOtherEditorsFormInputs').hide();
                            showSuccess();
                        }

                    }
                }).fail(function() {
                    alert("An error occurred, please try again.");
                }
            );
        }
    };

    var handleErrors = function(data) {
        var isValid = false;
        if (data.errorCode === 'noCookie') {
            alert("Sorry, your cookie has expired. Please sign in and try again.");
        } else if (data.errorCode === 'superUser') {
            alert("OSP Super users are not allowed to use this feature. Please contact the Data Team to pre-approve school officials.");
        } else if (data.errorCode === 'noMembership') {
            alert("Sorry, we could not process your request at this time. Please contact us to suggest an editor for this Official School Profile.");
        } else if (data.errorCode === 'noEmail' || data.errorCode === 'invalidEmail') {
            jQuery('#js_emailError').html("Please enter a valid email address.");
            jQuery('#js_emailErrorDiv').show();
        } else if (data.errorCode === 'userAlreadyApproved') {
            jQuery('#js_emailError').html("This user already has approved access to a school.");
            jQuery('#js_emailErrorDiv').show();
        } else if (data.errorCode === 'userAlreadyPreApproved') {
            jQuery('#js_emailError').html("This user already has approved access to a school.");
            jQuery('#js_emailErrorDiv').show();
        } else {
            isValid = true;
        }
        return isValid;
    };

    var validateEmail = function(email) {
        jQuery('#js_emailErrorDiv').hide();
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
        jQuery('#js_firstNameErrorDiv').hide();
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
        jQuery('#js_lastNameErrorDiv').hide();
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
    };

    var updateEditorsList = function(firstName, lastName) {
        jQuery('#js_espOtherEditorsList').append(
            jQuery('<li></li>').html(firstName + ' ' + lastName + ', <span class="hint">(processing)</span>')
        );
    };

    var showFormAgain = function() {
        hideAllErrors();
        $('#js_requestOtherEditorsFormInputs').find('form')[0].reset(); // reset fields
        jQuery('#js_requestOtherEditorsFormInputs').show();
        jQuery('#js_requestOtherEditorsThankYou').hide();
    };

    var showSuccess = function() {
        jQuery('#js_requestOtherEditorsThankYou').show();
        var numEditors = jQuery('#js_espOtherEditorsList').find('li').length;
        if (numEditors >= MAX_ACCOUNTS) {
            jQuery('#js_requestAnotherEspEditor').hide();
        }
    };

    return {
        preApproveNewEditor: preApproveNewEditor,
        hideAllErrors: hideAllErrors,
        showFormAgain: showFormAgain,
        validateFirstName: validateFirstName,
        validateLastName: validateLastName,
        validateEmail: validateEmail
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

    jQuery('#js_requestAnotherEspEditor').on('click', 'a', GS.form.requestOtherEditors.showFormAgain);

    jQuery('#js_firstName').blur(function() {
            var firstName = jQuery(this).val();
            GS.form.requestOtherEditors.validateFirstName(firstName);
        }
    );

    jQuery('#js_lastName').blur(function() {
            var lastName = jQuery(this).val();
            GS.form.requestOtherEditors.validateLastName(lastName);
        }
    );

    jQuery('#js_email').blur(function() {
            var email = jQuery(this).val();
            GS.form.requestOtherEditors.validateEmail(email);
        }
    );

});
