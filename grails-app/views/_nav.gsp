<img src="${createLinkTo(dir:'images/icons/silk',file:'package.png')}" alt="Product" /> &nbsp;
<g:link controller="product" action="browse">Manage Products</g:link>
<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
<g:link controller="productType" action="create">Add a Product </g:link> 			
<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
<g:link controller="product" action="create">Add a Product Item</g:link> 			
<%-- 
<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
<g:form method="get" action="browse">
	<table>
		<tr class="prop">
			<td class="">
				<label>Search </label>

				<g:textField name="nameContains" value="${params.nameContains}" size="30"/>		
				<span class="buttons">
					<button type="submit" class="positive">
						${warehouse.message(code: 'default.button.go.label', default: 'Go')}</button>
				</span>											
			</td>
		</tr>
	</table>	
</g:form>			
<table>
	<tr>
		<td>
			<g:each in="${productTypes}" status="i" var="productType">
				<g:set var="selected" value="${productType?.id==selectedProductType?.id}"/>
				<g:if test="${selected }">
					<img src="${createLinkTo(dir:'images/icons/silk',file: 'bullet_go.png')}" style="vertical-align: middle;"/>																			
				</g:if>
				<g:else>
					<img src="${createLinkTo(dir:'images/icons/silk',file: 'bullet_white.png')}" style="vertical-align: middle;"/>																			
				</g:else>
				<span class="${(productType?.id==selectedProductType?.id)?'selected':''}">
					<a href="${createLink(action:'browse',params:["productTypeId":productType.id])}">${productType.name}</a>
					
				</span>
			</g:each>
		</td>
	</tr>
</table>	
--%>
			
