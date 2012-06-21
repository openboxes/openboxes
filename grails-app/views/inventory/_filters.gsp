<div style="padding-top:0px;">
	<g:form method="GET" controller="inventory" action="browse">
		<div class="box">
			<div>
				<table style="width: 10%">
					<tr>
						<td>
							<label><warehouse:message code="inventory.filterBy.keyword"/></label>
						
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
					<tr>
						<td>
							<g:textField name="searchTerms" value="${commandInstance.searchTerms}" class="text medium" size="40"/>
						</td>
					</tr>
					<tr>
						<td>
							<div>
								<g:checkBox name="showHiddenProducts" value="${commandInstance.showHiddenProducts}"/>	
								<warehouse:message code="inventory.showHiddenProducts.label"/>					
							</div>
							<div>
								<g:checkBox name="showOutOfStockProducts" value="${commandInstance.showOutOfStockProducts}" size="24"/>	
								<warehouse:message code="inventory.showOutOfStockProducts.label"/>
							</div>					
						</td>				
					</tr>
					<tr>
						<td class="right">
							<button type="submit" class="" name="searchPerformed" value="true">
								<img src="${createLinkTo(dir: 'images/icons/silk', file: 'find.png' )}" class="middle"/>
								&nbsp;<warehouse:message code="default.find.label"/>&nbsp;
							</button>
						</td>
					</tr>
				</table>
			</div>
			
		</div>
	</g:form>
</div>
