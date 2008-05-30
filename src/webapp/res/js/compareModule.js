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
    var url = 'citiesAjax.page';
    var pars = 'state=' + stateSelect.value;
    $(cityDivId).innerHTML = '<select name="city" id="' + citySelectId + '" class="compareCitySelect"><option value="">Loading ...</option></select>';
    new Ajax.Updater (
            cityDivId,
            url,
            {
                method: 'get',
                parameters: pars
            });
}
