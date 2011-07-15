<div>
	<g:if test="${commandInstance}">
		<table>
			<tbody>			
				<tr>
					<td>
						<span class="title">${commandInstance?.warehouseInstance?.name}</span>
						<div class="fade" style="font-size: 0.9em; line-height: 20px;">
							<span class="action-menu">
								<button class="action-btn">
									<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle;"/>
									<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle;"/>
								</button>
								<div class="actions">
									<g:render template="browseInventoryMenuItems" model="[commandInstance: commandInstance]"/>																				
								</div>
							</span>		
							|
							<span>
								<g:set var="productMap" value="${commandInstance?.productList.groupBy {it.category} }"/>
								Showing ${commandInstance?.productList?.size() } product(s) in ${productMap?.keySet()?.size()} categories
							</span>
						</div>
					</td>	
										
					<td style="text-align: right;">
						<div id="searchTerms" style="margin: 5px;">
							<%-- 
							<g:form method="GET" controller="inventory" action="browse" style="display: inline;">
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'zoom.png' )}" class="middle"/>
								<g:globalSearch id="searchable" name="searchTerms" 
									jsonUrl="/warehouse/json/searchAll" />

							</g:form>	
							--%>
							<g:form method="GET" controller="inventory" action="browse" style="display: inline;">
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'find.png' )}" class="middle"/>				
								<g:textField name="searchTerms" value="" size="24"/>
								<button type="submit" class="" name="submitSearch">
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'zoom.png' )}" class="middle"/>
									&nbsp;Find&nbsp;</button>
							</g:form>	
						</div>
						<div style="margin: 5px;">
							<g:form action="addCategoryFilter">
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'folder.png' )}" class="middle"/>
								<select id="categoryFilter" name="categoryId" >
									<option value="">Filter by category</option>
									<g:render template="../category/selectOptions" model="[category:commandInstance?.rootCategory, selected:null, level: 0]"/>								
								</select>
							</g:form>								
						</div>
						<%-- Added to the action menu (for now)
						<div style="margin: 2px;">
							<g:if test="${session?.showHiddenProducts }">
								<g:link action="showHiddenProducts">Hide hidden products</g:link>					
							</g:if> 
							<g:else>
								<g:link action="showHiddenProducts">Show hidden products</g:link>					
							</g:else>
						</div>
						--%>
					</td>
				</tr>
			</tbody>
		</table>			
	</g:if>
</div>
	

<script>
	// 
	$(function() {
		$("#categoryFilter").change(function () { 
			$(this).closest("form").submit();
		});
	});
</script>


