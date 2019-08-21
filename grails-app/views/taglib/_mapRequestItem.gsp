<g:if test="${attrs?.requestItem?.product }">	 
	<g:autoSuggest id="product-${attrs.status }" name="product" jsonUrl="${request.contextPath}/json/findProductByName" 
		width="400" styleClass="text" valueId="${attrs.requestItem?.product?.id }" valueName="${attrs.requestItem?.product?.name }"/>	
</g:if>
<g:else>
	<g:if test="${attrs?.products }">
		<g:select 
			name="requestItems[${attrs.status }].product.id" optionKey="id"
			from="${attrs?.products }" 
			value="${attrs?.requestItem?.product?.id }"
			noSelection="['null':'Select a product']"
			style="width: 400px;"/>
	</g:if>
	<g:else>
		<g:autoSuggest id="product-${attrs.status }" 
			name="product-${attrs.status }" styleClass="text" 
			placeholder="Select a product"
			jsonUrl="${request.contextPath }/json/findProductByName" width="300" />
			
	</g:else>
</g:else>
