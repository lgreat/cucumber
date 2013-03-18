jQuery.noConflict();
var $j = jQuery;
Function.prototype.email_sign_up_bind = function(obj) {
    var method = this;
    return function() {
        return method.apply(obj, arguments);
    };
};

var GS = GS || {};
GS.form = GS.form || {};
GS.form.EmailSignUp = function() {

    this.validate = function(wrapper) {
        wrapper.find('.jq-emailSignUpError').hide();

        var passed = true;

        if (jQuery('.jq-emailSignUpEmail').val() == '') {
            jQuery('.jq-emailSignUpError-emailInvalid').show();
            passed = false;
        }

        return passed;
    };

    this.submit = function(submitButton) {
        var wrapper = submitButton;//submitButton.closest('.js-connectForm');

        if (this.validate(wrapper)) {
//            submitButton.hide();
            var params = {
                email: wrapper.find('.jq-emailSignUpEmail').val()
            };

            var urlToUse =  window.location.protocol + "//" + window.location.hostname + "/promo/emailSignUpAjax.page";
            jQuery.ajax({
                url:urlToUse,
                type:"POST",
                data:params,
                success:function(data) {

                    if (data.status.indexOf('OK') > -1) {
                        jQuery('.js-connectForm').hide();
                        jQuery('.jq-emailSignUpIntroText').hide();
                        jQuery('.jq-emailSignUpForm').hide();
                        if (data.status == 'OK-emailSent') {
                            jQuery('.jq-emailSignUpEmailSentThankYou').show();
                        } else {
                            if (data.omnitureTracking != undefined) {
                                omnitureEventNotifier.clear();
                                omnitureEventNotifier.successEvents = data.omnitureTracking.successEvents;
                                omnitureEventNotifier.send();
                            }
                            jQuery('.jq-emailSignUpNoEmailSentThankYou').show();
                        }
                    } else {
                        if (data.errors.indexOf('emailInvalid') > -1) {
                            jQuery('.jq-emailSignUpError-emailInvalid').show();
                        } else if (data.errors.indexOf('emailAlreadySignedUp') > -1) {
                            jQuery('.jq-emailSignUpError-emailAlreadySignedUp').show();
                        }
                        submitButton.show();
                    }
                },
                dataType:"json"
            });
        }

        return false;
    };
};



GS.form.emailSignUp = GS.form.emailSignUp || new GS.form.EmailSignUp();
jQuery('.jq-emailSignUpForm').on('submit', function() {
    return GS.form.emailSignUp.submit(jQuery(this));
});