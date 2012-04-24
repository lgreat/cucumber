define(function() {
    var init = function() {
            jQuery('#js-submit').click(function () {
                var newslettersSignUpForm = $('#newslettersSignUpForm');
                submitForm(newslettersSignUpForm)
                    .done(function() {
                        document.title = 'GreatSchools - Newsletters Sign Up Thank You';
                        newslettersSignUpForm.find('div.signUp').hide();
                        newslettersSignUpForm.find('div.thankYou').show();
                    });
                return false;
            });
    }

    var submitForm = function(newslettersSignUpForm) {
        var masterDeferred = new jQuery.Deferred();
        var data = newslettersSignUpForm.serializeArray();
        jQuery.ajax({type: 'POST', url: document.location, data: data}
        ).done(function(data) {
                if (data.error !== undefined) {
                    masterDeferred.reject();
                    newslettersSignUpForm.find('div.error').html('<p>' + data.error + '</p>');
                    return;
                }
                masterDeferred.resolve();
            });
        return masterDeferred.promise();
    }

    return {
        init:init
    }
});