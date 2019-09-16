<script type="text/javascript">
	$(document).ready(function(){									
		$("#dlgAddPerson").dialog({ 
			autoOpen: true, 
			modal: true, 
			open: function(event, ui){ $('body').css('overflow','hidden');$('.ui-widget-overlay').css('width','100%'); }, 
		    close: function(event, ui){ $('body').css('overflow','auto'); } 
		});
	});
</script>	   
<div id="dlgAddPerson" title="${warehouse.message(code:'default.add.label', args: [warehouse.message(code: 'person.label')])}" style="padding: 10px; display: none;" >
	<jqvalui:renderValidationScript for="org.pih.warehouse.core.Person" form="addPerson"
		renderErrorsOnTop="true"/>

	<jqvalui:renderErrors/>			

		
	<g:form id="addPerson" name="addPerson" action="createShipment">
		<table>
			<tbody>
				<g:if test="${personInstance}">
					<g:hiddenField name="person.id" value="${personInstance.id}"/>
				</g:if>
				
				<g:hiddenField name="target" value="${params.target}"/>
				<tr class="prop">
					<td class="name">
						<label for="firstName">
							<warehouse:message code="person.firstName.label"/>
						</label>
					</td>
					<td class="value">
						<g:textField id="firstName" name="firstName" value="${personInstance.firstName}"/>
					</td>
				</tr>
				<tr class="prop">
					<td class="name">
						<label for="lastName">
							<warehouse:message code="person.lastName.label"/>
						</label>
					</td>
					<td class="value">
						<g:textField name="lastName" value="${personInstance.lastName }"/>
					</td>
				</tr>
				<tr class="prop">
					<td class="name">
						<label for="email">
							<warehouse:message code="person.email.label"/>
						</label>
					</td>
					<td class="value">
						<g:textField name="email" value="${personInstance.email }" />
					</td>
				</tr>
				<tr class="prop">
					<td class="name">
						<label for="phoneNumber">
							<warehouse:message code="person.phoneNumber.label"/>
						</label>
					</td>
					<td class="value">
						<g:textField name="phoneNumber" value="${personInstance.phoneNumber }"/>
					</td>
				</tr>
				<tr class="prop">
					<td class="name"></td>
					<td class="value">
						<div class="left">
							<g:submitButton name="savePerson" value="${warehouse.message(code:'default.button.save.label')}"/>
							<button name="cancelDialog" type="reset" onclick="$('#dlgAddPerson').dialog('close');">
								<warehouse:message code="default.button.cancel.label"/>
							</button>
						</div>
					</td>
				</tr>
			</tbody>
		</table>
	</g:form>																	
</div>		
		     

