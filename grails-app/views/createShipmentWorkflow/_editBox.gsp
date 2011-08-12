<script type="text/javascript">
	$(document).ready(function(){									
		$("#dlgEditBox").dialog({ autoOpen: true, modal: true, width: '600px' });				
	});
</script>	   
	<div id="dlgEditBox" title="${warehouse.message(code:'shipping.editABox.message')}" style="padding: 10px; display: none;" >


	<jqvalui:renderValidationScript for="org.pih.warehouse.shipping.Container" form="editBox"/>
	<g:form name="editBox" action="createShipment">
		<table>
			<tbody>
				<g:render template="containerFields" model="['box':boxToEdit]"/>
				
			
				<g:if test="${addBoxToContainerId}">
					<g:hiddenField name="container.id" value="${addBoxToContainerId}"/>
				</g:if>
		
				
				<g:if test="${boxToEdit}">
					<g:hiddenField name="box.id" value="${boxToEdit.id}"/>
				</g:if>
				<tr>
					<td></td>
					<td style="text-align: left;">
						<div class="buttons">
							<g:submitButton name="saveBox" value="${warehouse.message(code:'shipping.button.saveBox.label')}"></g:submitButton>
							<g:if test="${boxToEdit}">
								<g:submitButton name="deleteBox" value="${warehouse.message(code:'shipping.button.removeBox.label')}" onclick="return confirm('${warehouse.message(code:'shipping.confirm.deleteBox.message')}')"></g:submitButton>
							</g:if>
							<button name="cancelDialog" type="reset" onclick="$('#dlgEditBox').dialog('close');"><warehouse:message code="default.button.cancel.label"/></button>
						</div>

							<div class="buttons">
								<g:submitButton name="addItemToBox" value="${warehouse.message(code:'shipping.addItemToBox.label')}"></g:submitButton>
							</div>
	
						
						<!--  
						<g:if test="${addBoxToContainerId}">
							<div class="buttons">
								<g:submitButton name="addAnotherBox" value="Save Box and Add Another Box"></g:submitButton>
							</div>
						</g:if>
				
						
							<div class="buttons">
								<g:submitButton name="cloneBox" value="Clone Box"></g:submitButton> Quantity: <g:textField id="cloneQuantity" name="cloneQuantity" size="3" value="0"/> 
							</div>  		-->
					</td>
				</tr>
			</tbody>
		</table>
	</g:form>																	
</div>		
		     

