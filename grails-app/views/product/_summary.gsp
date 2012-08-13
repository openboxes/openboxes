<div style="padding: 3px;">
	<table style="width: 0%">
		<tbody>			
			<tr>
				<td class="top">
					<g:render template="../product/actions" model="[productInstance:productInstance]" />
				</td>
				
				<td class="middle">
					<div>
						<g:if test="${productInstance?.manufacturer }">
							<span class="manufacturer">${productInstance?.manufacturer }</span> 
						</g:if>
						<g:if test="${productInstance?.manufacturerCode }">
							<span class="manufacturerCode">#${productInstance?.manufacturerCode }</span>
						</g:if>
					</div>
					<div class="title">
						${productInstance?.name} 
						<%-- 
						<span class="product-uom">(${productInstance?.unitOfMeasure })</span>
						<span class="product-category">
							${productInstance?.category?.name }
						</span>
						--%>
					</div>
					<div class="product-status">
						<%-- 
						<g:if test="${productInstance?.status?.code }"
							<span class="status">
								<warehouse:message code="shipping.status.label"/>:
								<label><format:metadata obj="${shipmentInstance?.status?.code}"/></label>
							</span>
						</g:if>
						<g:if test="${!shipmentInstance?.hasShipped() }">
							<g:if test="${shipmentInstance?.expectedShippingDate }">
								<span class="expectedShippingDate">
									<warehouse:message code="shipping.expectedShippingDate.label"/>:						
									<label><format:date obj="${shipmentInstance?.expectedShippingDate}"/></label> 
								</span>
							</g:if>
						</g:if>
						<g:else>
							<span class="actualShippingDate">
								<warehouse:message code="shipping.actualShippingDate.label"/>:						
								<label><format:date obj="${shipmentInstance?.actualShippingDate}"/></label> 
							</span>
						</g:else>
						<g:if test="${!shipmentInstance?.wasReceived() }">
							<g:if test="${shipmentInstance?.expectedDeliveryDate }">
								<span class="expectedDeliveryDate">
									<warehouse:message code="shipping.expectedDeliveryDate.label"/>:						
									<label>
										<format:date obj="${shipmentInstance?.expectedDeliveryDate}"/>
									</label> 
								</span>
							</g:if>
						</g:if>
						<g:else>
							<span class="actualDeliveryDate">
								<warehouse:message code="shipping.actualDeliveryDate.label"/>:						
								<label><format:date obj="${shipmentInstance?.actualDeliveryDate}"/></label> 
							</span>
						</g:else>
						--%>
					</div>
				</td>	
									
				<td style="text-align: right;">
					<div class="fade" style="font-weight: bold; font-size:1.5em;">
						<%-- 
						<img src="${createLinkTo(dir:'images/icons/eventStatus',file: shipmentInstance?.status?.name?.toLowerCase() + '.png')}"
								alt="${shipmentInstance?.status?.name}" style="vertical-align: middle"/>							
						${shipmentInstance?.status?.name}
								--%>
					</div>
					
					
				</td>
			</tr>
		</tbody>
	</table>
</div>
