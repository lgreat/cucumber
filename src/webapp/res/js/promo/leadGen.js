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
        wrapper.find('.js_leadGenError').hide();

        var passed = true;

        if (wrapper.find('.js_leadGenFirstName').val() == '') {
            passed = false;
        }
        if (wrapper.find('.js_leadGenLastName').val() == '') {
            passed = false;
        }
        if (wrapper.find('.js_leadGenEmail').val() == '') {
            passed = false;
        }
        if (wrapper.find('.js_leadGenZip').val() == '' || isNaN(wrapper.find('.js_leadGenZip').val())) {
            passed = false;
        }
        var childsAge = wrapper.find('.js_leadGenChildsAge');
        if (childsAge.size() === 1 && childsAge.val() == '') {
            passed = false;
        }

        if (!passed) {
            wrapper.find('.js_leadGenError-all').show();
        }

        return passed;
    };

    this.submit = function(submitButton) {
        var wrapper = submitButton.closest('.js_leadGenWrapper');

        if (this.validate(wrapper)) {
            submitButton.hide();
            var params = {
                campaign: wrapper.find('.js_leadGenCampaign').val(),
                firstName: wrapper.find('.js_leadGenFirstName').val(),
                lastName: wrapper.find('.js_leadGenLastName').val(),
                email: wrapper.find('.js_leadGenEmail').val(),
                zip: wrapper.find('.js_leadGenZip').val(),
                childsAge: wrapper.find('.js_leadGenChildsAge').val()
            };

            jQuery.ajax({
                url: "/promo/leadGenAjax.page",
                type: "POST",
                data: params,
                dataType: 'text',
                success: function(data, textStatus, jqXHR) {
                    if (data == "OK") {
                        wrapper.find('.js_leadGenIntroText').hide();
                        wrapper.find('.js_leadGenForm').hide();
                        wrapper.find('.js_leadGenThankYou').show();
                    } else {
                        if (data.indexOf('email') > -1) {
                            wrapper.find('.js_leadGenError-email').show();
                        } else {
                            if (data.indexOf('firstName') > -1) {
                                wrapper.find('.js_leadGenError-all').show();
                            }
                            if (data.indexOf('lastName') > -1) {
                                wrapper.find('.js_leadGenError-all').show();
                            }
                            if (data.indexOf('zip') > -1) {
                                wrapper.find('.js_leadGenError-all').show();
                            }
                            if (data.indexOf('childsAge') > -1) {
                                wrapper.find('.js_leadGenError-all').show();
                            }
                        }

                        submitButton.show();
                    }
                },
                error: function(jqXHR,textStatus,errorThrown) {
                    wrapper.find('.js_leadGenIntroText').show();
                    wrapper.find('.js_leadGenForm').show();
                    wrapper.find('.js_leadGenThankYou').hide();
                }
            });
        }

        return false;
    }.lead_gen_bind(this);
};




jQuery(function() {

    GS.form.leadGenCampaign = GS.form.leadGenCampaign || new GS.form.LeadGenCampaign();

    jQuery('.js_leadGenForm .js_leadGenSubmit').unbind('click').click(function() {
        return GS.form.leadGenCampaign.submit(jQuery(this));
    });
});
