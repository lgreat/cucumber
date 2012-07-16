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

        if (!disabled) {
            saveSchoolButton.on('click.saver', function() {
                if(disabled === false && localStorage.enabled === true) {
                    disabled = true;
                    saveSchoolButton.removeClass('but-2').addClass('but-2-inactive').text('Saving...');
                    if (saveSchool(schoolSave, state_schoolId)) {
                        tracking.clear();
                        tracking.successEvents = 'event68';
                        tracking.send();
                        saveSchoolButton.text('Saved');
                        saveSchoolButton.off('click.saver');
                    } else {
                        alert("To save schools, turn Private Browsing OFF in your browser settings.");
                        saveSchoolButton.addClass('but-2').removeClass('but-2-inactive').text('Save');
                        disabled = false;
                    }
                } else if (localStorage.enabled === false) {
                    alert("To save schools you must have local storage enabled.");
                    disabled = false;
                }
            });
        }
    };

    var saveSchool = function(schoolSave, state_schoolId) {
        var saveSchoolFormInput = schoolSave.find('#js-saveSchoolForm :input');
        var newSchool = {};
        saveSchoolFormInput.each(function() {
            newSchool[this.name] = this.value;
        });

        var schools = localStorage.getItem(schoolsKey);
        var schoolMap = localStorage.getItem(schoolMapKey);

        var success = true;
        if(schools == null) {
            var newSchoolsObject = {};
            newSchoolsObject.Schools = [];
            newSchoolsObject.Schools.push(newSchool);
            success = localStorage.setItem(schoolsKey, newSchoolsObject);
        }
        else if (schoolMap.SchoolMap[0].hasOwnProperty(state_schoolId) === false) {
            schools.Schools.push(newSchool);
            success = localStorage.setItem(schoolsKey, schools);
        }
        if (!success) {
            // on error saving to localStorage, bail out early
            return false;
        }

        if(schoolMap == null) {
            var newSchoolMap = {};
            newSchoolMap.SchoolMap = [];
            newSchoolMap.SchoolMap.push({});
            newSchoolMap.SchoolMap[0][state_schoolId] = 1;
            success = localStorage.setItem(schoolMapKey, newSchoolMap);
        }
        else if (schoolMap.SchoolMap[0].hasOwnProperty(state_schoolId) === false) {
            var state_schoolIds = schoolMap[schoolMapKey];
            state_schoolIds[0][state_schoolId] = 1;
            schoolMap[schoolMapKey] = state_schoolIds;
            success = localStorage.setItem(schoolMapKey, schoolMap);
        }
        return success;
    };

    return {
        init:init
    }
});