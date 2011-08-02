<div id="searchCriteria"  style="border-right: 0px solid lightgrey; padding: 0px;">
	
	<!-- Initial implementation of the product category --> 
	<h2>Browse by Category</h2>
	<div>      
    	<ul>
			<g:if test="${categoryInstance.parentCategory}">
       			<li>
       				<g:if test="${categoryInstance?.parentCategory?.name != 'ROOT' }">
						<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
						<g:link controller="inventory" action="browse" params="[categoryId: categoryInstance.parentCategory?.id ]">
							${categoryInstance.parentCategory.name }
						</g:link>
					</g:if>
					<li>
						<g:render template="dynamicMenuTree" model="[category:categoryInstance?.parentCategory, level: 0, selectedCategory: categoryInstance]"/>
					</li>
				</li>
			</g:if>
			<g:else>
				<li>
					<g:render template="dynamicMenuTree" model="[category:categoryInstance, level: 0, selectedCategory: categoryInstance]"/>
				</li>
			</g:else>
		</ul>
	</div>
	<h2>Browse by Site</h2>
	<div>      
		<ul>
			<g:each var="warehouse" in="${Warehouse.list() }">
				<li>
					<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
					<g:link controller="inventory" action="browse" params="[categoryId: categoryInstance?.id, warehouseId: warehouse.id ]">
						${warehouse?.name }
					</g:link>		
				</li>
						
			</g:each>
		</ul>
	</div>
	
</div>						
