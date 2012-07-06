	<li rel="${attrs?.from?.id }">
		${attrs.from}
		<g:if test="${attrs.from.categories }">
			<ul>
				<g:each var="category" in="${attrs.from.categories}">
					<g:selectCategory_v2 from="${category }" depth="${attrs.depth+1 }"/>
				</g:each>
			</ul>
		</g:if>
	</li>
