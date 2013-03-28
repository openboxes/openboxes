
<ul class="category-tree">
	<g:if test="${category?.categories }">
		<g:if test="${category?.id}">
			<li id="${category?.id }" class="category-tree-item draggable droppable">
				<p>		
					<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
					<g:link action="edit" id="${category?.id }"><format:category category="${category}"/></g:link>
					
					<span style="float: right">
                        <img src="${createLinkTo(dir: 'images/icons/silk', file: 'arrow_nsew.png') }"/>
                        <g:link action="edit" id="${category?.id }"><img src="${createLinkTo(dir: 'images/icons/silk', file: 'pencil.png') }"/></g:link>
						<g:link action="deleteCategory" id="${category?.id }"><img src="${createLinkTo(dir: 'images/icons/silk', file: 'delete.png') }"/></g:link>	
					</span>
				</p>
			</li>
			<g:each var="childCategory" in="${category?.categories?.sort()}">		
				<g:render template="tree" model="${['category': childCategory]}"/>
			</g:each>
		</g:if>
		<g:else>
			<g:each var="childCategory" in="${category?.categories?.sort()}">	
				<g:render template="tree" model="${['category': childCategory]}"/>
			</g:each>
		</g:else>
	</g:if>
	<g:else>
		<g:if test="${category?.id }">
			<li id="${category?.id }" class="category-tree-item draggable droppable">
				<p>
					<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
					<g:link action="edit" id="${category?.id }"><format:category category="${category}"/></g:link>
					<span style="float: right">
                        <img src="${createLinkTo(dir: 'images/icons/silk', file: 'arrow_nsew.png') }"/>
                        <g:link action="edit" id="${category?.id }"><img src="${createLinkTo(dir: 'images/icons/silk', file: 'pencil.png') }"/></g:link>
						<g:link action="deleteCategory" id="${category?.id }" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"><img src="${createLinkTo(dir: 'images/icons/silk', file: 'delete.png') }"/></g:link>
					</span>
				</p>
			</li>
		</g:if>
	</g:else>
</ul>