function addLoadEvent(func) {
  var oldonload = window.onload;
  if (typeof window.onload != 'function') {
    window.onload = func;
  } else {
    window.onload = function() {
      oldonload();
      func();
    };
  };
};
var utilHover = function() {
	// Support the standard nav without a class of nav.
	if(!document.getElementById("utilLinks")) return;
	var ut = document.getElementById("utilLinks");
	setHover(ut);
};
var subNavHover = function() {
	// Support the standard nav without a class of nav.
	if(!document.getElementById("topnav_menusubnav")) return;
	var sn = document.getElementById("topnav_menusubnav");
	setHover(sn);
};

function setHover(nav) {
	var ieULs = nav.getElementsByTagName('ul');
	if (navigator.appVersion.substr(22,3)!="5.0") {
		// IE script to cover <select> elements with <iframe>s
		for (j=0; j<ieULs.length; j++) {
		    if (ieULs[j].childNodes.length == 0)
		        continue;

			var ieMat=document.createElement('iframe');
            ieMat.className = "ieShim";
			if(document.location.protocol == "https:")
				ieMat.src="//0";
			else if(window.opera != "undefined")
				ieMat.src="";
			else
				ieMat.src="javascript:false";
			ieMat.scrolling="no";
			ieMat.frameBorder="0";
			ieMat.style.width=ieULs[j].offsetWidth+"px";
			ieMat.style.height=ieULs[j].offsetHeight+"px";
			ieMat.style.zIndex="-1";
			ieULs[j].insertBefore(ieMat, ieULs[j].childNodes[0]);
			ieULs[j].style.zIndex="101";
		}
	} else {
		// IE 5.0 doesn't support iframes so hide the select statements on hover and show on mouse out.
		// IE script to change class on mouseover
		var ieLIs = document.getElementById('dyMenu').getElementsByTagName('li');
		for (var i=0; i<ieLIs.length; i++) if (ieLIs[i]) {
			ieLIs[i].onmouseover=function() {this.className+=" over";hideSelects();};
			ieLIs[i].onmouseout=function() {this.className=this.className.replace(new RegExp(' over\\b'), '');showSelects()};
		}
	}
}

// If IE 5.0 hide and show the select statements.
function hideSelects(){
	var oSelects=document.getElementsByTagName("select");
	for(var i=0;i<oSelects.length;i++)
		oSelects[i].style.visibility="hidden";
};

function showSelects(){
	var oSelects=document.getElementsByTagName("select");
	for(var i=0;i<oSelects.length;i++)
		oSelects[i].style.visibility="visible";
};
if (window.attachEvent) window.attachEvent('onload', utilHover);

if (window.attachEvent) window.attachEvent('onload', subNavHover);
