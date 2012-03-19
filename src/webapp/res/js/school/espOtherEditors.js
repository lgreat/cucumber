var GS = GS || {};
GS.form = GS.form || {};
GS.form.RequestOtherEditors = function() {
    var preApproveNewEditor = function() {
        var email = jQuery('#js_email').val();
        var state = jQuery('#js_schoolState').val();
        var schoolId = jQuery('#js_schoolId').val();
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
                if (data == null) {
                    alert("null response");
                    jQuery('#js_requestOtherEditorsFormInputs').hide(); // added here for testing
                    jQuery('#js_requestOtherEditorsThankYou').show();
                } else {
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

                        jQuery('#js_requestOtherEditorsFormInputs').hide();
                        jQuery('#js_requestOtherEditorsThankYou').show();

                    }
                }
            }).fail(function() {
                alert("An error occurred, please try again.");
            }
        );
    };

    return {
        preApproveNewEditor: preApproveNewEditor
    };
};

GS.form.requestOtherEditors = new GS.form.RequestOtherEditors();

GSType.hover.EspOtherEditors = function() {
    this.loadDialog = function() {
        this.pageName='OSP Add/See Accounts';
        this.hier1='ESP';
    };
    this.showHover = function() {
        GSType.hover.espOtherEditors.show();
    };
    this.onClose = function() {};
};

GSType.hover.EspOtherEditors.prototype = new GSType.hover.HoverDialog("espOtherEditors",640);
GSType.hover.espOtherEditors = new GSType.hover.EspOtherEditors();

jQuery(function() {
    GSType.hover.espOtherEditors.loadDialog();

    jQuery('#js_otherEspEditors').click(
        GSType.hover.espOtherEditors.showHover
    );

    jQuery('#js_requestOtherPreApproval').click(
        GS.form.requestOtherEditors.preApproveNewEditor
    );

    jQuery('#js_requestAnotherEspEditor').click( function() {
            jQuery('#js_requestOtherEditorsFormInputs').show();
            jQuery('#js_requestOtherEditorsThankYou').hide();
        }
    );

});
