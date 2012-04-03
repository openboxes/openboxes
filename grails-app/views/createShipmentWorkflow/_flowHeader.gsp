
<div class="wizard-steps"> 
	<div class="${currentState.equals('Details')?'active-step':''}">
		<g:link action="createShipment" event="enterShipmentDetails"><warehouse:message code="shipping.enterShipmentDetails.label"/></g:link>
	</div>
	<div class="${currentState.equals('Tracking')?'active-step':''}">
		<g:link action="createShipment" event="enterTrackingDetails"><warehouse:message code="shipping.enterTrackingDetails.label"/></g:link>
	</div>
	<div class="${currentState.equals('Pack')?'active-step':''}">
		<g:link action="createShipment" event="enterContainerDetails"><warehouse:message code="shipping.enterContainerDetails.label"/></g:link>
	</div>
	<div class="${currentState.equals('Send')?'active-step':''}">
		<g:link action="createShipment" event="sendShipment"><warehouse:message code="shipping.sendShipment.label"/></g:link>
	</div>
</div>

<br clear="all"/>
<br/>