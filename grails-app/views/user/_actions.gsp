<span class="action-menu">
	<button class="action-btn">
		<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}"
			style="vertical-align: middle" /> <img
			src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}"
			style="vertical-align: middle" />
	</button>
	<div class="actions">
		<div class="action-menu-item">
			<g:link class="list" action="list">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'table.png')}" class="middle"/>&nbsp;
				${warehouse.message(code: 'user.list.label')}
			</g:link>
		</div>
		<div class="action-menu-item">
			<hr />
		</div>
		<div class="action-menu-item">
			<g:link class="edit" action="edit" id="${userInstance?.id}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'pencil.png')}" class="middle"/>&nbsp;
				${warehouse.message(code: 'default.button.edit.label')}
			</g:link>
		</div>
		<div class="action-menu-item">
			<g:link class="delete" action="delete" id="${userInstance?.id}"
				onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'delete.png')}" class="middle"/>&nbsp;
				${warehouse.message(code: 'default.button.delete.label')}
			</g:link>

		</div>
		<div class="action-menu-item">
			<g:link action="toggleActivation" id="${userInstance?.id}">
				<img src="${createLinkTo(dir:'images/icons/silk',file:'user_delete.png')}" class="middle"/>&nbsp;
				<g:if test="${userInstance?.active}">
					${warehouse.message(code: 'user.deactivate.label')}
				</g:if>
				<g:else>
					${warehouse.message(code: 'user.activate.label')}
				</g:else>
			</g:link>
		</div>
		<g:isInRole roles="[org.pih.warehouse.core.RoleType.ROLE_MANAGER]">
			<div class="action-menu-item">
				<g:link action="sendTestEmail" id="${userInstance?.id }">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'email.png')}" class="middle"/>&nbsp;
					<warehouse:message code="system.testEmail.label" args="[userInstance?.email]"/>									
				</g:link>
			</div>
		</g:isInRole>
		<g:if test="${params.action=='show'}">
			<div class="action-menu-item">
				<g:link controller="user" action="changePhoto"
					id="${userInstance?.id }">
					<img src="${createLinkTo(dir:'images/icons/silk',file:'photo.png')}" class="middle"/>&nbsp;
					<warehouse:message code="user.changePhoto.label" />
				</g:link>

			</div>
		</g:if>

	</div>
</span>