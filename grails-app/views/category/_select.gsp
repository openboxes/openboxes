<li>${root?.name }</li>
<g:if test="${root?.categories }">
	<g:set var="level" value="${level+1 }"/>
	<g:each var="childCategory" in="${root.categories }">	
		<option value="${childCategory.id }" ${(childCategory?.id == selected?.id)?'selected':'' }>
			${ new String("&nbsp").multiply(5*(level-1)) }			
			<g:if test="${!childCategory.parentCategory}"> + ${childCategory.name }</g:if>
			<g:else> - ${childCategory?.name }</g:else>
		</option>
		<g:render template="../category/selectOptions" model="${['category': childCategory, 'level': level, 'selected': selected]}"/>
	</g:each>
</g:if>