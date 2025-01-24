
<%@ page import="org.pih.warehouse.core.IdentifierTypeCode; org.pih.warehouse.core.Organization" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'organization.label', default: 'Organization')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message" role="status" aria-label="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${organizationInstance}">
	            <div class="errors" role="alert" aria-label="error-message">
	                <g:renderErrors bean="${organizationInstance}" as="list" />
	            </div>
            </g:hasErrors>
			<g:isSuperuser><g:set var="isSuperuser" value="${true}"/></g:isSuperuser>

			<div class="button-bar">
                <g:link class="button" action="list">
                    <img src="${resource(dir: 'images/icons/silk', file: 'application_side_list.png')}" />&nbsp;
                    <warehouse:message code="default.list.label" args="[g.message(code:'organizations.label')]"/>
                </g:link>
                <g:link class="button" action="create" params="[partyType:'ORG']">
                    <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                    <warehouse:message code="default.add.label" args="[g.message(code:'organization.label')]"/>
                </g:link>
            </div>
			<g:form method="post" onsubmit="return validateForm();">
				<g:hiddenField name="id" value="${organizationInstance?.id}" />
				<g:hiddenField name="version" value="${organizationInstance?.version}" />
				<div class="box">
					<h2><warehouse:message code="default.edit.label" args="[entityName]" /></h2>
					<table>
						<tbody>
							<tr class="prop">
								<td valign="top" class="name">
									<label for="active"><warehouse:message code="user.active.label" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: organizationInstance, field: 'active', 'errors')}">
									<g:checkBox name="active" value="${organizationInstance?.active}" id="active" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="organization.id"><warehouse:message code="default.id.label" default="ID" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: organizationInstance, field: 'id', 'errors')}">
									<g:textField name="organization.id" value="${organizationInstance?.id}" class="text large readonly"  disabled="disabled"/>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="code"><warehouse:message code="organization.code.label" default="Code" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: organizationInstance, field: 'code', 'errors')}">
									<g:if test="${!organizationInstance.hasPurchaseOrders() || isSuperuser}">
										<g:textField class="text large" name="code" maxlength="255" value="${organizationInstance?.code}" />
									</g:if>
									<g:else>
										<g:textField class="text large readonly" name="code" maxlength="255" value="${organizationInstance?.code}" disabled="disabled"/>
									</g:else>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="name"><warehouse:message code="organization.name.label" default="Name" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: organizationInstance, field: 'name', 'errors')}">
									<g:textField class="text large" name="name" maxlength="255" value="${organizationInstance?.name}" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="description"><warehouse:message code="organization.description.label" default="Description" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: organizationInstance, field: 'description', 'errors')}">
									<g:textArea class="text" name="description" maxlength="255" value="${organizationInstance?.description}" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="roles"><warehouse:message code="organization.roles.label" default="Roles" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: organizationInstance, field: 'roles', 'errors')}">
									<ul aria-label="Added Roles">
										<g:each in="${organizationInstance?.roles?}" var="r">
											<li><g:link controller="partyRole" action="show" id="${r.id}">${r}</g:link></li>
										</g:each>
									</ul>
									<g:link controller="partyRole" action="create" params="['party.id': organizationInstance?.id]">${warehouse.message(code: 'default.add.label', args: [warehouse.message(code: 'partyRole.label', default: 'PartyRole')])}</g:link>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  <label for="partyType.id"><warehouse:message code="organization.partyType.label" default="Party Type" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: organizationInstance, field: 'partyType', 'errors')}">
									<div data-testid="party-type-select">
										<g:select class="chzn-select-deselect" name="partyType.id" from="${org.pih.warehouse.core.PartyType.list()}" optionKey="id" value="${organizationInstance?.partyType?.id}"  />
									</div>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
									<label for="defaultLocation"><warehouse:message code="organization.defaultLocation.label" default="Default Location" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: organizationInstance, field: 'defaultLocation', 'errors')}">
									<div data-testid="default-location-select">
										<g:selectLocation name="defaultLocation"
														  from="${organizationInstance?.locations}"
														  class="chzn-select-deselect"
														  value="${organizationInstance?.defaultLocation?.id}"
														  noSelection="['':'']"
										/>
									</div>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
								  	<label for="sequencesReadOnly"><warehouse:message code="organization.sequences.label" default="Sequences" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: organizationInstance, field: 'sequences', 'errors')}">
									<g:textField name="sequencesReadOnly" value="${organizationInstance?.sequences?:message(code:'default.none.label')}" class="text large readonly" disabled="disabled"/>
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
								  	<label for="maxPurchaseOrderNumber"><warehouse:message code="organization.maxPurchaseOrderNumber.label" default="Last PO Number" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: organizationInstance, field: 'maxPurchaseOrderNumber', 'errors')}">
									<g:textField name="maxPurchaseOrderNumber" value="${organizationInstance?.maxPurchaseOrderNumber()?:message(code:'default.none.label')}" class="text large readonly" disabled="disabled"/>
								</td>
							</tr>
							<g:if test="${isSuperuser}">
								<g:each var="identifierTypeCode" in="${org.pih.warehouse.core.IdentifierTypeCode.values()}">
									<tr class="prop">
										<td valign="top" class="name">
										  <label for="sequences.${identifierTypeCode}">${identifierTypeCode}
										  </label>
										</td>
										<td valign="top" class="value ${hasErrors(bean: organizationInstance, field: 'sequences', 'errors')}">
											<g:textField name="sequences.${identifierTypeCode}" value="${organizationInstance?.sequences[identifierTypeCode.toString()]}" class="text large"/>
										</td>
									</tr>
								</g:each>
							</g:if>
						</tbody>
						<tfoot>
							<tr class="prop">
								<td valign="top"></td>
								<td valign="top left">
                                    <g:actionSubmit class="button" action="update" value="${warehouse.message(code: 'default.button.update.label', default: 'Update')}"/>
                                    <g:actionSubmit class="button" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
								</td>
							</tr>
					</tfoot>
					</table>
				</div>
            </g:form>
        </div>
		<script type="text/javascript">
			function validateForm()  {
				const checked = ($("#active").attr("checked") === 'checked');
				const currentValue = $("#active").val();
				if (checked && currentValue) {
					return true;
				}
				return confirm('${warehouse.message(code:'organization.confirm.inactive.message')}')
			}
		</script>
    </body>
</html>
