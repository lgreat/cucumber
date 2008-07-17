if (GSType == undefined)
    var GSType = {};
if (GSType.submodal == undefined)
    GSType.submodal = {};

GSType.submodal.interceptor = {};
GSType.submodal.interceptor.evaluateFrequencyRule = function(cookieName, propertyName){
    //todo: implement a cookie based solution such that the user only sees the survey once in a lifetime.
    var dataObject = subCookie.getObject(cookieName);
    //alert(dataObject != undefined ? dataObject[propertyName]: "dataObject is undefined");
    if (dataObject == undefined) {
        return true;
    }
    if (dataObject[propertyName] == undefined){
        return true;
    }
    return  false;
};

GSType.submodal.interceptor.getComparisonNumber = function(){
    // returns a psuedo random (based on time in ms) number between 0 and 99
    var now = new Date();
    var ms = now.getTime();
    return ms % 100 ;
};


GSType.submodal.interceptor.userIsLoggedIn = function(cookieName){
    // if cookie MEMID exists, the user is logged in
    var value = readCookie(cookieName);

    if (value != undefined  && value.length > 0) {
        return true;
    } else {
        return false;
    }
};

var subModalInterceptor = {
    interceptPercent: 0,
    hoverName: '',
    hoverUrl: '',
    hoverHeight: 0,
    hoverWidth: 0,
    newHoverName: '',
    cookieName: '',
    cookieProperty: '',
    userLoggedInCookieName: '',
    ajaxRequest: null,
    evaluate: function(hoverName) {
        if (this.hoverName != hoverName){
            return false;
        }
        if (this.userIsLoggedIn(this.userLoggedInCookieName)){
            return false;
        }
        if (this.evaluateFrequencyRule != undefined) {
            if (!this.evaluateFrequencyRule()){
                return false;
            }
        }
        var x = this.getComparisonNumber();
        return x < this.interceptPercent;
    },
    init: function(){
        this.interceptPercent = 0;
        this.hoverName = 'hoverNewsMss';
        this.hoverUrl = '/promo/surveyPromoHover.page';
        this.hoverHeight = 0;
        this.hoverWidth = 0;
        this.newHoverName = 'suveryHover';
        this.cookieName = 'surveyHoverPromo';
        this.cookieProperty = 'GS-6660';
        this.evaluateFrequencyRule = GSType.submodal.interceptor.evaluateFrequencyRule;
        this.getComparisonNumber = GSType.submodal.interceptor.getComparisonNumber;
        this.userIsLoggedIn = GSType.submodal.interceptor.userIsLoggedIn;
        this.userLoggedInCookieName = 'MEMID';
    },
    setPresentedToUser: function(){
        subCookie.setObjectProperty(this.cookieName, this.cookieProperty, new Date().toDateString(),1000);
    },
    evaluateFrequencyRule: function(cookieName, propertyName){return false;},
    getComparisonNumber: function(){return 0;},
    userIsLoggedIn: function(cookieName){return false},
    getSurveyHoverInterceptConfiguration: function(){
        this.getInterceptPercentAjaxRequest(this.getInterceptPercentAjaxResponse);
    },
    getInterceptPercentAjaxRequest: function(callBack){
        this.ajaxRequest = new XMLHttpRequest();
        this.ajaxRequest.open('GET', '/promo/getSurveyHoverInterceptConfiguration.page', false);
        this.ajaxRequest.onreadystatechange = callBack;
        this.ajaxRequest.send(null);
    },
    getInterceptPercentAjaxResponse: function(){
        subModalInterceptor.interceptPercent = 0;

        if( subModalInterceptor.ajaxRequest.readyState == 4 && subModalInterceptor.ajaxRequest.status == 200 ) {
            var percent = parseInt(subModalInterceptor.ajaxRequest.responseText);
            if (percent != undefined && percent != NaN && percent >= 0 && percent <= 100) {
                subModalInterceptor.interceptPercent = percent;
            }
        }
    }
};

subModalInterceptor.init();
subModalInterceptor.getSurveyHoverInterceptConfiguration();

var gPopupMask = null;
var gPopupContainer = null;
var gPopFrame = null;
var POP_CONTAINER_ID    = 'popupContainer';
var POP_MASK_ID         = 'popupMask';
var POP_FRAME_ID        = 'popupFrame';
var STATE_WIDGET        = 'stateWidget'
var gPopupReturnFunction = null;
var gPopupIsShown = false;
var gHideSelects = false;
var gLoading = "loading.html";
var gRedirectAnchor = null;
var gTabIndexes = new Array();
var gTabbableTags = new Array("A","BUTTON","TEXTAREA","INPUT","IFRAME");
var gIsIE = window.navigator.userAgent.indexOf("MSIE") > -1 ? 1 : 0;
var gHoverName = "";
if (!document.all) {
	document.onkeypress = keyDownHandler;
}

