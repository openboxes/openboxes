<div class="wizard-steps">
	<div class="center ${state.equals('enterShipmentDetails')?'active-step':''}" >
		<g:link action="receiveOrder" event="enterShipmentDetails">1. <warehouse:message code="order.enterShipmentDetails.label"/></g:link>
	</div>
	<div class="center ${state.equals('processOrderItems')?'active-step':''}">
		<g:link action="receiveOrder" event="processOrderItems">2. <warehouse:message code="order.selectItemsToReceive.label"/></g:link>
	</div>
	<div class="center ${state.equals('confirmOrderReceipt')?'active-step':''}">
		<g:link action="receiveOrder" event="confirmOrderReceipt">3. <warehouse:message code="order.markOrderAsReceived.label"/></g:link>				
	</div>
</div>			
