<g:if test="${requestInstance?.id }">
	<div class="actions" style="min-width: 200px;">
		<g:if test="${!request.request.requestURL.toString().contains('request/list')}">
			<div class="action-menu-item">
				<g:link controller="request" action="list">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}" alt="View requests" style="vertical-align: middle" />
					&nbsp;${warehouse.message(code: 'request.view.label', default: 'View requests')} 
				</g:link>
			</div>
			<div class="action-menu-item">
				<hr/>
			</div>
		</g:if>
		<div class="action-menu-item">
			<g:link controller="request" action="show" id="${requestInstance?.id}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" />
				&nbsp;${warehouse.message(code: 'request.show.label', default: 'Show request details')} 
			</g:link>		
		</div>
		<div class="action-menu-item">
			<g:link controller="createRequestWorkflow" action="createRequest" event="pickRequestItems" id="${requestInstance?.id}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" />
				&nbsp;${warehouse.message(code: 'request.edit.label', default: 'Edit request details')} 
			</g:link>		
		</div>
		<div class="action-menu-item">
			<g:link controller="request" action="addComment" id="${requestInstance?.id}">
				<img src="${resource(dir: 'images/icons/silk', file: 'comment_add.png')}" />
				&nbsp;${warehouse.message(code: 'request.addComment.label', default: 'Add comment')} 
			</g:link>				
		</div>		
		<div class="action-menu-item">
			<g:link controller="request" action="addDocument" id="${requestInstance?.id}">
				<img src="${resource(dir: 'images/icons/silk', file: 'page_add.png')}" />
				&nbsp;${warehouse.message(code: 'request.addDocument.label', default: 'Add document')} 
			</g:link>				
		</div>				
		<g:if test="${session?.warehouse?.id == requestInstance?.destination?.id }">
		
			<div class="action-menu-item">
				<g:link controller="request" action="place" id="${requestInstance?.id}">
					<img src="${resource(dir: 'images/icons/silk', file: 'cart_go.png')}" />
					&nbsp;${warehouse.message(code: 'request.submit.label', default: 'Submit request')} 
				</g:link>				
			</div>		
			<div class="action-menu-item">
				<g:link controller="request" action="delete" id="${requestInstance?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
					<img src="${resource(dir: 'images/icons/silk', file: 'bin.png')}" />
					&nbsp;${warehouse.message(code: 'request.delete.label', default: 'Delete request')} 
				</g:link>				
			</div>
		</g:if>		
	</div>
</g:if>