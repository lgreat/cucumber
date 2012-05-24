define(['localStorage', 'tracking'], function(localStorage, tracking) {
    var schoolsKey = "Schools";
    var schoolMapKey = "SchoolMap";

    var init = function(state_schoolId) {
        var schoolSave = $('#js-saveSchool');
        var saveSchoolButton = schoolSave.find('#js-saveSchoolButton');
        var disabled = false;
        var schoolMap = localStorage.getItem(schoolMapKey);
        if(schoolMap !== null && schoolMap.SchoolMap[0].hasOwnProperty(state_schoolId) === true) {
            saveSchoolButton.removeClass('but-2').addClass('but-2-inactive').text('Saved');
            disabled = true;
        }

        saveSchoolButton.click( function() {
            if(disabled === false && localStorage.enabled === true) {
                disabled = true;
                saveSchoolButton.removeClass('but-2').addClass('but-2-inactive').text('Saving...');
                tracking.clear();
                tracking.successEvents = 'event68';
                tracking.send();
                saveSchool(schoolSave, saveSchoolButton, state_schoolId);
            }
        });
    };

    var saveSchool = function(schoolSave, saveSchoolButton, state_schoolId) {
        var saveSchoolFormInput = schoolSave.find('#js-saveSchoolForm :input');
        var newSchool = {};
        saveSchoolFormInput.each(function() {
            newSchool[this.name] = this.value;
        });

        var schools = localStorage.getItem(schoolsKey);
        var schoolMap = localStorage.getItem(schoolMapKey);

        if(schools == null) {
            var newSchoolsObject = {};
            newSchoolsObject.Schools = [];
            newSchoolsObject.Schools.push(newSchool);
            localStorage.setItem(schoolsKey, newSchoolsObject);
        }
        else if (schoolMap.SchoolMap[0].hasOwnProperty(state_schoolId) === false) {
            schools.Schools.push(newSchool);
            localStorage.setItem(schoolsKey, schools);
        }

        if(schoolMap == null) {
            var newSchoolMap = {};
            newSchoolMap.SchoolMap = [];
            newSchoolMap.SchoolMap.push({});
            newSchoolMap.SchoolMap[0][state_schoolId] = 1;
            localStorage.setItem(schoolMapKey, newSchoolMap);
        }
        else if (schoolMap.SchoolMap[0].hasOwnProperty(state_schoolId) === false) {
            var state_schoolIds = schoolMap[schoolMapKey];
            state_schoolIds[0][state_schoolId] = 1;
            schoolMap[schoolMapKey] = state_schoolIds;
            localStorage.setItem(schoolMapKey, schoolMap);
        }
        saveSchoolButton.text('Saved');
    };

    return {
        init:init
    }
});