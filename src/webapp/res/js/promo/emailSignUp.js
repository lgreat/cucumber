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
            wrapper.find('.jq-emailSignUpError-email').show();
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

            jQuery.post('/promo/emailSignUpAjax.page', params, function(data) {
                if (data == "OK") {
                    wrapper.find('.jq-emailSignUpIntroText').hide();
                    wrapper.find('.jq-emailSignUpForm').hide();
                    wrapper.find('.jq-emailSignUpThankYou').show();
                } else {
                    if (data.indexOf('email') > -1) {
                        wrapper.find('.jq-emailSignUpError-email').show();
                    }
                    submitButton.show();
                }
            });
        }

        return false;
    }.email_sign_up_bind(this);
};




jQuery(function() {

    GS.form.emailSignUp = GS.form.emailSignUp || new GS.form.EmailSignUp();

    jQuery('.jq-emailSignUpForm .jq-emailSignUpSubmit').unbind('click').click(function() {
        return GS.form.emailSignUp.submit(jQuery(this));
    });
});
