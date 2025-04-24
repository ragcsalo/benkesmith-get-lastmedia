var exec = require('cordova/exec');

var GetMediaPlugin = {
  getLast: function(n, successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'GetMediaPlugin', 'getLast', [n]);
  }
};

module.exports = GetMediaPlugin;
