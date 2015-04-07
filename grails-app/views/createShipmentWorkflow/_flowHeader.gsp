<div class="wizard-box">
	<div class="wizard-steps ">

        <g:if test="${shipmentInstance?.id}">
            <div class="${currentState.equals('Details')?'active-step':''}">
                <g:link action="createShipment" event="enterShipmentDetails">
                    <span class="badge">1</span>
                    <warehouse:message code="shipping.enterShipmentDetails.label"/>
                </g:link>
            </div>
            <div class="${currentState.equals('Tracking')?'active-step':''}">
                <g:link action="createShipment" event="enterTrackingDetails">
                    <span class="badge">2</span>
                    <warehouse:message code="shipping.enterTrackingDetails.label"/>
                </g:link>
            </div>
            <div class="${currentState.equals('Pack')?'active-step':''}">
                <g:link action="createShipment" event="enterContainerDetails">
                    <span class="badge">3</span>
                    <warehouse:message code="shipping.enterContainerDetails.label"/>
                </g:link>
            </div>
            <div class="${currentState.equals('Send')?'active-step':''}">
                <g:link action="createShipment" event="sendShipment">
                    <span class="badge">4</span>
                    <warehouse:message code="shipping.sendShipment.label"/>
                </g:link>
            </div>
        </g:if>
        <g:else>
            <div class="${currentState.equals('Details')?'active-step':''}">
                <g:link action="createShipment" event="enterShipmentDetails">
                    <span class="badge">1</span>
                    <warehouse:message code="shipping.enterShipmentDetails.label"/>
                </g:link>
            </div>
            <div class="${currentState.equals('Tracking')?'active-step':''}">
                <a href="#">
                    <span class="badge">2</span>
                    <warehouse:message code="shipping.enterTrackingDetails.label"/>
                </a>
            </div>
            <div class="${currentState.equals('Pack')?'active-step':''}">
                <a href="#">
                    <span class="badge">3</span>
                    <warehouse:message code="shipping.enterContainerDetails.label"/>
                </a>
            </div>
            <div class="${currentState.equals('Send')?'active-step':''}">
                <a href="#">
                    <span class="badge">4</span>
                    <warehouse:message code="shipping.sendShipment.label"/>
                </a>
            </div>

        </g:else>
	</div>
</div>	