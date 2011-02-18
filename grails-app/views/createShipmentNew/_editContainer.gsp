<g:if test="${containerInstance}">
	<g:set var="formName" value="EditContainer-${containerInstance?.id}" />
</g:if>
<g:else>
	<g:set var="formName" value="AddContainer-${type}" />
</g:else>

<script type="text/javascript">
	$(document).ready(function(){
		$("#btn${formName}").click(function() { $("#dlg${formName}").dialog('open'); });									
		$("#dlg${formName}").dialog({ autoOpen: false, modal: true, width: '600px' });						
	});
</script>	   
	<div id="dlg${formName}" title="${containerInstance ? 'Edit ' + containerInstance?.containerType?.name : 'Add ' + type}" style="padding: 10px; display: none;" >
	
	<jqvalui:renderValidationScript for="org.pih.warehouse.shipping.Container" form="${formName}"/>
	<g:form name="${formName}" action="createShipment">
		
		<g:if test="${containerInstance}">
			<g:hiddenField name="container.id" value="${containerInstance?.id }"/>
		</g:if>
		<g:else>
			<g:hiddenField name="type" value="${type}"/>
		</g:else>
		
		<table>
			<tbody>
				<g:render template="containerFields" model="['containerInstance':containerInstance]"/>
				<tr>
					<td></td>
					<td style="text-align: left;">
						<div class="buttons">
							<g:submitButton name="saveContainer" value="Save ${containerInstance ? containerInstance?.containerType?.name : type}"></g:submitButton>
							<g:if test="${containerInstance}">
								<g:submitButton name="deleteContainer" value="Remove ${containerInstance ? containerInstance?.containerType?.name : type}"></g:submitButton>
							</g:if>
							<button name="cancelDialog" type="reset" onclick="$('#dlg${formName}').dialog('close');">Cancel</button>
						</div>
						<div class="buttons">
							<g:submitButton name="addBoxToContainer" value="Add a Box to this ${containerInstance ? containerInstance?.containerType?.name : type}"></g:submitButton>
						</div>
						<div class="buttons">
							<g:submitButton name="addItemToContainer" value="Add an Item to this ${containerInstance ? containerInstance?.containerType?.name : type}"></g:submitButton>
						</div>
					</td>
				</tr>
			</tbody>
		</table>
	</g:form>																	
</div>		
		     

