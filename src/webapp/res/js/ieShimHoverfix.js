var setShim = function() {
    if(!document.getElementById("topnav_menusubnav")) return;
	var el = document.getElementById("topnav_menusubnav");
	var ieULs = el.getElementsByTagName('ul');
	for (var j=0; j<ieULs.length; j++) {
        var ieMat=document.createElement('iframe');
        if(document.location.protocol == "https:") {
            ieMat.src="//0";
        }
        else if(window.opera != "undefined"){
            ieMat.src="";
        }
        else {
            ieMat.src="javascript:false";
        }
        ieMat.scrolling="no";
        ieMat.frameBorder="0";
        ieMat.style.width=ieULs[j].offsetWidth+"px";
        ieMat.style.height=ieULs[j].offsetHeight+"px";
        ieMat.style.zIndex="-1";
        ieULs[j].insertBefore(ieMat, ieULs[j].childNodes[0]);
        ieULs[j].style.zIndex="101";

    }
};


// Run this only for IE.
if (window.attachEvent) window.attachEvent('onload', setShim);
// end
