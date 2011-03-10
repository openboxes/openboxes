<script type="text/javascript">
	$(document).ready(function(){									
		$("#dlgEditBox").dialog({ autoOpen: true, modal: true, width: '600px' });				
	});
</script>	   
	<div id="dlgEditBox" title="Edit a Box" style="padding: 10px; display: none;" >


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
							<g:submitButton name="saveBox" value="Save Box"></g:submitButton>
							<g:if test="${boxToEdit}">
								<g:submitButton name="deleteBox" value="Remove Box" onclick="return confirm('Are you sure you want to delete this box?')"></g:submitButton>
							</g:if>
							<button name="cancelDialog" type="reset" onclick="$('#dlgEditBox').dialog('close');">Cancel</button>
						</div>

							<div class="buttons">
								<g:submitButton name="addItemToBox" value="Add an Item to this Box"></g:submitButton>
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
		     

