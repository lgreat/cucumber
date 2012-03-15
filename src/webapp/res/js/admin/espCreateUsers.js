var GS = GS || {};
GS.form = GS.form || {};
//Package that handles adding a single user.
GS.form.NewEspUserForm = function() {

    var createUser = function() {
        if (validateFields() === true) {
            var email = jQuery('#js_email').val();
            var state = jQuery('#js_state').val();
            var schoolId = jQuery('#js_schoolId').val();
            var firstName = jQuery('#js_firstName').val();
            var lastName = jQuery('#js_lastName').val();
            var jobTitle = jQuery('#js_jobTitle').val();
            jQuery.ajax({
                type: 'POST',
                url: "/admin/createEspUser.page",
                data: {email:email, state:state, schoolId:schoolId, firstName:firstName, lastName:lastName, jobTitle:jobTitle},
                dataType: 'json',
                async: true
            }).done(function(data) {
                    var debugInfo = "";
                    if (data.debugOutput && data.debugOutput.length > 0) {
                        debugInfo = debugInfo.concat(data.debugOutput);
                        debugInfo += "\n\n";
                    }
                    if (data.usersAlreadyApproved && data.usersAlreadyApproved.length > 0) {
                        debugInfo = "User already approved:" + debugInfo.concat(data.usersAlreadyApproved);
                        debugInfo += "\n\n";
                    }
                    if (data.usersAlreadyPreApproved && data.usersAlreadyPreApproved.length > 0) {
                        debugInfo = "user already pre-approved:" + debugInfo.concat(data.usersAlreadyPreApproved);
                        debugInfo += "\n\n";
                    }
                    if (debugInfo != "") {
                        alert(debugInfo);
                    }
                });
        }
    };

    var validateFields = function() {
        var isEmailValid = validateRequiredInput("js_email");
        var isStateValid = validateRequiredInput("js_state");
        var isSchoolIdValid = validateRequiredInput("js_schoolId");
        return isEmailValid && isStateValid && isSchoolIdValid;
    };

    var validateRequiredInput = function(fieldName) {
        var field = jQuery('#' + fieldName);
        var fieldVal = field.val();
        var fieldError = jQuery('#' + fieldName + '_error');
        var isValid = true;
        if (field === undefined) {
            isValid = false;
        } else {
            fieldVal = jQuery.trim(fieldVal);
            if (fieldVal === '') {
                fieldError.show();
                isValid = false;
            }
        }

        return isValid;
    };
    // interface
    return {
        createUser: createUser
    };
};

