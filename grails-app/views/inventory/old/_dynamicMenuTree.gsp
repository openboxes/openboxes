<style>
	span.selected { font-weight: bold; } 
</style>
<div id="menu" class="dropdown">
	<g:if test="${category?.categories }">
		<g:set var="level" value="${level+1 }"/>
		<ul>
			<g:each var="childCategory" in="${category.categories }">	
				<li class="${(childCategory?.id == selectedCategory?.id)?'selected':'' }"">
					${ new String("&nbsp").multiply(5*(level-1)) }			
					<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
					<g:link controller="inventory" action="browse" params="[categoryId: childCategory?.id ]">
						<span class="${(selectedCategory?.id==childCategory?.id)?'selected':''}"><format:category category="${childCategory}"/></span>
					</g:link>
				</li>
				<g:if test="${(selectedCategory?.id==childCategory?.id)?'selected':''}">
					<g:render template="dynamicMenuTree" model="${['category': childCategory, 'level': level, 'selectedCategory': selectedCategory]}"/>
				</g:if>
			</g:each>
		</ul>
	</g:if>
</div>