function cityChange(citySelect, selectId) {
    $(selectId).innerHTML = '<select name="city" class="selectCity"><option value="">Loading ...</option></select>';
    new Ajax.Updater(selectId, '/test/schoolsInCity.page', {
        method: 'get',
        parameters: {state : document.getElementById('userState').value, city : citySelect.value,onchange:'schoolChange(this)',includePrivateSchools :true,chooseSchoolLabel :'Choose a school'},
        onComplete: function(transport) {

        }
    });
}

function stateChange(stateSelect) {
    var url = '/community/registrationAjax.page';
    var pars = 'state=' + stateSelect.value + "&type=city&showNotListed=false&onchange=cityChange(this,'schools')";
    $('city').innerHTML = '<select name="city" class="selectCity"><option value="">Loading ...</option></select>';
    new Ajax.Updater(
            'city',
            url,
    {
        method: 'get',
        parameters: pars,
        onComplete: function(transport) {
        }
    });
}

function schoolChange(school) {
    var schoolObj = school;
    if (school.value != '') {
        var url = '/school/schoolForParentReview.page';
        new Ajax.Request(
                url,
        {
            method: 'get',
            parameters: {schoolId : school.value, state : document.getElementById('userState').value},
            onComplete: showResponse
        });

    }

}

function showResponse(x) {

    var isRatingInfoPresent = (x.responseText.indexOf('noRatingInfo') == -1);
    var isPreschool = (x.responseText.indexOf('isPreschool') != -1);
    var isPublic = (x.responseText.indexOf('isPublic') != -1);
    $('schoolAddress').style.paddingLeft = '0px';
    var schoolInfoArray = x.responseText.split(";");
    document.getElementById('schoolNameHeader').innerHTML = schoolInfoArray[0];

    if (document.getElementById('gsSchoolRating').childNodes.length > 0) {
        removeChildrenFromNode(document.getElementById('gsSchoolRating'));
    }

    if (schoolInfoArray[1] != "" && isRatingInfoPresent && schoolInfoArray[1] > 0) {
        var image = document.createElement('img');
        image.setAttribute('alt', 'GreatSchools Rating: ' + schoolInfoArray[1] + ' out of 10. Greatschools Ratings are based on test results. 10 is best.');
        image.setAttribute('src', '/res/img/school/ratings/ratings_gs_head_' + schoolInfoArray[1] + '.gif');
        image.setAttribute('class', 'rating_gs');
        document.getElementById('gsSchoolRating').appendChild(image);
        document.getElementById('gsSchoolRating').style.display = '';
    } else {
        document.getElementById('gsSchoolRating').style.display = 'none';
    }

    if (schoolInfoArray[2] != "" && schoolInfoArray[2] > 0) {
        document.getElementById('overallParentRating').innerHTML = '<img class="sm_stars" alt="Parent Rating: ' + schoolInfoArray[2] + ' out of 5 stars" src="/res/img/school/ratings/ratings_parent_head_' + schoolInfoArray[2] + '.gif"/>';
        document.getElementById('overallParentRating').style.display = '';
        if (schoolInfoArray[3] != "" && schoolInfoArray[3] > 0) {
            document.getElementById('parentRatingCount').innerHTML = 'Based on ' + schoolInfoArray[3] + ' ratings';
            document.getElementById('parentRatingCount').style.display = '';
        } else {
            document.getElementById('parentRatingCount').style.display = 'none';
        }
    } else {
        document.getElementById('overallParentRating').style.display = 'none';
        document.getElementById('parentRatingCount').style.display = 'none';
        if (schoolInfoArray[1] != "" && isRatingInfoPresent && schoolInfoArray[1] > 0) {
            $('schoolAddress').style.paddingLeft = '50px';
        }
    }


    if (schoolInfoArray[4] != "" && schoolInfoArray[5] == "") {
        document.getElementById('schoolAddressLine1').innerHTML = schoolInfoArray[4];
        document.getElementById('schoolAddressLine3').innerHTML = schoolInfoArray[6] + ',' + schoolInfoArray[7] + ' ' + schoolInfoArray[8];
        document.getElementById('schoolAddressLine4').innerHTML = schoolInfoArray[9] + ' ' + 'county';
    }
    else if (schoolInfoArray[4] != "" && schoolInfoArray[5] != "") {
        document.getElementById('schoolAddressLine1').innerHTML = schoolInfoArray[4];
        document.getElementById('schoolAddressLine2').innerHTML = schoolInfoArray[4];
        document.getElementById('schoolAddressLine3').innerHTML = schoolInfoArray[6] + ',' + schoolInfoArray[7] + ' ' + schoolInfoArray[8];
        document.getElementById('schoolAddressLine4').innerHTML = schoolInfoArray[9] + ' ' + 'county';
    }
    if (isPublic) {
        document.getElementById('weeklyEmails').innerHTML = '<input id="wantMssNL" type="checkbox" name="wantMssNL" value="yes" checked="checked" class="parentReviewChkBoxes"/> <div>Sign me up for weekly email updates from GreatSchools, including periodic updates about ' + schoolInfoArray[0] + '.</div>';
    } else {
        document.getElementById('weeklyEmails').innerHTML = '<input id="wantMssNL" type="checkbox" name="wantMssNL" value="yes" checked="checked" class="parentReviewChkBoxes"/> <div>Sign me up for weekly email updates from GreatSchools.</div>';
    }
    document.getElementById('schoolId').value = schoolInfoArray[10];
    document.getElementById('schoolState').value = schoolInfoArray[7];

    removeChildrenFromNode(document.getElementById('principalsLink'));
    var principalsHref = document.createElement('a');
    principalsHref.setAttribute('href', schoolInfoArray[11]);
    principalsHref.appendChild(document.createTextNode("Principal,sumit your review here >"));
    document.getElementById('principalsLink').appendChild(principalsHref);


    if (isPreschool) {
        var nonPreschoolRatingStars = document.getElementById('nonPreSchoolStars');
        var preschoolRatingStars = document.getElementById('preSchoolStars');
        preschoolRatingStars.style.display = '';
        nonPreschoolRatingStars.style.display = 'none';
    } else {
        var preschoolRatingStars = document.getElementById('preSchoolStars');
        var nonPreschoolRatingStars = document.getElementById('nonPreSchoolStars');
        preschoolRatingStars.style.display = 'none';
        nonPreschoolRatingStars.style.display = '';
    }

    if ($('parentReviewForm').style.display != 'none') {
        var prForm = document.getElementById('parentReviewForm');
        prForm.style.display = '';
        var review = document.getElementById('completeReview');
        review.style.display = '';
        var reviewBtn = document.getElementById('reviewThisSchoolButton');
        reviewBtn.style.display = 'none';
    }

}

