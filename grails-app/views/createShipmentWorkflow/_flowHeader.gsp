<div class="wizard-box">
	<div class="wizard-steps "> 
		<div class="${currentState.equals('Details')?'active-step':''}">
			<g:link action="createShipment" event="enterShipmentDetails">
				<img src="${createLinkTo(dir: 'images/icons/silk', file: 'lorry.png' )}" class="middle"/>&nbsp;
				<warehouse:message code="shipping.enterShipmentDetails.label"/>
			</g:link>
		</div>
		<div class="${currentState.equals('Tracking')?'active-step':''}">
			<g:link action="createShipment" event="enterTrackingDetails">
				<img src="${createLinkTo(dir: 'images/icons/silk', file: 'map.png' )}" class="middle"/>&nbsp;
				<warehouse:message code="shipping.enterTrackingDetails.label"/>
			</g:link>
		</div>
		<div class="${currentState.equals('Pack')?'active-step':''}">
			<g:link action="createShipment" event="enterContainerDetails">
				<img src="${createLinkTo(dir: 'images/icons/silk', file: 'package.png' )}" class="middle"/>&nbsp;
				<warehouse:message code="shipping.enterContainerDetails.label"/>
			</g:link>
		</div>
		<div class="${currentState.equals('Send')?'active-step':''}">
			<g:link action="createShipment" event="sendShipment">
				<img src="${createLinkTo(dir: 'images/icons/silk', file: 'lorry_go.png' )}" class="middle"/>&nbsp;
				<warehouse:message code="shipping.sendShipment.label"/>
			</g:link>
		</div>
	</div>
</div>	