function getElement(id) { return document.getElementById(id); }

function frameLoading() {
    return '<html><body><p><center>Loading...</center></p></body></html>';
}

function initPopUp(hoverName) {
    //if hover already initialized, early exit
    if (getElement(POP_MASK_ID)) {
        return;
    }
    gHoverName = hoverName;
    var body = document.getElementsByTagName('body')[0];
    var popmask = createContainer(body, POP_MASK_ID, true);
    var popcont = createContainer(body, POP_CONTAINER_ID, true);
    // check to see if this is IE version 6 or lower. hide select boxes if so
	// maybe they'll fix this in version 7?
	var brsVersion = parseInt(window.navigator.appVersion.charAt(0), 10);
	if (brsVersion <= 6 && window.navigator.userAgent.indexOf("MSIE") > -1) {
		gHideSelects = true;
	}
	var elms = document.getElementsByTagName('a');
	for (var i = 0; i < elms.length; i++) {
		if (elms[i].className.indexOf("submodal") == 0) {
           elms[i].onclick = subModalClickHandler;
        }
	}
}

function subModalClickHandler(e){
    //alert("subModalClickHandler");
    // default width and height
    var width = 400;
    var height = 200;
    var classes = "";
    var params = "";
    var obj = getNode(e);
    var className = obj.className;
    // Parse out optional width and height from className
    if(className.match(' ')){
        classes = className.split(' ');
        params = classes[0].split('-');
    }else{
        params = className.split('-');
    }
    if (params.length == 3) {
        width = parseInt(params[1]);
        height = parseInt(params[2]);
    }

    for (var n = 0; n< classes.length; n++){
        if (classes[n] == "GS_CI9_"){
            customInsight9ClickEventHandler(e);
            break;
        }
    }
    showPopWin(obj.href,width,height,null,gHoverName); return false;
}

function showPopWin(url, width, height, returnFunc, hoverName) {
    if (!getElement(POP_MASK_ID)) {
        initPopUp(hoverName);
    }
    gPopupReturnFunction = returnFunc;

    getElement(POP_MASK_ID).style.display = "block";
    var popContainer = getElement(POP_CONTAINER_ID);
    popContainer.style.display = "block";
    gPopupIsShown = true;
	disableTabIndexes();

    if (hoverName == STATE_WIDGET) {
        innerStateWidget(popContainer);
    } else {
        innerDefaultWidget(popContainer);
        setHoverCookie(hoverName);
    }
    centerPopWin(width, height);
    var titleBarHeight = parseInt(document.getElementById("popupTitleBar").offsetHeight, 10);

    popContainer.style.width = width + "px";
	popContainer.style.height = (height+titleBarHeight) + "px";

    var userAgent = navigator.userAgent.toLowerCase()
    var isIE6 = (userAgent.indexOf("msie 6") != -1);
    var accountForBorder = isIE6 ? 0 : 8;

    var popFrame = getElement(POP_FRAME_ID);
    popFrame.style.width = (parseInt(document.getElementById("popupTitleBar").offsetWidth, 10) - accountForBorder) + "px";
	popFrame.style.height = (height) + "px";
	popFrame.src = url;

    window.setTimeout("setPopTitleAndRewriteTargets();", 800);
    hideContainers();
}

var gi = 0;
function centerPopWin(width, height) {
	var popContainer = getElement(POP_CONTAINER_ID);

    if (gPopupIsShown == true) {
		if (width == null || isNaN(width)) {
			width = popContainer.offsetWidth;
		}
		if (height == null) {
			height = popContainer.offsetHeight;
		}
		var fullHeight = getViewportHeight();
		var fullWidth = getViewportWidth();
		var scLeft,scTop;
		if (self.pageYOffset) {
			scLeft = self.pageXOffset;
			scTop = self.pageYOffset;
		} else if (document.documentElement && document.documentElement.scrollTop) {
			scLeft = document.documentElement.scrollLeft;
			scTop = document.documentElement.scrollTop;
		} else if (document.body) {
			scLeft = document.body.scrollLeft;
			scTop = document.body.scrollTop;
		}

        var popMask = getElement(POP_MASK_ID);
        popMask.style.height = fullHeight + "px";
		popMask.style.width = fullWidth + "px";
		popMask.style.top = scTop + "px";
		popMask.style.left = scLeft + "px";
		window.status = popMask.style.top + " " + popMask.style.left + " " + gi++;
        var titleBarHeight = parseInt(document.getElementById("popupTitleBar").offsetHeight, 10);
        if (fullHeight > height) {
            popContainer.style.top = (scTop + ((fullHeight - (height+titleBarHeight)) / 2)) + "px";
        } else {
            // GS-3745 handle small windows
            popContainer.style.top = scTop + "px";            
        }
        if (fullWidth > width) {
            popContainer.style.left =  (scLeft + ((fullWidth - width) / 2)) + "px";
        }
    }
}
addEvent(window, "resize", centerPopWin);
window.onscroll = centerPopWin;

