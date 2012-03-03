Function.prototype.gs_bind = function(obj) {
    var method = this;
    return function() {
        return method.apply(obj, arguments);
    };
};

var GS = GS || {};
GS.util = GS.util || {};

// containerId = dom ID including the hash (#)
// submit callback: gets called when user hits submit. signature = callback(stateAbbreviation, cityName, schoolId)
// options: defaultState, defaultCity, defaultSchoolId
GS.util.CityStateSchoolSelect = function (containerId, submitCallback, options) {
    this.container = jQuery(containerId);
    this.CITIES_AJAX_URL = "/community/registrationAjax.page";
    this.SCHOOLS_AJAX_URL = "/community/registration2Ajax.page";

    this.stateSelect = jQuery('#js_stateAdd');
    this.citySelect = jQuery('#js_city');
    this.schoolSelect = jQuery('#js_school');

    this.submitCallback = submitCallback;
    this.options = options;

    //Handle State drop down change.
    this.stateChange = function () {

        this.citySelect.html('<option value="0">Loading ...</option>');
        this.schoolSelect.html('<option value="0">- Choose school -</option>');

        jQuery.ajax({
            type:'GET',
            url:this.CITIES_AJAX_URL,
            data:{state:this.stateSelect.val(), type:'city', format:'json'},
            dataType:'json',
            async:true
        }).done(function (data) {
            this.parseCities(data);
        }.gs_bind(this));

    }.gs_bind(this);

    //Handle City drop down change.
    this.cityChange = function () {

        var state = this.stateSelect.val();
        var city = this.citySelect.val();
        var excludePreschoolsOnly = this.options.excludePreschoolsOnly !== undefined
                && this.options.excludePreschoolsOnly === true;

        if (state !== '' && city !== '- Choose city -' && city !== 'My city is not listed') {
            jQuery('#js_school').html("<option>Loading...</option>");
            jQuery.ajax({
                type:'GET',
                url:this.SCHOOLS_AJAX_URL,
                data:{state:state, city:city, format:'json', type:'school', excludePreschoolsOnly: excludePreschoolsOnly},
                dataType:'json',
                async:true
            }).done(function (data) {
                this.parseSchools(data);
            }.gs_bind(this));
        }

    }.gs_bind(this);

    this.parseCities = function (data) {
        var citySelect = this.citySelect;
        if (data.cities) {
            citySelect.empty();
            for (var x = 0; x < data.cities.length; x++) {
                var city = data.cities[x];
                if (city.name) {
                    citySelect.append("<option value=\"" + city.name + "\">" + city.name + "</option>");
                }
            }

            // if defaultCity option set, update the select box after loading in cities
            if (this.options && this.options.defaultCity !== undefined) {
                this.citySelect.val(options.defaultCity);
                this.cityChange();
            }
        }
    }.gs_bind(this);

    this.parseSchools = function (data) {
        var schoolSelect = this.schoolSelect;
        if (data.schools) {
            schoolSelect.empty();
            for (var x = 0; x < data.schools.length; x++) {
                var school = data.schools[x];
                if (school.name && school.id) {
                    schoolSelect.append("<option value=\"" + school.id + "\">" + school.name + "</option>");
                }
            }

            if (this.options && this.options.defaultSchoolId !== undefined) {
                this.schoolSelect.val(this.options.defaultSchoolId);
            }
        }
    }.gs_bind(this);

    // attach event handlers

    this.stateSelect.change(function() {
        this.stateChange();
    }.gs_bind(this));

    this.citySelect.change(function() {
        this.cityChange();
    }.gs_bind(this));

    this.container.on('click','button',function() {
        this.submitCallback(
                this.stateSelect.val(),
                this.citySelect.val(),
                this.schoolSelect.val()
        );

    }.gs_bind(this));

    if (options && options.defaultState !== undefined) {
        this.stateChange(); // preload cities for chosen state. State will already be selected
    }


};

