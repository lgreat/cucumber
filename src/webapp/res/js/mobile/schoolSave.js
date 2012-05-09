define(['localStorage', 'hogan'], function(localStorage, hogan) {
    var schoolsKey = "Schools";
    var schoolMapKey = "SchoolMap";
    var emailSubject = "My Saved Schools from GreatSchools.org";
    var emailBody = "My Saved Schools List from GreatSchools.org:\n\n";
    
    var init = function(state_schoolId) {
        var schoolSave = $('div.mam #saveSchool');
        var saveSchoolButton = schoolSave.find('#saveSchoolButton');
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
                saveSchool(schoolSave, saveSchoolButton, state_schoolId);
            }
        });
    };

    var mySchoolListInit = function() {
        $(function() {
            template = hogan.compile($('#schoolTemplate').html());
        });
    };

    var saveSchool = function(schoolSave, saveSchoolButton, state_schoolId) {
        var saveSchoolFormInput = schoolSave.find('#saveSchoolForm :input');
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
        else {
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
        else {
            var state_schoolIds = schoolMap[schoolMapKey];
            state_schoolIds[0][state_schoolId] = 1;
            schoolMap[schoolMapKey] = state_schoolIds;
            localStorage.setItem(schoolMapKey, schoolMap);
        }
        saveSchoolButton.text('Saved');
    }

    var getSavedSchools = function() {
        var savedSchools = localStorage.getItem(schoolsKey);
        if(savedSchools == null || savedSchools[schoolsKey].length == 0) {
            $('div #noSavedSchools').css('display', 'block');
            return;
        }

        var schools = savedSchools[schoolsKey];
        var numOfSavedSchools = schools.length;
        var savedSchoolsDiv = $('#savedSchools');

        for(var i = 0; i < numOfSavedSchools; i++) {
            var type_school = schools[i].type;
            var display_gs_rating = "";
            if(type_school == "private" || schools[i].gsRating == ""){
                display_gs_rating = " dn"
            }
            var community_rating = schools[i].commRating;
            var display_comm_rating = "";
            if(community_rating == ""){
                display_comm_rating = " dn"
            }

            var html = template.render({
                schoolName: schools[i].name,
                schoolType: schools[i].type,
                schoolGradeLevels: schools[i].gradeLevels,
                schoolCity: schools[i].city,
                schoolState: schools[i].state,
                displayGSRating: display_gs_rating,
                displayCommRating: display_comm_rating,
                gsRating: schools[i].gsRating,
                commRating: schools[i].commRating,
                starOff: 5 - parseInt(schools[i].commRating),
                state_schoolId: schools[i].state + '_' + schools[i].id,
                schoolUrl: schools[i].schoolUrl,
                enrollment: schools[i].enrollment,
                address: schools[i].address,
                zip: schools[i].zip
            });
            savedSchoolsDiv.append(html);
            savedSchoolsDiv.find('.schoolTemplate').last().addClass(schools[i].state + '_' + schools[i].id);
//            savedSchoolsDiv.find('#schoolUrl-' + schools[i].state + '_' + schools[i].id).attr('href', schools[i].schoolUrl);
            savedSchoolsDiv.find('div.clearfloat').last().addClass(schools[i].state + '_' + schools[i].id);

            emailBody += schools[i].name + " " + schools[i].schoolUrl + "\n";
            emailBody += schools[i].type + ", " + schools[i].gradeLevels + "\n";
            emailBody += "Enrollment: " + schools[i].enrollment + "\n";
            emailBody += "Great Schools Rating: " + schools[i].gsRating + "\n";
            emailBody += schools[i].address + "\n\n";
        }

        var emailAndDeleteAll = $('div.emailAndDeleteAll');
        emailAndDeleteAll.css('display', 'block');
        emailAndDeleteAll.find('.emailMyList').attr('href', 'mailto:?subject=' + emailSubject + '&body=' + encodeURI(emailBody));
    };

    var deleteSchool = function(delete_school) {
        var savedSchoolMap = localStorage.getItem(schoolMapKey);
        var schoolMap = savedSchoolMap[schoolMapKey];
        var state_schoolId = delete_school.split('-')[1];
        if(schoolMap[0][state_schoolId] == 1) {
            var stateSchool = state_schoolId.split('_');
            var state = stateSchool[0];
            var schoolId = stateSchool[1];
            var savedSchools = localStorage.getItem(schoolsKey);
            var schools = savedSchools[schoolsKey];
            for(var i = 0; i < schools.length; i++) {
                if(schools[i].id == schoolId && schools[i].state == state) {
                    schools.splice(i, 1);
                    delete schoolMap[0][state_schoolId];
                    break;
                }
            }
            savedSchools.Schools = schools;
            savedSchoolMap[schoolMapKey] = schoolMap;
            localStorage.setItem(schoolsKey, savedSchools);
            localStorage.setItem(schoolMapKey, savedSchoolMap);
            $('div .' + state_schoolId).remove();

            if($('div #savedSchools').is(':empty')) {
                $('div .emailAndDeleteAll').css('display', 'none');
                $('div #noSavedSchools').css('display', 'block');
            }
        }
    };

    var deleteAllSchools = function() {
        var deleteAll = confirm('Are you sure you want to delete all? This will remove all your saved schools.');
        if(deleteAll == true) {
            localStorage.removeItem(schoolsKey);
            localStorage.removeItem(schoolMapKey);
            $('div #savedSchools').empty();
            $('div.emailAndDeleteAll').css('display', 'none');
            $('div #noSavedSchools').css('display', 'block');
        }
    };

    return {
        init:init,
        mySchoolListInit:mySchoolListInit,
        getSavedSchools:getSavedSchools,
        deleteSchool:deleteSchool,
        deleteAllSchools:deleteAllSchools
    }
});