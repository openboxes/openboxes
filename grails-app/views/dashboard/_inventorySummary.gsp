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
							<span>
								<label><warehouse:message code="inventory.filterBy.keyword"/>:</label>
								<g:textField name="searchTerms" value="" size="40" value="${params.searchTerms }"/>						
								
							</span>
							<g:hiddenField name="resetSearch" value="true"/>							
							<g:hiddenField name="category.id" value="${rootCategory.id }"/>							
							<span style="padding-left:10px;">
								<button type="submit" class="" name="searchPerformed" value="true">
									<img src="${createLinkTo(dir: 'images/icons/silk', file: 'find.png' )}" class="middle"/>
									&nbsp;<warehouse:message code="default.find.label"/>&nbsp;
								</button>
							</span>
						</div>
					</div>
				</g:form>
			</div>
			 			
 			
		</div>
	</div>
</div>	