define(['truncate', 'schoolSave'], function(truncate, schoolSave) {
    var init = function(stateAndSchoolId) {
        truncate.init();
        schoolSave.init(stateAndSchoolId);
    };

    return {
        init:init
    }
});