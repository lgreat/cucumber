Function.prototype.lead_gen_bind = function(obj) {
    var method = this;
    return function() {
        return method.apply(obj, arguments);
    };
};

GS = GS || {};
GS.form = GS.form || {};
GS.form.LeadGenCampaign2 = function() {

    var isPrimrose2013 = function(campaignCode) {
        return campaignCode == 'primrose-family-2013' || campaignCode == 'primrose-mom-daughter-2013';
    };

    var validate = function(wrapper, campaignCode) {
        wrapper.find('.jq-leadGenError').hide();

        var passed = true;

        if (isPrimrose2013(campaignCode)) {
            if (wrapper.find('.jq-leadGenFirstName').val() === '') {
                passed = false;
            }
            if (wrapper.find('.jq-leadGenLastName').val() === '') {
                passed = false;
            }
        } else {
            if (wrapper.find('.jq-leadGenFullName').val() === '') {
                passed = false;
            }
            if (wrapper.find('.jq-leadGenGradeLevel').val() === '') {
                passed = false;
            }
        }
        if (wrapper.find('.jq-leadGenEmail').val() === '') {
            passed = false;
        }

        if (!passed) {
            wrapper.find('.jq-leadGenError-all').show();
        }

        // no phone validation at this time; not required

        return passed;
    };

    this.submit = function(submitButton) {
        var wrapper = submitButton.closest('.jq-leadGen2Wrapper');

        wrapper.find('.jq-leadGenPrelimText').hide();

        var campaignCode = wrapper.find('.jq-leadGenCampaign').val();

        if (validate(wrapper, campaignCode)) {
            wrapper.find('.jq-leadGenForm').hide();
            submitButton.hide();
            var params = {
                campaign: wrapper.find('.jq-leadGenCampaign').val(),
                email: wrapper.find('.jq-leadGenEmail').val()
            };
            var controllerUrl = "/promo/leadGenAjax2.page";
            if (isPrimrose2013(campaignCode)) {
                params.firstName = wrapper.find('.jq-leadGenFirstName').val();
                params.lastName = wrapper.find('.jq-leadGenLastName').val();
                controllerUrl = "/promo/leadGenAjax.page";
            } else {
                params.fullName = wrapper.find('.jq-leadGenFullName').val(),
                params.phone = wrapper.find('.jq-leadGenPhone').val(),
                params.gradeLevel = wrapper.find('.jq-leadGenGradeLevel').val()
            }

            jQuery.ajax({
                url: controllerUrl,
                type: "POST",
                data: params,
                dataType: 'text',
                success: function(data, textStatus, jqXHR) {
                    if (data === "OK") {
                        wrapper.find('.jq-leadGenThankYou').show();
                    } else {
                        wrapper.find('.jq-leadGenError-all').show();
                        wrapper.find('.jq-leadGenForm').show();
                        submitButton.show();
                    }
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    wrapper.find('.jq-leadGenPrelimText').show();
                    wrapper.find('.jq-leadGenForm').show();
                    wrapper.find('.jq-leadGenThankYou').hide();
                    wrapper.find('.jq-leadGenError').hide();
                }
            });
        }

        return false;
    }.lead_gen_bind(this);
};

jQuery(function() {

    GS.form.leadGenCampaign2 = GS.form.leadGenCampaign2 || new GS.form.LeadGenCampaign2();

    jQuery('.jq-leadGen2Wrapper .jq-leadGenSubmit').unbind('click').click(function() {
        return GS.form.leadGenCampaign2.submit(jQuery(this));
    });

    // we know we won't call this twice because widget is within iframe
    GS.form.findAndApplyGhostTextSwitching('.jq-leadGen2Wrapper');
});
