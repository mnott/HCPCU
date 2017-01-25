/**
 * config.js
 *
 * General Place for Configurations
 * 
 * (c) 2015, SAP, Matthias Nott, @_eMaX_
 */
$.app.controller = {
  
};

/**
 * Place to make available your backend configuration
 * options.
 */
$.app.config = {
  debug:         ($.app.sync("cfg", "debug") == "true"),
  apptitle:      ($.app.sync("cfg", "apptitle")),
  dbuse:         ($.app.sync("cfg", "dbuse") == "true"),
  launchpadMode: ($.app.sync("cfg", "launchpadmode") == "true")
};
