<div style="padding-top:0px;">
	<g:form method="GET" controller="inventory" action="browse">
		<div class="box">
			<div>
				<table style="width: 10%">
					<tr>
						<td>
							<label><warehouse:message code="inventory.filterBy.keyword"/></label>
						
						</td>
						<g:if test="${!commandInstance?.categoryInstance.categories.isEmpty()}">
							<td>
								<label><warehouse:message code="inventory.withinCategory.label"/></label>						
							</td>					
						</g:if>
					</tr>
					<tr>
						<td>
							<g:textField name="searchTerms" value="${commandInstance.searchTerms}" size="40"/>
						</td>
						<g:if test="${!commandInstance?.categoryInstance.categories.isEmpty()}">
							<td>
								<select id="subcategoryId" name="subcategoryId" >
									<option value=""></option>
									<g:render template="../category/selectOptions" model="[category:commandInstance?.categoryInstance, selected:commandInstance?.subcategoryInstance, level: 0]"/>								
								</select>
							</td>					
						</g:if>
						<td nowrap="nowrap">
							<button type="submit" class="" name="searchPerformed" value="true">
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'find.png' )}" class="middle"/>
								&nbsp;<warehouse:message code="default.find.label"/>&nbsp;
							</button>
						</td>
					</tr>
					
				</table>
			</div>
			<div id="searchOptions">
				<span style="padding-left:10px;">
					<g:checkBox name="showHiddenProducts" value="${commandInstance.showHiddenProducts}"/>	
					<warehouse:message code="inventory.showHiddenProducts.label"/>					
				</span>

				<%-- 			
				<span style="padding-left:10px;">
					<g:checkBox name="showUnsupportedProducts" value="${commandInstance.showUnsupportedProducts}" size="24"/>	
					<warehouse:message code="inventory.showUnsupportedProducts.label"/>					
				</span>
				<span style="padding-left:10px;">
					<g:checkBox name="showNonInventoryProducts" value="${commandInstance.showNonInventoryProducts}" size="24"/>	
					<warehouse:message code="inventory.showNonInventoryProducts.label"/>					
				</span>
				--%>
				<span style="padding-left:10px;">
					<g:checkBox name="showOutOfStockProducts" value="${commandInstance.showOutOfStockProducts}" size="24"/>	
					<warehouse:message code="inventory.showOutOfStockProducts.label"/>					
				</span>
			</div>
		</div>
	</g:form>
</div>
