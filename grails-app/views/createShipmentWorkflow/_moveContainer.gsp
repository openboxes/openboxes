<script type="text/javascript">
	$(document).ready(function(){					
		$("#dlgMoveContainer").dialog({ autoOpen: true, modal: true, width: 800 });
	});			
</script>

<div id="dlgMoveContainer" title="${warehouse.message(code:'shipping.moveContainer.label')}" style="padding: 10px; display: none;" >
	<g:if test="${containerToMove}">
		<g:form name="moveContainer" action="createShipment">
		
			<g:hiddenField name="container.id" value="${containerToMove?.id }"/>
			<table>
				<tbody>
					<tr class="prop">
						<td valign="top" class="name">
							<label><format:metadata obj="${containerToMove?.containerType}"/></label>
						</td>                            
						<td valign="top" class="value">
							${containerToMove?.shipment?.name } &nbsp;&rsaquo;&nbsp;${containerToMove?.name }
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name">
							<label><warehouse:message code="default.to.label"/></label>
						</td>                            
						<td valign="top" class="value">
							<g:select optionKey="id" optionValue="name" class="chzn-select-deselect"
								name="shipment.id" id="shipment" from="${shipments}"/>
						</td>
					</tr>
					<tr>
						<td class="name"></td>
						<td>
							<div class="buttons left">
								<g:submitButton name="moveContainerToShipment" value="${warehouse.message(code:'default.button.move.label')}"></g:submitButton>
								<button name="cancelDialog" type="reset" onclick="$('#dlgMoveContainer').dialog('close');"><warehouse:message code="default.button.cancel.label"/></button>
							</div>
						</td>
					</tr>
				</tbody>
			</table>
		</g:form>														
	</g:if>
</div>		
		
