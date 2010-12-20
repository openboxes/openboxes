<g:if test="${category.categories }">
	<g:each var="childCategory" in="${category.categories }">
		<li>
			<g:if test="${!childCategory.parentCategory}"><b>${childCategory.name }</b></g:if>
			<g:else>${childCategory?.name }</g:else>
			<ul>
				<g:render template="tree" model="${['category': childCategory ]}"/>
			</ul>
		</li>
	</g:each>
</g:if>