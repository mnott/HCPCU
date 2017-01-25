/**
 * Registration.controller.js
 *
 * Registration View Controller
 * 
 * (c) 2015, SAP, Matthias Nott, @_eMaX_
 */
sap.ui.controller("hcpcu.view.registration.Registration", {

  onInit : function() {
    var sOrigin = window.location.protocol + "//" + window.location.hostname
                + (window.location.port ? ":" + window.location.port : "");
    var page = window.location.pathname.split('/')[1];
    var personsListOdataServiceUrl = sOrigin + "/"+page+"/personslist.svc";

    //console.log(personsListOdataServiceUrl);
		
    var odataModel = new sap.ui.model.odata.ODataModel(personsListOdataServiceUrl);
    odataModel.setCountSupported(false);
    this.getView().setModel(odataModel);
  },

  addNewPerson : function(sCompany, sFirstName, sLastName, sEmail, sWorkshop, sWorkshopW, sScenario) {
    var persons = {};
    
    if(sCompany == "" || sLastName == "" || sEmail == "") {
	  sap.ui.commons.MessageBox.alert("Please enter at least Company, Last Name and Email!", function(result) {
		return;
      });
	  return;
    }
      
    /* Maps to JPA setters */
    persons.Company = sCompany;
    persons.Firstname = sFirstName;
    persons.Lastname = sLastName;
    persons.Email = sEmail;
    persons.Workshop = sWorkshop;
    persons.WorkshopDate = sWorkshopW;
    persons.Scenario = sScenario;

    this.getView().getModel().create("/Registrations", persons, null, this.successMsg, this.errorMsg);
  },

  successMsg : function() {
    sap.ui.commons.MessageBox.alert("Thank you!"/*function(result) {
        var sOrigin = window.location.protocol + "//" + window.location.hostname
        + (window.location.port ? ":" + window.location.port : "");
        var page = window.location.pathname.split('/')[1];
        var pdfUrl = sOrigin + "/"+page+"/hcpcu.pdf";
        window.location.href=pdfUrl;
    }*/);
  },

  errorMsg : function() {
    sap.ui.commons.MessageBox.alert("Error occured when creating person entity");
  },

});