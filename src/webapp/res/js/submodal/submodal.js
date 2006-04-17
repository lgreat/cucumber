/**
 * This derivative version of subModal can be downloaded from http://gabrito.com/files/subModal/
 * Original By Seth Banks (webmaster at subimage dot com)  http://www.subimage.com/
 * Contributions by Eric Angel (tab index code), Scott (hiding/showing selects for IE users), Todd Huss (submodal class on hrefs, moving div containers into javascript, phark method for putting close.gif into CSS), and Thomas Risberg (safari fixes for scroll amount)
 */

// Popup code
var gPopupMask = null;
var gPopupContainer = null;
var gPopFrame = null;
var gReturnFunc;
var gPopupIsShown = false;
var gHideSelects = false;
var gLoading = "loading.html";

//A reference to a link object.
//If assigned, user will be redirected to this page when the modal dialog is closed.
var gRedirectAnchor = null;

var gTabIndexes = new Array();
// Pre-defined list of tags we want to disable/enable tabbing into
var gTabbableTags = new Array("A","BUTTON","TEXTAREA","INPUT","IFRAME");

// If using Mozilla or Firefox, use Tab-key trap.
if (!document.all) {
	document.onkeypress = keyDownHandler;
}

function frameLoading() {
    return '<html><body><p><center>Loading...</center></p></body></html>';
}

/**
 * Initializes popup code on load.
 */
function initPopUp() {
	// Add the HTML to the body
	var body = document.getElementsByTagName('body')[0];
	var popmask = document.createElement('div');
	popmask.id = 'popupMask';
    popmask.setAttribute('style', 'display:none');
    var popcont = document.createElement('div');
	popcont.id = 'popupContainer';
    popcont.setAttribute('style', 'display:none');
    popcont.innerHTML = '' +
		'<div id="popupInner">' +
			'<div id="popupTitleBar">' +
				'<div id="popupTitle"></div>' +
				'<div id="popupControls">' +
					'<a onclick="hidePopWin(false);"><span>Close</span></a>' +
				'</div>' +
			'</div>' +
			'<iframe src="javascript:parent.frameLoading()" style="width:100%;height:100%;background-color:transparent;" scrolling="auto" frameborder="0" allowtransparency="true" id="popupFrame" name="popupFrame" width="100%" height="100%"></iframe>' +
            '<div id="popupCloseBtn"><button onclick="hidePopWin(false);">Close window</button></div>' +
        '</div>';
	body.appendChild(popmask);
	body.appendChild(popcont);

	gPopupMask = document.getElementById("popupMask");
	gPopupContainer = document.getElementById("popupContainer");
	gPopFrame = document.getElementById("popupFrame");

	// check to see if this is IE version 6 or lower. hide select boxes if so
	// maybe they'll fix this in version 7?
	var brsVersion = parseInt(window.navigator.appVersion.charAt(0), 10);
	if (brsVersion <= 6 && window.navigator.userAgent.indexOf("MSIE") > -1) {
		gHideSelects = true;
	}

	// Add onclick handlers to 'a' elements of class submodal or submodal-width-height
	var elms = document.getElementsByTagName('a');
	for (i = 0; i < elms.length; i++) {
		if (elms[i].className.indexOf("submodal") == 0) {
			// var onclick = 'function (){showPopWin(\''+elms[i].href+'\','+width+', '+height+', null);return false;};';
			// elms[i].onclick = eval(onclick);
			elms[i].onclick = function(){
				// default width and height
				var width = 400;
				var height = 200;
				// Parse out optional width and height from className
				params = this.className.split('-');
				if (params.length == 3) {
					width = parseInt(params[1]);
					height = parseInt(params[2]);
				}
				showPopWin(this.href,width,height,null); return false;
			}
		}
	}
}

 /**
	* @argument width - int in pixels
	* @argument height - int in pixels
	* @argument url - url to display
	* @argument returnFunc - function to call when returning true from the window.
	*/

function showPopWin(url, width, height, returnFunc) {
    if (!gPopupMask) {
        initPopUp();
    }
    gPopupIsShown = true;
	disableTabIndexes();
	gPopupMask.style.display = "block";
	gPopupContainer.style.display = "block";
	centerPopWin(width, height);
	var titleBarHeight = parseInt(document.getElementById("popupTitleBar").offsetHeight, 10);
	gPopupContainer.style.width = width + "px";
	gPopupContainer.style.height = (height+titleBarHeight) + "px";
	gPopFrame.style.width = parseInt(document.getElementById("popupTitleBar").offsetWidth, 10) + "px";
	gPopFrame.style.height = (height) + "px";
	gPopFrame.src = url;
	gReturnFunc = returnFunc;
	window.setTimeout("setPopTitleAndRewriteTargets();", 800);

    hideContainers();
}

