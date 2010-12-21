<g:if test="${category?.categories }">
	<g:set var="level" value="${level+1 }"/>
	<g:each var="childCategory" in="${category.categories }">	
		<option value="${childCategory.id }" ${(childCategory?.id == selectedCategory?.parentCategory?.id)?'selected':'' }>
			${ new String("&nbsp").multiply(5*(level-1)) }			
			<g:if test="${!childCategory.parentCategory}"> + ${childCategory.name }</g:if>
			<g:else> - ${childCategory?.name }</g:else>
		</option>
		<g:render template="optionTree" model="${['category': childCategory, 'level': level, 'selectedCategory': selectedCategory]}"/>
	</g:each>
</g:if>