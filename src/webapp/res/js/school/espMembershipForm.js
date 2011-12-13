GS = GS || {};
GS.form = GS.form || {};
GS.form.EspForm = function() {

    this.stateChange = function(stateSelect, citySelect, updateSchoolSelect) {

        var params = {
            state: stateSelect.val(),
            type: 'city',
            notListedOption: '2. Choose city'
        };

        if (updateSchoolSelect) {
            jQuery('#jq-school').html('<option value="0">3. Choose school</option>');
        }
        citySelect.html('<option value="0">Loading ...</option>');

        jQuery.get('/util/ajax/ajaxCity.page', params, function(data) {
            citySelect.html(data.replace('</select>',''));
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
            school.html(data.replace('</select>',''));
        });
    };

};

jQuery(function() {
    GS.form.espForm = GS.form.espForm || new GS.form.EspForm();

    jQuery('#jq-stateAdd').change(function() {
        GS.form.espForm.stateChange(jQuery(this), jQuery('#jq-citySelect'), true);
    });

    jQuery('#jq-citySelect').change(function() {
        GS.form.espForm.emailCityChange(jQuery(this));
    });
});

