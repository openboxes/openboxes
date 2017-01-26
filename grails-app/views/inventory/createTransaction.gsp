<%@ page import="org.pih.warehouse.inventory.TransactionType" %>
<%@ page import="org.pih.warehouse.core.Constants" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'transaction.label')}" />
        <g:set var="transactionType" value="${format.metadata(obj: command?.transactionType)}"/>
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
			<g:hasErrors bean="${command.transaction}">
				<div class="errors">
					<g:renderErrors bean="${command.transaction}" as="list" />
				</div>
			</g:hasErrors>

			<div class="dialog">
				<g:if test="${command?.transactionType?.id == Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID}">
					<g:render template="outgoingTransfer"></g:render>
				</g:if>
				<g:elseif test="${command?.transactionType?.id == Constants.TRANSFER_IN_TRANSACTION_TYPE_ID}">
					<g:render template="incomingTransfer"></g:render>
				</g:elseif>
				<g:elseif test="${command?.transactionType?.id == Constants.INVENTORY_TRANSACTION_TYPE_ID}">
					<g:render template="inventoryAdjustment"></g:render>
				</g:elseif>
				<g:elseif test="${command?.transactionType?.id == Constants.EXPIRATION_TRANSACTION_TYPE_ID}">
					<g:render template="inventoryExpired"></g:render>
				</g:elseif>
				<g:elseif test="${command?.transactionType?.id == Constants.DAMAGE_TRANSACTION_TYPE_ID}">
					<g:render template="inventoryDamaged"></g:render>
				</g:elseif>
				<g:elseif test="${command?.transactionType?.id == Constants.CONSUMPTION_TRANSACTION_TYPE_ID}">
					<g:render template="inventoryConsumed"></g:render>
				</g:elseif>
				<g:else>
					Unknown transaction type
				</g:else>
			</div>
			<div class="clear"></div>
		</div>
	</body>
</html>