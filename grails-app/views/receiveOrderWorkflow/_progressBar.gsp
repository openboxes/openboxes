<style>
	.selected { background-color: lightgrey; } 
</style>


<div style="">
	<table border="0" width="50%" style="border: 3px solid lightgrey">
		<tr>
			<td class="center ${state.equals('enterShipmentDetails')?'selected':''}" >
				<g:link action="receiveOrder" event="enterShipmentDetails">1. Enter shipment details</g:link>
			</td>
			<td class="center ${state.equals('processOrderItems')?'selected':''}">
				<g:link action="receiveOrder" event="processOrderItems">2. Select items to receive </g:link>
			</td>
			<td class="center ${state.equals('confirmOrderReceipt')?'selected':''}">
				<g:link action="receiveOrder" event="confirmOrderReceipt">3. Mark order as received</g:link>				
			</td>
		</tr>
		<%-- Debugging information  
		<tr>
			<td colspan="3">
				orderItems = ${orderItems }
			</td>
		</tr>		
		<tr>
			<td colspan="3">
				orderCommand.orderItems = ${orderCommand?.orderItems }
			</td>
		</tr>
		<tr>
			<td colspan="3">
				flow.orderCommand = ${flow?.orderCommand }
			</td>
		</tr>
		<tr>
			<td colspan="3">
				orderCommand = ${orderCommand }
			</td>
		</tr>
		--%>
	</table>			
</div>			
