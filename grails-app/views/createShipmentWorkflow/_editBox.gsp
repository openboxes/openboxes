<script type="text/javascript">
	$(document).ready(function(){									
		$("#dlgEditBox").dialog({ autoOpen: true, modal: true, width: 800 });
	});
</script>	   
	<div id="dlgEditBox" title="${boxtoEdit ? warehouse.message(code:'shipping.editBox.label') : warehouse.message(code:'shipping.addBox.label')}" style="padding: 10px; display: none;" >

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
			</tbody>
		</table>
		<div class="buttons">
			<g:submitButton name="saveBox" value="${warehouse.message(code:'shipping.button.saveBox.label')}" class="button"></g:submitButton>
			<g:if test="${boxToEdit}">
				<g:submitButton name="deleteBox" value="${warehouse.message(code:'shipping.button.removeBox.label')}" onclick="return confirm('${warehouse.message(code:'shipping.confirm.deleteBox.message')}')" class="button"></g:submitButton>
			</g:if>
			<button name="cancelDialog" type="reset" onclick="$('#dlgEditBox').dialog('close');" class="button"><warehouse:message code="default.button.cancel.label"/></button>
		</div>
		<div class="buttons">
			<g:submitButton name="addItemToBox" value="${warehouse.message(code:'shipping.addItemToBox.label')}" class="button"></g:submitButton>
		</div>
	</g:form>
</div>		
		     

