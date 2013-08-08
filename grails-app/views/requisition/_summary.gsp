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
                                <div class="barcode">${requisition.requestNumber}</div>
							</g:if>
						</div>

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
                            <span class="fade">&nbsp;|&nbsp;</span>
                            <span id="updated-by">
                                <warehouse:message code="default.updatedBy.label"/>:
                                <b>${requisition?.updatedBy?.name}</b>
                            </span>
                            <span class="fade">&nbsp;|&nbsp;</span>
                            <span id="date-created">
                                <warehouse:message code="default.dateCreated.label"/>:
                                <b><g:formatDate date="${requisition?.dateCreated }" format="MMM dd, yyyy"/></b>
                            </span>
                            <span class="fade">&nbsp;|&nbsp;</span>
                            <span id="created-by">
                                <warehouse:message code="default.createdBy.label"/>:
                                <b>${requisition?.createdBy?.name}</b>
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
					<td class="middle">
						<div class="center">
                                <div class="box">
                                    <div class="title ">
                                        <format:metadata obj="${requisition?.status }"/>
                                    </div>
                                    <g:if test="${requisition.lastUpdated}">
                                        <span class="fade">
                                            <format:datetime obj="${requisition.lastUpdated}"/>
                                        </span>
                                    </g:if>
                                </div>
							<div class="clear"></div>
						</div>

                        <%--
                        <g:if test="${requisition?.status == org.pih.warehouse.requisition.RequisitionStatus.VERIFYING}">
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
                                ${warehouse.message(code:'requisition.processed.label', default: 'items verified')}
                            </div>
                            <div class="right">
                                ${value}%
                            </div>
                        </g:if>
                        --%>
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
</div>
<div id="flow-header">
    <g:render template="/requisition/flowHeader" model="[requisition:requisition]"/>
</div>


<g:if test="${requisition?.destination?.id && requisition?.destination?.id != session?.warehouse?.id}">
    <div class="error">
        <warehouse:message code="requisition.wrongLocation.message" default="CAUTION: You appear to be logged into the wrong location! Making any changes to this requisition within this location may cause it to become invalid."/>
    </div>
</g:if>