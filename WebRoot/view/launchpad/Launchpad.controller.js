/**
 * Launchpad.controller.js
 *
 * Launchpad View Controller
 * 
 * (c) 2015, SAP, Matthias Nott, @_eMaX_
 */
sap.ui.controller("hcpcu.view.launchpad.Launchpad", {
  onInit: function() {
		/*
		 * Attach delegates to show and hide a busy
		 * screen. These are used when navigating to
		 * the view; they are not used when creating
		 * the view.
		 */    	
		var view = this.getView();
		view.addEventDelegate({
			onBeforeShow: function(evt) {
				view.setBindingContext(evt.data);
				//
      	        // Setting busy here leads to
      	        // unpredictable behavior.
				//
				// TODO: Investigate
				//
				$.app.busy(false, "Launchpad.controller.js - view.addEventDelegate.onBeforeShow");
			}
		}, view);

    view.addEventDelegate({
      onAfterShow : jQuery.proxy(function(evt) {
    		$.app.busy(false, "Launchpad.controller.js - view.addEventDelegate.onAfterShow");
       }, view)
    });
		
    this.bus = sap.ui.getCore().getEventBus();
  },

	doNavOnSelect : function (event, parameter, type) {
		this.bus.publish("nav", "to", {
			id : event, 
      data : parameter,
      type : type
		});		
	},
	
	onAfterRendering: function() {
	}
	
});

