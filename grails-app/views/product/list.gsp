<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
   <head>        
        <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">        
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
		<!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><g:message code="default.list.label" args="[entityName]" /></content>		
		<script src="${createLinkTo(dir:'js/slick/', file:'jquery-1.4.3.min.js')}" type="text/javascript" ></script>
		<script src="${createLinkTo(dir:'js/jquery/', file:'jquery.event.drag.min.js')}" type="text/javascript" ></script>
		<script src="${createLinkTo(dir:'js/slick/', file:'slick.core.js')}" type="text/javascript" ></script>
		<script src="${createLinkTo(dir:'js/slick/', file:'slick.grid.js')}" type="text/javascript" ></script>		
		<link rel="stylesheet" href="${createLinkTo(dir:'js/slick/', file:'slick.grid.css')}" type="text/css" media="screen" charset="utf-8" />
		<link rel="stylesheet" href="${createLinkTo(dir:'js/slick/', file:'jquery-ui-1.8.5.custom.css')}" type="text/css" media="screen" charset="utf-8" />
		<link rel="stylesheet" href="${createLinkTo(dir:'js/slick/', file:'examples.css')}" type="text/css" media="screen" charset="utf-8" />		
    </head>    
    <body>
		<table width="100%">
		<tr>
			<td valign="top" width="50%">
				<div id="myGrid" style="width:600px;height:500px;display:none;"></div>
			</td>
			<td valign="top">
				<h2>Demonstrates:</h2>

				<ul>
					<li>basic grid with minimal configuration</li>
				</ul>
			</td>
		</tr>
		</table>
		<script>

		var grid;

		var columns = [
			{id:"title", name:"Title", field:"title"},
			{id:"duration", name:"Duration", field:"duration"},
			{id:"%", name:"% Complete", field:"percentComplete"},
			{id:"start", name:"Start", field:"start"},
			{id:"finish", name:"Finish", field:"finish"},
			{id:"effort-driven", name:"Effort Driven", field:"effortDriven"}
		];

		var options = {
			enableCellNavigation: true,
            enableColumnReorder: false
		};

		$(function() {
            var data = [];
			for (var i = 0; i < 500; i++) {
				data[i] = {
                    title: "Task " + i,
                    duration: "5 days",
                    percentComplete: Math.round(Math.random() * 100),
                    start: "01/01/2009",
                    finish: "01/05/2009",
                    effortDriven: (i % 5 == 0)
                };
			}
			grid = new Slick.Grid("#myGrid", data, columns, options);
            $("#myGrid").show();
		})

		</script>
    </body>
</html>
