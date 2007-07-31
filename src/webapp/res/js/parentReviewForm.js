function setStars(rating) {
    /** For some reason a loop wasn't working to set these style attributes. */
    document.getElementById('q_1').style.background="url(/res/img/school/review/emptyStars.gif) no-repeat 0 0;";
    document.getElementById('q_2').style.background="url(/res/img/school/review/emptyStars.gif) no-repeat 0 0;";
    document.getElementById('q_3').style.background="url(/res/img/school/review/emptyStars.gif) no-repeat 0 0;";
    document.getElementById('q_4').style.background="url(/res/img/school/review/emptyStars.gif) no-repeat 0 0;";
    document.getElementById('q_5').style.background="url(/res/img/school/review/emptyStars.gif) no-repeat 0 0;";

    if (rating > 0) { document.getElementById('q_1').style.background="url(/res/img/school/review/stars.gif) no-repeat 0px -23px;"; }
    if (rating > 1) { document.getElementById('q_2').style.background="url(/res/img/school/review/stars.gif) no-repeat 0px -23px;"; }
    if (rating > 2) { document.getElementById('q_3').style.background="url(/res/img/school/review/stars.gif) no-repeat 0px -23px;"; }
    if (rating > 3) { document.getElementById('q_4').style.background="url(/res/img/school/review/stars.gif) no-repeat 0px -23px;"; }
    if (rating > 4) { document.getElementById('q_5').style.background="url(/res/img/school/review/stars.gif) no-repeat 0px -23px;"; }
    return false;
}

function setRatingTitle(text, q) {
    document.getElementById('ratingTitle').innerHTML = text;
    setStars(q);
    return false;
}

var savedTitle;
var quality = "decline";

function resetRatingTitle() {
    if (savedTitle == undefined) {
        savedTitle = "Rate this school";
    }
    setRatingTitle(savedTitle, quality);
}

function setSubmitFields() {
    document.getElementById('confirm').value =
        document.getElementById('reviewEmail').value;
    document.getElementById('quality').value = quality;

    if (document.getElementById('reviewText').value == 'Enter your review here') {
        document.getElementById('reviewText').value = "";
    }
}

function saveQuality(title, q) {
    savedTitle = title;
    quality = q;
    setStars(q);
    return false;
}

function countWords(w){
    var MAXWORDS = 150;
    var y = w.value;
    var r = 0;
    var a = y.replace('\n',' ');
    a = y.replace('\t',' ');
    var z;
    for (z = 0; z < a.length; z++) {
        if (a.charAt(z) == ' ' && a.charAt(z-1) != ' ') { r++; }
 	    if (r > MAXWORDS) break;
    }

    if (r > MAXWORDS) {
        w.value = w.value.substr(0, z);
        alert("Please keep your review to "+MAXWORDS+" words or less.");
        return false;
    }
    return true;
}

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
            showPopWin(successUrl, 344, 362, null, 'ratingPage');
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
            var form = document.getElementById('frmPRModule');
            ul = form.parentNode.insertBefore(ul,form);
        } else {
            while ( ul.hasChildNodes() ) { ul.removeChild(ul.firstChild)};
        }
        return ul;
    }
};