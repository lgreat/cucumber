Function.prototype.email_sign_up_bind = function(obj) {
    var method = this;
    return function() {
        return method.apply(obj, arguments);
    };
};

GS = GS || {};
GS.form = GS.form || {};
GS.form.EmailSignUp = function() {

    this.validate = function(wrapper) {
        wrapper.find('.jq-emailSignUpError').hide();

        var passed = true;

        if (wrapper.find('.jq-emailSignUpEmail').val() == '') {
            wrapper.find('.jq-emailSignUpError-emailInvalid').show();
            passed = false;
        }

        return passed;
    };

    this.submit = function(submitButton) {
        var wrapper = submitButton.closest('.jq-emailSignUpWrapper');

        if (this.validate(wrapper)) {
            submitButton.hide();
            var params = {
                email: wrapper.find('.jq-emailSignUpEmail').val()
            };

            jQuery.ajax({
                url:'/promo/emailSignUpAjax.page',
                type:"POST",
                data:params,
                success:function(data) {
                    if (data.status.indexOf('OK') > -1) {
                        wrapper.find('.js-connectForm').hide();
                        wrapper.find('.jq-emailSignUpIntroText').hide();
                        wrapper.find('.jq-emailSignUpForm').hide();
                        if (data.status == 'OK-emailSent') {
                            wrapper.find('.jq-emailSignUpEmailSentThankYou').show();
                        } else {
                            if (data.omnitureTracking != undefined) {
                                omnitureEventNotifier.clear();
                                omnitureEventNotifier.successEvents = data.omnitureTracking.successEvents;
                                omnitureEventNotifier.send();
                            }
                            wrapper.find('.jq-emailSignUpNoEmailSentThankYou').show();
                        }
                    } else {
                        if (data.errors.indexOf('emailInvalid') > -1) {
                            wrapper.find('.jq-emailSignUpError-emailInvalid').show();
                        } else if (data.errors.indexOf('emailAlreadySignedUp') > -1) {
                            wrapper.find('.jq-emailSignUpError-emailAlreadySignedUp').show();
                        }
                        submitButton.show();
                    }
                },
                dataType:"json"
            });
        }

        return false;
    }.email_sign_up_bind(this);
};


jQuery(function() {

    GS.form.emailSignUp = GS.form.emailSignUp || new GS.form.EmailSignUp();

    jQuery('.jq-emailSignUpForm').unbind('submit').submit(function() {
        return GS.form.emailSignUp.submit(jQuery(this));
    });
//    jQuery('.jq-emailSignUpFormFooter').unbind('submit').submit(function() {
//        return GS.form.emailSignUp.submit(jQuery(this));
//    });
//    jQuery('.jq-emailSignUpFormFooter').on('submit')(function() {
//        return GS.form.emailSignUp.submit(jQuery(this));
//    });
//    jQuery('.jq-emailSignUpFormFooter').on("submit", function(event){
//        GS.form.emailSignUp.submit(jQuery(this));
//    });
});
