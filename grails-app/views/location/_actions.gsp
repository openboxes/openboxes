<%@ page import="org.pih.warehouse.auth.AuthService" %>
<span class="action-menu">
	<button class="action-btn" aria-label="Action">
		<img
			src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}"
			style="vertical-align: middle" />
	</button>
	<div class="actions" role="menu">
		<div class="action-menu-item" role="menuitem">
			<g:link class="list" action="list">
				<img src="${resource(dir:'images/icons/silk',file:'table.png')}" class="middle"/>&nbsp;
				<warehouse:message code="default.list.label" args="[warehouse.message(code:'locations.label').toLowerCase()]"/>
			</g:link>
		</div>
		<g:if test="${locationInstance?.id}">
			<div class="action-menu-item" role="menuitem">
				<g:link class="edit" action="edit" id="${locationInstance?.id}">
					<img src="${resource(dir:'images/icons/silk',file:'pencil.png')}" class="middle"/>&nbsp;
					${warehouse.message(code: 'default.edit.label', args: [warehouse.message(code:'location.label')])}
				</g:link>
			</div>
			<div class="action-menu-item" role="menuitem">
				<g:link controller="location" action="uploadLogo" id="${locationInstance?.id }">
					<img src="${resource(dir:'images/icons/silk',file:'photo_add.png')}" class="middle"/>&nbsp;
					<warehouse:message code="location.uploadLogo.label" />
				</g:link>
			</div>
			<div class="action-menu-item" role="menuitem">
				<g:link class="delete" action="delete" id="${locationInstance?.id}"
					onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
					<img src="${resource(dir:'images/icons/silk',file:'delete.png')}" class="middle"/>&nbsp;
					${warehouse.message(code: 'default.delete.label', args: [warehouse.message(code:'location.label')])}
				</g:link>
			</div>
            <g:isSuperuser>
                <g:set var="templates" value="${org.pih.warehouse.core.Document.findAllByDocumentCode(org.pih.warehouse.core.DocumentCode.ZEBRA_TEMPLATE)}"/>
                <g:each in="${templates}" var="template">
                    <g:if test="${template?.documentNumber && template?.documentNumber in ['barcode:location']}">
                        <hr/>
                        <div class="action-menu-item">
                            <g:link controller="document" action="printZebraTemplate" id="${template.id}" params="['location.id': locationInstance.id, protocol: 'raw']">
                                <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}"/>&nbsp;
                                <g:message code="default.print.label" args="[template.name]"/>
                            </g:link>
                        </div>

                        <div class="action-menu-item">
                            <g:link controller="document" action="buildZebraTemplate" id="${template.id}" params="['location.id': locationInstance?.id]" target="_blank">
                                <img src="${createLinkTo(dir: 'images/icons/silk', file: 'brick.png')}"/>&nbsp;
                                <g:message code="default.build.label" args="[template.name]"/>
                            </g:link>
                        </div>
                        <div class="action-menu-item">
                            <g:link controller="document" action="renderZebraTemplate" id="${template.id}" params="['location.id': locationInstance?.id]" target="_blank">
                                <img src="${resource(dir: 'images/icons', file: 'barcode.png')}"/>&nbsp;
                                <g:message code="default.render.label" args="[template.name]"/>
                            </g:link>
                        </div>
                        <div class="action-menu-item">
                            <g:link controller="document" action="exportZebraTemplate" id="${template.id}" params="['location.id': locationInstance?.id]" target="_blank">
                                <img src="${resource(dir: 'images/icons/silk', file: 'application_link.png')}"/>&nbsp;
                                <g:message code="default.export.label" args="[template.name]"/>
                            </g:link>
                        </div>
                    </g:if>
                </g:each>
            </g:isSuperuser>
		</g:if>
	</div>
</span>
