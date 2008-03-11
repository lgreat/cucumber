/*
 *  GS-5574
 *
 *  depends on global.js
 */

/*
 * ClickCapture object constructor
 */
function ClickCapture(){

    //alert("ClickCapture()");

    this.getData();
    this.clearData();
    //this.debug();

}

ClickCapture.prototype.cookieName = "OmnitureTracking"   ;

/*
 *  capture()
 *
 *  Saves the values to the cookie.
 *
 *  Note that multiple handlers may be associated to one event, so there may be multiple calls
 *  to this method.
 */
ClickCapture.prototype.capture = function (omnitureEntity, value) {
    //alert("capture(" + omnitureEntity + ", " + value + ")");
    subCookie.setObjectProperty(this.cookieName, omnitureEntity, value);
};

/*
 * getData()
 *
 * Gets the captured info from the this.cookie and clears the this.cookie
 */
ClickCapture.prototype.getData = function (){
    //alert("TrackItLoadFromCookie()");
    this.dataObject = subCookie.getObject(this.cookieName);
} ;

/*
 *  clearData
 *
 *  Clears this.cookie
 */
ClickCapture.prototype.clearData = function (){
    //alert("clearData()");
    subCookie.setObject(this.cookieName, new Array()) ;
};


/*
 *  debug
 *
 *  takes the captured data and passes it along to omniture
 */
ClickCapture.prototype.debug = function ()  {
    //alert("debug");
    var debugStr = "ClickCapture Debug";
    for(var propName in this.dataObject){
        debugStr += "\n" + propName + " = " + this.dataObject[propName];
    }
    alert(debugStr) ;
} ;

ClickCapture.prototype.getVariable = function(variableType, index, overrideValue){

    if (overrideValue != undefined && overrideValue.length > 0){
        return overrideValue;
    }

    if (this.dataObject == undefined){
        return null;
    }

    if (this.dataObject[variableType + index] == undefined){
        return null;
    }


    return this.dataObject[variableType + index];
} ;

ClickCapture.prototype.getProp = function( index, overrideValue){
    return this.getVariable("prop", index, overrideValue);
} ;

ClickCapture.prototype.getEVar = function( index, overrideValue){
    return this.getVariable("eVar", index, overrideValue);
}

ClickCapture.prototype.getEvents = function(pageEvents){

    // if pageEvents isn't null, merge it into the ones captured

    
} ;













