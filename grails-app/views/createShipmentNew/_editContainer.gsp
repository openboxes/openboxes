<script type="text/javascript">
	$(document).ready(function(){								
		$("#dlgEditContainer").dialog({ autoOpen: true, modal: true, width: '600px' });						
	});
</script>	   
	<div id="dlgEditContainer" title="${container ? 'Edit ' + container?.containerType?.name : 'Add ' + containerTypeToAdd?.name}" style="padding: 10px; display: none;" >
	

	<g:form name="dlgEditContainer" action="createShipment">
		
		<g:if test="${container}">
			<g:hiddenField name="container.id" value="${container?.id}"/>
		</g:if>
		<g:else>
			<g:hiddenField name="containerTypeToAddId" value="${containerTypeToAdd.id}"/>
		</g:else>
		
		<table>
			<tbody>
				<g:render template="containerFields" model="['container':container]"/>
				<tr>
					<td></td>
					<td style="text-align: left;">
						<div class="buttons">
							<g:submitButton name="saveContainer" value="Save ${container ? container?.containerType?.name : containerTypeToAdd?.name}"></g:submitButton>
							<g:if test="${container}">
								<g:submitButton name="deleteContainer" value="Remove ${container ? container?.containerType?.name : containerTypeToAdd?.name}"></g:submitButton>
							</g:if>
							<button name="cancelDialog" type="reset" onclick="$('#dlgEditContainer').dialog('close');">Cancel</button>
						</div>
						<div class="buttons">
							<g:submitButton name="addBoxToContainer" value="Add a Box to this ${container ? container?.containerType?.name : containerTypeToAdd?.name}"></g:submitButton>
						</div>
						<div class="buttons">
							<g:submitButton name="addItemToContainer" value="Add an Item to this ${container ? container?.containerType?.name : containerTypeToAdd?.name}"></g:submitButton>
						</div>
					</td>
				</tr>
			</tbody>
		</table>
	</g:form>																	
</div>		
		     

