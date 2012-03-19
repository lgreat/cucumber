var GS = GS || {};
GS.form = GS.form || {};
GS.form.RequestOtherEditors = function() {
    var preApproveNewEditor = function() {
        var email = jQuery('#js_email').val();
        var state = 'ak';
        var schoolId = 53;
        var firstName = jQuery('#js_firstName').val();
        var lastName = jQuery('#js_lastName').val();
        var jobTitle = jQuery('#js_jobTitle').val();

        jQuery.ajax({
            type: 'POST',
            url: "/official-school-profile/addPreApprovedMemberships.page",
            data: {email:email, state:state, schoolId:schoolId, firstName:firstName, lastName:lastName, jobTitle:jobTitle},
            dataType: 'json',
            async: true
        }).done(
            function(data) {
                if (data.debugOutput !== '') {
                    alert(data.debugOutput);
                }
                if (data.usersAlreadyApproved !== '') {
                    alert(data.usersAlreadyApproved);
                }
                if (data.usersAlreadyPreApproved !== '') {
                    alert(data.usersAlreadyPreApproved);
                }
                if (data.newOtherEspMemberships !== '') {
                    alert(data.newOtherEspMemberships);

                    jQuery('# js_requestOtherEditorsForm').hide();
                    jQuery('# js_requestOtherEditorsThankYou').show();

                }
            }).fail(function() {
                alert("AN error occurred, please try again.");
            }
        );
    }

    return {
        preApproveNewEditor: preApproveNewEditor
    };
}

GS.form.requestOtherEditors = new GS.form.RequestOtherEditors();

jQuery(function() {
    jQuery('#js_otherEspEditors').click(
        GSType.hover.espOtherEditors.showHover
    );

    jQuery('#js_requestOtherPreApproval').click(
        GS.form.requestOtherEditors.preApproveNewEditor
    );

    jQuery('#js_requestAnotherEspEditor').click( function() {
            jQuery('# js_requestOtherEditorsForm').show();
            jQuery('# js_requestOtherEditorsThankYou').hide();
        }
    );

});