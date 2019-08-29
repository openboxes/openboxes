<div class="filters" >
	<g:form method="GET" controller="inventory" action="browse">
		<div class="box">
			<h2><warehouse:message code="inventory.filterByProduct.label"/></h2>
			<table>
				<tr>
					<td>
						<g:hiddenField name="max" value="${params.max?:10 }"/>
						<g:textField name="searchTerms"
									 value="${commandInstance.searchTerms}"
									 placeholder="${warehouse.message(code:'inventory.searchTerms.label')}"
									 class="text medium" style="width:100%;"/>
					</td>
				</tr>
				<tr>
					<td>
                        <g:selectCategory id="subcategoryId"
                                          name="subcategoryId"
                                          class="chzn-select-deselect"
                                          noSelection="['null':'']"
                                          data-placeholder="Select a category"
                                          value="${commandInstance?.subcategoryInstance?.id}"/>
					</td>
				</tr>
				<tr>
					<td>
						<g:selectTags name="tags" noSelection="['':'']"
									  value="${commandInstance.tags}"
                                      data-placeholder="Select a tag"
									  class="chzn-select-deselect"/>
					</td>
				</tr>
				<tr>
					<td>
						<g:selectCatalogs name="catalogs" noSelection="['':'']"
									  value="${commandInstance?.catalogs}"
									  data-placeholder="Select a catalog"
									  class="chzn-select-deselect"/>
					</td>
				</tr>
				<tr>
					<td>
						<div class="left buttons">
							<button type="submit" class="button icon search" name="searchPerformed" value="true">
								<warehouse:message code="default.search.label"/>
							</button>
                            &nbsp;
							<g:link controller="inventory" action="browse" params="[categoryId:session?.rootCategory?.id,resetSearch:true]" >
								<warehouse:message code="inventoryBrowser.resetAll.label" default="Reset all"/>
							</g:link>
						</div>
					</td>
				</tr>
			</table>
		</div>
    </g:form>
</div>
