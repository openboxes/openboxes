<span class="action-menu">
	<span class="action-btn" style="">
		<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
	</span>
	<div class="actions">
		<div class="action-menu-item">																			
			<g:link controller="report" action="showTransactionReport" params="['location.id':session?.warehouse?.id,'category.id':commandInstance?.categoryInstance?.id]" style="margin: 0px;">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'report.png')}" alt="${warehouse.message(code: 'report.showTransactionReport.label') }" style="vertical-align: middle"/>
				&nbsp;<warehouse:message code="report.showTransactionReport.label"/>
			</g:link>
		</div>		
	</div>
</span>