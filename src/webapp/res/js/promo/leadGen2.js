Function.prototype.lead_gen_bind = function(obj) {
    var method = this;
    return function() {
        return method.apply(obj, arguments);
    };
};

GS = GS || {};
GS.form = GS.form || {};
GS.form.LeadGenCampaign2 = function() {

    this.validate = function(wrapper) {
        wrapper.find('.jq-leadGenError').hide();

        var passed = true;

        if (wrapper.find('.jq-leadGenFullName').val() == '') {
            passed = false;
        }
        if (wrapper.find('.jq-leadGenEmail').val() == '') {
            passed = false;
        }
        if (wrapper.find('.jq-leadGenGradeLevel').val() == '') {
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

        if (this.validate(wrapper)) {
            wrapper.find('.jq-leadGenForm').hide();
            submitButton.hide();
            var params = {
                campaign: wrapper.find('.jq-leadGenCampaign').val(),
                fullName: wrapper.find('.jq-leadGenFullName').val(),
                email: wrapper.find('.jq-leadGenEmail').val(),
                phone: wrapper.find('.jq-leadGenPhone').val(),
                gradeLevel: wrapper.find('.jq-leadGenGradeLevel').val()
            };

            jQuery.ajax({
                url: "/promo/leadGenAjax2.page",
                type: "POST",
                data: params,
                dataType: 'text',
                success: function(data, textStatus, jqXHR) {
                    if (data == "OK") {
                        wrapper.find('.jq-leadGenThankYou').show();
                    } else {
                        wrapper.find('.jq-leadGenError-all').show();
                        wrapper.find('.jq-leadGenForm').show();
                        submitButton.show();
                    }
                },
                error: function(jqXHR,textStatus,errorThrown) {
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

/*
TODO-13218: What to do with this line from espForm.js? data = GS.form.handleInputsWithGhostTextAsValue(data, '#espFormPage-' + GS.espForm.currentPage);
TODO-13218: test ghost text in different browsers
TODO-13218: test UI across browsers
 */


jQuery(function() {

    GS.form.leadGenCampaign2 = GS.form.leadGenCampaign2 || new GS.form.LeadGenCampaign2();

    jQuery('.jq-leadGen2Wrapper .jq-leadGenSubmit').unbind('click').click(function() {
        return GS.form.leadGenCampaign2.submit(jQuery(this));
    });

    // we know we won't call this twice because widget is within iframe
    GS.form.findAndApplyGhostTextSwitching('.jq-leadGen2Wrapper');
});
