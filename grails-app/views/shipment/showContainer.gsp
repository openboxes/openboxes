
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>        
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle">Shipment Details</content>
</head>
	
<body>
    
	<div class="body">
	     	
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>


			
	</div>

</body>
</html>