//Package that handles adding multiple users as a batch.
GS.form.NewEspUsersBatchForm = function() {
    var inputElementSelector = "#js_users";
    var debugElementSelector = "#js_userCreationDebug";
    var progressElementSelector = "#js_userCreationProgress";

    var currentIndex = 0;
    var step = 10;
    var totalRows = 0;
    var working = false;
    var abort = false;

    var genericDebugOutput = [];
    var usersAlreadyApproved = [];
    var usersAlreadyPreApproved = [];
    var usersWithErrors = [];

    var updateProgress = function() {
        $(progressElementSelector).html(currentIndex + "/" + totalRows);
    };

    var updateDebug = function() {
        var content = '';
        if (usersAlreadyApproved.length > 0) {
            content += "Users already approved: " + usersAlreadyApproved.join(",");
            content += "\n\n";
        }
        if (usersAlreadyPreApproved.length > 0) {
            content += "Users already pre-approved: " + usersAlreadyPreApproved.join(",");
            content += "\n\n";
        }
        if (usersWithErrors.length > 0) {
            content += "Users with errors (see debug output): " + usersWithErrors.join(",");
            content += "\n\n";
        }
        if (genericDebugOutput.length > 0) {
            content += "Generic debug output:\n" + genericDebugOutput.join("\n");
        }
        $(debugElementSelector).val(content);
    };

    var countRows = function(elem) {
        if ($.trim(elem.val()) == '') {
            return 0;
        }
        return $.trim(elem.val()).split("\n").length;
    };

    var startUserCreation = function() {
        var state = jQuery('#js_batchState').val();
        if (state === '') {
            alert("Please select a state.");
        } else if (confirm("You will be pre-approving users in STATE:" + state + ".Are you sure you want to continue?")) {
            abort = false;
            jQuery(inputElementSelector).attr('disabled','');
            createUsers(currentIndex, step);
        }

    };

    var stopUserCreation = function() {
        abort = true;
    };

//    var resetUserCreation = function() {
//        abort = true;
//        currentIndex = 0;
//        updateProgress();
//        genericDebugOutput = [];
//        usersAlreadyApproved = [];
//        usersWithErrors = [];
//        $(debugElementSelector).val('');
//    };

    var sliceRows = function(dataElement, startRow, endRow) {
        var rows = dataElement.val().split("\n");
        if (startRow > rows.length || endRow < startRow || startRow < 0) {
            return [];
        }
        if (endRow > rows.length) {
            endRow = rows.length;
        }
        return rows.slice(startRow, endRow).join("\n");
    };

    var createUsers = function(myIndex, myStep) {
        if (working || abort) {
            return;
        }
        working = true;
        var lines = sliceRows($(inputElementSelector), myIndex, myIndex + myStep);
        var state = jQuery('#js_batchState').val();
        jQuery.ajax({
            type: 'POST',
            url: "/admin/createEspUsersBatch.page",
            data: {data:lines,state:state},
            dataType: 'json',
            async: true
        }).done(createUsersSuccessCallback)
            .fail(createUsersErrorCallback);
    };

    var createUsersSuccessCallback = function(data) {
        working = false;
        currentIndex += step;
        if (currentIndex > totalRows) {
            currentIndex = totalRows;
        }
        updateProgress();

        if (data.debugOutput && data.debugOutput.length > 0) {
            genericDebugOutput = genericDebugOutput.concat(data.debugOutput);
        }
        if (data.usersAlreadyApproved && data.usersAlreadyApproved.length > 0) {
            usersAlreadyApproved = usersAlreadyApproved.concat(data.usersAlreadyApproved);
        }
        if (data.usersAlreadyPreApproved && data.usersAlreadyPreApproved.length > 0) {
            usersAlreadyPreApproved = usersAlreadyPreApproved.concat(data.usersAlreadyPreApproved);
        }
        if (data.usersWithErrors && data.usersWithErrors.length > 0) {
            usersWithErrors = usersWithErrors.concat(data.usersWithErrors);
        }

        updateDebug();

        if (currentIndex < totalRows) {
            createUsers(currentIndex, step); // recurse
        } else {
            alert("Done!");
            jQuery(inputElementSelector).removeAttr('disabled');
        }
    };

    var createUsersErrorCallback = function() {
        working = false;
        alert("Unexpected error with the AJAX request!");
    };

    var updateTotalRows = function() {
        totalRows = countRows(jQuery('#js_users'));
    };

    // interface
    return {
        startUserCreation: startUserCreation,
        stopUserCreation: stopUserCreation,
//        resetUserCreation: resetUserCreation,
        updateTotalRows: updateTotalRows
    };

};

GS.form.newEspUserForm = new GS.form.NewEspUserForm();
GS.form.newEspUsersBatchForm = new GS.form.NewEspUsersBatchForm();

jQuery(function() {
    jQuery('#js_submit').click(
        GS.form.newEspUserForm.createUser
    );

    jQuery('#js_users').change(function() {
        GS.form.newEspUsersBatchForm.updateTotalRows();
//        GS.form.newEspUsersBatchForm.resetUserCreation();
    });

    jQuery('#js_startCreateEspUsers').click(GS.form.newEspUsersBatchForm.startUserCreation);
    jQuery('#js_stopCreateEspUsers').click(GS.form.newEspUsersBatchForm.stopUserCreation);
//    jQuery('#js_resetCreateEspUsers').click(GS.form.newEspUsersBatchForm.resetUserCreation);
});

