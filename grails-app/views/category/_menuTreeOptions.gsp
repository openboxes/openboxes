<g:if test="${root?.categories }">
	<g:set var="level" value="${level+1 }"/>
	<g:each var="child" in="${root.categories }">	
		<li>
			<g:link controller="product" action="browse" params="[categoryId: child?.id ]">${child?.name }</g:link>
			<g:if test="${recursive }">
				<g:if test="${child?.categories}">
					<ul>
						<g:render template="../category/menuTreeOptions" model="${['root': child, 'level': level, 'selected': selected]}"/>
					</ul>
				</g:if>
			</g:if>
		</li>
	</g:each>
</g:if>