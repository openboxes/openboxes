<g:if test="${category?.categories }">
	<g:each var="childCategory" in="${category?.categories }">
		<li>
			<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
			<g:link action="editCategory" id="${childCategory?.id }">
				<g:if test="${!childCategory?.parentCategory}"><b>${childCategory?.name }</b></g:if>
				<g:else>${childCategory?.name }</g:else>
				
			</g:link>
			<g:link action="deleteCategory" id="${childCategory?.id }"><img src="${createLinkTo(dir: 'images/icons/silk', file: 'delete.png') }"/></g:link>
			
			<ul>
				<g:render template="tree" model="${['category': childCategory]}"/>
			</ul>
		</li>
	</g:each>
</g:if>