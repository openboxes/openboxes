<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'requisitions.label', default: 'Stock Requisitions').toLowerCase()}" />
        <title>
	        <warehouse:message code="requisition.label"/>
		</title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            
            <div class="yui-ga">
				<div class="yui-u first">
					<g:set var="requisitions" value="${requisitions?.sort { it.status }}"/>
					<g:set var="requisitionMap" value="${requisitions?.groupBy { it.status }}"/>
					
					<g:render template="list" model="[requisitions:requisitions]"/>
								
				</div>
			</div>		
        </div>
    </body>
</html>
