<g:set var="level" value="${level+1 }"/>
<g:if test="${category?.categories }">
	<g:set var="productCount" value="${category?.products?.size() }"/>		
	<option value="${category?.id ?: 'null' }" ${(category?.id && category?.id == selected?.id) ? 'selected' : '' }>
		${ new String("&nbsp").multiply(2*(level)) } 
		<g:if test="${!category.parentCategory}">${category.name } </g:if>
		<g:else>${category?.name }</g:else>
		<g:if test="${productCount > 0 }">
			<span class="fade">(${productCount })</span>
		</g:if>
	</option>

	<g:each var="childCategory" in="${category.categories }">	
		<g:render template="../category/selectOptions" model="${['category': childCategory, 'level': level, 'selected': selected]}"/>
	</g:each>
</g:if>
<g:else>
	<g:set var="productCount" value="${category?.products?.size() }"/>		
	<option value="${category?.id }" ${(category?.id == selected?.id)?'selected':'' }>
		${ new String("&nbsp").multiply(2*(level)) } 
		<g:if test="${!category?.parentCategory}">${category?.name } </g:if>
		<g:else>${category?.name }</g:else>
		<g:if test="${productCount > 0 }">
			<span class="fade">(${productCount })</span>
		</g:if>
	</option>
</g:else>
