<style>
	.selected { background-color: #f7f7f7; } 
</style>

<div class="wizard-steps">
	<div class="${state.equals('pickRequestItems')?'active-step':''}">
		<g:link action="fulfillRequest" event="pickRequestItems">
			<span>1</span> Pick request items
		</g:link>
	</div>
	<div class="${state.equals('packRequestItems')?'active-step':''}">
		<g:link action="fulfillRequest" event="packRequestItems">
			<span>2</span> Ship request items
		</g:link>
	</div>
	<div class="${state.equals('confirmFulfillment')?'active-step':''}">
		<g:link action="fulfillRequest" event="confirmFulfillment">
			<span>3</span> Mark request as fulfilled
		</g:link>
	</div>
</div>

