<g:if test="${category?.categories }">
	<g:if test="${category?.id}">
		<li>
			<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
			<g:link action="tree" id="${category?.id }">${category?.name }</g:link>
			<g:link action="deleteCategory" id="${category?.id }"><img src="${createLinkTo(dir: 'images/icons/silk', file: 'delete.png') }"/></g:link>	
			<g:each var="childCategory" in="${category?.categories }">		
				<ul>
					<g:render template="tree" model="${['category': childCategory]}"/>
				</ul>
			</g:each>
		</li>
	</g:if>
	<g:else>
		<li>
			<g:each var="childCategory" in="${category?.categories }">		
				<ul>
					<g:render template="tree" model="${['category': childCategory]}"/>
				</ul>
			</g:each>
		</li>	
	</g:else>
</g:if>
<g:else>
	<g:if test="${category?.id }">
		<li>
			<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
			<g:link action="tree" id="${category?.id }">${category?.name }</g:link>
			<g:link action="deleteCategory" id="${category?.id }"><img src="${createLinkTo(dir: 'images/icons/silk', file: 'delete.png') }"/></g:link>			
		</li>
	</g:if>
</g:else>
