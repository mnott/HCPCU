/**
 * global.js
 *
 * Space to put some global actions you
 * might want.
 *
 * Mostly always a bad idea: What you
 * put here will likely bite you at
 * some point if you use it productively.
 * 
 * (c) 2015, SAP, Matthias Nott, @_eMaX_
 */

/**
 * Helper Function for debugging.
 * 
 * TODO: Remove dynamically using some
 *       parameter on whether we are in
 *       production mode.
 */
function inspect (o, l) {
  var maxdepth = 10; // careful
  var r = "";
  if (l == undefined) {
    l = 0;
    r = "<html>";
  }
  r += "<ul>";
  for (var i in o) {
    r += "<li>" + i + " : " + (("object" == typeof o[i] && o[i] != o && l <= maxdepth) ? inspect(o[i], ++l) : o[i]) + "</li>";
  }
  r += "</ul>";
  if(l == 2) {
    r += "</html>";
  }
  return r;
}

/**
 * Do Something:
 * 
 * A placeholder method to try something when clicking
 * on the (optional) button under the menu bar.
 */
function doSomething () {
	$("#isbusy").toggle();
}