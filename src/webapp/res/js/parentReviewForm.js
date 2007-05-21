function setRatingTitle(text) {
    document.getElementById('ratingTitle').innerHTML = text;
    return false;
}

var savedTitle;
var quality = "decline";

function resetRatingTitle() {
    if (savedTitle == undefined) {
        savedTitle = "Rate this school";
    }
    setRatingTitle(savedTitle);
}

function setHiddenFields() {
    document.getElementById('confirm').value =
        document.getElementById('reviewEmail').value;
    document.getElementById('quality').value = quality;
}

function setQuality(title, q) {

    savedTitle = title;
    quality = q;

    /** For some reason a loop wasn't working to set these style attributes. */
    document.getElementById('q_1').style.background="url(/res/img/school/review/emptyStars.gif) no-repeat 0 0;";
    document.getElementById('q_2').style.background="url(/res/img/school/review/emptyStars.gif) no-repeat 0 0;";
    document.getElementById('q_3').style.background="url(/res/img/school/review/emptyStars.gif) no-repeat 0 0;";
    document.getElementById('q_4').style.background="url(/res/img/school/review/emptyStars.gif) no-repeat 0 0;";
    document.getElementById('q_5').style.background="url(/res/img/school/review/emptyStars.gif) no-repeat 0 0;";

    if (q > 0) { document.getElementById('q_1').style.background="url(/res/img/school/review/stars.gif) no-repeat 0px -23px;"; }
    if (q > 1) { document.getElementById('q_2').style.background="url(/res/img/school/review/stars.gif) no-repeat 0px -23px;"; }
    if (q > 2) { document.getElementById('q_3').style.background="url(/res/img/school/review/stars.gif) no-repeat 0px -23px;"; }
    if (q > 3) { document.getElementById('q_4').style.background="url(/res/img/school/review/stars.gif) no-repeat 0px -23px;"; }
    if (q > 4) { document.getElementById('q_5').style.background="url(/res/img/school/review/stars.gif) no-repeat 0px -23px;"; }
    return false;
}
