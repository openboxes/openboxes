<g:if test="${category?.categories }">
	<g:set var="level" value="${level+1 }"/>
	<ul>
		<g:each var="childCategory" in="${category.categories }">	
			<li class="${(childCategory?.id == selectedCategory?.id)?'selected':'' }"">
				${ new String("&nbsp").multiply(5*(level-1)) }			
				<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
				<g:link controller="inventory" action="browse" params="[categoryId: childCategory?.id ]">
					<format:category category="${childCategory}"/>
				</g:link>
			</li>
			<g:render template="menuTree" model="${['category': childCategory, 'level': level, 'selectedCategory': selectedCategory]}"/>
		</g:each>
	</ul>
</g:if>