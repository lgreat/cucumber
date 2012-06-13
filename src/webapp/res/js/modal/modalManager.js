/* =1 modal loading - can extend to more generic uses - Mitchell Seltzer

 // calling public methods is then as easy as:
showModal();
hideModal();

 -------------------------------------------------------------------------------------------*/

var modalManager = (function($) {
  var trackModal = {};

  function removeModal(id){
	  console.log("DELETE ID:"+id);
	  delete trackModal[id];
  }

	  
  function addModal (ml) {
     var keya = ml.getId();
	 console.log("ADD ID:"+keya);
	 trackModal[keya] = ml;
	 ml.showModal(); 
  }
  
  function checkModalExists (options) {
	  var settings = $.extend( {
            'layerId' : 'not set',
            'containerId' : 'fullPageOverlay',
            'overlay' : 'true',
            'content' : 'no content',
            'templateId' : '0',
            'closeButton' : 'false'
        }, options);	
	  var matchModal = 0;
	  $.each(trackModal, function(key, value) {  	
		if (value.getContainerId() == settings['containerId']) {
			if(settings['content'] === 'no content'){ 
				settings['content'] = $('#'+settings['layerId']).html();
			}			
			if (value.getContent() == settings['content']) {
				matchModal = value;
			}
		}
		
  	  });
	  return matchModal;
  }
  
  function generateUniqueId () {
     return Math.round(Math.random()*1000000000000000);
  }
  
  return{
    showModal: function( options ){
		if(!checkModalExists(options)){
			var temp = new ModalLayer(options, generateUniqueId());
			addModal(temp);
		}
    },

    hideModal: function( options ){
		var r = checkModalExists(options);
		if(r){
			r.hideModal();
			removeModal(r.getId());
		}
    }
  };
})(jQuery);

	// DialogOpen DialogClose
function ModalLayer(options, id){
    
	var mlUniqueId = 0;
	var layerId = "";
	var containerId = "";
	var templateId = "0";
	var overlay = "true";
	var content = "";
	var zIndex = "0";
	
	var setId = function( id ) { mlUniqueId = id; }
	var setLayerId = function( id ) { layerId = id; }
	var setContainerId = function( id ) { containerId = id; }
	var setTemplateId = function( id ) { templateId = id; }	
	var setOverlayState= function( state ) { overlay = state; }	
	var setZIndex= function( value ) { zIndex = value; }
	var setContent= function( str ) { content = str; }	
	
	var getId = function(  ) { return mlUniqueId; }
	var getLayerId = function(  ) { return layerId; }
	var getTemplateId = function(  ) {	return templateId; }
	var getOverlayState = function(  ) { return overlay; }
	var getZIndex = function(  ) { return zIndex; }
	var getContent = function(  ) { return content; }
	var getContainerId = function(  ) {	return containerId; }
	var showModal = function(  ){
		var containerObj = $('#'+getContainerId());
		var overlayState = "";
		if(this.getOverlayState() == 'true') { overlayState = '<div class="js-overlay"></div>' };
		containerObj.append('<div id="modallayer'+getId()+'">'
				+overlayState
				+'<div class="horizon"><div class="js-modal">'
				+getContent()+'</div></div>'+'</div>');
	}
	var hideModal = function( ) {
		$('#modallayer'+getId()).remove();
		console.log("REMOVE ID:"+getId());
		$('#'+getContainerId()).trigger("onModalClose");
	}
	
	var settings = $.extend( {
            'layerId' : 'not set',
            'containerId' : 'fullPageOverlay',
            'overlay' : 'true',
            'content' : 'no content',
            'templateId' : '0',
            'closeButton' : 'false'
        }, options);	

	
	setLayerId(settings['layerId']);
	setTemplateId(settings['templateId']);
	setContainerId(settings['containerId']);
	setOverlayState(settings['overlay']);
	setId(id);
	setContent(settings['content']);
	if(getContent() == 'no content') setContent($('#'+getLayerId()).html());			

	return{
		getId : getId,
		getLayerId : getLayerId,
		getTemplateId : getTemplateId,
		getOverlayState : getOverlayState,
		getZIndex : getZIndex,
		getContent : getContent,
		getContainerId : getContainerId,	
		showModal : showModal,
		hideModal : hideModal
	};	
}

$(document).ready(function(e) {
	$('#fullPageOverlay').bind('onModalClose', function(event) {
	  	console.log("onModalClose");
	});    
});

