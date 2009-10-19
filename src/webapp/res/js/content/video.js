function delvePlayerCallback(playerId, eventName, data) {
    switch (eventName) {
        case 'onChannelLoad':
            doOnChannelLoad(data);
    }
}


function doOnChannelLoad(e) {
    DelvePlayer.doSetAd("preroll", "Acudeo","programId=4a8d97cdc8979");
    var vid = getParam('id');
    vid = vid > 0 ? vid - 1 : 0;
    DelvePlayer.doSkipToIndex(vid);
    DelvePlayer.doPlay();
    DelvePlayer.doSetAdFrequency(1);
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
