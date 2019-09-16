<div id="dlgMoveDraggableItem" title="${warehouse.message(code:'shipping.moveItem.label')}" style="display: none;" >
	<g:form name="moveItem" action="createShipment">
		<table>
			<tbody>
			
				<tr>
					<td>
					
					</td>
					<td>
					
					</td>
				</tr>
				
				<tr>
					<td colspan="2">
						<div class="buttons center">
							<g:submitButton name="moveItemToContainer" value="${warehouse.message(code:'default.button.move.label')}"></g:submitButton>
							<button name="cancelDialog" type="reset" onclick="$('#dlgMoveDraggableItem').dialog('close');"><warehouse:message code="default.button.cancel.label"/></button>
						</div>
					</td>
				</tr>
			</tbody>
		</table>
	</g:form>														
</div>		
		
