// Step 3

function downloadWorksheet() {
    var selector = document.getElementById('worksheet');
    var path = selector.options[selector.selectedIndex].value;
    var newWindow = window.open(path, '_blank');
    newWindow.focus();
    return false;
}

// Next

function whatYourChildShouldKnow() {
    var selector = document.getElementById('shouldKnow');
    var articleId = selector.options[selector.selectedIndex].value;
    window.location.href = '/cgi-bin/showarticle/' + articleId;
    return false;
}

// GreatSchools Top 5

function viewTopSchools() {
    var selector = document.getElementById('top5State');
    var longStateName = selector.options[selector.selectedIndex].innerHTML.toLowerCase();
    window.location.href = '/top-high-schools/' + longStateName.replace(' ', '-') + '/';
    return false;
}