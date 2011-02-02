GS = GS || {};
GS.form = GS.form || {};
GS.form.EmailManagement = function() {
    this.toggleNthGraderNewsletters = function(greatNewsCheckbox) {
        var elem = jQuery('#jq-mynth');
        if (greatNewsCheckbox.is(':checked')) {
            elem.show();
        } else {
            elem.hide();
        }
    };

    this.removeMssSchool = function(schoolCheckbox) {
        schoolCheckbox.parent().remove();

        var numMssSchools = parseInt(jQuery('#jq-numMssSchools').val());
        jQuery('#jq-numMssSchools').val(numMssSchools-1);
        jQuery('#jq-mssAddSchoolSection').show();
    };

    this.stateChange = function(stateSelect, citySelect) {
        var params = {
            state: stateSelect.val(),
            type: 'city',
            notListedOption: '2. Choose city'
        };

        jQuery('#jq-school').html('<option value="0">3. Choose school</option>');
        citySelect.html('<option value="0">Loading ...</option>');

        jQuery.get('/util/ajax/ajaxCity.page', params, function(data) {
            citySelect.html(data);
        });
    };

    this.emailCityChange = function(citySelect) {
        var parentState = jQuery('#jq-stateAdd').val();
        var parentCity = citySelect.val();
        var school = jQuery('#jq-school');

        var params = {
            state: parentState,
            city: parentCity,
            notListedOption: '3. Choose school'
        };

        school.html('<option value="0">Loading ...</option>');

        jQuery.get('/util/ajax/ajaxCity.page', params, function(data) {
            school.html(data);
        });
    };

    // don't allow duplicates
    // don't allow more than 4
    // don't allow invalid options
    this.addMssSchool = function() {
        // validate numSchools
        var numMssSchools = parseInt(jQuery('#jq-numMssSchools').val());
        if (numMssSchools == 4) {
            alert('You can track a maximum of four schools.');
            jQuery('#jq-mssAddSchoolSection').hide(); // how'd they get here? Better hide this control
            return;
        }
        var schoolSelect = jQuery('#jq-school');
        // validate valid options
        if (parseInt(schoolSelect.val()) < 1) {
            alert('Please select a school.');
            return;
        }
        // pull some data
        var selectedSchoolName = schoolSelect.find('option:selected').text();
        var citySelect = jQuery('#jq-citySelect');
        var selectedCityName = citySelect.find('option:selected').text();
        var stateSelect = jQuery('#jq-stateAdd');
        var selectedStateName = stateSelect.find('option:selected').text();
        var myStateId = selectedStateName + schoolSelect.val();

        // validate duplicates
        jQuery('#jq-mssSchoolSection input[name=uniqueStateId').each(function() {
            if (jQuery(this).val() == myStateId) {
                alert("You are already subscribed to that school.");
                return;
            }
        });

        numMssSchools = numMssSchools + 1;

        // construct containing div
        var newMssSchoolLine = jQuery('#jq-mssSchoolLineTemplate').clone();
        newMssSchoolLine.removeAttr('id');
        newMssSchoolLine.removeClass('hidden');

        // construct checkbox
        var schoolCheckbox = newMssSchoolLine.find('input[type=checkbox]');
        // have to use defaultChecked for IE, as it doesn't respect the value of checked before element is in dom
        schoolCheckbox.attr('defaultChecked','true');
        schoolCheckbox.attr('id','jq-mssSchool' + numMssSchools);

        // construct label
        var schoolNameLabel = newMssSchoolLine.find('label');
        schoolNameLabel.attr('for','jq-mssSchool' + numMssSchools);
        schoolNameLabel.text(selectedSchoolName + ', ' + selectedCityName + ', ' + selectedStateName);

        // construct hidden input
        var uniqueHiddenValue = newMssSchoolLine.find('input[name=uniqueStateId]');
        uniqueHiddenValue.val(myStateId);

        // insert into dom
        jQuery('#jq-mssSchoolSection').append(newMssSchoolLine);
        // increment numSchools
        jQuery('#jq-numMssSchools').val(numMssSchools);
        if (numMssSchools >= 4) {
            jQuery('#jq-mssAddSchoolSection').hide();
        }
    };
};

jQuery(function() {
    GS.form.emailManagement = GS.form.emailManagement || new GS.form.EmailManagement();

    jQuery('#jq-greatnews').click(function() {
        GS.form.emailManagement.toggleNthGraderNewsletters(jQuery(this));
    });

    jQuery('#jq-mssSchoolSection input[type=checkbox]').livequery('click', function() {
        GS.form.emailManagement.removeMssSchool(jQuery(this));
    });

    jQuery('#jq-addMssSchool').click(function() {
        GS.form.emailManagement.addMssSchool();
        return false;
    });

    jQuery('#jq-userState').change(function() {
        GS.form.emailManagement.stateChange(jQuery(this), jQuery('#jq-yourLocationCitySelect'));
    });

    jQuery('#jq-stateAdd').change(function() {
        GS.form.emailManagement.stateChange(jQuery(this), jQuery('#jq-citySelect'));
    });

    jQuery('#jq-citySelect').change(function() {
        GS.form.emailManagement.emailCityChange(jQuery(this));
    });
});

