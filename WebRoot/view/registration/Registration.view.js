/**
 * Registration.view.js
 *
 * Registration View
 * 
 * (c) 2015, SAP, Matthias Nott, @_eMaX_
 */
sap.ui.jsview("hcpcu.view.registration.Registration", {
  getControllerName : function() {
    return "hcpcu.view.registration.Registration";
  },

  createContent : function(oController) {
    var that = this;
    var what = [];
    
    var oContainer = new sap.ui.commons.layout.MatrixLayout({
    	id : 'matrix2',
    	layoutFixed : false,
    	width : '100%',
    	columns : 2,
    	widths : ['100%'],
    	padding: '100px',
        align: "center"
    });
    
    what.push(oContainer);
    
    var oImage1 = new sap.ui.commons.Image({
    	src : "img/hcpcu.png",
    	alt : "HCP Catalogue Updater"
    });
    oContainer.createRow(oImage1);
    
    var panel = new sap.ui.commons.Panel({
      id: "regpanel",
      text: "User Registration", 
      width: "450px"
    });
    
    oContainer.createRow(panel);
    
    var oMatrix = new sap.ui.commons.layout.MatrixLayout({
    	id : 'matrix4',
    	layoutFixed : false,
    	width : '100%',
    	columns : 2,
    	widths : ['10%', '40%'],
    	padding: '100px'
    });
    
    panel.addContent(oMatrix);
    
    /*
     * company field
     */
    var oCompanyLabel = new sap.ui.commons.Label({text: 'Company:'});
    var oCompanyField = new sap.ui.commons.TextField({
      id : 'companyFieldId',
      value: '',
      width: '20em',
    });
    oCompanyLabel.setLabelFor(oCompanyField);
    oMatrix.createRow(oCompanyLabel, oCompanyField);
    
	/*
	 * first name field
	 */
	var oFirstNameLabel = new sap.ui.commons.Label({text : 'First Name:'});
	var oFirstNameField = new sap.ui.commons.TextField({
      id : 'firstNameFieldId',
	  value : '',
      width : '20em',
	});
	oFirstNameLabel.setLabelFor(oFirstNameField);    
	oMatrix.createRow(oFirstNameLabel, oFirstNameField);
	
    /*
     *  last name field
     */
    var oLastNameLabel = new sap.ui.commons.Label({text : 'Last Name:' });
    var oLastNameField = new sap.ui.commons.TextField({
      id : 'lastNameFieldId',
      value : '',
      width : '20em',
    });
    oLastNameLabel.setLabelFor(oLastNameField);	
	oMatrix.createRow(oLastNameLabel, oLastNameField);
	
	
    /*
     *  email field
     */
    var oEmailLabel = new sap.ui.commons.Label({text : 'Email:' });
    var oEmailField = new sap.ui.commons.TextField({
      id : 'emailFieldId',
      value : '',
      width : '20em',
      type: 'Email'
    });
    oEmailLabel.setLabelFor(oEmailField);	
	oMatrix.createRow(oEmailLabel, oEmailField);
	
	
    /*
     *  workshop field
     */
    var oWorkshopLabel = new sap.ui.commons.Label({text : 'Am interested in Workshop:' });
    var oWorkshopField = new sap.ui.commons.CheckBox({
      id : 'workshopFieldId',
      value : 'true'
    });
    oWorkshopLabel.setLabelFor(oWorkshopField);
	oMatrix.createRow(oWorkshopLabel, oWorkshopField);
    
	/*
	 * timeframe field
	 */
    var oWorkshopWLabel = new sap.ui.commons.Label({text : 'Approximate Timeframe:' });
    var oWorkshopWField = new sap.ui.commons.TextField({
        id : 'workshopWFieldId',
        value : '',
        width : '20em'
      });
    oWorkshopWLabel.setLabelFor(oWorkshopWField);	
	oMatrix.createRow(oWorkshopWLabel, oWorkshopWField);

	
	/*
	 * Scenario field
	 */
    var oScenarioLabel = new sap.ui.commons.Label({text : 'Describe your Scenario:' });
	var oScenarioField = new sap.ui.commons.TextArea({
		id : 'scenarioFieldId',
		rows : 3,
		width : "20em",
		maxLength : 8192,
		placeholder : "Max Length 8192"
	}).attachLiveChange(function(e) {
		// growing textarea
		var $ta = jQuery(this.getFocusDomRef());
		if (!$ta.data("first")) {
			$ta.data("first", true).css({
				"min-height" : $ta.outerHeight(),
				"overflow-y" : "hidden"
			});
		}
		$ta.height(0).height($ta[0].scrollHeight);
	});
	oScenarioLabel.setLabelFor(oScenarioField);
	oMatrix.createRow(oScenarioLabel, oScenarioField);

	
    // add button
    var oAddPersonButton = new sap.ui.commons.Button({
      id : 'addPersonButtonId',
      text : "Register",
      press : function() {
        oController.addNewPerson(
          sap.ui.getCore().getControl("companyFieldId").getValue(),
          sap.ui.getCore().getControl("firstNameFieldId").getValue(),
          sap.ui.getCore().getControl("lastNameFieldId").getValue(),
          sap.ui.getCore().getControl("emailFieldId").getValue(),
          sap.ui.getCore().getControl("workshopFieldId").getChecked()?1:0,
	      sap.ui.getCore().getControl("workshopWFieldId").getValue(),
	      sap.ui.getCore().getControl("scenarioFieldId").getValue()
        );
      }
    });	
    
    oMatrix.createRow(oAddPersonButton);

    return what;

  }
});