function verifyCity(selectId) {
    var citySelect = document.getElementById(selectId);
    if (citySelect.value == 'Choose city' || citySelect.value == '') {
        alert("Please choose a city");
        citySelect.focus();
        return false;
    }
    return true;
}

function stateChange(stateSelect, cityDivId, citySelectId) {
    document.getElementById(cityDivId).innerHTML = '<select name="city" id="' + citySelectId + '" class="compareCitySelect"><option value="">Loading ...</option></select>';
    var url = '/citiesAjax.page';
    var params = {'state':stateSelect.value};
    jQuery.ajax({
            url: url,
            data: params,
            success: function(data) {
              jQuery('#' + cityDivId).html(data);
            },
            dataType: 'html'
        }
    );
}
