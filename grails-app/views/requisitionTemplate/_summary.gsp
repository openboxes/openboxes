<div id="requisition-template-header">
	<g:if test="${requisition?.id}">
		<table>
			<tbody>			
				<tr>
					<td class="center">
                        <g:if test="${requisition?.requestNumber }">
                            <div>
                                <img src="${createLink(controller:'product',action:'barcode',params:[data:requisition?.requestNumber,width:100,height:30,format:'CODE_128']) }"/>
    						</div>
                            <div class="requisition-number">
                                ${requisition?.requestNumber }
                            </div>
                        </g:if>
					</td>				
					<td>
						<div class="title" id="description">
							${requisition?.name }
						</div> 						
						<div class="clear"></div>
						<div class="fade">
							<span id="origin">
								<warehouse:message code="requisition.origin.label"/>:
								<b>${requisition?.origin?.name?.encodeAsHTML()?:warehouse.message(code: 'default.none.label')}</b>
							</span>							
							<span class="fade">&nbsp;|&nbsp;</span>
							<span id="destination">
								<warehouse:message code="requisition.destination.label"/>:
	                            <b>${requisition?.destination?.name?.encodeAsHTML()?:session?.warehouse?.name}</b>
							</span>							
							<span class="fade">&nbsp;|&nbsp;</span>
							<span class="requested-date">
								<warehouse:message code="requisition.date.label"/>: 
								<b><format:date obj="${requisition?.dateRequested}"/></b>
							</span>
							<span class="fade">&nbsp;|&nbsp;</span>
							<span class="request-items">
								<warehouse:message code="requisition.requisitionItem.label"/>:
								<b>${requisition?.requisitionItems?.size()?:0}</b>
							</span>
							
							<%--
							<span class="fade">&nbsp;|&nbsp;</span>
							<span class="requested-by">
								<warehouse:message code="requisition.processedBy.label"/>:
								<b>${requisition?.createdBy?.name?:warehouse.message(code: 'default.none.label') }</b>								
							</span>
							<span class="fade">&nbsp;|&nbsp;</span>
							<span class="requested-by">
								<warehouse:message code="requisition.requestedBy.label"/>:
								<b>${requisition?.requestedBy?.name?:warehouse.message(code: 'default.none.label') }</b>								
							</span>
							<span class="fade">&nbsp;|&nbsp;</span>
							<span id="recipientProgram">
								<warehouse:message code="requisition.recipientProgram.label"/>:
	                            <b>${requisition?.recipientProgram?:warehouse.message(code: 'default.none.label') }</b>
							</span>							
							<span class="fade">&nbsp;|&nbsp;</span>
							<span id="recipient">
								<warehouse:message code="requisition.recipient.label"/>:
	                           <b>${requisition?.recipient?.name?:warehouse.message(code: 'default.none.label')}</b>
							</span>							
							 --%>	
						</div>
					</td>
					<td>
						<div class="left">	
							<div class="title">${requisition?.status }</div>
							<div class="clear"></div>
							<div class="fade">
								<g:formatDate date="${requisition?.lastUpdated }" format="MMM dd, yyyy hh:mma"/>
							</div>
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
					<td class="left">
						<div id="new-requisition-template" class="title">
                            ${warehouse.message(code: 'requisitionTemplate.new.label') }
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
	</g:else>
</div>