/* http://google-ajax-examples.googlecode.com/svn/trunk/gspreadsheet/ */
var GSpreadsheet = function(key, json, options) {
  this.key = key;
  this.options = options || {};
  this.data = [];
  this.headers = [];
  this.index = [];

  for (var x = 0; x < json.feed.entry.length; x++) {
    var entry = json.feed.entry[x];
    var row = {};
    for (var i in entry) {
      if (i.indexOf('gsx$') == 0) {
        var key = i.substring(4);

        if (x == 0) {
          this.headers.push(key);
        }

        if (key == this.options['index']) {
          this.index[entry[i].$t] = x;
        }
        row[key] = entry[i].$t;
      }
    }
    this.data.push(row);
  }

  this.each = function(callback) {
    for (var x = 0; x < this.data.length; x++) {
      callback(this.data[x]);
    }
  };

  /* Take either a key (e.g. 'firstname') or the row id that you want */
  this.select = function(id) {
    if (typeof id == 'string') {
      return this.data[this.index[id]];
    } else {
      return this.data[id];
    }
  };
}

GSpreadsheet.load = function(key, options, callback) {
  if (!options['worksheet']) options['worksheet'] = 'od6';
  var worksheet = options['worksheet'];

  var callback_key = key.replace("-", "__");
  var callbackName = "GSpreadsheet.loader_" + callback_key + "_" + worksheet;
  eval(callbackName + " = function(json) { var gs = new GSpreadsheet(key, json, options); callback(gs); }");

  var script = document.createElement('script');

  script.setAttribute('src', 'http://spreadsheets.google.com/feeds/list/' + key + '/' + worksheet + '/public/values' +
                        '?alt=json-in-script&callback=' + callbackName);
  script.setAttribute('id', 'jsonScript');
  script.setAttribute('type', 'text/javascript');
  document.documentElement.firstChild.appendChild(script);
}