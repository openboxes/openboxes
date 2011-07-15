<div id="product-details">
	
		<h2 class="fade">Product Details</h2>
		<table>
			<tr class="details odd">	
				<td class="left label">
					<span class="name">Name</span>
				</td>
				<td colspan="2">
					<span class="value">${productInstance?.name }</span>
				</td>
			</tr>
			<tr class="details even">	
				<td class="left label">
					<span class="name">Units</span>
				</td>
				<td colspan="2">
					<span class="value">${productInstance?.unitOfMeasure }</span>
				</td>
			</tr>
			<tr class="details odd">	
				<td class="left label">
					<span class="name">Category</span>
				</td>
				<td>
					<span class="value">
						<g:if test="${productInstance?.category?.name }">
							${productInstance?.category?.name }
						</g:if>
						<g:else>
							<span class="fade">None</span>
						</g:else>
					</span>
				</td>
			</tr>
			
			<tr class="details even">	
				<td class="left label">
					<span class="name">Manufacturer</span>
				</td>
				<td>
					<span class="value">
						<g:if test="${productInstance?.manufacturer }">
							${productInstance?.manufacturer }
						</g:if>
						<g:else>
							<span class="fade">None</span>
						</g:else>
					</span>
				</td>
			</tr>
			
			<tr class="details odd">	
				<td class="left label">
					<span class="name">Manufacturer Code</span>
				</td>
				<td>
					<span class="value">
						<g:if test="${productInstance?.manufacturerCode }">
							${productInstance?.manufacturerCode }
						</g:if>
						<g:else>
							<span class="fade">None</span>
						</g:else>
					</span>
				</td>
			</tr>
			
			<tr class="details even">	
				<td class="left label">
					<span class="name">UPC</span>
				</td>
				<td>
					<span class="value">
						<g:if test="${productInstance?.upc }">
							${productInstance?.upc }
						</g:if>
						<g:else>
							<span class="fade">None</span>
						</g:else>
					</span>
				</td>
			</tr>
			
			<tr class="details odd">	
				<td class="left label">
					<span class="name">NDC</span>
				</td>
				<td>
					<span class="value">
						<g:if test="${productInstance?.ndc }">
							${productInstance?.ndc }
						</g:if>
						<g:else>
							<span class="fade">None</span>
						</g:else>
					</span>
				</td>
			</tr>
			
			<tr class="details even">	
				<td class="left label">
					<span class="name">Cold Chain</span>
				</td>
				<td>
					<span class="value">${productInstance?.coldChain?'Yes':'No' }</span>
				</td>
			</tr>
			<g:each var="productAttribute" in="${productInstance?.attributes}" status="status">
				<tr class="${status%2==0?'odd':'even' }">
					<td class="label left">
						<span class="name">${productAttribute?.attribute.name }</span>
					</td>
					<td>
						<span class="value">${productAttribute.value }</span>
					</td>
				</tr>													
			</g:each>

	
		</table>

		<h2 class="fade">Product Status</h2>
		<table>
			<tr class="details odd">
				<td class="label left">
					<span class="name">Supported</span>
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
									Yes  
								</g:if>			
								<g:else>										
									No 
								</g:else>
							</g:if>
							<g:else>
								<span class="fade">N/A</span>
							</g:else>
						</span>
					</span>
				</td>
			</tr>				
			<tr class="details even">	
				<td class="label left">
					<span class="name">Status</span>
				</td>
				<td>
					<span class="value">
						<g:if test="${totalQuantity <= 0}">
							<span style="color: red">No stock</span>
						</g:if>
						<g:elseif test="${totalQuantity <= inventoryLevelInstance?.minQuantity}">
							<span style="color: yellow">Low stock</span>
						</g:elseif>
						<g:elseif test="${totalQuantity <= inventoryLevelInstance?.reorderQuantity }">
							<span style="color: orange;">Reorder</span>
						</g:elseif>
						<g:else>
							<span style="color: green;">In Stock</span>
						</g:else>
					</span>
				</td>
			</tr>				
		
			<tr class="details odd">	
				<td class="label left">
					<span class="name">On-Hand Quantity</span>
				</td>
				<td>
					<span class="value">
						<b>${totalQuantity }</b></span>
				</td>
			</tr>			
		
			<tr class="details even">
				<td class="label left">
					<span class="name">Min Level</span>
				</td>
				<td>
					<span id="minQuantityTextValue" class="value" >
						<span id="minQuantityValue">
							<g:if test="${inventoryLevelInstance?.minQuantity}">
								${inventoryLevelInstance?.minQuantity?:'' }
							</g:if>
							<g:else>
								<span class="fade">N/A</span>
							</g:else>
						</span>
						&nbsp;
					</span>
				</td>				
			</tr>
			<tr class="details odd">
				<td class="label left">
					<span class="name">Reorder Level</span>
				</td>
				<td class="value left">
					<span id="reorderQuantityTextValue" class="value">
						<span id="reorderQuantityValue">
							<g:if test="${inventoryLevelInstance?.reorderQuantity}">
								${inventoryLevelInstance?.reorderQuantity?:'' }
							</g:if>
							<g:else>
								<span class="fade">N/A</span>
							</g:else>
						</span>
					</span>
				</td>
			</tr>				
		</table>
</div>



