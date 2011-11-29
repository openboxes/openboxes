<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'transaction.label')}" />
        <g:set var="transactionType" value="${format.metadata(obj: command?.transactionInstance?.transactionType)}"/>
        <title>${transactionType }</title>
    </head>    
    <body>
        <div class="body">
            <g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
            </g:if>						
            <g:hasErrors bean="${command}">
	            <div class="errors">
	                <g:renderErrors bean="${command}" as="list" />
	            </div>
            </g:hasErrors>    
            <g:hasErrors bean="${command?.transactionInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${command?.transactionInstance}" as="list" />
	            </div>
            </g:hasErrors>    

			<div class="dialog" >
				<div>
					<g:if test="${command?.transactionInstance?.transactionType?.id == 9 }">
						<g:render template="outgoingTransfer"></g:render>
					</g:if>
					<g:elseif test="${command?.transactionInstance?.transactionType?.id == 8}">
						<g:render template="incomingTransfer"></g:render>
					</g:elseif>
					<g:elseif test="${command?.transactionInstance?.transactionType?.id == 7}">
						<g:render template="inventoryAdjustment"></g:render>
					</g:elseif>
					<g:elseif test="${command?.transactionInstance?.transactionType?.id == 4}">
						<g:render template="inventoryExpired"></g:render>
					</g:elseif>
					<g:elseif test="${command?.transactionInstance?.transactionType?.id == 5}">
						<g:render template="inventoryDamaged"></g:render>
					</g:elseif>
					<g:elseif test="${command?.transactionInstance?.transactionType?.id == 2}">
						<g:render template="inventoryConsumed"></g:render>
					</g:elseif>
					<g:else>
						Unknown transaction type
					</g:else> 
					
				</div>
			</div>
		</div>
	</body>
</html>