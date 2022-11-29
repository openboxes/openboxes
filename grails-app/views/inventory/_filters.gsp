<div class="filters" >
	<g:form method="GET" controller="inventory" action="browse">
		<g:hiddenField name="max" value="${params.max?:10 }"/>
		<g:hiddenField name="location.id" value="${session.warehouse.id}"/>
		<div class="box">
			<h2><warehouse:message code="inventory.filterByProduct.label"/></h2>
			<div class="filter-list-item">
				<g:textField name="searchTerms"
							 value="${commandInstance.searchTerms}"
							 placeholder="${warehouse.message(code:'inventory.searchTerms.label')}"
							 class="text medium" style="width:100%;"/>
			</div>
			<div class="filter-list-item">
				<g:selectCategoryViaAjax id="category"
										 name="categoryId"
										 class="ajaxSelect2"
										 noSelection="['':'']"
										 data-placeholder="Select a category"
										 value="${commandInstance?.category?.id}"
										 data-allow-clear="true"
										 data-ajax--url="${request.contextPath }/json/findCategory"
										 data-ajax--cache="true"/>
			</div>
			<div class="filter-list-item">
				<g:selectTagsViaAjax id="tags"
										 name="tags"
										 class="ajaxSelect2"
										 noSelection="['':'']"
										 multiple="true"
					                     data-placeholder="Select a tag"
										 value="${commandInstance?.tags*.id}"
										 data-allow-clear="true"
										 data-ajax--url="${request.contextPath }/json/findTagsByName"
										 data-ajax--cache="true"/>
			</div>
			<div class="filter-list-item">
				<g:selectCatalogsViaAjax id="catalogs"
										 name="catalogs"
										 class="ajaxSelect2"
										 noSelection="['':'']"
										 data-placeholder="Select a catalog"
										 multiple="true"
										 value="${commandInstance?.catalogs*.id}"
										 data-allow-clear="true"
										 data-ajax--url="${request.contextPath }/json/findCatalogs"
										 data-ajax--cache="true"/>
			</div>
			<div class="buttons">
				<button type="submit" class="button icon search" name="searchPerformed" value="true">
					<warehouse:message code="default.search.label"/>
				</button>
				&nbsp;
				<g:link controller="inventory" action="browse" params="[resetSearch:true]" class="button icon reload">
					<warehouse:message code="default.button.reset.label" default="Reset"/>
				</g:link>
			</div>
		</div>
    </g:form>
</div>
