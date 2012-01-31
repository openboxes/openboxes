<div style="padding-top:0px;">
	<g:form method="GET" controller="inventory" action="browse">
		<div class="box">
			<div>
				<span>
					<label><warehouse:message code="inventory.filterBy.keyword"/>:</label>
					<g:textField name="searchTerms" value="${commandInstance.searchTerms}" size="40"/>						
				</span>
				<g:if test="${!commandInstance?.categoryInstance.categories.isEmpty()}">
					<span>
						<label>
							<warehouse:message code="inventory.withinCategory.label"/>
						</label>							
						<select id="subcategoryId" name="subcategoryId" >
							<option value=""></option>
							<g:render template="../category/selectOptions" model="[category:commandInstance?.categoryInstance, selected:commandInstance?.subcategoryInstance, level: 0]"/>								
						</select>
					</span>
				</g:if>
				<span style="padding-left:10px;">
					<button type="submit" class="" name="searchPerformed" value="true">
						<img src="${createLinkTo(dir: 'images/icons/silk', file: 'find.png' )}" class="middle"/>
						&nbsp;<warehouse:message code="default.find.label"/>&nbsp;
					</button>
				</span>
			</div>
			<div>
				<span style="padding-left:10px;">
					<g:checkBox name="showUnsupportedProducts" value="${commandInstance.showUnsupportedProducts}" size="24"/>	
					<warehouse:message code="inventory.showUnsupportedProducts.label"/>					
				</span>
				<span style="padding-left:10px;">
					<g:checkBox name="showNonInventoryProducts" value="${commandInstance.showNonInventoryProducts}" size="24"/>	
					<warehouse:message code="inventory.showNonInventoryProducts.label"/>					
				</span>
				<span style="padding-left:10px;">
					<g:checkBox name="showOutOfStockProducts" value="${commandInstance.showOutOfStockProducts}" size="24"/>	
					<warehouse:message code="inventory.showOutOfStockProducts.label"/>					
				</span>
			</div>
		</div>
	</g:form>
</div>
