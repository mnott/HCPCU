<%--

 UI5 Boilerplate Application
 
 Based on the excellent boilerplate provided by
 
 https://github.com/6of5/UI5SplitApp-Boilerplate 
 
 (c) 2015, SAP, Matthias Nott, @_eMaX_
 
--%>

<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<jsp:useBean id="APP" class="com.sap.hcpcu.application.Application" scope="application" />


<!DOCTYPE HTML>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta http-equiv='Content-Type' content='text/html;charset=UTF-8'/>
    <meta charset="UTF-8">
    
    <title><%=APP.getAttribute("apptitle") %></title>
   
		<!-- 3rd Party Scripts -->
    <script src="js/queue.v1.min.js"          type="text/javascript" charset="utf-8"></script>
    <script src="js/topojson.v1.min.js"       type="text/javascript" charset="utf-8"></script>
    <script src="js/colorbrewer.js"           type="text/javascript" charset="utf-8"></script>
    <script src="js/mootools-core-1.4.5.js"   type="text/javascript" charset="utf-8"></script>
    <script src="js/mootools-more-1.4.0.1.js" type="text/javascript" charset="utf-8"></script>
    <script src="js/d3.min.js"                type="text/javascript" charset="utf-8" id="d3"></script>

    <script id="sap-ui-bootstrap" type="text/javascript" 
      src="resources/sap-ui-core.js" 
      data-sap-ui-theme="sap_bluecrystal" 
      data-sap-ui-xx-bindingSyntax="complex" 
      data-sap-ui-libs="sap.m, sap.ui.layout, sap.ui.commons, sap.suite.ui.commons"
      data-sap-ui-resourceroots='{
        "hcpcu": "./"
      }'>        
    </script>

	<!-- Application Scripts -->
    <script src="app/app.js"    type="text/javascript" charset="utf-8"></script>
    <script src="app/config.js" type="text/javascript" charset="utf-8"></script>
    <script src="app/global.js" type="text/javascript" charset="utf-8"></script>
        
    <!-- Custom Styles -->
    <link rel="stylesheet" type="text/css" href="css/style.css" />
    <link rel="stylesheet" type="text/css" href="css/base.css" />
    <link rel="stylesheet" type="text/css" href="css/skeleton.css" />
    <link rel="stylesheet" type="text/css" href="css/splash.css" />
    <link rel="stylesheet" type="text/css" href="css/detail.css" />

	<!-- View specific Styles -->
    <link rel="stylesheet" type="text/css" href="view/oil/Oil.styles.css" />
		
  </head>
  <body class="sapUiBody" role="application">
    <script>
   		/*
   		 * RootView is added more than once, so we catch it.
   		 */
       sap.ui.getCore().attachInit(function() {
       	if($.app.rootViews++ == 0) {
       		sap.ui.jsview("RootView", "hcpcu.view.app.App").placeAt('content');
       	}
       });
    </script>
    <div id="content"></div>
    <div id="isbusy" style="visible:none">
	    <div class="isbusy-text">Loading</div>
	    <div class="isbusy-circle-outer"></div>
	    <div class="isbusy-circle-inner"></div>
	  </div>
  </body>
</html>

