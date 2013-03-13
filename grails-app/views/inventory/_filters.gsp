<div>
	<g:form method="GET" controller="inventory" action="browse">
		<div class="box">
			<table>
				<tr>
					<td>
                        <div style="float: right">
                            <g:link controller="inventory" action="browse" params="[categoryId:session?.rootCategory?.id,resetSearch:true]">
                                <warehouse:message code="inventory.resetSearch.label" default="Reset search"/>
                            </g:link>
                        </div>
						<h3><warehouse:message code="inventory.filterByProduct.label"/></h3>
					</td>
				</tr>				
				<tr>
					<td>
						<g:hiddenField name="max" value="${params.max?:10 }"/>
						<g:textField name="searchTerms" 
							value="${commandInstance.searchTerms}" placeholder="${warehouse.message(code:'inventory.searchTerms.label')}"
                            class="text medium" size="46"/>
					</td>
				</tr>
				<g:if test="${!commandInstance?.categoryInstance.categories.isEmpty()}">
					<tr>
						<td>							
							<select id="subcategoryId" name="subcategoryId" class="text">
								<option value=""><warehouse:message code="inventory.filterByCategory.label"/></option>
								<g:render template="../category/selectOptions" 
									model="[category:commandInstance?.categoryInstance, selected:commandInstance?.subcategoryInstance, level: 0]"/>								
							</select>
						</td>
					</tr>					
				</g:if>
				<g:else>				
					<tr>
						<td>
							<format:category category="${commandInstance?.categoryInstance }"/>
						</td>
					</tr>
				</g:else>
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
            <table>
				<tr>
					<td>					
                        <h3><warehouse:message code="inventory.browseByTag.label"/></h3>
					</td>
				</tr>	
				<tr class="">
					<td>
						<g:if test="${tags }">			
							<g:each in="${tags }" var="tag" status="status">
								<g:set var="selectedTag" value="${params.tag == tag.key.tag }"/>								
								<g:link controller="inventory" action="browse" params="['tag':tag.key.tag,'max':params.max]">
									<span class="tag ${selectedTag?'selected':'' }">
										${tag?.key?.tag } (${tag?.key?.products?.size() })
										<%-- (${tag.value })--%>
									</span>
								</g:link>
							</g:each>
						</g:if>
						<g:else>
							<span class="fade">
								${warehouse.message(code: 'default.none.label') }
							</span>
						</g:else>	
					</td>
				</tr>
            </table>
        </div>
        <div class="box">
            <table>
				<tr>
					<td>					
                        <h3><warehouse:message code="inventory.browseByCategory.label"/></h3>
                    </td>
				</tr>	
				<g:each var="category" in="${commandInstance?.categoryInstance?.categories}">
					<tr>
						<td>
							<img src="${resource(dir: 'images/icons', file: 'indent.gif')}" class="middle"/>
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


