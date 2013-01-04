<div class="widget-large">
	<div class="widget-header">
		<h2><warehouse:message code="inventory.label" args="[session.warehouse.name]"/></h2>
	</div>	    			
	<div class="widget-content">	    					    			
		<div id="inventorysummary">	
			<div style="padding-top:0px;">
				<g:form method="GET" controller="inventory" action="browse">
					<div>
						<g:textField id="dashboardSearchBox" name="searchTerms" style="width: 60%" value="${params.searchTerms }" 
							class="globalSearch"/>						
						<g:hiddenField name="resetSearch" value="true"/>							
						<g:hiddenField name="categoryId" value="${rootCategory.id }"/>							
						<g:hiddenField name="showHiddenProducts" value="on"/>
						<g:hiddenField name="showOutOfStockProducts" value="on"/>
						
						<button type="submit" class="" name="searchPerformed" value="true">
							<img src="${createLinkTo(dir: 'images/icons/silk', file: 'find.png' )}" class="middle"/>
							&nbsp;<warehouse:message code="default.find.label"/>&nbsp;
						</button>
							
					</div>
				</g:form>
			</div>
		</div>
	</div>
</div>
	<script>
		$(document).ready(function() {
			$("#dashboardSearchBox").watermark("${warehouse.message(code:'inventory.filterByKeyword.label')}");
		});
	</script>
	