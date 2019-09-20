<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <g:set var="entityName" value="${warehouse.message(code: 'country.label', default: 'Country')}" />
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>        
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle">${entityName}</content>
		<content tag="menuTitle">${entityName}</content>		
		<content tag="globalLinksMode">append</content>
		<content tag="localLinksMode">override</content>
		<content tag="globalLinks"><g:render template="global" model="[entityName:entityName]"/></content>
		<content tag="localLinks"><g:render template="local" model="[entityName:entityName]"/></content>
				
		<!-- Combo-handled YUI CSS files: -->
		<link rel="stylesheet" type="text/css" href="//yui.yahooapis.com/combo?2.8.0r4/build/datatable/assets/skins/sam/datatable.css">
		<!-- Combo-handled YUI JS files: -->
		<script type="text/javascript" src="//yui.yahooapis.com/combo?2.8.0r4/build/yahoo-dom-event/yahoo-dom-event.js&2.8.0r4/build/connection/connection-min.js&2.8.0r4/build/element/element-min.js&2.8.0r4/build/datasource/datasource-min.js&2.8.0r4/build/datatable/datatable-min.js&2.8.0r4/build/json/json-min.js"></script>
		<script type="text/javascript">
		  YAHOO.util.Event.addListener(window, "load", function() {
		      YAHOO.example.Basic = function() {
			  this.formatUrl = function(elCell, oRecord, oColumn, sData) {
			      elCell.innerHTML = "<a href='" + oRecord.getData("id") + "' target='_blank'>" + sData + "</a>";
			  };
			  var myColumnDefs = [
			      {key:"id", sortable:true, resizeable:true, formatter:this.formatUrl},
			      {key:"country", sortable:true, resizeable:true},
			      {key:"date", formatter:YAHOO.widget.DataTable.formatDate, sortable:true, sortOptions:{defaultDir:YAHOO.widget.DataTable.CLASS_DESC},resizeable:true},
			      {key:"population", formatter:YAHOO.widget.DataTable.formatNumber, sortable:true, resizeable:true},
			      {key:"gdp", formatter:YAHOO.widget.DataTable.formatCurrency, sortable:true, resizeable:true}
			  ];
			  var myDataSource = new YAHOO.util.DataSource("${createLink(controller: 'country', action: 'listData')}?");
			  myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
			  myDataSource.connXhrMode = "queueRequests";
			  myDataSource.responseSchema = {
			      resultsList: "results",
			      fields: ["class","id","gdp","date","population","country"]
			  };
			  var myDataTable = new YAHOO.widget.DataTable("basic",
				  myColumnDefs, myDataSource, {caption:"DataTable Caption"});
			  return {
			      oDS: myDataSource,
			      oDT: myDataTable
			  };
		      }();
		  });
		</script>
    </head>
    <body>
	<div class="body">
            <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
            </g:if>

	    <div class="list">
		<h6>An example using YUI Data Source and Data Table</h6>
		<div id="basic"></div>

	    </div>
	</div>
    </body>
    
  

</html>
