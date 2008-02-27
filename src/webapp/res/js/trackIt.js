/*
 *  GS-5574
 *
 *  depends on global.js
 */

/*
 * ClickCapture object constructor
 */
function ClickCapture(pageName){
    this.pageName = pageName;
    //alert("ClickCapture(" + this.pageName + ")");
    /*
     * associate the methods to the ClickCapture object
     *
     * Note:  Since this object will only be created once per html page, it didn't seem important enough to
     * define the methods using the object's prototype property - jn
     */
    this.capture = TrackItCaptureSave2Cookie;
    this.getData = TrackItLoadFromCookie;
    this.clearData = TrackItClearCookieData;
    this.registerClickEventHandler = TrackItRegisterClickEventHandler;
    this.forward = TrackIt2Omniture;
    this.pageLoad = TrackItLifecyclePageLoad;
    this.registerCi9LInks = TrackItGetCi9Links;

    this.pageLoad();
}

function DataObject(){
    this.prop1 = "";
    this.prop2 = "";
    this.prop3 = "";
    this.prop4 = "";
    this.prop5 = "";
    this.prop6 = "";
    this.prop7 = "";
    this.prop8 = "";
    this.prop9 = "";
    this.prop10 = "";
    this.prop11 = "";
    this.prop12 = "";
    this.prop13 = "";

    this.eVar1 = "";
    this.eVar2 = "";
    this.eVar3 = "";
    this.eVar4 = "";
    this.eVar5 = "";
    this.eVar6 = "";
    this.eVar7 = "";
    this.eVar8 = "";
    this.eVar9 = "";

    this.event1 = "";
    this.event2 = "";
    this.event3 = "";
    this.event4 = "";
    this.event5 = "";
    this.event6 = "";
    this.event7 = "";
    this.event8 = "";
    this.event9 = "";
    this.event10 = "";

}


/*
 *  TrackItCapture2Cookie
 *
 *  Saves the values to the cookie.
 *
 *  Note that multiple handlers may be associated to one event, so there may be multiple calls
 *  to this method.
 */
function TrackItCaptureSave2Cookie(omnitureEntity, value) {
    //alert("TrackItCaptureSave2Cookie(" + omnitureEntity + ", " + value + ")");
    createCookie(omnitureEntity, value);
}

/*
 * TrackItLoadFromCookie
 *
 * Gets the captured info from the this.cookie and clears the this.cookie
 */
function TrackItLoadFromCookie(){
    this.dataObject = new DataObject();

    for (var propName in this.dataObject)  {
        //alert("TrackItLoadFromCookie: " + propName);
        this.dataObject[propName] = readCookie(propName) ;
    }

    /* lastly, clear the cookie */
    this.clearData();
}

/*
 *  TrackItClearCookieData
 *
 *  Clears this.cookie
 */
function TrackItClearCookieData(){
    /*
     * dataObject is disposable and is intentially not associated to this
     */
    var dataObject = new DataObject();

    for (var propName in dataObject)  {
        createCookie(propName, dataObject[propName]) ;
    }
}


/*
 *  TrackItRegisterClickEventHandler
 *
 *  registers the event handler to the object click event
 */
function TrackItRegisterClickEventHandler(obj, clickHandler) {
    //alert("TrackItRegisterClickEventHandler(" + obj.id + ")");
    registerEventHandler(obj,"click", clickHandler);
}

/*
 *  TrackRegisterClickEventHandlers
 *
 *  registers multiple click handlers to the object click event

function TrackRegisterClickEventHandlers(obj, clickHandlerList) {
    for (var clickHandler in clickHandlerList){
        this.registerClickEventHandler(obj, clickHandler);
    }
}
*/


/*
 *  TrackIt2Omniture
 *
 *  takes the captured data and passes it along to omniture
 */
function TrackIt2Omniture(){
    var debugStr = "What is sent to Omniture";

    for(var propName in this.dataObject){
        debugStr += "\n" + propName + ":" + this.dataObject[propName];
            this.dataObject[propName] = this.dataObject[propName];
    }

    alert("TrackIt2Omniture" + debugStr) ;
}


function TrackItLifecyclePageLoad(){
    //alert("TrackItLifecyclePageLoad()");
    this.getData();
    //this.forward();
    //this.dataObject = new DataObject();
}

function TrackItGetCi9Links(customerClickHandler){
    var ci9List = document.links;

    //var debugLinks = "";
    for(var i = 0; i < ci9List.length; i++){
        if (ci9List[i].id.indexOf("GS_") > -1)  {
            this.registerClickEventHandler(ci9List[i], customerClickHandler);
            //debugLinks += "\n" + ci9List[i].id + "\t" + ci9List[i].href;
        }
    }
    //alert(debugLinks);
}













