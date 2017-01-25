/**
 * Oil.view.js
 *
 * Oil View
 * 
 * Based on the Work of Timo Grossenbacher
 * 
 * (c) 2015, SAP, Matthias Nott, @_eMaX_
 */
sap.ui.jsview("hcpcu.view.oil.Oil", {
  /**
   * Specifies the Controller belonging to this View. In the case that it is
   * not implemented, or that "null" is returned, this View does not have a
   * Controller.
   * 
   * @memberOf view.NewFeatues
   */
    getControllerName: function() {
        return "hcpcu.view.oil.Oil";
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
      var hdr  = [];
      
      /*
       * A FlexBox for the Header Content
       * 
       * TODO: Cleanup. We've added some alignments here which we finally
       *       ended up not needing, as they are better CSS driven.
       */
      var oHdrBox = new sap.m.FlexBox("MyHdrOil", {
        width: "100%",
        height: "100%",        	
        direction: sap.m.FlexDirection.Row,
        justifyContent : sap.m.FlexJustifyContent.Center,
        alignItems: sap.m.FlexAlignItems.Center,
        
        fitContainer: true,
      });
      hdr.push(oHdrBox);
      
      /*
       * The button to sort the data chart by.
       * 
       * TODO: Cleanup. There's hard coded strings, and generally,
       *       we should abstract from "consumption" etc.
       */
      var sortButton = new sap.ui.commons.ToggleButton({
      	text : (oController.order == 'consumption') ? "Sort by Consumption" : "Sort by Production",
      	tooltip : "Sort Order",
      	pressed : (oController.order == 'consumption') ? true : false,
      	press : function() {
      		if (oController.order != 'consumption') {
      			oController.order = 'consumption';
      			this.setText("Sort by Consumption");
      			this.pressed = true;
      		} else {
      			oController.order = 'production';
      			this.setText("Sort by Production");
      			this.pressed = false;
      		}
      		oController.displayData();
      	},
      	VerticalAlign: 'middle'
      });
      sortButton.addStyleClass("middle");
      oHdrBox.addItem(sortButton);
      oHdrBox.addItem(new sap.m.ToolbarSpacer({ width: '20px'}));
      
      
      /*
       * Label for the "Year:"
       */
      oHdrBox.addItem(new sap.m.Label({text: "Year: "})).addStyleClass("middle");;
      oHdrBox.addItem(new sap.m.ToolbarSpacer({ width: '10px'}));
      var oYearLabel = new sap.m.Label({
      	id : 'oilyearLabel',
      	title: oController.CURRENTYEAR
      }).addStyleClass("middle");
      
      
      /*
       * Slider to select the year.
       * 
       * TODO: Better control of width.
       */
      var oYearSlider = new sap.ui.commons.Slider({
      	id : 'oilyearSlider',
      	tooltip: 'Year',
      	width: parseInt(oController.yswidth)+'px',
      	height: '48px',
      	min: oController.STARTYEAR,
      	max: oController.ENDYEAR,
      	value: oController.CURRENTYEAR,
      	smallStepWidth: 1,
      	totalUnits: 5,
      	stepLabels : false,
      	liveChange: function(){
      		var yearValue = oYearSlider.getValue();
      		oYearLabel.setText(yearValue);
      		oController.year = parseInt(yearValue);
      		oController.displayData();
      	  }, 
      	change : function(){
      		var yearValue = oYearSlider.getValue();
      		oYearLabel.setText(yearValue);
      		oController.year = parseInt(yearValue);
      		oController.displayData();
      	  }
      	});
      oHdrBox.addItem(oYearLabel);
      oYearLabel.setText(oController.CURRENTYEAR);
      oHdrBox.addItem(new sap.m.ToolbarSpacer({ width: '20px'}));
      oHdrBox.addItem(oYearSlider);
      oController.yearSlider = oYearSlider;
      oHdrBox.addItem(new sap.m.ToolbarSpacer({ width: '40px'}));
      
      /*
       * Container for the main Content.
       * 
       * TODO: Improve UI5 integration. This approach
       *       is needed only for the injection part
       *       of the D3 graphs:
       *       
       *       var ui = (new Element('div', {
       *         'id': 'ui'
       *       })).inject(document.getElement('#container'));
       *       
       *       and should be done differently.
       */
      var myContainer = '<div id="container"></div>';
      var myhtml = new sap.ui.core.HTML();
      myhtml.setContent(myContainer);
      what.push(myhtml);

      /*
       * Optional: Add a Launchpad button to the
       *           right side, if launchpad mode
       *           is active.
       */
      var oBtnLaunchpad = new sap.m.Button({
          icon : "sap-icon://home",
          visible : $.app.config.LaunchpadMode,
          tooltip : "Back to Launchpad",
          press : function(ev) {
              sap.ui.getCore().getEventBus().publish("nav", "back", {id : "Launchpad"});
          }
      });
      oHdrBox.addItem(oBtnLaunchpad);

      /*
       * Return the content
       */
      return new sap.m.Page({
          title: "{title}",
          showNavButton: "{device>/isPhone}",
          navButtonPress: [oController.doNavBack, oController],
          content: [what],
          headerContent: [hdr],
          footer: new sap.m.Bar({})
      });
    }
});