var starSelected = false;
function onLoadCities() {
    if (document.getElementById('userState').value != '') {
        var url = '/community/registrationAjax.page';
        var pars = 'state=' + document.getElementById('userState').value + "&type=city&showNotListed=false&onchange=cityChange(this,'schools')";
        $('city').innerHTML = '<select name="city" class="selectCity"><option value="">Loading ...</option></select>';
        var myAjax = new Ajax.Updater(
                'city',
                url,
        {
            method: 'get',
            parameters: pars,
            onComplete: function(transport) {
            }
        });

    } else {
        $('city').innerHTML = '<select name="city" class="selectCity"><option value="Choose a city">Choose a city</option></select>';
    }

    var starHandler = function(event) {
        starSelected = true;
    };

    $('validateStar1').observe('click', starHandler);
    $('validateStar2').observe('click', starHandler);
    $('validateStar3').observe('click', starHandler);
    $('validateStar4').observe('click', starHandler);
    $('validateStar5').observe('click', starHandler);

}

function validateReview() {
    var noError = true;
    var height = 360;

    if (!starSelected && ((document.getElementById('reviewText').value == '') || (document.getElementById('reviewText').value == 'Enter your review here'))) {
        document.getElementById('reviewRatingError').style.display = '';
        document.getElementById('parentRating').style.height = height + 28 + 'px';
        document.getElementById('categoryRatings').style.height = height + 28 + 'px';
        height = height + 29;
        noError = false;
    } else {
        document.getElementById('reviewRatingError').style.display = 'none';
    }

    if (document.getElementById('reviewEmail').value == '' || (document.getElementById('reviewEmail').value == 'Enter your email address') || (validateEmail(document.getElementById('reviewEmail').value) == false)) {
        document.getElementById('emailError').style.display = '';
        document.getElementById('parentRating').style.height = height + 28 + 'px';
        document.getElementById('categoryRatings').style.height = height + 28 + 'px';
        height = height + 29;
        noError = false;
    } else {
        document.getElementById('emailError').style.display = 'none';
    }

    if (!document.getElementById('permission').checked) {
        document.getElementById('termsError').style.display = '';
        document.getElementById('parentRating').style.height = height + 28 + 'px';
        document.getElementById('categoryRatings').style.height = height + 28 + 'px';
        height = height + 29;
        noError = false;
    } else {
        document.getElementById('termsError').style.display = 'none';
    }
    if (!noError) {
        document.getElementById('reviewAndGuidlines').style.marginTop = '8px';
    } else {
        if (document.getElementById('reviewText').value == 'Enter your review here') {
            document.getElementById('reviewText').value = "";
        }
    }
    return noError;
}

function validateEmail(elementValue) {
    var emailPattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
    return emailPattern.test(elementValue);
}


function removeChildrenFromNode(node)
{
    var len = node.childNodes.length;

    while (node.hasChildNodes())
    {
        node.removeChild(node.firstChild);
    }
}

function reviewThisSchool() {
    var errMsg = 'Please enter the following feilds : ';
    var noError = true;

    if ((document.getElementById('userState').value == '') || (document.getElementById('userState').value == 'Choose a state')) {
        errMsg = errMsg + '\nPlease select a state.';
        noError = false;
    }

    if ((document.getElementById('citySelect').value == '') || (document.getElementById('citySelect').value == 'Choose a school')) {
        errMsg = errMsg + '\nPlease select a city.';
        noError = false;
    }

    if ((document.getElementById('schoolSelect').value == '') || (document.getElementById('schoolSelect').value == 'Choose a school')) {
        errMsg = errMsg + '\nPlease select a school.';
        noError = false;
    }

    if (!noError) {
        alert(errMsg);
    } else {
        if ($('parentReviewForm').style.display == 'none') {
            var prForm = document.getElementById('parentReviewForm');
            prForm.style.display = '';
            var review = document.getElementById('completeReview');
            review.style.display = '';
            var reviewBtn = document.getElementById('reviewThisSchoolButton');
            reviewBtn.style.display = 'none';
        }
    }

    return false;
}