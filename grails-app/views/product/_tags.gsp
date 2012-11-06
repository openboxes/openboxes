<g:each var="tag" in="${tags}">
	<span class="tag">
		<g:link controller="inventory" action="browse" params="['tag':tag.tag]">${tag.tag }</g:link>
	</span>
</g:each>