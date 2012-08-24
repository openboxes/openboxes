<g:if test="${attrs?.requestItem?.product }">
	<%--
	<format:product product="${attrs?.requestItem?.product }"/>
	<g:hiddenField name="requestItems[${attrs.status }].product.id" value="${attrs.requestItem?.product?.id }"/>
	 --%>
	 
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
<%--
<g:if test="${attrs.products }">
	<table>
		<g:each var="inventoryItem" in="${attrs.inventoryItems }">
			<tr>
				<td>
					<g:textField class="text" name="picklistItems[${attrs.index }].quantity" value="${attrs.requestItem?.quantity }" size="5"/>				
				</td>
				<td>${inventoryItem.product.name }</td>	
				<td>${inventoryItem.lotNumber }</td>			
				<td>${inventoryItem.expirationDate}</td>			
			</tr>
		</g:each>	
	</table>
	
</g:if>
<g:else>
	<table>
		<tr>
			<td>
				<g:textField class="text" name="picklistItems[${attrs.index }].quantity" 
					value="${attrs.requestItem?.quantity }" size="5"/>
			</td>
			<td>
				${attrs.product }
			</td>
			<td>
				<g:select name="picklistItems[${attrs.index }].inventoryItem.id" 
					from="${attrs.inventoryItems }" 
					optionKey="id" 
					optionValue="${{it.lotNumber + ' [expires: ' + it.expirationDate + ']'}}"
					value="${attrs?.inventoryItem?.id }"/>
					
					
			</td>
			<td>
			
			</td>
		</tr>
	</table>
</g:else>
--%>
					
									