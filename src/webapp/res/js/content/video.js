
function delvePlayerCallback(playerId, eventName, data) {
    switch (eventName) {
        case 'onMediaLoad':
            doOnMediaLoad(data);
            break;
    }
}

function doOnPlayerLoad() {
}

function doOnChannelLoad(e) {
}

function doOnMediaLoad(e) {
    var vid = getParam('id');
    DelvePlayer.doSkipToIndex(vid);
}

function doOnPlayStateChanged(e) {
}

function doOnPlayheadUpdate(e) {
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
