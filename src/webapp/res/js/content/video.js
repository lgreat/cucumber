
function delvePlayerCallback(playerId, eventName, data) {
    switch (eventName) {
        case 'onChannelLoad':
            doOnChannelLoad(data);
            break;
    }
}

function doOnPlayerLoad() {
}

function doOnChannelLoad(e) {
    var vid = getParam('id');
    vid = vid > 0 ? vid - 1 : 0;
    DelvePlayer.doSkipToIndex(vid);
}

function doOnMediaLoad(e) {
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
