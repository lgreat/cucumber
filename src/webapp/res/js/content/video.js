function delvePlayerCallback(playerId, eventName, data) {
    switch (eventName) {
        case 'onMediaLoad':
            doOnMediaLoad(data);
    }
}

var initialized = false;

function doOnMediaLoad() {
  if (!initialized) {
    initialized = true;
    var vid = getParam('id');
    vid = vid > 0 ? vid - 1 : 0;
    if (DelvePlayer.doGetCurrentIndex() != vid) {
      DelvePlayer.doSkipToIndex(vid);
    }
    DelvePlayer.doPlay();
  }
}

function getParam( name )
{
    name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
    var urlstring = "[\\?&]"+name+"=([^&#]*)";
    var regexp = new RegExp( urlstring );
    var varArray = regexp.exec( window.location.href );
    if( varArray == null )
        return "";
    else
        return varArray[1];
}