<div>            	
	<span class="menuButton">
		<g:link class="list" controller="product" action="browse"><warehouse:message code="default.browse.label" args="['Products']"/></g:link>
	</span>
	<span class="menuButton">
		<g:link class="new" controller="product" action="create" params="['category.id':params.categoryId]"><warehouse:message code="default.add.label" args="['Product']"/></g:link> 			
	</span>
	<span class="menuButton">
		<g:link class="edit" controller="category" action="tree"><warehouse:message code="default.edit.label" args="['Categories']"/></g:link>
	</span>
	<span class="menuButton">
		<g:link class="edit" controller="attribute" action="list"><warehouse:message code="default.edit.label" args="['Attributes']"/></g:link>
	</span>
</div>



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
			