function hidePopWin(callReturnFunc) {
	gPopupIsShown = false;
	restoreTabIndexes();

    var popMask = getElement(POP_MASK_ID);
    if (popMask == null) {
        // we're in a a popup window, so close
        window.close();
        return;
    }
	popMask.style.display = "none";


    var popContainer = getElement(POP_CONTAINER_ID);
    popContainer.style.display = "none";

    var popFrame = getElement(POP_FRAME_ID);
    popFrame.src = "javascript:parent.frameLoading()";

    if (gRedirectAnchor != null && gRedirectAnchor.href != null) {
        freeOnClickMem();
        location = gRedirectAnchor.href;
    } else {
        showContainers();
        // GS-3744 implement callback function
        if (callReturnFunc && gPopupReturnFunction != null && typeof gPopupReturnFunction == 'function') {
            gPopupReturnFunction();
        }
    }
}

function setPopTitleAndRewriteTargets() {
	if (window.frames["popupFrame"].document == null || window.frames["popupFrame"].document.title == null) {
		window.setTimeout("setPopTitleAndRewriteTargets();", 10);
	} else {
		var popupDocument = window.frames["popupFrame"].document;
		if (popupDocument.getElementsByTagName('base').length < 1) {
			var aList  = window.frames["popupFrame"].document.getElementsByTagName('a');
			for (var i = 0; i < aList.length; i++) {
				if (aList[i].target == null) aList[i].target='_parent';
			}
			var fList  = window.frames["popupFrame"].document.getElementsByTagName('form');
			for (i = 0; i < fList.length; i++) {
				if (fList[i].target == null) fList[i].target='_parent';
			}
		}
	}
}

function keyDownHandler(e) {
    if (gPopupIsShown && e.keyCode == 9)  return false;
}

function disableTabIndexes() {
	if (document.all) {
		var i = 0;
		for (var j = 0; j < gTabbableTags.length; j++) {
			var tagElements = document.getElementsByTagName(gTabbableTags[j]);
			for (var k = 0 ; k < tagElements.length; k++) {
				gTabIndexes[i] = tagElements[k].tabIndex;
				tagElements[k].tabIndex="-1";
				i++;
			}
		}
	}
}

function restoreTabIndexes() {
	if (document.all) {
		var i = 0;
		for (var j = 0; j < gTabbableTags.length; j++) {
			var tagElements = document.getElementsByTagName(gTabbableTags[j]);
			for (var k = 0 ; k < tagElements.length; k++) {
				tagElements[k].tabIndex = gTabIndexes[i];
				tagElements[k].tabEnabled = true;
				i++;
			}
		}
	}
}

function addEvent(obj, evType, fn){
 if (obj.addEventListener){
    obj.addEventListener(evType, fn, false);
    return true;
 } else if (obj.attachEvent){
    var r = obj.attachEvent("on"+evType, fn);
    return r;
 } else {
    return false;
 }
}

function getViewportHeight() {
	if (window.innerHeight!=window.undefined) return window.innerHeight;
	if (document.compatMode=='CSS1Compat') return document.documentElement.clientHeight;
	if (document.body) return document.body.clientHeight;
	return window.undefined;
}

function getViewportWidth() {
	if (window.innerWidth!=window.undefined) return window.innerWidth;
	if (document.compatMode=='CSS1Compat') return document.documentElement.clientWidth;
	if (document.body) return document.body.clientWidth;
	return window.undefined;
}

function hideContainers() {
var hidden = getElementsByCondition(
    function(el) {
        try {
            if ((el.tagName == "OBJECT" && gIsIE) || el.id.indexOf("ad")==0){el.style.display='none';return el}
            else if ((gHideSelects && el.tagName == "SELECT")) {el.style.visibility="hidden";return el}
        } catch(err) {}
    }
    )
}

