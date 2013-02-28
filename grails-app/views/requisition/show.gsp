
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition').toLowerCase()}" />
        <title><warehouse:message code="default.view.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
            
                <g:render template="summary" model="[requisition:requisition]"/>
                
                <div id="tabs-details">

<div class="yui-ga">
	<div class="yui-u first">
				<div class="box">
					<a class="toggle" href="javascript:void(0);">
						<img src="${createLinkTo(dir: 'images/icons/silk', file: 'section_collapsed.png')}" style="vertical-align: bottom;"/>
					</a>
					<h3 style="display: inline">${requisition?.requestNumber } ${requisition?.name }</h3>				
					<g:if test="${requisition?.id }">
						<g:if test="${!params.editHeader }">
							<g:link controller="requisition" action="edit" id="${requisition?.id }" params="[editHeader:'true']">
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'pencil.png')}" style="vertical-align: bottom;"/>
							</g:link>
						</g:if>
						<g:else>
							<g:link controller="requisition" action="edit" id="${requisition?.id }">
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'cross.png')}" style="vertical-align: bottom;"/>
							</g:link>									
						</g:else>
					</g:if>
				</div>
				<div id="requisition-header-details" class="dialog ui-validation expandable" style="${(!requisition?.id||params.editHeader)?'':'display: none;'}">
					<%-- 
					<div id="requisition-header">
						<div class="title" data-bind="html: requisition.name"></div>
						<div class="time-stamp fade"
							data-bind="html:requisition.lastUpdated"></div>
						<div class="status fade" data-bind="html: requisition.status"></div>
					</div>
					--%>
					
					
					<div class="yui-g">
						<div class="yui-u first">
							<table id="requisition-header-details-table" class="header-summary-table requisition">
								<tbody>							
									<tr class="prop">
										<td class="name">
											<label for="origin.id"> <g:if
													test="${requisition.isWardRequisition()}">
													<warehouse:message code="requisition.requestingWard.label" />
												</g:if> <g:else>
													<warehouse:message code="requisition.requestingDepot.label" />
												</g:else>
											</label>
										</td>
										<td class="value ${hasErrors(bean: requisition, field: 'origin', 'errors')}">
											${requisition?.origin?.name }
										</td>
									</tr>
									<tr class="prop">
										<td class="name"><label><warehouse:message
													code="requisition.requestedBy.label" /></label></td>
										<td class="value">
											${requisition?.requestedBy?.name }
										</td>
									</tr>
									<tr class="prop">
										<td class="name"><label><warehouse:message
													code="requisition.dateRequested.label" /></label></td>
										<td class="value">
											${requisition?.dateRequested }
										</td>
									</tr>
									<tr class="prop">
										<td class="name"><label><warehouse:message code="requisition.requestedDeliveryDate.label" /></label></td>
										<td class="value">
											
											${requisition?.requestedDeliveryDate }
										</td>
									</tr>									
									
								</tbody>
							</table>
						</div>
						<div class="yui-u">
							<table class="requisition">
								<tbody>
									<tr class="prop">
										<td class="name">
											<label for="destination.id"> 
												<warehouse:message code="requisition.destination.label" />
											</label>
										</td>
										<td class="value">	
											${session?.warehouse?.name }
										</td>
									</tr>
									<tr class="prop">
										<td class="name">
											<label><warehouse:message
													code="requisition.processedBy.label" /></label>
										</td>
										<td class="value">
											${requisition?.createdBy?.name?:session?.user?.name }
										</td>
									</tr>
									<g:if test="${requisition.isDepotRequisition()}">
										<tr>
											<td class="name"><label><warehouse:message
														code="requisition.program.label" /></label></td>
											<td class="value">
												${requisition?.recipientProgram }
											</td>
										</tr>
									</g:if>
									<tr class="prop">							
										<td class="name">
											<label for="description"> 
												<warehouse:message code="default.description.label" />
											</label>
										</td>
										<td class="value">	
											${requisition?.description?:warehouse.message(code:'default.none.label') }
										</td>
									</tr>	
								</tbody>
							</table>							
						</div>
					</div>
				</div>
								

					<table class="requisition">
						<thead>
							<tr class="odd">
								<th><warehouse:message code="product.label" /></th>
								<th class="right"><warehouse:message code="requisition.quantity.label" /></th>
								<th class="right"><warehouse:message code="picklist.quantity.label" /></th>
								<th class="right"><warehouse:message code="requisitionItem.quantityCanceled.label" /></th>
								<th class="right"><warehouse:message code="requisition.quantityRemaining.label" /></th>
								<th><warehouse:message code="product.uom.label" /></th>
								<th><warehouse:message code="requisition.progressBar.label" /></th>
								<th><warehouse:message code="requisition.progressPercentage.label" /></th>
							</tr>
						</thead>
						<tbody>
							<g:if test="${requisition?.requisitionItems?.size() == 0}">
								<tr class="prop odd">
									<td colspan="8" class="center"><warehouse:message
											code="requisition.noRequisitionItems.message" /></td>
								</tr>
							</g:if>
							<g:each var="requisitionItem"
								in="${requisition?.requisitionItems}" status="i">
								<g:set var="quantityRemaining"
									value="${(requisitionItem?.quantity?:0)-(requisitionItem?.calculateQuantityPicked()?:0)}" />
								<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
									<td class="product">
										<g:link controller="inventoryItem" action="showStockCard" id="${requisitionItem?.product?.id }">
										<format:metadata
											obj="${requisitionItem?.product?.name}" /></g:link></td>
									<td class="quantity right">
										${requisitionItem?.quantity} 
									</td>
									<td class="quantityPicked right">
										${requisitionItem?.calculateQuantityPicked()?:0}
									</td>
									<td class="quantityCanceled right">
										${requisitionItem?.quantityCanceled?:0}
									</td>
									<td class="quantityRemaining right">
										${quantityRemaining } 
									</td>
									<td>
										${requisitionItem?.product.unitOfMeasure?:"EA" }
									</td>
									<td>
										<g:set var="value" value="${((requisitionItem?.calculateQuantityPicked()?:0)+(requisitionItem?.quantityCanceled?:0))/(requisitionItem?.quantity?:1) * 100 }" />
										<div id="progressbar-${requisitionItem?.id }" class="progressbar" style="width: 100px;"></div>
										<script type="text/javascript">
											    $(function() {
											    	$( "#progressbar-${requisitionItem?.id }" ).progressbar({value: ${value}});
											    });
									    </script>								
									</td>
									<td>
										${value }
									</td>
								</tr>
							</g:each>
						</tbody>
					</table>
				</div>
				<div class="clear"></div>	
				<div class="buttons">
					<div class="right">
						<g:link controller="requisition" action="list" class="button">
							<warehouse:message code="default.button.back.label"/>	
						</g:link>

						<g:link controller="requisition" action="edit" id="${requisition.id }" class="button">
							<warehouse:message code="default.button.next.label"/>	
						</g:link>
					</div>
				</div>				
			</div>
		</div>
         
    </body>
</html>
