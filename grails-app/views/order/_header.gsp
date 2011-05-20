<div style="padding: 10px;">
	<g:if test="${orderInstance?.id}">
		<table>
			<tbody>			
				<tr>
					<td>
						<div>
							<span style="font-size: 1.5em; font-weight: bold; line-height: 1em;">${orderInstance?.description}</span>							
						</div> 
						<div class="fade" style="font-size: 0.9em; line-height: 20px;">
							<!-- Hide action menu menu if the user is in the shipment workflow -->						
							<g:if test="${!params.execution }">
								<g:render template="/order/actions" /> &nbsp;|&nbsp;
							</g:if>
							<b>Order #:</b> ${orderInstance?.orderNumber}  
							&nbsp;|&nbsp; 
							<b>Date ordered:</b> ${orderInstance?.dateOrdered}							
						</div>
					</td>										
					<td style="text-align: right;">
						<div class="fade" style="font-weight: bold; font-size:1.5em;">
						</div>
						
						
					</td>
				</tr>
			</tbody>
		</table>			
	</g:if>
</div>