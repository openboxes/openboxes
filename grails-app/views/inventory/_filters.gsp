<div class="filters" >
	<g:form method="GET" controller="inventory" action="browse">
		<g:hiddenField name="max" value="${params.max?:10 }"/>
		<div class="box">
			<h2><warehouse:message code="inventory.filterByProduct.label"/></h2>
			<div class="filter-list-item">
				<g:textField name="searchTerms"
							 value="${commandInstance.searchTerms}"
							 placeholder="${warehouse.message(code:'inventory.searchTerms.label')}"
							 class="text medium" style="width:100%;"/>
			</div>
			<div class="filter-list-item">
				<g:selectCategory id="category"
								  name="categoryId"
								  class="chzn-select-deselect"
								  noSelection="['':'']"
								  data-placeholder="Select a category"
								  value="${commandInstance?.category?.id}"/>
			</div>
			<div class="filter-list-item">
				<g:selectTags name="tags" noSelection="['':'']"
							  value="${commandInstance.tags*.id}"
							  data-placeholder="Select a tag"
							  class="chzn-select-deselect"/>
			</div>
			<div class="filter-list-item">
				<g:selectCatalogs name="catalogs" noSelection="['':'']"
							  value="${commandInstance?.catalogs*.id}"
							  data-placeholder="Select a catalog"
							  class="chzn-select-deselect"/>
			</div>
			<div class="filter-list-item">
				<g:selectProductType
								  name="productTypes"
								  class="chzn-select-deselect"
								  noSelection="['':'']"
								  data-placeholder="Select a product type"
								  value="${commandInstance.productTypes*.id}"/>
			</div>
			<div class="filter-list-item">
				<g:checkBox name="showOutOfStockProducts" value="${commandInstance.showOutOfStockProducts}"/>
				<label for="showOutOfStockProducts">${warehouse.message(code:'inventory.showOutOfStockProducts.label')}</label>
			</div>
			<div class="buttons">
				<button type="submit" class="button icon search" name="searchPerformed" value="true">
					<warehouse:message code="default.search.label"/>
				</button>
				&nbsp;
				<g:link controller="inventory" action="browse" params="[resetSearch:true]" class="button">
					<img src="${resource(dir:'images/icons/silk',file:'reload.png')}" />
					<warehouse:message code="default.button.reset.label" default="Reset"/>
				</g:link>
			</div>
		</div>
    </g:form>
</div>
