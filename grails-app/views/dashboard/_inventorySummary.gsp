<div class="widget-large">
	<div class="widget-header">
		<h2><warehouse:message code="inventory.label" args="[session.warehouse.name]"/></h2>
	</div>	    			
	<div class="widget-content">	    					    			
		<div id="inventorysummary">	
 			
 			
			<div style="padding-top:0px;">
				<g:form method="GET" controller="inventory" action="browse">
					<div class="">
						<div>
							<label><warehouse:message code="inventory.filterBy.keyword"/>:</label>
							<div>
								<g:textField name="searchTerms" size="60" value="${params.searchTerms }" class="text medium"/>						
								<g:hiddenField name="resetSearch" value="true"/>							
								<g:hiddenField name="category.id" value="${rootCategory.id }"/>							
								
									<button type="submit" class="" name="searchPerformed" value="true">
										<img src="${createLinkTo(dir: 'images/icons/silk', file: 'find.png' )}" class="middle"/>
										&nbsp;<warehouse:message code="default.find.label"/>&nbsp;
									</button>
								
							</div>
						</div>
					</div>
				</g:form>
			</div>
			 			
 			
		</div>
	</div>
</div>	