// This REQUIRES Prototype.js 1.6+ and Scriptaculous Effects.js
document.observe("dom:loaded", function() {
	// Setup handling for the tabbed info box
	var currentTab = '';
	$$('#tabbed-info .contents').each(function(el) {
		el.setStyle({
			'position': 'absolute',
			'top': '0px',
			'right': '0px'
		});
	});
	$$('#tabbed-info h3 a').each(function(el) {
		if (!el.hasClassName('active')) {
			$(el.id + '-contents').hide();
		} else {
			currentTab = el;
		}
		el.observe('click', function(e) {
			Event.stop(e);
			currentTab.removeClassName('active');
			$(currentTab.id + '-contents').hide();
			this.addClassName('active');
			$(this.id + '-contents').show();
			currentTab = this;
		});
	});
	
	// Setup the accordion effect
	function toggleClosed() {
		if (this.hasClassName('closed')) {
			this.removeClassName('closed');
		} else {
			this.addClassName('closed');
		}
	}
	
	$$('#browse-tips > li > a').each(function(el) {
		if (el.hasClassName('closed')) {
			el.next('.toggle').hide();
		}
		el.observe('click', function(e) {
			Event.stop(e);
			Effect.toggle(this.next('.toggle'), 'blind', {
				'duration': 1.0,
				'afterFinish': toggleClosed.bind(this)
			});
		});
	});
});