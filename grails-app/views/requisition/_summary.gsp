<div id="requisition-summary">
	<g:if test="${requisition?.id}">
		<table>
			<tbody>			
				<tr>

                    <td class="top">
                        <g:render template="../requisition/actions" model="[requisition:requisition]" />
                    </td>
                    <td class="center">
						<div>
							<g:if test="${requisition?.requestNumber }">
								<img src="${createLink(controller:'product',action:'barcode',params:[data:requisition?.requestNumber,width:100,height:30,format:'CODE_128']) }"/>
							</g:if>
						</div>

						<div class="requisition-number">${requisition?.requestNumber }</div>
					</td>
					<td>
						<div class="title" id="name">
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
							<span class="requested-by">
								<warehouse:message code="requisition.requestedBy.label"/>:
								<b>${requisition?.requestedBy?.name}</b>
							</span>
                            <span class="fade">&nbsp;|&nbsp;</span>
                            <span id="last-updated">
                                <warehouse:message code="default.lastUpdated.label"/>:
                                <b><g:formatDate date="${requisition?.lastUpdated }" format="MMM dd, yyyy"/></b>
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
						</div>
                        <div class="clear"></div>
					    <div>

                            <g:set var="value"><g:formatNumber number="${requisition.calculatePercentageCompleted()}" maxFractionDigits="0" minFractionDigits="0"/></g:set>
                            <div id="progressbar-${requisition?.id }" class="progressbar"></div>
                            <script type="text/javascript">
                                $(function() {
                                    $( "#progressbar-${requisition?.id }" ).progressbar({value: ${value}});
                                });
                            </script>

					    </div>
                        <div class="left">
                            ${requisition?.completeRequisitionItems?.size()} of ${requisition?.initialRequisitionItems?.size()}
                            ${warehouse.message(code:'requisition.processed.label', default: 'processed')}
                        </div>
                        <div class="right">
                            ${value}%
                        </div>

                    </td>
					
										
				</tr>
			</tbody>
            <%--
            <tfoot>
                <tr>
                    <td class="top" colspan="4">
                        <g:render template="actionsOther" model="[requisition:requisition]"/>
                        <g:render template="../requisition/buttons" model="[requisition:requisition]"/>
                    </td>
                </tr>
            </tfoot>
            --%>
        </table>
	</g:if>
	<g:else>
		<div class="title" id="new-requisition">
            <h1>${requisition?.name?:warehouse.message(code: 'requisition.new.label') }</h1>
		</div>
		<div class="clear"></div>	
	</g:else>
</div>
<div id="flow-header">
    <g:render template="/requisition/flowHeader" model="[requisition:requisition]"/>
</div>


<g:if test="${requisition?.destination?.id && requisition?.destination?.id != session?.warehouse?.id}">
    <div class="error">
        <warehouse:message code="requisition.wrongLocation.message" default="CAUTION: You appear to be logged into the wrong location! Making any changes to this requisition within this location may cause it to become invalid."/>
    </div>
</g:if>