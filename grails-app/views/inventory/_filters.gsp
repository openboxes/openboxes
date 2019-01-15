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
                <%--
                <tr>
                    <td>
                        <div>
                            <g:checkBox id="showHiddenProducts" name="showHiddenProducts" value="${commandInstance.showHiddenProducts}"/>
                            <label for="showHiddenProducts"><g:message code="inventory.showHiddenProducts.label"/></label>
                        </div>
                        <div>
                            <g:checkBox name="showOutOfStockProducts" value="${commandInstance.showOutOfStockProducts}" size="24"/>
                            <g:message code="inventory.showOutOfStockProducts.label"/>
                        </div>
					</td>
				</tr>
                --%>
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

	    <%--
        <div class="box">
            <h2><warehouse:message code="inventory.browseByTag.label"/></h2>
            <table>
                <tr class="">
                    <td class="middle">
                        <g:if test="${tags }">
                            <div id="tagcloud" style="max-height: 200px; overflow: auto;" >
                                <g:each in="${tags }" var="tag" status="status">
                                    <g:set var="selectedTag" value="${params.tag == tag.key.tag }"/>
                                    <span class="${selectedTag?'selected':'' }">
                                        <g:link controller="inventory" action="browse" params="['tag':tag.key.tag,'max':params.max]" rel="${tag?.key?.products?.size() }">
                                            ${tag?.key?.tag } (${tag?.key?.products?.size() })</g:link>
                                    </span>
                                </g:each>
                            </div>
                        </g:if>
                        <g:else>
                            <div class="empty middle center">
                                ${warehouse.message(code: 'tags.empty.label', default:'No public tags') }
                            </div>
                        </g:else>
                    </td>
                </tr>
            </table>
        </div>
		<div class="box">
			<h2><warehouse:message code="inventory.browseByCategory.label"/></h2>

            <table>
                <g:each var="category" in="${commandInstance?.categoryInstance?.categories}">
                    <tr class="prop">
                        <td class="middle">
                            <g:link controller="inventory" action="browse" params="['subcategoryId':category?.id,'max':params.max,'searchPerformed':true,'showHiddenProducts':params.showHiddenProducts?:'on','showOutOfStockProducts':params.showOutOfStockProducts?:'on']">
                                ${category?.name } (${category?.products?.size() })
                            </g:link>
                        </td>
                    </tr>
                </g:each>
            </table>
        </div>
        --%>

    </g:form>
</div>
