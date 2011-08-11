<style>
	.selected { background-color: #f7f7f7; } 
</style>

<div class="wizard-steps">
	<div class="${state.equals('previewRequest')?'active-step':''}">
		<g:link action="fulfillRequest" event="previewRequest">
			<span>0</span> Preview request 
		</g:link>
	</div>
	<div class="${state.equals('pickRequestItems')?'active-step':''}">
		<g:link action="fulfillRequest" event="pickRequestItems">
			<span>1</span> Pick items
		</g:link>
	</div>
	<div class="${state.equals('packRequestItems')?'active-step':''}">
		<g:link action="fulfillRequest" event="packRequestItems">
			<span>2</span> Pack items
		</g:link>
	</div>
	<div class="${state.equals('confirmFulfillment')?'active-step':''}">
		<g:link action="fulfillRequest" event="confirmFulfillment">
			<span>3</span> Mark request as fulfilled
		</g:link>
	</div>
</div>


