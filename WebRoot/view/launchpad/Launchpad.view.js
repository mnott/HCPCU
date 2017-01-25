/**
 * Launchpad.view.js
 *
 * Launchpad View
 * 
 * (c) 2015, SAP, Matthias Nott, @_eMaX_
 */
sap.ui.jsview("hcpcu.view.launchpad.Launchpad", {

  getControllerName: function() {
  	return "hcpcu.view.launchpad.Launchpad";
  },

  
  createContent: function(oController) {
  	if (!$.app.config.launchpadMode) return;
      var tc = new sap.m.TileContainer("tc", {});

      var data;
      if ($.app.config.dbuse) {
        /*
         * Database driven
         */
        data = $.app.sync("JSONMenu");
      } else {
        /*
         * File driven
         */
      	data = JSON.parse($.app.sync("raw", "menu.json"));
      }
      
      
    if($.app.config.debug) {
      //alert(JSON.stringify(data));
      console.log("Menu data: " + JSON.stringify(data));
    }
        
    function navFn(targetPage, targetPageType) {
      return function() {
        oController.doNavOnSelect(targetPage, null, targetPageType);
      };
    }        
        
    var m, menu;
    if(data && data.menu) {
      for (m = 0; m < data.menu.length; m++) {
        menu = data.menu[m];
        if($.app.config.debug) {
        	console.log("Registered Target Page: " + menu.targetpage);
        }
        tc.addTile(new sap.m.StandardTile({
          icon: menu.icon,
          title: menu.title,
          info: menu.info,
          infoState: menu.infostate,
          number: menu.number,
          numberUnit: menu.numberunit,
          press: navFn(menu.targetpage, menu.targetpagetype)
        }));
      }
    }
 
    var page = new sap.m.Page({
      setShowHeader: true,
      title: $.app.config.apptitle,
      footer: new sap.m.Bar({
			// contentMiddle: [new sap.m.Link("HCPCU", {
			//    text: "v0.1",
			//    href: "https://github.com/mnott/HCPCU"
			//  })]
      })
	  });
	  page.setEnableScrolling(false);
	  page.setShowHeader(true);
	  page.addContent(tc);
	
	  return page;
  }

});