//
var gi = 0;
function centerPopWin(width, height) {
	if (gPopupIsShown == true) {
		if (width == null || isNaN(width)) {
			width = gPopupContainer.offsetWidth;
		}
		if (height == null) {
			height = gPopupContainer.offsetHeight;
		}
		var fullHeight = getViewportHeight();
		var fullWidth = getViewportWidth();
		// scLeft and scTop changes by Thomas Risberg
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
		gPopupMask.style.height = fullHeight + "px";
		gPopupMask.style.width = fullWidth + "px";
		gPopupMask.style.top = scTop + "px";
		gPopupMask.style.left = scLeft + "px";
		window.status = gPopupMask.style.top + " " + gPopupMask.style.left + " " + gi++;
		var titleBarHeight = parseInt(document.getElementById("popupTitleBar").offsetHeight, 10);
		gPopupContainer.style.top = (scTop + ((fullHeight - (height+titleBarHeight)) / 2)) + "px";
		gPopupContainer.style.left =  (scLeft + ((fullWidth - width) / 2)) + "px";
		//alert(fullWidth + " " + width + " " + gPopupContainer.style.left);
	}
}
addEvent(window, "resize", centerPopWin);
//addEvent(window, "scroll", centerPopWin);
window.onscroll = centerPopWin;

/**
 * @argument callReturnFunc - bool - determines if we call the return function specified
 * @argument returnVal - anything - return value
 */
function hidePopWin(callReturnFunc) {
	gPopupIsShown = false;
	restoreTabIndexes();
	if (gPopupMask == null) {
		return;
	}
	gPopupMask.style.display = "none";
	gPopupContainer.style.display = "none";
	if (callReturnFunc == true && gReturnFunc != null) {
		gReturnFunc(window.frames["popupFrame"].returnVal);
	}
	gPopFrame.src = "javascript:parent.frameLoading()";

    if (gRedirectAnchor != null && gRedirectAnchor.href != null) {
        freeOnClickMem();
        location = gRedirectAnchor.href;
    } else {
        showContainers();
    }
}

/**
 * Sets the popup title based on the title of the html document it contains.
 * Also adds a base attribute so links and forms will jump out out of the iframe
 * unless a base or target is already explicitly set.
 * Uses a timeout to keep checking until the title is valid.
 */
function setPopTitleAndRewriteTargets() {
	if (window.frames["popupFrame"].document.title == null) {
		window.setTimeout("setPopTitleAndRewriteTargets();", 10);
	} else {
		var popupDocument = window.frames["popupFrame"].document;
		document.getElementById("popupTitle").innerHTML = popupDocument.title;
		if (popupDocument.getElementsByTagName('base').length < 1) {
			var aList  = window.frames["popupFrame"].document.getElementsByTagName('a');
			for (var i = 0; i < aList.length; i++) {
				if (aList.target == null) aList[i].target='_parent';
			}
			var fList  = window.frames["popupFrame"].document.getElementsByTagName('form');
			for (i = 0; i < fList.length; i++) {
				if (fList.target == null) fList[i].target='_parent';
			}
		}
	}
}

// Tab key trap. iff popup is shown and key was [TAB], suppress it.
// @argument e - event - keyboard event that caused this function to be called.
function keyDownHandler(e) {
    if (gPopupIsShown && e.keyCode == 9)  return false;
}

// For IE.  Go through predefined tags and disable tabbing into them.
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

// For IE. Restore tab-indexes.
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

/**
 * X-browser event handler attachment and detachment
 * @argument obj - the object to attach event to
 * @argument evType - name of the event - DONT ADD "on", pass only "mouseover", etc
 * @argument fn - function to call
 */
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

/**
 * Code below taken from - http://www.evolt.org/article/document_body_doctype_switching_and_more/17/30655/ *
 * Modified 4/22/04 to work with Opera/Moz (by webmaster at subimage dot com)
 * Gets the full width/height because it's different for most browsers.
 */
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
        if(el.id.indexOf("ad")==0){el.style.display='none';return el}
        //IE wants select boxes to have highest z-index
        else if (gHideSelects && el.tagName == "SELECT") {el.style.visibility="hidden";return el}
    }
    )
}

function showContainers() {
    var hidden = getElementsByCondition(
        function(el){
            if(el.id.indexOf("ad")==0){el.style.display='block';return el}
            //IE wants select boxes to have highest z-index
            else if (gHideSelects && el.tagName == "SELECT") {el.style.visibility="visible";return el}
        }
        )
}


//Show the modal dialog when user exits the current page by way of an outbound link
function showPopWinOnExit(url, width, height, returnFunc) {
    var arr = getElementsByCondition(
        function(el) {
            if (el.tagName == "A" && el.href != "" && el.target == "") return el; else return false;
        }
    )

    for (var i=0;i < arr.length;i++) {
        arr[i].onclick = function () {
            gRedirectAnchor = this;
            showPopWin(url, width, height, returnFunc);
            return false;
        }
    }
}

//code from http://www.webmasterworld.com/forum91/1729.htm
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

//Clear onclick references for all links in order to free memory
function freeOnClickMem() {
    var alltags=document.getElementsByTagName("A");

    for(var i=0;i<alltags.length;i++){
        alltags[i].onclick=null;
    }
}