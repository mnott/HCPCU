/**
 * Menu.controller.js
 *
 * Menu View Controller
 * 
 * (c) 2015, SAP, Matthias Nott, @_eMaX_
 */
jQuery.sap.require("sap.ui.model.json.JSONModel");

sap.ui.controller("hcpcu.view.menu.Menu", {

    onInit: function() {
	    $.app.async("JSONMenu", "menu.json", this, function(controller, result){
		    var model = new sap.ui.model.json.JSONModel(null);
		    model.setJSON(result);
		    controller.getView().setModel(model);
		    controller.bus = sap.ui.getCore().getEventBus();
		  }, null);
    	
    },

    doNavOnSelect: function(event) {
      if (sap.ui.Device.system.phone) {
        event.getParameter("listItem").setSelected(false);
      }
      this.bus.publish("nav", "to", {
        id: event.getParameter('listItem').getCustomData()[0].getValue(),
        type: event.getParameter('listItem').getCustomData()[1].getValue(),
      });
    },
});
