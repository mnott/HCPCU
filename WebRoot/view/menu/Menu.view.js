/**
 * Menu.view.js
 *
 * Menu View
 * 
 * (c) 2015, SAP, Matthias Nott, @_eMaX_
 */
sap.ui.jsview("hcpcu.view.menu.Menu", {
	
  getControllerName: function() {
    return "hcpcu.view.menu.Menu";
  },

  createContent: function(oController) {
    var oListTemplate = new sap.m.StandardListItem({
      title: "{title}",
      icon: "{icon}",
      description: "{description}",
      type: sap.m.ListType.Navigation,
      customData: [ 
          new sap.ui.core.CustomData({
          	key: "targetpage",
          	value: "{targetpage}" }), 
          new sap.ui.core.CustomData({
            key: "targetpagekind",
            value: "{targetpagetype}" })
      ]
    });

    var oList = new sap.m.List({
        selectionChange: [oController.doNavOnSelect, oController],
        mode: sap.m.ListMode.SingleSelectMaster
    });
    oList.bindAggregation("items", "/menu", oListTemplate);

    /*
     * Some debug option should we need
     * one button to test stuff with.
     */
    var aControls = [];
      
    if($.app.sync("cfg", "actionbutton") == "true") {
      var oButton = new sap.ui.commons.Button({
        id : this.createId("MyButton"),
        text : "Action Button"
      });
      aControls.push(oButton.attachPress($.app.clickActionButton));
		}
      
    return new sap.m.Page({
      customHeader: new sap.m.Bar({
        contentLeft: [new sap.m.Image("ui5Logo", {
          src: "img/57_iPhone_Desktop_Launch.png",
          width: "35px",
          height: "35px"
        })],
        contentMiddle: [new sap.m.Text({
          text: "{i18n>WELCOME_TITLE}"
        })]
      }),
      content: [oList],
      footer: new sap.m.Bar({
          contentMiddle: aControls
      })
    });
  }

});
