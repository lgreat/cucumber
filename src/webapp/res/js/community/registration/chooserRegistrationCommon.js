function scrollToError() {
    window.location.href = document.location.pathname + '#error';
}

function stateChange(stateSelect) {
    var url = 'registrationAjax.page';
    var pars = 'state=' + stateSelect.value + "&type=city&showNotListed=true&citySelectName=schoolChoiceCity";
    $('city').innerHTML = '<select name="schoolChoiceCity" class="selectCity"><option value="">Loading ...</option></select>';
    var myAjax = new Ajax.Updater(
            'city',
            url,
    {
        method: 'get',
        parameters: pars
    });
}