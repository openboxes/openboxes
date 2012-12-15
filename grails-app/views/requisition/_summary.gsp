<div>
	<g:if test="${requisition?.id}">
	
		<table>
			<tbody>			
				<tr>
					<td>
						<g:render template="/requisition/actions" model="[requisition:requisition]"/>
					</td>				
					<td>
						<div>
							<span class="title">${requisition?.name}</span>
						</div> 
						<div class="fade" style="font-size: 0.9em; line-height: 20px;">
						    
							<span class="status">
								${warehouse.message(code: 'default.status.label') }:
								<b><format:metadata obj="${requisition?.status}"/></b>
							</span>
							<span class="fade">&nbsp;|&nbsp;</span>
							<span class="requested-date">
								<warehouse:message code="requisition.date.label"/>: <b><format:date obj="${requisition?.dateRequested}"/></b>
							</span>
							<span class="fade">&nbsp;|&nbsp;</span>
							<span class="request-items">
								<warehouse:message code="requisition.requisitionItem.label"/>:
								<b>${requisition?.requisitionItems?.size()}</b>
							</span>
							<span class="fade">&nbsp;|&nbsp;</span>
							<span class="requested-by">
								<warehouse:message code="requisition.requestedBy.label"/>:
								<g:if test="${requisition?.requestedBy }"><b>${requisition?.requestedBy?.name }</b></g:if>
							</span>
						</div>
					</td>										

				</tr>
			</tbody>
		</table>			
	</g:if>
	<g:else>
		<table>
			<tbody>			
				<tr>
					<td>
						<div>
							<span class="title">
								<g:if test="${requisition?.name}">
									${requisition?.name }
								</g:if>
								<g:else>
									<warehouse:message code="requisition.new.label"/>
								</g:else>
							</span>							
						</div> 
					</td>										
					<td style="text-align: right;">
						
					</td>
				</tr>
			</tbody>
		</table>			
	
	</g:else>
</div>