<div >
	<table style="width: 0%" id="product-summary">
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
						
					</div>
				</td>	
									
				<td style="text-align: right; vertical-align: middle">
					<g:each var="tag" in="${productInstance?.tags}">
						<span class="tag">
							<g:link controller="inventory" action="browse" params="['tag':tag.tag]">
								${tag.tag }
							</g:link>
						</span>
					</g:each>
					
				</td>
			</tr>
		</tbody>
	</table>
</div>
