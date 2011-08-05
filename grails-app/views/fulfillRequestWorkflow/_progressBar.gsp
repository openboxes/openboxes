<style>
	.selected { background-color: #f7f7f7; } 
</style>


<div style="">
	<table border="0" style="border-bottom: 1px solid lightgrey">
		<tr>				
			<td class="center ${state.equals('pickRequestItems')?'selected':''}">
				<g:link action="fulfillRequest" event="pickRequestItems">1. Pick request items</g:link>
			</td>
			<td class="center ${state.equals('packRequestItems')?'selected':''}">
				<g:link action="fulfillRequest" event="packRequestItems">2. Pack request items</g:link>
			</td>
			<td class="center ${state.equals('confirmFulfillment')?'selected':''}">
				<g:link action="fulfillRequest" event="confirmFulfillment">3. Mark request as fulfilled</g:link>				
			</td>
		</tr>
	</table>			
</div>			
