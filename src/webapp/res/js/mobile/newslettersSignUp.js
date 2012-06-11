define(['tracking'],function(tracking) {
    var init = function() {
        var newslettersSignUpForm = jQuery('#js-newslettersSignUpForm');
        newslettersSignUpForm.submit(function () {
            submitForm(newslettersSignUpForm)
                .done(function() {
                    document.title = 'Email Confirmation | GreatSchools';
                    newslettersSignUpForm.find('.js-signUp').hide();
                    newslettersSignUpForm.find('.js-thankYou').show();
                    tracking.clear();
                    tracking.pageName='MobileNLLandingPageThanks';
                    tracking.hierarchy='Newsletter';
                    tracking.send();
                });
            return false;
        });
    };

    var submitForm = function(newslettersSignUpForm) {
        var masterDeferred = new jQuery.Deferred();
        var data = newslettersSignUpForm.serializeArray();
        tracking.clear();
        tracking.successEvents = 'event64';
        tracking.send();
        jQuery.ajax({type: 'POST', url: document.location, data: data}
        ).done(function(data) {
                if (data.error !== undefined) {
                    masterDeferred.reject();
                    newslettersSignUpForm.find('div.error').html('<p>' + data.error + '</p>').css("display", "block");
                    return;
                }
                newslettersSignUpForm.find('.js-signUpSuccessMessage').html('<p>' + data.success + '</p>');
                masterDeferred.resolve();
            });
        return masterDeferred.promise();
    };

    return {
        init:init
    }
});