<span class="action-menu">
	<button class="action-btn">
		<img
			src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}"
			style="vertical-align: middle" />
	</button>
	<div class="actions">
		<g:if test="${params.action!='list'}">
			<div class="action-menu-item">
	       		<g:link class="list" action="list">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'table.png')}" class="middle"/>&nbsp;
	       			<warehouse:message code="default.list.label" args="[warehouse.message(code:'locations.label').toLowerCase()]"/>
	       		</g:link>
			</div>
			<div class="action-menu-item">
				<hr />
			</div>
		</g:if>
		<g:if test="${params.action!='show'}">
			<div class="action-menu-item">
				<g:link class="edit" action="show" id="${locationInstance?.id}">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'building.png')}" class="middle"/>&nbsp;
					${warehouse.message(code: 'default.show.label', args: [warehouse.message(code:'location.label')])}
				</g:link>
			</div>
		</g:if>
		<g:if test="${params.action!='edit'}">
			<div class="action-menu-item">
				<g:link class="edit" action="edit" id="${locationInstance?.id}">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" class="middle"/>&nbsp;
					${warehouse.message(code: 'default.edit.label', args: [warehouse.message(code:'location.label')])}
				</g:link>
			</div>
		</g:if>
		<div class="action-menu-item">
			<g:link controller="location" action="uploadLogo" id="${locationInstance?.id }">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'photo_add.png')}" class="middle"/>&nbsp;
				<warehouse:message code="location.uploadLogo.label" />
			</g:link>
		</div>
		<div class="action-menu-item">
			<g:link class="delete" action="delete" id="${locationInstance?.id}"
				onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" class="middle"/>&nbsp;
				${warehouse.message(code: 'default.delete.label', args: [warehouse.message(code:'location.label')])}
			</g:link>
		</div>
	</div>
</span>