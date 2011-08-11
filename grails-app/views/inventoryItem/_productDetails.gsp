<div id="product-details">
	
		<h2 class="fade"><warehouse:message code="product.details.label"/></h2>
		<table>
			<tr class="details odd">	
				<td class="left label">
					<span class="name"><warehouse:message code="default.name.label"/></span>
				</td>
				<td colspan="2">
					<span class="value"><format:product product="${productInstance}"/></span>
				</td>
			</tr>
			<tr class="details even">	
				<td class="left label">
					<span class="name"><warehouse:message code="product.units.label"/></span>
				</td>
				<td colspan="2">
					<span class="value"><format:metadata obj="${productInstance?.unitOfMeasure}"/></span>
				</td>
			</tr>
			<tr class="details odd">	
				<td class="left label">
					<span class="name"><warehouse:message code="category.label"/></span>
				</td>
				<td>
					<span class="value">
						<g:if test="${productInstance?.category?.name }">
							<format:category category="${productInstance?.category}"/>
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.none.label"/></span>
						</g:else>
					</span>
				</td>
			</tr>
			
			<tr class="details even">	
				<td class="left label">
					<span class="name"><warehouse:message code="product.manufacturer.label"/></span>
				</td>
				<td>
					<span class="value">
						<g:if test="${productInstance?.manufacturer }">
							${productInstance?.manufacturer }
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.none.label"/></span>
						</g:else>
					</span>
				</td>
			</tr>
			
			<tr class="details odd">	
				<td class="left label">
					<span class="name"><warehouse:message code="product.manufacturerCode.label"/></span>
				</td>
				<td>
					<span class="value">
						<g:if test="${productInstance?.manufacturerCode }">
							${productInstance?.manufacturerCode }
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.none.label"/></span>
						</g:else>
					</span>
				</td>
			</tr>
			
			<tr class="details even">	
				<td class="left label">
					<span class="name"><warehouse:message code="product.upc.label"/></span>
				</td>
				<td>
					<span class="value">
						<g:if test="${productInstance?.upc }">
							${productInstance?.upc }
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.none.label"/></span>
						</g:else>
					</span>
				</td>
			</tr>
			
			<tr class="details odd">	
				<td class="left label">
					<span class="name"><warehouse:message code="product.ndc.label"/></span>
				</td>
				<td>
					<span class="value">
						<g:if test="${productInstance?.ndc }">
							${productInstance?.ndc }
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.none.label"/></span>
						</g:else>
					</span>
				</td>
			</tr>
			
			<tr class="details even">	
				<td class="left label">
					<span class="name"><warehouse:message code="product.coldChain.label"/></span>
				</td>
				<td>
					<span class="value">${productInstance?.coldChain ? warehouse.message(code:'default.yes.label') : warehouse.message(code:'default.no.label') }</span>
				</td>
			</tr>
			<g:each var="productAttribute" in="${productInstance?.attributes}" status="status">
				<tr class="${status%2==0?'odd':'even' }">
					<td class="label left">
						<span class="name"><format:metadata obj="${productAttribute?.attribute}"/></span>
					</td>
					<td>
						<span class="value">${productAttribute.value }</span>
					</td>
				</tr>													
			</g:each>

	
		</table>

		<h2 class="fade"><warehouse:message code="product.status.label"/></h2>
		<table>
			<tr class="details odd">
				<td class="label left">
					<span class="name"><warehouse:message code="product.supported.label"/></span>
				</td>
				<td>
				<%-- 
					<script>
						$(document).ready(function() {
							$('#toggleSupportedImage').click(function() {	
								var image = $('#toggleSupportedImage');
								var currImageSrc = image.attr("src");
								var playImageSrc = "${createLinkTo(dir: 'images/icons/silk', file: 'control_play.png' )}";							
								var stopImageSrc = "${createLinkTo(dir: 'images/icons/silk', file: 'control_stop.png' )}";							
								var imageSrc = (currImageSrc == playImageSrc)?stopImageSrc:playImageSrc;							
								image.attr("src",imageSrc);								
							});
						});
					</script>
				--%>			
					<span id="supported" class="value">
						<span id="supportedValue">
							<g:if test="${inventoryLevelInstance}">
								<g:if test="${inventoryLevelInstance?.supported}">
									<warehouse:message code="default.yes.label"/>
								</g:if>			
								<g:else>										
									<warehouse:message code="default.no.label"/>
								</g:else>
							</g:if>
							<g:else>
								<span class="fade"><warehouse:message code="default.na.label"/></span>
							</g:else>
						</span>
					</span>
				</td>
			</tr>				
			<tr class="details even">	
				<td class="label left">
					<span class="name"><warehouse:message code="default.status.label"/></span>
				</td>
				<td>
					<span class="value">
						<g:if test="${totalQuantity <= 0}">
							<span style="color: red"><warehouse:message code="product.noStock.label"/></span>
						</g:if>
						<g:elseif test="${totalQuantity <= inventoryLevelInstance?.minQuantity}">
							<span style="color: yellow"><warehouse:message code="product.lowStock.label"/></span>
						</g:elseif>
						<g:elseif test="${totalQuantity <= inventoryLevelInstance?.reorderQuantity }">
							<span style="color: orange;"><warehouse:message code="product.reorder.label"/></span>
						</g:elseif>
						<g:else>
							<span style="color: green;"><warehouse:message code="product.inStock.label"/></span>
						</g:else>
					</span>
				</td>
			</tr>				
		
			<tr class="details odd">	
				<td class="label left">
					<span class="name"><warehouse:message code="product.onHandQuantity.label"/></span>
				</td>
				<td>
					<span class="value">
						<b>${totalQuantity }</b></span>
				</td>
			</tr>			
		
			<tr class="details even">
				<td class="label left">
					<span class="name"><warehouse:message code="product.minLevel.label"/></span>
				</td>
				<td>
					<span id="minQuantityTextValue" class="value" >
						<span id="minQuantityValue">
							<g:if test="${inventoryLevelInstance?.minQuantity}">
								${inventoryLevelInstance?.minQuantity?:'' }
							</g:if>
							<g:else>
								<span class="fade"><warehouse:message code="default.na.label"/></span>
							</g:else>
						</span>
						&nbsp;
					</span>
				</td>				
			</tr>
			<tr class="details odd">
				<td class="label left">
					<span class="name"><warehouse:message code="product.reorderLevel.label"/></span>
				</td>
				<td class="value left">
					<span id="reorderQuantityTextValue" class="value">
						<span id="reorderQuantityValue">
							<g:if test="${inventoryLevelInstance?.reorderQuantity}">
								${inventoryLevelInstance?.reorderQuantity?:'' }
							</g:if>
							<g:else>
								<span class="fade"><warehouse:message code="default.na.label"/></span>
							</g:else>
						</span>
					</span>
				</td>
			</tr>				
		</table>
</div>



