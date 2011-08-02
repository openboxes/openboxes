<div id="add-person-dialog" title="Add a new recipient" style="display: none;" >
	<ul id="people"></ul>
	
	<g:form name="addPersonForm"
		url="${[controller: 'shipment', action:'savePerson']}">
		<g:hiddenField name="id" value="0" />
		<table>
			<tbody>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message
						code="person.firstName.label" default="First Name" /></label></td>
					<td valign="top"
						class="value ${hasErrors(bean: personInstance, field: 'firstName', 'errors')}">
					<g:textField id="firstName" name="firstName" size="15" /></td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message
						code="person.lastName.label" default="Last Name" /></label></td>
					<td valign="top"
						class="value ${hasErrors(bean: personInstance, field: 'lastName', 'errors')}">
					<g:textField id="lastName" name="lastName" size="15" /></td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name"><label><warehouse:message
						code="person.email.label" default="Email" /></label></td>
					<td valign="top"
						class="value ${hasErrors(bean: personInstance, field: 'email', 'errors')}">
					<g:textField id="email" name="email" size="15" /></td>
				</tr>
			</tbody>
		</table>
	</g:form>
</div>		