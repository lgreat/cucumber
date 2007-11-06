function showStars(numStars) {
    setDisplay(numStars);
}

function setStars(numStars) {
    document.getElementById('overallStarRating').value = numStars;
    setDisplay(numStars);
    return false;
}

function resetStars() {
    setDisplay(document.getElementById('overallStarRating').value);
}

function setDisplay(numStars) {
    document.getElementById('currentStarDisplay').style.width = 20*numStars + '%';
    var title = '';

    switch (parseInt(numStars)) {
        case 1: title = 'Unsatisfactory'; break;
        case 2: title = 'Below Average'; break;
        case 3: title = 'Average'; break;
        case 4: title = 'Above Average'; break;
        case 5: title = 'Excellent'; break;
        default: title = document.getElementById('hdnSchoolName').value; break;  
    }
    document.getElementById('ratingTitle').innerHTML = title;
}

function setSubmitFields() {
    document.getElementById('confirm').value = document.getElementById('reviewEmail').value;
    if (document.getElementById('reviewText').value == 'Enter your review here') {
        document.getElementById('reviewText').value = "";
    }
}

var countWords = makeCountWords(150);

//Uses yui to handle ajax call.  Callback object initialized in tag file and used in processResult
var AjaxObject = {
    handleSuccess:function(o){
        this.processResult(o);
    },

    handleFailure:function(o){
        var ul = this.getErrorNode();
        var li = document.createElement('li');
        var liText = document.createTextNode('Sorry your request could not be handled at this time. Please try again later.');
        li.appendChild(liText);
        ul.appendChild(li);
    },

    processResult:function(o){
        var ul = this.getErrorNode();
        var jsonResponse = o.responseText.parseJSON();
        if (jsonResponse.status) {
            document.getElementById('submitReview').disabled = true;
            var email = document.getElementById('reviewEmail').value;
            var successUrl = o.argument[0] + '&email='+email;
            var referPage = o.argument[1];
            if (referPage && referPage != 'hover') {
                showPopWin(successUrl, 344, 362, null, 'ratingPage');
            } else {
                location.href = successUrl;
            }
        } else {
            for (var i=0;i<jsonResponse.errors.length;i++) {
                var li = document.createElement('li');
                var liText = document.createTextNode(jsonResponse.errors[i]);
                li.appendChild(liText);
                ul.appendChild(li);
            }
        }
    },

    startRequest:function(url,callback) {
        setSubmitFields();
        var formObject = document.getElementById('frmPRModule');
        YAHOO.util.Connect.setForm(formObject);
        YAHOO.util.Connect.asyncRequest('POST', url, callback, "");
    },

    getErrorNode:function() {
        var ul = document.getElementById('frmErrors');
        if (ul==null) {
            ul = document.createElement('ul');
            ul.id = "frmErrors";
            var div = document.getElementById('userInput');
            ul = div.parentNode.insertBefore(ul,div);
        } else {
            while ( ul.hasChildNodes() ) { ul.removeChild(ul.firstChild)};
        }
        return ul;
    }
};