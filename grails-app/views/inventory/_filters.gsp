<div>
	<g:form method="GET" controller="inventory" action="browse">
		<div class="box">
            <div style="float: right; vertical-align:middle">
                <g:link controller="inventory" action="browse" params="[categoryId:session?.rootCategory?.id,resetSearch:true]">
                    <warehouse:message code="inventoryBrowser.resetAll.label" default="Reset all"/>
                </g:link>
            </div>
            <h2><warehouse:message code="inventory.filterByProduct.label"/></h2>
			<table>
				<tr>
					<td>
						<g:hiddenField name="max" value="${params.max?:10 }"/>
						<g:textField name="searchTerms" 
							value="${commandInstance.searchTerms}" placeholder="${warehouse.message(code:'inventory.searchTerms.label')}"
                            class="text medium" size="45"/>
					</td>
				</tr>
                <tr>
                    <td>
                        <g:selectCategory_v2 id="subcategoryId" name="subcategoryId" class="chzn-select"
                            style="width:100%;"
                            value="${commandInstance?.subcategoryInstance?.id}"/>
                    </td>
                </tr>
				<tr>
					<td>
						<div >
							<div>
								<g:checkBox name="showHiddenProducts" value="${commandInstance.showHiddenProducts}"/>
								<warehouse:message code="inventory.showHiddenProducts.label"/>
							</div>
							<div>
								<g:checkBox name="showOutOfStockProducts" value="${commandInstance.showOutOfStockProducts}" size="24"/>
								<warehouse:message code="inventory.showOutOfStockProducts.label"/>

							</div>					
						</div>
                    </td>
                </tr>
                <tr>
                    <td>
						<div class="left">
							<button type="submit" class="button icon search" name="searchPerformed" value="true">
								<warehouse:message code="default.search.label"/>
							</button>
						</div>
					</td>
				</tr>
			</table>
        </div>
        <div class="box">
            <h2><warehouse:message code="inventory.browseByTag.label"/></h2>
            <table>
				<tr class="">
					<td class="middle">
						<g:if test="${tags }">
                            <div id="tagcloud">
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
								${warehouse.message(code: 'tags.empty.label', default:'There are no public tags') }
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
				
				
				<%-- 	
				<tr class="prop">
					<td>
						<label><warehouse:message code="inventory.filterKey.label"/></label>	
						<div style="padding: 10px;" class="buttonsBar">						
							<div class="linkButton">
								<a href="?status=${org.pih.warehouse.inventory.InventoryStatus.SUPPORTED}" class="supported">								
									<warehouse:message code="enum.InventoryStatus.SUPPORTED"/>
								</a>
							</div>
							<div class="linkButton">
								<a href="?status=${org.pih.warehouse.inventory.InventoryStatus.FORMULARY}" class="formulary">									
									<warehouse:message code="enum.InventoryStatus.FORMULARY"/>																	
								</a>
							</div>
							<div class="linkButton">
								<a href="?status=${org.pih.warehouse.inventory.InventoryStatus.STOCK}" class="stocked">									
									<warehouse:message code="enum.InventoryStatus.STOCK"/>																	
								</a>
							</div>
							<div class="linkButton">
								<a href="?status=${org.pih.warehouse.inventory.InventoryStatus.NOT_SUPPORTED}" class="notSupported">									
									<warehouse:message code="enum.InventoryStatus.NOT_SUPPORTED"/>																	
								</a>
							</div>
						</div>
					</td>
				</tr>
				--%>
			</table>
		</div>
	</g:form>
</div>


