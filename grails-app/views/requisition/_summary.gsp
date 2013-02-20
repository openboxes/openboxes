<div id="requisition-header">

			<%-- 	
			<div id="requisition-header">
				<div class="title" id="description"
					data-bind="html: requisition.name"></div>
				<div class="time-stamp fade"
					data-bind="text: requisition.lastUpdated"></div>
				<div class="status fade">
					<span data-bind="text: requisition.status"></span>
				</div>
			</div>
			--%>


	<g:if test="${requisition}">
		<table>
			<tbody>			
				<tr>
					<td>
						<div class="title" id="description">
							${requisition?.name }
							<b>${requisition?.requestNumber }</b>
						</div> 
						<div class="status">	
							<span class="title">${requisition?.status }</span>
							<br/>
							<span class="fade right">
								<g:formatDate date="${requisition?.lastUpdated }" format="MMM dd, yyyy"/><br/>
								<g:formatDate date="${requisition?.lastUpdated }" format="hh:mm a"/>
							</span>
						</div>
						<div class="clear"></div>
						<div class="fade">
							<span id="origin">
								<warehouse:message code="requisition.origin.label"/>:
								<b>${requisition?.origin?.name?.encodeAsHTML()}</b>
							</span>							
							<span class="fade">&nbsp;|&nbsp;</span>
							<span id="destination">
								<warehouse:message code="requisition.destination.label"/>:
	                            <b>${requisition?.destination?.name?.encodeAsHTML()}</b>
							</span>							
							<span class="fade">&nbsp;|&nbsp;</span>
							<span class="requested-date">
								<warehouse:message code="requisition.date.label"/>: 
								<b><format:date obj="${requisition?.dateRequested}"/></b>
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
							<span class="fade">&nbsp;|&nbsp;</span>
							<span id="recipientProgram">
								<warehouse:message code="requisition.recipientProgram.label"/>:
	                            ${requisition?.recipientProgram?:"N/A" }
							</span>							
							<span class="fade">&nbsp;|&nbsp;</span>
							<span id="recipient">
								<warehouse:message code="requisition.recipient.label"/>:
	                            ${requisition?.recipient?.name?:"N/A"}
							</span>							
											
						</div>
					</td>
				</tr>
			</tbody>
			<tfoot>
				<tr>
					<td class="top">
						<%-- 
						<g:render template="actionsOther" model="[requisition:requisition]"/>
						--%>
						<g:render template="buttons" model="[requisition:requisition]"/>
					</td>				
				</tr>
			</tfoot>
			
		</table>			
	</g:if>
</div>