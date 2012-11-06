<div style="padding-top:0px;">
	<g:form method="GET" controller="inventory" action="browse">
		<div class="box">
			<table style="width: 25%">
				<tr>
					<td>					
						<label><warehouse:message code="inventory.filterByProduct.label"/></label>
						<g:link controller="inventory" action="browse" params="[categoryId:session?.rootCategory?.id,resetSearch:true]">reset</g:link>
					</td>
				</tr>				
			
				<g:if test="${!commandInstance?.categoryInstance.categories.isEmpty()}">
					<tr>
						<td>							
							<select id="subcategoryId" name="subcategoryId" class="text">
								<option value=""></option>
								<g:render template="../category/selectOptions" model="[category:commandInstance?.categoryInstance, selected:commandInstance?.subcategoryInstance, level: 0]"/>								
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
						<g:textField name="searchTerms" value="${commandInstance.searchTerms}" placeholder="${warehouse.message(code:'inventory.searchTerms.label')}" class="text medium" size="40"/>
						
						<div style="padding: 10px;">
							<div>
								<g:checkBox name="showHiddenProducts" value="${commandInstance.showHiddenProducts}"/>	
								<warehouse:message code="inventory.showHiddenProducts.label"/>					
							</div>
							<div>
								<g:checkBox name="showOutOfStockProducts" value="${commandInstance.showOutOfStockProducts}" size="24"/>	
								<warehouse:message code="inventory.showOutOfStockProducts.label"/>
							</div>					
						</div>
						<div class="right">
							<button type="submit" class="" name="searchPerformed" value="true">
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'find.png' )}" class="middle"/>
								&nbsp;<warehouse:message code="default.find.label"/>&nbsp;
							</button>
						</div>
					</td>
				</tr>			
				<tr class="prop">
					<td>					
						<label><warehouse:message code="inventory.filterByTag.label"/></label>
						<%-- 
						<g:link controller="inventory" action="browse" params="[categoryId:session?.rootCategory?.id,resetSearch:true]">reset</g:link>
						--%>
					</td>
				</tr>			
				<tr>
					<td>
						<div class="tags">
							<g:each in="${tags }" var="tag">
								<span class="tag">
									<g:link controller="inventory" action="browse" params="['tag':tag]">${tag }</g:link>
								</span>
							</g:each>
						</div>
					</td>
				</tr>
				
				<tr class="prop">
					<td>
						<label><warehouse:message code="inventory.filterKey.label"/></label>	
						<div style="padding: 10px;">						
							<div>
								<img src="${createLinkTo(dir:'images/icons/silk',file:'flag_green.png')}" alt="${warehouse.message(code: 'inventory.markAsSupported.label') }" style="vertical-align: middle"/>
								&nbsp;<warehouse:message code="enum.InventoryStatus.SUPPORTED"/>
							</div>
							<div>
		
								<img src="${createLinkTo(dir:'images/icons/silk',file:'flag_orange.png')}" alt="${warehouse.message(code: 'inventory.markAsNonInventoried.label') }" style="vertical-align: middle"/>
								&nbsp;<warehouse:message code="enum.InventoryStatus.SUPPORTED_NON_INVENTORY"/>
							</div>
							<div>
							
								<img src="${createLinkTo(dir:'images/icons/silk',file:'flag_red.png')}" alt="${warehouse.message(code: 'inventory.markAsNotSupported.label') }" style="vertical-align: middle"/>
								&nbsp;<warehouse:message code="enum.InventoryStatus.NOT_SUPPORTED"/>																	
							</div>
						</div>
					</td>
				</tr>
			</table>
		</div>
	</g:form>
</div>


