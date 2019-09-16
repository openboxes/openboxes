<script type="text/javascript">
	$(document).ready(function(){									
		$("#dlgAddShipper").dialog({ 
			autoOpen: true, 
			modal: true, 
			open: function(event, ui){ $('body').css('overflow','hidden');$('.ui-widget-overlay').css('width','100%'); }, 
		    close: function(event, ui){ $('body').css('overflow','auto'); } 
		});
	});
</script>	   
<div id="dlgAddShipper" title="${warehouse.message(code:'default.add.label', args: [warehouse.message(code: 'shipper.label')])}" style="padding: 10px; display: none;" >
	<jqvalui:renderValidationScript for="org.pih.warehouse.shipping.Shipper" form="addShipper"
		renderErrorsOnTop="true"/>

	<jqvalui:renderErrors/>			

		
	<g:form name="addShipper" action="createShipment">
		<table>
			<tbody>
				<g:if test="${shipperInstance}">
					<g:hiddenField name="location.id" value="${shipperInstance.id}"/>
				</g:if>
				
				<g:hiddenField name="target" value="${params.target}"/>
				<tr class="prop">
					<td class="name">
						<label for="name">
							<warehouse:message code="shipper.name.label"/>
						</label>
					</td>
					<td class="value">
						<g:textField name="name" value="${shipperInstance.name}"/>
					</td>
				</tr>
				<tr class="prop">
					<td class="name">
						<label for="name">
							<warehouse:message code="shipper.description.label"/>
						</label>
					</td>
					<td class="value">
						<g:textArea name="description" value="${shipperInstance.description}"/>
					</td>
				</tr>
				<tr class="prop">
					<td class="name"></td>
					<td class="value">
						<div class="left">
							<g:submitButton name="saveShipper" value="${warehouse.message(code:'default.button.save.label')}"/>
							<button name="cancelDialog" type="reset" onclick="$('#dlgAddShipper').dialog('close');">
								<warehouse:message code="default.button.cancel.label"/>
							</button>
						</div>
					</td>
				</tr>
			</tbody>
		</table>
	</g:form>																	
</div>		
		     

