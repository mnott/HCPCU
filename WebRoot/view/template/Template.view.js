/**
 * Template.view.js
 *
 * Template View
 * 
 * Serves as a boilerplate
 * 
 * (c) 2015, SAP, Matthias Nott, @_eMaX_
 */
sap.ui.jsview("hcpcu.view.template.Template", {

  /**
   * Specifies the Controller belonging to this View. In the case that it is
   * not implemented, or that "null" is returned, this View does not have a
   * Controller.
   * 
   * @memberOf view.NewFeatues
   */
    getControllerName: function() {
        return "hcpcu.view.template.Template";
    },

    /**
     * Is initially called once after the Controller has been instantiated. It
     * is the place where the UI is constructed. Since the Controller is given
     * to this method, its event handlers can be attached right away.
     * 
     * @memberOf view.NewFeatues
     */
    createContent: function(oController) {
        var that = this;
        var what = [];

        
        /*
         * Dummy Content
         */
        var oLabel = new sap.m.Label({
          text : "Hello World",
        });
        what.push(oLabel);
        
        
        
        /*
         * Standard Content
         */
        var oBtnLaunchpad = new sap.m.Button({
            icon : "sap-icon://home",
            visible : $.app.config.LaunchpadMode,
            tooltip : "Back to Launchpad",
            press : function(ev) {
                sap.ui.getCore().getEventBus().publish("nav", "back", {id : "Launchpad"});
            }
        });

        return new sap.m.Page({
            title: "{title}",
            showNavButton: "{device>/isPhone}",
            navButtonPress: [oController.doNavBack, oController],
            content: [what],
            headerContent: [oBtnLaunchpad],
            footer: new sap.m.Bar({})
        });
    }

});
