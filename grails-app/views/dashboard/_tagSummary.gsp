<div class="widget-large">
	<div class="widget-header">
		<h2><warehouse:message code="tags.label" default="Tags"/></h2>
	</div>	    			
	<div class="widget-content" style="padding:0; margin:0">	    					    			
		<div id="tagSummary">	
			<g:each in="${org.pih.warehouse.core.Tag.list() }" var="tag">
				<g:link controller="inventory" action="browse" params="['tag':tag.tag]">
					<span class="tag">${tag.tag } (${tag?.products?.size() })</span>
				</g:link>
			</g:each>
		</div>
		<div class="clear"></div>
	</div>
</div>