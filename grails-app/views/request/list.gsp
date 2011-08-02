<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'request.label', default: 'Request')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">


				<div id="mymenu">
					<g:include controller="request" action="menu"></g:include>
				</div>
				
				<div id="outgoingRequests" class="list">            
	           		<h3>Requests placed <b>with</b> you (${session.warehouse?.name })</h3>
					<g:render template="list" model="[requestInstanceList:outgoingRequests,requestType:'outgoing']"/>
				</div>
				
				<br/>
				
				
				<div id="incomingRequests" class="list">            
	            	<h3>Requests placed <b>by</b> you (${session.warehouse?.name })</h3>
					<g:render template="list" model="[requestInstanceList:incomingRequests,requestType:'incoming']"/>
				</div>
				
			</div>
        </div>
    </body>
</html>
