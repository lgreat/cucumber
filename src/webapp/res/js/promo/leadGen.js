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
            passed = false;
        }
        if (wrapper.find('.jq-leadGenLastName').val() == '') {
            passed = false;
        }
        if (wrapper.find('.jq-leadGenEmail').val() == '') {
            passed = false;
        }
        if (wrapper.find('.jq-leadGenZip').val() == '' || isNaN(wrapper.find('.jq-leadGenZip').val())) {
            passed = false;
        }

        if (!passed) {
            wrapper.find('.jq-leadGenError-all').show();
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
                email: wrapper.find('.jq-leadGenEmail').val(),
                zip: wrapper.find('.jq-leadGenZip').val()
            };

            jQuery.ajax({
                url: "/promo/leadGenAjax.page",
                type: "POST",
                data: params,
                success: function(data, textStatus, jqXHR) {
                    if (data == "OK") {
                        wrapper.find('.jq-leadGenIntroText').hide();
                        wrapper.find('.jq-leadGenForm').hide();
                        wrapper.find('.jq-leadGenThankYou').show();
                    } else {
                        if (data.indexOf('email') > -1) {
                            wrapper.find('.jq-leadGenError-email').show();
                        } else {
                            if (data.indexOf('firstName') > -1) {
                            wrapper.find('.jq-leadGenError-all').show();
                            }
                            if (data.indexOf('lastName') > -1) {
                                wrapper.find('.jq-leadGenError-all').show();
                            }
                            if (data.indexOf('zip') > -1) {
                                wrapper.find('.jq-leadGenError-all').show();
                            }
                        }

                        submitButton.show();
                    }
                },
                error: function(jqXHR,textStatus,errorThrown) {
                    wrapper.find('.jq-leadGenIntroText').show();
                    wrapper.find('.jq-leadGenForm').show();
                    wrapper.find('.jq-leadGenThankYou').hide();
                }
            });
            jQuery.ajax({
                url:"http://cdn4.eyewonder.com/cm/ck/9826-133851-21419-0",
                type: "POST",
                data: {
                  mpt:   Math.random()*10000000000000000
                },
                success: function(data, textStatus, jqXHR) {
                    //do nothing
                },
                error: function(jqXHR,textStatus,errorThrown) {
                    //do nothing
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
