define(function() {
    var init = function() {
        $(function() {
            jQuery('#button-1').click(function () {
                var targetUrl = "/email/newslettersSignUpThankYou-mobile.page";
                submitForm()
                    .done(function() {
                        document.location = targetUrl;
                    });
                return false;
            });
        });

    };

    var submitForm = function() {
        var masterDeferred = new jQuery.Deferred();
        var data = $('#newslettersSignUpForm').serializeArray();
        jQuery.ajax({type: 'POST', url: document.location, data: data}
        ).done(function(data) {
                if(data.error !== undefined) {
                    masterDeferred.reject();
                    $(document).ready(function() {
                        $('div.error').html('<p>' + data.error + '</p>')
                    });
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