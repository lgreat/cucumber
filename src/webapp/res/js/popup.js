var sdf = (	navigator.userAgent.indexOf('MSIE 5') != -1	&&	navigator.userAgent.indexOf('Mac') != -1);

var W3CDOM = (!sdf &&  document.getElementsByTagName && document.createElement);

window.onload = initialize;
function initialize () {

/* Hide nifty stuff from old browsers */
	if (W3CDOM)
	{
			var langspan = document.createElement('span');
			langspan.className = 'smaller lang';

			var x = document.getElementsByTagName('a');
			for (var i=0;i<x.length;i++)
			{
					if (x[i].getAttribute('type') == 'popup')
					{
						x[i].onclick = function () {
							return pop(this.href);
						}
						//x[i].innerHTML += '<span class="smaller"> (pop)</span>';
					}
			}
	}
	if (self.init) self.init();
}

// Popup
var popUp = null;
function pop(url)
{
	if (popUp && !popUp.closed)
		popUp.location.href = url;
	else
		popUp = window.open(url,'popUp','toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=yes, width=610,height=600,left=50,top=50');
	popUp.focus();
	return false;
}