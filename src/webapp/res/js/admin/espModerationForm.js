var GS = GS || {};
GS.form = GS.form || {};
GS.form.EspModerationForm = function () {

    var checkForProvisionalUsersStateLock = function () {
        var membershipIds = "";
        var dfd = jQuery.Deferred();

        $('#js_espModerationTable input[type=checkbox]:checked').each(function (index, value) {
            var memberId = $(this).attr("id");
            membershipIds = membershipIds + "," + memberId.substring(3, memberId.length);
            membershipIds = membershipIds.substring(1, membershipIds.length);
        });

        if (membershipIds !== "") {
            jQuery.ajax({
                type:'GET',
                url:'/admin/espModerationForm/checkStateLocks.page',
                data:{memberIds:membershipIds},
                dataType:'json',
                async:false
            }).done(
                function (data) {
                    if (data.idsWithStateLocked !== undefined) {
                        alert("State locked.Please remove the member ids:-" + data.idsWithStateLocked);
                        dfd.reject();
                    } else {
                        dfd.resolve();
                    }
                }
            ).fail(function () {
                    dfd.reject();
                });

            return dfd.promise();
        }
    };

    // interface
    return {
        checkForProvisionalUsersStateLock:checkForProvisionalUsersStateLock
    };
};

GS.form.espModerationForm = new GS.form.EspModerationForm();

jQuery(function () {
    jQuery('#js_espModerationForm').submit(function (event) {
        if ($(event.originalEvent.explicitOriginalTarget).val() === 'approve') {
            jQuery.when(
                GS.form.espModerationForm.checkForProvisionalUsersStateLock()
            ).done(
                function () {
                    jQuery('#js_espModerationForm').submit();
                }
            )
            return false;
        }
    });
});

