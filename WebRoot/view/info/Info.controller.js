/**
 * Info.controller.js
 *
 * About Screen View Controller
 * 
 * (c) 2015, SAP, Matthias Nott, @_eMaX_
 */
sap.ui.controller("hcpcu.view.info.Info", {

  onInit: function() {
      this.bus = sap.ui.getCore().getEventBus();
  },
  
  onAfterRendering: function() {
  	$("#Info--customername").html(" "+$.app.sync("cfg", "customer"));
  },

  doNavBackLaunchpad: function(event) {
      this.bus.publish("nav", "backToPage", {id : "Launchpad"});
  },

  doNavBack: function(event) {
      this.bus.publish("nav", "back");
  } 
});
