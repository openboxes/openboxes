
<div class="wizard-steps">
	<div class="${state.equals('previewRequest')?'active-step':''}">
		<g:link action="fulfillRequest" event="previewRequest">
			<span>0</span>  <warehouse:message code="fulfillRequestWorkflow.previewRequest.label" default="Preview request"/>
		</g:link>
	</div>
	<div class="${state.equals('pickRequestItems')?'active-step':''}">
		<g:link action="fulfillRequest" event="pickRequestItems">
			<span>1</span> <warehouse:message code="fulfillRequestWorkflow.pickItems.label" default="Pick items"/>
		</g:link>
	</div>
	<div class="${state.equals('packRequestItems')?'active-step':''}">
		<g:link action="fulfillRequest" event="packRequestItems">
			<span>2</span> <warehouse:message code="fulfillRequestWorkflow.packItems.label" default="Pack items"/>
		</g:link>
	</div>
	<div class="${state.equals('confirmFulfillment')?'active-step':''}">
		<g:link action="fulfillRequest" event="confirmFulfillment">
			<span>3</span>  <warehouse:message code="fulfillRequestWorkflow.markAsFulfilled.label" default="Mark request as fulfilled"/>
		</g:link>
	</div>
</div>


