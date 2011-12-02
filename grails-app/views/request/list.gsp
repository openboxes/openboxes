<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'requests.label', default: 'Requests').toLowerCase()}" />
        <title>
			<g:if test="${params.requestType == 'INCOMING'}">            
	        	<warehouse:message code="request.requestsPlacedWithYou.message"/>
			</g:if>
			<g:if test="${params.requestType == 'OUTGOING'}">            
				<warehouse:message code="request.requestsPlacedByYou.message"/>
			</g:if>
		</title>
		<content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>
        
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">		
				<g:if test="${params.requestType == 'OUTGOING'}">            
					<g:render template="list" model="[requestInstanceList:outgoingRequests,requestType:'OUTGOING']"/>
				</g:if>
				<g:if test="${params.requestType == 'INCOMING'}">            
	            	<g:render template="list" model="[requestInstanceList:incomingRequests,requestType:'INCOMING']"/>
				</g:if>
			</div>
        </div>
    </body>
</html>
