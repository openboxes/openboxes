<%-- 
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
--%>
<g:if test="${category?.categories }">
	<g:set var="level" value="${level+1 }"/>
	<g:each var="childCategory" in="${category.categories }">	
		<option value="${childCategory.id }" ${(childCategory?.id == selected?.id)?'selected':'' }>
			${ new String("&nbsp").multiply(1*(level)*(level)) } 
			<%--<g:render template="../category/breadcrumb" model="${['categoryInstance':childCategory] }"/>--%>			
			<g:if test="${!childCategory.parentCategory}">${childCategory.name } </g:if>
			<g:else>${childCategory?.name }</g:else>
			<span class="fade">(${childCategory?.products?.size() })</span>
		</option>
		<g:render template="../category/selectOptions" model="${['category': childCategory, 'level': level, 'selected': selected]}"/>
	</g:each>
</g:if>
<g:else>

</g:else>
