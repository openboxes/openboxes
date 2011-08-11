<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'request.label', default: 'Request')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
		<content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>
        
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">				
				<div id="outgoingRequests" class="list">            
	           		<h3><warehouse:message code="request.requestsPlacedWithYou.message"/> (${session.warehouse?.name })</h3>
					<g:render template="list" model="[requestInstanceList:outgoingRequests,requestType:'outgoing']"/>
				</div>				
				<br/>
				<div id="incomingRequests" class="list">            
	            	<h3><warehouse:message code="request.requestsPlacedByYou.message"/> (${session.warehouse?.name })</h3>
					<g:render template="list" model="[requestInstanceList:incomingRequests,requestType:'incoming']"/>
				</div>
				
			</div>
        </div>
    </body>
</html>
