<g:if test="${requisition?.id }">
	<div class="actions" style="min-width: 200px;">
		<g:if test="${!request.request.requestURL.toString().contains('requisition/list')}">
			<div class="action-menu-item">
				<g:link controller="requisition" action="list">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}" alt="View requests" style="vertical-align: middle" />
					&nbsp;${warehouse.message(code: 'requisition.view.label', default: 'View requisitions')}
				</g:link>
			</div>
			<div class="action-menu-item">
				<hr/>
			</div>
		</g:if>
		<div class="action-menu-item">
			<g:link controller="requisition" action="show" id="${requisition?.id}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'zoom.png')}" />
				&nbsp;${warehouse.message(code: 'requisition.show.label', default: 'Show requisition details')}
			</g:link>		
		</div>
		<div class="action-menu-item">
			<g:link controller="requisition" action="edit" id="${requisition?.id}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" />
				&nbsp;${warehouse.message(code: 'requisition.edit.label', default: 'Edit requisition details')}
			</g:link>		
		</div>
		<div class="action-menu-item">
			<g:link controller="requisition" action="addComment" id="${requisition?.id}">
				<img src="${resource(dir: 'images/icons/silk', file: 'comment_add.png')}" />
				&nbsp;${warehouse.message(code: 'requisition.addComment.label', default: 'Add comment')}
			</g:link>				
		</div>		
		<div class="action-menu-item">
			<g:link controller="requisition" action="addDocument" id="${requisition?.id}">
				<img src="${resource(dir: 'images/icons/silk', file: 'page_add.png')}" />
				&nbsp;${warehouse.message(code: 'requisition.addDocument.label', default: 'Add document')}
			</g:link>				
		</div>				
		<g:if test="${session?.warehouse?.id == requisition?.destination?.id }">
			<div class="action-menu-item">
				<g:link controller="requisition" name="processRequisition" action="process" id="${requisition?.id}">
					<img src="${resource(dir: 'images/icons/silk', file: 'cart_go.png')}" />
					&nbsp;${warehouse.message(code: 'requisition.process.label', default: 'Process requisition')}
				</g:link>				
			</div>		
			<div class="action-menu-item">
				<g:link controller="requisition" action="cancel" id="${requisition?.id}" onclick="return confirm('${warehouse.message(code: 'default.button.cancel.confirm.message', default: 'Are you sure?')}');">
					<img src="${resource(dir: 'images/icons/silk', file: 'bin.png')}" />
					&nbsp;${warehouse.message(code: 'requisition.cancel.label', default: 'Cancel requisition')}
				</g:link>				
			</div>
		</g:if>		
	</div>
</g:if>