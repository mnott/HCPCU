/**
 * App.view.js
 *
 * Application View
 * 
 * (c) 2015, SAP, Matthias Nott, @_eMaX_
 */
sap.ui.jsview("hcpcu.view.app.App", {

  getControllerName: function() {
    return "hcpcu.view.app.App";
  },

  createContent: function(oController) {
    /*
     * Optionally disable launchpad mode by parameter
     */
    if(jQuery.sap.getUriParameters().get("mode") === "LeftMenuNavi"){
      $.app.config.launchpadMode = false;
    }
    
    /*
     * Handle web-app side i18n.
     */
    var oI18nModel = new sap.ui.model.resource.ResourceModel({
        bundleUrl: "i18n/i18n.properties"
    });
    sap.ui.getCore().setModel(oI18nModel, "i18n");
    this.setModel(oI18nModel, "i18n");
    
    
    /*
     * Handle the device model
     */
    var oDeviceModel = new sap.ui.model.json.JSONModel({
      isTouch:       sap.ui.Device.support.touch,
      isNoTouch:    !sap.ui.Device.support.touch,
      isPhone:       sap.ui.Device.system.phone && !$.app.config.launchpadMode,
      isNoPhone:    !sap.ui.Device.system.phone,
      listMode:     (sap.ui.Device.system.phone) ? "None" : "SingleSelectMaster",
      listItemType: (sap.ui.Device.system.phone) ? "Active" : "Inactive",
      launchpadMode: $.app.config.launchpadMode
    });
    oDeviceModel.setDefaultBindingMode("OneWay");
    sap.ui.getCore().setModel(oDeviceModel, "device");
    this.setModel(oDeviceModel, "device");

    
    /*
     * To avoid scrollbars on desktop the root
     * view must be set to block display
     */ 
    this.setDisplayBlock(true);

    
    /*
     * Configure the app layout
     */
    this.app = new sap.m.SplitApp({
      afterDetailNavigate: function() {
        if (sap.ui.Device.system.phone || $.app.config.launchpadMode) {
          this.hideMaster();
        }
      },
      homeIcon: {
	      'phone':    'img/57_iPhone_Desktop_Launch.png',
	      'phone@2':  'img/114_iPhone-Retina_Web_Clip.png',
	      'tablet':   'img/72_iPad_Desktop_Launch.png',
	      'tablet@2': 'img/144_iPad_Retina_Web_Clip.png',
	      'favicon':  'img/favicon.ico',
	      'precomposed': false
      }
    });
    
    
    /*
     * Handle launchpad mode
     */
    if($.app.config.launchpadMode){
        this.app.setMode(sap.m.SplitAppMode.HideMode);
    }
    
    
    /*
     * Add the Menu
     */
    this.app.addMasterPage(sap.ui.jsview("Menu", "hcpcu.view.menu.Menu"));

    
    /*
     * If in launchpad mode, add the launchpad
     */
    if($.app.config.launchpadMode){        
        this.app.addDetailPage(sap.ui.jsview("Launchpad", "hcpcu.view.launchpad.Launchpad"));
    }
    
    
    /*
     * Preload any pages that you want. These add runtime and possible
     * upfront backend requests to the loading of your application. If
     * you do not load a page here, it will be loaded lazily.
     */
    this.app.addDetailPage(sap.ui.xmlview("collector.Info",      "hcpcu.view.info.Info"));

    
    /*
     * Navigate to the first page in both master and detail areas.
     * The toMaster must be called after calling the toDetail,
     * because both of them point to the same reference in phone and
     * the real first page that will be shown in phone is the page in master area.
     * 
     * Notice, also, that the page navigated to needs to be preloaded (see above).
     */
    if($.app.config.launchpadMode){
        this.app.toDetail("Launchpad");
    } else {
        this.app.toDetail("Info");
        this.app.toMaster("Menu");
        setTimeout(function(){
        	$("#isbusy").hide();	
        },100);
    }
            
    return this.app;
  }
});
