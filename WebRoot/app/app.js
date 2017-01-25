/**
 * app.js
 *
 * Provides Application Message Handing
 * 
 * (c) 2015, SAP, Matthias Nott, @_eMaX_
 */
$.app = {
	/**
	 * Root View gets loaded twice in some
	 * situations, so as a workaround, we
	 * keep a counter.
	 */
	rootViews : 0,
	
	/**
	 * We need to be careful because we use
	 * a debug option for our backend requests,
	 * but whether we want to be debugging does
	 * rely on the outcome of one of those
	 * requests.
	 * 
	 * @returns {Boolean}
	 */
  debug: function () {
  	if($.app.config == undefined || $.app.config.debug == undefined) {
  		return false;
  	}
  	return $.app.config.debug == "true";
  },
	
	/**
	 * Busy indicator function
	 * 
	 * @param busy
	 * @param caller
	 */
	busy: function(busy, caller) {
		if($.app.debug()) {
			console.log("busy: " + busy + " called from " + caller);
		}
		
		if(busy) {
			setTimeout(function() {
				$("#isbusy").show();
			}, 100);
		} else {
			setTimeout(function() {
				$("#isbusy").hide();
			}, 100);
		}
	},		

  /**
   * Simple synchronous backend access method
   * using Application proxy and AJAX.
   */  
  sync : function(type, parameters) {
		if(type == "raw" || type == "cfg") {
		  var result = $.ajax({
		      type: "POST",
		      url: "Application",
		      async: false,
		      data: {
		        'type'        : type,
		        'parameters'  : parameters,
		        'debug'       : $.app.debug() ? "true" : "false"
		      },
		      dataType: 'json'		  
		  }).responseText;
		  
		  return result;
		} else {
	    var result = JSON.parse($.ajax({
	      type: "POST",
	      url: "Application",
	      async: false,
	      data: {
	        'type'        : type,
	        'parameters'  : parameters,
	        'debug'       : $.app.debug() ? "true" : "false"
	      },
	      dataType: 'json'
	    }).responseText);
	    return result.response;
    }
  },

  /**
   * Simple asynchronous backend access method
   * using Application proxy and AJAX.
   */  
  async : function(type, parameters, caller, callback, callfail) { 
    $.ajax({
      type: "POST",
      url: "Application",
      data: {
        'type'        : type,
        'parameters'  : parameters,
        'debug'       : $.app.debug() ? "true" : "false"
      },
      dataType: 'json',
      // If we received a response from the server
      success: function(data, status, jqXHR) {
      	 //console.log(JSON.stringify(data));
      	 if(data.success == undefined) {
      		 if(callback != undefined) {
	            callback(caller, JSON.stringify(data));
	          }
      	 }
        // alert(JSON.stringify(data));
        // alert(JSON.stringify(data.response));
        if(data.success != undefined && data.success) {
          // alert(JSON.stringify(data.response));
          if(callback != undefined) {
            callback(caller, JSON.stringify(data.response));
          }
        } else {
          if(callfail != undefined && callfail != null) {
            callfail(caller, JSON.stringify(data.response));
          } else {
            jQuery.sap.log.info("Error: " + JSON.stringify(data.response));
          }
        }
      },
      // If there was no response from the server
      error: function(jqXHR, status, error) {
        jQuery.sap.log.info("Error: " + status);
      },
      // Capture the request before it is sent to the server
      beforeSend: function(jqXHR, settings) {
        // how to add a request parameter here
        // settings.data += "&something=whatever
        // how to disable some button
        // $('#mybutton').attr("disabled", true);
      },
      // Called after response or error functions return
      complete: function(jqXHR, status) {
        // how to enable some button
        // $('#myButton').attr("disabled", false);
      }
      
    });
  },
}