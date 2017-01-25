/**
 * Oil.controller.js
 *
 * Oil View Controller
 * 
 * Based on the Work of Timo Grossenbacher
 * 
 * (c) 2015, SAP, Matthias Nott, @_eMaX_
 */
sap.ui.controller("hcpcu.view.oil.Oil", {
	
	/* Global Data */
  consumption: {}, production: {}, names: {}, world: {},

  /* SVG container */
  svg: {},
  
  /* IPAD Defaults */
  IPWINDOWWIDTH      : 1024,
  IPWINDOWHEIGHT     :  672,
  IPYSWIDTH          :  480,
  IPTSWIDTH          :  550,
  IPTSHEIGHT         :  140,
  IPMAPSCALE         :  110,

  /* Runtime Values */
  yswidth            :  480,
  tswidth            :  550,
  tsheight           :  140,
  mapscale           :  110,
  
  
  /* Parameters */
  STARTYEAR          : 1985,
  ENDYEAR            : 2032,
  CURRENTYEAR        : 2015,
  LONCENTER          :   10,
  LATCENTER          :   30,
  
  
  MAPTRANSLATEX      :  120,
  MAPTRANSLATEY      :   60,
  BARCHARTTRANSLATEX : -120,
  BARCHARTTRANSLATEY :   10,
  TSTRANSLATEX       :  465,
  TSTRANSLATEY       :   20,
  MAXCIRCLERADIUS    :   30,
  MAXBARCHARTWIDTH   :  150,
  TRANSDURATION      : 1000,
  BARCHARTSHOWONLY   :   25,

  
  
  /* Global Variables */
  year               : 2015,
  numParts           :    1,
  partsReady         :    0,
  map                : null,
  projection         : null,
  path               : null,
  barchart           : null,
  timeseries         : null,
  data               : null,
  dataByCountry      : null,
  yearSlider         : null,
  order              : 'consumption',
  
  windowWidth        : $(window).width(),
  windowHeight       : $(window).height(),
  widthRatio         : 1,
  heightRatio        : 1,
  
  
	onInit: function() {
		/*
		 * Attach delegates to show and hide a busy
		 * screen. These are used when navigating to
		 * the view; they are not used when creating
		 * the view.
		 */
		var view = this.getView();
		view.addEventDelegate({
			onBeforeShow: function(evt) {
				view.setBindingContext(evt.data);
				$.app.busy(true, "hcpcu.oil.Oil.controller.js - view.addEventDelegate");
			}
		}, view); // give this (= the View) as additional parameter to make it available inside the delegate's functions as "this" object

    view.addEventDelegate({
      onAfterShow : jQuery.proxy(function(evt) {
    		$.app.busy(false, "hcpcu.oil.Oil.controller.js - view.addEventDelegate");
       }, view)
    });
	},

	
  /**
   * Get data
   */
  _rebindAll: function() {
  	var that = this;
    jQuery.sap.log.debug("> _rebindAll");

    that.windowHeight = $(window).height();
    that.windowWidth  = $(window).width();
    that.widthRatio   = that.windowWidth / that.IPWINDOWWIDTH;
    that.heightRatio  = that.windowHeight / that.IPWINDOWHEIGHT;
    that.resizeComponents();

    
    $(window).bind('resize', function () { 
      that.windowHeight = $(window).height();
      that.windowWidth  = $(window).width();
      that.widthRatio   = that.windowWidth / that.IPWINDOWWIDTH;
      that.heightRatio  = that.windowHeight / that.IPWINDOWHEIGHT;
      
      that.resizeComponents();
      //console.log("resize: " + that.windowWidth + "*" + that.windowHeight + " (" + that.widthRatio + "," + that.heightRatio + ")");

    });

    /*
     * Load Data
     */
    this.names       = d3.dsv(';').parse($.app.sync("raw", "world-country-names.csv", "false"));
    this.production  = d3.dsv(';').parse($.app.sync("raw", "production.csv", "false"));
    this.consumption = d3.dsv(';').parse($.app.sync("raw", "consumption.csv", "false"));
    this.world       = JSON.parse($.app.sync("raw", "world-110m.json", "false"));

  },
	
  /**
   * Resize Components with Window
   */
  resizeComponents: function() {
  	var that = this;
  	
    /*
     * Adjust Slider
     */
  	that.yswidth = that.IPYSWIDTH * that.widthRatio;
    that.yearSlider.setWidth(parseInt(that.yswidth) + 'px');
    
    /*
     * Adjust the Time Series
     */
    that.tswidth = that.IPTSWIDTH * that.widthRatio;
    that.tsheight = that.IPTSHEIGHT * that.heightRatio;
    
    /*
     * Adjust the Map
     */
    //that.mapscale = that.IPMAPSCALE * that.widthRatio;
    
    if(that.map != null) {
	    //that.map.transition().attr("transform", "scale(" + that.widthRatio + ")translate(-" + that.MAPTRANSLATEX*that.widthRatio + "," + that.MAPTRANSLATEY*that.heightRatio + ")");//center("+that.LONCENTER*that.widthRatio+","+that.LATCENTER*that.heightRatio+")")
    	//last working: that.map.transition().attr("transform", "scale(" + that.widthRatio + ")translate("+ 0+ "," +0+ ")");
    	//projection = d3.geo.mercator().rotate([0,0,0]).center([that.LONCENTER, that.LATCENTER]);
    	//that.map.transition().attr("transform", "scale(" + that.widthRatio + ")");
	    console.log(that.widthRatio + "/" + that.MAPTRANSLATEX);
	    
	    //.translate([width / 2, height / 2])
    }
    that.displayData();
  },
  

  
  doNavBack: function(event) {
    this.bus.publish("nav", "back");
  },
    
    
	onAfterRendering: function() {
		var that = this;

		/*
		 * This, together with the setTimeout,
		 * will allow the busy indicator to show.
		 * The onAfterShow will take care of removing
		 * the indicator.
		 */
		$.app.busy(true, "hcpcu.oil.Oil.controller.js - onAfterRendering");
		
    setTimeout( function (){
  		that._rebindAll();
  		/*
  		 * Add the UI Containers
  		 */
  		that.addUIContainers();
  		
      /*
       * Prepare the Map
       */
      that.prepareMap(that.world, that.names);
      
      /*
       * Prepare BarChart
       */
      that.prepareBarChart();

      /*
       * Prepare timeseries
       */
      that.prepareTimeseries();
      
      /*
       * Prepare the data
       */
      that.prepareData();
    }, 200);
    
	},
	
	/**
	 * Prepare data
	 */
	prepareData : function() {
		var that = this;
		
    /* 
     * data is an object, containing all countries for every year, where the year
     * is the index and the value is an array with all countries (can be easily used with d3 data joins)
     * every year contains only the countries where at least one of production or consumption is not NaN
     */
    that.data = {};
    for(var i = that.STARTYEAR; i <= that.ENDYEAR; i++){
      var countryarray = [];
      that.consumption.forEach(function(d,j){
        var country = {
        		'name': d.name, 
        		'id': parseInt(d.id), 
        		'consumption': parseInt(d[i + '']), 
        		'production': parseInt(that.production[j][i + ''])};
        if(typeOf(parseInt(country.production)) == 'number' || typeOf(parseInt(country.consumption)) == 'number')
          countryarray.push(country);
      });
      that.data[i + ''] = countryarray;
    }
    
    /*
     * this is used for the timeseries
     */ 
    that.dataByCountry = {};
    Object.each(that.data, function(yearData,year){
        yearData.forEach(function(country,i){
            // initialize array if not existing
            if(typeOf(that.dataByCountry[country.name]) != 'array'){
                that.dataByCountry[country.name] = [];
                
            } 
            that.dataByCountry[country.name].push({'year': year, 'consumption': country.consumption, 'production': country.production});         
        })
    });
    that.partReady();
    that.displayData();  
		
	},
	
	
	/**
	 * Add the container that will contain the
	 * actual charts
	 */
	addUIContainers: function() {
		var that = this;

    /*
     * Create the SVG
     */
    that.svg = d3.select('#container').append('svg');

    /*
     * Create the content div
     */
    var ui = (new Element('div', {
      'id': 'ui'
    })).inject(document.getElement('#container'));
	},
	
	/**
	 * Erhöht den partsReady-Counter um eins und überprüft, ob alle Teile schon geladen sind.
	 */
	partReady : function() {
		var that = this;
    that.partsReady++;
    that.checkLoader();
	},
	
	/**
	 * Überprüft, ob alle Teile schon geladen sind.
	 * Wenn ja, wird der Loading-Screen entfernt  und der Benutzer kann auf die App zugreifen.
	 */
	checkLoader : function() {
		var that = this;
	    if (that.partsReady >= that.numParts)
	        that.addUIContainers();
	        //createImpress();
	        // inject header to bottom of container
	        //document.getElement('#loading').destroy();
	},
  
  /**
   * Draws the initial map without any thematic data 
   */
  prepareMap: function(world, names){
  	var that = this;

  	/*
  	 * Projection =========================
  	 */
    projection = d3.geo.mercator().rotate([0,0,0]).center([that.LONCENTER, that.LATCENTER]);//.center([that.LONCENTER, that.LATCENTER]);
    that.mapscale = that.IPMAPSCALE * that.widthRatio;
    projection.scale(that.mapscale);
    that.projection = projection;
    // var graticule = d3.geo.graticule();

    
      // Path for projection
      path = d3.geo.path().projection(projection);
      that.path = path;
      
      that.map = that.svg
          .append('g')
          .attr('id', 'map')
          .attr('transform', 'translate(' + [that.MAPTRANSLATEX, that.MAPTRANSLATEY] + ')');
      
      var countries = topojson.feature(that.world, that.world.objects.countries).features;
      
      // attach names to country
      countries.forEach(function(d){
          var country = names.filter(function(n) { 
              return d.id == parseInt(n.id); 
          });
          if(country.length !== 0){
              d.name = country[0].name;
          }
      });
      
      var country = that.map.append('g').selectAll('.country').data(countries);

      country
          .enter()
          .append('path')
          .attr('class', 'country')    
          .attr('d', path)
          .attr('id', function(d){ return 'map-' + parseInt(d.id);})
          // tooltip /mouseover
          .on('mouseover', function(d){
          	that.selectCountry(d.id, true);
          })
          .on('mouseout', function(d){
          	that.unselectCountry(d.id, true);
          })

      // mesh for boundaries
      that.map
          .append('path')
          .datum(topojson.mesh(world, world.objects.countries, function(a, b) { return a !== b; }))
          .attr('d', path)
          .attr('class', 'country-boundaries');
          
			//      that.map
			//          .append('path')
			//          .datum(graticule)
			//          .attr('class','graticule')
			//          .attr('d', path);
      
      // groups for circles    
      that.map
          .append('g')
          .attr('id', 'consumptionGroup');
      that.map
          .append('g')
          .attr('id', 'productionGroup');
          
      // clip path definitions
      that.map.select('#consumptionGroup').append('defs');
      that.map.select('#productionGroup').append('defs');
      var productionClipPaths = that.map.select('#productionGroup').select('defs').selectAll('.productionClipPath')
          .data(countries);
      // clipping mask for consumption
      var consumptionClipPaths = that.map.select('#consumptionGroup').select('defs').selectAll('.consumptionClipPath')
          .data(countries);
      consumptionClipPaths
          .enter()
          .append('clipPath')
          .attr('id', function(d){ return 'cut-off-consumption-' + d.id})
          .append('rect')
          // give high enough width and height
          .attr('width', 300)
          .attr('height', 300)
          .attr('x', function(d){ return that.getCentroid(d.id)[0];})
          .attr('y', function(d){ return that.getCentroid(d.id)[1] - 150;})
      // clipping mask for production
      productionClipPaths
          .enter()
          .append('clipPath')
          .attr('id', function(d){ return 'cut-off-production-' + d.id})
          .append('rect')
          // give high enough width and height
          .attr('width', 300)
          .attr('height', 300)
          .attr('x', function(d){ return that.getCentroid(d.id)[0] - 300;})
          .attr('y', function(d){ return that.getCentroid(d.id)[1] - 150;})
  },
  
  /*
   * Helper function which returns the geographical centroid for the polygon with the given
   * id
   */
  getCentroid: function(id) {
  	var that = this;
  	
      // filter for the country with specified id
      var country = that.map.selectAll('.country').filter(function(d,i){
          return d.id === id;
      });
      // certain countries are spread all across the world...
      // in such cases, we need to "nudge" the centroids
      // USA
      if(id === 840){
          return projection([-97, 40]);
      // Russia
      } else if(id === 643){
          return projection([60, 60]);
      // France   
      } else if(id === 250){
          return projection([3, 47]);
      // Norway
      } else if(id === 578){
          return projection([6, 61]);
      // Canada
      } else if(id === 124){
          return projection([-110, 60]);
      }
      // the following is for cases where an object in the data has no counterpart in the geometry (for example Hong Kong, for some reason)
      else if(country[0].length == 0){
          return [10000,10000];
      } else {
          return path.centroid(country.datum().geometry);
      }
  },  
  
  
  
  /**
   * Generates a group within the svg element to contain the barchart elements 
   */
  prepareBarChart : function() {
  	var that = this;
    // barchart group
    that.barchart = that.svg
              .append('g')
              .attr('id', 'barchart')
              .attr('transform', 'translate(' + [that.BARCHARTTRANSLATEX, that.BARCHARTTRANSLATEY] + ')');
  },

  /**
   * Prepares the time series group 
   */
  prepareTimeseries : function() {
  	var that = this;
      ts = that.svg
          .append('g')
          .attr('id', 'timeseries')
          .attr('transform', 'translate(' + [that.TSTRANSLATEX, that.TSTRANSLATEY] + ')');
  },

  
  /*
   * INTERACTION FUNCTIONS
   */

  /**
   * visually deselects country and hides the tooltip, switches back to world timeseries
   */
  unselectCountry : function(id, showTooltip){
  	var that = this;
      var country = d3.select('#map').selectAll('.country').filter(function(d){return d.id == id});
      country
          .classed('selected', false);
      // delete tooltip
      if(showTooltip){
          d3.select('#container')
              .selectAll('.tooltip')
              .transition()
              .duration(250)
              .style('opacity', 1e-6)
              .remove();
      }
      that.displayTimeseries(that.dataByCountry['World'], 'World');
  },

  /** 
   * visually selects a country on the map and displays a tooltip, updates the timeseries
   */

  selectCountry : function(id, showTooltip){
  	var that = this;
      var numFormatter = d3.format('.2f');
      // select the country data from the map
      var country = d3.select('#map').selectAll('.country').filter(function(d){return d.id == id});
      country
          .classed('selected', true);
      // get the data from the circles
      var countryInfo = d3.select('#map').selectAll('.consumption').filter(function(d){return d.id == id}).data();
      // if no consumption circle, get production circle instead
      if(countryInfo.length == 0){
          countryInfo = d3.select('#map').selectAll('.production').filter(function(d){return d.id == id}).data();
      }
      // construct data object for which one (!) tooltip will be added
      if(country[0].length > 0){
          var data = {
              'name': country.datum().name,
              'production': (countryInfo[0] !== undefined && !that.isNaN(countryInfo[0].production)) ? numFormatter(countryInfo[0].production / 1000) + ' Bn USD': 'n/a',
              'consumption': (countryInfo[0] !== undefined && !that.isNaN(countryInfo[0].consumption)) ? numFormatter(countryInfo[0].consumption / 1000) + ' Bn USD': 'n/a'
          };
      }
      // show tooltip (only the first time)
      if(showTooltip){
          var tooltip = d3.select('#container').append('div')
              .attr('class', 'tooltip')
              .html('<strong>' + data.name + ', ' + that.year + '</strong><br/>Production: ' + data.production + '<br/>Consumption: ' + data.consumption)
              .attr('style', 'left:'+ (d3.event.pageX - 150) +'px;top:'+ (d3.event.pageY - 75) +'px')
              .transition()
              .duration(250)
              .style('opacity', 1)
          /* ugly hack */    
          /*d3.select('#container')
              .selectAll('.tooltip')
              .attr('style', 'left:'+ (d3.event.pageX - 150) +'px;top:'+ (d3.event.pageY - 75) +'px');*/
      }
      // if country has data, display it in timeseries
      if(id !== 900 && data !== undefined && data.name !== undefined && that.dataByCountry[data.name] !== undefined){
      	that.displayTimeseries(that.dataByCountry[country.datum().name], country.datum().name);
      }
  },

  
  /**
   * Displays the data
   * Calls the function which displays the map, the barchart and the time series
   */
  displayData : function() {
  	if(this.data == null) return;

  	var that = this;
  	
      var currentData = that.data[that.year].clone();
      // strip off world data
      var worlddata = currentData.pop();
      // sort data according to either production or consumption
      currentData.sort(function(a, b){ 
          if (a[that.order] > b[that.order] || (that.isNaN(b[that.order]) && !isNaN(a[that.order]))) {
              return 1;
          } else if (a[that.order] < b[that.order] || (that.isNaN(a[that.order]) && !isNaN(b[that.order]))) {
              return -1;
          // if both are equal (or NaN), sort according to the other attribute
          } else {
              var otherorder = (that.order === 'production') ? 'consumption' : 'production';
              if (a[otherorder] > b[otherorder] || (that.isNaN(b[otherorder]) && !that.isNaN(a[otherorder]))) {
                  return 1;
              } else if (a[otherorder] < b[otherorder] || (that.isNaN(a[otherorder]) && !that.isNaN(b[otherorder]))) {
                  return -1;
              } else {
                  return 0;
              }
          }       
      });
      currentData.reverse();
      // barchart data
      that.displayBarchartData(currentData, worlddata);
      // map data
      that.displayMapData(currentData);
      // ts data (initially showing the world)
      that.displayTimeseries(that.dataByCountry['World'], 'World');
      
  },
  
  
  /**
   * Helper function
   * checks whether a value is NaN 
   */
  isNaN : function(number){
      return (typeOf(number) === 'null') ? true : false;
  },
  
  
  /**
   * Will be called by displayData
   * Shows the bar charts on the left side 
   */
  displayBarchartData : function(data, worlddata) {
  	var that = this;
      var numFormatter = d3.format('.2f');
      data = data.slice(0, that.BARCHARTSHOWONLY);
      // add world data
      data.push(worlddata);
      // dimensions
      var width = 600;
      var groupHeight = 20;
      var textWidth = 120;
      var margin = 0;
      var barWidth = (width / 2) - (textWidth / 2) - margin;
      var barHeight = groupHeight - 1;
      // the highest consumption (USA) is higher than the highest production, thus always use
      // highest consumption as scale reference
      var barchartScale = d3.scale.linear().domain([0,20732]).range([1,that.MAXBARCHARTWIDTH]); 
      
      // DATA JOIN
      var countries = that.barchart.selectAll('.country').data(data, function(d){ return d.id;});
      
      
      // ENTER
      var countriesEnter = countries
          .enter()
          .append('g')
          .attr('class', 'country')
          // position according to index
          .attr('transform', function(d,i) { return 'translate(0,' + (i * groupHeight + margin) +')'})
          .style('fill-opacity', 1e-6)
          // tooltip /mouseover
          .on('mouseover', function(d){
          	that.selectCountry(d.id, false);
              d3.select(this).select('text').classed('selected', true);
          })
          .on('mouseout', function(d){
          		that.unselectCountry(d.id, false);
              d3.select(this).select('text').classed('selected', false);
          });

      // add text 
      countriesEnter
          .append('text')
          .attr('x', barWidth + margin + textWidth / 2)
          .attr('y', barHeight / 2)
          .attr('dy', '0.35em')
          .attr('class', 'label')
          .attr('text-anchor', 'middle')
          .text(function(d){ return d.name;});
      // add rectangle with opacity zero behind text (for easier mouseover)  
      countriesEnter
          .append('rect')
          .attr('class', 'hidden')
          .attr('x', barWidth + margin)
          .attr('y', 0)
          .attr('width', textWidth)
          .attr('height', barHeight);
      // the following only applies to ordinary countries
      var onlycountriesEnter = countriesEnter.filter(function(d){
          return d.id !== 900;
      });
      // add production chart
      onlycountriesEnter
          .append('rect')
          .attr('class', 'production')
          .attr('height', barHeight)
          .attr('width', 0)
          .attr('x', barWidth + margin);
      // add consumption chart
      onlycountriesEnter
          .append('rect')
          .attr('class', 'consumption')
          .attr('height', barHeight)
          .attr('width', 0)
          .attr('x', margin + barWidth + textWidth);
      // add values
      // production
      onlycountriesEnter
          .append('text')
          .attr('x', barWidth + margin - 30)
          .attr('y', barHeight / 2)
          .attr('dy', '0.35em')
          .attr('text-anchor', 'right')
          .attr('class', 'valueProduction')
          .text(function(d){
              if(!that.isNaN(d.production)){
                  return numFormatter(d.production / 1000);
              } else {
                  return 'n/a';
              }
          });
      // consumption
      onlycountriesEnter
          .append('text')
          .attr('x', barWidth + textWidth + margin + 5)
          .attr('y', barHeight / 2)
          .attr('dy', '0.35em')
          .attr('text-anchor', 'left')
          .attr('class', 'valueConsumption')
          .text(function(d){
              if(!that.isNaN(d.consumption)){
                  return numFormatter(d.consumption / 1000);
              } else {
                  return 'n/a';
              }
          });
          
      // the following applies only to world
      var onlyworldEnter = countriesEnter.filter(function(d){
          return d.id === 900;
      });
      // add consumption chart
      onlyworldEnter
          .append('rect')
          .attr('class', 'consumption')
          .attr('height', barHeight / 2)
          .attr('width', 0)
          .attr('x', margin + barWidth + textWidth);
      onlyworldEnter
          .append('rect')
          .attr('class', 'production')
          .attr('height', barHeight / 2)
          .attr('width', 0)
          .attr('x', margin + barWidth + textWidth)
          .attr('y', barHeight / 2)
          //.attr('transform', 'translate(0,'+ barHeight / 2 + ')')
      // add values
      // production
      onlyworldEnter
          .append('text')
          .attr('x', barWidth + textWidth + margin + 5)
          .attr('y', barHeight / 2 + 5)
          .attr('dy', '0.35em')
          .attr('text-anchor', 'right')
          .attr('class', 'valueProduction')
          .text(function(d){
                  return numFormatter(d.production / 1000);
          })
          .classed('world', true);
      // consumption
      onlyworldEnter
          .append('text')
          .attr('x', barWidth + textWidth + margin + 5)
          .attr('y', 4)
          .attr('dy', '0.35em')
          .attr('text-anchor', 'left')
          .attr('class', 'valueConsumption')
          .text(function(d){
              return numFormatter(d.consumption / 1000);
          })
          .classed('world', true);    
      
      // annotation
      that.barchart
          .selectAll('.annotation')
          .data([{a:'* Volume in Billion USD. Only ' + that.BARCHARTSHOWONLY + ' countries are shown'}])
          .enter()
          .append('g')
          .append('text')
          .text(function(d){ return d.a;})
          .attr('class','annotation')
          .attr('y', function(){ return that.data.length * groupHeight + margin + 10;})
          .attr('x', barWidth + textWidth + margin);
      
       
      // UPDATE
      
      // movement of whole group
      var barUpdate = countries
          .transition()
          .duration(that.TRANSDURATION)
          // position according to index
          .attr('transform', function(d,i) { return 'translate(0,' + (i * groupHeight + margin) +')'})
          .style('fill-opacity', 1);
      
      // transition of charts
      // the following only applies to countries
      barUpdate.filter(function(d){
          return d.id !== 900;
      }).select('.production')
          .transition()
          .duration(that.TRANSDURATION)
          .attr('width', function(d){ 
              if(!that.isNaN(d.production)){
                  return barchartScale(d.production);  
              } else {
                  return 0;
              }
          })
          .attr('x', function(d){ 
              if(!that.isNaN(d.production)){
                  return barWidth - barchartScale(d.production) + margin;  
              } else {
                  return 0;
              }
          });
      // the following only applies to the world
      barUpdate.filter(function(d){
          return d.id === 900;
      }).select('.production')
          .transition()
          .duration(that.TRANSDURATION)
          .attr('width', function(d){             
              return barchartScale(d.production);  
          });
          
      barUpdate.select('.consumption')
          .transition()
          .duration(that.TRANSDURATION)
          .attr('width', function(d){ 
              if(!that.isNaN(d.consumption)){
                  return barchartScale(d.consumption);  
              } else {
                  return 0;
              }
          }); 
      // movement of values
      // the following only applies to countries
      barUpdate.filter(function(d){
          return d.id !== 900;
      }).select('.valueProduction')    
          .transition()
          .duration(that.TRANSDURATION)
          .attr('x', function(d){ 
              if(!that.isNaN(d.production)){
                  return barWidth - barchartScale(d.production) + margin - 30;  
              } else {
                  return barWidth - 20;
              }
          })
          .text(function(d){
              if(!that.isNaN(d.production)){
                  return numFormatter(d.production / 1000);
              } else {
                  return 'n/a';
              }
          });
      // the following only applies to the world
      barUpdate.filter(function(d){
          return d.id === 900;
      }).select('.valueProduction')    
          .transition()
          .duration(that.TRANSDURATION)
          .attr('x', function(d){      
              return barWidth + textWidth + barchartScale(d.production) + margin + 5;  
          })
          .text(function(d){
              return numFormatter(d.production / 1000);
          });
      barUpdate.select('.valueConsumption')    
          .transition()
          .duration(that.TRANSDURATION)
          .attr('x', function(d){ 
              if(!that.isNaN(d.consumption)){
                  return barWidth + textWidth + barchartScale(d.consumption) + margin + 5;  
              } else {
                  return barWidth + textWidth + margin + 5;
              }
          })
          .text(function(d){
              if(!that.isNaN(d.consumption)){
                  return numFormatter(d.consumption / 1000);
              } else {
                  return 'n/a';
              }
          });
      // movement of annotation
      that.barchart
          .selectAll('.annotation')
          .transition()
          .duration(that.TRANSDURATION)
          .attr('y', function(){ return data.length * groupHeight + margin + 10;});

      // EXIT
      countries
          .exit()
          .transition()
          .duration(that.TRANSDURATION)
          .style('fill-opacity', 1e-6)
          .remove();
          
  },
  
  /**
   * Will be called by displayData
   * Fills the map with thematic data
   */
  displayMapData : function(data) {
  	var that = this;
      // the highest consumption (USA) is higher than the highest production, thus always use
      // highest consumption as scale reference
      var consumptionMapScale = d3.scale.sqrt().domain([0,20732]).range([1,that.MAXCIRCLERADIUS]);
      
      // DATA JOIN
      var consumption = that.map.select('#consumptionGroup').selectAll('.consumption').data(function(d){
          return data.filter(function(d){ return !that.isNaN(d.consumption)});
      }, function(d){ 
          return d.id;
      });

      var production = that.map.select('#productionGroup').selectAll('.production').data(function(d){
          return data.filter(function(d){ return !that.isNaN(d.production)});
      }, function(d){ 
          return d.id;
      });
      
      // UPDATE
      consumption
          .transition()
          .duration(that.TRANSDURATION)
          // includes a short delay because barchart ordering is done first
          .delay(that.TRANSDURATION)
          .attr('r', function(d){ return consumptionMapScale(d.consumption);});
      production
          .transition()
          .duration(that.TRANSDURATION)
          // includes a short delay because barchart ordering is done first
          .delay(that.TRANSDURATION)
          //.delay((!production.exit().empty() + !production.enter().empty()) * TRANSDURATION)
          .attr('r', function(d){ return consumptionMapScale(d.production);});
      
      // ENTER
      // consumption half circles
      consumption
          .enter()
          .append('circle')
          .attr('class', 'consumption')
          .attr('cx', function(d){ return that.getCentroid(d.id)[0];})
          .attr('cy', function(d){ return that.getCentroid(d.id)[1];})
          // interaction
          .on('mouseover', function(d){
          	that.selectCountry(d.id, true);
          })
          .on('mouseout', function(d){
          	that.unselectCountry(d.id, true);
          })
          // clip path so only half circle is shown
          .attr('clip-path', function(d){ return 'url(#cut-off-consumption-' + d.id + ')'})
          .attr('r', 0)
          .transition()
          .duration(that.TRANSDURATION)
          .delay((!consumption.exit().empty() + !consumption.enter().empty()) * that.TRANSDURATION)
          .attr('r', function(d){ return consumptionMapScale(d.consumption);})
          
      // production half circles
      production
          .enter()
          .append('circle')
          .attr('class', 'production')
          .attr('cx', function(d){ return that.getCentroid(d.id)[0];})
          .attr('cy', function(d){ return that.getCentroid(d.id)[1];})
          // interaction
          .on('mouseover', function(d){
          	that.selectCountry(d.id, true);
          })
          .on('mouseout', function(d){
          	that.unselectCountry(d.id, true);
          })
          // clip path so only half circle is shown
          .attr('clip-path', function(d){ return 'url(#cut-off-production-' + d.id + ')'})
          .attr('r', 0)
          .transition()
          .duration(that.TRANSDURATION)
          .delay((!production.exit().empty() + !production.enter().empty()) * that.TRANSDURATION)
          .attr('r', function(d){ return consumptionMapScale(d.production);});
      
      // EXIT  
      consumption
          .exit()
          .transition()
          .duration(that.TRANSDURATION)
          .attr('r', 0)
          .remove()
      production
          .exit()
          .transition()
          .duration(that.TRANSDURATION)
          .attr('r', 0)
          .remove()    
  },

  /**
   * displays the timeseries data for a particular country/ the world 
   */
  displayTimeseries : function(data, country){
  	var that = this;
      // pass by value instead of by reference
  	  if(data == null) return;
      var dataToDisplay = data.clone();
      // num formatter
      var numFormatter = d3.format('.2f');
      // dimensions
      var margin = {top: 0, right: 20, bottom: 20, left: 60},
      width  = that.tswidth  - margin.left - margin.right,
      height = that.tsheight - margin.top - margin.bottom;
      
      // date parser
      var parseDate = d3.time.format('%Y').parse;
      // scales
      var y = d3.scale.linear()
          .range([height, 0]);
      var x = d3.time.scale()
          .range([0, width]);
      // color scale maps production and consumption to their respective colors
      var color = d3.scale.ordinal().range(['rgb(255, 146, 6)', 'rgb(85, 85, 85)']);
      // set domain (using the keys)
      color.domain(d3.keys(dataToDisplay[0]).filter(function(key) { return key !== "year"; }));

      var xAxis = d3.svg.axis()
          .scale(x)
          .orient("top");

      var yAxis = d3.svg.axis()
          .scale(y)
          .orient("right");
          
      var line = d3.svg.line()
          .interpolate("linear")
          .x(function(d) { return x(d.date); })
          .y(function(d) { return y(d.value); });
      
      // line is not defined everywhere
      line.defined(function(d){
          return !isNaN(d.value);
      });
      // preprocess date
      dataToDisplay.forEach(function(d){
              d.date = parseDate(d.year);
      })
      // convert data array to date-amount pairs
      var yearlyAmounts = color.domain().map(function(type){
         return {
             'type' : type.charAt(0).toUpperCase() + type.slice(1),
             'values' : dataToDisplay.map(function(d){
                 return { 
                     'date': d.date, 
                     'value': +numFormatter(+d[type] / 1000)};
             })
         }
      });
      // specify axis domains
      x.domain(d3.extent(dataToDisplay, function(d) { return d.date; }));
      y.domain([
          0,//d3.min(yearlyAmounts, function(c) { return d3.min(c.values, function(v) { return v.value; }); }),
          d3.max(yearlyAmounts, function(c) { return d3.max(c.values, function(v) { return v.value; }); })
      ]);
      // begin construction of panel
      // delete prior axis
      ts.selectAll('.axis').remove();
      ts.selectAll('.type').remove();
      ts
          .attr('width', width + margin.left + margin.right)
          .attr('height', height + margin.top + margin.bottom)
          
      ts.append('g')
        .attr('class', 'x axis')
        /*.attr('transform', 'translate(0,' + height + ')')*/
        .call(xAxis)
        .append('text')
        .attr('class', 'axisText')
        .attr('x', margin.right)
        .attr('y', 10)
        .attr('dy', '.71em')
        .style('text-anchor', 'start')
        .text(function(){
            return country;
        });

      ts.append('g')
        .attr('class', 'y axis').attr("transform", "translate(" + width + " ,0)")
        .call(yAxis)
        .append('text')
        .attr('transform', 'rotate(-90)')
        .attr('y', 40)
        /*.attr('dy', '.71em')*/
        .style('text-anchor', 'end')
        .text('Volume in Billion USD');
      
      // data join
      var type = ts.selectAll('.type')
          .data(yearlyAmounts)
          .enter()
          .append('g')
          .attr('class', 'type');
      // append curves
      type.append('path')
        .attr('class', 'line')
        .attr('d', function(d) { return line(d.values); })
        .style('stroke', function(d) { return color(d.type); });

      /*city.append('text')
        .datum(function(d) { return {name: d.name, value: d.values[d.values.length - 1]}; })
        .attr('transform', function(d) { return 'translate(' + x(d.value.date) + ',' + y(d.value.temperature) + ')'; })
        .attr('x', 3)
        .attr('dy', '.35em')
        .text(function(d) { return d.name; });*/

      
     
  }
  
  
  
  
  
	
});
