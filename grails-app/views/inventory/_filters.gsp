<%-- 
<div>
	<span>		
		<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" />
	</span>
	<div class="actions" style="max-height: 300px; overflow: auto;">
		<div style="padding: 10px;">
			<b>Choose a category to narrow products displayed</b>
		</div>
		<g:each var="category" in="${productMap?.keySet().sort() }">
			<g:if test="${category?.id }">
				<div class="action-menu-item">
					<img src="${createLinkTo(dir: 'images/icons/silk', file: 'folder.png' )}"/>&nbsp;
					<g:link action="narrowCategoryFilter" params="[categoryId:category?.id]">
						${category?.name }
					</g:link>
					&nbsp;
					<span class="fade">
						<g:render template="../category/breadcrumb" model="[categoryInstance:category]"/>	
					</span>
				</div>
			</g:if>
		</g:each>
	</div>
	<span>
        <g:if test="${commandInstance?.categoryFilters || commandInstance?.searchTermFilters}">	
			&nbsp;<g:link action="clearAllFilters">Reset</g:link>							
		</g:if>
	</span>
--%>
					
<div>
	<table>
		<tr>
			<td style="padding: 0; margin: 0;">
				<warehouse:message code="inventory.filters.label"/>: &nbsp;

				<g:if test="${commandInstance?.categoryFilters || commandInstance?.searchTermFilters}">			
					<g:each var="filter" in="${commandInstance?.categoryFilters }">
						<span class="filter">
							<%--<img src="${createLinkTo(dir: 'images/icons/silk', file: 'folder.png' )}"/>&nbsp; --%>
							<g:link action="removeCategoryFilter" params="[categoryId:filter.id]">
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'delete.png' )}" style="vertical-align:middle"/>
							</g:link>
							<format:category category="${filter}"/>
						</span>
					</g:each>
					<g:each var="filter" in="${commandInstance?.searchTermFilters }">
						<span class="filter">	
							<%-- <img src="${createLinkTo(dir: 'images/icons/silk', file: 'zoom.png' )}" class=""/>--%>
							<g:link action="removeSearchTerm" params="[searchTerm:filter]">
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'delete.png' )}" style=""/>
							</g:link>
							${filter }
						</span>
					</g:each>
					<span class="clear-all">
						<g:link action="clearAllFilters">
							<warehouse:message code="inventory.showAll.label"/>
						</g:link>	
					</span>
				</g:if>
				<g:else>
					<span class="fade"><warehouse:message code="inventory.noFilters.label"/></span>
				</g:else>
			</td>
		</tr>
	</table>
</div>			