function showContainers() {
    var hidden = getElementsByCondition(
        function(el){
            try {
                if ((el.tagName == "OBJECT" && gIsIE) || el.id.indexOf("ad")==0){el.style.display='block';return el}
                else if ((gHideSelects && el.tagName == "SELECT")) {el.style.visibility="visible";return el}
            } catch(err) {}
        }
        )
}

function showPopWinOnExit(url, width, height, returnFunc, hoverName, forceShow) {
    if (forceShow || showHover()) {
        var arr = getElementsByCondition(
            function(el) {
                if (el.tagName == "A") {
                    if (el.target || el.onclick)
                        return false;
                    if (el.href && el.href != '')
                        return el;
                }
                return false;
            }
        )

        for (var i=0;i < arr.length;i++) {
            arr[i].onclick = function () {
                gRedirectAnchor = this;
                if (forceShow || showHover()) {
                    if (subModalInterceptor.evaluate(hoverName)){
                        url = subModalInterceptor.hoverUrl;
                        /*
                        hoverName = subModalInterceptor.newHoverName;
                        height = subModalInterceptor.height;
                        width = subModalInterceptor.width;
                        alert("showPopWinOnExit - subModalInterceptor");
                        */
                    }
                    showPopWin(url, width, height, returnFunc, hoverName);
                    return false;
                } else {
                    return true;
                }
            }
        }
    }
}

function showPopWinOnLoad(url, width, height, returnFunc, hoverName, forceShow) {
    if (forceShow || showHover()) {
        if (subModalInterceptor.evaluate(hoverName)){
            url = subModalInterceptor.hoverUrl;
            /*
            hoverName = subModalInterceptor.newHoverName;
            height = subModalInterceptor.height;
            width = subModalInterceptor.width;
            alert("showPopWinOnLoad - subModalInterceptor");
            */
        }
        showPopWin(url, width, height, returnFunc, hoverName);
    }
}

function getElementsByCondition(condition,container) {
    container = container||document
    var all = container.all||container.getElementsByTagName('*')

    var arr = []
    for(var k=0;k<all.length;k++)
    {
        var elm = all[k]
        if(condition(elm,k))
            arr[arr.length] = elm
    }
    all = null
    return arr
}

function freeOnClickMem() {
    var alltags=document.getElementsByTagName("A");

    for(var i=0;i<alltags.length;i++){
        alltags[i].onclick=null;
    }
}

function setHoverCookie(hoverName) {
    var oneDay=24 * 60 * 60 * 1000, expDate=new Date(), curDate=new Date();
    expDate.setTime(expDate.getTime()+oneDay * 15);
    document.cookie=hoverName+'='+curDate.toGMTString()+';expires='+expDate.toGMTString()+';path=/';
}

function showHover(hoverName) {
    var idx=parseInt(document.cookie.indexOf('MEMID'));
    if (idx>-1) {return false;}
    idx=parseInt(document.cookie.indexOf(hoverName));
    if (idx>-1) {return false;}
    idx=parseInt(document.cookie.indexOf('hover'));
    if (idx>-1) {return false;}
    return true;
}

function showStateWidget(url, width, height) {
    showPopWin(url, width, height, null, STATE_WIDGET);
}

function innerStateWidget(parent) {
    if (parent.firstChild) {
        parent.removeChild(parent.firstChild);
    }
    parent.innerHTML = '<div id="' + STATE_WIDGET+ '">' + modalWindowHtml('GreatSchools') + '</div>';;
    return parent;
}

function innerDefaultWidget(parent) {
    if (parent.firstChild) {
        parent.removeChild(parent.firstChild);
    }
    parent.innerHTML = modalWindowHtml('');
    return parent;
}

function modalWindowHtml(title) {
    var html =
        '<div id="popupInner">' +
            '<div id="popupTitleBar">' +
                '<div id="popupTitle">'+title+'</div>' +
                '<div id="popupControls">' +
                    '<a onclick="hidePopWin(false);"><img src="/res/img/submodal/close.gif"/></a>' +
                '</div>' +
            '</div>' +
            '<iframe src="javascript:parent.frameLoading()" style="width:100%;height:100%;" scrolling="no" frameborder="0" id="popupFrame" name="popupFrame" width="100%" height="100%"></iframe>' +
        '</div>';
    return html;
}

function createContainer(parent, id, hidden) {
    var _div = document.createElement('div');
    _div.id = id;

    if (hidden) {_div.setAttribute('style', 'display:none');}
    parent.appendChild(_div);

    return _div;
}