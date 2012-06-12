define(['localStorage', 'hogan', 'tracking', 'modal'], function(localStorage, hogan, tracking, modal) {
    var schoolsKey = "Schools";
    var schoolMapKey = "SchoolMap";
    var emailSubject = "My Saved Schools from GreatSchools.org";
    var emailBody = "My Saved Schools List from GreatSchools.org:\n\n";
    var noSavedSchools = "No schools have been saved to your list";
    var errorFetchingSchools = "We are not able to display your saved schools at this time. Please try again later.";
    var emailStringMap = {};
    var template;

    var init = function() {
        $(function() {
            template = hogan.compile($('#js-schoolTemplate').html());
        });
    };

    var renderSchools = function(data) {
        if(data == null || data.JsonError === true) {
            showNoSchoolsToDisplay(errorFetchingSchools);
            return;
        }
        if(data.NumSavedSchools == 0) {
            showNoSchoolsToDisplay(noSavedSchools);
            return;
        }

        var schools = data.Schools;
        var numOfSavedSchools = data.NumSavedSchools;
        var savedSchoolsDiv = $('#js-savedSchools');

        for(var i = 1; i <= numOfSavedSchools; i++) {
            var type_school = schools[i].type;
            var display_gs_rating = "";
            if(type_school == "private" || schools[i].gsRating == ""){
                display_gs_rating = " dn";
            }
            var community_rating = schools[i].commRating;
            var display_comm_rating = "";
            if(community_rating == ""){
                display_comm_rating = " dn";
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
            var $schoolHref = savedSchoolsDiv.find('#js-schoolUrl-' + schools[i].state + '_' + schools[i].id);
            $schoolHref.attr('href', $schoolHref.attr('data-href')); // copy data-href into href, fix for desktop FF bug
            savedSchoolsDiv.find('.js-schoolTemplate').last().addClass('js-' + schools[i].state + '_' + schools[i].id);
            savedSchoolsDiv.find('.js-keyline').last().addClass('js-' + schools[i].state + '_' + schools[i].id);

            var startIndex = emailBody.length;
            emailBody += schools[i].name + " " + schools[i].schoolUrl + "\n";
            emailBody += schools[i].type + ", " + schools[i].gradeLevels + "\n";
            if(schools[i].enrollment !== '') {
                emailBody += "Enrollment: " + schools[i].enrollment + "\n";
            }
            if(schools[i].gsRating !== '') {
                emailBody += "Great Schools Rating: " + schools[i].gsRating + "\n";
            }
            emailBody += schools[i].address + "\n\n";
            var endIndex = emailBody.length;
            emailStringMap[schools[i].state + '_' + schools[i].id] = startIndex + ',' + endIndex;
        }

        var emailAndDeleteAll = $('.js-emailAndDeleteAll');
        emailAndDeleteAll.show();
        emailAndDeleteAll.find('.js-emailMyList').attr('href', 'mailto:?subject=' + emailSubject + '&body=' + encodeURIComponent(emailBody));
    };

    var getSavedSchools = function() {
        var savedSchools = localStorage.getItem(schoolsKey);
        if(savedSchools != null && savedSchools[schoolsKey].length > 0) {
            modal.showModal();
            var savedSchoolsJson = savedSchools[schoolsKey];
            $.ajax({
                type: 'POST',
                url: document.location,
                dataType: 'json',
                data: {savedSchoolsJson: JSON.stringify({ schools : savedSchoolsJson})}
            }).done(function(data) {
                    modal.hideModal();
                    //$('#js-loadingSchools').hide();
                    renderSchools(data);

                    // hide address bar
                    window.scrollTo(0, 1);

                    $('#js-savedSchools .js-deleteSchool').click(function(){
                        deleteSchool(this.id);
                    });

                    $('.js-emailMyList').click(function() {
                        tracking.clear();
                        tracking.successEvents = 'event69';
                        tracking.send();
                    });
                }
            ).fail(function() {
                    modal.hideModal();
                    showNoSchoolsToDisplay(errorFetchingSchools);
                }
            );
        }
        else {
            showNoSchoolsToDisplay(noSavedSchools);
        }
    };

    var deleteSchool = function(delete_school) {
        var savedSchoolMap = localStorage.getItem(schoolMapKey);
        var schoolMap = savedSchoolMap[schoolMapKey];
        var state_schoolId = delete_school.split('-')[2];
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
            var savedSchoolsDiv  = $('#js-savedSchools');
            savedSchoolsDiv.find('.js-' + state_schoolId).remove();

            var startIndex = emailStringMap[state_schoolId].split(',')[0];
            var endIndex = emailStringMap[state_schoolId].split(',')[1];
            var schoolString = emailBody.substring(startIndex, endIndex);
            emailBody = emailBody.replace(schoolString, '');
            var emailAndDeleteAll = $('.js-emailAndDeleteAll');
            emailAndDeleteAll.find('.js-emailMyList').attr('href', 'mailto:?subject=' + emailSubject + '&body=' + encodeURIComponent(emailBody));

            if(savedSchoolsDiv.is(':empty')) {
                emailAndDeleteAll.hide();
                showNoSchoolsToDisplay(noSavedSchools);
            }
        }
    };

    var deleteAllSchools = function() {
        var deleteAll = confirm('Are you sure you want to delete all? This will remove all your saved schools.');
        if(deleteAll == true) {
            localStorage.removeItem(schoolsKey);
            localStorage.removeItem(schoolMapKey);
            emailBody = '';
            $('#js-savedSchools').empty();
            $('.js-emailAndDeleteAll').hide();
            showNoSchoolsToDisplay(noSavedSchools);
        }
    };

    var showNoSchoolsToDisplay = function(message) {
        var noSchoolsToDisplay = $('#js-noSchoolsToDisplay');
        noSchoolsToDisplay.show();
        noSchoolsToDisplay.find('p').text(message);
    };

    return {
        init:init,
        getSavedSchools:getSavedSchools,
        deleteSchool:deleteSchool,
        deleteAllSchools:deleteAllSchools
    }
});