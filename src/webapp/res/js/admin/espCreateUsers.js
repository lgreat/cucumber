var GS = GS || {};
GS.form = GS.form || {};
GS.form.CreateEspUserForm = function() {

    this.createUser = function() {
        if (GS.form.CreateEspUserForm.validateFields() === true) {
            var email = jQuery('#js_email').val();
            var state = jQuery('#js_state').val();
            var schoolId = jQuery('#js_schoolId').val();
            var firstName = jQuery('#js_firstName').val();
            var lastName = jQuery('#js_lastName').val();
            var jobTitle = jQuery('#js_jobTitle').val();
            jQuery.ajax({
                type: 'POST',
                url: "/admin/createEspUsers.page",
                data: {email:email, state:state, schoolId:schoolId, firstName:firstName, lastName:lastName, jobTitle:jobTitle},
                dataType: 'json',
                async: true
            }).done(function(data) {
                    alert("user created");
                });
        }
    };

    this.validateFields = function() {
        var isEmailValid = GS.form.EspCreateUsersForm.validateRequiredInput("js_email");
        var isStateValid = GS.form.EspCreateUsersForm.validateRequiredInput("js_state");
        var isSchoolIdValid = GS.form.EspCreateUsersForm.validateRequiredInput("js_schoolId");
        return isEmailValid && isStateValid && isSchoolIdValid;
    };

    this.validateRequiredInput = function(fieldName) {
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
};

GS.form.CreateEspUsersBatchForm = function() {
    var inputElementSelector = "#js_users";
    var debugElementSelector = "#js_userCreationDebug";
    var progressElementSelector = "#js_userCreationProgress";

    var currentIndex = 0;
    var step = 10;
    var totalRows = 0;
    var working = false;
    var abort = false;

    var genericDebugOutput = [];
    var usersAlreadyCreated = [];
    var usersWithErrors = [];

    var updateProgress = function() {
        $(progressElementSelector).html(currentIndex + "/" + totalRows);
    };

    var updateDebug = function() {
        var content = '';
        if (usersAlreadyCreated.length > 0) {
            content += "Users already created: " + usersAlreadyCreated.join(",");
            content += "\n";
        }
        if (usersWithErrors.length > 0) {
            content += "Users with errors (see debug output): " + usersWithErrors.join(",");
            content += "\n";
        }
        if (genericDebugOutput.length > 0) {
            content += "Generic debug output:\n" + genericDebugOutput.join("\n");
        }
        $(debugElementSelector).val(content);
    };

    this.countRows = function(elem) {
        if ($.trim(elem.val()) == '') {
            return 0;
        }
        return $.trim(elem.val()).split("\n").length;
    };

    this.startUserCreation = function() {
        if (confirm("ARE YOU SURE?")) {
            abort = false;
            createUsers(currentIndex, step);
        }
    };

    this.stopUserCreation = function() {
        abort = true;
    };

    this.resetUserCreation = function() {
        abort = true;
        currentIndex = 0;
        updateProgress();
        genericDebugOutput = [];
        usersAlreadyCreated = [];
        usersWithErrors = [];
        $(debugElementSelector).val('');
    };

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
        jQuery.ajax({
            type: 'POST',
            url: "/admin/createEspUsers.page",
            data: {data:lines},
            dataType: 'json',
            async: true
        }).done(GS.form.CreateEspUsersBatchForm.createUsersSuccessCallback)
            .fail(GS.form.CreateEspUsersBatchForm.createUsersErrorCallback);
    };

    this.createUsersSuccessCallback = function(data) {
        working = false;
        currentIndex += step;
        if (currentIndex > totalRows) {
            currentIndex = totalRows;
        }
        updateProgress();

//        if (data.debugOutput && data.debugOutput.length > 0) {
//            genericDebugOutput = genericDebugOutput.concat(data.debugOutput);
//        }
//        if (data.usersAlreadyCreated && data.usersAlreadyCreated.length > 0) {
//            usersAlreadyCreated = usersAlreadyCreated.concat(data.usersAlreadyCreated);
//        }
//        if (data.usersWithErrors && data.usersWithErrors.length > 0) {
//            usersWithErrors = usersWithErrors.concat(data.usersWithErrors);
//        }
//
//        updateDebug();

        if (currentIndex < totalRows) {
            createUsers(currentIndex, step); // recurse
        } else {
            alert("Done!");
        }
    };

    this.createUsersErrorCallback = function() {
        working = false;
        alert("Unexpected error with the AJAX request!");
    };

    this.updateTotalRows = function() {
        totalRows = GS.form.CreateEspUsersBatchForm.countRows(jQuery('#js_users'));
    };

};

GS.form.CreateEspUserForm = new GS.form.CreateEspUserForm();
GS.form.CreateEspUsersBatchForm = new GS.form.CreateEspUsersBatchForm();

jQuery(function() {
    jQuery('#js_submit').click(
        GS.form.CreateEspUserForm.createUser
    );

    jQuery('#js_users').change(function() {
        GS.form.CreateEspUsersBatchForm.updateTotalRows();
        GS.form.CreateEspUsersBatchForm.resetUserCreation();
    });
    jQuery('#js_startCreateEspUsers').click(GS.form.CreateEspUsersBatchForm.startUserCreation);
    jQuery('#js_stopCreateEspUsers').click(GS.form.CreateEspUsersBatchForm.stopUserCreation);
    jQuery('#js_resetCreateEspUsers').click(GS.form.CreateEspUsersBatchForm.resetUserCreation);
});

