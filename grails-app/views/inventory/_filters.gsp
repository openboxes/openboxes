<div style="padding-top:0px;">
	<g:form method="POST" controller="inventory" action="browse">

		<table style="width:100%; border-collapse: collapse; border-color: black;">
			<tr>
				<g:each var="quickCategory" in="${quickCategories}">
					<td class="filterTab filterRow paddingRow"></td>
					<td class="<g:if test="${commandInstance?.categoryInstance == quickCategory}">filterSelected </g:if>filterTab filterRow">
						<a href="?categoryId=${quickCategory?.id}&resetSearch=true">
							<format:category category="${quickCategory}"/>
						</a>
						<g:if test="${commandInstance?.categoryInstance == quickCategory}">
							<g:render template="categoryActions" model="[commandInstance:commandInstance]"/>
						</g:if>
					</td>		
				</g:each>
				<td class="filterTab filterRow paddingRow" style="width:100%">&nbsp;</td>
			</tr>
		</table>
		
		<div style="padding-top:5px; margin:5px;"/>
			<div>
				<g:if test="${!commandInstance?.categoryInstance.categories.isEmpty()}">
					<span>
						<%--
						<warehouse:message code="inventory.filterBy.label"/>:
						&nbsp;&nbsp;
						 --%>
						<warehouse:message code="inventory.filterBy.category"/>
						<select id="subcategoryId" name="subcategoryId" >
							<option value=""></option>
							<g:render template="../category/selectOptions" model="[category:commandInstance?.categoryInstance, selected:commandInstance?.subcategoryInstance, level: 0]"/>								
						</select>							
					</span>
				</g:if>
				<span>
					<warehouse:message code="inventory.filterBy.keyword"/>:
					<g:textField name="searchTerms" value="${commandInstance.searchTerms}" size="24"/>						
				</span>
				<span style="padding-left:10px;">
					<button type="submit" class="" name="searchPerformed" value="true">
						<img src="${createLinkTo(dir: 'images/icons/silk', file: 'find.png' )}" class="middle"/>
						&nbsp;<warehouse:message code="default.find.label"/>&nbsp;
					</button>
				</span>
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
