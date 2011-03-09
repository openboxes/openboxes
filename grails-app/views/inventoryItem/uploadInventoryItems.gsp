
<%@ page import="org.pih.warehouse.product.Product"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="custom" />
		<g:set var="entityName"
			value="${message(code: 'inventoryItem.label', default: 'Inventory item')}" />
		<title><g:message code="default.import.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="body">
	
			
			<g:if test="${flash.message}">
				<div class="message">
					${flash.message}
				</div>
			</g:if> 
			<g:hasErrors bean="${commandInstance}">
				<div class="errors"><g:renderErrors bean="${commandInstance}" as="list" /></div>
			</g:hasErrors>


			<div clss="dialog">
				<g:render template="uploadFileForm"/>
			</div>

	
	
		</div>
	</body>
</html>
