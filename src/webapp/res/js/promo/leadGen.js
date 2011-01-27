Function.prototype.lead_gen_bind = function(obj) {
    var method = this;
    return function() {
        return method.apply(obj, arguments);
    };
};

GS = GS || {};
GS.form = GS.form || {};
GS.form.LeadGenCampaign = function() {

    this.validate = function(wrapper) {
        wrapper.find('.jq-leadGenError').hide();

        var passed = true;

        if (wrapper.find('.jq-leadGenFirstName').val() == '') {
            wrapper.find('.jq-leadGenError').show();
            passed = false;
        }
        if (wrapper.find('.jq-leadGenFirstName').val() == '') {
            wrapper.find('.jq-leadGenError').show();
            passed = false;
        }
        if (wrapper.find('.jq-leadGenEmail').val() == '') {
            wrapper.find('.jq-leadGenError').show();
            passed = false;
        }

        return passed;
    };

    this.submit = function(submitButton) {
        var wrapper = submitButton.closest('.jq-leadGenWrapper');

        if (this.validate(wrapper)) {
            submitButton.hide();
            var params = {
                campaign: wrapper.find('.jq-leadGenCampaign').val(),
                firstName: wrapper.find('.jq-leadGenFirstName').val(),
                lastName: wrapper.find('.jq-leadGenLastName').val(),
                email: wrapper.find('.jq-leadGenEmail').val()
            };

            jQuery.post('/promo/leadGenAjax.page', params, function(data) {
                if (data == "0") {
                    wrapper.find('.jq-leadGenError').show();
                    submitButton.show();
                } else {
                    wrapper.find('.jq-leadGenForm').hide();
                    wrapper.find('.jq-leadGenThankYou').show();
                }
            });
        }

        return false;
    }.lead_gen_bind(this);
};




jQuery(function() {

    GS.form.leadGenCampaign = GS.form.leadGenCampaign || new GS.form.LeadGenCampaign();

    jQuery('.jq-leadGenForm .jq-leadGenSubmit').unbind('click').click(function() {
        return GS.form.leadGenCampaign.submit(jQuery(this));
    });
});
