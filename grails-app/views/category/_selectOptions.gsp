<g:set var="level" value="${level+1 }"/>
<g:if test="${category?.categories }">
	<g:set var="productCount" value="${category?.products?.size() }"/>	
	<g:if test="${!excludeSpaces }">	
		<option value="${category?.id ?: 'null' }" ${(category?.id && category?.id == selected?.id) ? 'selected' : '' }>
			${ new String("&nbsp").multiply(2*(level)) }
			<g:if test="${!category.parentCategory}"><format:category category="${category}" shorten="true"/> </g:if>
			<g:else><format:category category="${category}" shorten="true"/></g:else>
			<g:if test="${productCount > 0 }">
				<span class="fade">(${productCount })</span>
			</g:if>
		</option>
	</g:if>
	<g:else>
		<option value="${category?.id ?: 'null' }" ${(category?.id && category?.id == selected?.id) ? 'selected' : '' }>
			<format:category category="${category}" shorten="true"/>
		</option>
	</g:else>
	<g:each var="childCategory" in="${category.categories }">	
		<g:render template="../category/selectOptions" model="${[category:childCategory,level:level,selected:selected,excludeSpaces:excludeSpaces]}"/>
	</g:each>
</g:if>
<g:else>
	<g:set var="productCount" value="${category?.products?.size() }"/>		
	
	<g:if test="${!excludeSpaces }">	
		<option value="${category?.id ?: 'null' }" ${(category?.id == selected?.id)?'selected':'' }>
			${ new String("&nbsp").multiply(2*(level)) } 
			<g:if test="${!category?.parentCategory}"><format:category category="${category}" shorten="true"/> </g:if>
			<g:else><format:category category="${category}" shorten="true"/></g:else>
			<g:if test="${productCount > 0 }">
				<span class="fade">(${productCount })</span>
			</g:if>
		</option>
	</g:if>
	<g:else>
		<option value="${category?.id ?: 'null' }" ${(category?.id && category?.id == selected?.id) ? 'selected' : '' }>
			<format:category category="${category}"/>
		</option>
	</g:else>
</g:else>
