/**
 * Uploader.controller.js
 *
 * Uploader View Controller
 * 
 * Serves as a boilerplate
 * 
 * (c) 2015, SAP, Matthias Nott, @_eMaX_
 */
sap.ui.controller("hcpcu.view.uploader.Uploader", {

    onInit: function() {
      /*
       * Some example on how to get backend data on load
       * 
		    $.app.async("JSONMenu", "menu.json", this, function(controller, result){
			    var model = new sap.ui.model.json.JSONModel(null);
			    model.setJSON(result);
			    controller.getView().setModel(model);
			    controller.bus = sap.ui.getCore().getEventBus();
			  }, null);
      */
    },

    doNavBack: function(event) {
        this.bus.publish("nav", "back");
    }    
    
});
