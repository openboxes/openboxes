<script type="text/javascript">
	$(document).ready(function(){									
		$("#dlgAddLocation").dialog({ 
			autoOpen: true, 
			modal: true, 
			open: function(event, ui){ $('body').css('overflow','hidden');$('.ui-widget-overlay').css('width','100%'); }, 
		    close: function(event, ui){ $('body').css('overflow','auto'); } 
		});
	});
</script>	   
<div id="dlgAddLocation" title="${warehouse.message(code:'default.add.label', args: [warehouse.message(code: 'location.label')])}" style="padding: 10px; display: none;" >

	<jqvalui:renderValidationScript for="org.pih.warehouse.core.Location" form="addLocation"
		renderErrorsOnTop="true"/>

	<jqvalui:renderErrors/>				

	<g:hasErrors bean="${locationInstance}">
		<div class="errors">
			<g:renderErrors bean="${locationInstance}" as="list" />
		</div>
	</g:hasErrors>	
			
	<g:form id="addLocation" name="addLocation" action="createShipment">
		<table>
			<tbody>
				<g:if test="${locationInstance}">
					<g:hiddenField name="location.id" value="${locationInstance.id}"/>
				</g:if>
				
				<g:hiddenField name="target" value="${params.target}"/>
				<tr class="prop">
					<td class="name">
						<label for="name">
							<warehouse:message code="location.locationType.label"/>
						</label>
					</td>
					<td class="value">
						<g:select name="locationType.id" from="${org.pih.warehouse.core.LocationType.list()}"
							optionKey="id" optionValue="${{format.metadata(obj:it)}}" value="${locationInstance?.locationType?.id}" noSelection="['':'']" />
	                                							
						
					</td>
				</tr>
				<tr class="prop">
					<td class="name">
						<label for="name">
							<warehouse:message code="location.name.label"/>
						</label>
					</td>
					<td class="value">
						<g:textField id="name" name="name" value="${locationInstance.name}"/>
					</td>
				</tr>
				<tr class="prop">
					<td class="name"></td>
					<td class="value">
						<div class="left">
							<g:submitButton name="saveLocation" value="${warehouse.message(code:'default.button.save.label')}"/>
							<button name="cancelDialog" type="reset" onclick="$('#dlgAddLocation').dialog('close');">
								<warehouse:message code="default.button.cancel.label"/>
							</button>
						</div>
					</td>
				</tr>
			</tbody>
		</table>
	</g:form>																	
</div>		
		     

